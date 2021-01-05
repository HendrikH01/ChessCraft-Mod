package com.xX_deadbush_Xx.chessmod.game_logic;

import java.io.File;
import java.util.function.Consumer;

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
					.setPath(new File("../src/main/resources/assets/chessmod/stockfish").getAbsolutePath() + "/")
					.build();
		} catch (StockfishInitException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	public static void getPossibleMoves(String fen, Consumer<String> callback) {
		try {
	        INSTANCE.CLIENT.submit(new Query.Builder(QueryType.Legal_Moves)
	                .setFen(fen)
	                .build(),
	                result -> callback.accept(result));
			}	
		catch(Exception e) {
			e.printStackTrace();
		}
  	}
}
