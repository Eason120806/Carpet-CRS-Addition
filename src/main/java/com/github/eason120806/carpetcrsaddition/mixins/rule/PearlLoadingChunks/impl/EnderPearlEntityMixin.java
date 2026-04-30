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

import com.github.eason120806.carpetcrsaddition.interfaces.EnderPearlEntityInterface;
import com.github.eason120806.carpetcrsaddition.interfaces.ServerPlayerEntityInterface;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnderPearlEntity.class)
public abstract class EnderPearlEntityMixin extends ThrownItemEntity implements EnderPearlEntityInterface {
    public EnderPearlEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void pearl$removeFromOwner() {
        if (this.getOwner() instanceof ServerPlayerEntity serverPlayerEntity) {
            ((ServerPlayerEntityInterface) serverPlayerEntity).pearl$removeEnderPearl(
                    (EnderPearlEntity) (Object) this
            );
        }
    }
}