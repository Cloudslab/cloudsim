package GUI.cloudsimgui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author sabir
 */
public class FXMLDocumentController implements Initializable {
    @FXML
    private Label label;
    
    @FXML
    private void goToSimulationConfig() throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("/GUI/fxml/SimulationConfig.fxml"));
        Stage window = new Stage();
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/GUI/CSS/simulationconfig.css");
        window.setScene(scene);
        window.setTitle("Simulation Configuration");
        window.getIcons().add(new Image("/GUI/icons/MainIcon.png"));
        window.setResizable(false);
        window.show();   
    }
    
    @FXML
    private void goToAbout() throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("/GUI/fxml/AboutUs.fxml"));
        Stage window = new Stage();
        Scene scene = new Scene(root);
        window.setScene(scene);
        window.setTitle("About Us");
        window.getIcons().add(new Image("/GUI/icons/MainIcon.png"));
        window.setResizable(false);
        window.show();   
    }
    
    
    @FXML
    private void goToApproach() throws IOException{
        
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
