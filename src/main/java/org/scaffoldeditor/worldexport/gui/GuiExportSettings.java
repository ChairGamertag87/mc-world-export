package org.scaffoldeditor.worldexport.gui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.scaffoldeditor.worldexport.gui.bounds_editor.GuiBoundsEditor;
import org.scaffoldeditor.worldexport.replaymod.export.ReplayExportSettings;
import org.scaffoldeditor.worldexport.replaymod.export.ReplayExporter;
import org.scaffoldeditor.worldexport.vcap.VcapSettings.FluidMode;

import com.mojang.blaze3d.systems.RenderSystem;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.AbstractGuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiContainer;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiPanel;
import com.replaymod.lib.de.johni0702.minecraft.gui.container.GuiScreen;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiButton;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiLabel;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.GuiTextField;
import com.replaymod.lib.de.johni0702.minecraft.gui.element.advanced.GuiDropdownMenu;
import com.replaymod.lib.de.johni0702.minecraft.gui.function.Closeable;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.CustomLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.layout.HorizontalLayout;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.Colors;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.replaymod.render.ReplayModRender;
import com.replaymod.replay.ReplayHandler;
import com.replaymod.replaystudio.pathing.path.Timeline;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class GuiExportSettings extends GuiScreen implements Closeable {

    public final GuiPanel contentPanel = new GuiPanel(this).setBackgroundColor(Colors.DARK_TRANSPARENT);

    private ReplayHandler replayHandler;
    private Timeline timeline;

    private MinecraftClient client = MinecraftClient.getInstance();

    private File outputFolder;
    private BlockBox bounds;

    @Nullable
    public AbstractGuiScreen<?> prevScreen = null;

    // --- Title ---
    public final GuiLabel titleLabel = new GuiLabel()
            .setI18nText("worldexport.gui.export.title")
            .setColor(Colors.WHITE);

    // --- Output file: text field instead of popup file picker ---
    public final GuiLabel outputFileLabel = new GuiLabel()
            .setI18nText("replaymod.gui.rendersettings.outputfile")
            .setColor(Colors.WHITE);

    public final GuiTextField fileNameField = new GuiTextField()
            .setSize(new Dimension(200, 20))
            .setMaxLength(128);

    public final GuiLabel folderHintLabel = new GuiLabel()
            .setColor(Colors.LIGHT_GRAY);

    // --- Bounds ---
    public final GuiLabel boundsLabel = new GuiLabel()
            .setI18nText("worldexport.gui.export.bounds")
            .setColor(Colors.WHITE);

    public final GuiLabel boundsInfoLabel = new GuiLabel()
            .setColor(Colors.LIGHT_GRAY);

    public final GuiButton boundsEditorButton = new GuiButton()
            .setI18nLabel("worldexport.gui.export.edit_bounds")
            .setSize(200, 20)
            .onClick(this::openBoundsEditor);

    // --- Fluid mode ---
    public final GuiLabel fluidModeLabel = new GuiLabel()
            .setI18nText("worldexport.gui.export.fluid_mode")
            .setColor(Colors.WHITE);

    public final GuiDropdownMenu<FluidMode> fluidModeDropdown = new GuiDropdownMenu<FluidMode>()
            .setMinSize(new Dimension(200, 20))
            .setValues(FluidMode.NONE, FluidMode.STATIC)
            .setSelected(FluidMode.NONE);

    // --- Bottom buttons ---
    public final GuiPanel buttonPanel = new GuiPanel().setLayout(new HorizontalLayout().setSpacing(8));

    public final GuiButton exportButton = new GuiButton(buttonPanel)
            .setI18nLabel("worldexport.gui.export")
            .setSize(100, 20)
            .onClick(this::export);

    public final GuiButton cancelButton = new GuiButton(buttonPanel)
            .setI18nLabel("replaymod.gui.cancel")
            .setSize(100, 20)
            .onClick(this::close);

    public GuiExportSettings(ReplayHandler replayHandler, Timeline timeline) {
        this.replayHandler = replayHandler;
        this.timeline = timeline;
        this.outputFolder = ReplayModRender.instance.getVideoFolder();

        setBackground(Background.NONE);

        // Add all elements to contentPanel
        contentPanel.addElements(null,
                titleLabel,
                outputFileLabel, fileNameField, folderHintLabel,
                boundsLabel, boundsInfoLabel, boundsEditorButton,
                fluidModeLabel, fluidModeDropdown,
                buttonPanel);

        // Layout: stack everything vertically with proper spacing
        contentPanel.setLayout(new CustomLayout<GuiPanel>() {
            private static final int LEFT_MARGIN = 20;
            private static final int SECTION_SPACING = 18;
            private static final int INNER_SPACING = 4;

            @Override
            protected void layout(GuiPanel container, int width, int height) {
                int contentWidth = width - LEFT_MARGIN * 2;
                int fieldWidth = Math.min(260, contentWidth);

                // Title centered at top
                pos(titleLabel, width / 2 - width(titleLabel) / 2, 15);

                int y = 40;

                // Output file section
                pos(outputFileLabel, LEFT_MARGIN, y);
                y += height(outputFileLabel) + INNER_SPACING;

                size(fileNameField, fieldWidth, 20);
                pos(fileNameField, LEFT_MARGIN, y);
                y += 22;

                pos(folderHintLabel, LEFT_MARGIN, y);
                y += height(folderHintLabel) + SECTION_SPACING;

                // Bounds section
                pos(boundsLabel, LEFT_MARGIN, y);
                y += height(boundsLabel) + INNER_SPACING;

                pos(boundsInfoLabel, LEFT_MARGIN, y);
                y += height(boundsInfoLabel) + INNER_SPACING + 2;

                size(boundsEditorButton, fieldWidth, 20);
                pos(boundsEditorButton, LEFT_MARGIN, y);
                y += 22 + SECTION_SPACING;

                // Fluid mode section
                pos(fluidModeLabel, LEFT_MARGIN, y);
                y += height(fluidModeLabel) + INNER_SPACING;

                size(fluidModeDropdown, fieldWidth, 20);
                pos(fluidModeDropdown, LEFT_MARGIN, y);

                // Buttons at the bottom, centered
                pos(buttonPanel, width / 2 - width(buttonPanel) / 2, height - height(buttonPanel) - 15);
            }

            @Override
            public ReadableDimension calcMinSize(GuiContainer<?> container) {
                return new Dimension(300, 250);
            }
        });

        // Screen layout: content panel fills most of the screen
        this.setLayout(new CustomLayout<GuiScreen>() {
            @Override
            protected void layout(GuiScreen screen, int width, int height) {
                int panelWidth = width - 40;
                int panelHeight = height - 40;
                width(contentPanel, panelWidth);
                height(contentPanel, panelHeight);
                pos(contentPanel, 20, 20);
            }
        });

        // Load saved settings or generate defaults
        int minLowerDepth = client.world.getBottomSectionCoord();
        int maxLowerDepth = client.world.getTopSectionCoord();

        BlockPos centerPos = client.getCameraEntity().getBlockPos();
        ChunkPos centerChunk = new ChunkPos(centerPos);

        setBounds(new BlockBox(
                centerChunk.x - 4, minLowerDepth, centerChunk.z - 4,
                centerChunk.x + 4, maxLowerDepth, centerChunk.z + 4));

        setOutputFileName(generateFileName());

        folderHintLabel.setText("Folder: " + outputFolder.getAbsolutePath());

        ReplayExportSettings settings;
        try {
            settings = ReplayExportSettings.readFromFile(replayHandler.getReplayFile());
        } catch (IOException e) {
            LogManager.getLogger().error("Error reading export settings from file.", e);
            settings = null;
        }

        if (settings != null) {
            applySettings(settings);
        }
    }

    public void export() {
        close();

        ReplayExportSettings settings = readSettings();

        try {
            ReplayExporter exporter = new ReplayExporter(settings, replayHandler, timeline);
            exporter.exportReplay();
        } catch (Throwable e) {
            throw new CrashException(CrashReport.create(e, "Exporting replay"));
        }
    }

    public ReplayExportSettings readSettings() {
        return new ReplayExportSettings()
                .setBounds(bounds)
                .setFluidMode(getFluidMode())
                .setOutputFile(getOutputFile());
    }

    public void applySettings(ReplayExportSettings settings) {
        if (settings.getBounds() != null) {
            setBounds(settings.getBounds());
        }

        if (settings.getFluidMode() != null) {
            setFluidMode(settings.getFluidMode());
        }

        File settingsFile = settings.getOutputFile();
        if (settingsFile != null && settingsFile.getParentFile().isDirectory()) {
            outputFolder = settingsFile.getParentFile();
            setOutputFileName(settingsFile.getName());
            folderHintLabel.setText("Folder: " + outputFolder.getAbsolutePath());
        }
    }

    public File getOutputFile() {
        String name = fileNameField.getText().trim();
        if (name.isEmpty()) {
            name = generateFileName();
        }
        if (!name.endsWith(".replay")) {
            name += ".replay";
        }
        return new File(outputFolder, name);
    }

    public void setOutputFileName(String name) {
        fileNameField.setText(name);
    }

    public void setFluidMode(FluidMode fluidMode) {
        fluidModeDropdown.setSelected(fluidMode);
    }

    public FluidMode getFluidMode() {
        return fluidModeDropdown.getSelectedValue();
    }

    public BlockBox getBounds() {
        return bounds;
    }

    public void setBounds(BlockBox bounds) {
        this.bounds = bounds;
        updateBoundsInfo();
    }

    private void updateBoundsInfo() {
        if (bounds != null) {
            int chunksX = (bounds.getMaxX() - bounds.getMinX()) + 1;
            int chunksZ = (bounds.getMaxZ() - bounds.getMinZ()) + 1;
            boundsInfoLabel.setText(chunksX + " x " + chunksZ + " chunks"
                    + "  (Y: " + bounds.getMinY() + " to " + bounds.getMaxY() + ")");
        }
    }

    protected String generateFileName() {
        return new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".replay";
    }

    public void openBoundsEditor() {
        MinecraftClient mc = getMinecraft();
        int radius = mc.options.getClampedViewDistance() * 2;
        ChunkPos centerPos = mc.getCameraEntity().getChunkPos();

        GuiBoundsEditor.openEditor(bounds, this, mc.world, radius * 2, radius * 2,
                new ChunkPos(centerPos.x - radius, centerPos.z - radius)).thenAccept(this::setBounds);
    }

    @Override
    @SuppressWarnings("null")
    public void close() {
        try {
            ReplayExportSettings.writeToFile(replayHandler.getReplayFile(), readSettings());
        } catch (IOException e) {
            LogManager.getLogger().error("Error saving export settings to file.", e);
        }

        if (prevScreen != null) {
            RenderSystem.recordRenderCall(prevScreen::display);
        } else {
            client.setScreen(null);
        }
    }
}
