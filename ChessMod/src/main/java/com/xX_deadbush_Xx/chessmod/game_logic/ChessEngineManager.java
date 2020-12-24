package com.xX_deadbush_Xx.chessmod.game_logic;

import java.io.File;
import java.util.function.Consumer;

import com.xX_deadbush_Xx.chessmod.game_logic.inventory.ChessBoard;

import xyz.niflheim.stockfish.StockfishClient;
import xyz.niflheim.stockfish.engine.enums.Query;
import xyz.niflheim.stockfish.engine.enums.QueryType;
import xyz.niflheim.stockfish.engine.enums.Variant;
import xyz.niflheim.stockfish.exceptions.StockfishInitException;

public class ChessEngineManager {
	
	public static final ChessEngineManager INSTANCE = new ChessEngineManager();
	
	private final StockfishClient CLIENT;
	
	private ChessEngineManager() {
		try {
			this.CLIENT = new StockfishClient.Builder().
					setInstances(1)
					.setVariant(Variant.BMI2)
					.setPath(new File("../src/main/resources/assets/chessmod/stockfish/stockfish_10_x64_bmi2").getAbsolutePath())
					.build();
		} catch (StockfishInitException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	public static void getPossibleMoves(ChessBoard position, Consumer<String> callback) {
        INSTANCE.CLIENT.submit(new Query.Builder(QueryType.Legal_Moves)
                .setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
                .build(),
                result -> System.out.println(result));
	}
	
	public String convertStockfishQuery(ChessBoard position) {
		return "";
	}
}
