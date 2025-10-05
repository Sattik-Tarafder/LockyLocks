package com.locks.lockylocks;

import com.locks.lockylocks.registry.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MOD_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LockyLocks.MOD_ID);

    public static final Supplier<CreativeModeTab> LOCKYLOCKS_ITEM_TAB = CREATIVE_MOD_TAB.register("lockylocks_item_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.COPPER_LOCK.get()))
                    .title(Component.translatable("creativetab.lockylocks.lockylocks_item"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.IRON_KEY.get());
                        output.accept(ModItems.IRON_LOCK.get());
                        output.accept(ModItems.COPPER_LOCK.get());
                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MOD_TAB.register(eventBus);
    }
}
