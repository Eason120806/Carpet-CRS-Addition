package com.github.eason120806.carpetcrsaddition.mixins.rule.FireworkBackport;

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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.OptionalInt;

@Mixin(value = FireworkRocketEntity.class,priority = 2000)
public abstract class FireworkRocketEntityMixin extends ProjectileEntity {

    @Shadow private int life;
    @Shadow private int lifeTime;
    @Shadow @Nullable private LivingEntity shooter;

    // 仅需这个 TrackedData 来获取 shooter 的 ID
    @Shadow private static TrackedData<OptionalInt> SHOOTER_ENTITY_ID;

    // 私有方法影子（必须声明，否则无法调用）
    @Shadow private boolean wasShotByEntity() { return false; }
    @Shadow private boolean wasShotAtAngle() { return false; }
    @Shadow private void explodeAndRemove() {}
    @Shadow private boolean hasExplosionEffects() { return false; }

    // 构造器，类型匹配 ProjectileEntity
    public FireworkRocketEntityMixin(EntityType<? extends ProjectileEntity> type, World world) {
        super(type, world);
    }

    /**
     * @reason Port 1.21.6 FireworkRocketEntity tick logic.
     * @author EasonChen
     */
    @Overwrite
    public void tick() {
        super.tick();
        HitResult hitResult;

        if (this.wasShotByEntity()) {
            // 延迟加载 shooter
            if (this.shooter == null) {
                this.dataTracker.get(SHOOTER_ENTITY_ID).ifPresent(id -> {
                    Entity entity = this.getWorld().getEntityById(id);
                    if (entity instanceof LivingEntity) {
                        this.shooter = (LivingEntity) entity;
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
            // 普通飞行（非实体发射）
            if (!this.wasShotAtAngle()) {
                double f = this.horizontalCollision ? 1.0 : 1.15;
                this.setVelocity(this.getVelocity().multiply(f, 1.0, f).add(0.0, 0.04, 0.0));
            }

            Vec3d velocity = this.getVelocity();
            hitResult = ProjectileUtil.getCollision(this, this::canHit);
            this.move(MovementType.SELF, velocity);
            // 1.21.6 中有 tickBlockCollision()，但 1.21.1 没有，此处安全跳过
            this.setVelocity(velocity);
        }

        // 碰撞处理（与 1.21.6 一致）
        if (!this.noClip && this.isAlive() && hitResult.getType() != HitResult.Type.MISS) {
            this.hitOrDeflect(hitResult);
            this.velocityDirty = true;
        }

        this.updateRotation();

        // 发射音效
        if (this.life == 0 && !this.isSilent()) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH,
                    SoundCategory.AMBIENT, 3.0F, 1.0F);
        }

        this.life++;

        // 客户端粒子（原版 1.21.1 也如此）
        if (this.getWorld().isClient && this.life % 2 < 2) {
            this.getWorld().addParticle(ParticleTypes.FIREWORK,
                    this.getX(), this.getY(), this.getZ(),
                    this.random.nextGaussian() * 0.05,
                    -this.getVelocity().y * 0.5,
                    this.random.nextGaussian() * 0.05);
        }

        // 服务端爆炸判定
        if (!this.getWorld().isClient && this.life > this.lifeTime) {
            this.explodeAndRemove();
        }
    }

    /**
     * @reason Port 1.21.6 onEntityHit behavior (always explode, even on client).
     * @author EasonChen
     */
    @Overwrite
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        this.explodeAndRemove();
    }

    /**
     * @reason Port 1.21.6 onBlockHit behavior (call super and keep explosion condition).
     * @author EasonChen
     */
    @Overwrite
    protected void onBlockHit(BlockHitResult blockHitResult) {
        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState state = this.getWorld().getBlockState(blockPos);
        state.onEntityCollision(this.getWorld(), blockPos, this);
        super.onBlockHit(blockHitResult);
        if (!this.getWorld().isClient && this.hasExplosionEffects()) {
            this.explodeAndRemove();
        }
    }
}