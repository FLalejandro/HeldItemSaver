package me.novoro.helditemsaver.util;

import me.novoro.helditemsaver.config.SettingsManager;
import me.novoro.helditemsaver.util.HeldItemSaverLogger;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HeldItemManager {

    // Unified map for storing held items before battle for both players' and wild Pokémon's UUIDs
    private final Map<UUID, ItemStack[]> heldItemsBeforeBattle = new ConcurrentHashMap<>();

    /**
     * Stores the held items of Pokémon before a battle begins.
     * Can handle both player Pokémon (array of items) and wild Pokémon (single item).
     *
     * @param uuid      The UUID of the player or Pokémon.
     * @param heldItems The held items to store. For a player, this is an array; for a wild Pokémon, a single-item array.
     */
    public void storeHeldItemsBeforeBattle(UUID uuid, ItemStack[] heldItems) {
        if (heldItems == null) {
            if (SettingsManager.areLogsEnabled()) {
                HeldItemSaverLogger.error("Failed to store held items: 'heldItems' is null for UUID {}", uuid);
            }
            return;
        }
        heldItemsBeforeBattle.put(uuid, heldItems);
        if (SettingsManager.areLogsEnabled()) {
            HeldItemSaverLogger.error("Stored held items for UUID {}", uuid);
        }
    }

    /**
     * Restores the held items of Pokémon after a battle.
     * Can handle both player Pokémon (array of items) and wild Pokémon (single item).
     *
     * @param uuid     The UUID of the player or Pokémon.
     * @param pokemons The list of Pokémon to restore items to. For a player, multiple Pokémon; for a wild Pokémon, a single-item list.
     */
    public void restoreHeldItems(UUID uuid, List<Pokemon> pokemons) {
        ItemStack[] heldItemsBefore = heldItemsBeforeBattle.get(uuid);
        if (heldItemsBefore == null) {
            if (SettingsManager.areLogsEnabled()) {
                HeldItemSaverLogger.error("Restoration failed: Missing data for UUID {}", uuid);
            }
            return;
        }

        for (int i = 0; i < pokemons.size(); i++) {
            Pokemon pokemon = pokemons.get(i);
            if (pokemon == null) continue;

            ItemStack currentHeldItem = pokemon.heldItem();
            if (!ItemStack.areItemsEqual(heldItemsBefore[i], currentHeldItem)) {
                pokemon.swapHeldItem(heldItemsBefore[i], false, false);
                if (SettingsManager.areLogsEnabled()) {
                    HeldItemSaverLogger.error("Restored held item for UUID {}", uuid);
                }
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
        if (SettingsManager.areLogsEnabled()) {
            HeldItemSaverLogger.error("Clearing stored held items for UUID {}", uuid);
            if (heldItemsBeforeBattle.remove(uuid) == null) {
                if (SettingsManager.areLogsEnabled()) {
                    HeldItemSaverLogger.error("Attempted to clear non-existent held items for UUID {}", uuid);
                }
            }
        }
    }
}