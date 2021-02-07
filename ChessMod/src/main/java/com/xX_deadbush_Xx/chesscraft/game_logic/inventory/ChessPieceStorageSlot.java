package com.xX_deadbush_Xx.chesscraft.game_logic.inventory;

import java.util.function.Supplier;

import com.xX_deadbush_Xx.chesscraft.game_logic.ChessPieceType;
import com.xX_deadbush_Xx.chesscraft.game_logic.PieceColor;
import com.xX_deadbush_Xx.chesscraft.objects.ChessPiece;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ChessPieceStorageSlot extends ToggleableSlot {
	
	public final ChessPieceType type;
	public final PieceColor color;
	
	public int inGame = 0; 
	
	public ChessPieceStorageSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, ChessPieceType type, PieceColor color, Supplier<Boolean> enabled) {
		super(itemHandler, index, xPosition, yPosition, enabled);
		this.type = type;
		this.color = color;
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		if(stack.getItem() instanceof ChessPiece) {
			ChessPiece piece = (ChessPiece) stack.getItem();
			return piece.type == type && piece.color == color;
		}
		
		return false;
	}
}
