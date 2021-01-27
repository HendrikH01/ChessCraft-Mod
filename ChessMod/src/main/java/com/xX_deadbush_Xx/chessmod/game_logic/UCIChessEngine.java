package com.xX_deadbush_Xx.chessmod.game_logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class UCIChessEngine {
	
	protected final ExecutorService executor;
    private BufferedReader input;
    private  BufferedWriter output;
    private Process process;

	public UCIChessEngine() throws Exception {
        this.executor = Executors.newSingleThreadExecutor();
		this.start();
	}
	
	protected void send(String command) {
		try {
			output.write(command + "\n");
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    protected void sendAndRead(String command, final String cancel, Consumer<List<String>> callback) throws IOException {
		try {
			output.write(command + "\n");
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<String> lines = new ArrayList<>();

		try {
			while (true) {
				String line = input.readLine();
				
				if (line != null) {
					lines.add(line);
					if (line.startsWith(cancel)) {
						if (callback != null)
							callback.accept(lines);

						break;
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    protected boolean isAlive() {
    	return process.isAlive();
    }
    
    protected void start() {
        try {
			process = Runtime.getRuntime().exec(getPath());
	        input = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
	        
	        sendAndRead("uci", "uciok", null);
			waitUntilReady();
			send("setoption name Skill_Level value 20");
			waitUntilReady();
			send("setoption name UCI_LimitStrength value true");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
	protected void waitUntilReady() {
		try {
			send("isready");
			while (true) {
				String line = input.readLine();
				if(line != null && line.equals("readyok"))
					return;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	private String getPath() {
		return new File("../src/main/resources/assets/chessmod/stockfish/stockfish_x32.exe").getAbsolutePath();
	}
}
