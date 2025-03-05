package com.iceyetiwins.universalPlayerData.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.util.Util;
import net.minecraft.world.PlayerSaveHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;

@Mixin(PlayerSaveHandler.class)
public class PlayerSaveHandlerMixin {
    private static final Path UNIVERSAL_PLAYER_DATA_DIR = FabricLoader.getInstance().getGameDir().resolve("universal-playerdata");
    private static final Logger LOGGER = LoggerFactory.getLogger("universalPlayerData");

    //right before Util.backupAndReplace in savePlayerData()
    @Inject(
            method = "savePlayerData",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Util;backupAndReplace(Ljava/nio/file/Path;Ljava/nio/file/Path;Ljava/nio/file/Path;)V", //finds net.minecraft.util.Util.backupAndReplace(Path, Path, Path)
                    shift = At.Shift.BEFORE
            )
    )
    private void onSavePlayerData(PlayerEntity player, CallbackInfo ci, @Local(ordinal = 1) Path path2) { //first local variable of type Path
        try {
            Files.createDirectories(UNIVERSAL_PLAYER_DATA_DIR);

            // temporary deep copy of path2
            Path path2Copy = Files.createTempFile(player.getUuidAsString() + "-", ".dat");
            Files.copy(path2, path2Copy, StandardCopyOption.REPLACE_EXISTING);

            Path path3 = UNIVERSAL_PLAYER_DATA_DIR.resolve(player.getUuidAsString() + ".dat");
            Path path4 = UNIVERSAL_PLAYER_DATA_DIR.resolve(player.getUuidAsString() + ".dat_old");

            Util.backupAndReplace(path3, path2Copy, path4);

            Files.deleteIfExists(path2Copy);
        } catch (IOException e) {
            LOGGER.error("Failed to save universal player data for {}", player.getName().getString(), e);
        }
    }

    @Final
    @Shadow
    private File playerDataDir;

    @Inject(method = "loadPlayerData", at = @At("HEAD"), cancellable = true)
    private void loadPlayerData(PlayerEntity player, String extension, CallbackInfoReturnable<Optional<NbtCompound>> cir) {
        File worldPlayerData = new File(playerDataDir, player.getUuidAsString() + extension);
        File universalPlayerData = new File(UNIVERSAL_PLAYER_DATA_DIR.toFile(), player.getUuidAsString() + extension);

        if (worldPlayerData.exists() && worldPlayerData.isFile() && universalPlayerData.exists() && universalPlayerData.isFile()) {
            try {
                NbtCompound worldNbt = NbtIo.readCompressed(worldPlayerData.toPath(), NbtSizeTracker.ofUnlimitedBytes());
                NbtCompound universalNbt = NbtIo.readCompressed(universalPlayerData.toPath(), NbtSizeTracker.ofUnlimitedBytes());

                if (worldNbt.contains("SpawnX") && worldNbt.contains("SpawnY") && worldNbt.contains("SpawnZ")) {
                    int spawnX = worldNbt.getInt("SpawnX");
                    int spawnY = worldNbt.getInt("SpawnY");
                    int spawnZ = worldNbt.getInt("SpawnZ");

                    universalNbt.putInt("SpawnX", spawnX);
                    universalNbt.putInt("SpawnY", spawnY);
                    universalNbt.putInt("SpawnZ", spawnZ);

                    NbtIo.writeCompressed(universalNbt, universalPlayerData.toPath());

                    cir.setReturnValue(Optional.of(universalNbt));
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to load or modify player data for {}", player.getName().getString(), e);
            }
        } else if (universalPlayerData.exists() && universalPlayerData.isFile()){
            try {
                cir.setReturnValue(Optional.of(NbtIo.readCompressed(universalPlayerData.toPath(), NbtSizeTracker.ofUnlimitedBytes())));
            } catch (Exception e) {
                LOGGER.warn("Failed to load or modify player data for {}", player.getName().getString(), e);
            }
        }
    }
}
