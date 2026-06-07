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
        // On the server: register all payload types (both S2C and C2S) so that
        // registerServerReceivers() can find the C2S type. Connector on the server
        // correctly marks S2C channels as "server will send these" — no negotiation issue.
        // On the client: skip here; onInitializeClient() handles registration instead,
        // because Connector on the client would incorrectly add S2C channels to NeoForge's
        // negotiation table causing disconnect ("client wants CLIENTBOUND but server doesn't support it").
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            WebviewNetworking.registerPayloadTypes();
        }
        WebviewNetworking.registerServerReceivers();
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
