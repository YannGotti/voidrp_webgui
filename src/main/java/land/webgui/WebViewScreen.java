package land.webgui;

import com.cinemamod.mcef.MCEF;
import com.cinemamod.mcef.MCEFBrowser;
//? if >=1.21.5 {
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
//? }
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WebViewScreen extends Screen {

    private static boolean guiPageReady;
    private final String initialUrl;
    private MCEFBrowser browser;

    public WebViewScreen(String startUrl) {
        super(Text.translatable("screen.webgui.title"));
        this.initialUrl = startUrl == null || startUrl.isBlank() ? StartUrls.primary() : startUrl;
    }

    @Override
    protected void init() {
        super.init();
        if (!MCEF.isInitialized()) {
            if (this.client != null && this.client.player != null) {
                this.client.player.sendMessage(Text.translatable("message.webgui.mcef_not_ready"), false);
            }
            this.close();
            return;
        }

        if (browser != null) {
            resizeBrowser();
            return;
        }

        WebHudOverlay.onGuiOpened();
        guiPageReady = false;
        browser = WebSession.openForGui(initialUrl);
        resizeBrowser();
    }

    private int getBrowserWidth() {
        return Math.max(1, this.width);
    }

    private int getBrowserHeight() {
        return Math.max(1, this.height);
    }

    private boolean isInBrowserBounds(double x, double y) {
        return x >= 0 && y >= 0 && x < this.width && y < this.height;
    }

    private int browserLocalMouseX(double x) {
        return (int) (x * this.client.getWindow().getScaleFactor());
    }

    private int browserLocalMouseY(double y) {
        return (int) (y * this.client.getWindow().getScaleFactor());
    }

    private void resizeBrowser() {
        if (browser != null && this.client != null) {
            browser.resize(this.client.getWindow().getWidth(), this.client.getWindow().getHeight());
        }
    }

    //? if >=1.21.5 {
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        resizeBrowser();
    }
    //? } else {
    /*@Override
    public void resize(net.minecraft.client.MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        resizeBrowser();
    }*/
    //? }

    @Override
    public void close() {
        guiPageReady = false;
        WebSession.closeGuiAndRestoreHud();
        super.close();
        if (this.client != null) {
            WebHudOverlay.onGuiClosed(this.client);
        }
    }

    static void onGuiBrowserLoadStart(MCEFBrowser browser) {
        if (browser == null) {
            return;
        }
        if (WebSession.mode() == WebSession.Mode.GUI_SCREEN && browser == WebSession.browser()) {
            guiPageReady = false;
        }
    }

    static void onGuiBrowserLoadFinished(MCEFBrowser browser) {
        if (browser == null) {
            return;
        }
        if (WebSession.mode() == WebSession.Mode.GUI_SCREEN && browser == WebSession.browser()) {
            guiPageReady = true;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (browser != null && guiPageReady && browser.isTextureReady()) {
            //? if >=1.21.5 {
            Identifier textureLocation = browser.getTextureIdentifier();
            //? } else {
            /*Identifier textureLocation = browser.getTextureLocation();*/
            //? }
            if (textureLocation != null) {
                int fw = getBrowserWidth();
                int fh = getBrowserHeight();
                //? if >=1.21.5 {
                context.drawTexture(
                        RenderPipelines.GUI_TEXTURED,
                        textureLocation,
                        0,
                        0,
                        0f,
                        0f,
                        fw,
                        fh,
                        fw,
                        fh);
                //? } else {
                /*context.drawTexture(
                        textureLocation,
                        0,
                        0,
                        0,
                        0f,
                        0f,
                        fw,
                        fh,
                        fw,
                        fh);*/
                //? }
            }
        }
    }

    //? if >=1.20.5 {
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        // no darkening — web fills the entire screen
    }
    //? } else {
    /*@Override
    public void renderBackground(DrawContext context) {
        // no darkening — web fills the entire screen
    }*/
    //? }

    //? if >=1.21.5 {
    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (browser == null || !isInBrowserBounds(click.x(), click.y())) {
            return false;
        }
        browser.sendMousePress(browserLocalMouseX(click.x()), browserLocalMouseY(click.y()), click.button());
        browser.setFocus(true);
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (browser == null) {
            return false;
        }
        browser.sendMouseRelease(browserLocalMouseX(click.x()), browserLocalMouseY(click.y()), click.button());
        browser.setFocus(true);
        return true;
    }
    //? } else {
    /*@Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (browser == null || !isInBrowserBounds(mouseX, mouseY)) {
            return false;
        }
        browser.sendMousePress(browserLocalMouseX(mouseX), browserLocalMouseY(mouseY), button);
        browser.setFocus(true);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (browser == null) {
            return false;
        }
        browser.sendMouseRelease(browserLocalMouseX(mouseX), browserLocalMouseY(mouseY), button);
        browser.setFocus(true);
        return true;
    }*/
    //? }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (browser != null && isInBrowserBounds(mouseX, mouseY)) {
            browser.sendMouseMove(browserLocalMouseX(mouseX), browserLocalMouseY(mouseY));
        }
        super.mouseMoved(mouseX, mouseY);
    }

    //? if >=1.20.5 {
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (browser == null || !isInBrowserBounds(mouseX, mouseY)) {
            return false;
        }
        browser.sendMouseWheel(browserLocalMouseX(mouseX), browserLocalMouseY(mouseY), verticalAmount, 0);
        return true;
    }
    //? } else {
    /*@Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (browser == null || !isInBrowserBounds(mouseX, mouseY)) {
            return false;
        }
        browser.sendMouseWheel(browserLocalMouseX(mouseX), browserLocalMouseY(mouseY), amount, 0);
        return true;
    }*/
    //? }

    //? if >=1.21.5 {
    @Override
    public boolean keyPressed(KeyInput input) {
        if (super.keyPressed(input)) {
            return true;
        }
        if (browser == null) {
            return false;
        }
        browser.sendKeyPress(input.key(), input.scancode(), input.modifiers());
        browser.setFocus(true);
        return true;
    }

    @Override
    public boolean keyReleased(KeyInput input) {
        if (super.keyReleased(input)) {
            return true;
        }
        if (browser == null) {
            return false;
        }
        browser.sendKeyRelease(input.key(), input.scancode(), input.modifiers());
        browser.setFocus(true);
        return true;
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (super.charTyped(input)) {
            return true;
        }
        if (browser == null || !input.isValidChar()) {
            return false;
        }
        int cp = input.codepoint();
        if (cp <= 0 || cp > 0xFFFF) {
            return false;
        }
        browser.sendKeyTyped((char) cp, input.modifiers());
        browser.setFocus(true);
        return true;
    }
    //? } else {
    /*@Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (browser == null) {
            return false;
        }
        browser.sendKeyPress(keyCode, scanCode, modifiers);
        browser.setFocus(true);
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (super.keyReleased(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (browser == null) {
            return false;
        }
        browser.sendKeyRelease(keyCode, scanCode, modifiers);
        browser.setFocus(true);
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (super.charTyped(chr, modifiers)) {
            return true;
        }
        if (browser == null) {
            return false;
        }
        browser.sendKeyTyped(chr, modifiers);
        browser.setFocus(true);
        return true;
    }*/
    //? }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
