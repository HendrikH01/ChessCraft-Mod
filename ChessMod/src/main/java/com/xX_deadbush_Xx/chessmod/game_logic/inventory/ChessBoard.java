package com.xX_deadbush_Xx.chessmod.game_logic.inventory;

import java.util.Arrays;

import javax.annotation.Nullable;

import com.xX_deadbush_Xx.chessmod.game_logic.Util;
import com.xX_deadbush_Xx.chessmod.objects.ChessPiece;

import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;

public class ChessBoard implements IItemHandler {

	protected ItemStack[] inv = new ItemStack[64];
	public boolean canCastleShort = true;
	public boolean canCastleLong = true;

	public ChessBoard() {
		Arrays.fill(this.inv, ItemStack.EMPTY);
	}
	
	@Override
	public int getSlots() {
		return 64;
	}

	/* * * * * * *
	 * 2 * 5 * 8 *
	 * * * * * * *
	 * 1 * 4 * 7 *
	 * * * * * * *
	 * 0 * 3 * 6 * 
	 * * * * * * *
	 */
	
	@Override
	public ItemStack getStackInSlot(int slot) {
		return this.inv[slot];
	}
	
	public ItemStack getPieceAt(int x, int y) {
		return this.inv[x * 8 + y];
	}
	
	/**
	 * move piece from one square to another, returns the taken piece. if out of bounds returns null.
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public @Nullable ItemStack movePieceTo(int x1, int y1, int x2, int y2) {
		if(withinBounds(x2, y2))
			return null;
		
		ItemStack taken = getPieceAt(x2, y2);
		placePieceAt(x2, y2, removePieceFrom(x1, y1));
		
		return taken;
	}
	
	public void placePieceAt(int x, int y, ItemStack piece) {
		piece.setCount(1);
		this.inv[x * 8 + y] = piece;
	}

	private ItemStack removePieceFrom(int x, int y) {
		ItemStack removed = this.inv[x * 8 + y];
		this.inv[x * 8 + y] = ItemStack.EMPTY;
		return removed;
	}

	public boolean withinBounds(int x, int y) {
		return x >= 0 && x < 8 && y >= 0 && y < 8;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return stack.getItem() instanceof ChessPiece;
	}
	
	//it's a chess board, we don't extract items from a chess board!
	
	@Deprecated
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if(!simulate) {
			this.inv[slot] = stack;
		}
		return ItemStack.EMPTY;
	}

	@Deprecated
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return ItemStack.EMPTY;
	}

	public void readCompound(CompoundNBT compound) {
		this.canCastleLong = compound.getBoolean("longcastle");
		this.canCastleShort = compound.getBoolean("shortcastle");
		NonNullList<ItemStack> list = NonNullList.withSize(64, ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(compound, list);
		for(int i = 0; i < list.size(); i++) {
			inv[i] = list.get(i);
		}
	}
	
	public CompoundNBT getCompound() {
		CompoundNBT compound = new CompoundNBT();
		compound.putBoolean("longcastle", this.canCastleLong);
		compound.putBoolean("shortcastle", this.canCastleShort);
		ItemStackHelper.saveAllItems(compound, Util.toNonNullList(this));

		return compound;
	}
}
