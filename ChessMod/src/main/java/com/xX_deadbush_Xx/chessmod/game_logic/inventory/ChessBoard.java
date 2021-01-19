package com.xX_deadbush_Xx.chessmod.game_logic.inventory;

import java.util.Arrays;

import javax.annotation.Nullable;

import com.xX_deadbush_Xx.chessmod.game_logic.ChessBoardContainer;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessHelper;
import com.xX_deadbush_Xx.chessmod.game_logic.ChessPieceType;
import com.xX_deadbush_Xx.chessmod.game_logic.PieceColor;
import com.xX_deadbush_Xx.chessmod.game_logic.Util;
import com.xX_deadbush_Xx.chessmod.objects.ChessPiece;

import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;

public class ChessBoard implements IItemHandler {

	protected ItemStack[] inv = new ItemStack[64];
	public int enPassantSquare = -1;
	public boolean[] canCastle = new boolean[] {true, true, true, true};
	public PieceColor toPlay = PieceColor.WHITE;
	
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
	
	public ItemStack setStackInSlot(int slot, ItemStack stack) {
		return this.inv[slot] = stack;
	}
	
	public ItemStack getPieceAt(int x, int y) {
		return this.inv[x * 8 + y];
	}
	
	public ChessBoard copy() {
		ChessBoard c = new ChessBoard();
		c.inv = Arrays.copyOf(this.inv, 64);
		c.canCastle = Arrays.copyOf(this.canCastle, 4);
		c.enPassantSquare = this.enPassantSquare;
		c.toPlay = this.toPlay;
		
		return c;
	}
	
