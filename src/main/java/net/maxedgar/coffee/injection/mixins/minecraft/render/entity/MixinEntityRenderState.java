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
package net.maxedgar.coffee.injection.mixins.minecraft.render.entity;

import net.maxedgar.coffee.interfaces.EntityRenderStateAddition;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class MixinEntityRenderState implements EntityRenderStateAddition {

    @Unique
    private Entity coffee$entity;

    @Unique
    private boolean coffee$isCustom = false;

    @Unique
    @Override
    public void coffee$setEntity(Entity entity) {
        this.coffee$entity = entity;
    }

    @Unique
    @Override
    public Entity coffee$getEntity() {
        return coffee$entity;
    }

    @Override
    public boolean coffee$isCustom() {
        return coffee$isCustom;
    }

    @Override
    public void coffee$setCustom(boolean custom) {
        coffee$isCustom = custom;
    }
}
