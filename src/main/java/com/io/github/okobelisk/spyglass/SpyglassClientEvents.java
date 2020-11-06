package com.io.github.okobelisk.spyglass;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@Mod.EventBusSubscriber(modid = Spyglass.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SpyglassClientEvents {

    private static boolean flagScotopic = true;
    private static boolean flagCurseOfSecrets = true;

    @SubscribeEvent
    public static void updateSpyglassStuff(TickEvent.PlayerTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.world == null)
            return;

        if (!mc.world.isRemote)
            return;

        if (mc.player == null)
            return;

        if (U.isSpyglassActive()) {
            SpyglassItem spyglassItem = (SpyglassItem) mc.player.getActiveItemStack().getItem();
            if (!spyglassItem.zoomed)
                return;

            // Update world so we don't get faulty frustum culling..
            mc.worldRenderer.setDisplayListEntitiesDirty();
            // Smooth camera only when in first person
            mc.gameSettings.smoothCamera = U.isFirstPerson();
            // Scotopic only when in first person
            if (flagScotopic) {
                if (U.hasEnchant(Spyglass.SCOTOPIC_ENCHANTMENT.get()) && U.isFirstPerson()) {
                    flagScotopic = false;
                    mc.gameSettings.gamma = 69.420D;
                }
            } else {
                if (U.hasEnchant(Spyglass.SCOTOPIC_ENCHANTMENT.get()) && !U.isFirstPerson()) {
                    flagScotopic = true;
                    mc.gameSettings.gamma = spyglassItem.originalGamma;
                }
            }

            // Curse of Secrets only when in first person
            if (flagCurseOfSecrets) {
                if (U.hasEnchant(Spyglass.CURSE_OF_SECRETS_ENCHANTMENT.get()) && U.isFirstPerson()) {
                    flagCurseOfSecrets = false;
                    U.loadCurseOfSecretsShader();
                }
            } else {
                if (U.hasEnchant(Spyglass.CURSE_OF_SECRETS_ENCHANTMENT.get()) && !U.isFirstPerson()) {
                    flagCurseOfSecrets = true;
                    U.stopCurseOfSecretsShader();
                }
            }
        }
    }

    @SubscribeEvent
    public static void updateFOV(FOVUpdateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.world == null)
            return;

        if (!mc.world.isRemote)
            return;

        if (mc.player == null)
            return;

        CompoundNBT nbt = mc.player.getActiveItemStack().getTag();
        if (nbt != null && U.isSpyglassActive() && U.isFirstPerson()) {
            event.setNewfov(event.getFov() - nbt.getFloat(U.zoom));
        }
    }

    // scrollDelta is 1.0D when scrolled up, -1.0D when scrolled down
    @SubscribeEvent
    public static void scroll(InputEvent.MouseScrollEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.world == null)
            return;

        if (!mc.world.isRemote)
            return;

        if (mc.player == null)
            return;

        CompoundNBT nbt = mc.player.getActiveItemStack().getTag();
        if (nbt != null && U.isSpyglassActive() && U.isFirstPerson()) {
            event.setCanceled(true); // cancel the input so we can't swap items

            // check to make sure we can't go over the limit
            // negative limit is 50% of getMagnifyAmount
            if (event.getScrollDelta() >= 0 && (nbt.getFloat(U.zoom) + U.STEP) <= U.getMaxZoom()) {
                nbt.putFloat(U.zoom, nbt.getFloat(U.zoom) + U.STEP);
                U.playClickSound();
            } else if ((event.getScrollDelta() < 0) && ((nbt.getFloat(U.zoom) - U.STEP) >= (U.getMaxZoom() * (50f / 100f)))) {
                nbt.putFloat(U.zoom, nbt.getFloat(U.zoom) - U.STEP);
                U.playClickSound();
            }
        }
    }

    @SubscribeEvent
    public static void renderSpyglassOverlay(RenderGameOverlayEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.world == null)
            return;

        if (!mc.world.isRemote)
            return;

        if (mc.player == null)
            return;

        if (U.isSpyglassActive() && U.isFirstPerson()) {

            if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR || event.getType() == RenderGameOverlayEvent.ElementType.HEALTHMOUNT || event.getType() == RenderGameOverlayEvent.ElementType.ARMOR || event.getType() == RenderGameOverlayEvent.ElementType.FOOD || event.getType() == RenderGameOverlayEvent.ElementType.HEALTH || event.getType() == RenderGameOverlayEvent.ElementType.BOSSHEALTH || event.getType() == RenderGameOverlayEvent.ElementType.EXPERIENCE || event.getType() == RenderGameOverlayEvent.ElementType.JUMPBAR || event.getType() == RenderGameOverlayEvent.ElementType.POTION_ICONS)
                event.setCanceled(true);

            if (event.getType() == RenderGameOverlayEvent.ElementType.HELMET && mc.player.inventory.armorItemInSlot(3).getItem() != Blocks.CARVED_PUMPKIN.asItem()) {
                // Copied from IngameGui#renderPumpkinOverlay
                // (I have no idea what's happening)
                int scaledHeight = event.getWindow().getScaledHeight();
                int scaledWidth = event.getWindow().getScaledWidth();

                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.defaultBlendFunc();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.disableAlphaTest();
                mc.getTextureManager().bindTexture(new ResourceLocation(Spyglass.MOD_ID + ":textures/misc/spyglass_overlay.png"));
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                bufferbuilder.pos(0.0D, scaledHeight, -90.0D).tex(0.0F, 1.0F).endVertex();
                bufferbuilder.pos(scaledWidth, scaledHeight, -90.0D).tex(1.0F, 1.0F).endVertex();
                bufferbuilder.pos(scaledWidth, 0.0D, -90.0D).tex(1.0F, 0.0F).endVertex();
                bufferbuilder.pos(0.0D, 0.0D, -90.0D).tex(0.0F, 0.0F).endVertex();
                tessellator.draw();
                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();
                RenderSystem.enableAlphaTest();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    @SubscribeEvent
    public static void hidePlayerHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.world == null)
            return;

        if (!mc.world.isRemote)
            return;

        if (mc.player == null)
            return;

        if (U.isSpyglassActive() && U.isFirstPerson())
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void keyInputEvent(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.world == null)
            return;

        if (!mc.world.isRemote)
            return;

        if (mc.player == null)
            return;

        if (U.isSpyglassActive()) {
            GameSettings g = mc.gameSettings;

            // Disable hotbar switching
            for (int i = 0; i < 9; ++i) {
                while (g.keyBindsHotbar[i].isPressed()) g.keyBindsHotbar[i].setPressed(false);
            }

            // Disable hand swapping (F key)
            while (g.keyBindSwapHands.isPressed()) g.keyBindSwapHands.setPressed(false);

            // Disable dropping item
            while (g.keyBindDrop.isPressed()) g.keyBindDrop.setPressed(false);

            // Disable advancements
            while (g.keyBindAdvancements.isPressed()) g.keyBindAdvancements.setPressed(false);

            // Disable toggling shader with curse of secrets
            if (event.getKey() == 293 && U.hasEnchant(Spyglass.CURSE_OF_SECRETS_ENCHANTMENT.get()))
                ObfuscationReflectionHelper.setPrivateValue(GameRenderer.class, mc.gameRenderer, true, U.useShader);
        }
    }
}
