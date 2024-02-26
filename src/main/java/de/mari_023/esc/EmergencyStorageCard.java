package de.mari_023.esc;

import org.jetbrains.annotations.UnknownNullability;

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

import de.mari_023.ae2wtlib.UpgradeHelper;
import de.mari_023.ae2wtlib.wct.CraftingTerminalHandler;
import de.mari_023.ae2wtlib.wut.WUTHandler;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.upgrades.Upgrades;
import appeng.integration.modules.curios.CuriosIntegration;
import appeng.me.helpers.PlayerSource;

@Mod(EmergencyStorageCard.MOD_ID)
public class EmergencyStorageCard {
    private static boolean RAN_INIT = false;
    public static final String MOD_ID = "emergency_storage_card";
    @UnknownNullability
    public static Item EMERGENCY_STORAGE_CARD;

    public EmergencyStorageCard(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener((RegisterEvent event) -> {
            if (RAN_INIT)
                return;
            RAN_INIT = true;

            EMERGENCY_STORAGE_CARD = Upgrades.createUpgradeCardItem(new Item.Properties().stacksTo(1));
            Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, MOD_ID), EMERGENCY_STORAGE_CARD);
            UpgradeHelper.addUpgradeToAllTerminals(EMERGENCY_STORAGE_CARD, 1);
        });
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        var handler = CraftingTerminalHandler.getCraftingTerminalHandler(player);
        if (!handler.inRange())
            return;
        if (!UpgradeInventories.forItem(handler.getCraftingTerminal(), WUTHandler.getUpgradeCardCount())
                .isInstalled(EMERGENCY_STORAGE_CARD))
            return;
        var grid = handler.getTargetGrid();
        if (grid == null)
            return;

        var gridInv = grid.getStorageService().getInventory();
        var playerSource = new PlayerSource(player, null);
        // store Player Inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            int insert = (int) gridInv.insert(AEItemKey.of(stack), stack.getCount(), Actionable.MODULATE, playerSource);
            stack.setCount(stack.getCount() - insert);
            player.getInventory().setItem(i, stack);
        }

        // store Curios
        var cap = player.getCapability(CuriosIntegration.ITEM_HANDLER);
        if (cap == null)
            return;
        for (int i = 0; i < cap.getSlots(); i++) {
            var stack = cap.extractItem(i, Integer.MAX_VALUE, true);
            int insert = (int) gridInv.insert(AEItemKey.of(stack), stack.getCount(), Actionable.SIMULATE, playerSource);
            stack = cap.extractItem(i, insert, false);
            gridInv.insert(AEItemKey.of(stack), stack.getCount(), Actionable.MODULATE, playerSource);
        }
    }
}
