package land.webgui;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
//? if >=1.21.5 {
import net.minecraft.command.DefaultPermissions;
//? }
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

public final class WebviewCommands {

    private WebviewCommands() {}

    //? if >=1.21.5 {
    private static boolean hasOp2(ServerCommandSource s) {
        return s.getPermissions().hasPermission(DefaultPermissions.GAMEMASTERS);
    }
    //? } else {
    /*private static boolean hasOp2(ServerCommandSource s) {
        return s.hasPermissionLevel(2);
    }*/
    //? }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(
                        CommandManager.literal("webgui")
                                .requires(WebviewCommands::hasOp2)
                                .then(CommandManager.literal("gui")
                                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                                .then(CommandManager.argument("url", StringArgumentType.greedyString())
                                                        .executes(ctx -> {
                                                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(ctx, "targets");
                                                            String url = StringArgumentType.getString(ctx, "url");
                                                            for (ServerPlayerEntity p : players) {
                                                                WebviewNetworking.openGui(p, url);
                                                            }
                                                            ctx.getSource().sendFeedback(
                                                                    () -> Text.literal("Web GUI → " + players.size() + " player(s)"),
                                                                    true);
                                                            return players.size();
                                                        }))))
                                .then(CommandManager.literal("hud")
                                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                                .then(CommandManager.argument("url", StringArgumentType.greedyString())
                                                        .executes(ctx -> {
                                                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(ctx, "targets");
                                                            String url = StringArgumentType.getString(ctx, "url");
                                                            for (ServerPlayerEntity p : players) {
                                                                WebviewNetworking.openHud(p, url);
                                                            }
                                                            ctx.getSource().sendFeedback(
                                                                    () -> Text.literal("Web HUD → " + players.size() + " player(s)"),
                                                                    true);
                                                            return players.size();
                                                        }))))));
    }
}
