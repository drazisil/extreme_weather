package com.drazisil.extreme_weather.event;

import com.drazisil.extreme_weather.Helpers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.drazisil.extreme_weather.ExtremeWeather.*;
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

            Vector3d targetVec = player.position();

            BlockPos newTargetPos = Helpers.getTopSolidBlockForPlayer(world, new BlockPos(targetVec), player);

            Helpers.spawnLightning(world, newTargetPos);

        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        EntityType<?> entityType = event.getEntity().getType();
        World world = event.getWorld();

        if (entityType == EntityType.LIGHTNING_BOLT && !world.isClientSide) {

            LightningBoltEntity bolt = (LightningBoltEntity) event.getEntity();

            bolt.setVisualOnly(true);

            Helpers.boomLightning(world, bolt.blockPosition());
            return;
        }

        if (IS_WORLD_LOADED && entityType != EntityType.LIGHTNING_BOLT && !world.isClientSide) {

            boolean isRaining = world.isRaining();
            boolean isThundering = world.isThundering();

            boolean shouldStrike = world.random.nextInt(STRIKE_CHANCE) == 0;

            if (isRaining && isThundering && shouldStrike) {

                BlockPos playerBlockPos = event.getEntity().blockPosition();
                BlockPos newBoltPos = Helpers.findLightingTargetAround(world, playerBlockPos);

                Helpers.spawnLightning(world, newBoltPos);
            }
            return;
        }

        if (entityType == EntityType.PLAYER && !world.isClientSide) {

            IS_WORLD_LOADED = true;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        // This event fires when the chunk looks to see where it can spawn new entities on every tick
        // It is as close to an every chunk tick event as I've seen so far.

        Entity player = event.getEntity();
        World world = player.level;

        boolean isRaining = world.isRaining();
        boolean isThundering = world.isThundering();

        boolean shouldStrike = world.random.nextInt(STRIKE_CHANCE) == 0;

        if (isRaining && isThundering && shouldStrike) {

            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            BlockPos playerBlockPos = new BlockPos(x, y, z);
            BlockPos newBoltPos = Helpers.findLightingTargetAround(world, playerBlockPos);

            Helpers.spawnLightning(world, newBoltPos);
        }
    }
}
