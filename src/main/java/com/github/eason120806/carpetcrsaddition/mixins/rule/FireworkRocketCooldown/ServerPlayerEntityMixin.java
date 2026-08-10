package com.github.eason120806.carpetcrsaddition.mixins.rule.FireworkRocketCooldown;

import com.github.eason120806.carpetcrsaddition.accessor.ServerPlayerEntityAccessor;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements ServerPlayerEntityAccessor {

    @Unique
    private long lastFireworkUseTick = 0;

    @Override
    public long getLastFireworkUseTick() {
        return lastFireworkUseTick;
    }

    @Override
    public void setLastFireworkUseTick(long tick) {
        this.lastFireworkUseTick = tick;
    }
}