package com.xX_deadbush_Xx.chessmod.game_logic.inventory;

import java.util.Optional;
import java.util.function.Supplier;

import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.game_logic.Util;

import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class ChessBoardSquareSlot extends ToggleableSlot {
	
	public Optional<HighlightMode> highlight = Optional.empty();
	
	public ChessBoardSquareSlot(ChessBoard itemHandler, int index, int xPosition, int yPosition, Supplier<Boolean> enabled) {
		super(itemHandler, index, xPosition, yPosition, enabled);
	}
	
	public ItemStack insertStack(ItemStack stack) {
		if(stack.getCount() == 1)
			putStack(stack.copy());
		else {
			ItemStack newStack = stack.copy();
			newStack.setCount(1);
			putStack(newStack);
			stack.shrink(1);
		}
		
		return stack;
	}
	
	@Deprecated
	@Override
	public void putStack(ItemStack stack) {
		this.getItemHandler().insertItem(this.slotNumber, stack, false);
	}

	public void clear(ChessBoardContainer chessBoardContainer) {
		if(!this.getHasStack()) return;
		
		Slot slot = chessBoardContainer.inventorySlots.get(Util.getStorageSlotIndex(getStack()));
		if(slot.getHasStack()) {
			slot.decrStackSize(-1);
		} else slot.putStack(getStack());
		
		this.getItemHandler().insertItem(this.slotNumber, ItemStack.EMPTY, false);
	}
	
	public static enum HighlightMode {
		SELECTED(0x000000),
		VISIBLE(0x000000),
		ATTACKING(0x000000),
		IN_CHECK(0x000000);

		public final int color;

		HighlightMode(int color) {
			this.color = color;
		}
	}
}
