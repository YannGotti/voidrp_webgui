package land.webgui;

import com.cinemamod.mcef.MCEF;
import com.cinemamod.mcef.MCEFBrowser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public final class WebviewClientBridge {
    private static final Gson GSON = new Gson();
    private static String lastSentJson;
    private static String pendingEntityContextJson;

    private WebviewClientBridge() {}

    /** Called from the client networking thread; the actual push happens on the next tick. */
    static void setEntityContext(String json) {
        pendingEntityContextJson = json;
        lastSentJson = null; // force re-push so the page gets updated immediately
    }

    static void clearCache() {
        lastSentJson = null;
        pendingEntityContextJson = null;
    }

    public static void tick(MinecraftClient client) {
        tryPush(client, true, false);
    }

    /** Always pushes, regardless of dedup — page must get data on first load. */
    public static void pushAfterDocumentLoad(MinecraftClient client) {
        tryPush(client, false, true);
    }

    private static void tryPush(MinecraftClient client, boolean requireTexture, boolean ignoreDedup) {
        if (!MCEF.isInitialized()) return;
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        MCEFBrowser main = WebSession.browser();
        MCEFBrowser hud  = WebSession.hudBrowser();
        boolean hasMain = main != null && (!requireTexture || main.isTextureReady());
        boolean hasHud  = hud  != null && hud != main;
        if (!hasMain && !hasHud) return;

        JsonObject payload = buildPayload(client, player);
        String clientJson = GSON.toJson(payload);
        String entityLiteral = (pendingEntityContextJson != null && !pendingEntityContextJson.equals("null"))
                ? pendingEntityContextJson : "null";
        String dedupKey = clientJson + "|entity=" + entityLiteral;
        if (!ignoreDedup && dedupKey.equals(lastSentJson)) return;
        lastSentJson = dedupKey;

        String js = "(function(){"
                + "var c=" + clientJson + ";"
                + "var e=" + entityLiteral + ";"
                + "if(typeof window.webgui==='undefined')window.webgui={};"
                + "window.webgui.entity=e;"
                + "window.webgui.client=c;"
                + "try{window.dispatchEvent(new CustomEvent('webgui:entity',{detail:e}));}catch(ex){}"
                + "try{window.dispatchEvent(new CustomEvent('webgui:client',{detail:c}));}catch(ex){}"
                + "if(typeof window.webgui.onClientInfo==='function')try{window.webgui.onClientInfo(c);}catch(ex){console.error(ex);}"
                + "})();";

        if (hasMain) executeJs(main, js);
        if (hasHud)  executeJs(hud,  js);
    }

    private static void executeJs(MCEFBrowser browser, String js) {
        try {
            String url = browser.getURL();
            browser.executeJavaScript(js, url != null ? url : "", 0);
        } catch (Throwable t) {
            WebGUIMod.LOGGER.debug("webgui client bridge: {}", t.toString());
        }
    }

    private static JsonObject buildPayload(MinecraftClient client, ClientPlayerEntity player) {
        JsonObject o = new JsonObject();

        o.addProperty("playerUuid",  player.getUuid().toString());
        o.addProperty("username",    player.getName().getString());
        o.addProperty("webviewMode", WebSession.mode().name());
        o.addProperty("dimension",   player.getEntityWorld().getRegistryKey().getValue().toString());

        JsonObject pos = new JsonObject();
        pos.addProperty("x", player.getX());
        pos.addProperty("y", player.getY());
        pos.addProperty("z", player.getZ());
        o.add("pos", pos);

        JsonObject server = buildServerInfo(client);
        if (server != null) o.add("server", server);

        return o;
    }

    private static JsonObject buildServerInfo(MinecraftClient client) {
        var nh = client.getNetworkHandler();
        if (nh == null) return null;
        JsonObject s = new JsonObject();
        var entry = client.getCurrentServerEntry();
        if (entry != null) {
            s.addProperty("address", entry.address);
            if (entry.ping >= 0) s.addProperty("ping", entry.ping);
        } else {
            var conn = nh.getConnection();
            if (conn != null && conn.getAddress() != null) {
                s.addProperty("address", conn.getAddress().toString().replaceFirst("^/", ""));
            }
        }
        return s;
    }
}
