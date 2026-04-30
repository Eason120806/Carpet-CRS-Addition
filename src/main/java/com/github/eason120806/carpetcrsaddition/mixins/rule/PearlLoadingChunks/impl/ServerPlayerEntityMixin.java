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
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements ServerPlayerEntityInterface {
    @Unique
    private final Set<EnderPearlEntity> enderPearls = new HashSet<>();

    @Override
    public long pearl$handleThrownEnderPearl(EnderPearlEntity enderPearl) {
        if (enderPearl.getWorld() instanceof ServerWorld serverWorld) {
            ChunkPos chunkPos = enderPearl.getChunkPos();
            this.pearl$addEnderPearl(enderPearl);
            serverWorld.resetIdleTimeout();
            return ChunkUtils.addEnderPearlTicket(serverWorld, chunkPos) - 1L;
        } else {
            return 0L;
        }
    }

    @Override
    public void pearl$addEnderPearl(EnderPearlEntity enderPearl) {
        this.enderPearls.add(enderPearl);
    }

    @Override
    public void pearl$removeEnderPearl(EnderPearlEntity enderPearl) {
        this.enderPearls.remove(enderPearl);
    }

    @Override
    public Set<EnderPearlEntity> pearl$getEnderPearls() {
        return this.enderPearls;
    }
}