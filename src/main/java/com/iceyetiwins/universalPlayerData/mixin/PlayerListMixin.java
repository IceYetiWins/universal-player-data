package com.iceyetiwins.universalPlayerData.mixin;

import com.iceyetiwins.universalPlayerData.UniversalPlayerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Final
    @Shadow
    private PlayerDataStorage playerIo;

    //this method tries to load world data instead of the player dat file when in singleplayer, we cannot have this
    @Inject(at = @At("RETURN"), method = "loadPlayerData", cancellable = true)
    private void forceLoadUniversalPlayerData(NameAndId nameAndId, CallbackInfoReturnable<Optional<CompoundTag>> cir) {
        cir.setReturnValue(playerIo.load(nameAndId));
    }

    @Redirect(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;info(Ljava/lang/String;[Ljava/lang/Object;)V"
            )
    )
    private void removeCoordinatesFromLog(org.slf4j.Logger logger, String message, Object[] args) {
        UniversalPlayerData.LOGGER.info("{}[{}] logged in with entity id {}", args[0], args[1], args[2]);
    }
}
