package com.xX_deadbush_Xx.chesscraft.game_logic;

public enum PieceColor {
	WHITE("w"),
	BLACK("b");

	private String fenId;

	PieceColor(String fen) {
		this.fenId = fen;
	}
	
	public String getFENid() {
		return this.fenId;
	}
	
	public PieceColor getOpposite() {
		return this == WHITE ? BLACK : WHITE;
	}
}
