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
    private @Nullable String coffee$id = null;

    @Unique
    private int coffee$count = 1;

    @Unique
    @Override
    public void coffee$setId(@Nullable String id) {
        this.coffee$id = id;
    }

    @Unique
    @Override
    public @Nullable String coffee$getId() {
        return coffee$id;
    }

    @Unique
    @Override
    public void coffee$setCount(int count) {
        this.coffee$count = count;
    }

    @Unique
    @Override
    public int coffee$getCount() {
        return coffee$count;
    }

}
