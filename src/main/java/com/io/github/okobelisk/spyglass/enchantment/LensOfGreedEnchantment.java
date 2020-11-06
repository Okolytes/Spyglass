package com.io.github.okobelisk.spyglass.enchantment;

import com.io.github.okobelisk.spyglass.Spyglass;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

public class LensOfGreedEnchantment extends Enchantment {

    // Setting the enchantment type to digger so the enchanted book gets placed in the Tools creative tab
    public LensOfGreedEnchantment() {
        super(Rarity.COMMON, EnchantmentType.DIGGER, new EquipmentSlotType[]{EquipmentSlotType.MAINHAND});
    }

    @Override
    public boolean canApply(ItemStack itemStack){
        return itemStack.getItem() == Spyglass.SPYGLASS.get();
    }


}