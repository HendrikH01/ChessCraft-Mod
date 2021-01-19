package com.xX_deadbush_Xx.chessmod.game_logic;

import com.xX_deadbush_Xx.chessmod.objects.ChessPiece;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;

public class Util {
	@SuppressWarnings("unchecked")
	public static <T extends TileEntity> T getTileEntity(Class<T> clazz, final PlayerInventory playerInventory, final PacketBuffer data) {
		final TileEntity tileAtPos = playerInventory.player.world.getTileEntity(data.readBlockPos());
		if (clazz.isInstance(tileAtPos)) {
			return (T) tileAtPos;
		}
		throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
	}
	
	public static NonNullList<ItemStack> toNonNullList(IItemHandler inv) {
		NonNullList<ItemStack> out = NonNullList.create();
		for(int i = 0; i < inv.getSlots(); i++)  
			out.add(inv.getStackInSlot(i));
		
		return out;
	}
	
	public static int getStorageSlotIndex(ItemStack stack) {
		if(stack.getItem() instanceof ChessPiece) {
			ChessPiece piece = (ChessPiece)stack.getItem();
			return 64 + piece.type.ordinal() + piece.color.ordinal() * 6;
		} else return -1;
	}	
}
