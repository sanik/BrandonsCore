package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.utils.GuiHelper;

import java.io.IOException;
import java.util.function.Supplier;

public class GuiDraggable extends MGuiElementBase<GuiDraggable> {

    protected int dragXOffset = 0;
    protected int dragYOffset = 0;
    protected boolean dragging = false;
    protected Supplier<Boolean> canDrag = () -> true;
    protected PositionValidator dragZoneValidator = null;
    protected int dragBarHeight = 20;
    protected Runnable onMovedCallback = null;
    protected PositionRestraint positionRestraint = MGuiElementBase::normalizePosition;

    public void setCanDrag(Supplier<Boolean> canDrag) {
        this.canDrag = canDrag;
    }

    public void setDragBarHeight(int dragBarHeight) {
        this.dragBarHeight = dragBarHeight;
    }

    public void setDragZoneValidator(PositionValidator dragZoneValidator) {
        this.dragZoneValidator = dragZoneValidator;
    }

    public void setOnMovedCallback(Runnable onMovedCallback) {
        this.onMovedCallback = onMovedCallback;
    }

    public void setPositionRestraint(PositionRestraint positionRestraint) {
        this.positionRestraint = positionRestraint;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        boolean captured = super.mouseClicked(mouseX, mouseY, mouseButton);

        if (!captured && canDrag.get() && (dragZoneValidator != null ? dragZoneValidator.validate(mouseX, mouseY) : GuiHelper.isInRect(xPos(), yPos(), xSize(), dragBarHeight, mouseX, mouseY))) {
            dragging = true;
            dragXOffset = mouseX - xPos();
            dragYOffset = mouseY - yPos();
        }

        return captured;
    }

    @Override
    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging) {
            int xMove = (mouseX - dragXOffset) - xPos();
            int yMove = (mouseY - dragYOffset) - yPos();
            translate(xMove, yMove);

            validatePosition();

            if (onMovedCallback != null) {
                onMovedCallback.run();
            }
        }
        return super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void reloadElement() {
        super.reloadElement();
        if (xSize() > 0 && ySize() > 0) {
            validatePosition();
        }
    }

    private void validatePosition() {
        int x = xPos();
        int y = yPos();
        positionRestraint.restrainPosition(this);
        if ((x != xPos() || y != yPos()) && onMovedCallback != null) {
            onMovedCallback.run();
        }
    }

    public interface PositionRestraint {
        void restrainPosition(GuiDraggable draggable);
    }

    public interface PositionValidator {
        boolean validate(int x, int y);
    }
}