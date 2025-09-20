package me.novoro.helditemsaver.commands;

import com.mojang.brigadier.CommandDispatcher;
import me.novoro.helditemsaver.HeldItemSaver;
import com.mojang.brigadier.Command;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.novoro.helditemsaver.util.ColorUtil;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class ReloadCommand {
    public static final String RELOAD_PERMISSION_NODE = "helditemsaver.reload";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("helditemsaver")
                    .then(literal("reload")
                    .requires(Permissions.require(RELOAD_PERMISSION_NODE, 2))
                        .executes(context -> {
                            HeldItemSaver.inst().reloadConfigs();
                            context.getSource().sendMessage(ColorUtil.parseColour(HeldItemSaver.MOD_PREFIX + "&aReloaded Configs!"));
                            return Command.SINGLE_SUCCESS;
                        }))
        );
    }

}
