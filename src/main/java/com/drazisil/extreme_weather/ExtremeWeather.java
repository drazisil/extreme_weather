package com.drazisil.extreme_weather;

import com.drazisil.extreme_weather.client.AdvancedLightningBoltRender;
import com.drazisil.extreme_weather.entity.AdvancedLightningBoltEntity;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
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
    public static final Logger EW_LOGGER = LogManager.getLogger();

    public static final int STRIKE_CHANCE  = 600;
    public static final boolean SHOULD_LIGHTNING_EXPLODE = true;

    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_ID);

    public static final RegistryObject<EntityType<AdvancedLightningBoltEntity>> ADVANCED_BOLT = ENTITIES.register("advanced_bolt", () -> EntityType.Builder.of(AdvancedLightningBoltEntity::new, EntityClassification.MISC).noSave().sized(0.0F, 0.0F).clientTrackingRange(16).updateInterval(Integer.MAX_VALUE).build(MOD_ID));

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
        EW_LOGGER.info("HELLO FROM PREINIT");
        EW_LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());

    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        EW_LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().options);


        EW_LOGGER.info("Registering Renderer..");


        RenderingRegistry.registerEntityRenderingHandler(ADVANCED_BOLT.get(), AdvancedLightningBoltRender::new);

        EW_LOGGER.info("Model registered");
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("extreme_weather", "helloworld", () -> { EW_LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        EW_LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        EW_LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // This event fires when the chunk looks to see where it can spawn new entities on every tick
        // It is as close to an every chunk tick event as I've seen so far.

        PlayerEntity player = event.player;
        World world = player.level;

        boolean isRaining = world.isRaining();
        boolean isThundering = world.isThundering();

        boolean shouldStrike = world.random.nextInt(STRIKE_CHANCE) == 0;

        if (isRaining && isThundering && shouldStrike) {

            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            BlockPos playerBlockPos = new BlockPos(x, y, z);
            Chunk chunk = (Chunk) world.getChunk(playerBlockPos);
            AdvancedLightningBoltEntity.strike(chunk, world);
        }
    }

    @SubscribeEvent
    public void onLightningSpawn(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof LightningBoltEntity && !(entity instanceof AdvancedLightningBoltEntity)) {
            // Cast to LightingBolt Entity
            LightningBoltEntity old_bolt = (LightningBoltEntity) entity;

            World world = event.getWorld();

            BlockPos position = old_bolt.blockPosition();

            EW_LOGGER.info("Replacing old bolt " + position + entity.getStringUUID());

            double x = old_bolt.getX();
            double y = old_bolt.getY();
            double z = old_bolt.getZ();
            BlockPos blockPos = new BlockPos(x, y, z);
            Chunk chunk = (Chunk) world.getChunk(blockPos);

            AdvancedLightningBoltEntity.strike(chunk, world);

            event.setCanceled((true));
        }

        if (entity instanceof AdvancedLightningBoltEntity) {
            EW_LOGGER.debug("advanced bolt has joined the world");
        }

    }

    @SubscribeEvent
    public void onEnterChunk(EntityEvent.EnteringChunk event) {
        if (event.getEntity() instanceof AdvancedLightningBoltEntity) {
            EW_LOGGER.debug("Advanced bolt has entered chunk");
        }
    }

    @SubscribeEvent
    public void onEntityDamage(EntityStruckByLightningEvent event) {
        if (event.getEntity() instanceof AdvancedLightningBoltEntity) {
            EW_LOGGER.warn("Advanced bolt was hit by lightning!");
        }
    }

    @SubscribeEvent
    public void onLeaveWorld(EntityLeaveWorldEvent event) {
        if (event.getEntity() instanceof AdvancedLightningBoltEntity) {
            EW_LOGGER.debug("Advanced bolt has left the world after living " + event.getEntity().tickCount + " ticks");
        }

        if (event.getEntity() instanceof LightningBoltEntity) {
            EW_LOGGER.debug("normal bolt has left the world after living " + event.getEntity().tickCount + " ticks");
        }
    }
}
