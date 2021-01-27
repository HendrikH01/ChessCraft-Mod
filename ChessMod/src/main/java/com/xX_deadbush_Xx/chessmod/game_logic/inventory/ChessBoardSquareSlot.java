package com.xX_deadbush_Xx.chessmod.game_logic.inventory;

import java.util.Optional;
import java.util.function.Supplier;

import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessHelper;

import net.minecraft.item.ItemStack;

public class ChessBoardSquareSlot extends ToggleableSlot {
	
	public Optional<HighlightMode> highlight = Optional.empty();
	
	public ChessBoardSquareSlot(ChessBoardContainer container, int index, int xPosition, int yPosition, Supplier<Boolean> enabled) {
		super(container.getBoard(), index, xPosition, yPosition, enabled);
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

	public void clear(ChessBoardContainer container) {
		if(!this.getHasStack()) return;
		
		ChessHelper.putPieceBack(container, getStack());
		putStack(ItemStack.EMPTY);
	}
	
	public void mark(HighlightMode mode) {
		this.highlight = Optional.of(mode);
	}
	
	public static enum HighlightMode {
		MOVED(0x8fE5D400),
		VISIBLE(0x00000000),
		ATTACKING(0xffE87F45),
		IN_CHECK(0xffD85D5E), 
		HINT(0x8F17E4B5);

		public final int color;

		HighlightMode(int color) {
			this.color = color;
		}
	}
}
