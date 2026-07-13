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
package net.maxedgar.coffee.interfaces;

import org.jspecify.annotations.Nullable;

/**
 * Additions to {@link net.minecraft.client.multiplayer.chat.GuiMessage} and
 * {@link net.minecraft.client.multiplayer.chat.GuiMessage.Line}.
 */
public interface GuiMessageLineAddition {

    /**
     * Sets the ID for the chat message.
     * The ID will be used for removing chat messages.
     */
    void liquid_bounce$setId(@Nullable String id);

    /**
     * Gets the ID of the chat message.
     */
    @Nullable String liquid_bounce$getId();

}
