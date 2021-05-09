package com.drazisil.extreme_weather;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;

import java.util.List;

import static com.drazisil.extreme_weather.ExtremeWeather.EW_LOGGER;
import static com.drazisil.extreme_weather.ExtremeWeather.SHOULD_LIGHTNING_EXPLODE;

public class Helpers {

    public static BlockPos findLightingTargetAround(World world, BlockPos p_175736_1_) {
        BlockPos blockpos = world.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING, p_175736_1_);
        AxisAlignedBB axisalignedbb = (new AxisAlignedBB(blockpos, new BlockPos(blockpos.getX(), world.getMaxBuildHeight(), blockpos.getZ()))).inflate(3.0D);
        List<LivingEntity> list = world.getEntitiesOfClass(LivingEntity.class, axisalignedbb, (p_241115_1_) -> {
            return p_241115_1_ != null && p_241115_1_.isAlive() && world.canSeeSky(p_241115_1_.blockPosition());
        });
        if (!list.isEmpty()) {
            return list.get(world.random.nextInt(list.size())).blockPosition();
        } else {
            if (blockpos.getY() == -1) {
                blockpos = blockpos.above(2);
            }

            return blockpos;
        }
    }

    public static void boomLightning(World world, BlockPos pos) {
        if (SHOULD_LIGHTNING_EXPLODE) {
            int explosionRadius = 3;
            float f = 1.0F;
            world.explode(null, pos.getX(), pos.getY(), pos.getZ(), (float) explosionRadius * f, Explosion.Mode.DESTROY);
        }
    }

    public static void spawnLightning(World world, BlockPos pos) {
        LightningBoltEntity bolt = EntityType.LIGHTNING_BOLT.create(world);

        assert bolt != null;
        bolt.setVisualOnly(true);

        bolt.teleportTo(pos.getX(), pos.getY(), pos.getZ());

        world.addFreshEntity(bolt);

        Helpers.boomLightning(world, pos);
    }

    public static BlockPos.Mutable getTopSolidBlockForPlayer(World world, BlockPos blockPos, PlayerEntity playerEntity) {
        if (world.loadedAndEntityCanStandOn(blockPos, playerEntity)) {
            return (BlockPos.Mutable) blockPos;
        }

        int x = blockPos.getX();
        int y = world.getMaxBuildHeight() - 1;
        int z = blockPos.getZ();

        BlockPos.Mutable blockPosCandidate = new BlockPos.Mutable(x, y, z);

        while (!world.getBlockState(blockPosCandidate).getMaterial().isSolid())
        {

            blockPosCandidate.setY(blockPosCandidate.getY() - 1);
        }
        return blockPosCandidate;
    }
}
