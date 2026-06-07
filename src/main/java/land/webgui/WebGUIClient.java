package land.webgui;

import com.cinemamod.mcef.MCEF;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class WebGUIClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MCEF.scheduleForInit(success -> {
            if (!success) {
                WebGUIMod.LOGGER.error("MCEF (Chromium) failed to initialize — web GUI will not work.");
                return;
            }
            MCEF.getClient().addDisplayHandler(new WebviewBrowserConsoleLogger());
            WebviewPageToClientBridge.register();
            WebviewPageLoadHooks.register();
            WebGUIMod.LOGGER.info("WebGUI bridge ready (console log, page↔game, client data).");
        });

        //? if >=1.20.5 {
        ClientPlayNetworking.registerGlobalReceiver(WebviewPayloads.OpenWebS2CPayload.ID, (payload, context) -> {
            if (payload.protocolVersion() != WebviewNetworking.PROTOCOL_VERSION) {
                return;
            }
            context.client().execute(() -> handleOpenPayload(context.client(), payload.displayMode(), payload.url()));
        });

        ClientPlayNetworking.registerGlobalReceiver(WebviewPayloads.WebUIMainMenuPayload.ID, (payload, context) -> {
            context.client().execute(() -> WebGUIMainMenuUrl.setUrl(payload.url()));
        });

        ClientPlayNetworking.registerGlobalReceiver(WebviewPayloads.WebviewEmitS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> WebviewClientEmit.dispatch(payload.eventName(), payload.jsonPayload()));
        });
        //? } else {
        /*ClientPlayNetworking.registerGlobalReceiver(WebviewPayloads.OPEN_WEB_CHANNEL, (client, handler, buf, responseSender) -> {
            int protocolVersion = buf.readVarInt();
            int displayMode = buf.readVarInt();
            String url = buf.readString(WebviewNetworking.MAX_URL_LENGTH);
            if (protocolVersion != WebviewNetworking.PROTOCOL_VERSION) {
                return;
            }
            client.execute(() -> handleOpenPayload(client, displayMode, url));
        });

        ClientPlayNetworking.registerGlobalReceiver(WebviewPayloads.MAIN_MENU_CHANNEL, (client, handler, buf, responseSender) -> {
            String url = buf.readString(WebviewNetworking.MAX_URL_LENGTH);
            client.execute(() -> WebGUIMainMenuUrl.setUrl(url));
        });

        ClientPlayNetworking.registerGlobalReceiver(WebviewPayloads.EMIT_TO_PAGE_CHANNEL, (client, handler, buf, responseSender) -> {
            String eventName   = buf.readString(WebviewPayloads.MAX_EVENT_NAME_LENGTH);
            String jsonPayload = buf.readString(WebviewPayloads.MAX_EVENT_DATA_LENGTH);
            client.execute(() -> WebviewClientEmit.dispatch(eventName, jsonPayload));
        });*/
        //? }

        WebGUIKeys.register();
        WebHudOverlay.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            WebHudOverlay.tickCursor(client);
            WebGUIKeys.tick(client);
            WebviewClientBridge.tick(client);
        });
    }

    private static void handleOpenPayload(net.minecraft.client.MinecraftClient client, int mode, String url) {
        if (!MCEF.isInitialized()) {
            if (client.player != null) {
                client.player.sendMessage(net.minecraft.text.Text.translatable("message.webgui.mcef_not_ready"), false);
            }
            return;
        }
        String u = url == null || url.isBlank() ? StartUrls.primary() : url;
        if (mode == WebviewNetworking.MODE_GUI) {
            client.setScreen(new WebViewScreen(u));
        } else if (mode == WebviewNetworking.MODE_HUD) {
            WebHudOverlay.applyServerOpen(client, u);
        }
    }
}
