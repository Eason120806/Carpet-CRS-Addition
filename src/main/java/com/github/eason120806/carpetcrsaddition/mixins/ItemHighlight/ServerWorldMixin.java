package com.github.eason120806.carpetcrsaddition.mixins.ItemHighlight;

import com.github.eason120806.carpetcrsaddition.CRSSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickReturn(CallbackInfo ci) {
        boolean shouldGlow = CRSSettings.ItemHighlight;
        ServerWorld world = (ServerWorld) (Object) this;
        for (Entity entity : world.iterateEntities()) {
            if (entity instanceof ItemEntity item) {
                item.setGlowing(shouldGlow);
            }
        }
    }
}