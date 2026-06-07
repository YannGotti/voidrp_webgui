package land.webgui.server;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public final class WebviewServerEvents {
    private WebviewServerEvents() {}

    /**
     * Fired on the server thread when a page sends a message via
     * {@code window.webgui.postToGame({ channel: "myEvent", ... })}.
     *
     * Built-in channels ("log", "close") are handled client-side and never reach this event.
     */
    public static final Event<PageEventHandler> PAGE_EVENT = EventFactory.createArrayBacked(
            PageEventHandler.class,
            handlers -> (player, channel, payload) -> {
                for (PageEventHandler h : handlers) h.onPageEvent(player, channel, payload);
            }
    );

    @FunctionalInterface
    public interface PageEventHandler {
        /**
         * @param player  the player whose page sent the message
         * @param channel value of the {@code channel} field in the JSON payload
         * @param payload the full raw JSON string as sent by the page
         */
        void onPageEvent(ServerPlayerEntity player, String channel, String payload);
    }
}
