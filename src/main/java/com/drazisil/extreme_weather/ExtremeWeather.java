package com.drazisil.extreme_weather;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("extreme_weather")
public class ExtremeWeather
{
    public static final String MOD_ID = "extreme_weather";
    // Directly reference a log4j logger.
    public static final Logger EW_LOGGER = LogManager.getLogger();

    public static final int STRIKE_CHANCE  = 600;
    public static final boolean SHOULD_LIGHTNING_EXPLODE = false;


    public ExtremeWeather() {

        ModEntityType.ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }


}
