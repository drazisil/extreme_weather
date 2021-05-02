package com.drazisil.extreme_weather.client.event;

import net.minecraft.entity.EntityType;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.drazisil.extreme_weather.ExtremeWeather.EW_LOGGER;
import static com.drazisil.extreme_weather.ExtremeWeather.MOD_ID;
import static net.minecraftforge.api.distmarker.Dist.CLIENT;
import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = FORGE, value = CLIENT)
public class ClientForgeEventSubscriber {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity().getType() == EntityType.LIGHTNING_BOLT) {
            EW_LOGGER.info("A lighting bolt has entered the world.");
        }
    }
}
