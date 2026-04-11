package org.scaffoldeditor.worldexport.gui.bounds_editor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.scaffoldeditor.worldexport.util.Box2i;

import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiSlider;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.VerticalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.popup.AbstractGuiPopup;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class GuiBoundsEditor extends AbstractGuiPopup<GuiBoundsEditor> {

    private final GuiBoundsOverview overview;

    private int minSection;
    private int maxSection = 16;

    private List<Runnable> closeListeners = new LinkedList<>();

    public final GuiLabel titleLabel = new GuiLabel()
            .setI18nText("worldexport.gui.export.bounds")
            .setColor(Colors.WHITE);

    public final GuiLabel boundsInfoLabel = new GuiLabel()
            .setColor(Colors.LIGHT_GRAY);

    public final GuiSlider upperLimitSlider = new GuiSlider().onValueChanged(this::handleChangeUpperLimit)
            .setHeight(20).setSteps(32);

    private void handleChangeUpperLimit() {
        upperLimitSlider.setI18nText("worldexport.gui.export.upper_limit", getUpperLimit() * 16 + 15);
        overview.setTopY(getUpperLimit() * 16 + 15);

        if (getUpperLimit() < getLowerDepth()) {
            setLowerDepth(getUpperLimit());
        }
        updateBoundsInfo();
    }

    public void setUpperLimit(int upperLimit) {
        upperLimitSlider.setSteps(maxSection - minSection);
        upperLimitSlider.setValue(upperLimit - minSection);
    }

    public int getUpperLimit() {
        return upperLimitSlider.getValue() + minSection;
    }

    public final GuiSlider lowerDepthSlider = new GuiSlider().onValueChanged(this::handleChangeLowerDepth)
            .setHeight(20).setSteps(32);

    private void handleChangeLowerDepth() {
        lowerDepthSlider.setI18nText("worldexport.gui.export.lower_depth", getLowerDepth() * 16);
        overview.setBottomY(getLowerDepth() * 16);

        if (getLowerDepth() > getUpperLimit()) {
            setUpperLimit(getLowerDepth());
        }
        updateBoundsInfo();
    }

    public void setLowerDepth(int lowerDepth) {
        lowerDepthSlider.setSteps(maxSection - minSection);
        lowerDepthSlider.setValue(lowerDepth - minSection);
    }

    public int getLowerDepth() {
        return lowerDepthSlider.getValue() + minSection;
    }

    private final GuiButton applyButton = new GuiButton()
            .setI18nLabel("worldexport.gui.export.apply")
            .setSize(100, 20)
            .onClick(this::close);

    private final GuiPanel bottomPanel = new GuiPanel().setLayout(new VerticalLayout().setSpacing(5))
            .addElements(new VerticalLayout.Data(0.5), upperLimitSlider, lowerDepthSlider);

    public GuiBoundsEditor(GuiContainer<?> container, World world, int width, int height, ChunkPos rootPos) {
        super(container);

        minSection = world.getBottomSectionCoord();
        maxSection = world.getTopSectionCoord();

        overview = new GuiBoundsOverview(world, new OverviewData(width, height, rootPos));
        popup.setLayout(new CustomLayout<GuiPanel>() {

            @Override
            protected void layout(GuiPanel panel, int width, int height) {
                // Title at top
                pos(titleLabel, width / 2 - width(titleLabel) / 2, 5);

                // Apply button at bottom
                pos(applyButton, width / 2 - width(applyButton) / 2, height - height(applyButton) - 5);

                // Bottom panel (sliders) above the button
                pos(bottomPanel, 0, height - height(applyButton) - 10 - height(bottomPanel) - 5);
                width(bottomPanel, width);

                // Bounds info above sliders
                pos(boundsInfoLabel, width / 2 - width(boundsInfoLabel) / 2,
                        y(bottomPanel) - height(boundsInfoLabel) - 5);

                // Overview map fills the remaining space
                int mapTop = y(titleLabel) + height(titleLabel) + 8;
                int mapBottom = y(boundsInfoLabel) - 8;
                pos(overview, 0, mapTop);
                width(overview, width);
                height(overview, Math.max(64, mapBottom - mapTop));
            }

            @Override
            public ReadableDimension calcMinSize(GuiContainer<?> localContainer) {
                ReadableDimension containerMin = container.getMinSize();
                int popupWidth = Math.min(320, (int)(containerMin.getWidth() * 0.65));
                int popupHeight = Math.min(350, (int)(containerMin.getHeight() * 0.7));
                return new Dimension(popupWidth, popupHeight);
            }

        }).addElements(null, titleLabel, overview, boundsInfoLabel, bottomPanel, applyButton);

        setLowerDepth(minSection);
        setUpperLimit(maxSection);
    }

    private void updateBoundsInfo() {
        Box2i b = overview.getBounds();
        int chunksX = Math.abs(b.getX2() - b.getX1()) + 1;
        int chunksZ = Math.abs(b.getY2() - b.getY1()) + 1;
        boundsInfoLabel.setText(chunksX + " x " + chunksZ + " chunks"
                + "  |  Y: " + (getLowerDepth() * 16) + " to " + (getUpperLimit() * 16 + 15));
    }

    public GuiBoundsOverview getOverview() {
        return overview;
    }

    @Override
    protected GuiBoundsEditor getThis() {
        return this;
    }

    public BlockBox getBounds() {
        Box2i bounds = overview.getBounds();
        return new BlockBox(bounds.getX1(), getLowerDepth(), bounds.getY1(),
                bounds.getX2(), getUpperLimit(), bounds.getY2());
    }

    public void setBounds(BlockBox bounds) {
        Box2i bounds2d = new Box2i(bounds.getMinX(), bounds.getMinZ(), bounds.getMaxX(), bounds.getMaxZ());

        overview.setBounds(bounds2d);
        setLowerDepth(bounds.getMinY());
        setUpperLimit(bounds.getMaxY());
        updateBoundsInfo();
    }

    @Override
    protected void close() {
        super.close();
        overview.close();
        closeListeners.forEach(Runnable::run);
        closeListeners.clear();
    }

    @Override
    public void open() {
        super.open();
    }

    public void onClose(Runnable r) {
        closeListeners.add(r);
    }

    public static CompletableFuture<BlockBox> openEditor(BlockBox defaultBounds, GuiContainer<?> container, World world, int width, int height, ChunkPos rootPos) {
        GuiBoundsEditor editor = new GuiBoundsEditor(container, world, width, height, rootPos);
        editor.setBounds(defaultBounds);

        CompletableFuture<BlockBox> future = new CompletableFuture<>();
        editor.onClose(() -> {
            future.complete(editor.getBounds());
        });

        editor.open();
        return future;
    }
}
