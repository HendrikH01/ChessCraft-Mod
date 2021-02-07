package com.xX_deadbush_Xx.chesscraft.game_logic;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import com.xX_deadbush_Xx.chesscraft.ChessMod;

import net.minecraft.util.ResourceLocation;

public class ChessEngineManager extends UCIChessEngine {
	
	private static ChessEngineManager INSTANCE;
	private Random rand = new Random();
	
	private ChessEngineManager(String path) throws Exception { 
		super(path);
	}
	
	public static synchronized void init(String path) {
		
		try {
			INSTANCE = new ChessEngineManager(path);
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
				
				if(!INSTANCE.isAlive()) {
					ChessMod.LOGGER.fatal("Chess engine crashed while trying to find the legal moves! Restarting now. FEN-string: " + fen);
				}
			}
		});
	}
	
	public static void getNextMove(String fen, final int strength, Consumer<String> callback) {
				
		int elo;
		final int depth;
		final float randomchance;
		
		switch(strength) {
		case 1: {
			elo = 1350;
			randomchance = 1.0f;
			depth = 1;
			break;
		}
		case 2: {
			elo = 1350;
			randomchance = 0.15f;
			depth = 1;
			break;
		}
		case 3: {
			elo = 1350;
			randomchance = 0.05f;
			depth = 1;
			break;
		}
		case 4: {
			elo = 1350;
			depth = 1;
			randomchance = 0.0f;
			break;
		}
		case 5: {
			elo = 1500;
			depth = 3;
			randomchance = 0.0f;
			break;
		}
		case 6: {
			elo = 1600;
			depth = 5;
			randomchance = 0.0f;
			break;
		}
		case 7: {
			elo = 1700;
			depth = 8;
			randomchance = 0.0f;
			break;
		}
		case 8: {
			elo = 1900;
			depth = 10;
			randomchance = 0.0f;
			break;
		}
		case 9: {
			elo = 2200;
			depth = 16;
			randomchance = 0.0f;
			break;
		}
		default: {
			elo = 2800;
			depth = 8;
			randomchance = 0.0f;
			break;
		}
		}
		
		INSTANCE.executor.execute(new Runnable() {
 			
			@Override
			public void run() {
				
				if(randomchance != 0 && INSTANCE.rand.nextFloat() < randomchance) {
					randomMove(fen, callback);
					try {
						Thread.sleep(1600);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return;
				}
				
				INSTANCE.waitUntilReady();
				
				INSTANCE.send("setoption name UCI_Elo value " + elo);
				INSTANCE.waitUntilReady();
				
				INSTANCE.send("position fen " + fen);
				INSTANCE.waitUntilReady();
				
				try {
					INSTANCE.sendAndRead("go bestmove depth " + depth, "bestmove", (response) -> {
						
						try {
							Thread.sleep(1600);
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
				
				if(!INSTANCE.isAlive()) {
					ChessMod.LOGGER.fatal("Chess engine crashed while trying to find the best move! Restarting now. FEN-string: " + fen + ", set strength: " + strength);
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

	public static ResourceLocation getRL() {
		return new ResourceLocation(ChessMod.MOD_ID, "stockfish/stockfish_x32.exe");
	}
}
