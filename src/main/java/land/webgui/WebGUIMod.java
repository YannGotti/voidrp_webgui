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
        // Server-only: register payload types AND server receivers together.
        // On the client both are skipped here; onInitializeClient() registers payload types
        // instead, because Connector on the client would incorrectly add S2C channels to
        // NeoForge's negotiation table causing disconnect. registerServerReceivers() must
        // not run on the client either — the C2S payload type won't be registered yet.
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            WebviewNetworking.registerPayloadTypes();
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
