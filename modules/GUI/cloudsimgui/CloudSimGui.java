
package GUI.cloudsimgui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author sabir
 */
public class CloudSimGui extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/GUI/fxml/FXMLDocument.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        scene.getStylesheets().add("/GUI/CSS/style.css");
        stage.setTitle("CloudSim GUI");
        stage.getIcons().add(new Image("/GUI/icons/MainIcon.png"));
        stage.setResizable(false);
        stage.setMaxWidth(1095);
        stage.setMaxHeight(635);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
