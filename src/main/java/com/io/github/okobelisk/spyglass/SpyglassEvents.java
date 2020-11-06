package com.io.github.okobelisk.spyglass;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IceBlock;
import net.minecraft.block.TNTBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SpyglassEvents {

    private int heatEntity = 0;
    private int heatBlock = 0;
    private UUID currentEntity;

    private double radius = 5D;

    private final Random rand = new Random();

    @SubscribeEvent
    public void klutzCurse(TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        World world = event.player.world;

        if (world == null)
            return;

        if (!(player.getActiveItemStack().getItem() == Spyglass.SPYGLASS.get()))
            return;

        if (!(EnchantmentHelper.getEnchantmentLevel(Spyglass.CURSE_OF_KLUTZ_ENCHANTMENT.get(), player.getActiveItemStack()) > 0))
            return;

        if (player.isCreative())
            return;

        double distance = 42;
        Vec3d vec3d = player.getEyePosition(1.0F);
        Vec3d vec3d1 = player.getLook(1.0F);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * distance, vec3d1.y * distance, vec3d1.z * distance);
        EntityRayTraceResult entityRayTraceResult = ProjectileHelper.rayTraceEntities(player, vec3d, vec3d2, player.getBoundingBox().expand(vec3d1.scale(distance)).grow(1.0D, 1.0D, 1.0D), (p_215312_0_) -> !p_215312_0_.isSpectator() && p_215312_0_.canBeCollidedWith(), distance * distance);
        if (entityRayTraceResult != null && entityRayTraceResult.getEntity() instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) entityRayTraceResult.getEntity();
            if (!entity.canEntityBeSeen(player))
                return;

            if (entity instanceof IMob) {
                // yeah.. not too sure about this one
                try {
                    ((MobEntity) entity).setAttackTarget(player);
                } catch (ClassCastException ex) {
                    Spyglass.LOGGER.warn("Tried to set an attack target on an invalid mob");
                }
            }
        }
    }

    @SubscribeEvent
    public void spectralLens(TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        World world = event.player.world;

        if (world == null)
            return;

        if (!(player.getActiveItemStack().getItem() == Spyglass.SPYGLASS.get())) {
            return;
        }

        if (!(EnchantmentHelper.getEnchantmentLevel(Spyglass.SPECTRAL_LENS_ENCHANTMENT.get(), player.getActiveItemStack()) > 0))
            return;

        if(!player.isSneaking()) {
            radius = 5;
            return;
        }

        radius += .05D;

        if(!world.isRemote) {

            AxisAlignedBB alignedBB = new AxisAlignedBB(player.getPosX(), player.getPosY(), player.getPosZ(),
                    player.getPosX() + radius, player.getPosY() + radius, player.getPosZ() + radius);

            List<LivingEntity> entityList = world.getEntitiesWithinAABB(LivingEntity.class, alignedBB);
            for(LivingEntity entity : entityList){
                if(!entity.equals(player)) {
                    entity.addPotionEffect(new EffectInstance(Effects.GLOWING, 10, 0, false, false));
                }
            }

            System.out.printf("%s | %s\n", entityList.size(), radius);

            if(radius >= 30){
                for(LivingEntity entity : entityList){
                    if(!entity.equals(player)) {
                        entity.removeActivePotionEffect(Effects.GLOWING);
                    }
                }
                radius = 5;
            }
        }
    }




    /*
         it checks if the spyglass is active & has flaring, if not set heat to 0
        checks if player can see the sky
        checks if dimension has the sun and is daytime
        raytraces all entities non collaterally in 42 block distance
        raytraces all blocks non collaterally in 42 block distance
        if the block raytrace result doesn't hit an entity it will start the logic for heating up blocks

        increases the heat integer for blocks by 1 every tick
        if the heat int reaches >= 20 then it has a 20% chance to spawn a particle favouring the side of the block ur looking at
        if heat reaches 50 then it has a 80% chance to play the fire ambient noise at the block location
        once heat reaches 100 it checks if the block ur looking at is tnt or ice, and ignites/melts, then resets back to 0

        if the entity raytrace result doesn't hits an entity it will increase the heat integer for entities by 1 every tick then..

        check if it's living, not immune to fire, and make sure it can actually be seen by the player
        checks the unique id of the entity and stores it so u cant 'transfer' the heat
        more particle and sound crap
        once heat reaches 150 it will check if the entity is a creeper, if so, ignite it
        5% chance to ignite player
        then sets the entity on fire for 5 seconds
     */
    @SubscribeEvent
    public void flaringCurse(TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        World world = event.player.world;

        if (world == null)
            return;

        // Reset heat when not scoped in
        if (!(player.getActiveItemStack().getItem() == Spyglass.SPYGLASS.get())) {
            heatEntity = 0;
            heatBlock = 0;
            return;
        }

        if (!(EnchantmentHelper.getEnchantmentLevel(Spyglass.CURSE_OF_FLARING_ENCHANTMENT.get(), player.getActiveItemStack()) > 0))
            return;

        // Make sure we're standing in the sunlight
        if (!world.canBlockSeeSky(new BlockPos(player.getPosX(), player.getPosY(), player.getPosZ())))
            return;

        //System.out.printf("%s | %s \n", heatB, heatE);
        if (world.isDaytime() && world.getDimension().hasSkyLight()) {
            double distance = 42;
            Vec3d vec3d = player.getEyePosition(1.0F);
            Vec3d vec3d1 = player.getLook(1.0F);
            Vec3d vec3d2 = vec3d.add(vec3d1.x * distance, vec3d1.y * distance, vec3d1.z * distance);
            EntityRayTraceResult entityRayTraceResult = ProjectileHelper.rayTraceEntities(player, vec3d, vec3d2, player.getBoundingBox().expand(vec3d1.scale(distance)).grow(1.0D, 1.0D, 1.0D), (p_215312_0_) -> !p_215312_0_.isSpectator() && p_215312_0_.canBeCollidedWith(), distance * distance);
            RayTraceResult blockRayTraceResult = player.pick(distance, 1.0F, false);

            if (blockRayTraceResult.getType() == RayTraceResult.Type.BLOCK && entityRayTraceResult == null) {
                BlockPos blockpos = ((BlockRayTraceResult) blockRayTraceResult).getPos();
                Direction blockside = ((BlockRayTraceResult) blockRayTraceResult).getFace();
                BlockState blockstate = world.getBlockState(blockpos);

                heatBlock++;

                if (heatBlock >= 10 && rand.nextFloat() <= 0.15F) {
                    world.addParticle(ParticleTypes.FLAME, true, blockpos.getX() + blockside.getXOffset() / 1.5F + rand.nextFloat(), blockpos.getY() + blockside.getYOffset() / 1.5F + rand.nextFloat(), blockpos.getZ() + blockside.getZOffset() / 1.5F + rand.nextFloat(), 0, 0, 0);
                }

                if (heatBlock == 50 && rand.nextFloat() <= 0.8F) {
                    world.playSound(null, blockpos, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.PLAYERS, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.2F) * 1.2F);
                }

                if (heatBlock >= 100 && !world.isRemote) {
                    heatBlock = 0;

                    if (blockstate.getBlock() instanceof TNTBlock) {
                        world.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 11);
                        blockstate.getBlock().catchFire(blockstate, world, blockpos, null, player);
                    } else if (blockstate.getBlock() instanceof IceBlock) {
                        if (world.dimension.doesWaterVaporize()) {
                            world.removeBlock(blockpos, false);
                        } else {
                            world.setBlockState(blockpos, Blocks.WATER.getDefaultState());
                            world.neighborChanged(blockpos, Blocks.WATER, blockpos);
                        }
                    }
                }
            }

            if (entityRayTraceResult == null) {
                heatEntity = 0;
                return;
            }

            if (entityRayTraceResult.getEntity() instanceof LivingEntity) {
                LivingEntity entity = (LivingEntity) entityRayTraceResult.getEntity();
                if (entity.isImmuneToFire() || !entity.canEntityBeSeen(player))
                    return;

                // So we can't transfer the heat to other entities
                if (currentEntity != entity.getUniqueID()) {
                    currentEntity = entity.getUniqueID();
                    heatEntity = 0;
                }

                heatEntity++;

                if (rand.nextFloat() <= 0.15F) {
                    world.addParticle(ParticleTypes.FLAME, true, entity.getPosXRandom(1D), entity.getPosYRandom(), entity.getPosZRandom(1D), 0, 0, 0);
                }

                if (rand.nextFloat() <= 0.8F && heatEntity == 20 || heatEntity == 90) {
                    world.playMovingSound(null, entity, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.PLAYERS, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.2F) * 1.2F);
                }

                if (heatEntity >= 150 && !world.isRemote) {
                    heatEntity = 0;

                    if (entity instanceof CreeperEntity)
                        ((CreeperEntity) entity).ignite();

                    if (rand.nextFloat() <= .05F && !player.isCreative())
                        player.setFire(5);

                    entity.setFire(5);
                }
            }
        }
    }
}
