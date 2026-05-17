package land.webgui;

import com.cinemamod.mcef.MCEF;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
//? if >=1.21.5 {
import net.minecraft.util.Identifier;
//? }
import org.lwjgl.glfw.GLFW;

public final class WebGUIKeys {
    private static KeyBinding mainMenu;
    private static KeyBinding hudInteractive;

    private WebGUIKeys() {}

    public static void register() {
        //? if >=1.21.5 {
        KeyBinding.Category cat = KeyBinding.Category.create(Identifier.of(WebGUIMod.MOD_ID, "web"));
        mainMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.webgui.main_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F6,
                cat));
        hudInteractive = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.webgui.hud_interactive",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                cat));
        //? } else {
        /*mainMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.webgui.main_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F6,
                WebGUIMod.MOD_ID + ".web"));
        hudInteractive = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.webgui.hud_interactive",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                WebGUIMod.MOD_ID + ".web"));*/
        //? }
    }

    public static void tick(MinecraftClient client) {
        while (mainMenu.wasPressed()) {
            tryOpenMainMenu(client);
        }
        while (hudInteractive.wasPressed()) {
            if (!WebHudOverlay.isHudVisible() || client.currentScreen != null) {
                continue;
            }
            WebHudOverlay.toggleInteractive(client);
        }
    }

    private static void tryOpenMainMenu(MinecraftClient client) {
        if (client.currentScreen instanceof WebViewScreen) {
            return;
        }
        if (!MCEF.isInitialized()) {
            if (client.player != null) {
                client.player.sendMessage(Text.translatable("message.webgui.mcef_not_ready"), false);
            }
            return;
        }
        client.setScreen(new WebViewScreen(WebGUIMainMenuUrl.getUrl()));
    }
}
