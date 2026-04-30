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

package com.github.eason120806.carpetcrsaddition.mixins.rule.PearlLoadingChunks;

import com.github.eason120806.carpetcrsaddition.CRSSettings;
import com.github.eason120806.carpetcrsaddition.utils.ChunkUtils;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderPearlEntity.class)
public abstract class EnderPearlEntityMixin extends ThrownItemEntity {

    @Unique
    private long chunkTicketExpiryTicks = 0L;

    public EnderPearlEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void getVector(CallbackInfo ci, @Share("i") LocalIntRef i, @Share("j") LocalIntRef j) {
        if (!CRSSettings.PearlCanLoadingChunks) return;
        i.set(ChunkUtils.getSectionCoordFloored(this.getPos().getX()));
        j.set(ChunkUtils.getSectionCoordFloored(this.getPos().getZ()));
    }

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void loadingChunks(
            CallbackInfo ci,
            @Local(ordinal = 0) Entity entity,
            @Share("i") LocalIntRef i,
            @Share("j") LocalIntRef j
    ) {
        if (!CRSSettings.PearlCanLoadingChunks) return;

        if (this.isAlive()) {
            BlockPos blockPos = BlockPos.ofFloored(this.getPos());
            if (
                    (
                            --this.chunkTicketExpiryTicks <= 0L
                                    || i.get() != ChunkUtils.getSectionCoord(blockPos.getX())
                                    || j.get() != ChunkUtils.getSectionCoord(blockPos.getZ())
                    )
                            && entity instanceof ServerPlayerEntity serverPlayerEntity
            ) {
                this.chunkTicketExpiryTicks = ((com.github.eason120806.carpetcrsaddition.interfaces.ServerPlayerEntityInterface) serverPlayerEntity)
                        .pearl$handleThrownEnderPearl((EnderPearlEntity) (Object) this);
            }
        }
    }
}