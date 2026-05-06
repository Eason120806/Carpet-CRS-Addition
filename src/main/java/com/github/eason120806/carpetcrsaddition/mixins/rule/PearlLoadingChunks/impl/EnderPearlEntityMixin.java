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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ThrownEnderpearl.class)
public abstract class EnderPearlEntityMixin extends ThrowableItemProjectile implements EnderPearlEntityInterface {
    public EnderPearlEntityMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void pearl$removeFromOwner() {
        if (this.getOwner() instanceof ServerPlayer serverPlayerEntity) {
            ((ServerPlayerEntityInterface) serverPlayerEntity).pearl$removeEnderPearl(
                    (ThrownEnderpearl) (Object) this
            );
        }
    }
}