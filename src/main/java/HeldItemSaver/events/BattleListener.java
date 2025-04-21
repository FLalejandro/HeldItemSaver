package HeldItemSaver.events;

import HeldItemSaver.configs.ModLogger;
import HeldItemSaver.util.HeldItemManager;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.events.battles.*;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.Unit;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BattleListener {
    private final HeldItemManager heldItemManager = new HeldItemManager();
    // Set to keep track of processed battles to avoid re-processing
    private Set<UUID> processedBattles = ConcurrentHashMap.newKeySet();

    /**
     * Constructor for BattleListener.
     * Registers this listener to various battle events in the game.
     */
    public BattleListener() {
        subscribeToBattleEvents();
    }

    /**
     * Subscribes to battle-related events in the game.
     * Listens for battle start, victory, and flee events.
     */
    private void subscribeToBattleEvents() {
        CobblemonEvents.BATTLE_STARTED_POST.subscribe(Priority.NORMAL, this::handleBattleStartedPostEvent);
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, this::handleBattleEndedEvent);
        CobblemonEvents.BATTLE_FLED.subscribe(Priority.NORMAL, this::handleBattleEndedEvent);
        // Uncomment if handling faint events is desired
        // CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.NORMAL, this::handleBattleEndedEvent);
    }

    /**
     * Handles the event when a battle starts.
     * It logs and processes the start of the battle.
     *
     * @param event The BattleStartedPostEvent object containing information about the started battle.
     * @return Unit.INSTANCE as required by Kotlin interfaces.
     */
    private Unit handleBattleStartedPostEvent(BattleStartedPostEvent event) {
        try {
            onBattleStartedPost(event);
        } catch (NoPokemonStoreException e) {
            ModLogger.error("Storage exception during battle start processing", e);
        }
        return Unit.INSTANCE;
    }

    /**
     * Handles the event when a battle ends, either by victory, fleeing, or fainting.
     * It determines the type of battle end event and processes it accordingly.
     *
     * @param event The BattleEvent object containing information about the ended battle.
     * @return Unit.INSTANCE as required by Kotlin interfaces.
     */
    private Unit handleBattleEndedEvent(BattleEvent event) {
        String eventType = event instanceof BattleVictoryEvent ? "Victory" :
                event instanceof BattleFledEvent ? "Flee" :
                        "Faint";
        ModLogger.info("Battle ended with " + eventType);

        try {
            processBattleEnd(event.getBattle());
        } catch (NoPokemonStoreException e) {
            ModLogger.error("Storage exception during battle " + eventType + " processing", e);
        }
        return Unit.INSTANCE;
    }

    /**
     * Processes the end of a battle.
     * Restores held items to players and logs the final state of items.
     *
     * @param battle The PokemonBattle object representing the concluded battle.
     * @throws NoPokemonStoreException If the player's Pokémon store cannot be accessed.
     */
    private void processBattleEnd(PokemonBattle battle) throws NoPokemonStoreException {
        if (!processedBattles.add(battle.getBattleId())) {
            ModLogger.debug("Battle already processed, skipping: {}", battle.getBattleId());
            return;
        }

        ModLogger.info("Processing end of battle: {}", battle);

        for (PlayerBattleActor actor : getPlayerBattleActors(battle)) {
            processPlayerBattleEnd(actor);
        }
    }

    /**
     * Processes the start of a battle.
     * Stores held items for all players involved in the battle.
     *
     * @param event The BattleStartedPostEvent object containing information about the started battle.
     * @throws NoPokemonStoreException If the player's Pokémon store cannot be accessed.
     */
    private void onBattleStartedPost(BattleStartedPostEvent event) throws NoPokemonStoreException {
        ModLogger.info("Processing start of a battle");

        // Retrieves the list of players involved in the battle
        List<ServerPlayerEntity> players = event.getBattle().getPlayers();
        for (ServerPlayerEntity player : players) {
            UUID playerUUID = player.getUuid();
            // Retrieves the player's party store
            PlayerPartyStore partyStore = Cobblemon.INSTANCE.getStorage().getParty(player);

            ModLogger.debug("Storing held items for player {} (UUID: {})", player.getName().getString(), playerUUID);

            // Allocates space for held items including empty slots
            ItemStack[] heldItems = new ItemStack[partyStore.size()];
            for (int i = 0; i < partyStore.size(); i++) {
                Pokemon pokemon = partyStore.get(i);
                // Stores the held item or an empty item if no Pokémon is present in the slot
                heldItems[i] = (pokemon != null) ? pokemon.heldItem() : ItemStack.EMPTY;

                // Logs the start item for each slot
                ModLogger.info("Slot {} - Start Item: {}", i, heldItems[i] != ItemStack.EMPTY ? heldItems[i].getItem().toString() : "null");
            }

            // Stores the held items for the player before the battle starts
            heldItemManager.storeHeldItemsBeforeBattle(playerUUID, heldItems);
            ModLogger.info("Held items stored for player {} (UUID: {})", player.getName().getString(), playerUUID);
        }
    }

    private List<PlayerBattleActor> getPlayerBattleActors(PokemonBattle battle) {
        return StreamSupport.stream(battle.getActors().spliterator(), false)
                .filter(PlayerBattleActor.class::isInstance)
                .map(PlayerBattleActor.class::cast)
                .collect(Collectors.toList());
    }

    private void processPlayerBattleEnd(PlayerBattleActor actor) throws NoPokemonStoreException {
        for (UUID playerUUID : actor.getPlayerUUIDs()) {
            ServerPlayerEntity player = actor.getEntity();
            ModLogger.debug("Processing battle end for player UUID: {}", playerUUID);
            restorePlayerHeldItems(player, playerUUID);
        }
    }

    private void restorePlayerHeldItems(ServerPlayerEntity player, UUID playerUUID) throws NoPokemonStoreException {
        PlayerPartyStore partyStore = Cobblemon.INSTANCE.getStorage().getParty(player);
        if (partyStore == null) {
            ModLogger.error("Party store not found for player UUID: {}", playerUUID);
            return;
        }

        List<Pokemon> partyPokemon = new ArrayList<>();
        for (int i = 0; i < partyStore.size(); i++) {
            Pokemon pokemon = partyStore.get(i);
            String endItemDesc = (pokemon != null && pokemon.heldItem() != null) ? pokemon.heldItem().getItem().toString() : "null";
            ModLogger.info("Slot {} - End Item: {}", i, endItemDesc);
            partyPokemon.add(pokemon);
        }

        heldItemManager.restoreHeldItems(playerUUID, partyPokemon);
        logFinalItemStates(partyStore, playerUUID);
    }

    private void logFinalItemStates(PlayerPartyStore partyStore, UUID playerUUID) {
        for (int i = 0; i < partyStore.size(); i++) {
            Pokemon pokemon = partyStore.get(i);
            String finalItemDesc = (pokemon != null && pokemon.heldItem() != null) ? pokemon.heldItem().getItem().toString() : "null";
            ModLogger.info("Final state - Slot {} - Item: {}", i, finalItemDesc);
        }
        ModLogger.info("Restored held items for player UUID: {}", playerUUID);
    }


}
