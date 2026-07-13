/*
 * This file is part of Coffee (https://github.com/MaxEdgar/Coffee)
 *
 * Copyright (c) 2025 MaxEdgar
 *
 * Coffee is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Coffee is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Coffee. If not, see <https://www.gnu.org/licenses/>.
 */
package net.maxedgar.coffee.injection.mixins.minecraft.render.entity.feature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.maxedgar.coffee.api.models.cosmetics.CosmeticCategory;
import net.maxedgar.coffee.features.cosmetic.CosmeticService;
import net.maxedgar.coffee.interfaces.EntityRenderStateAddition;
import net.minecraft.client.renderer.entity.layers.Deadmau5EarsLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Deadmau5EarsLayer.class)
public abstract class MixinDeadmau5EarsLayer {

    @ModifyExpressionValue(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;showExtraEars:Z", remap = false, opcode = Opcodes.GETFIELD))
    private boolean onRender(boolean original, @Local(argsOnly = true, name = "state") AvatarRenderState playerEntityRenderState) {
        if (original) return true;

        var entity = ((EntityRenderStateAddition) playerEntityRenderState).liquid_bounce$getEntity();
        return entity != null && CosmeticService.INSTANCE.hasCosmetic(entity.getUUID(), CosmeticCategory.DEADMAU5_EARS);
    }

}
