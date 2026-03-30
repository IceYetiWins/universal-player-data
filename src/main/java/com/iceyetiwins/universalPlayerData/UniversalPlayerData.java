package com.iceyetiwins.universalPlayerData;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniversalPlayerData implements ModInitializer {
    public static final String MOD_ID = "universal-player-data-2";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final String[] NBT_TAGS = {"abilities", "LastDeathLocation", "playerGameType", "respawn", "warden_spawn_tracker"};

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("this work?");
    }
}