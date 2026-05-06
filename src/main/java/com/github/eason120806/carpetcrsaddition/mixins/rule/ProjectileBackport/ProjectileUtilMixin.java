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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileUtil.class)
public abstract class ProjectileUtilMixin {

    @Inject(
            method = "rotateTowardsMovement",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void setRotationFromVelocityMixin(Entity entity, float delta, CallbackInfo ci) {
        if (!CRSSettings.UseV1212ProjectileLogic) return;

        ci.cancel();
        Vec3 vec3d = entity.getDeltaMovement();
        if (vec3d.lengthSqr() != 0.0) {
            double d = vec3d.horizontalDistance();
            entity.setYRot((float) (Mth.atan2(vec3d.z, vec3d.x) * (180F / Math.PI)) + 90.0F);
            entity.setXRot((float) (Mth.atan2(d, vec3d.y) * (180F / Math.PI)) - 90.0F);

            while (entity.getXRot() - entity.xRotO < -180.0F) {
                entity.xRotO -= 360.0F;
            }
            while (entity.getXRot() - entity.xRotO >= 180.0F) {
                entity.xRotO += 360.0F;
            }
            while (entity.getYRot() - entity.yRotO < -180.0F) {
                entity.yRotO -= 360.0F;
            }
            while (entity.getYRot() - entity.yRotO >= 180.0F) {
                entity.yRotO += 360.0F;
            }

            entity.setXRot(Mth.lerp(delta, entity.xRotO, entity.getXRot()));
            entity.setYRot(Mth.lerp(delta, entity.yRotO, entity.getYRot()));
        }
    }
}