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
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHurtingProjectile.class)
public abstract class ExplosiveProjectileEntityMixin extends Projectile {

    public ExplosiveProjectileEntityMixin(EntityType<? extends Projectile> entityType, Level level) {
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
        Entity entity = this.getOwner();
        this.apply1212Drag();

        if (this.level().isClientSide || (entity == null || !entity.isRemoved()) && this.level().hasChunkAt(this.blockPosition())) {
            HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            Vec3 vec3d;
            if (hitResult.getType() != HitResult.Type.MISS) {
                vec3d = hitResult.getLocation();
            } else {
                vec3d = this.position().add(this.getDeltaMovement());
            }

            ProjectileUtil.rotateTowardsMovement(this, 0.2F);
            this.setPos(vec3d);
            this.checkInsideBlocks();
            super.tick();

            if (this.isOnFire()) {
                this.setRemainingFireTicks(1);
            }

            if (hitResult.getType() != HitResult.Type.MISS && this.isAlive()) {
                this.hitTargetOrDeflectSelf(hitResult);
            }

            this.add1212Particles();
        } else {
            this.discard();
        }
    }

    @Unique
    private void apply1212Drag() {
        Vec3 vec3d = this.getDeltaMovement();
        Vec3 vec3d2 = this.position();
        float g;
        if (this.isInWater()) {
            Level level = this.level();
            for (int i = 0; i < 4; ++i) {
                level.addParticle(
                        ParticleTypes.BUBBLE,
                        vec3d2.x - vec3d.x * 0.25,
                        vec3d2.y - vec3d.y * 0.25,
                        vec3d2.z - vec3d.z * 0.25,
                        vec3d.x, vec3d.y, vec3d.z
                );
            }
            g = this.getWaterInertia();
        } else {
            g = this.getInertia();
        }

        Vec3 acceleration = vec3d.normalize().scale(this.getAccelerationPower());
        this.setDeltaMovement(vec3d.add(acceleration).scale(g));
    }

    @Unique
    private void add1212Particles() {
        ParticleOptions particleEffect = this.getTrailParticle();
        Vec3 vec3d = this.position();
        if (particleEffect != null) {
            this.level().addParticle(
                    particleEffect,
                    vec3d.x, vec3d.y + 0.5, vec3d.z,
                    0.0, 0.0, 0.0
            );
        }
    }

    @Unique
    private double getAccelerationPower() {
        return Math.sqrt(
                this.xPower * this.xPower +
                        this.yPower * this.yPower +
                        this.zPower * this.zPower
        );
    }

    public double xPower;

    public double yPower;

    public double zPower;

    @Shadow
    protected abstract float getInertia();

    protected abstract float getWaterInertia();

    @Shadow
    protected abstract ParticleOptions getTrailParticle();
}