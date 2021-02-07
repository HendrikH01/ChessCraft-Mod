package com.xX_deadbush_Xx.chesscraft.game_logic;

import static com.xX_deadbush_Xx.chesscraft.objects.ModRegistry.*;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;

public enum ChessPieceType {
	PAWN(1, "p", WHITE_PAWN, BLACK_PAWN),
	HORSEY(3, "n", WHITE_HORSEY, BLACK_HORSEY),
	BISHOP(3, "b", WHITE_BISHOP, BLACK_BISHOP),
	ROOK(5, "r", WHITE_ROOK, BLACK_ROOK),
	QUEEN(9, "q", WHITE_QUEEN, BLACK_QUEEN),
	KING(3, "k", WHITE_KING, BLACK_KING);

	public final int value;
	private String fenId;
	private RegistryObject<Item> white;
	private RegistryObject<Item> black;
	
	ChessPieceType(int value, String fen, RegistryObject<Item> w, RegistryObject<Item> b) {
		this.value = value;
		this.fenId = fen;
		this.black = b;
		this.white = w;
	}
	
	public Item getItem(PieceColor color) {
		return color == PieceColor.WHITE ? this.white.get() : this.black.get();
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
