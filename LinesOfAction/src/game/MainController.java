package game;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import static game.Main.*;

public class MainController {
	@FXML
	Text turnText, whitePlayer, blackPlayer;
	@FXML
	GridPane checkerBoard;
	private static final StackPane[][] grid = new StackPane[10][10];

	private void addRectangle(byte row, byte col, Color color) {
		byte strokeWidth = CELL_DIMENSION/15;
		Rectangle rectangle = new Rectangle(CELL_DIMENSION-strokeWidth-2, CELL_DIMENSION-strokeWidth-2, Color.TRANSPARENT);
		rectangle.setStroke(color);
		rectangle.setStrokeWidth(strokeWidth);
		grid[row][col].getChildren().add(rectangle);
	}

	private void displayState() {
		turnText.setText(stateName[turn] + "'s turn");
		for (byte row = 0; row < boardSize; row++) {
			for (byte col = 0; col < boardSize; col++) {
				ObservableList<Node> children = grid[row][col].getChildren();
				if (children.size() > 1) children.remove(1, children.size());
				if (states[row][col] != NONE) {
					children.add(new ImageView(stateImage[states[row][col]]));
				}
				if (selectedRow==row && selectedCol==col) {
					addRectangle(row, col, Color.GREEN);
				}
			}
		}
		if (selectedRow!=-1 && selectedCol!=-1) {
			boolean[][] validTo = validDest();
			for (byte row = 0; row < boardSize; row++) {
				for (byte col = 0; col < boardSize; col++) {
					if (validTo[row][col]) {
						addRectangle(row, col, Color.YELLOW);
					}
				}
			}
		}
	}

	public void initialize() {
		for (byte col = 0; col < boardSize; col++) {
			for (byte row = 0; row < boardSize; row++) {
				final byte _col = col, _row = row;
				grid[row][col] = new StackPane(new ImageView(((row + col) & 1) != 0 ? bg_black : bg_white));
				grid[row][col].setOnMouseClicked(event -> { if (stateIsHuman[turn]) Main.click(_row, _col); });
			}
		}
		for (byte row = 0; row < boardSize; row++) {
			for (byte col = 0; col < boardSize; col++) {
				states[row][col] = (row==0 || row== boardSize -1) && !(col==0 || col== boardSize -1) ? BLACK :
						(col==0 || col== boardSize -1) && !(row==0 || row== boardSize -1) ? WHITE :
								NONE;
				checkerBoard.add(grid[row][col], col, row);
			}
		}
		whitePlayer.setText(stateIsHuman[WHITE] ? "Human" : "AI");
		blackPlayer.setText(stateIsHuman[BLACK] ? "Human" : "AI");
		new Thread(()->{
			while (true) {
				Platform.runLater(this::displayState);
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@FXML
	void help() {
		Main.showHelp();
	}
}
