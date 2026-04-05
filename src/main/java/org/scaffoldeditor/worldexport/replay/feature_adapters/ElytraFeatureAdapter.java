package org.scaffoldeditor.worldexport.replay.feature_adapters;

import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.scaffoldeditor.worldexport.mat.Material;
import org.scaffoldeditor.worldexport.mat.MaterialConsumer;
import org.scaffoldeditor.worldexport.mat.MaterialUtils;
import org.scaffoldeditor.worldexport.mat.PromisedReplayTexture;
import org.scaffoldeditor.worldexport.mat.ReplayTexture;
import org.scaffoldeditor.worldexport.mixins.ElytraModelAccessor;
import org.scaffoldeditor.worldexport.replay.model_adapters.BipedModelAdapter;
import org.scaffoldeditor.worldexport.replay.model_adapters.LivingEntityModelAdapter;
import org.scaffoldeditor.worldexport.replay.models.ReplayModel.Pose;
import org.scaffoldeditor.worldexport.replay.models.ReplayModelPart;
import org.scaffoldeditor.worldexport.replay.models.Transform;
import org.scaffoldeditor.worldexport.util.MeshUtils;
import org.scaffoldeditor.worldexport.util.ModelUtils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

/**
 * Feature adapter that captures the elytra wings' animation whenever the
 * underlying entity is wearing an elytra. The wings are appended as children of
 * the base model's body bone and each frame their rotations are driven by the
 * vanilla {@link ElytraEntityModel} using the same animation parameters used
 * for the main entity model.
 */
public class ElytraFeatureAdapter implements ReplayFeatureAdapter<ReplayModelPart> {

    private static final Identifier ELYTRA_TEXTURE = Identifier.of("textures/entity/elytra.png");

    private final BipedModelAdapter<?> baseModel;
    private final ElytraEntityModel<LivingEntity> elytraModel;
    private final ModelPart elytraLeftWing;
    private final ModelPart elytraRightWing;

    private ReplayModelPart leftWing;
    private ReplayModelPart rightWing;
    private boolean initialized = false;

    public ElytraFeatureAdapter(BipedModelAdapter<?> baseModel) {
        this.baseModel = baseModel;
        ModelPart root = MinecraftClient.getInstance()
                .getEntityModelLoader()
                .getModelPart(EntityModelLayers.ELYTRA);
        this.elytraModel = new ElytraEntityModel<>(root);

        ElytraModelAccessor accessor = (ElytraModelAccessor) (Object) this.elytraModel;
        this.elytraLeftWing = accessor.getLeftWing();
        this.elytraRightWing = accessor.getRightWing();
    }

    private void init() {
        String texName = MaterialUtils.getTexName(ELYTRA_TEXTURE);

        leftWing = new ReplayModelPart("elytra.left_wing");
        leftWing.getMesh().setActiveMaterialGroupName(texName);
        MeshUtils.appendModelPart(elytraLeftWing, leftWing.getMesh(), false, null);

        rightWing = new ReplayModelPart("elytra.right_wing");
        rightWing.getMesh().setActiveMaterialGroupName(texName);
        MeshUtils.appendModelPart(elytraRightWing, rightWing.getMesh(), false, null);

        // Wings hang off the body bone so they inherit the player's back position.
        baseModel.getBody().children.add(leftWing);
        baseModel.getBody().children.add(rightWing);

        initialized = true;
    }

    @Override
    public void writePose(Pose<ReplayModelPart> pose, float tickDelta) {
        if (!initialized) init();

        LivingEntity entity = baseModel.getEntity();
        boolean visible = isWearingElytra(entity);

        if (visible) {
            LivingEntityModelAdapter<?, ?> src = baseModel;
            // Re-run the elytra model's setAngles with the same parameters the main
            // model used this frame so the wing rotations stay in sync.
            elytraModel.setAngles(entity,
                    src.getLastLimbAngle(),
                    src.getLastLimbDistance(),
                    src.getLastAnimationProgress(),
                    src.getLastHeadYaw(),
                    src.getLastHeadPitch());

            writeWingTransform(pose, leftWing, elytraLeftWing);
            writeWingTransform(pose, rightWing, elytraRightWing);
        } else {
            pose.bones.put(leftWing, new Transform(false));
            pose.bones.put(rightWing, new Transform(false));
        }
    }

    private static void writeWingTransform(Pose<ReplayModelPart> pose, ReplayModelPart bone, ModelPart part) {
        Matrix4d local = new Matrix4d();
        ModelUtils.getPartTransform(part, local);

        Vector3d translation = local.getTranslation(new Vector3d());
        Vector3d scale = local.getScale(new Vector3d());
        Quaterniond rotation = local.getUnnormalizedRotation(new Quaterniond());

        pose.bones.put(bone, new Transform(translation, rotation, scale, true));
    }

    private static boolean isWearingElytra(LivingEntity entity) {
        ItemStack chest = entity.getEquippedStack(EquipmentSlot.CHEST);
        return !chest.isEmpty() && chest.isOf(Items.ELYTRA);
    }

    @Override
    public void generateMaterials(MaterialConsumer consumer) {
        String texName = MaterialUtils.getTexName(ELYTRA_TEXTURE);
        if (consumer.hasMaterial(texName)) return;

        Material material = new Material();
        material.setColor(texName);
        material.setRoughness(1);
        material.setTransparent(true);

        ReplayTexture tex = new PromisedReplayTexture(ELYTRA_TEXTURE);
        consumer.addTexture(texName, tex);
        consumer.addMaterial(texName, material);
    }
}
