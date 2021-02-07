package com.xX_deadbush_Xx.chesscraft.objects;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ChessPiecesRecipe extends SpecialRecipe {
	
	private static final Ingredient E = Ingredient.EMPTY;
	private static final Ingredient DW = Ingredient.fromTag(ModTags.DARK_WOOD);
	private static final Ingredient LW = Ingredient.fromTag(ModTags.LIGHT_WOOD);
	private static final Ingredient G = Ingredient.fromStacks(new ItemStack(Items.GOLD_NUGGET));

	private static final List<Pair<List<Ingredient>, ItemStack>> BLACK_PIECES = ImmutableList.of(
			Pair.of(ImmutableList.of(E, E, E, E, DW, E, DW, DW, DW), new ItemStack(ModRegistry.BLACK_PAWN.get(), 4)),
			Pair.of(ImmutableList.of(E, DW, E, DW, DW, DW, E, E, E), new ItemStack(ModRegistry.BLACK_PAWN.get(), 4)),
			Pair.of(ImmutableList.of(E, DW, E, E, DW, E, DW, DW, DW), new ItemStack(ModRegistry.BLACK_BISHOP.get(), 2)),
			Pair.of(ImmutableList.of(DW, DW, E, E, DW, E, DW, DW, DW), new ItemStack(ModRegistry.BLACK_HORSEY.get(), 2)),
			Pair.of(ImmutableList.of(DW, DW, E, DW, DW, E, DW, DW, E), new ItemStack(ModRegistry.BLACK_ROOK.get(), 2)),
			Pair.of(ImmutableList.of(E, DW, DW, E, DW, DW, E, DW, DW), new ItemStack(ModRegistry.BLACK_ROOK.get(), 2)),
			Pair.of(ImmutableList.of(E, G, E, E, DW, E, DW, DW, DW), new ItemStack(ModRegistry.BLACK_QUEEN.get())),
			Pair.of(ImmutableList.of(DW, G, DW, E, DW, E, DW, DW, DW), new ItemStack(ModRegistry.BLACK_KING.get()))
		);
	
	private static final List<Pair<List<Ingredient>, ItemStack>> WHITE_PIECES = ImmutableList.of(
			Pair.of(ImmutableList.of(E, E, E, E, LW, E, LW, LW, LW), new ItemStack(ModRegistry.WHITE_PAWN.get(), 4)),
			Pair.of(ImmutableList.of(E, LW, E, LW, LW, LW, E, E, E), new ItemStack(ModRegistry.WHITE_PAWN.get(), 4)),
			Pair.of(ImmutableList.of(E, LW, E, E, LW, E, LW, LW, LW), new ItemStack(ModRegistry.WHITE_BISHOP.get(), 2)),
			Pair.of(ImmutableList.of(LW, LW, E, E, LW, E, LW, LW, LW), new ItemStack(ModRegistry.WHITE_HORSEY.get(), 2)),
			Pair.of(ImmutableList.of(LW, LW, E, LW, LW, E, LW, LW, E), new ItemStack(ModRegistry.WHITE_ROOK.get(), 2)),
			Pair.of(ImmutableList.of(E, LW, LW, E, LW, LW, E, LW, LW), new ItemStack(ModRegistry.WHITE_ROOK.get(), 2)),
			Pair.of(ImmutableList.of(E, G, E, E, LW, E, LW, LW, LW), new ItemStack(ModRegistry.WHITE_QUEEN.get())),
			Pair.of(ImmutableList.of(LW, G, LW, E, LW, E, LW, LW, LW), new ItemStack(ModRegistry.WHITE_KING.get()))
		);
	
	public ChessPiecesRecipe(ResourceLocation idIn) {
	      super(idIn);
	}

	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {
		boolean hasKnife = false;
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				if(inv.getStackInSlot(i*3 + j).getItem() == ModRegistry.CARVING_KNIFE.get()) {
					if(hasKnife)
						return false;
					else 
						hasKnife = true;
				}
			}
		}
		
		if(hasKnife) {
			
			//center always has wood
			Item center = inv.getStackInSlot(inv.getWidth() * inv.getHeight() / 2).getItem();

			recipes:
			for(Pair<List<Ingredient>, ItemStack> pair : ModTags.DARK_WOOD.contains(center) ? BLACK_PIECES : (ModTags.LIGHT_WOOD.contains(center) ? WHITE_PIECES : new ArrayList<Pair<List<Ingredient>, ItemStack>>())) {
				for(int i = 0; i < 9; i++) {
					Ingredient ingred = pair.getFirst().get(i);
					
					if(ingred != Ingredient.EMPTY) {
						if(!ingred.test(inv.getStackInSlot(i))) {
							continue recipes;
						}
						
					} else if (!inv.getStackInSlot(i).isEmpty() && inv.getStackInSlot(i).getItem() != ModRegistry.CARVING_KNIFE.get()) {
						continue recipes;
					}
				}
				
				return true;
			};
			
		} else return false;
		
		return false;
	}


	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
		boolean hasKnife = false;
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				if(inv.getStackInSlot(i*3 + j).getItem() == ModRegistry.CARVING_KNIFE.get()) {
					if(hasKnife)
						return ItemStack.EMPTY;
					else 
						hasKnife = true;
				}
			}
		}
		
		if(hasKnife) {
			//center always has wood
			Item center = inv.getStackInSlot(inv.getWidth() * inv.getHeight() / 2).getItem();

			recipes:
			for(Pair<List<Ingredient>, ItemStack> pair : ModTags.DARK_WOOD.contains(center) ? BLACK_PIECES : (ModTags.LIGHT_WOOD.contains(center) ? WHITE_PIECES : new ArrayList<Pair<List<Ingredient>, ItemStack>>())) {
					for(int i = 0; i < 9; i++) {
					Ingredient ingred = pair.getFirst().get(i);
					
					if(ingred != Ingredient.EMPTY) {
						if(!ingred.test(inv.getStackInSlot(i))) {
							continue recipes;
						}
						
					} else if (!inv.getStackInSlot(i).isEmpty() && inv.getStackInSlot(i).getItem() != ModRegistry.CARVING_KNIFE.get()) {
						continue recipes;
					}
				}
				
				return pair.getSecond().copy();
			};
		}
		
		return ItemStack.EMPTY;
	}
	
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
	      NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

	      for(int i = 0; i < nonnulllist.size(); ++i) {	
	         ItemStack item = inv.getStackInSlot(i).copy();
	         if (item.getItem() == ModRegistry.CARVING_KNIFE.get()) {
	        	 int d = item.getDamage() + 1;
	        	 
	        	 if(d < item.getMaxDamage()) {
	        		 item.setDamage(d);
	        		 nonnulllist.set(i, item);
	        	 }
	         }
	      }

	      return nonnulllist;
	}
	
	@Override
	public boolean canFit(int width, int height) {
	     return width >= 3 && height >= 3;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return ModRecipes.CHESS_PIECES_SERIALIZER.get();
	}
}