	/**
	 * move piece from one square to another, returns the taken piece. if out of bounds returns null.
	 * @param container 
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public @Nullable ItemStack movePieceTo(ChessBoardContainer container, int first, int second) {
		if(!withinBounds(second))
			return null;
		
		ItemStack taken = removePieceAt(container, ChessHelper.getX(second), ChessHelper.getY(second));
		placePieceAt(second, removePieceFrom(first));
				
		return taken;
	}
	
	private void placePieceAt(int pos, ItemStack piece) {
		piece.setCount(1);
		this.inv[pos] = piece;
	}

	private ItemStack removePieceFrom(int pos) {
		ItemStack stack = inv[pos];
		inv[pos] = ItemStack.EMPTY;
		return stack;
	}

	public void placePieceAt(int x, int y, ItemStack piece) {
		piece.setCount(1);
		this.inv[x * 8 + y] = piece;
	}

	
	public boolean withinBounds(int pos) {
		return pos >= 0 && pos < 64;
	}
	
	public boolean withinBounds(int x, int y) {
		return x >= 0 && y >= 0 && x < 8 && y < 8;
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
		if(compound.contains("wlongcastle")) this.canCastle[0] = compound.getBoolean("wlongcastle");
		if(compound.contains("wshortcastle")) this.canCastle[1] = compound.getBoolean("wshortcastle");
		if(compound.contains("bshortcastle")) this.canCastle[2] = compound.getBoolean("blongcastle");
		if(compound.contains("bshortcastle")) this.canCastle[3] = compound.getBoolean("bshortcastle");
		if(compound.contains("color")) this.toPlay= compound.getBoolean("color") ? PieceColor.BLACK : PieceColor.WHITE;

		if(compound.contains("enpassant")) this.enPassantSquare = compound.getInt("enpassant");

		NonNullList<ItemStack> list = NonNullList.withSize(64, ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(compound, list);
		for(int i = 0; i < list.size(); i++) {
			inv[i] = list.get(i);
		}
	}
	
	public CompoundNBT getCompound() {
		CompoundNBT compound = new CompoundNBT();
		compound.putBoolean("wlongcastle", this.canCastle[0]);
		compound.putBoolean("wshortcastle", this.canCastle[1]);
		compound.putBoolean("wblongcastle", this.canCastle[2]);
		compound.putBoolean("bshortcastle", this.canCastle[3]);
		compound.putBoolean("color", this.toPlay == PieceColor.BLACK);

		compound.putInt("enpassant", this.enPassantSquare);
		
		ItemStackHelper.saveAllItems(compound, Util.toNonNullList(this));

		return compound;
	}
	
	public int getKingPos(PieceColor color) {
		int kingpos = -1;

		for(int i = 0; i < 64; i++) {
			if(inv[i].isEmpty()) continue;
			if(((ChessPiece)inv[i].getItem()).type == ChessPieceType.KING && ((ChessPiece)inv[i].getItem()).color == color) {
				kingpos = i;
				break;
			}
		}
		
		return kingpos;
	}
	
	public boolean validatePosition() {
		int kposblack = -1;
		int kposwhite = -1;
		
		for(int i = 0; i < 64; i++) {
			if(inv[i].isEmpty()) continue;
			if(((ChessPiece)inv[i].getItem()).type == ChessPieceType.KING) {
				if(((ChessPiece)inv[i].getItem()).color == PieceColor.WHITE) {
					if(kposwhite != -1) return false;
					else kposwhite = i;
				} else {
					if(kposblack != -1) return false;
					else kposblack = i;
				}
			}
		}
		
		if(kposblack == -1 || kposwhite == -1)
			return false;
		
		if(isInCheck(toPlay.getOpposite())) {
			return false;
		}
		
		if(kposblack != 32) {
			this.canCastle[3] = false;
			this.canCastle[2] = false;
		} else {
			if(inv[0].isEmpty() || ((ChessPiece)inv[0].getItem()).type != ChessPieceType.ROOK || ((ChessPiece)inv[0].getItem()).color != PieceColor.BLACK) {
				this.canCastle[2] = false;
			}
			
			if(inv[56].isEmpty() || ((ChessPiece)inv[56].getItem()).type != ChessPieceType.ROOK || ((ChessPiece)inv[56].getItem()).color != PieceColor.BLACK) {
				this.canCastle[3] = false;
			}
		}
		
		if(kposwhite != 39) {
			this.canCastle[0] = false;
			this.canCastle[1] = false;
		} else {
			if(inv[7].isEmpty() || ((ChessPiece)inv[7].getItem()).type != ChessPieceType.ROOK || ((ChessPiece)inv[7].getItem()).color != PieceColor.WHITE) {
				this.canCastle[0] = false;
			}
			
			if(inv[63].isEmpty() || ((ChessPiece)inv[63].getItem()).type != ChessPieceType.ROOK || ((ChessPiece)inv[63].getItem()).color != PieceColor.WHITE) {
				this.canCastle[1] = false;
			}
		}
		
		return true;
	}
	
	public boolean isInCheck(PieceColor color) {
		int kingpos = getKingPos(color);
		
		if(kingpos != -1) {
			int x = ChessHelper.getX(kingpos);
			int y = ChessHelper.getY(kingpos);
			//STRAIGHT
			for(int i = x - 1; i >= 0; i--) {
				ItemStack stack = getPieceAt(i, y);
				if(stack.isEmpty()) continue;
				else if(((ChessPiece)stack.getItem()).type.canMoveStraight() && ((ChessPiece)stack.getItem()).color != color) return true; 
				else break;
			}

			for(int i = y - 1; i >= 0; i--) {
				ItemStack stack = getPieceAt(x, i);
				if(stack.isEmpty()) continue;
				else if(((ChessPiece)stack.getItem()).type.canMoveStraight() && ((ChessPiece)stack.getItem()).color != color) return true; 
				else break;
			}
			
			for(int i = x + 1; i < 8; i++) {
				ItemStack stack = getPieceAt(i, y);
				if(stack.isEmpty()) continue;
				else if(((ChessPiece)stack.getItem()).type.canMoveStraight() && ((ChessPiece)stack.getItem()).color != color) return true; 
				else break;
			}
			
			for(int i = y + 1; i < 8; i++) {
				ItemStack stack = getPieceAt(x, i);
				if(stack.isEmpty()) continue;
				else if(((ChessPiece)stack.getItem()).type.canMoveStraight() && ((ChessPiece)stack.getItem()).color != color) return true; 
				else break;
			}
			
			//DIAGONAL
			for(int i = -1; x + i >= 0 && y + i >= 0; i--) {
				ItemStack stack = getPieceAt(x + i, y + i);
				if(stack.isEmpty()) continue;
				else if(((ChessPiece)stack.getItem()).type.canMoveDiagonaly() && ((ChessPiece)stack.getItem()).color != color) return true; 
				else break;
			}
			
			for(int i = -1; x + i >= 0 && y - i < 8; i--)  {
				ItemStack stack = getPieceAt(x + i, y - i);
				if(stack.isEmpty()) continue;
				else if(((ChessPiece)stack.getItem()).type.canMoveDiagonaly() && ((ChessPiece)stack.getItem()).color != color) return true; 
				else break;
			}
			
			for(int i = -1; x - i < 8 && y + i >= 0; i--) {
				ItemStack stack = getPieceAt(x - i, y + i);
				if(stack.isEmpty()) continue;
				else if(((ChessPiece)stack.getItem()).type.canMoveDiagonaly() && ((ChessPiece)stack.getItem()).color != color) return true; 
				else break;
			}
			
			for(int i = -1; x - i < 8 && y - i < 8; i--)  {
				ItemStack stack = getPieceAt(x - i, y - i);
				if(stack.isEmpty()) continue;
				else if(((ChessPiece)stack.getItem()).type.canMoveDiagonaly() && ((ChessPiece)stack.getItem()).color != color) return true; 
				else break;
			}
			//KNIGHT
			for(int i = -2; i <= 2; i+=4) {
				for(int j = -2; j <= 2; j+=4) {
						if(withinBounds(x + i, y + j/2)) {
							ItemStack stack = getPieceAt(x + i, y + j/2);
							if(!stack.isEmpty() && ((ChessPiece)stack.getItem()).color != color && ((ChessPiece)stack.getItem()).type == ChessPieceType.HORSEY) return true; 
						}
					
						if(withinBounds(x + i/2, y + j)) {
							ItemStack stack = getPieceAt(x + i/2, y + j);
							if(!stack.isEmpty() && 	((ChessPiece)stack.getItem()).color != color && ((ChessPiece)stack.getItem()).type == ChessPieceType.HORSEY) return true; 
						}
				}
			}
			//PAWN
			int dy = color == PieceColor.WHITE ? -1 : 1;
			if(withinBounds(x + 1, y + dy)) {
				ItemStack stack = getPieceAt(x + 1, y + dy);			
				if(!stack.isEmpty() && ((ChessPiece)stack.getItem()).type == ChessPieceType.PAWN && ((ChessPiece)stack.getItem()).color != color) 
					return true; 
			}
			
			if(withinBounds(x - 1, y + dy)) {
				ItemStack stack = getPieceAt(x - 1, y + dy);			
				if(!stack.isEmpty() && ((ChessPiece)stack.getItem()).type == ChessPieceType.PAWN && ((ChessPiece)stack.getItem()).color != color) 
					return true; 
			}
		}
		return false;
	}
	
	public ChessPieceType getType(int x, int y) {
		return ((ChessPiece)getPieceAt(x, y).getItem()).type;
	}
	
	public String getFEN(PieceColor toPlay) {
		int empty = 0;
		String fen = "";
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				ItemStack stack = inv[i + j * 8];

				if(stack.isEmpty()) {
					empty++;
				} else {
					if(empty > 0) {
						fen += empty;
						empty = 0;
					}
					
					fen += ((ChessPiece)stack.getItem()).type.getFENid(((ChessPiece)stack.getItem()).color);
				}
			}
			
			if(i != 7) {
				if(empty > 0) {
					fen += empty;
					empty = 0;
				}
				fen += "/";
			} else if(empty > 0) {
				fen += empty;
			}
		}
		
		fen += " " + toPlay.getFENid() + " ";
		
		
		if(this.canCastle[0]) fen += "Q";
		if(this.canCastle[1]) fen += "K";
		if(this.canCastle[2]) fen += "q";
		if(this.canCastle[3]) fen += "k";
		
		if(!this.canCastle[0] && !this.canCastle[1] && !this.canCastle[2] && !this.canCastle[3]) fen += "- ";
		else fen += " ";
		
		if(this.enPassantSquare != -1) fen += ChessHelper.convertLetterFormat(this.enPassantSquare) + " ";
		else fen += "- ";
		return fen;
	}

	public PieceColor getCurrentColor() {
		return this.toPlay ;
	}

	public ItemStack removePieceAt(ChessBoardContainer container, int x, int y) {
			if(!withinBounds(x, y))
				return null;
			
			ItemStack captured = inv[8 * x + y];
			inv[8 * x + y] = ItemStack.EMPTY;
			
			ChessHelper.putPieceBack(container, captured);
			return captured;
	}
}
