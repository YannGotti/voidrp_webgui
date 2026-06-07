package land.webgui;

import land.webgui.server.WebGUIUpdateChecker;
import land.webgui.server.WebviewServerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WebGUIMod implements ModInitializer {
    public static final String MOD_ID = "webgui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Server-only: register only the C2S payload type, then the server receiver.
        // S2C types must NOT be registered server-side — Connector would add them to
        // NeoForge's channel negotiation as required CLIENTBOUND, causing disconnect.
        // onInitializeClient() handles full payload registration on the client.
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            WebviewNetworking.registerServerC2SPayloadType();
            WebviewNetworking.registerServerReceivers();
        }
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            WebviewServerConfig.load();
            EntityBindingStore.load();
        });
        EntityInteractionListener.register();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            WebGUIUpdateChecker.checkAsync();
        });
        WebviewCommands.register();
        WebviewJoinHud.register();
        LOGGER.info("WebGUI common init (S2C payloads, commands).");
    }
}
