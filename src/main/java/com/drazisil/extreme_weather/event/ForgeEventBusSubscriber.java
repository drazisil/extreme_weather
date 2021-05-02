package com.drazisil.extreme_weather.event;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.drazisil.extreme_weather.ExtremeWeather.EW_LOGGER;
import static com.drazisil.extreme_weather.ExtremeWeather.MOD_ID;
import static net.minecraftforge.api.distmarker.Dist.CLIENT;
import static net.minecraftforge.api.distmarker.Dist.DEDICATED_SERVER;
import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE;

// These subscribers listen to forge events on both server and client side
@Mod.EventBusSubscriber(modid = MOD_ID, bus = FORGE, value = {CLIENT, DEDICATED_SERVER})
public class ForgeEventBusSubscriber {

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent event) {
        PlayerEntity player = event.getPlayer();
        World world = player.level;
        Item item = event.getItemStack().getItem();

        if (item == Items.STICK && !world.isClientSide) {
            EW_LOGGER.info("stick");

            LightningBoltEntity bolt = EntityType.LIGHTNING_BOLT.create(world);

            Vector3d targetVec = player.position();

            bolt.teleportTo(targetVec.x, targetVec.y, targetVec.z);

            world.addFreshEntity(bolt);
            EW_LOGGER.info("new bolt added at " + targetVec);
        }
    }
}
