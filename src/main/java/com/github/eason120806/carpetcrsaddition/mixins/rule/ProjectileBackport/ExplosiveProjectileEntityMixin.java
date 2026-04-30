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
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExplosiveProjectileEntity.class)
public abstract class ExplosiveProjectileEntityMixin extends ProjectileEntity {

    @Shadow
    public double accelerationPower;

    public ExplosiveProjectileEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * 1.21.2: tick() 重构 - 使用 applyDrag(), addParticles(), hitOrDeflect()
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
        Entity entity = this.getOwner();
        this.apply1212Drag();

        if (this.getWorld().isClient || (entity == null || !entity.isRemoved()) && this.getWorld().isChunkLoaded(this.getBlockPos())) {
            HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit, this.getRaycastShapeType());
            Vec3d vec3d;
            if (hitResult.getType() != HitResult.Type.MISS) {
                vec3d = hitResult.getPos();
            } else {
                vec3d = this.getPos().add(this.getVelocity());
            }

            ProjectileUtil.setRotationFromVelocity(this, 0.2F);
            this.setPosition(vec3d);
            this.checkBlockCollision();
            super.tick();

            if (this.isBurning()) {
                this.setOnFireFor(1.0F);
            }

            if (hitResult.getType() != HitResult.Type.MISS && this.isAlive()) {
                this.hitOrDeflect(hitResult);
            }

            this.add1212Particles();
        } else {
            this.discard();
        }
    }

    @Unique
    private void apply1212Drag() {
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
            g = this.getDragInWater();
        } else {
            g = this.getDrag();
        }

        this.setVelocity(vec3d.add(vec3d.normalize().multiply(this.accelerationPower)).multiply(g));
    }

    @Unique
    private void add1212Particles() {
        ParticleEffect particleEffect = this.getParticleType();
        Vec3d vec3d = this.getPos();
        if (particleEffect != null) {
            this.getWorld().addParticle(
                    particleEffect,
                    vec3d.x, vec3d.y + 0.5, vec3d.z,
                    0.0, 0.0, 0.0
            );
        }
    }

    @Shadow
    protected abstract net.minecraft.world.RaycastContext.ShapeType getRaycastShapeType();

    @Shadow
    protected abstract boolean isBurning();

    @Shadow
    protected abstract float getDrag();

    @Shadow
    protected abstract float getDragInWater();

    @Shadow
    protected abstract ParticleEffect getParticleType();
}