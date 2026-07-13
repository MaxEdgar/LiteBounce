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
package net.maxedgar.coffee.injection.mixins.minecraft.render;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.platform.FramerateLimitTracker;
import net.maxedgar.coffee.event.EventManager;
import net.maxedgar.coffee.event.events.FpsLimitEvent;
import net.maxedgar.coffee.utils.render.RefreshRateKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = FramerateLimitTracker.class, priority = 100)
public abstract class MixinFramerateLimitTracker {

    /**
     * Removes frame rate limit
     */
    @ModifyConstant(method = "getFramerateLimit", constant = @Constant(intValue = 60), require = 0)
    private int getFramerateLimit(int original) {
        return RefreshRateKt.getRefreshRate();
    }

    @ModifyReturnValue(method = "getFramerateLimit", at = @At("RETURN"))
    private int hookFpsLimit(int original) {
        return EventManager.INSTANCE.callEvent(new FpsLimitEvent(original)).getFps();
    }

}
