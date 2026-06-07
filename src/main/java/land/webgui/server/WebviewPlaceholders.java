package land.webgui.server;

import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;

public final class WebviewPlaceholders {
    private WebviewPlaceholders() {}

    public static String resolve(String template, ServerPlayerEntity player, Entity entity) {
        String entityId = entity.getUuid().toString();
        String entityType = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
        return template
                .replace("{entity_id}",   entityId)
                .replace("{entity_uuid}", entityId)
                .replace("{entity_type}", entityType)
                .replace("{player_name}", player.getName().getString())
                .replace("{player_uuid}", player.getUuid().toString());
    }
}
