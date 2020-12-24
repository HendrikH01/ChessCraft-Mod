package com.xX_deadbush_Xx.chessmod.game_logic;

public enum ChessPieceType {
	PAWN(1),
	HORSEY(3),
	BISHOP(3),
	ROOK(5),
	QUEEN(9),
	KING(3);

	public final int value;

	ChessPieceType(int value) {
		this.value = value;
	}
}
