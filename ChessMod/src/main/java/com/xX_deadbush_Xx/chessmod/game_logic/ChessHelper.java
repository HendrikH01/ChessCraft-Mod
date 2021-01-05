package com.xX_deadbush_Xx.chessmod.game_logic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.xX_deadbush_Xx.chessmod.game_logic.inventory.ChessBoard;
import com.xX_deadbush_Xx.chessmod.objects.ChessPiece;

import net.minecraft.item.ItemStack;

public class ChessHelper {

	public static final List<String> LETTERS = ImmutableList.of("a", "b", "c", "d", "e", "f", "g", "h"); 
	
	public static void executeMove(ChessBoardContainer container, int first, int second, @Nullable Runnable callback) {
		getLegalMoves(container.getBoard(), moves -> {
			try {
				if(moves.contains(Pair.of(first, second))) {
					//detect castle, double pawn move and enpassant
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
						int d = getY(first) - getY(second);
						if(Math.abs(d) == 2) {
							container.getBoard().enPassantSquare = first - d/2;
						} else if(captured.isEmpty() && getX(first) - getX(second) != 0) {
							//enpassant capture
							container.getBoard().capturePiece(container, getX(second), getY(first));
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
									
	 				if(callback != null) {
						callback.run();
					}
				};
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		});
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

	public static void getLegalMoves(ChessBoard board, Consumer<List<Pair<Integer, Integer>>> callback) {
		List<Pair<Integer, Integer>> list = new ArrayList<>();
		ChessEngineManager.getPossibleMoves(board.getFEN(board.getCurrentColor()), (result) -> {
			String[] moves = result.split(" ");
			
			for(String move : moves) {
				String first = move.substring(0, 2);
				String second = move.substring(2, 4);
				
				list.add(Pair.of(convertNumberFormat(first), convertNumberFormat(second)));
			}
			callback.accept(list);
		});
	}
}
