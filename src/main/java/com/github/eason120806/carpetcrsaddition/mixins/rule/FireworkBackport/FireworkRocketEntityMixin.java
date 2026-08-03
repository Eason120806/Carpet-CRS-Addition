package com.github.eason120806.carpetcrsaddition.mixins.rule.FireworkBackport;

import com.github.eason120806.carpetcrsaddition.CRSSettings;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.OptionalInt;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin extends ProjectileEntity {

    @Shadow private int life;
    @Shadow private int lifeTime;
    @Shadow @Nullable private LivingEntity shooter;

    @Shadow @Final private static TrackedData<OptionalInt> SHOOTER_ENTITY_ID;

    @Shadow private boolean wasShotByEntity() { return false; }

    @Shadow public boolean wasShotAtAngle() { return false; }
    @Shadow private void explodeAndRemove() {}
    @Shadow private boolean hasExplosionEffects() { return false; }

    protected FireworkRocketEntityMixin(EntityType<? extends ProjectileEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        if (!CRSSettings.UseV1216FireworkLogic) {
            return;
        }

        super.tick();
        HitResult hitResult;

        if (this.wasShotByEntity()) {
            if (this.shooter == null) {
                this.dataTracker.get(SHOOTER_ENTITY_ID).ifPresent(id -> {
                    Entity entity = this.getWorld().getEntityById(id);
                    if (entity instanceof LivingEntity living) {
                        this.shooter = living;
                    }
                });
            }

            if (this.shooter != null) {
                Vec3d handOffset;
                if (this.shooter.isFallFlying()) {
                    Vec3d lookVec = this.shooter.getRotationVector();
                    Vec3d vel = this.shooter.getVelocity();
                    this.shooter.setVelocity(
                            vel.add(lookVec.x * 0.1 + (lookVec.x * 1.5 - vel.x) * 0.5,
                                    lookVec.y * 0.1 + (lookVec.y * 1.5 - vel.y) * 0.5,
                                    lookVec.z * 0.1 + (lookVec.z * 1.5 - vel.z) * 0.5)
                    );
                    handOffset = this.shooter.getHandPosOffset(Items.FIREWORK_ROCKET);
                } else {
                    handOffset = Vec3d.ZERO;
                }
                this.setPosition(
                        this.shooter.getX() + handOffset.x,
                        this.shooter.getY() + handOffset.y,
                        this.shooter.getZ() + handOffset.z
                );
                this.setVelocity(this.shooter.getVelocity());
            }

            hitResult = ProjectileUtil.getCollision(this, this::canHit);
        } else {
            if (!this.wasShotAtAngle()) {
                double f = this.horizontalCollision ? 1.0 : 1.15;
                this.setVelocity(this.getVelocity().multiply(f, 1.0, f).add(0.0, 0.04, 0.0));
            }
            Vec3d velocity = this.getVelocity();
            hitResult = ProjectileUtil.getCollision(this, this::canHit);
            this.move(MovementType.SELF, velocity);
            this.setVelocity(velocity);
        }

        if (!this.noClip && this.isAlive() && hitResult.getType() != HitResult.Type.MISS) {
            this.hitOrDeflect(hitResult);
            this.velocityDirty = true;
        }

        this.updateRotation();

        if (this.life == 0 && !this.isSilent()) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH,
                    SoundCategory.AMBIENT, 3.0F, 1.0F);
        }

        this.life++;

        if (this.getWorld().isClient && this.life % 2 < 1) {
            this.getWorld().addParticle(ParticleTypes.FIREWORK,
                    this.getX(), this.getY(), this.getZ(),
                    this.random.nextGaussian() * 0.05,
                    -this.getVelocity().y * 0.5,
                    this.random.nextGaussian() * 0.05);
        }

        if (!this.getWorld().isClient && this.life > this.lifeTime) {
            this.explodeAndRemove();
        }

        ci.cancel();
    }

    @Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true)
    protected void onEntityHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (!CRSSettings.UseV1216FireworkLogic) return;
        super.onEntityHit(entityHitResult);
        this.explodeAndRemove();
        ci.cancel();
    }

    @Inject(method = "onBlockHit", at = @At("HEAD"), cancellable = true)
    protected void onBlockHit(BlockHitResult blockHitResult, CallbackInfo ci) {
        if (!CRSSettings.UseV1216FireworkLogic) return;
        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState state = this.getWorld().getBlockState(blockPos);
        state.onEntityCollision(this.getWorld(), blockPos, this);
        super.onBlockHit(blockHitResult);
        if (!this.getWorld().isClient && this.hasExplosionEffects()) {
            this.explodeAndRemove();
        }
        ci.cancel();
    }
}