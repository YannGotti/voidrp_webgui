package land.webgui;

//? if >=1.20.5 {
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
//? }
import net.minecraft.util.Identifier;

public final class WebviewPayloads {
    private WebviewPayloads() {}

    public static final int MAX_EVENT_NAME_LENGTH = 256;
    public static final int MAX_EVENT_DATA_LENGTH = 32_768;

    // Channel identifiers — used in both legacy (1.20.1) and modern networking
    public static final Identifier OPEN_WEB_CHANNEL  = Identifier.of(WebGUIMod.MOD_ID, "open_web");
    public static final Identifier MAIN_MENU_CHANNEL = Identifier.of(WebGUIMod.MOD_ID, "set_main_menu");
    public static final Identifier EMIT_TO_PAGE_CHANNEL = Identifier.of(WebGUIMod.MOD_ID, "emit_to_page");
    public static final Identifier PAGE_EVENT_CHANNEL   = Identifier.of(WebGUIMod.MOD_ID, "page_event");

    //? if >=1.20.5 {
    /** S2C: server emits a named event to the page. */
    public record WebviewEmitS2CPayload(String eventName, String jsonPayload) implements CustomPayload {
        public static final CustomPayload.Id<WebviewEmitS2CPayload> ID =
                new CustomPayload.Id<>(EMIT_TO_PAGE_CHANNEL);
        public static final PacketCodec<RegistryByteBuf, WebviewEmitS2CPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.string(MAX_EVENT_NAME_LENGTH), WebviewEmitS2CPayload::eventName,
                PacketCodecs.string(MAX_EVENT_DATA_LENGTH), WebviewEmitS2CPayload::jsonPayload,
                WebviewEmitS2CPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /** C2S: page sends a named event to the server. */
    public record WebviewPageEventC2SPayload(String channel, String jsonPayload) implements CustomPayload {
        public static final CustomPayload.Id<WebviewPageEventC2SPayload> ID =
                new CustomPayload.Id<>(PAGE_EVENT_CHANNEL);
        public static final PacketCodec<RegistryByteBuf, WebviewPageEventC2SPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.string(MAX_EVENT_NAME_LENGTH), WebviewPageEventC2SPayload::channel,
                PacketCodecs.string(MAX_EVENT_DATA_LENGTH), WebviewPageEventC2SPayload::jsonPayload,
                WebviewPageEventC2SPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /** displayMode: 0 = GUI, 1 = HUD */
    public record OpenWebS2CPayload(int protocolVersion, int displayMode, String url) implements CustomPayload {
        public static final CustomPayload.Id<OpenWebS2CPayload> ID =
                new CustomPayload.Id<>(OPEN_WEB_CHANNEL);
        public static final PacketCodec<RegistryByteBuf, OpenWebS2CPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT,
                OpenWebS2CPayload::protocolVersion,
                PacketCodecs.VAR_INT,
                OpenWebS2CPayload::displayMode,
                PacketCodecs.string(WebviewNetworking.MAX_URL_LENGTH),
                OpenWebS2CPayload::url,
                OpenWebS2CPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record WebUIMainMenuPayload(String url) implements CustomPayload {
        public static final CustomPayload.Id<WebUIMainMenuPayload> ID =
                new CustomPayload.Id<>(MAIN_MENU_CHANNEL);
        public static final PacketCodec<RegistryByteBuf, WebUIMainMenuPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.string(WebviewNetworking.MAX_URL_LENGTH),
                WebUIMainMenuPayload::url,
                WebUIMainMenuPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    //? }
}
