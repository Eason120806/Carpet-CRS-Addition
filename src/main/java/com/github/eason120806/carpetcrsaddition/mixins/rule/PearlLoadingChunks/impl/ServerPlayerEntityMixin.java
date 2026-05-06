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

package com.github.eason120806.carpetcrsaddition.mixins.rule.PearlLoadingChunks.impl;

import com.github.eason120806.carpetcrsaddition.interfaces.ServerPlayerEntityInterface;
import com.github.eason120806.carpetcrsaddition.utils.ChunkUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin implements ServerPlayerEntityInterface {
    @Unique
    private final Set<ThrownEnderpearl> enderPearls = new HashSet<>();

    @Override
    public long pearl$handleThrownEnderPearl(ThrownEnderpearl enderPearl) {
        if (enderPearl.level() instanceof ServerLevel serverLevel) {
            ChunkPos chunkPos = enderPearl.chunkPosition();
            this.pearl$addEnderPearl(enderPearl);
            serverLevel.resetEmptyTime();
            return ChunkUtils.addEnderPearlTicket(serverLevel, chunkPos) - 1L;
        } else {
            return 0L;
        }
    }

    @Override
    public void pearl$addEnderPearl(ThrownEnderpearl enderPearl) {
        this.enderPearls.add(enderPearl);
    }

    @Override
    public void pearl$removeEnderPearl(ThrownEnderpearl enderPearl) {
        this.enderPearls.remove(enderPearl);
    }

    @Override
    public Set<ThrownEnderpearl> pearl$getEnderPearls() {
        return this.enderPearls;
    }
}