package com.brandon3055.brandonscore.client.gui.modulargui;

import com.brandon3055.brandonscore.client.utils.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by brandon3055 on 30/08/2016.
 */
public class GuiElementBase {

    public int xPos;
    public int yPos;
    public int xSize;
    public int ySize;
    private boolean enabled = true;
    public IModularGui modularGui;
    /**
     * An id that is unique to this element
     */
    public String id = null;
    private List<String> groups = new ArrayList<String>();
    public List<GuiElementBase> childElements = new LinkedList<GuiElementBase>();
    private List<GuiElementBase> toRemove = new ArrayList<GuiElementBase>();
    /**
     * For use by ModuleManager ONLY
     */
    public int displayLevel = 0;
    /**
     * Offsets the zLevel when rendering
     */
    protected double zOffset = 0;

    public GuiElementBase(IModularGui modularGui) {
        this.modularGui = modularGui;
    }

    public GuiElementBase(IModularGui modularGui, int xPos, int yPos) {
        this(modularGui);
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public GuiElementBase(IModularGui modularGui, int xPos, int yPos, int xSize, int ySize) {
        this(modularGui, xPos, yPos);
        this.xSize = xSize;
        this.ySize = ySize;
    }

    public void initElement() {
        for (GuiElementBase element : childElements) {
            element.initElement();
        }
    }

    //region Child Elements

    public GuiElementBase addChild(GuiElementBase element) {
        childElements.add(element);
        return this;
    }

    public GuiElementBase removeChild(GuiElementBase element) {
        if (childElements.contains(element)) {
            toRemove.add(element);
        }
        return this;
    }

    public GuiElementBase removeChildByID(String id) {
        Iterator<GuiElementBase> i = childElements.iterator();
        while (i.hasNext()) {
            GuiElementBase element = i.next();
            if (element.id != null && element.id.equals(id)) {
                toRemove.add(element);
                return this;
            }
        }
        return this;
    }

    public GuiElementBase removeChildByGroup(String group) {
        Iterator<GuiElementBase> i = childElements.iterator();
        while (i.hasNext()) {
            GuiElementBase element = i.next();
            if (element.isInGroup(group)) {
                toRemove.add(element);
            }
        }
        return this;
    }

    public GuiElementBase setChildIDEnabled(String id, boolean enabled) {
        Iterator<GuiElementBase> i = childElements.iterator();
        while (i.hasNext()) {
            GuiElementBase element = i.next();
            if (element.id != null && element.id.equals(id)) {
                element.enabled = enabled;
                return this;
            }
        }
        return this;
    }

    public GuiElementBase setChildGroupEnabled(String group, boolean enabled) {
        Iterator<GuiElementBase> i = childElements.iterator();
        while (i.hasNext()) {
            GuiElementBase element = i.next();
            if (element.isInGroup(group)) {
                element.enabled = enabled;
            }
        }
        return this;
    }

    //endregion

    //region Group & ID Stuff

    public GuiElementBase addToGroup(String group) {
        groups.add(group);
        return this;
    }

    public GuiElementBase removeFromGroup(String group) {
        if (groups.contains(group)) {
            groups.remove(group);
        }
        return this;
    }

    public GuiElementBase removeFromAllGroups() {
        groups.clear();
        return this;
    }

    public boolean isInGroup(String group) {
        return groups.contains(group);
    }

    public List<String> getGroups() {
        return groups;
    }

    //endregion

    //region Enable

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    //endregion

    //region Mouse

    /**
     * @param mouseX Mouse x position
     * @param mouseY Mouse y position
     * @return true is the mouse is over this element
     */
    public boolean isMouseOver(int mouseX, int mouseY) {
        return GuiHelper.isInRect(xPos, yPos, xSize, ySize, mouseX, mouseY);
    }

    /**
     * Called whenever the mouse is clicked regardless of weather or not the mouse is over this element.
     *
     * @param mouseX      Mouse x position
     * @param mouseY      Mouse y position
     * @param mouseButton Mouse mutton pressed
     * @return Return true to prevent any further processing for this mouse action.
     * @throws IOException
     */
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (GuiElementBase element : childElements) {
            if (element.isEnabled() && element.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called whenever the mouse is released regardless of weather or not the mouse is over this element.
     *
     * @param mouseX Mouse x position
     * @param mouseY Mouse y position
     * @param state  the mouse state.
     * @return Return true to prevent any further processing for this mouse action.
     */
    public boolean mouseReleased(int mouseX, int mouseY, int state) {
        for (GuiElementBase element : childElements) {
            if (element.isEnabled() && element.mouseReleased(mouseX, mouseY, state)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param mouseX             Mouse x position
     * @param mouseY             Mouse y position
     * @param clickedMouseButton
     * @param timeSinceLastClick
     * @return Return true to prevent any further processing for this mouse action.
     */
    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (GuiElementBase element : childElements) {
            if (element.isEnabled() && element.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called whenever a mouse event is fired.
     *
     * @return Return true to prevent any further processing for this mouse action.
     */
    public boolean handleMouseInput() {
        int mouseX = Mouse.getEventX() * modularGui.screenWidth() / modularGui.getMinecraft().displayWidth;
        int mouseY = modularGui.screenHeight() - Mouse.getEventY() * modularGui.screenHeight() / modularGui.getMinecraft().displayHeight - 1;
        int scrollDirection = Mouse.getEventDWheel();

        if (scrollDirection != 0) {
            for (GuiElementBase element : childElements) {
                if (element.isEnabled() && element.handleMouseScroll(mouseX, mouseY, scrollDirection)) {
                    return true;
                }
            }
        }

        for (GuiElementBase element : childElements) {
            if (element.isEnabled() && element.handleMouseInput()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called whenever the scroll wheel is active.
     *
     * @param mouseX          Mouse x position
     * @param mouseY          Mouse y position
     * @param scrollDirection will ether be -1 or 1 depending on the scroll direction (May potentially be greater than 1 or less then -1)
     * @return true to prevent further processing on this mouse action.
     */
    public boolean handleMouseScroll(int mouseX, int mouseY, int scrollDirection) {
        for (GuiElementBase element : childElements) {
            if (element.isEnabled() && element.handleMouseScroll(mouseX, mouseY, scrollDirection)) {
                return true;
            }
        }
        return false;
    }

    //endregion

    //region Keyboard

    /**
     * Called whenever a key is typed. Return true to cancel further processing.
     */
    protected boolean keyTyped(char typedChar, int keyCode) throws IOException {
        for (GuiElementBase element : childElements) {
            if (element.isEnabled() && element.keyTyped(typedChar, keyCode)) {
                return true;
            }
        }
        return false;
    }

    //endregion

    //region Update

    /**
     * Called every tick to update the element. Note this is called regardless of weather or not the element is actually enabled.
     */
    public void onUpdate() {
        if (!toRemove.isEmpty()) {
            childElements.removeAll(toRemove);
            toRemove.clear();
        }

        for (GuiElementBase element : childElements) {
            element.onUpdate();
        }
    }

    //endregion

    //region Render

    public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        for (GuiElementBase element : childElements) {
            if (element.isEnabled()) {
                element.renderBackgroundLayer(minecraft, mouseX, mouseY, partialTicks);
            }
        }
    }

    public void renderForegroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        for (GuiElementBase element : childElements) {
            if (element.isEnabled()) {
                element.renderForegroundLayer(minecraft, mouseX, mouseY, partialTicks);
            }
        }
    }

    public void renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        for (GuiElementBase element : childElements) {
            if (element.isEnabled()) {
                element.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
            }
        }
    }

    //endregion

    //region Misc

    public GuiScreen getScreen() {
        return modularGui.getScreen();
    }

    @Override
    public int hashCode() {
        return ("[" + id + "-" + xPos + "-" + yPos + "-" + xSize + "-" + ySize + "" + displayLevel + "]").hashCode();
    }

    //endregion

    //region GUI Render Helper ports

    public double getRenderZLevel() {
        return modularGui.getZLevel() + zOffset;
    }

    public void drawHorizontalLine(double startX, double endX, double y, int color) {
        if (endX < startX) {
            double i = startX;
            startX = endX;
            endX = i;
        }

        drawRect(startX, y, endX + 1, y + 1, color);
    }

    public void drawVerticalLine(double x, double startY, double endY, int color) {
        if (endY < startY) {
            double i = startY;
            startY = endY;
            endY = i;
        }

        drawRect(x, startY + 1, x + 1, endY, color);
    }

    public void drawRect(double left, double top, double right, double bottom, int color) {
        double zLevel = getRenderZLevel();
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION);
        vertexbuffer.pos(left, bottom, zLevel).endVertex();
        vertexbuffer.pos(right, bottom, zLevel).endVertex();
        vertexbuffer.pos(right, top, zLevel).endVertex();
        vertexbuffer.pos(left, top, zLevel).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height) {
        double zLevel = getRenderZLevel();
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos((double) (x + 0), (double) (y + height), zLevel).tex((double) ((float) (textureX + 0) * 0.00390625F), (double) ((float) (textureY + height) * 0.00390625F)).endVertex();
        vertexbuffer.pos((double) (x + width), (double) (y + height), zLevel).tex((double) ((float) (textureX + width) * 0.00390625F), (double) ((float) (textureY + height) * 0.00390625F)).endVertex();
        vertexbuffer.pos((double) (x + width), (double) (y + 0), zLevel).tex((double) ((float) (textureX + width) * 0.00390625F), (double) ((float) (textureY + 0) * 0.00390625F)).endVertex();
        vertexbuffer.pos((double) (x + 0), (double) (y + 0), zLevel).tex((double) ((float) (textureX + 0) * 0.00390625F), (double) ((float) (textureY + 0) * 0.00390625F)).endVertex();
        tessellator.draw();
    }

    public void drawTexturedModalRect(double xCoord, double yCoord, int minU, int minV, int maxU, int maxV) {
        double zLevel = getRenderZLevel();
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos((xCoord + 0.0F), (yCoord + (float) maxV), zLevel).tex((double) ((float) (minU + 0) * 0.00390625F), (double) ((float) (minV + maxV) * 0.00390625F)).endVertex();
        vertexbuffer.pos((xCoord + (float) maxU), (yCoord + (float) maxV), zLevel).tex((double) ((float) (minU + maxU) * 0.00390625F), (double) ((float) (minV + maxV) * 0.00390625F)).endVertex();
        vertexbuffer.pos((xCoord + (float) maxU), (yCoord + 0.0F), zLevel).tex((double) ((float) (minU + maxU) * 0.00390625F), (double) ((float) (minV + 0) * 0.00390625F)).endVertex();
        vertexbuffer.pos((xCoord + 0.0F), (yCoord + 0.0F), zLevel).tex((double) ((float) (minU + 0) * 0.00390625F), (double) ((float) (minV + 0) * 0.00390625F)).endVertex();
        tessellator.draw();
    }

    public void drawTexturedModalRect(int xCoord, int yCoord, TextureAtlasSprite textureSprite, int widthIn, int heightIn) {
        double zLevel = getRenderZLevel();
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos((double) (xCoord + 0), (double) (yCoord + heightIn), zLevel).tex((double) textureSprite.getMinU(), (double) textureSprite.getMaxV()).endVertex();
        vertexbuffer.pos((double) (xCoord + widthIn), (double) (yCoord + heightIn), zLevel).tex((double) textureSprite.getMaxU(), (double) textureSprite.getMaxV()).endVertex();
        vertexbuffer.pos((double) (xCoord + widthIn), (double) (yCoord + 0), zLevel).tex((double) textureSprite.getMaxU(), (double) textureSprite.getMinV()).endVertex();
        vertexbuffer.pos((double) (xCoord + 0), (double) (yCoord + 0), zLevel).tex((double) textureSprite.getMinU(), (double) textureSprite.getMinV()).endVertex();
        tessellator.draw();
    }

    public void drawModalRectWithCustomSizedTexture(double x, double y, double u, double v, double width, double height, double textureWidth, double textureHeight) {
        double zLevel = getRenderZLevel();
        double f = 1.0D / textureWidth;
        double f1 = 1.0D / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos(x, (y + height), zLevel).tex((u * f), ((v + height) * f1)).endVertex();
        vertexbuffer.pos((x + width), (y + height), zLevel).tex(((u + width) * f), ((v + height) * f1)).endVertex();
        vertexbuffer.pos((x + width), y, zLevel).tex(((u + width) * f), (v * f1)).endVertex();
        vertexbuffer.pos(x, y, zLevel).tex((u * f), (v * f1)).endVertex();
        tessellator.draw();
    }

    public void drawScaledCustomSizeModalRect(double x, double y, double u, double v, double uWidth, double vHeight, double width, double height, double tileWidth, double tileHeight) {
        double zLevel = getRenderZLevel();
        double f = 1.0F / tileWidth;
        double f1 = 1.0F / tileHeight;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos(x, (y + height), zLevel).tex((u * f), ((v + vHeight) * f1)).endVertex();
        vertexbuffer.pos((x + width), (y + height), zLevel).tex(((u + uWidth) * f), ((v + vHeight) * f1)).endVertex();
        vertexbuffer.pos((x + width), y, zLevel).tex(((u + uWidth) * f), (v * f1)).endVertex();
        vertexbuffer.pos(x, y, zLevel).tex((u * f), (v * f1)).endVertex();
        tessellator.draw();
    }

    //endregion

    //region Custom Render Helpers

    public void drawString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, getRenderZLevel());
        fontRendererIn.drawStringWithShadow(text, (float) x, (float) y, color);
        GlStateManager.popMatrix();
    }

    public void drawCenteredString(FontRenderer fontRenderer, String text, int x, int y, int color, boolean dropShadow) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, getRenderZLevel());
        fontRenderer.drawString(text, (float) (x - fontRenderer.getStringWidth(text) / 2), (float) y, color, dropShadow);
        GlStateManager.popMatrix();
    }

    public void drawCenteredSplitString(FontRenderer fontRenderer, String str, int x, int y, int wrapWidth, int color, boolean dropShadow) {
        for (String s : fontRenderer.listFormattedStringToWidth(str, wrapWidth)) {
            drawCenteredString(fontRenderer, s, x, y, color, dropShadow);
            y += fontRenderer.FONT_HEIGHT;
        }
    }

    public void drawHoveringText(List list, int x, int y, FontRenderer font, int guiWidth, int guiHeight) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, -300 + getRenderZLevel());
        net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(list, x, y, guiWidth, guiHeight, -1, font);
        GlStateManager.popMatrix();
    }

    public void drawGradientRect(double left, double top, double right, double bottom, int colour1, int colour2) {
        double zLevel = getRenderZLevel();
        float alpha1 = ((colour1 >> 24 & 255) / 255.0F);
        float red1 = (float) (colour1 >> 16 & 255) / 255.0F;
        float green1 = (float) (colour1 >> 8 & 255) / 255.0F;
        float blue1 = (float) (colour1 & 255) / 255.0F;
        float alpha2 = ((colour2 >> 24 & 255) / 255.0F);
        float red2 = (float) (colour2 >> 16 & 255) / 255.0F;
        float green2 = (float) (colour2 >> 8 & 255) / 255.0F;
        float blue2 = (float) (colour2 & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos(right, top, zLevel).color(red1, green1, blue1, alpha1).endVertex();
        vertexbuffer.pos(left, top, zLevel).color(red1, green1, blue1, alpha1).endVertex();
        vertexbuffer.pos(left, bottom, zLevel).color(red2, green2, blue2, alpha2).endVertex();
        vertexbuffer.pos(right, bottom, zLevel).color(red2, green2, blue2, alpha2).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public void drawColouredRect(double posX, double posY, double xSize, double ySize, int colour) {
        drawGradientRect(posX, posY, posX + xSize, posY + ySize, colour, colour);
    }

    public void drawBorderedRect(double posX, double posY, double xSize, double ySize, double borderWidth, int fillColour, int borderColour) {
        drawColouredRect(posX, posY, xSize, borderWidth, borderColour);
        drawColouredRect(posX, posY + ySize - borderWidth, xSize, borderWidth, borderColour);
        drawColouredRect(posX, posY + borderWidth, borderWidth, ySize - (2 * borderWidth), borderColour);
        drawColouredRect(posX + xSize - borderWidth, posY + borderWidth, borderWidth, ySize - (2 * borderWidth), borderColour);
        drawColouredRect(posX + borderWidth, posY + borderWidth, xSize - (2 * borderWidth), ySize - (2 * borderWidth), fillColour);
    }

    //endregion
}