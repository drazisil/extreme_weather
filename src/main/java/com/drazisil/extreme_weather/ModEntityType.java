package com.drazisil.extreme_weather;

import com.drazisil.extreme_weather.entity.AdvancedLightningBoltEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static com.drazisil.extreme_weather.ExtremeWeather.MOD_ID;

public class ModEntityType {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_ID);

    public static final RegistryObject<EntityType<AdvancedLightningBoltEntity>> ADVANCED_BOLT = ENTITY_TYPES.register("bolt",
            () -> EntityType.Builder.<AdvancedLightningBoltEntity>of(AdvancedLightningBoltEntity::new, EntityClassification.MISC)
                    .noSave()
                    .sized(0.0F, 0.0F)
                    .clientTrackingRange(16)
                    .updateInterval(Integer.MAX_VALUE)
                    .build("bolt"));

}
