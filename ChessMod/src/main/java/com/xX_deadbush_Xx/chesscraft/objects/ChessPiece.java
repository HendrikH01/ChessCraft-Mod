package com.xX_deadbush_Xx.chesscraft.objects;

import java.util.List;

import com.xX_deadbush_Xx.chesscraft.game_logic.ChessPieceType;
import com.xX_deadbush_Xx.chesscraft.game_logic.PieceColor;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class ChessPiece extends Item {
	
	public final ChessPieceType type;
	public final PieceColor color;

	public ChessPiece(ChessPieceType type, PieceColor color,  Properties properties) {
		super(properties);
		this.type = type;
		this.color = color;
	}

	public static int getColorFromStack(ItemStack stack) {
		if (!(stack.getItem() instanceof ChessPiece))
			return -1;

		CompoundNBT nbt = stack.getOrCreateTag();
		if (nbt.contains("color")) {
			return DyeColor.byId(nbt.getInt("color")).getColorValue();
		} else
			return -1;
	}
	
	public static int getColorIndex(ItemStack stack) {
		CompoundNBT nbt = stack.getOrCreateTag();
		if (nbt.contains("color")) {
			return nbt.getInt("color");
		} else
			return -1;
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		int colorIndex = getColorIndex(stack);
		tooltip.add(new StringTextComponent("§6§lPiece Type: §r§b" + this.type.toString()));
		tooltip.add(new StringTextComponent("§6§lPlaying Color: §r§b" + this.color.toString()));
		tooltip.add(new StringTextComponent("§6§lDye Color: §r§b" + (colorIndex >= 0 ? DyeColor.byId(colorIndex).toString().toUpperCase() : "NONE")));

	}
	
	public static ItemStack putColorOnPiece(ItemStack stack, DyeColor color) {
		stack.getOrCreateTag().putInt("color", color.getId());
		return stack;
	}

	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.isInGroup(group)) {
			
			//no color
			items.add(new ItemStack(this));
			
			for (DyeColor color : DyeColor.values()) {
				items.add(putColorOnPiece(new ItemStack(this), color));
			}
		}
	}
}
