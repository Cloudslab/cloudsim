package GUI.cloudsimgui;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.collections.ObservableList;

/**
 * FXML Controller class
 *
 * @author Sabir Mohammedi Taieb
 */
public class ViewChartController implements Initializable {

    /**
     * Initializes the controller class.
     */
    
    ObservableList<SimulationLine> data = SimulationConfigController.getLines();
    
    @FXML
    private BarChart<String, Double> EnergyChart;

    @FXML
    private CategoryAxis X;

    @FXML
    private NumberAxis Y;
    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        X.setCategories(FXCollections.<String>observableArrayList(Arrays.asList("50/50","150/120","200/150","250/200","350/250","400/350","450/350","500/450",
                 "550/450","600/400","600/420","600/450")));
      
        XYChart.Series set1 = new XYChart.Series<>();
        set1.setName("NfIqrGpaMmt");
        set1.getData().add(new XYChart.Data("50/50",SimulationConfigController.getEnergyMatrix(0, 0)));
        set1.getData().add(new XYChart.Data("150/120",SimulationConfigController.getEnergyMatrix(1, 0)));
        set1.getData().add(new XYChart.Data("200/150",SimulationConfigController.getEnergyMatrix(2, 0)));
        set1.getData().add(new XYChart.Data("250/200",SimulationConfigController.getEnergyMatrix(3, 0)));
        set1.getData().add(new XYChart.Data("350/250",SimulationConfigController.getEnergyMatrix(4, 0)));
        set1.getData().add(new XYChart.Data("400/350",SimulationConfigController.getEnergyMatrix(5, 0)));
        set1.getData().add(new XYChart.Data("450/350",SimulationConfigController.getEnergyMatrix(6, 0)));
        set1.getData().add(new XYChart.Data("500/450",SimulationConfigController.getEnergyMatrix(7, 0)));
        set1.getData().add(new XYChart.Data("550/450",SimulationConfigController.getEnergyMatrix(8, 0)));
        set1.getData().add(new XYChart.Data("600/400",SimulationConfigController.getEnergyMatrix(9, 0)));
        set1.getData().add(new XYChart.Data("600/420",SimulationConfigController.getEnergyMatrix(10,0)));
        set1.getData().add(new XYChart.Data("600/450",SimulationConfigController.getEnergyMatrix(11, 0)));
        
        XYChart.Series set2 = new XYChart.Series<>();
        set2.setName("IqrMc");
        set2.getData().add(new XYChart.Data("50/50",SimulationConfigController.getEnergyMatrix(0, 1)));
        set2.getData().add(new XYChart.Data("150/120",SimulationConfigController.getEnergyMatrix(1, 1)));
        set2.getData().add(new XYChart.Data("200/150",SimulationConfigController.getEnergyMatrix(2, 1)));
        set2.getData().add(new XYChart.Data("250/200",SimulationConfigController.getEnergyMatrix(3, 1)));
        set2.getData().add(new XYChart.Data("350/250",SimulationConfigController.getEnergyMatrix(4, 1)));
        set2.getData().add(new XYChart.Data("400/350",SimulationConfigController.getEnergyMatrix(5, 1)));
        set2.getData().add(new XYChart.Data("450/350",SimulationConfigController.getEnergyMatrix(6, 1)));
        set2.getData().add(new XYChart.Data("500/450",SimulationConfigController.getEnergyMatrix(7, 1)));
        set2.getData().add(new XYChart.Data("550/450",SimulationConfigController.getEnergyMatrix(8, 1)));
        set2.getData().add(new XYChart.Data("600/400",SimulationConfigController.getEnergyMatrix(9, 1)));
        set2.getData().add(new XYChart.Data("600/420",SimulationConfigController.getEnergyMatrix(10,1)));
        set2.getData().add(new XYChart.Data("600/450",SimulationConfigController.getEnergyMatrix(11, 1)));
        
