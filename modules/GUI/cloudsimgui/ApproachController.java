/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI.cloudsimgui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;

/**
 *
 * @author sabir
 */
public class ApproachController implements Initializable{
    
    @FXML
    private Button SelectionBtn;

    @FXML
    private Button AllocationBtn;

    @FXML
    private Button LowerThrBtn; 
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }
    
    @FXML
    public void gotoSelection() throws IOException{
    }
    
    @FXML
    public void gotoAllocation() throws IOException{
        
    }
    
    @FXML
    public void gotoLowerThr() throws IOException{
        
    }
}
