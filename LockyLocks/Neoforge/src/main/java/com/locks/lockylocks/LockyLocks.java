package com.locks.lockylocks;

import com.locks.lockylocks.config.Config;
import com.locks.lockylocks.registry.item.ModItems;
import com.locks.lockylocks.sound.ModSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(LockyLocks.MOD_ID)
public class LockyLocks{
    public static final String MOD_ID = "lockylocks";

    public LockyLocks(IEventBus modEventBus) {
        ModCreativeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModSounds.register(modEventBus);

        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, Config.SPEC, "lockylocks-common.toml");
    }
}