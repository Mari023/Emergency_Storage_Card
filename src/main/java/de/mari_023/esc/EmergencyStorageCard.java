package de.mari_023.esc;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import appeng.api.upgrades.Upgrades;

@Mod(EmergencyStorageCard.MOD_ID)
public class EmergencyStorageCard {
    private static boolean RAN_INIT = false;
    public static final String MOD_ID = "emergency_storage_card";
    public static Item EMERGENCY_STORAGE_CARD;

    public EmergencyStorageCard(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener((RegisterEvent event) -> {
            if (RAN_INIT)
                return;
            RAN_INIT = true;

            EMERGENCY_STORAGE_CARD = Upgrades.createUpgradeCardItem(new Item.Properties().stacksTo(1));
            Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, MOD_ID), EMERGENCY_STORAGE_CARD);
        });
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        player.getInventory();
    }
}
