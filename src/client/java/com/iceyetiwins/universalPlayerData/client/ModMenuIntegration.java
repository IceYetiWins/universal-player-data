package com.iceyetiwins.universalPlayerData.client;

import com.iceyetiwins.universalPlayerData.ModConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> YetAnotherConfigLib.createBuilder()
                .save(() -> ModConfig.HANDLER.save())
                .title(Component.literal("Universal Player Data Mod Options."))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Universal Player Data"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Options"))
                                .description(OptionDescription.of(Component.literal("Mod Options")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.literal("Start At Spawn"))
                                        .description(OptionDescription.of(Component.literal("Whether or not you start at your spawn point every time you join a world. If this is unchecked you will want to add Motion, Pos, and Rotation to your NBT Tag Exclusions so that position is per-world (to avoid suffocation).")))
                                        .binding(true, () -> ModConfig.HANDLER.instance().startAtSpawn, newVal -> ModConfig.HANDLER.instance().startAtSpawn = newVal)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                        .build())
                        .option(ListOption.<String>createBuilder()
                                .name(Component.literal("NBT Tag Exclusions"))
                                .description(OptionDescription.of(Component.literal("Which player data should not be shared between all worlds. For example playerGameType so that some worlds can be survival while others are creative. \n\nThese can be found using NBTExplorer.")))
                                .binding(List.of("abilities", "LastDeathLocation", "playerGameType", "respawn", "warden_spawn_tracker"), () -> ModConfig.HANDLER.instance().NbtTagExclusions, newVal -> ModConfig.HANDLER.instance().NbtTagExclusions = newVal)
                                .controller(StringControllerBuilder::create) // usual controllers, passed to every entry
                                .initial("") // when adding a new entry to the list, this is the initial value it has
                                .build())
                        .build())
                .build()
                .generateScreen(parentScreen);
    }
}
