package com.iceyetiwins.universalPlayerData;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniversalPlayerData implements ModInitializer {
    public static final String MOD_ID = "universal-player-data";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final String[] NBT_TAGS = {"abilities", "LastDeathLocation", "playerGameType", "respawn", "warden_spawn_tracker"};

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();

            player.teleport(player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING));

            LOGGER.info("{} logged in at ({}, {}, {})", player.getName().getString(), player.getX(), player.getY(), player.getZ());
        });
    }
}