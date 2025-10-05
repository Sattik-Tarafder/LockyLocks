package com.locks.lockylocks.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue PROTECT_LOCKED_CONTAINERS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("LockyLocks Configuration");
        PROTECT_LOCKED_CONTAINERS = builder
                .comment("Should locked containers be unbreakable? (true/false)")
                .define("protectLockedContainers", true);

        SPEC = builder.build();
    }
}