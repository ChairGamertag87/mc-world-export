package org.scaffoldeditor.worldexport.replay.model_adapters.custom;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.scaffoldeditor.worldexport.mat.MaterialConsumer;
import org.scaffoldeditor.worldexport.replay.model_adapters.ReplayModelAdapter;
import org.scaffoldeditor.worldexport.replay.models.MultipartReplayModel;
import org.scaffoldeditor.worldexport.replay.models.ReplayItemRenderer;
import org.scaffoldeditor.worldexport.replay.models.ReplayModelPart;
import org.scaffoldeditor.worldexport.replay.models.Transform;
import org.scaffoldeditor.worldexport.replay.models.ReplayModel.Pose;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.item.ItemStack;

/**
 * Generic model adapter for thrown item entities that implement
 * {@link FlyingItemEntity} (snowball, ender pearl, egg, wind charge, etc.).
 * The item stack returned by the entity is baked into the replay model using
 * the GROUND transformation mode, matching the vanilla FlyingItemEntityRenderer.
 */
public class FlyingItemModelAdapter implements ReplayModelAdapter<MultipartReplayModel> {

    private final Entity entity;
    private final FlyingItemEntity flyingItem;
    private final ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

    private MultipartReplayModel model;
    private ReplayModelPart base;
    private BakedModel itemModel;

    private final float scale;

    public FlyingItemModelAdapter(Entity entity) {
        this(entity, 1f);
    }

    public FlyingItemModelAdapter(Entity entity, float scale) {
        if (!(entity instanceof FlyingItemEntity flying)) {
            throw new ClassCastException("Entity " + entity + " does not implement FlyingItemEntity.");
        }
        this.entity = entity;
        this.flyingItem = flying;
        this.scale = scale;
        genModel();
    }

    private void genModel() {
        model = new MultipartReplayModel();
        base = new ReplayModelPart("item");
        model.bones.add(base);

        ItemStack stack = flyingItem.getStack();
        itemModel = itemRenderer.getModel(stack, entity.getWorld(), null, entity.getId());
        ReplayItemRenderer.renderItem(stack, ModelTransformationMode.GROUND, false,
                new MatrixStack(), base.getMesh(), itemModel);
    }

    @Override
    public MultipartReplayModel getModel() {
        return model;
    }

    @Override
    public void generateMaterials(MaterialConsumer file) {
        ReplayItemRenderer.addMaterials(file);
    }

    @Override
    public Pose<ReplayModelPart> getPose(float tickDelta) {
        Vector3d pos = new Vector3d(entity.getX(), entity.getY(), entity.getZ());
        Quaterniond rot = new Quaterniond();

        Pose<ReplayModelPart> pose = new Pose<>();
        pose.root = new Transform(pos, rot, new Vector3d(scale));
        pose.bones.put(base, Transform.NEUTRAL);

        return pose;
    }

    public Entity getEntity() {
        return entity;
    }

    public BakedModel getItemModel() {
        return itemModel;
    }
}
