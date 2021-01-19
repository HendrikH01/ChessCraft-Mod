package com.xX_deadbush_Xx.chessmod.objects;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ChessPieceRecipe extends SpecialRecipe {

	public ChessPieceRecipe(ResourceLocation idIn) {
	      super(idIn);
	   }

	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean canFit(int width, int height) {
	     return width * height >= 4;
	}

	@Override
	public IRecipeSerializer getSerializer() {
		return ModRecipes.BLOOD_VIAL_SERIALIZER.get();
	}
}
