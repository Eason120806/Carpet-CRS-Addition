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
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileUtil.class)
public abstract class ProjectileUtilMixin {

    /**
     * 1.21.2: setRotationFromVelocity 使用不同的旋转计算方式
     */
    @Inject(
            method = "setRotationFromVelocity",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void setRotationFromVelocityMixin(Entity entity, float delta, CallbackInfo ci) {
        if (!CRSSettings.UseV1212ProjectileLogic) return;

        ci.cancel();
        Vec3d vec3d = entity.getVelocity();
        if (vec3d.lengthSquared() != 0.0) {
            double d = vec3d.horizontalLength();
            entity.setYaw((float) (MathHelper.atan2(vec3d.z, vec3d.x) * (180F / Math.PI)) + 90.0F);
            entity.setPitch((float) (MathHelper.atan2(d, vec3d.y) * (180F / Math.PI)) - 90.0F);

            while (entity.getPitch() - entity.prevPitch < -180.0F) {
                entity.prevPitch -= 360.0F;
            }
            while (entity.getPitch() - entity.prevPitch >= 180.0F) {
                entity.prevPitch += 360.0F;
            }
            while (entity.getYaw() - entity.prevYaw < -180.0F) {
                entity.prevYaw -= 360.0F;
            }
            while (entity.getYaw() - entity.prevYaw >= 180.0F) {
                entity.prevYaw += 360.0F;
            }

            entity.setPitch(MathHelper.lerp(delta, entity.prevPitch, entity.getPitch()));
            entity.setYaw(MathHelper.lerp(delta, entity.prevYaw, entity.getYaw()));
        }
    }
}