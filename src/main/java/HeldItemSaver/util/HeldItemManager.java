package HeldItemSaver.util;

import HeldItemSaver.configs.ModLogger;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HeldItemManager {

    // Unified map for storing held items before battle for both players' and wild Pokémon's UUIDs
    private Map<UUID, ItemStack[]> heldItemsBeforeBattle = new ConcurrentHashMap<>();

    /**
     * Stores the held items of Pokémon before a battle begins.
     * Can handle both player Pokémon (array of items) and wild Pokémon (single item).
     *
     * @param uuid The UUID of the player or Pokémon.
     * @param heldItems The held items to store. For a player, this is an array; for a wild Pokémon, a single-item array.
     */
    public void storeHeldItemsBeforeBattle(UUID uuid, ItemStack[] heldItems) {
        if (heldItems == null) {
            ModLogger.error("Failed to store held items: 'heldItems' is null for UUID {}", uuid);
            return;
        }
        heldItemsBeforeBattle.put(uuid, heldItems);
        ModLogger.debug("Stored held items for UUID {}", uuid);
    }

    /**
     * Restores the held items of Pokémon after a battle.
     * Can handle both player Pokémon (array of items) and wild Pokémon (single item).
     *
     * @param uuid The UUID of the player or Pokémon.
     * @param pokemons The list of Pokémon to restore items to. For a player, multiple Pokémon; for a wild Pokémon, a single-item list.
     */
    public void restoreHeldItems(UUID uuid, List<Pokemon> pokemons) {
        ItemStack[] heldItemsBefore = heldItemsBeforeBattle.get(uuid);
        if (heldItemsBefore == null) {
            ModLogger.error("Restoration failed: Missing data for UUID {}", uuid);
            return;
        }

        for (int i = 0; i < pokemons.size(); i++) {
            Pokemon pokemon = pokemons.get(i);
            if (pokemon == null) continue;

            ItemStack currentHeldItem = pokemon.heldItem();
            if (!ItemStack.areItemsEqual(heldItemsBefore[i], currentHeldItem)) {
                pokemon.swapHeldItem(heldItemsBefore[i], false);
                ModLogger.debug("Restored held item for UUID {}", uuid);
            }
        }

        clearHeldItems(uuid);
    }

    /**
     * Clears the stored held items for a specific UUID after they have been restored.
     *
     * @param uuid The UUID of the player or Pokémon.
     */
    public void clearHeldItems(UUID uuid) {
        ModLogger.debug("Clearing stored held items for UUID {}", uuid);
        if (heldItemsBeforeBattle.remove(uuid) == null) {
            ModLogger.warn("Attempted to clear non-existent held items for UUID {}", uuid);
        }
    }
}