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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin extends ProjectileEntity {

    @Shadow
    protected boolean inGround;

    @Shadow
    protected int inGroundTime;

    public PersistentProjectileEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
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
        boolean bl = !this.isNoClip();
        Vec3d vec3d = this.getVelocity();

        if (this.inGround && bl) {
            if (!this.getWorld().isClient()) {
                this.age();
            }
            ++this.inGroundTime;
            if (this.isAlive()) {
                this.checkBlockCollision();
            }
        } else {
            this.inGroundTime = 0;
            Vec3d vec3d3 = this.getPos();

            if (this.isTouchingWater()) {
                this.spawnBubbleParticles(vec3d3);
            }

            if (bl) {
                BlockHitResult blockHitResult = this.getWorld().raycast(
                        new RaycastContext(vec3d3, vec3d3.add(vec3d),
                                RaycastContext.ShapeType.COLLIDER,
                                RaycastContext.FluidHandling.NONE, this)
                );
                this.applyCollision(blockHitResult);
            } else {
                this.setPosition(vec3d3.add(vec3d));
                this.checkBlockCollision();
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
            Vec3d vec3d = this.getPos();
            EntityHitResult entityHitResult = this.getEntityCollision(vec3d, blockHitResult.getPos());
            Vec3d vec3d2 = ((HitResult) Objects.requireNonNullElse(entityHitResult, blockHitResult)).getPos();
            this.setPosition(vec3d2);

            if (this.portalManager != null && this.portalManager.isInPortal()) {
                this.tickPortalTeleportation();
            }

            if (entityHitResult == null) {
                if (this.isAlive() && blockHitResult.getType() != HitResult.Type.MISS) {
                    this.hitOrDeflect(blockHitResult);
                    this.velocityDirty = true;
                }
            } else {
                if (this.isAlive()) {
                    ProjectileDeflection projectileDeflection = this.hitOrDeflect(entityHitResult);
                    this.velocityDirty = true;
                }
            }
        }
    }

    @Unique
    @Nullable
    private EntityHitResult getEntityCollision(Vec3d currentPosition, Vec3d nextPosition) {
        return ProjectileUtil.getEntityCollision(
                this.getWorld(),
                this,
                currentPosition,
                nextPosition,
                this.getBoundingBox().stretch(this.getVelocity()).expand(1.0),
                this::canHit
        );
    }

    @Unique
    private void spawnBubbleParticles(Vec3d pos) {
        Vec3d vec3d = this.getVelocity();
        for (int i = 0; i < 4; ++i) {
            this.getWorld().addParticle(
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
        Vec3d vec3d = this.getVelocity();
        float f = 0.99F;
        if (this.isTouchingWater()) {
            f = this.getDragInWater();
        }
        this.setVelocity(vec3d.multiply(f));
    }

    @Shadow
    protected abstract boolean isNoClip();

    @Shadow
    protected abstract void age();

    @Shadow
    protected abstract float getDragInWater();
}