        XYChart.Series set3 = new XYChart.Series<>();
        set3.setName("LrMmt");
        set3.getData().add(new XYChart.Data("50/50",SimulationConfigController.getEnergyMatrix(0, 2)));
        set3.getData().add(new XYChart.Data("150/120",SimulationConfigController.getEnergyMatrix(1, 2)));
        set3.getData().add(new XYChart.Data("200/150",SimulationConfigController.getEnergyMatrix(2, 2)));
        set3.getData().add(new XYChart.Data("250/200",SimulationConfigController.getEnergyMatrix(3, 2)));
        set3.getData().add(new XYChart.Data("350/250",SimulationConfigController.getEnergyMatrix(4, 2)));
        set3.getData().add(new XYChart.Data("400/350",SimulationConfigController.getEnergyMatrix(5, 2)));
        set3.getData().add(new XYChart.Data("450/350",SimulationConfigController.getEnergyMatrix(6, 2)));        
        set3.getData().add(new XYChart.Data("500/450",SimulationConfigController.getEnergyMatrix(7, 2)));
        set3.getData().add(new XYChart.Data("550/450",SimulationConfigController.getEnergyMatrix(8, 2)));
        set3.getData().add(new XYChart.Data("600/400",SimulationConfigController.getEnergyMatrix(9, 2)));
        set3.getData().add(new XYChart.Data("600/420",SimulationConfigController.getEnergyMatrix(10,2)));
        set3.getData().add(new XYChart.Data("600/450",SimulationConfigController.getEnergyMatrix(11, 2)));
        
        XYChart.Series set4 = new XYChart.Series<>();
        set4.setName("MadMu");
        set4.getData().add(new XYChart.Data("50/50",SimulationConfigController.getEnergyMatrix(0, 3)));
        set4.getData().add(new XYChart.Data("150/120",SimulationConfigController.getEnergyMatrix(1, 3)));
        set4.getData().add(new XYChart.Data("200/150",SimulationConfigController.getEnergyMatrix(2, 3)));
        set4.getData().add(new XYChart.Data("250/200",SimulationConfigController.getEnergyMatrix(3, 3)));
        set4.getData().add(new XYChart.Data("350/250",SimulationConfigController.getEnergyMatrix(4, 3)));
        set4.getData().add(new XYChart.Data("400/350",SimulationConfigController.getEnergyMatrix(5, 3)));
        set4.getData().add(new XYChart.Data("450/350",SimulationConfigController.getEnergyMatrix(6, 3)));
        set4.getData().add(new XYChart.Data("500/450",SimulationConfigController.getEnergyMatrix(7, 3)));
        set4.getData().add(new XYChart.Data("550/450",SimulationConfigController.getEnergyMatrix(8, 3)));
        set4.getData().add(new XYChart.Data("600/400",SimulationConfigController.getEnergyMatrix(9, 3)));
        set4.getData().add(new XYChart.Data("600/420",SimulationConfigController.getEnergyMatrix(10,3)));
        set4.getData().add(new XYChart.Data("600/450",SimulationConfigController.getEnergyMatrix(11, 3)));
        
        XYChart.Series set5 = new XYChart.Series<>();
        set5.setName("ThrRs");
        set5.getData().add(new XYChart.Data("50/50",SimulationConfigController.getEnergyMatrix(0, 4)));
        set5.getData().add(new XYChart.Data("150/120",SimulationConfigController.getEnergyMatrix(1, 4)));
        set5.getData().add(new XYChart.Data("200/150",SimulationConfigController.getEnergyMatrix(2, 4)));
        set5.getData().add(new XYChart.Data("250/200",SimulationConfigController.getEnergyMatrix(3, 4)));
        set5.getData().add(new XYChart.Data("350/250",SimulationConfigController.getEnergyMatrix(4, 4)));
        set5.getData().add(new XYChart.Data("400/350",SimulationConfigController.getEnergyMatrix(5, 4)));
        set5.getData().add(new XYChart.Data("450/350",SimulationConfigController.getEnergyMatrix(6, 4)));
        set5.getData().add(new XYChart.Data("500/450",SimulationConfigController.getEnergyMatrix(7, 4)));
        set5.getData().add(new XYChart.Data("550/450",SimulationConfigController.getEnergyMatrix(8, 4)));
        set5.getData().add(new XYChart.Data("600/400",SimulationConfigController.getEnergyMatrix(9, 4)));
        set5.getData().add(new XYChart.Data("600/420",SimulationConfigController.getEnergyMatrix(10,4)));
        set5.getData().add(new XYChart.Data("600/450",SimulationConfigController.getEnergyMatrix(11,4)));
        
