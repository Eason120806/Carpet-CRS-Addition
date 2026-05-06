/*
 * This file is part of the Carpet CRS Addition project, licensed under the
 * GNU General Public License v3.0
 *
 * Copyright (C) 2025  Eason120806 and contributors
 *
 * Carpet CRS Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Carpet CRS Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Carpet CRS Addition.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.eason120806.carpetcrsaddition.utils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

import java.util.Comparator;

public class ChunkUtils {
    public static final TicketType<ChunkPos> ENDER_PEARL = TicketType.create(
            "ender_pearl",
            Comparator.comparingLong(ChunkPos::toLong),
            40
    );

    public static int getSectionCoordFloored(double coord) {
        return Mth.floor(coord) >> 4;
    }

    public static int getSectionCoord(int coord) {
        return coord >> 4;
    }

    public static long addEnderPearlTicket(ServerLevel world, ChunkPos chunkPos) {
        world.getChunkSource().addRegionTicket(ENDER_PEARL, chunkPos, 2, chunkPos);
        return 40L;
    }
}