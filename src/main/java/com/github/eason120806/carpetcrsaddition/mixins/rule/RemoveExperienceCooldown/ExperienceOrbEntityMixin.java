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

package com.github.eason120806.carpetcrsaddition.mixins.rule.RemoveExperienceCooldown;

import com.github.eason120806.carpetcrsaddition.CRSSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin extends Entity {

    @Shadow
    private int amount;

    @Shadow
    private PlayerEntity target;

    public ExperienceOrbEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void tickMixin(CallbackInfo ci) {
        if (!CRSSettings.RemoveExperienceCooldown) return;

        // 如果已经有目标玩家，立即吸收
        if (this.target != null) {
            this.target.addExperience(this.amount);
            this.discard();
        }
    }

    @Inject(
            method = "expensiveUpdate",
            at = @At("HEAD"),
            cancellable = true
    )
    private void expensiveUpdateMixin(CallbackInfo ci) {
        if (!CRSSettings.RemoveExperienceCooldown) return;

        // 如果已经有目标玩家，直接吸收
        if (this.target != null && this.target.isAlive()) {
            this.target.addExperience(this.amount);
            this.discard();
            ci.cancel();
        }
    }
}