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

package net.maxedgar.coffee.injection.mixins.minecraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.maxedgar.coffee.event.EventManager;
import net.maxedgar.coffee.event.events.KeyEvent;
import net.maxedgar.coffee.event.events.KeyboardCharEvent;
import net.maxedgar.coffee.event.events.KeyboardKeyEvent;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class MixinKeyboardHandler {

    @Shadow
    @Final
    private Minecraft minecraft;

    /**
     * Hook key event
     */
    @Inject(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;screen()Lnet/minecraft/client/gui/screens/Screen;", shift = At.Shift.BEFORE, ordinal = 0))
    private void hookKeyboardKey(long l, int action, net.minecraft.client.input.KeyEvent keyEvent, CallbackInfo ci) {
        // does if (window == this.client.getWindow().getHandle())
        var inputKey = InputConstants.getKey(keyEvent);

        EventManager.INSTANCE.callEvent(new KeyboardKeyEvent(
            inputKey, keyEvent.key(),
            keyEvent.scancode(), action,
            keyEvent.modifiers(), this.minecraft.gui.screen()
        ));
        if (minecraft.gui.screen() == null) {
            EventManager.INSTANCE.callEvent(new KeyEvent(inputKey, action));
        }
    }

    /**
     * Hook char event
     */
    @Inject(method = "charTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;screen()Lnet/minecraft/client/gui/screens/Screen;", shift = At.Shift.BEFORE))
    private void hookKeyboardChar(long window, CharacterEvent input, CallbackInfo ci) {
        // does if (window == this.client.getWindow().getHandle())
        EventManager.INSTANCE.callEvent(new KeyboardCharEvent(input.codepoint()));
    }

}
