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

package com.github.eason120806.carpetcrsaddition.mixins.rule.ProjectileBackport;

import com.github.eason120806.carpetcrsaddition.CRSSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity {

    @Shadow
    private boolean leftOwner;

    @Shadow
    private boolean shot;

    public ProjectileEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    @Nullable
    public abstract Entity getOwner();

    /**
     * 1.21.2: tick() 统一处理 GameEvent.PROJECTILE_SHOOT 和 leftOwner 逻辑
     */
    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void tickMixin(CallbackInfo ci) {
        if (!CRSSettings.UseV1212ProjectileLogic) return;

        if (!this.shot) {
            this.shot = true;
        }

        if (!this.leftOwner) {
            this.leftOwner = this.shouldLeaveOwner();
        }
    }

    @Unique
    private boolean shouldLeaveOwner() {
        Entity entity = this.getOwner();
        if (entity != null) {
            for (Entity passenger : entity.getRootVehicle().getPassengersDeep()) {
                if (this.getBoundingBox().stretch(this.getVelocity()).expand(1.0).intersects(passenger.getBoundingBox())) {
                    return false;
                }
            }
            return !this.getBoundingBox().stretch(this.getVelocity()).expand(1.0).intersects(entity.getRootVehicle().getBoundingBox());
        }
        return true;
    }
}