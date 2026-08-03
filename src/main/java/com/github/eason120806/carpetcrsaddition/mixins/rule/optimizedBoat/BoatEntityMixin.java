package com.github.eason120806.carpetcrsaddition.mixins.rule.optimizedBoat;

import com.github.eason120806.carpetcrsaddition.CRSSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin {

    @Unique
    private int boatOptimizer$stillTicks = 0;
    @Unique
    private boolean boatOptimizer$sleeping = false;

    @Unique
    private static final double SPEED_THRESHOLD_SQ = 1.0E-4;
    @Unique
    private static final int STILL_THRESHOLD = 60;   // 3 秒后进入半休眠
    @Unique
    private static final int REGION_SIZE_X = 256;
    @Unique
    private static final int REGION_SIZE_Z = 512;
    @Unique
    private static final long DENSITY_CHECK_INTERVAL = 200L; // 10 秒

    @Unique
    private long boatOptimizer$lastDensityCheckTime = -1L;
    @Unique
    private int boatOptimizer$cachedDensity = 0;

    @Accessor("ticksUnderwater")
    abstract float getTicksUnderwater();

    @Accessor("ticksUnderwater")
    abstract void setTicksUnderwater(float ticks);

    //半休眠tick
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        if (!CRSSettings.optimizedBoat) return;
        BoatEntity self = (BoatEntity) (Object) this;
        if (!boatOptimizer$sleeping) return;

        // 区域密度检查
        if (CRSSettings.optimizedBoatConstraints > 0) {
            int count = getCachedDensity(self);
            if (count <= CRSSettings.optimizedBoatConstraints) {
                wakeUp();
                return; // 唤醒后不取消，让原版tick执行
            }
        }

        // ---- 1. 水下乘客移除 ----
        if (!self.getWorld().isClient) {
            if (self.isSubmergedInWater()) {
                setTicksUnderwater(getTicksUnderwater() + 1.0F);
            } else {
                setTicksUnderwater(0.0F);
            }
            if (getTicksUnderwater() >= 60.0F) {
                self.removeAllPassengers();
            }
        }

        // ---- 2. 怪物上船 ----
        List<Entity> list = self.getWorld().getOtherEntities(
                self,
                self.getBoundingBox().expand(0.2, -0.01, 0.2),
                EntityPredicates.canBePushedBy(self)
        );
        if (!list.isEmpty()) {
            boolean bl = !self.getWorld().isClient
                    && !(self.getControllingPassenger() instanceof PlayerEntity);
            for (Entity entity : list) {
                if (!entity.hasPassenger(self)) {
                    if (bl
                            && self.getPassengerList().size() < 2
                            && !entity.hasVehicle()
                            && self.isSmallerThanBoat(entity)
                            && entity instanceof LivingEntity
                            && !(entity instanceof WaterCreatureEntity)
                            && !(entity instanceof PlayerEntity)) {
                        entity.startRiding(self);
                    }
                }
            }
        }

        ci.cancel();
    }

    //正常tick结尾
    @Inject(method = "tick", at = @At("TAIL"))
    private void afterTick(CallbackInfo ci) {
        if (!CRSSettings.optimizedBoat) return;

        BoatEntity self = (BoatEntity) (Object) this;
        if (!self.hasPassengers() && self.getVelocity().lengthSquared() < SPEED_THRESHOLD_SQ) {
            boolean canSleep = true;

            if (CRSSettings.optimizedBoatConstraints > 0) {
                int count = getCachedDensity(self);
                if (count <= CRSSettings.optimizedBoatConstraints) {
                    canSleep = false;
                }
            }

            if (canSleep) {
                boatOptimizer$stillTicks++;
                if (boatOptimizer$stillTicks >= STILL_THRESHOLD) {
                    boatOptimizer$sleeping = true;
                    boatOptimizer$stillTicks = 0;
                }
            } else {
                boatOptimizer$stillTicks = 0;
                if (boatOptimizer$sleeping) {
                    wakeUp();
                }
            }
        } else {
            boatOptimizer$stillTicks = 0;
            boatOptimizer$sleeping = false;
        }
    }

    //玩家上船唤醒
    @Inject(method = "interact", at = @At("HEAD"))
    private void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (CRSSettings.optimizedBoat) {
            wakeUp();
        }
    }

    @Unique
    private void wakeUp() {
        boatOptimizer$sleeping = false;
        boatOptimizer$stillTicks = 0;
        // 清除密度缓存，确保下次检测时获取最新值
        boatOptimizer$lastDensityCheckTime = -1L;
    }

    @Unique
    private int getCachedDensity(BoatEntity boat) {
        World world = boat.getWorld();
        long currentTime = world.getTime();

        if (boatOptimizer$lastDensityCheckTime < 0L
                || currentTime - boatOptimizer$lastDensityCheckTime >= DENSITY_CHECK_INTERVAL) {
            // 更新缓存
            boatOptimizer$cachedDensity = countBoatsInRegion(boat);
            boatOptimizer$lastDensityCheckTime = currentTime;
        }
        return boatOptimizer$cachedDensity;
    }

    @Unique
    private int countBoatsInRegion(BoatEntity boat) {
        World world = boat.getWorld();
        int x = boat.getBlockX();
        int z = boat.getBlockZ();
        int regionX = Math.floorDiv(x, REGION_SIZE_X) * REGION_SIZE_X;
        int regionZ = Math.floorDiv(z, REGION_SIZE_Z) * REGION_SIZE_Z;
        Box box = new Box(
                regionX, world.getBottomY(), regionZ,
                regionX + REGION_SIZE_X, world.getTopY(), regionZ + REGION_SIZE_Z
        );
        return world.getEntitiesByClass(BoatEntity.class, box, e -> true).size();
    }
}