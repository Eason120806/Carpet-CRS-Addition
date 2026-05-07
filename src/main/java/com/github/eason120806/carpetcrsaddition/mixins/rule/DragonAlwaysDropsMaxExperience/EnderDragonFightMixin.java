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

package com.github.eason120806.carpetcrsaddition.mixins.rule.DragonAlwaysDropsMaxExperience;

import com.github.eason120806.carpetcrsaddition.CRSSettings;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonFight.class)
public abstract class EnderDragonFightMixin {

    @Shadow
    private boolean previouslyKilled;

    /**
     * 强制设置 previouslyKilled 为 false，使得每次击杀都像首次击杀一样掉落 12000 经验
     */
    @Inject(
            method = "dragonKilled",
            at = @At("HEAD")
    )
    private void dragonKilledMixin(net.minecraft.entity.boss.dragon.EnderDragonEntity dragon, CallbackInfo ci) {
        if (CRSSettings.DragonAlwaysDropsMaxExperience) {
            this.previouslyKilled = false;
        }
    }
}