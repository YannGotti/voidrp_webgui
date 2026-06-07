package land.webgui;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import land.webgui.server.EntityBinding;
import land.webgui.server.WebviewServerConfig;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
//? if >=1.21.5 {
import net.minecraft.command.DefaultPermissions;
//? }
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

public final class WebviewCommands {

    private WebviewCommands() {}

    private static int bindEntities(com.mojang.brigadier.context.CommandContext<ServerCommandSource> ctx, boolean cancel)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        Collection<? extends Entity> entities = EntityArgumentType.getEntities(ctx, "selector");
        String url = StringArgumentType.getString(ctx, "url");
        EntityBinding binding = new EntityBinding(url, cancel);
        for (Entity e : entities) {
            EntityBindingStore.bind(e.getUuid(), binding);
        }
        final int count = entities.size();
        ctx.getSource().sendFeedback(
                () -> Text.literal("WebGUI: bound " + count + " entity/entities → " + url),
                true);
        return count;
    }

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
                                .then(CommandManager.literal("bind")
                                        .then(CommandManager.literal("entity")
                                                .then(CommandManager.argument("selector", EntityArgumentType.entities())
                                                        .then(CommandManager.argument("url", StringArgumentType.string())
                                                                .executes(ctx -> bindEntities(ctx, false))
                                                                .then(CommandManager.argument("cancel_interaction", BoolArgumentType.bool())
                                                                        .executes(ctx -> bindEntities(ctx,
                                                                                BoolArgumentType.getBool(ctx, "cancel_interaction"))))))))
                                .then(CommandManager.literal("unbind")
                                        .then(CommandManager.literal("entity")
                                                .then(CommandManager.argument("selector", EntityArgumentType.entities())
                                                        .executes(ctx -> {
                                                            Collection<? extends Entity> entities = EntityArgumentType.getEntities(ctx, "selector");
                                                            int count = 0;
                                                            for (Entity e : entities) {
                                                                if (EntityBindingStore.unbind(e.getUuid())) count++;
                                                            }
                                                            final int removed = count;
                                                            ctx.getSource().sendFeedback(
                                                                    () -> Text.literal("WebGUI: unbound " + removed + " entity/entities"),
                                                                    true);
                                                            return removed;
                                                        }))))
                                .then(CommandManager.literal("reload")
                                        .executes(ctx -> {
                                            String msg = WebviewServerConfig.reload();
                                            EntityBindingStore.load();
                                            ctx.getSource().sendFeedback(() -> Text.literal(msg), true);
                                            return 1;
                                        }))
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
