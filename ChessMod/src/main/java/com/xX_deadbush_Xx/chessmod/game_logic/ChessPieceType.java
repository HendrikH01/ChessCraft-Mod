package com.xX_deadbush_Xx.chessmod.game_logic;

import javax.annotation.Nullable;

public enum ChessPieceType {
	PAWN(1, "p"),
	HORSEY(3, "n"),
	BISHOP(3, "b"),
	ROOK(5, "r"),
	QUEEN(9, "q"),
	KING(3, "k");

	public final int value;
	private String fenId;

	ChessPieceType(int value, String fen) {
		this.value = value;
		this.fenId = fen;
	}

	public String getFENid(PieceColor color) {
		return color == PieceColor.WHITE ? this.fenId.toUpperCase() : this.fenId;
	}
	
	@Nullable
	public static ChessPieceType getTypeFromFEN(String fen) {
		for(ChessPieceType type : values()) {
			if(type.fenId == fen.toLowerCase())
				return type;
		}
		
		return null;
	}
	
	public boolean canMoveStraight() {
		return this == ROOK || this == QUEEN;
	}
	
	public boolean canMoveDiagonaly() {
		return this == BISHOP || this == QUEEN;
	}
}
