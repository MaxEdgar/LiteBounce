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
package net.maxedgar.coffee.interfaces;

import net.maxedgar.coffee.features.module.modules.misc.betterchat.ModuleBetterChat;

/**
 * Additions to {@link net.minecraft.client.GuiMessage}.
 */
public interface GuiMessageAddition {

    /**
     * Sets the count of the message.
     * This indicates how many times this massage has already been sent in {@link ModuleBetterChat}.
     */
    void liquid_bounce$setCount(int count);

    /**
     * Gets the count stored in this line.
     */
    @SuppressWarnings("unused")
    int liquid_bounce$getCount();

}
