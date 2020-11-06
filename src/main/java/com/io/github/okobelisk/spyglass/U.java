package com.io.github.okobelisk.spyglass;

import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.Random;

public class U {

    // Spyglass step for scrolling in / out
    public static final float STEP = .1f;

    // QoL 4 me
    public static final String zoom = "zoom";

    // Curse of Secrets
    public static final ResourceLocation[] SHADERS = new ResourceLocation[]{new ResourceLocation("shaders/post/notch.json"), new ResourceLocation("shaders/post/fxaa.json"), new ResourceLocation("shaders/post/art.json"), new ResourceLocation("shaders/post/bumpy.json"), new ResourceLocation("shaders/post/blobs2.json"), new ResourceLocation("shaders/post/pencil.json"), new ResourceLocation("shaders/post/color_convolve.json"), new ResourceLocation("shaders/post/deconverge.json"), new ResourceLocation("shaders/post/flip.json"), new ResourceLocation("shaders/post/invert.json"), new ResourceLocation("shaders/post/ntsc.json"), new ResourceLocation("shaders/post/outline.json"), new ResourceLocation("shaders/post/phosphor.json"), new ResourceLocation("shaders/post/scan_pincushion.json"), new ResourceLocation("shaders/post/sobel.json"), new ResourceLocation("shaders/post/bits.json"), new ResourceLocation("shaders/post/desaturate.json"), new ResourceLocation("shaders/post/green.json"), new ResourceLocation("shaders/post/blur.json"), new ResourceLocation("shaders/post/wobble.json"), new ResourceLocation("shaders/post/blobs.json"), new ResourceLocation("shaders/post/antialias.json"), new ResourceLocation("shaders/post/creeper.json"), new ResourceLocation("shaders/post/spider.json")};

    // GameRenderer#useShader
    public static final String useShader = "field_175083_ad";

    public static boolean isFirstPerson() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.world != null && mc.world.isRemote && mc.player != null)
            return mc.gameSettings.thirdPersonView == 0;
        else
            return false;
    }

    public static void playClickSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.world != null && mc.world.isRemote && mc.player != null)
            mc.player.playSound(Spyglass.SPYGLASS_CLICK_SOUND.get(), SoundCategory.PLAYERS, 1.0F, 1.0F / ((float) Math.random() * 0.4F + 1.5F) * 1.3F);
    }

    public static boolean hasEnchant(Enchantment enchantment) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.world != null && mc.world.isRemote && mc.player != null)
            return EnchantmentHelper.getEnchantmentLevel(enchantment, mc.player.getActiveItemStack()) > 0;
        else
            return false;
    }

    public static float getMaxZoom() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.world != null && mc.world.isRemote && mc.player != null)
            return 0.8f +
                    (EnchantmentHelper.getEnchantmentLevel(Spyglass.MAGNIFY_ENCHANTMENT.get(), mc.player.getActiveItemStack())
                            / 10f);
        else
            return 0.8f;
    }

    public static boolean isSpyglassActive() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.world != null && mc.world.isRemote && mc.player != null)
            return mc.player.getActiveItemStack().getItem() == Spyglass.SPYGLASS.get();
        else
            return false;
    }

    public static void loadCurseOfSecretsShader() {
        try {
            Minecraft mc = Minecraft.getInstance();
            Random random = new Random();
            // don't remove pl0x
            //String[] shaders = {"pencil", "blobs2", "flip", "bits", "notch", "blur", "sobel"};
            //mc.gameRenderer.loadShader(new ResourceLocation("shaders/post/" + shaders[random.nextInt(shaders.length)] + ".json"));

            if(FMLEnvironment.dist == Dist.CLIENT)
                mc.gameRenderer.loadShader(U.SHADERS[random.nextInt(U.SHADERS.length)]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopCurseOfSecretsShader() {
        try {
            if(FMLEnvironment.dist == Dist.CLIENT)
                Minecraft.getInstance().gameRenderer.stopUseShader();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
