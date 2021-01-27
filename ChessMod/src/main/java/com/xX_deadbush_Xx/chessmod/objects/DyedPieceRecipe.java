package com.xX_deadbush_Xx.chessmod.objects;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class DyedPieceRecipe extends SpecialRecipe {

	public DyedPieceRecipe(ResourceLocation idIn) {
	      super(idIn);
	   }

	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {
	      ItemStack piece = ItemStack.EMPTY;
	      DyeColor color = null;
	      int piececount = 0;
	      
	      for(int i = 0; i < inv.getSizeInventory(); ++i) {
	         ItemStack inslot = inv.getStackInSlot(i);
	         if (!inslot.isEmpty()) {
	            if (inslot.getItem() instanceof DyeItem) {
	               if (color != null) {
	                  return false;
	               }

	               color = ((DyeItem)inslot.getItem()).getDyeColor();
	            } else {
	               if (!(inslot.getItem() instanceof ChessPiece) || piececount >= 4) {
	                  return false;
	               }

	               if(piece.isEmpty()) {
	            	   piece = inslot;
	               }
	               
	               piececount++;
	            }
	         }
	      }

	      return !piece.isEmpty() && piececount == 4 && color != null;
	}



	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
	      ItemStack piece = ItemStack.EMPTY;
	      DyeColor color = null;
	      
	      for(int i = 0; i < inv.getSizeInventory(); ++i) {
	         ItemStack inslot = inv.getStackInSlot(i);
	         if (!inslot.isEmpty()) {
	            if (inslot.getItem() instanceof DyeItem) {
	               if (color != null) {
	                  return ItemStack.EMPTY;
	               }

	               color = ((DyeItem)inslot.getItem()).getDyeColor();
	            } else {
	               if (!(inslot.getItem() instanceof ChessPiece)) {
	                  return ItemStack.EMPTY;
	               }

	               if(piece.isEmpty()) {
	            	   piece = inslot;
 	               } else if(color != null) break;
	            }
	         }
	      }

	      return !piece.isEmpty() && color != null ? ChessPiece.putColorOnPiece(piece, color) : ItemStack.EMPTY;
	}
	
	@Override
	public boolean canFit(int width, int height) {
	     return true;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return ModRecipes.DYED_PIECES_SERIALIZER.get();
	}
}
