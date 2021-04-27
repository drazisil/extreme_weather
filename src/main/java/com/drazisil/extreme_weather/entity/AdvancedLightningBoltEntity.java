package com.drazisil.extreme_weather.entity;

import com.drazisil.extreme_weather.ExtremeWeather;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import static com.drazisil.extreme_weather.ExtremeWeather.MOD_ID;
import static com.drazisil.extreme_weather.ExtremeWeather.SHOULD_LIGHTNING_EXPLODE;

public class AdvancedLightningBoltEntity extends LightningBoltEntity {
    private int life;
    private boolean visualOnly;
    private int flashes;
    private ServerPlayerEntity cause;

    public AdvancedLightningBoltEntity(EntityType<? extends LightningBoltEntity> p_i231491_1_, World p_i231491_2_) {
        super(p_i231491_1_, p_i231491_2_);
        this.noCulling = true;
        this.life = 5;
        this.seed = this.random.nextLong();
        this.flashes = this.random.nextInt(3) + 1;
    }

    public void setVisualOnly(boolean p_233623_1_) {
        this.visualOnly = p_233623_1_;
    }

    public void setCause(@Nullable ServerPlayerEntity p_204809_1_) {
        this.cause = p_204809_1_;
    }

    public void tick() {
        super.tick();
        if (this.life == 2) {
            Difficulty difficulty = this.level.getDifficulty();
            if (difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD) {
                this.spawnFire(4);
            }

            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 10000.0F, 0.8F + this.random.nextFloat() * 0.2F);
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundCategory.WEATHER, 2.0F, 0.5F + this.random.nextFloat() * 0.2F);
        }

        --this.life;
        if (this.life < 0) {
            if (this.flashes == 0) {
                this.remove();
            } else if (this.life < -this.random.nextInt(10)) {
                --this.flashes;
                this.life = 1;
                this.seed = this.random.nextLong();
                this.spawnFire(0);
            }
        }

        if (this.life >= 0) {
            if (!(this.level instanceof ServerWorld)) {
                this.level.setSkyFlashTime(2);
            } else if (!this.visualOnly) {
                double d0 = 3.0D;
                List<Entity> list = this.level.getEntities(this, new AxisAlignedBB(this.getX() - 3.0D, this.getY() - 3.0D, this.getZ() - 3.0D, this.getX() + 3.0D, this.getY() + 6.0D + 3.0D, this.getZ() + 3.0D), Entity::isAlive);

                for (Entity entity : list) {
                    if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(entity, this))
                        entity.thunderHit((ServerWorld) this.level, this);
                }

                if (this.cause != null) {
                    CriteriaTriggers.CHANNELED_LIGHTNING.trigger(this.cause, list);
                }
            }
        }

    }

    public boolean ignoreExplosion() {
        return true;
    }


    public static void strike(Chunk chunk, World world) {
        int i = chunk.getPos().getMinBlockX();
        int j = chunk.getPos().getMinBlockZ();

        BlockPos blockpos = AdvancedLightningBoltEntity.findLightingTargetAround(world, world.getBlockRandomPos(i, 0, j, 15));
        if (world.isRainingAt(blockpos)) {
            DifficultyInstance difficultyinstance = world.getCurrentDifficultyAt(blockpos);
            boolean flag1 = world.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && world.random.nextDouble() < (double) difficultyinstance.getEffectiveDifficulty() * 0.01D;

            AdvancedLightningBoltEntity bolt = EntityType.Builder.of(AdvancedLightningBoltEntity::new, EntityClassification.MISC).noSave().sized(0.0F, 0.0F).clientTrackingRange(16).updateInterval(Integer.MAX_VALUE).build(MOD_ID).create(world);
            Vector3d targetPos = Vector3d.atBottomCenterOf(blockpos);
            bolt.moveTo(targetPos);
            bolt.setVisualOnly(flag1);
            world.addFreshEntity(bolt);
            ExtremeWeather.EW_LOGGER.debug("Advanced Strike");
            if (SHOULD_LIGHTNING_EXPLODE) {
//                Explosion.Mode explosion$mode = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(bolt.level, bolt) ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;
                int explosionRadius = 3;
                float f = 1.0F;
                bolt.level.explode(bolt, bolt.getX(), bolt.getY(), bolt.getZ(), (float) explosionRadius * f, Explosion.Mode.DESTROY);
            }

        }

    }

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


    private void spawnFire(int p_195053_1_) {
        if (!this.visualOnly && !this.level.isClientSide && this.level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            BlockPos blockpos = this.blockPosition();
            BlockState blockstate = AbstractFireBlock.getState(this.level, blockpos);
            if (this.level.getBlockState(blockpos).isAir() && blockstate.canSurvive(this.level, blockpos)) {
                this.level.setBlockAndUpdate(blockpos, blockstate);
            }

            for (int i = 0; i < p_195053_1_; ++i) {
                BlockPos blockpos1 = blockpos.offset(this.random.nextInt(3) - 1, this.random.nextInt(3) - 1, this.random.nextInt(3) - 1);
                blockstate = AbstractFireBlock.getState(this.level, blockpos1);
                if (this.level.getBlockState(blockpos1).isAir() && blockstate.canSurvive(this.level, blockpos1)) {
                    this.level.setBlockAndUpdate(blockpos1, blockstate);
                }
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldRenderAtSqrDistance(double p_70112_1_) {
        ExtremeWeather.EW_LOGGER.debug("Checking for shouldRender");
        double d0 = 64.0D * getViewScale();
        return p_70112_1_ < d0 * d0;
    }

    protected void defineSynchedData() {
    }

    protected void readAdditionalSaveData(CompoundNBT p_70037_1_) {
    }

    protected void addAdditionalSaveData(CompoundNBT p_213281_1_) {
    }

    public IPacket<?> getAddEntityPacket() {
        return new SSpawnObjectPacket(this);
    }
}
