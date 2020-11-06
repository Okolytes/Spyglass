package com.io.github.okobelisk.spyglass;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class SpyglassItem extends Item {

    public boolean zoomed;
    public double originalGamma;

    public SpyglassItem() {
        super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.NONE; // todo
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!worldIn.isRemote && entityIn instanceof PlayerEntity) {
            if (stack.getTag() == null) {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putFloat(U.zoom, U.getMaxZoom());
                stack.setTag(nbt);
            } else if (!stack.getTag().contains(U.zoom)) {
                stack.getTag().putFloat(U.zoom, U.getMaxZoom());
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        worldIn.playSound(null, playerIn.getPosX(), playerIn.getPosY(),
                playerIn.getPosZ(), Spyglass.SPYGLASS_ZOOM_SOUND.get(), SoundCategory.PLAYERS, 1.0F,
                1.0F / (random.nextFloat() * 0.4F + 1.2F) * 1.2F);

        if (worldIn.isRemote) {
            zoomed = true;
            Minecraft.getInstance().gameSettings.smoothCamera = true;
        }

        playerIn.setActiveHand(handIn);

        if (worldIn.isRemote) {
            // Enchants
            if (U.isFirstPerson()) {
                if (U.hasEnchant(Spyglass.CURSE_OF_SECRETS_ENCHANTMENT.get())) {
                    U.loadCurseOfSecretsShader();
                }

                if (U.hasEnchant(Spyglass.SCOTOPIC_ENCHANTMENT.get())) {
                    originalGamma = Minecraft.getInstance().gameSettings.gamma;
                    Minecraft.getInstance().gameSettings.gamma = 69.420D;
                }
            }
        }

        return ActionResult.resultConsume(itemstack);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof PlayerEntity))
            return;

        if (worldIn.isRemote) {
            Minecraft mc = Minecraft.getInstance();

            zoomed = false;

            mc.gameSettings.smoothCamera = false;

            if (U.hasEnchant(Spyglass.SCOTOPIC_ENCHANTMENT.get()))
                mc.gameSettings.gamma = originalGamma;

            if (U.hasEnchant(Spyglass.CURSE_OF_SECRETS_ENCHANTMENT.get()))
                U.stopCurseOfSecretsShader();

            // this is probably very unsafe
            // My scuffed fix to stop frustum culling applying when it shouldn't
            // Basically just calling setDisplayListEntitiesDirty for ~200 ms
            // thanks to Paul Fulham#8350 for letting me know about the function
            Thread thread = new Thread(() -> {
                long end = System.currentTimeMillis() + 200;
                while (System.currentTimeMillis() < end) {
                    mc.worldRenderer.setDisplayListEntitiesDirty();
                }
            });
            thread.setName("SpyglassFrustumCullingFix");
            thread.start();
        }
    }
}
