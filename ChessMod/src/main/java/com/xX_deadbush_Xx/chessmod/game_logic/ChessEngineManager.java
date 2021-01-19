package com.xX_deadbush_Xx.chessmod.game_logic;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class ChessEngineManager extends UCIChessEngine {
	
	private static ChessEngineManager INSTANCE;
	private Random rand = new Random();
	
	private ChessEngineManager() throws Exception {
		super();
	}
	
	public static synchronized void init() {
		
		try {
			INSTANCE = new ChessEngineManager();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void getLegalMoves(String fen, Consumer<List<String>> callback) {
 		INSTANCE.executor.execute(new Runnable() {

			@Override
			public void run() {
				INSTANCE.waitUntilReady();

				INSTANCE.send("position fen " + fen);
		
				INSTANCE.waitUntilReady();

				try {
					INSTANCE.sendAndRead("go perft 1 ", "Nodes", (response) -> {
						for (int i = response.size() - 1; i >= 0; i--) {
							String s = response.get(i);
							if (s.contains("Nodes searched") || s.isEmpty()) {
								response.remove(i);
							} else {
								s.replaceAll("^[^:]*", "");
							}
						}
						callback.accept(response);
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void getNextMove(String fen, int strength, Consumer<String> callback) {
		
		int elo;
		float randomchance = 0.0f;
		
		switch(strength) {
		case 1: {
			elo = 100;
			randomchance = 0.0f;
			break;
		}
		case 2: {
			elo = 300;
			randomchance = 0.15f;
			break;
		}
		case 3: {
			elo = 600;
			randomchance = 0.05f;
			break;
		}
		case 4: {
			elo = 1000;
			break;
		}
		case 5: {
			elo = 1300;
			break;
		}
		case 6: {
			elo = 1500;
			break;
		}
		case 7: {
			elo = 1700;
			break;
		}
		case 8: {
			elo = 1900;
			break;
		}
		case 9: {
			elo = 2200;
			break;
		}
		default: {
			elo = 2500;
			break;
		}
		}
		
		if(randomchance != 0 && INSTANCE.rand.nextFloat() < randomchance) {
			randomMove(fen, callback);
			try {
				Thread.sleep(1200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return;
		}
		
		INSTANCE.executor.execute(new Runnable() {
 			
			@Override
			public void run() {
				INSTANCE.waitUntilReady();
				
				INSTANCE.send("setoption name UCI_Elo value " + elo);
				INSTANCE.waitUntilReady();
				
				INSTANCE.send("position fen " + fen);
				INSTANCE.waitUntilReady();
				
				try {
					INSTANCE.sendAndRead("go bestmove depth 1", "bestmove", (response) -> {
						try {
							Thread.sleep(1200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						String s = response.get(response.size() - 1);
						String[] split = s.split("\\s+");
		
						if (split.length >= 2)
							callback.accept(split[1]);
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
 		});
	}
	
	private static void randomMove(String fen, Consumer<String> callback) {
		getLegalMoves(fen, (moves) -> {
			if(!moves.isEmpty())
				callback.accept(moves.get(INSTANCE.rand.nextInt(moves.size())));
		});
	}
}
