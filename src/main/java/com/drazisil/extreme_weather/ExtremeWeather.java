package com.drazisil.extreme_weather;

import com.drazisil.extreme_weather.entity.AdvancedLightingBoltEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("extreme_weather")
public class ExtremeWeather
{
    public static final String MOD_ID = "extreme_weather";
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_ID);

    public static final RegistryObject<EntityType<AdvancedLightingBoltEntity>> ADVANCED_BOLT = ENTITIES.register("advanced_bolt", () -> EntityType.Builder.of(AdvancedLightingBoltEntity::new, EntityClassification.MISC).noSave().sized(0.0F, 0.0F).clientTrackingRange(16).updateInterval(Integer.MAX_VALUE).build(MOD_ID));

    public ExtremeWeather() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());

    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().options);
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("extreme_weather", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // This event fires when the chunk looks to see where it can spawn new entities on every tick
        // It is as close to an every chunk tick event as I've seen so far.

        World world = event.player.level;

        boolean isRaining = world.isRaining();
        boolean isThundering = world.isThundering();

        boolean shouldStrike = world.random.nextInt(1000) == 0;

        if (isRaining && isThundering && shouldStrike) {

            LOGGER.debug("Could Strike");

        }
    }

    @SubscribeEvent
    public void onLightningSpawn(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof LightningBoltEntity && !(entity instanceof AdvancedLightingBoltEntity)) {
            // Cast to LightingBolt Entity
            LightningBoltEntity lightningBoltEntity = (LightningBoltEntity) entity;

            World world = event.getWorld();
            BlockPos position = lightningBoltEntity.blockPosition();

            LOGGER.info("Lighting bolt");


            AdvancedLightingBoltEntity new_bolt = EntityType.Builder.of(AdvancedLightingBoltEntity::new, EntityClassification.MISC).noSave().sized(0.0F, 0.0F).clientTrackingRange(16).updateInterval(Integer.MAX_VALUE).build(MOD_ID).create(world);
            assert new_bolt != null;
            new_bolt.moveTo(Vector3d.atBottomCenterOf(position));
            boolean shouldSpawn = world.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
            new_bolt.setVisualOnly(shouldSpawn);
            world.addFreshEntity(new_bolt);

            event.setCanceled((true));
        }
    }
}
