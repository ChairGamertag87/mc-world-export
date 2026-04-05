package org.scaffoldeditor.worldexport.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import org.scaffoldeditor.worldexport.replaymod.util.FovProvider;
import org.scaffoldeditor.worldexport.replaymod.util.RollProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RotationAxis;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow @Final
    private MinecraftClient client;

    @Inject(method = "getFov", at = @At("HEAD"), cancellable = true)
    void overrideFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> ci) {
        Entity entity = this.client.getCameraEntity() == null ? this.client.player : this.client.getCameraEntity();
        if (!camera.isThirdPerson() && entity instanceof FovProvider) {
            ci.setReturnValue(((FovProvider) entity).getFov());
        }
    }

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;bobView(Lnet/minecraft/client/util/math/MatrixStack;F)V", shift = At.Shift.AFTER))
    void applyRoll(RenderTickCounter tickCounter, CallbackInfo ci, @Local MatrixStack matrices) {
        Entity entity = this.client.getCameraEntity() == null ? this.client.player : this.client.getCameraEntity();
        if (entity instanceof RollProvider rollProvider) {
            matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(rollProvider.getRoll()));
        }
    }
}
