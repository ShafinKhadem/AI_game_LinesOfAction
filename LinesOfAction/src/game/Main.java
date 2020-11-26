package game;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/**
 * @author Nafiur Rahman Khadem
 */

public class Main extends Application {
	private static Stage gameWindow;

	//<editor-fold defaultstate="collapsed" desc="Images">
	static final byte CELL_DIMENSION = 60;
	static final Image bg_white = new Image("images/bg_white.png", CELL_DIMENSION, CELL_DIMENSION, false, true, true);
	static final Image bg_black = new Image("images/bg_black.png", CELL_DIMENSION, CELL_DIMENSION, false, true, true);
	private static final Image white_piece = new Image("images/chips_white.png", CELL_DIMENSION, CELL_DIMENSION, true, true, true);
	private static final Image black_piece = new Image("images/chips_black.png", CELL_DIMENSION, CELL_DIMENSION, true, true, true);
	//</editor-fold>

	static final byte WHITE = 0, BLACK = 1, NONE = 2;
	static final String[] stateName = {"White", "Black", "None"};
	static final Image[] stateImage = {white_piece, black_piece};
	static boolean[] stateIsHuman = {false, false};

	//<editor-fold defaultstate="collapsed" desc="GameState">
	static byte boardSize = 6;
	static byte turn = BLACK, selectedRow = -1, selectedCol = -1;
	static byte[][] states = new byte[10][10]; // WHITE / BLACK / NONE, 0-based indexing, states[i][j] -> state of cell at row i, column j
	//</editor-fold>

	static final byte dir = 8;
	static final byte[] dirr = {-1,-1,-1,0,0,1,1,1};
	static final byte[] dirc = {-1,0,1,-1,1,-1,0,1};

	static byte opponent() {
		return turn == WHITE ? BLACK : WHITE;
	}

	static int bfs(int row, int col) {
		int ret = 0;
		boolean[][] vstd = new boolean[10][10];
		Queue<Cell> q = new LinkedList<>();
		q.add(new Cell(row, col));
		vstd[row][col] = true;
		while (!q.isEmpty()) {
			Cell cur = q.poll();
			++ret;
			for (byte i = 0; i < dir; i++) {
				Cell nc = new Cell(cur.row + dirr[i], cur.col + dirc[i]);
				if (isValid(nc.row, nc.col) && states[nc.row][nc.col]== states[row][col] && !vstd[nc.row][nc.col]) {
					q.add(nc);
					vstd[nc.row][nc.col] = true;
				}
			}
		}
		return ret;
	}

	private static boolean checkWin(byte player) {
		int cnt = 0, cntOne = 0;
		for (byte row = 0; row < boardSize; ++row) {
			for (byte col = 0; col < boardSize; ++col) {
				if (states[row][col] == player) {
					if (cnt == 0) cntOne = bfs(row, col);
					++cnt;
				}
			}
		}
		return cnt == cntOne;
    }

	private static boolean isValid(int row, int col) {
		return row >= 0 && row < boardSize && col >= 0 && col < boardSize;
	}

	static boolean[][] validDest() {
		assert selectedRow!=-1 && selectedCol!=-1 : "Call to validDest without selecting!!!";
		boolean[][] validTo = new boolean[10][10];
		for (int i = 0; i < dir; i++) {
			int cnt = 0, minOpponent = boardSize, curr = selectedRow, curc = selectedCol, curMov = 0;
			while (isValid(curr + dirr[i], curc + dirc[i])) {
				curr += dirr[i];
				curc += dirc[i];
				++curMov;
				if (states[curr][curc]!=NONE) {
					++cnt;
					if (states[curr][curc]!=turn) minOpponent = Math.min(minOpponent, curMov);
				}
			}
			if (minOpponent >= cnt) {
				curr = selectedRow + cnt*dirr[i];
				curc = selectedCol + cnt*dirc[i];
				if (states[curr][curc]!= states[selectedRow][selectedCol]) validTo[curr][curc] = true;
			}
		}
		return validTo;
	}

	private static void selectCell(byte row, byte col) {
		selectedRow = row;
		selectedCol = col;
	}

	private static void maybeAITurn() {
		if (stateIsHuman[turn]) return;
		try {
			PrintWriter writer = new PrintWriter("shared_file.txt");
			writer.println(turn);
			writer.println(boardSize);
			for (byte i = 0; i < boardSize; i++) {
				for (byte j = 0; j < boardSize; j++) {
					writer.print((j==0 ? "" : " ") + states[i][j]);
				}
				writer.println();
			}
			writer.close();
			String[] args = new String[] {"/bin/bash", "-c", "./src/ai"};
			Process process = new ProcessBuilder(args).redirectErrorStream(true).start();
			new Thread(()->{
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					String line;
					while ((line = reader.readLine()) != null) {
						System.out.println(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();
			new java.util.Timer().schedule(
				new java.util.TimerTask() {
					@Override
					public void run() {
						Scanner scanner = null;
						try {
							scanner = new Scanner(new File("shared_file.txt"));
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						assert scanner != null;
						byte sr = scanner.nextByte(), sc = scanner.nextByte(), dr = scanner.nextByte(), dc = scanner.nextByte();
						click(sr, sc);
						new java.util.Timer().schedule(
							new java.util.TimerTask() {
								@Override
								public void run() {
									click(dr, dc);
								}
							},
							500
						);
					}
				},
				boardSize==6 ? 1100 : 2100
			);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void moveTo(byte row, byte col) {
		states[row][col] = turn;
		states[selectedRow][selectedCol] = NONE;
		selectedRow = selectedCol = -1;
		if (checkWin(turn)) win(turn);
		else {
			turn = opponent();
			if (checkWin(turn)) win(turn);
			else maybeAITurn();
		}
	}

	static void click(byte row, byte col) {
		if (states[row][col] == turn) selectCell(row, col);
		else if (selectedRow!=-1 && selectedCol!=-1) {
			boolean[][] validTo = validDest();
			if (validTo[row][col]) {
				moveTo(row, col);
			}
		}
	}

	private static Object setScene(Stage window, String sceneFile) {
		try {
			FXMLLoader loader = new FXMLLoader(Main.class.getResource(sceneFile));
			window.setScene(new Scene(loader.load()));
			return loader.getController();
		} catch (IOException e) {
			System.out.println("fxml file " + sceneFile + " could not be loaded");
			e.printStackTrace(System.out);
			return null;
		}
	}

	static void showHelp() {
		Stage dialog = new Stage();
		dialog.initModality(Modality.NONE);
		dialog.initOwner(gameWindow);
		setScene(dialog, "helpscene.fxml");
//		dialog.setResizable(false);
		dialog.show();
	}

	private static void win(byte player) {
		Platform.runLater(()-> {
			Stage dialog = new Stage();
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.initOwner(gameWindow);
			dialog.setOnCloseRequest(event -> System.exit(1));
			dialog.show();
			Finishedscene finishedscene = (Finishedscene) setScene(dialog, "finishedscene.fxml");
			assert finishedscene != null;
			finishedscene.result.setText(stateName[player] + " won");
		});
	}

	public static void gameStart() {
		setScene(gameWindow, "mainscene.fxml");
		maybeAITurn();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		gameWindow = primaryStage;
		setScene(gameWindow, "optionsscene.fxml");
		gameWindow.setTitle("Lines of Action");
		gameWindow.getIcons().add(new Image("images/icon.png"));
//		gameWindow.setResizable(false);
		gameWindow.setOnCloseRequest(e -> System.exit(1));
		gameWindow.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
