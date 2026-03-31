package com.iceyetiwins.universalPlayerData;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.world.level.portal.TeleportTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniversalPlayerData implements ModInitializer {
    public static final String MOD_ID = "universal-player-data";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModConfig.HANDLER.load();

        ServerPlayerEvents.JOIN.register((player) -> {
            if (ModConfig.HANDLER.instance().startAtSpawn) {
                player.teleport(player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING));

                LOGGER.info("{} logged in at ({}, {}, {})", player.getName().getString(), player.getX(), player.getY(), player.getZ());
            }
        });
    }
}