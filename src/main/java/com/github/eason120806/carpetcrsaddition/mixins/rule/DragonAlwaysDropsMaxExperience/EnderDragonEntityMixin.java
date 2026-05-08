package com.github.eason120806.carpetcrsaddition.mixins.rule.DragonAlwaysDropsMaxExperience;

import com.github.eason120806.carpetcrsaddition.CRSSettings;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EnderDragon.class)
public abstract class EnderDragonEntityMixin {
    @ModifyVariable(
            method = "tickDeath",
            at = @At(
                    value = "STORE",
                    ordinal = 0
            ),
            ordinal = 0
    )
    private int modifyExperienceAmount(int i) {
        if (CRSSettings.DragonAlwaysDropsFirstKillXP) {
            return 12000;
        }
        return i;
    }
}