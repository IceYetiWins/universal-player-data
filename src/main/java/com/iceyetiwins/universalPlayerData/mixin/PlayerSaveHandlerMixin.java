package com.iceyetiwins.universalPlayerData.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
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

    @Final
    @Shadow
    private File playerDataDir;

    @Inject(method = "savePlayerData", at = @At("TAIL"))
    private void onSavePlayerData(PlayerEntity player, CallbackInfo ci){
        try {
            Files.createDirectories(UNIVERSAL_PLAYER_DATA_DIR);

            File worldPlayerData = new File(playerDataDir, player.getUuidAsString() + ".dat");
            File worldPlayerDataBackup = new File(playerDataDir, player.getUuidAsString() + ".dat_old");
            File universalPlayerData = new File(UNIVERSAL_PLAYER_DATA_DIR.toFile(), player.getUuidAsString() + ".dat");
            File universalPlayerDataBackup = new File(UNIVERSAL_PLAYER_DATA_DIR.toFile(), player.getUuidAsString() + ".dat_old");

            if (worldPlayerData.exists() && worldPlayerData.isFile()){
                Files.copy(worldPlayerData.toPath(), universalPlayerData.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            if (worldPlayerDataBackup.exists() && worldPlayerDataBackup.isFile()){
                Files.copy(worldPlayerData.toPath(), universalPlayerDataBackup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save universal player data for {}", player.getName().getString(), e);
        }

    }

    @Inject(method = "loadPlayerData", at = @At("HEAD"), cancellable = true)
    private void loadPlayerData(PlayerEntity player, String extension, CallbackInfoReturnable<Optional<NbtCompound>> cir) {
        File worldPlayerData = new File(playerDataDir, player.getUuidAsString() + extension);
        File universalPlayerData = new File(UNIVERSAL_PLAYER_DATA_DIR.toFile(), player.getUuidAsString() + extension);

        if (worldPlayerData.exists() && worldPlayerData.isFile() && universalPlayerData.exists() && universalPlayerData.isFile()) {
            try {
                NbtCompound worldNbt = NbtIo.readCompressed(worldPlayerData.toPath(), NbtSizeTracker.ofUnlimitedBytes());
                NbtCompound universalNbt = NbtIo.readCompressed(universalPlayerData.toPath(), NbtSizeTracker.ofUnlimitedBytes());

                if (worldNbt.contains("Abilities")) {
                    universalNbt.remove("Abilities");
                    universalNbt.put("Abilities", worldNbt.get("Abilities"));
                } else {
                    universalNbt.remove("Abilities");
                }

                if (worldNbt.contains("LastDeathLocation")) {
                    universalNbt.put("LastDeathLocation", worldNbt.get("LastDeathLocation"));
                } else {
                    universalNbt.remove("LastDeathLocation");
                }

                if (worldNbt.contains("playerGameType")) {
                    universalNbt.remove("playerGameType");
                    universalNbt.put("playerGameType", worldNbt.get("playerGameType"));
                } else {
                    universalNbt.remove("playerGameType");
                }

                if (worldNbt.contains("respawn")){
                    universalNbt.remove("respawn");
                    universalNbt.put("respawn", worldNbt.get("respawn"));
                } else {
                    universalNbt.remove("respawn");
                }

                if (worldNbt.contains("warden_spawn_tracker")) {
                    universalNbt.put("warden_spawn_tracker", worldNbt.get("warden_spawn_tracker"));
                } else {
                    universalNbt.remove("warden_spawn_tracker");
                }

                NbtIo.writeCompressed(universalNbt, universalPlayerData.toPath());
                cir.setReturnValue(Optional.of(universalNbt));
            } catch (Exception e) {
                LOGGER.warn("Failed to load or modify player data for {}", player.getName().getString(), e);
            }
        } else if (universalPlayerData.exists() && universalPlayerData.isFile()){
            try {
                NbtCompound universalNbt = NbtIo.readCompressed(universalPlayerData.toPath(), NbtSizeTracker.ofUnlimitedBytes());
                universalNbt.remove("Abilities");
                universalNbt.remove("LastDeathLocation");
                universalNbt.remove("playerGameType");
                universalNbt.remove("respawn");
                universalNbt.remove("warden_spawn_tracker");

                cir.setReturnValue(Optional.of(universalNbt));
            } catch (Exception e) {
                LOGGER.warn("Failed to load or modify player data for {}", player.getName().getString(), e);
            }
        }
    }
}
