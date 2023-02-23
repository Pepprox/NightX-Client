package net.aspw.client.visual.client.clickgui;

import de.enzaxd.viaforge.ViaForge;
import de.enzaxd.viaforge.protocol.ProtocolCollection;
import net.aspw.client.features.module.ModuleCategory;
import net.aspw.client.features.module.modules.client.Fix;
import net.aspw.client.features.module.modules.client.Gui;
import net.aspw.client.utils.AnimationUtils;
import net.aspw.client.utils.EntityUtils;
import net.aspw.client.utils.MouseUtils;
import net.aspw.client.utils.PacketUtils;
import net.aspw.client.utils.render.RenderUtils;
import net.aspw.client.utils.render.Stencil;
import net.aspw.client.visual.client.clickgui.element.CategoryElement;
import net.aspw.client.visual.client.clickgui.element.SearchElement;
import net.aspw.client.visual.client.clickgui.element.module.ModuleElement;
import net.aspw.client.visual.font.Fonts;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewUi extends GuiScreen {

    private static NewUi instance;
    public final List<CategoryElement> categoryElements = new ArrayList<>();
    private float startYAnim = height / 2F;
    private float endYAnim = height / 2F;
    private float fading = 0F;
    private SearchElement searchElement;

    private NewUi() {
        for (ModuleCategory c : ModuleCategory.values())
            categoryElements.add(new CategoryElement(c));
        categoryElements.get(0).setFocused(true);
    }

    public static final NewUi getInstance() {
        return instance == null ? instance = new NewUi() : instance;
    }

    public static void resetInstance() {
        instance = new NewUi();
    }

    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        for (CategoryElement ce : categoryElements) {
            for (ModuleElement me : ce.getModuleElements()) {
                if (me.listeningKeybind())
                    me.resetState();
            }
        }
        searchElement = new SearchElement(34F, 38F, 192F, 20F);
        super.initGui();
    }

    public void onGuiClosed() {
        for (CategoryElement ce : categoryElements) {
            if (ce.getFocused())
                ce.handleMouseRelease(-1, -1, 0, 0, 0, 0, 0);
        }
        Keyboard.enableRepeatEvents(false);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawFullSized(mouseX, mouseY, partialTicks, Gui.getAccentColor());
    }

    private void drawFullSized(int mouseX, int mouseY, float partialTicks, Color accentColor) {
        RenderUtils.originalRoundedRect(31F, 31F, this.width - 31F, this.height - 31F, 8F, 0xFF060606);
            Fonts.fontSFUI40.drawStringWithShadow(
                    "inBound: §a" + PacketUtils.avgInBound,
                    242f,
                    Fonts.fontSFUI35.FONT_HEIGHT + 28f,
                    -1);
            Fonts.fontSFUI40.drawStringWithShadow(
                    "outBound: §a" + PacketUtils.avgOutBound,
                    308f,
                    Fonts.fontSFUI35.FONT_HEIGHT + 28f,
                    -1);
        if (MouseUtils.mouseWithinBounds(mouseX, mouseY, this.width - 54F, 30F, this.width - 30F, 50F))
            fading += 0.2F * RenderUtils.deltaTime * 0.045F;
        else
            fading -= 0.2F * RenderUtils.deltaTime * 0.045F;
        fading = MathHelper.clamp_float(fading, 0F, 2F);
        GlStateManager.disableAlpha();
        RenderUtils.drawImage(IconManager.removeIcon, this.width - 49, 32, 18, 18);
        GlStateManager.enableAlpha();
        Stencil.write(true);
        Stencil.erase(true);
        Stencil.dispose();

        final float elementHeight = 24;
        float startY = 60F;
        for (CategoryElement ce : categoryElements) {
            ce.drawLabel(mouseX, mouseY, 30F, startY, 200F, elementHeight);
            if (ce.getFocused()) {
                startYAnim = Fix.fixValue.get() ? startY + 6F : AnimationUtils.animate(startY + 6F, startYAnim, (startYAnim - (startY + 5F) > 0 ? 0.65F : 0.55F) * RenderUtils.deltaTime * 0.025F);
                endYAnim = Fix.fixValue.get() ? startY + elementHeight - 6F : AnimationUtils.animate(startY + elementHeight - 6F, endYAnim, (endYAnim - (startY + elementHeight - 5F) < 0 ? 0.65F : 0.55F) * RenderUtils.deltaTime * 0.025F);

                ce.drawPanel(mouseX, mouseY, 230, 0, width - 260, height - 40, Mouse.getDWheel(), accentColor);
                ce.drawPanel(mouseX, mouseY, 230, 0, width - 260, height - 40, Mouse.getDWheel(), accentColor);
            }
            startY += elementHeight;
        }
        RenderUtils.originalRoundedRect(32F, startYAnim, 34F, endYAnim, 1F, accentColor.getRGB());
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (searchElement.drawBox(mouseX, mouseY, accentColor)) {
            searchElement.drawPanel(mouseX, mouseY, 230, 50, width - 260, height - 80, Mouse.getDWheel(), categoryElements, accentColor);
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (MouseUtils.mouseWithinBounds(mouseX, mouseY, this.width - 54F, 30F, this.width - 30F, 50F)) {
            mc.displayGuiScreen(null);
            return;
        }
        final float elementHeight = 24;
        float startY = 60F;
        searchElement.handleMouseClick(mouseX, mouseY, mouseButton, 230, 50, width - 260, height - 80, categoryElements);
        if (!searchElement.isTyping()) for (CategoryElement ce : categoryElements) {
            if (ce.getFocused())
                ce.handleMouseClick(mouseX, mouseY, mouseButton, 230, 0, width - 260, height - 40);
            if (MouseUtils.mouseWithinBounds(mouseX, mouseY, 30F, startY, 230F, startY + elementHeight)) {
                categoryElements.forEach(e -> e.setFocused(false));
                ce.setFocused(true);
                return;
            }
            startY += elementHeight;
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {

        for (CategoryElement ce : categoryElements) {
            if (ce.getFocused()) {
                if (ce.handleKeyTyped(typedChar, keyCode))
                    return;
            }
        }
        if (searchElement.handleTyping(typedChar, keyCode, 230, 50, width - 260, height - 80, categoryElements))
            return;
        super.keyTyped(typedChar, keyCode);
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (CategoryElement ce : categoryElements) {
            if (ce.getFocused())
                ce.handleMouseRelease(mouseX, mouseY, state, 230, 50, width - 260, height - 80);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}