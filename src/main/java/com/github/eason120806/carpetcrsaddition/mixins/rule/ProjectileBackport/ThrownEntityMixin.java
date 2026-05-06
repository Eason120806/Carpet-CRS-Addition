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
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrowableProjectile.class)
public abstract class ThrownEntityMixin extends Projectile {

    public ThrownEntityMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickMixin(CallbackInfo ci) {
        if (!CRSSettings.UseV1212ProjectileLogic) return;

        ci.cancel();
        onV1212Tick();
    }

    @Unique
    private void onV1212Tick() {
        this.tickInitialBubbleColumnCollision();
        this.applyGravity();
        this.applyDrag1212();

        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        Vec3 vec3d;
        if (hitResult.getType() != HitResult.Type.MISS) {
            vec3d = hitResult.getLocation();
        } else {
            vec3d = this.position().add(this.getDeltaMovement());
        }

        this.setPos(vec3d);
        this.updateRotation();
        this.checkInsideBlocks();
        super.tick();

        if (hitResult.getType() != HitResult.Type.MISS && this.isAlive()) {
            this.hitTargetOrDeflectSelf(hitResult);
        }
    }

    @Unique
    private void applyDrag1212() {
        Vec3 vec3d = this.getDeltaMovement();
        Vec3 vec3d2 = this.position();
        float g;
        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                float f = 0.25F;
                this.level().addParticle(
                        ParticleTypes.BUBBLE,
                        vec3d2.x - vec3d.x * (double) 0.25F,
                        vec3d2.y - vec3d.y * (double) 0.25F,
                        vec3d2.z - vec3d.z * (double) 0.25F,
                        vec3d.x, vec3d.y, vec3d.z
                );
            }
            g = 0.8F;
        } else {
            g = 0.99F;
        }
        this.setDeltaMovement(vec3d.scale(g));
    }

    @Unique
    private void tickInitialBubbleColumnCollision() {
        if (this.firstTick) {
            AABB box = this.getBoundingBox();
            BlockPos.betweenClosedStream(
                    BlockPos.containing(box.minX, box.minY, box.minZ),
                    BlockPos.containing(box.maxX, box.maxY, box.maxZ)
            ).forEach(blockPos -> {
                BlockState blockState = this.level().getBlockState(blockPos);
                if (blockState.is(Blocks.BUBBLE_COLUMN)) {
                    blockState.entityInside(this.level(), blockPos, this);
                }
            });
        }
    }
}