package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.CobblemonItems;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PiglinEntity.class)
public class PiglinEntityMixin  {
    @Redirect(method = "equipToOffHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    public boolean cobblemon$isValidBarteringItem(ItemStack stack, Item item) {
        //TODO: Make a tag?
        return stack.isOf(item) || stack.isOf(CobblemonItems.RELIC_COIN_POUCH);
    }
}
