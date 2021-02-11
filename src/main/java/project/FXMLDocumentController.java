package project;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;

public class FXMLDocumentController implements Initializable {
    
    @FXML
    private ProgressBar progressBar;
    protected static ProgressBar statProgressBar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
       statProgressBar = progressBar;
    }
}
