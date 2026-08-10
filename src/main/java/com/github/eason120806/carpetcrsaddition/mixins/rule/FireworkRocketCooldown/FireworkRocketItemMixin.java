package com.github.eason120806.carpetcrsaddition.mixins.rule.FireworkRocketCooldown;

import carpet.patches.EntityPlayerMPFake;
import com.github.eason120806.carpetcrsaddition.CRSSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public abstract class FireworkRocketItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity player = context.getPlayer();
        if (player == null) return;
        if (CRSSettings.FireworkRocketCooldown) {
            if (player.isFallFlying()) {
                cir.setReturnValue(ActionResult.PASS);
                return;
            }
        }

        // 烟花火箭使用冷却（对方块使用）
        if (CRSSettings.FireworkRocketCooldown) {
            // 假人跳过冷却
            if (player instanceof EntityPlayerMPFake) return;
            player.getItemCooldownManager().set((FireworkRocketItem) (Object) this, 5);
        }
    }

    @Inject(method = "use", at = @At("HEAD"))
    private void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (CRSSettings.FireworkRocketCooldown && user != null && user.isFallFlying()) {
            // 假人跳过冷却
            if (user instanceof EntityPlayerMPFake) return;
            user.getItemCooldownManager().set((FireworkRocketItem) (Object) this, 5);
        }
    }
}