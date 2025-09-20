package me.novoro.helditemsaver.events;

import kotlin.Unit;
import me.novoro.helditemsaver.config.SettingsManager;
import me.novoro.helditemsaver.util.HeldItemSaverLogger;
import me.novoro.helditemsaver.util.HeldItemManager;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleEvent;
import com.cobblemon.mod.common.api.events.battles.BattleFledEvent;
import com.cobblemon.mod.common.api.events.battles.BattleStartedEvent;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

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
    private final Set<UUID> processedBattles = ConcurrentHashMap.newKeySet();

    /**
     * Registers this listener to various battle and held item events in the game.
     */
    public void BattleEventListener() {
        CobblemonEvents.BATTLE_STARTED_POST.subscribe(Priority.NORMAL, this::handleBattleStartedEvent);
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, this::handleBattleEndedEvent);
        CobblemonEvents.BATTLE_FLED.subscribe(Priority.NORMAL, this::handleBattleEndedEvent);
    }

    /**
     * Handles the event when a battle starts.
     * It logs and processes the start of the battle.
     *
     * @param event The BattleStartedEvent object containing information about the started battle.
     */
    private Unit handleBattleStartedEvent(BattleStartedEvent.Post event) {
        try {
            onBattleStartedPost(event);
        } catch (NoPokemonStoreException e) {
            if (SettingsManager.areLogsEnabled()) {
                HeldItemSaverLogger.error("Storage exception during battle start processing");
                HeldItemSaverLogger.printStackTrace(e);
            }
        }
        return Unit.INSTANCE;
    }

    /**
     * Handles the event when a battle ends, either by victory, fleeing, or fainting.
     * It determines the type of battle end event and processes it accordingly.
     *
     * @param event The BattleEvent object containing information about the ended battle.
     */
    private Unit handleBattleEndedEvent(BattleEvent event) {
        String eventType = event instanceof BattleVictoryEvent ? "Victory" :
                event instanceof BattleFledEvent ? "Flee" :
                        "Faint";
        if (SettingsManager.areLogsEnabled()) {
            HeldItemSaverLogger.info("Battle ended with " + eventType);
        }

        try {
            processBattleEnd(event.getBattle());
        } catch (NoPokemonStoreException e) {
            if (SettingsManager.areLogsEnabled()) {
                HeldItemSaverLogger.error("Storage exception during battle " + eventType + " processing");
                HeldItemSaverLogger.printStackTrace(e);
            }
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
            if (SettingsManager.areLogsEnabled()) {
                HeldItemSaverLogger.error("Battle already processed, skipping: {}", battle.getBattleId());
            }
            return;
        }

        for (PlayerBattleActor actor : getPlayerBattleActors(battle)) {
            processPlayerBattleEnd(actor);
        }
    }

    /**
     * Processes the start of a battle.
     * Stores held items for all players involved in the battle.
     *
     * @param event The BattleStartedEvent object containing information about the started battle.
     * @throws NoPokemonStoreException If the player's Pokémon store cannot be accessed.
     */
    private void onBattleStartedPost(BattleStartedEvent.Post event) throws NoPokemonStoreException {
        HeldItemSaverLogger.info("Processing start of a battle");

        // Retrieves the list of players involved in the battle
        List<ServerPlayerEntity> players = event.getBattle().getPlayers();
        for (ServerPlayerEntity player : players) {
            UUID playerUUID = player.getUuid();
            // Retrieves the player's party store
            PlayerPartyStore partyStore = Cobblemon.INSTANCE.getStorage().getParty(player);

            //HeldItemSaverLogger.info("Storing held items for player {} (UUID: {})", player.getName().getString(), playerUUID);

            // Allocates space for held items including empty slots
            ItemStack[] heldItems = new ItemStack[partyStore.size()];
            for (int i = 0; i < partyStore.size(); i++) {
                Pokemon pokemon = partyStore.get(i);
                // Stores the held item or an empty item if no Pokémon is present in the slot
                heldItems[i] = (pokemon != null) ? pokemon.heldItem() : ItemStack.EMPTY;

                // Logs the start item for each slot
                //HeldItemSaverLogger.info("Slot {} - Start Item: {}", i, heldItems[i] != ItemStack.EMPTY ? heldItems[i].getItem().toString() : "null");
            }

            // Stores the held items for the player before the battle starts
            heldItemManager.storeHeldItemsBeforeBattle(playerUUID, heldItems);
            //HeldItemSaverLogger.info("Held items stored for player {} (UUID: {})", player.getName().getString(), playerUUID);
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
            if (SettingsManager.areLogsEnabled()) {
                HeldItemSaverLogger.error("Processing battle end for player UUID: {}", playerUUID);
            }
            restorePlayerHeldItems(player, playerUUID);
        }
    }

    private void restorePlayerHeldItems(ServerPlayerEntity player, UUID playerUUID) {
        if (player == null) {
            HeldItemSaverLogger.error("Player (UUID: {}) unavailable to restore items.", playerUUID);
            return;
        }
        PlayerPartyStore partyStore = Cobblemon.INSTANCE.getStorage().getParty(player);

        if (partyStore == null) {
            HeldItemSaverLogger.error("Party store not found for player UUID: {}", playerUUID);
            return;
        }

        List<Pokemon> partyPokemon = new ArrayList<>();
        for (int i = 0; i < partyStore.size(); i++) {
            Pokemon pokemon = partyStore.get(i);
            String endItemDesc = (pokemon != null && pokemon.heldItem() != null) ? pokemon.heldItem().getItem().toString() : "null";
            //HeldItemSaverLogger.info("Slot {} - End Item: {}", i, endItemDesc);
            partyPokemon.add(pokemon);
        }

        heldItemManager.restoreHeldItems(playerUUID, partyPokemon);
        logFinalItemStates(partyStore, playerUUID);
    }

    private void logFinalItemStates(PlayerPartyStore partyStore, UUID playerUUID) {
        for (int i = 0; i < partyStore.size(); i++) {
            Pokemon pokemon = partyStore.get(i);
            String finalItemDesc = (pokemon != null && pokemon.heldItem() != null) ? pokemon.heldItem().getItem().toString() : "null";
            //HeldItemSaverLogger.info("Final state - Slot {} - Item: {}", i, finalItemDesc);
        }

        if (SettingsManager.areLogsEnabled()) {
            HeldItemSaverLogger.error("Restored held items for player UUID: {}", playerUUID);
        }
    }


}
