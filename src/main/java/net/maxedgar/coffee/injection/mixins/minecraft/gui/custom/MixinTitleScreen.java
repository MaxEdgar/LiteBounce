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

package net.maxedgar.coffee.injection.mixins.minecraft.gui.custom;

import net.maxedgar.coffee.features.module.modules.render.clickgui.AltManagerScreen;
import net.maxedgar.coffee.injection.mixins.minecraft.gui.MixinScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends MixinScreen {

    @Inject(method = "init", at = @At("TAIL"))
    private void addAltManagerButton(CallbackInfo ci) {
        int x = 4;
        int y = this.height - 24;

        var button = Button.builder(
                Component.literal("Alt Manager"),
                btn -> Minecraft.getInstance().gui.setScreen(new AltManagerScreen())
        ).bounds(x, y, 80, 20).build();

        addRenderableWidget(button);
    }

}
