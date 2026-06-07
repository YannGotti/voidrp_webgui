package land.webgui;

import land.webgui.server.EntityBinding;
import land.webgui.server.WebviewEntityContext;
import land.webgui.server.WebviewPlaceholders;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public final class EntityInteractionListener {
    private EntityInteractionListener() {}

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
            if (world.isClient()) return ActionResult.PASS;

            var opt = EntityBindingStore.get(entity.getUuid());
            if (opt.isEmpty()) return ActionResult.PASS;

            ServerPlayerEntity sp = (ServerPlayerEntity) player;
            EntityBinding b = opt.get();
            String url = WebviewPlaceholders.resolve(b.urlTemplate(), sp, entity);
            String entityJson = WebviewEntityContext.buildJson(entity);
            WebviewNetworking.openGuiForEntity(sp, url, entityJson);

            return b.cancelInteraction() ? ActionResult.SUCCESS : ActionResult.PASS;
        });
    }
}
