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
	
	protected ExecutorService executor;
    private final BufferedReader input;
    private final BufferedWriter output;
    private final Process process;

	public UCIChessEngine() throws Exception {
        try {
            process = Runtime.getRuntime().exec(getPath());
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            this.executor = Executors.newSingleThreadExecutor();
            
            sendAndRead("uci", "uciok", null);
			waitUntilReady();
			send("setoption name UCI_LimitStrength value true");
			
        } catch (IOException e) {
            throw new Exception("Exception while trying to initialize chess engine: ", e);
        }
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
