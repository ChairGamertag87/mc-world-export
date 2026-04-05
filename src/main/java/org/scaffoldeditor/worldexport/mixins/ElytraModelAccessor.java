package org.scaffoldeditor.worldexport.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.ElytraEntityModel;

@Mixin(ElytraEntityModel.class)
public interface ElytraModelAccessor {

    @Accessor("leftWing")
    ModelPart getLeftWing();

    @Accessor("rightWing")
    ModelPart getRightWing();
}
