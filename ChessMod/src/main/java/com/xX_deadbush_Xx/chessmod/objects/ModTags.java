package com.xX_deadbush_Xx.chessmod.objects;

import com.xX_deadbush_Xx.chessmod.ChessMod;

import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class ModTags {
	public static final Tag<Item> DARK_WOOD = makeItemWrapper("dark_wood");
	public static final Tag<Item> LIGHT_WOOD = makeItemWrapper("light_wood");

	private static Tag<Item> makeItemWrapper(String name) {
		return new ItemTags.Wrapper(new ResourceLocation(ChessMod.MOD_ID, name));
	}
}
