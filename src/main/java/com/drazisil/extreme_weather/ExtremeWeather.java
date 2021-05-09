package com.drazisil.extreme_weather;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("extreme_weather")
public class ExtremeWeather
{
    public static final String MOD_ID = "extreme_weather";
    // Directly reference a log4j logger.
    public static final Logger EW_LOGGER = LogManager.getLogger();

    public static final int STRIKE_CHANCE  = 1000;

    public static boolean IS_WORLD_LOADED  = false;
    public static final boolean SHOULD_LIGHTNING_EXPLODE = true;


    public ExtremeWeather() {


    }


}
