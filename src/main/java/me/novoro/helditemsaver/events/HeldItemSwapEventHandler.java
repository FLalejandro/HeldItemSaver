package me.novoro.helditemsaver.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.pokemon.Pokemon;
import me.novoro.helditemsaver.HeldItemSaver;
import me.novoro.helditemsaver.config.LangManager;
import me.novoro.helditemsaver.config.SettingsManager;
import me.novoro.helditemsaver.util.ColorUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import com.cobblemon.mod.common.api.events.pokemon.HeldItemEvent;
import kotlin.Unit;
import java.util.Map;
import java.util.UUID;

public class HeldItemSwapEventHandler {

    public static void register() {
        CobblemonEvents.HELD_ITEM_PRE.subscribe(Priority.HIGHEST, event -> {

            Pokemon pokemon = event.getPokemon();
            UUID ownerUuid = pokemon.getOwnerUUID();

            MinecraftServer server = HeldItemSaver.getServer();
            if (server == null) return Unit.INSTANCE;

            ServerPlayerEntity player = server.getPlayerManager().getPlayer(ownerUuid);
            if (player == null) return Unit.INSTANCE;

            ItemStack receivingItem = event.getReceiving();
            if (SettingsManager.isHeldItemBlacklisted(receivingItem)) {
                event.cancel();
                LangManager.sendLang(player, "Blacklisted-Item", Map.of("{item}", ColorUtil.serialize(receivingItem.getName())));
            }

            return Unit.INSTANCE;
        });
    }
}
