package com.iceyetiwins.universalPlayerData;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import java.util.List;

public class ModConfig {
    public static ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
            .id(Identifier.fromNamespaceAndPath("universal-player-data", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("universal-player-data.json"))
                    .setJson5(false)
                    .build())
            .build();

    @SerialEntry
    public boolean startAtSpawn = true;

    @SerialEntry
    public List<String> NbtTagExclusions = List.of("abilities", "LastDeathLocation", "playerGameType", "respawn", "warden_spawn_tracker");
}