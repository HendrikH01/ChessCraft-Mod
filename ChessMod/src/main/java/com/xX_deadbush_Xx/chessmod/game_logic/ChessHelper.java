package com.xX_deadbush_Xx.chessmod.game_logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.xX_deadbush_Xx.chessmod.client.ChessBoardScreen;
import com.xX_deadbush_Xx.chessmod.objects.ChessPiece;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class ChessHelper {

	public static final List<String> LETTERS = ImmutableList.of("a", "b", "c", "d", "e", "f", "g", "h"); 


	public static void executeMove(ChessBoardContainer container, int first, int second, String piece) {
		//detect castle, double pawn move and enpassant
		if(!container.getSlot(first).getHasStack()) return;

		ChessPiece moved = (ChessPiece) container.getSlot(first).getStack().getItem();
		ItemStack captured = container.getBoard().movePieceTo(container, first, second);
		container.getBoard().enPassantSquare = -1;
		
		if(moved.type == ChessPieceType.KING) {
			int d = getX(first) - getX(second);
			if(Math.abs(d) == 2) {
				
				//CASTLED
				if(d > 0) {
					//Queenside
					int rookpos = getY(first);
					container.getBoard().movePieceTo(container, rookpos, rookpos + 24);
				} else {
					//Kingside
					int rookpos = 56 + getY(first);
					container.getBoard().movePieceTo(container, rookpos, rookpos - 16);
				}
			}
			
			if(moved.color == PieceColor.WHITE) {
				container.getBoard().canCastle[0] = false;
				container.getBoard().canCastle[1] = false;
			} else {
				container.getBoard().canCastle[2] = false;
				container.getBoard().canCastle[3] = false;
			}
		} else if(moved.type == ChessPieceType.ROOK) {
			int x = getX(first);
			int y = getY(first);

			if(y == 7) {
				//white
				if(x == 0)
					container.getBoard().canCastle[0] = false;
				else if(x == 7)
					container.getBoard().canCastle[1] = false;
				
			} else if(y == 0) {
				//black
				if(x == 0)
					container.getBoard().canCastle[2] = false;
				else if(x == 7)
					container.getBoard().canCastle[3] = false;
			}
		} else if(moved.type == ChessPieceType.PAWN) {
			
			if(!piece.isEmpty()) {
				//bot promotion
				
				int id = -1;
				
				switch(piece) {
					case "q" : id = 4; break;
					case "r" : id = 3; break;
					case "b" : id = 2; break;
					case "n" : id = 1; break;
				}
				
				if(id > 0 && id < 5) {
					//valid
					PieceColor color = container.tile.challengerColor.getOpposite();
					ChessPieceType type = ChessPieceType.values()[id];
					
					if(container.tile.getInventory().getStackInSlot(color.ordinal() * 6 + type.ordinal()).isEmpty()) {
						//must wait
						container.tile.computerPromotionPiece = id;
						container.tile.promotionColor = Optional.of(color);
					} else {
						ItemStack promoted = container.tile.getInventory().getStackInSlot(color.ordinal() * 6 + type.ordinal()).copy();
						container.tile.getInventory().getStackInSlot(color.ordinal() * 6 + type.ordinal()).shrink(1);
						promoted.setCount(1);
						container.getBoard().setStackInSlot(second, promoted);
					}
				}
				
			} else {
				int d = getY(first) - getY(second);
				if(Math.abs(d) == 2) {
					container.getBoard().enPassantSquare = first - d/2;
				} else if(captured.isEmpty() && getX(first) - getX(second) != 0) {
					//enpassant capture
					container.getBoard().removePieceAt(container, getX(second), getY(first));
				}
			}
		}
		
		/*
		 * This is literally never going to change anything in a game but
		 * if a rook gets captured on its original square and the 
		 * other rook moves to that square before the king moves we have to prevent castling.
		 */
		if(!captured.isEmpty()) {
			if(((ChessPiece)captured.getItem()).type == ChessPieceType.ROOK) {
				int x = getX(second);
				int y = getY(second);

				if(y == 7) {
					//white
					if(x == 0)
						container.getBoard().canCastle[0] = false;
					else if(x == 7)
						container.getBoard().canCastle[1] = false;
					
				} else if(y == 0) {
					//black
					if(x == 0)
						container.getBoard().canCastle[2] = false;
					else if(x == 7)
						container.getBoard().canCastle[3] = false;
				}
			}
		}
	}
	
	public static void putPieceBack(ChessBoardContainer container, ItemStack stack) {
		if(stack.getItem() instanceof ChessPiece) {
			ChessPiece p = (ChessPiece)stack.getItem();
			int i = 64 + p.color.ordinal() * 6 + p.type.ordinal();
			
			ItemStack inslot = container.inventorySlots.get(i).getStack();
			
			if(inslot.isEmpty()) {
				container.inventorySlots.get(i).putStack(stack);
			} else {
				if(inslot.getCount() < 64 && ItemStack.areItemsEqual(stack, inslot) && ItemStack.areItemStackTagsEqual(stack, inslot)) {
					inslot.grow(1);
				} else {
					BlockPos pos = container.tile.getPos();
					ItemEntity itementity = new ItemEntity(container.tile.getWorld(), pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, stack);
			        container.tile.getWorld().addEntity(itementity);
				}
			}
		}
	}
	
	public static int getX(int slot) {
		return slot / 8;
	}
	
	public static int getY(int slot) {
		return slot % 8;
	}
	
	public static String convertLetterFormat(int x, int y) {
		if(x > 7 || y > 7) return "";
		
		return LETTERS.get(x) + (8 - y);
	}
	
	public static int convertNumberFormat(String coord) {
		int x = LETTERS.indexOf(coord.substring(0, 1));
		int y = 8 - Integer.parseInt(coord.substring(1, 2));
		
		return x * 8 + y;
	}
	
	public static String convertLetterFormat(int s) {
		return convertLetterFormat(getX(s), getY(s));
	}

	@SuppressWarnings("resource")
	public static void getLegalMoves(ChessBoardContainer container, PieceColor color, Consumer<List<Pair<Integer, Integer>>> callback) {
		
		if(!container.getBoard().validatePosition()) {
			if(container.tile.getWorld().isRemote) {
				if(Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof ChessBoardScreen) {
					((ChessBoardScreen)Minecraft.getInstance().currentScreen).displayInvalidPosError();
				}
			}
			
			return;
		}
		
		List<Pair<Integer, Integer>> list = new ArrayList<>();
		ChessEngineManager.getLegalMoves(container.getBoard().getFEN(color), (result) -> {
			for(String move : result) {
				String first = move.substring(0, 2);
				String second = move.substring(2, 4);
				
				list.add(Pair.of(convertNumberFormat(first), convertNumberFormat(second)));
			}
			callback.accept(list);
		});
	}
}
