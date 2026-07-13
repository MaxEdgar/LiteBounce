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
package net.maxedgar.coffee.injection.mixins.minecraft.text;

import net.maxedgar.coffee.interfaces.GuiMessageAddition;
import net.maxedgar.coffee.interfaces.GuiMessageLineAddition;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiMessage.class)
public abstract class MixinGuiMessage implements GuiMessageLineAddition, GuiMessageAddition {

    @Unique
    private @Nullable String liquid_bounce$id = null;

    @Unique
    private int liquid_bounce$count = 1;

    @Unique
    @Override
    public void liquid_bounce$setId(@Nullable String id) {
        this.liquid_bounce$id = id;
    }

    @Unique
    @Override
    public @Nullable String liquid_bounce$getId() {
        return liquid_bounce$id;
    }

    @Unique
    @Override
    public void liquid_bounce$setCount(int count) {
        this.liquid_bounce$count = count;
    }

    @Unique
    @Override
    public int liquid_bounce$getCount() {
        return liquid_bounce$count;
    }

}
