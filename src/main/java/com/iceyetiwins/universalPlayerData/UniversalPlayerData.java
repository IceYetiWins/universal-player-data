package com.iceyetiwins.universalPlayerData;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.TeleportTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniversalPlayerData implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("Minecraft");

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();

            player.teleportTo(player.getRespawnTarget(true, TeleportTarget.NO_OP));

            LOGGER.info("{} logged in at ({}, {}, {})", player.getName().getString(), player.getX(), player.getY(), player.getZ());
        });
    }
}
