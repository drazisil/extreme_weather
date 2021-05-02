package com.drazisil.extreme_weather.client;

import com.drazisil.extreme_weather.ExtremeWeather;
import com.drazisil.extreme_weather.ModEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ExtremeWeather.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventBusSubscriber {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ExtremeWeather.EW_LOGGER.info("Registering rendering handler");
        RenderingRegistry.registerEntityRenderingHandler(ModEntityType.ADVANCED_BOLT.get(), AdvancedLightningBoltRender::new);
    }
}