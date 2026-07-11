/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2026 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */

package net.ccbluex.liquidbounce.injection.mixins.minecraft.gui.custom;

import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.AltManagerScreen;
import net.ccbluex.liquidbounce.injection.mixins.minecraft.gui.MixinScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends MixinScreen {

    @Unique
    private Button altManagerButton;

    @Inject(method = "init", at = @At("TAIL"))
    private void addAltManagerButton(CallbackInfo ci) {
        int x = 4;
        int y = this.height - 24;

        altManagerButton = Button.builder(
                Component.literal("Alt Manager"),
                button -> Minecraft.getInstance().gui.setScreen(new AltManagerScreen())
        ).bounds(x, y, 80, 20).build();

        addRenderableWidget(altManagerButton);
    }

    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void moveAltManagerButton(CallbackInfo ci) {
        if (altManagerButton != null) {
            altManagerButton.setPosition(4, this.height - 24);
        }
    }

}
