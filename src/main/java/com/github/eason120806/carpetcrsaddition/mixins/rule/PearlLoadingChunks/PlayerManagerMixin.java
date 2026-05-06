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

package com.github.eason120806.carpetcrsaddition.mixins.rule.PearlLoadingChunks;

import com.github.eason120806.carpetcrsaddition.CRSSettings;
import com.github.eason120806.carpetcrsaddition.interfaces.ServerPlayerEntityInterface;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerManagerMixin {
    @Inject(
            method = "remove",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;" +
                            "removePlayerImmediately(Lnet/minecraft/server/level/ServerPlayer;" +
                            "Lnet/minecraft/world/entity/Entity$RemovalReason;)V"
            )
    )
    private void removeMixin(ServerPlayer player, CallbackInfo ci) {
        if (CRSSettings.PearlCanLoadingChunks) {
            ((ServerPlayerEntityInterface) player).pearl$getEnderPearls().forEach(
                    enderPearlEntity -> enderPearlEntity.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER)
            );
        }
    }
}