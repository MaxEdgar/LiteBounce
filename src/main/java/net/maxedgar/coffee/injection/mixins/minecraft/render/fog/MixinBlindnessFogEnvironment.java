/*
 * This file is part of Coffee (https://github.com/MaxEdgar/Coffee)
 *
 * Copyright (c) 2015 - 2026 CCBlueX
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

package net.maxedgar.coffee.injection.mixins.minecraft.render.fog;

import net.maxedgar.coffee.features.module.modules.render.DoRender;
import net.maxedgar.coffee.features.module.modules.render.ModuleAntiBlind;
import net.minecraft.client.renderer.fog.environment.BlindnessFogEnvironment;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlindnessFogEnvironment.class)
public abstract class MixinBlindnessFogEnvironment {
    @Inject(method = "getMobEffect()Lnet/minecraft/core/Holder;", at = @At("HEAD"), cancellable = true)
    public void hookGetStatusEffect(CallbackInfoReturnable<Holder<MobEffect>> cir) {
        if (!ModuleAntiBlind.canRender(DoRender.BLINDING)) {
            cir.setReturnValue(null);
        }
    }
}
