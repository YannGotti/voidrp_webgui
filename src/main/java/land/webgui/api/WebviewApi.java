package land.webgui.api;

import land.webgui.WebviewNetworking;
import land.webgui.server.WebviewServerEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.function.BiConsumer;

/** Call from the server thread. */
public final class WebviewApi {
    private WebviewApi() {}

    /** If signed tokens are enabled in server.json, a token is appended to the URL automatically. */
    public static void openGui(ServerPlayerEntity player, String url) {
        WebviewNetworking.openGui(player, url);
    }

    /** If signed tokens are enabled in server.json, a token is appended to the URL automatically. */
    public static void openHud(ServerPlayerEntity player, String url) {
        WebviewNetworking.openHud(player, url);
    }

    /** Player opens this URL via the main menu keybind (default F6). */
    public static void sendMainMenuUrl(ServerPlayerEntity player, String url) {
        WebviewNetworking.sendMainMenuUrl(player, url);
    }

    /**
     * Sends a named event to the player's active WebGUI page(s).
     *
     * @param eventName   name of the event; the page receives it as {@code webgui:<eventName>}
     *                    via {@code window.addEventListener} or {@code window.webgui.on}
     * @param jsonPayload valid JSON value (object, array, string, number, boolean, or {@code "null"});
     *                    passed as {@code event.detail} in the browser
     */
    public static void emitToPage(ServerPlayerEntity player, String eventName, String jsonPayload) {
        WebviewNetworking.emitToPage(player, eventName, jsonPayload);
    }

    /**
     * Registers a server-side handler for events sent from the page via
     * {@code window.webgui.postToGame({ channel: "myEvent", ... })}.
     *
     * <p>Built-in channels ({@code "log"}, {@code "close"}) are handled client-side
     * and never reach this handler.
     *
     * @param channel the {@code channel} field value to match
     * @param handler receives (player, rawJsonPayload)
     */
    public static void onPageEvent(String channel, BiConsumer<ServerPlayerEntity, String> handler) {
        WebviewServerEvents.PAGE_EVENT.register(
                (player, ch, payload) -> { if (ch.equals(channel)) handler.accept(player, payload); });
    }

    /** Maximum URL length accepted by the mod (16 384 characters). */
    public static int maxUrlLength() {
        return WebviewNetworking.MAX_URL_LENGTH;
    }

    /** S2C protocol version this build of the mod uses. */
    public static int protocolVersion() {
        return WebviewNetworking.PROTOCOL_VERSION;
    }
}
