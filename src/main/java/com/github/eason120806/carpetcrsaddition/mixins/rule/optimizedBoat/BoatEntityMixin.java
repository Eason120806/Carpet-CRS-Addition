package com.github.eason120806.carpetcrsaddition.mixins.rule.optimizedBoat;

import com.github.eason120806.carpetcrsaddition.CRSSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
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

    // 访问私有字段 ticksUnderwater
    @Accessor("ticksUnderwater")
    abstract float getTicksUnderwater();

    @Accessor("ticksUnderwater")
    abstract void setTicksUnderwater(float ticks);

    /**
     * 半休眠时只执行：水下乘客移除 + 怪物上船检查，跳过其他所有物理、移动、气泡柱等。
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        if (!CRSSettings.optimizedBoat) return;
        if (!boatOptimizer$sleeping) return;

        BoatEntity self = (BoatEntity) (Object) this;

        // ---- 1. 原版水下乘客移除逻辑 ----
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

        // ---- 2. 原版怪物上船逻辑 ----
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
                        // 注意：不上车后唤醒，继续保持半休眠
                    }
                    // 原版 else 会 pushAwayFrom，我们忽略以避免船位移
                }
            }
        }

        ci.cancel(); // 跳过原版 tick 的全部剩余逻辑
    }

    /**
     * 正常 tick 结尾：根据条件进入 / 退出半休眠。
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void afterTick(CallbackInfo ci) {
        if (!CRSSettings.optimizedBoat) return;

        BoatEntity self = (BoatEntity) (Object) this;
        if (!self.hasPassengers() && self.getVelocity().lengthSquared() < SPEED_THRESHOLD_SQ) {
            boatOptimizer$stillTicks++;
            if (boatOptimizer$stillTicks >= STILL_THRESHOLD) {
                boatOptimizer$sleeping = true;
                boatOptimizer$stillTicks = 0;
            }
        } else {
            boatOptimizer$stillTicks = 0;
            boatOptimizer$sleeping = false;
        }
    }

    /**
     * 玩家右键上船时立即完全唤醒（恢复完整物理）。
     */
    @Inject(method = "interact", at = @At("HEAD"))
    private void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (CRSSettings.optimizedBoat) {
            wakeUp();
        }
    }

    // NBT 持久化
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onWriteNbt(NbtCompound nbt, CallbackInfo ci) {
        if (CRSSettings.optimizedBoat) {
            nbt.putBoolean("BoatSleeping", boatOptimizer$sleeping);
            nbt.putInt("BoatStillTicks", boatOptimizer$stillTicks);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onReadNbt(NbtCompound nbt, CallbackInfo ci) {
        if (CRSSettings.optimizedBoat && nbt.contains("BoatSleeping")) {
            boatOptimizer$sleeping = nbt.getBoolean("BoatSleeping");
            boatOptimizer$stillTicks = nbt.getInt("BoatStillTicks");
        }
    }

    @Unique
    private void wakeUp() {
        boatOptimizer$sleeping = false;
        boatOptimizer$stillTicks = 0;
    }
}