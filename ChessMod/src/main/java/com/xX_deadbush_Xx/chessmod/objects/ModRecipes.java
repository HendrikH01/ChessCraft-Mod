package com.xX_deadbush_Xx.chessmod.objects;

import com.xX_deadbush_Xx.chessmod.ChessMod;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModRecipes {
	public static final DeferredRegister<IRecipeSerializer<?>> SERIALIZERS = new DeferredRegister<>(ForgeRegistries.RECIPE_SERIALIZERS, ChessMod.MOD_ID);


	//SPECIAL CRAFTING
	public static final RegistryObject<SpecialRecipeSerializer<ChessPieceRecipe>> BLOOD_VIAL_SERIALIZER = SERIALIZERS
			.register("crafting_blood_vial", () -> new SpecialRecipeSerializer<>(ChessPieceRecipe::new));

}
