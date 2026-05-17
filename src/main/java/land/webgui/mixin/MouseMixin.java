package land.webgui.mixin;

import com.cinemamod.mcef.MCEFBrowser;
import land.webgui.WebHudOverlay;
import land.webgui.WebSession;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
//? if >=1.21.5 {
import net.minecraft.client.input.MouseInput;
//? }
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    //? if >=1.21.5 {
    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void webgui$cancelVanillaForWebHudPseudoGui(long window, MouseInput input, int action, CallbackInfo ci) {
    //? } else {
    /*@Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void webgui$cancelVanillaForWebHudPseudoGui(long window, int button, int action, int mods, CallbackInfo ci) {*/
    //? }
        MinecraftClient client = MinecraftClient.getInstance();
        if (!WebHudOverlay.shouldDeliverHudBrowserInput(client)) {
            return;
        }
        var win = client.getWindow();
        if (win.getHandle() != window) {
            return;
        }
        //? if >=1.21.5 {
        double mx = client.mouse.getScaledX(win);
        double my = client.mouse.getScaledY(win);
        //? } else {
        /*double mx = client.mouse.getX() / win.getScaleFactor();
        double my = client.mouse.getY() / win.getScaleFactor();*/
        //? }
        if (!WebHudOverlay.containsMouse(mx, my, client)) {
            return;
        }
        MCEFBrowser browser = WebSession.browser();
        if (browser != null) {
            int lx = WebHudOverlay.toBrowserLocalX(mx, client);
            int ly = WebHudOverlay.toBrowserLocalY(my, client);
            browser.setFocus(true);
            //? if >=1.21.5 {
            if (action == GLFW.GLFW_PRESS) {
                browser.sendMousePress(lx, ly, input.button());
            } else if (action == GLFW.GLFW_RELEASE) {
                browser.sendMouseRelease(lx, ly, input.button());
            }
            //? } else {
            /*if (action == GLFW.GLFW_PRESS) {
                browser.sendMousePress(lx, ly, button);
            } else if (action == GLFW.GLFW_RELEASE) {
                browser.sendMouseRelease(lx, ly, button);
            }*/
            //? }
        }
        ci.cancel();
    }

    @Inject(method = "onCursorPos", at = @At("HEAD"))
    private void webgui$moveToWebHud(long window, double x, double y, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!WebHudOverlay.shouldDeliverHudBrowserInput(client)) {
            return;
        }
        var win = client.getWindow();
        if (win.getHandle() != window) {
            return;
        }
        MCEFBrowser browser = WebSession.browser();
        if (browser == null) {
            return;
        }
        double mx = x / win.getScaleFactor();
        double my = y / win.getScaleFactor();
        if (WebHudOverlay.containsMouse(mx, my, client)) {
            browser.sendMouseMove((int) x, (int) y);
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void webgui$scrollToWebHudPseudoGui(long window, double horizontal, double vertical, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!WebHudOverlay.shouldDeliverHudBrowserInput(client)) {
            return;
        }
        var win = client.getWindow();
        if (win.getHandle() != window) {
            return;
        }
        //? if >=1.21.5 {
        double mx = client.mouse.getScaledX(win);
        double my = client.mouse.getScaledY(win);
        //? } else {
        /*double mx = client.mouse.getX() / win.getScaleFactor();
        double my = client.mouse.getY() / win.getScaleFactor();*/
        //? }
        if (!WebHudOverlay.containsMouse(mx, my, client)) {
            return;
        }
        MCEFBrowser browser = WebSession.browser();
        if (browser == null) {
            return;
        }
        int lx = WebHudOverlay.toBrowserLocalX(mx, client);
        int ly = WebHudOverlay.toBrowserLocalY(my, client);
        browser.setFocus(true);
        browser.sendMouseWheel(lx, ly, (int) vertical, (int) horizontal);
        ci.cancel();
    }
}
