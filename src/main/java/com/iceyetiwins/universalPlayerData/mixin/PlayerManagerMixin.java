package com.iceyetiwins.universalPlayerData.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PlayerSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    //don't use level.dat save
    @Inject(method = "loadPlayerData", at = @At("RETURN"), cancellable = true)
    private void forceElseCondition(ServerPlayerEntity player, CallbackInfoReturnable<Optional<NbtCompound>> cir) {
        if (cir.getReturnValue().isPresent()) {
            PlayerSaveHandler saveHandler = ((PlayerManagerAccessor) this).getSaveHandler();

            Optional<NbtCompound> optional = saveHandler.loadPlayerData(player);

            cir.setReturnValue(optional);
        }
    }
}


