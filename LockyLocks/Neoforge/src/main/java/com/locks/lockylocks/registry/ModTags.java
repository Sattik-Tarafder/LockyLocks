package com.locks.lockylocks.registry;

import com.locks.lockylocks.LockyLocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    public static class Items {
        public static TagKey<Item> LOCKS = createTag("locks");
        public static TagKey<Item> KEYS = createTag("keys");

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(LockyLocks.MOD_ID, name));
        }
    }
}
