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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class PersistentProjectileEntityMixin extends Projectile {

    @Shadow
    protected int inGroundTime;

    @Shadow
    protected boolean inGround;

    public PersistentProjectileEntityMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * 1.21.2: tick() 重构 - 使用 applyCollision(), spawnBubbleParticles(), applyDrag()
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
        boolean bl = !this.isNoPhysics();
        Vec3 vec3d = this.getDeltaMovement();

        if (this.inGround && bl) {
            if (!this.level().isClientSide()) {
                this.tickDespawn();
            }
            ++this.inGroundTime;
            if (this.isAlive()) {
                this.checkInsideBlocks();
            }
        } else {
            this.inGroundTime = 0;
            Vec3 vec3d3 = this.position();

            if (this.isInWater()) {
                this.spawnBubbleParticles(vec3d3);
            }

            if (bl) {
                BlockHitResult blockHitResult = this.level().clip(
                        new ClipContext(vec3d3, vec3d3.add(vec3d),
                                ClipContext.Block.COLLIDER,
                                ClipContext.Fluid.NONE, this)
                );
                this.applyCollision(blockHitResult);
            } else {
                this.setPos(vec3d3.add(vec3d));
                this.checkInsideBlocks();
            }

            this.applyDrag1212();

            if (bl && !this.inGround) {
                this.applyGravity();
            }

            super.tick();
        }
    }

    @Unique
    private void applyCollision(BlockHitResult blockHitResult) {
        if (this.isAlive()) {
            Vec3 vec3d = this.position();
            EntityHitResult entityHitResult = this.getEntityCollision(vec3d, blockHitResult.getLocation());
            Vec3 vec3d2 = entityHitResult != null ? entityHitResult.getLocation() : blockHitResult.getLocation();
            this.setPos(vec3d2);

            if (this.portalProcess != null && this.portalProcess.isInsidePortalThisTick()) {
                this.handlePortal();
            }

            if (entityHitResult == null) {
                if (this.isAlive() && blockHitResult.getType() != HitResult.Type.MISS) {
                    this.hitTargetOrDeflectSelf(blockHitResult);
                    this.hasImpulse = true;
                }
            } else {
                if (this.isAlive()) {
                    this.hitTargetOrDeflectSelf(entityHitResult);
                    this.hasImpulse = true;
                }
            }
        }
    }

    @Unique
    @Nullable
    private EntityHitResult getEntityCollision(Vec3 currentPosition, Vec3 nextPosition) {
        return ProjectileUtil.getEntityHitResult(
                this.level(),
                this,
                currentPosition,
                nextPosition,
                this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0),
                this::canHitEntity
        );
    }

    @Unique
    private void spawnBubbleParticles(Vec3 pos) {
        Vec3 vec3d = this.getDeltaMovement();
        Level level = this.level();
        for (int i = 0; i < 4; ++i) {
            level.addParticle(
                    ParticleTypes.BUBBLE,
                    pos.x - vec3d.x * 0.25,
                    pos.y - vec3d.y * 0.25,
                    pos.z - vec3d.z * 0.25,
                    vec3d.x, vec3d.y, vec3d.z
            );
        }
    }

    @Unique
    private void applyDrag1212() {
        Vec3 vec3d = this.getDeltaMovement();
        float f = 0.99F;
        if (this.isInWater()) {
            f = this.getWaterInertia();
        }
        this.setDeltaMovement(vec3d.scale(f));
    }

    @Shadow
    protected abstract boolean isNoPhysics();

    @Shadow
    protected abstract void tickDespawn();

    @Shadow
    public abstract float getWaterInertia();
}