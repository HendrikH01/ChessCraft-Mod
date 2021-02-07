package com.xX_deadbush_Xx.chesscraft.objects;

import com.xX_deadbush_Xx.chesscraft.ChessMod;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModRecipes {
	public static final DeferredRegister<IRecipeSerializer<?>> RECIPES = new DeferredRegister<>(ForgeRegistries.RECIPE_SERIALIZERS, ChessMod.MOD_ID);

	//SPECIAL CRAFTING
	public static final RegistryObject<SpecialRecipeSerializer<DyedPieceRecipe>> DYED_PIECES_SERIALIZER = RECIPES
			.register("dyed_pieces", () -> new SpecialRecipeSerializer<>(DyedPieceRecipe::new));
	public static final RegistryObject<SpecialRecipeSerializer<ChessPiecesRecipe>> CHESS_PIECES_SERIALIZER = RECIPES
			.register("chess_pieces", () -> new SpecialRecipeSerializer<>(ChessPiecesRecipe::new));
}
