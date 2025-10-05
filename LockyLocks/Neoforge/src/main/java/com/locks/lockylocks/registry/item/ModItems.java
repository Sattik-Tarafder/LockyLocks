package com.locks.lockylocks.registry.item;

import com.locks.lockylocks.LockyLocks;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LockyLocks.MOD_ID);

    public static final DeferredItem<Item> IRON_KEY = ITEMS.register("iron_key",
            () -> new Item(new Item.Properties().stacksTo(1).durability(35)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.lockylocks.iron_key"));
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.lockylocks.iron_key.usage"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.lockylocks.iron_key.press"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });

    public static final DeferredItem<Item> IRON_LOCK = ITEMS.register("iron_lock",
            () -> new Item(new Item.Properties().durability(180)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.lockylocks.iron_lock"));
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.lockylocks.iron_lock.usage"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.lockylocks.iron_lock.press"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });

    public static final DeferredItem<Item> COPPER_LOCK = ITEMS.register("copper_lock",
            () -> new Item(new Item.Properties().durability(120)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.lockylocks.copper_lock"));
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.lockylocks.copper_lock.usage"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.lockylocks.copper_lock.press"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
