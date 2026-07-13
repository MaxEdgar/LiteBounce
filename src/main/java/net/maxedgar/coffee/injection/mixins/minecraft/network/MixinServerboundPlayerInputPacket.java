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

package net.maxedgar.coffee.injection.mixins.minecraft.network;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.maxedgar.coffee.additions.ServerboundPlayerInputPacketAddition;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@NullMarked
@Mixin(ServerboundPlayerInputPacket.class)
public abstract class MixinServerboundPlayerInputPacket implements ServerboundPlayerInputPacketAddition {

    @Shadow
    @Final
    private Input input;

    @Unique
    private boolean coffee$forceSneak = false;

    @Override
    public void setCoffee$forceSneak(boolean b) {
        this.coffee$forceSneak = b;
    }

    @Override
    public boolean getCoffee$forceSneak() {
        return this.coffee$forceSneak;
    }

    public Input coffee$getRawInput() {
        return this.input;
    }

    @ModifyReturnValue(method = "input", at = @At("RETURN"))
    private Input applyForceSneak(Input original) {
        if (this.coffee$forceSneak && !original.shift()) {
            return new Input(
                original.forward(),
                original.backward(),
                original.left(),
                original.right(),
                original.jump(),
                true,
                original.sprint()
            );
        } else {
            return original;
        }
    }

}
