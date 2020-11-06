package com.io.github.okobelisk.spyglass;

import com.io.github.okobelisk.spyglass.enchantment.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Spyglass.MOD_ID)
public class Spyglass {

    public static final String MOD_ID = "spyglass";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    // Registry
    private static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = new DeferredRegister<>(ForgeRegistries.SOUND_EVENTS, MOD_ID);
    private static final DeferredRegister<Enchantment> ENCHANTMENTS = new DeferredRegister<>(ForgeRegistries.ENCHANTMENTS, MOD_ID);

    public static final RegistryObject<Item> SPYGLASS = ITEMS.register("spyglass", SpyglassItem::new);

    public static final RegistryObject<SoundEvent> SPYGLASS_ZOOM_SOUND = SOUNDS.register("item.spyglass.zoom", () -> new SoundEvent(new ResourceLocation(MOD_ID, "item.spyglass.zoom")));
    public static final RegistryObject<SoundEvent> SPYGLASS_CLICK_SOUND = SOUNDS.register("item.spyglass.click", () -> new SoundEvent(new ResourceLocation(MOD_ID, "item.spyglass.click")));
    public static final RegistryObject<SoundEvent> SPYGLASS_FLARING_SOUND = SOUNDS.register("item.spyglass.flaring", () -> new SoundEvent(new ResourceLocation(MOD_ID, "item.spyglass.flaring")));

    public static final RegistryObject<Enchantment> MAGNIFY_ENCHANTMENT = ENCHANTMENTS.register("magnify", MagnifyEnchantment::new);
    public static final RegistryObject<Enchantment> SPECTRAL_LENS_ENCHANTMENT = ENCHANTMENTS.register("spectral_lens", SpectralLensEnchantment::new);
    public static final RegistryObject<Enchantment> LENS_OF_GREED_ENCHANTMENT = ENCHANTMENTS.register("lens_of_greed", LensOfGreedEnchantment::new);
    public static final RegistryObject<Enchantment> SCOTOPIC_ENCHANTMENT = ENCHANTMENTS.register("scotopic", ScotopicEnchantment::new);
    public static final RegistryObject<Enchantment> CURSE_OF_SECRETS_ENCHANTMENT = ENCHANTMENTS.register("curse_of_secrets", CurseOfSecretsEnchantment::new);
    public static final RegistryObject<Enchantment> CURSE_OF_KLUTZ_ENCHANTMENT = ENCHANTMENTS.register("curse_of_klutz", CurseOfSecretsEnchantment::new);
    public static final RegistryObject<Enchantment> CURSE_OF_FLARING_ENCHANTMENT = ENCHANTMENTS.register("curse_of_flaring", CurseOfFlaringEnchantment::new);

    public Spyglass() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        SOUNDS.register(bus);
        ENCHANTMENTS.register(bus);

        MinecraftForge.EVENT_BUS.register(new SpyglassEvents());
    }
}
