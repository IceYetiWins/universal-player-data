package com.iceyetiwins.universalPlayerData.mixin;

import com.iceyetiwins.universalPlayerData.ModConfig;
import com.iceyetiwins.universalPlayerData.UniversalPlayerData;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Mixin(PlayerDataStorage.class)
public class PlayerDataStorageMixin {
    @Final
    @Shadow
    private File playerDir;

    private static final Path UNIVERSAL_PLAYER_DATA_DIR = FabricLoader.getInstance().getGameDir().resolve("universal-playerdata");

    //this copies world player data save to universal player data folder
    @Inject(at = @At("TAIL"), method = "save")
    private void onPlayerSave (Player player, CallbackInfo ci){
        try {
            Files.createDirectories(UNIVERSAL_PLAYER_DATA_DIR);

            File worldPlayerData = new File(playerDir, player.getStringUUID() + ".dat");
            File worldPlayerDataBackup = new File(playerDir, player.getStringUUID() + ".dat_old");
            File universalPlayerData = new File(UNIVERSAL_PLAYER_DATA_DIR.toFile(), player.getStringUUID() + ".dat");
            File universalPlayerDataBackup = new File(UNIVERSAL_PLAYER_DATA_DIR.toFile(), player.getStringUUID() + ".dat_old");

            if (worldPlayerData.exists() && worldPlayerData.isFile()){
                Files.copy(worldPlayerData.toPath(), universalPlayerData.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            if (worldPlayerDataBackup.exists() && worldPlayerDataBackup.isFile()){
                Files.copy(worldPlayerData.toPath(), universalPlayerDataBackup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            UniversalPlayerData.LOGGER.error("Failed to save universal player data for {}", player.getName().getString(), e);
        }
    }

    //load universal player data instead of world player data unless universal player data doesn't exist
    @Inject(at = @At("HEAD"), method = "load(Lnet/minecraft/server/players/NameAndId;Ljava/lang/String;)Ljava/util/Optional;", cancellable = true)
    private void onLoad (NameAndId nameAndId, String suffix, CallbackInfoReturnable<Optional<CompoundTag>> cir) {
        File worldPlayerData = new File(playerDir, nameAndId.id() + suffix);
        File universalPlayerData = new File(UNIVERSAL_PLAYER_DATA_DIR.toFile(), nameAndId.id() + suffix);
        if (universalPlayerData.exists() && universalPlayerData.isFile()) {
            try {
                if (worldPlayerData.exists() && worldPlayerData.isFile() && universalPlayerData.exists() && universalPlayerData.isFile()) {
                    CompoundTag worldNbt = NbtIo.readCompressed(worldPlayerData.toPath(), NbtAccounter.unlimitedHeap());
                    CompoundTag universalNbt = NbtIo.readCompressed(universalPlayerData.toPath(), NbtAccounter.unlimitedHeap());

                    for (String tag : ModConfig.HANDLER.instance().NbtTagExclusions) {
                        if (worldNbt.contains(tag)) {
                            universalNbt.remove(tag);
                            universalNbt.put(tag, worldNbt.get(tag));
                        } else {
                            universalNbt.remove(tag);
                        }
                    }

                    cir.setReturnValue(Optional.of(universalNbt));
                } else if (universalPlayerData.exists() && universalPlayerData.isFile()) {
                    CompoundTag universalNbt = NbtIo.readCompressed(universalPlayerData.toPath(), NbtAccounter.unlimitedHeap());

                    for (String tag : ModConfig.HANDLER.instance().NbtTagExclusions) {
                        universalNbt.remove(tag);
                    }

                    cir.setReturnValue(Optional.of(universalNbt));
                }

            } catch (Exception e) {
                UniversalPlayerData.LOGGER.error("Failed to modify or load universal player data for {}", nameAndId.name());
            }
        }
    }
}
