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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEntity.class)
public abstract class ThrownEntityMixin extends ProjectileEntity {

    public ThrownEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * 1.21.2: tick() 重构 - 使用 applyDrag() 并调用 super.tick()
     */
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

        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        Vec3d vec3d;
        if (hitResult.getType() != HitResult.Type.MISS) {
            vec3d = hitResult.getPos();
        } else {
            vec3d = this.getPos().add(this.getVelocity());
        }

        this.setPosition(vec3d);
        this.updateRotation();
        this.checkBlockCollision();
        super.tick();

        if (hitResult.getType() != HitResult.Type.MISS && this.isAlive()) {
            this.hitOrDeflect(hitResult);
        }
    }

    @Unique
    private void applyDrag1212() {
        Vec3d vec3d = this.getVelocity();
        Vec3d vec3d2 = this.getPos();
        float g;
        if (this.isTouchingWater()) {
            for (int i = 0; i < 4; ++i) {
                float f = 0.25F;
                this.getWorld().addParticle(
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
        this.setVelocity(vec3d.multiply(g));
    }

    @Unique
    private void tickInitialBubbleColumnCollision() {
        if (this.firstUpdate) {
            Box box = this.getBoundingBox();
            BlockPos.stream(
                    BlockPos.ofFloored(box.minX, box.minY, box.minZ),
                    BlockPos.ofFloored(box.maxX, box.maxY, box.maxZ)
            ).forEach(blockPos -> {
                BlockState blockState = this.getWorld().getBlockState(blockPos);
                if (blockState.isOf(Blocks.BUBBLE_COLUMN)) {
                    blockState.onEntityCollision(this.getWorld(), blockPos, this);
                }
            });
        }
    }

    @Shadow
    protected abstract double getGravity();
}