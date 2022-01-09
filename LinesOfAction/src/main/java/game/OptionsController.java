package game;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

public class OptionsController {
    @FXML
    ToggleGroup bs, bp, wp;

    @FXML
    void optionsSubmit() {
        RadioButton bs_selected = (RadioButton) bs.getSelectedToggle();
        Main.boardSize = Byte.parseByte(bs_selected.getText());
        RadioButton wp_selected = (RadioButton) wp.getSelectedToggle();
        Main.stateIsHuman[0] = wp_selected.getText().equals("Human");
        RadioButton bp_selected = (RadioButton) bp.getSelectedToggle();
        Main.stateIsHuman[1] = bp_selected.getText().equals("Human");
        Main.gameStart();
    }
}
