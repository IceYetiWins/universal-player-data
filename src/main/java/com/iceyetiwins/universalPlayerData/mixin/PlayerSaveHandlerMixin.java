package com.iceyetiwins.universalPlayerData.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.world.PlayerSaveHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(PlayerSaveHandler.class)
public class PlayerSaveHandlerMixin {
    private static final Path UNIVERSAL_PLAYER_DATA_DIR = new File(FabricLoader.getInstance().getGameDir().toFile(), "universal-playerdata").toPath();
    private static final Logger LOGGER = LoggerFactory.getLogger("universalPlayerData");

    // Runs before backupAndReplace in savePlayerData()
    @Inject(
            method = "savePlayerData",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Util;backupAndReplace(Ljava/nio/file/Path;Ljava/nio/file/Path;Ljava/nio/file/Path;)V",
                    shift = At.Shift.BEFORE
            )
    )
    public void onSavePlayerData(PlayerEntity player, CallbackInfo ci, @Local(ordinal = 1) Path path2) {
        try {
            UNIVERSAL_PLAYER_DATA_DIR.toFile().mkdirs();

            // Create a deep copy of path2
            Path tempCopy = Files.createTempFile(player.getUuidAsString() + "-", ".dat");
            Files.copy(path2, tempCopy, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            Path path3 = UNIVERSAL_PLAYER_DATA_DIR.resolve(player.getUuidAsString() + ".dat");
            Path path4 = UNIVERSAL_PLAYER_DATA_DIR.resolve(player.getUuidAsString() + ".dat_old");

            // Use the deep copy in backupAndReplace instead of the original path2
            Util.backupAndReplace(path3, tempCopy, path4);

            // Cleanup: Mark the temp file for deletion after the game releases it
            tempCopy.toFile().deleteOnExit();
        } catch (IOException e) {
            LOGGER.error("Failed to save universal player data for {}", player.getName().getString(), e);
        }
    }
}