        XYChart.Series set6 = new XYChart.Series<>();
        set6.setName("LrrMmt");
        set6.getData().add(new XYChart.Data("50/50",SimulationConfigController.getEnergyMatrix(0, 5)));
        set6.getData().add(new XYChart.Data("150/120",SimulationConfigController.getEnergyMatrix(1, 5)));
        set6.getData().add(new XYChart.Data("200/150",SimulationConfigController.getEnergyMatrix(2, 5)));
        set6.getData().add(new XYChart.Data("250/200",SimulationConfigController.getEnergyMatrix(3, 5)));
        set6.getData().add(new XYChart.Data("350/250",SimulationConfigController.getEnergyMatrix(4, 5)));
        set6.getData().add(new XYChart.Data("400/350",SimulationConfigController.getEnergyMatrix(5, 5)));
        set6.getData().add(new XYChart.Data("450/350",SimulationConfigController.getEnergyMatrix(6, 5)));
        set6.getData().add(new XYChart.Data("500/450",SimulationConfigController.getEnergyMatrix(7, 5)));
        set6.getData().add(new XYChart.Data("550/450",SimulationConfigController.getEnergyMatrix(8, 5)));
        set6.getData().add(new XYChart.Data("600/400",SimulationConfigController.getEnergyMatrix(9, 5)));
        set6.getData().add(new XYChart.Data("600/420",SimulationConfigController.getEnergyMatrix(10,5)));
        set6.getData().add(new XYChart.Data("600/450",SimulationConfigController.getEnergyMatrix(11, 5)));
        
        XYChart.Series set7 = new XYChart.Series<>();
        set7.setName("IqrMmt");
        set7.getData().add(new XYChart.Data("50/50",SimulationConfigController.getEnergyMatrix(0, 6)));
        set7.getData().add(new XYChart.Data("150/120",SimulationConfigController.getEnergyMatrix(1, 6)));
        set7.getData().add(new XYChart.Data("200/150",SimulationConfigController.getEnergyMatrix(2, 6)));
        set7.getData().add(new XYChart.Data("250/200",SimulationConfigController.getEnergyMatrix(3, 6)));
        set7.getData().add(new XYChart.Data("350/250",SimulationConfigController.getEnergyMatrix(4, 6)));
        set7.getData().add(new XYChart.Data("400/350",SimulationConfigController.getEnergyMatrix(5, 6)));
        set7.getData().add(new XYChart.Data("450/350",SimulationConfigController.getEnergyMatrix(6, 6)));
        set7.getData().add(new XYChart.Data("500/450",SimulationConfigController.getEnergyMatrix(7, 6)));
        set7.getData().add(new XYChart.Data("550/450",SimulationConfigController.getEnergyMatrix(8, 6)));
        set7.getData().add(new XYChart.Data("600/400",SimulationConfigController.getEnergyMatrix(9, 6)));
        set7.getData().add(new XYChart.Data("600/420",SimulationConfigController.getEnergyMatrix(10,6)));
        set7.getData().add(new XYChart.Data("600/450",SimulationConfigController.getEnergyMatrix(11, 6)));
        
        EnergyChart.getData().addAll(set1,set2,set3,set4,set5,set6,set7);
        
        
    }    
    
}
