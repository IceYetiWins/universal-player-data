package com.iceyetiwins.universalPlayerData.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PlayerSaveHandler;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Redirect(
            method = "onPlayerConnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;info(Ljava/lang/String;[Ljava/lang/Object;)V"
            )
    )
    private void suppressLoginLog(Logger logger, String message, Object... objects) {
        String playerName = (String) objects[0];
        String playerIp = (String) objects[1];
        int playerId = (int) objects[2];

        logger.info("{}[{}] logged in with entity id {}", playerName, playerIp, playerId); //remove location from log because it will differ depending on spawn point
    }
}


