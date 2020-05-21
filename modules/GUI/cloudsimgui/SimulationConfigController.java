package GUI.cloudsimgui;

import examples.org.cloudbus.cloudsim.examples.power.Helper;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import examples.org.cloudbus.cloudsim.examples.power.random.*;
import java.io.IOException;

/**
 * FXML Controller class
 *
 * @author Sabir Mohammedi Taieb
 */
public class SimulationConfigController implements Initializable {
    
    String[] args = {};
    String choices[] = {"NfIqrGpaMmt","IqrMc","LrMmt","MadMu","ThrRs"};
    int vmNumber;
    int pmNumber;
    
        @FXML
    private TextField numVmsField;

    @FXML
    private TextField numPmField;

    @FXML
    private Button startSimulation;
    
    @FXML
    private ChoiceBox<String> Approach;
    
     
    @FXML private TableView<SimulationLine> simulationTable;
    
    @FXML private TableColumn<SimulationLine,String> name;
      
    @FXML
    private TableColumn<SimulationLine, Integer> Vm;
    
    @FXML
    private TableColumn<SimulationLine, Integer> Pm;

    @FXML
    private TableColumn<SimulationLine,Double> energy;
    
    @FXML
    private TableColumn<SimulationLine, Double> Time;

    @FXML
    private TableColumn<SimulationLine,Double> oslav;

    @FXML
    private TableColumn<SimulationLine,Integer> nbMigr;

    @FXML
    private TableColumn<SimulationLine,Double> Emigr;

    @FXML
    private TableColumn<SimulationLine,Double> Tmigr;
      
    /**
     * Initializes the controller class.
     * @param url
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        name.setCellValueFactory(new PropertyValueFactory<>("approachName"));
        Vm.setCellValueFactory(new PropertyValueFactory<>("vm"));
        Pm.setCellValueFactory(new PropertyValueFactory<>("pm"));
        energy.setCellValueFactory(new PropertyValueFactory<>("energy"));
        Time.setCellValueFactory(new PropertyValueFactory<>("time"));
        oslav.setCellValueFactory(new PropertyValueFactory<>("OSLAV"));
        nbMigr.setCellValueFactory(new PropertyValueFactory<>("numberMigration"));
        Emigr.setCellValueFactory(new PropertyValueFactory<>("migrationEnergy"));
        Tmigr.setCellValueFactory(new PropertyValueFactory<>("migrationTime"));
        numVmsField.setText("50");
        numPmField.setText("50");
        Approach.setItems(FXCollections.observableArrayList(choices));
        Approach.setValue("NfIqrGpaMmt");
        simulationTable.setItems(getSLines());
    } 
   
    // get the simulation results in lines
    public  ObservableList<SimulationLine> getSLines(){
        
        ObservableList<SimulationLine> lines = FXCollections.observableArrayList();
        lines.add(new SimulationLine("NfIqrGpaMmtDynamic",50,50,45.25,10.23,3.23,2000,12.23,16.23));
        lines.add(new SimulationLine("NfIqrGpaMmt",50,50,45.25,456.23,3.23,2000,12.23,16.23));
        
        return lines;
    }
    
    // Start Simulation button
    public void StartSimulation()throws IOException{
        ObservableList<SimulationLine> line = FXCollections.observableArrayList();
       vmNumber = Integer.parseInt(numVmsField.getText());
       pmNumber = Integer.parseInt(numPmField.getText());
       RandomConstants.setNUMBER_OF_VMS(vmNumber);
       RandomConstants.setNUMBER_OF_HOSTS(pmNumber);
        if(Approach.getValue() == ("NfIqrGpaMmt")){
            System.out.println("NfGpa");
            NfIqrGpaMmt.main(args);
            simulationTable.getItems().add(new SimulationLine("NfIqrGpaMmt",Helper.getNbVm(),Helper.getNbPm(),
                    Helper.getEnergy(),Helper.getTime(),Helper.getOslav(), Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
        }
        else if(Approach.getValue() == ("IqrMc")){
            System.out.println("IqrMc");
            IqrMc.main(args);
            simulationTable.getItems().add(new SimulationLine
        ("IqrMc",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
        }
    else if(Approach.getValue() == ("LrMmt")){
            System.out.println("LrMmt");
            LrMmt.main(args);
            simulationTable.getItems().add(new SimulationLine
        ("LrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
    }else if(Approach.getValue() == ("MadMu")){
        MadMu.main(args);
        simulationTable.getItems().add(new SimulationLine
        ("MadMu",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
    }else if(Approach.getValue().equals("ThrRs")){
        ThrRs.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("ThrRs",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
        }else{
            System.out.println("NULL");
        }
    }
    
}
