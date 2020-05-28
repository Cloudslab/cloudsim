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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Sabir Mohammedi Taieb
 */
public class SimulationConfigController implements Initializable {
    
    String[] args = {};
    String choices[] = {"NfIqrGpaMmt","IqrMc","LrMmt","MadMu","ThrRs","LrrMmt"};
    String vmPmChoices[] = {"50/50","150/120","200/150","250/200","350/250","400/350","450/350"};
    
    /*int vmNumber;
    int pmNumber; */
    
    public static ObservableList<SimulationLine> lines = FXCollections.observableArrayList();
    
    public static double energyMatrix[][] = new double[50][50];
    public static double oslavMatrix[][] = new double[50][50];
    
     /*   @FXML
    private TextField numVmsField;

    @FXML
    private TextField numPmField;
    */
    
     @FXML
    private ChoiceBox<String> VmPmChoiceBox;
     
    @FXML
    private Button startSimulation;
    
    @FXML
    private Button SeeChart;
    
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
       /* numVmsField.setText("50");
        numPmField.setText("50"); */
        Approach.setItems(FXCollections.observableArrayList(choices));
        Approach.setValue("NfIqrGpaMmt");
        VmPmChoiceBox.setItems(FXCollections.observableArrayList(vmPmChoices));
        VmPmChoiceBox.setValue("50/50");
        //simulationTable.setItems(getSLines());
    } 
   
    // get the simulation results in lines
    public  ObservableList<SimulationLine> getSLines(){
        
        ObservableList<SimulationLine> lines = FXCollections.observableArrayList();
        lines.add(new SimulationLine("NfIqrGpaMmtDynamic",50,50,45.25,10.23,3.23,2000,12.23,16.23));
        lines.add(new SimulationLine("NfIqrGpaMmt",50,50,45.25,456.23,3.23,2000,12.23,16.23));
        
        return lines;
    }
    
    // Start Simulation button
    public void StartSimulation()throws IOException,NullPointerException{
        ObservableList<SimulationLine> lines = FXCollections.observableArrayList();
        lines = simulationTable.getItems();
        
       /*vmNumber = Integer.parseInt(numVmsField.getText());
       pmNumber = Integer.parseInt(numPmField.getText());
       RandomConstants.setNUMBER_OF_VMS(vmNumber);
       RandomConstants.setNUMBER_OF_HOSTS(pmNumber); */
       
       //if(vmNumber == 50 && pmNumber == 50){
       if(VmPmChoiceBox.getValue().equals("50/50")){
            RandomConstants.setNUMBER_OF_VMS(50);
       RandomConstants.setNUMBER_OF_HOSTS(50);
        if(Approach.getValue() == ("NfIqrGpaMmt")){
            System.out.println("NfIqrGpaMmt");
            NfIqrGpaMmt.main(args);
            simulationTable.getItems().add(new SimulationLine("NfIqrGpaMmt",Helper.getNbVm(),Helper.getNbPm(),
                    Helper.getEnergy(),Helper.getTime(),Helper.getOslav(), Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[0][0]=Helper.getEnergy();
            oslavMatrix[0][0]=Helper.getOslav();
        }
        else if(Approach.getValue() == ("IqrMc")){
            System.out.println("IqrMc");
            IqrMc.main(args);
            simulationTable.getItems().add(new SimulationLine
        ("IqrMc",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[0][1]=Helper.getEnergy();
            oslavMatrix[0][1]=Helper.getOslav();
        }
    else if(Approach.getValue() == ("LrMmt")){
            System.out.println("LrMmt");
            LrMmt.main(args);
            simulationTable.getItems().add(new SimulationLine
        ("LrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[0][2]=Helper.getEnergy();
            oslavMatrix[0][2]=Helper.getOslav();
    }else if(Approach.getValue() == ("MadMu")){
        MadMu.main(args);
        simulationTable.getItems().add(new SimulationLine
        ("MadMu",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
        energyMatrix[0][3]=Helper.getEnergy();
        oslavMatrix[0][3]=Helper.getOslav();
    }else if(Approach.getValue().equals("ThrRs")){
        ThrRs.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("ThrRs",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[0][4]=Helper.getEnergy();
         oslavMatrix[0][4]=Helper.getOslav();
    }else if(Approach.getValue().equals("LrrMmt")){
        LrrMmt.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("LrrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[0][5]=Helper.getEnergy();
         oslavMatrix[0][5]=Helper.getOslav();
    } else if(Approach.getValue().equals("IqrMmt")){
        IqrMmt.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("IqrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[0][6]=Helper.getEnergy();
         oslavMatrix[0][6]=Helper.getOslav();
        }else{
            System.out.println("NULL");
        }
          
            
       }else 
           //if (vmNumber == 150 && pmNumber == 120) {
            if(VmPmChoiceBox.getValue().equals("150/120")){
            RandomConstants.setNUMBER_OF_VMS(150);
       RandomConstants.setNUMBER_OF_HOSTS(120);
            if(Approach.getValue() == ("NfIqrGpaMmt")){
            System.out.println("NfIqrGpaMmt");
            NfIqrGpaMmt.main(args);
            simulationTable.getItems().add(new SimulationLine("NfIqrGpaMmt",Helper.getNbVm(),Helper.getNbPm(),
                    Helper.getEnergy(),Helper.getTime(),Helper.getOslav(), Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[1][0]=Helper.getEnergy();
            oslavMatrix[1][0]=Helper.getOslav();
        }
        else if(Approach.getValue() == ("IqrMc")){
            System.out.println("IqrMc");
            IqrMc.main(args);
            simulationTable.getItems().add(new SimulationLine
        ("IqrMc",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[1][1]=Helper.getEnergy();
            oslavMatrix[1][1]=Helper.getOslav();
        }
    else if(Approach.getValue() == ("LrMmt")){
            System.out.println("LrMmt");
            LrMmt.main(args);
            simulationTable.getItems().add(new SimulationLine
        ("LrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[1][2]=Helper.getEnergy();
            oslavMatrix[1][2]=Helper.getOslav();
    }else if(Approach.getValue() == ("MadMu")){
        MadMu.main(args);
        simulationTable.getItems().add(new SimulationLine
        ("MadMu",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
        energyMatrix[1][3]=Helper.getEnergy();
        oslavMatrix[1][3]=Helper.getOslav();
    }else if(Approach.getValue().equals("ThrRs")){
        ThrRs.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("ThrRs",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[1][4]=Helper.getEnergy();
         oslavMatrix[1][4]=Helper.getOslav();
    }else if(Approach.getValue().equals("LrrMmt")){
        LrrMmt.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("LrrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[1][5]=Helper.getEnergy();
         oslavMatrix[1][5]=Helper.getOslav();
          } else if(Approach.getValue().equals("IqrMmt")){
        IqrMmt.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("IqrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[1][6]=Helper.getEnergy();
         oslavMatrix[1][6]=Helper.getOslav();
        }else{
            System.out.println("NULL");
        }
           
        }else
        //if (vmNumber == 200 && pmNumber == 150) {
                 if(VmPmChoiceBox.getValue().equals("200/150")){
            RandomConstants.setNUMBER_OF_VMS(200);
       RandomConstants.setNUMBER_OF_HOSTS(150);
             if(Approach.getValue() == ("NfIqrGpaMmt")){
            System.out.println("NfIqrGpaMmt");
            NfIqrGpaMmt.main(args);
            simulationTable.getItems().add(new SimulationLine("NfIqrGpaMmt",Helper.getNbVm(),Helper.getNbPm(),
                    Helper.getEnergy(),Helper.getTime(),Helper.getOslav(), Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[2][0]=Helper.getEnergy();
            oslavMatrix[2][0]=Helper.getOslav();
        }
        else if(Approach.getValue() == ("IqrMc")){
            System.out.println("IqrMc");
            IqrMc.main(args);
            simulationTable.getItems().add(new SimulationLine
        ("IqrMc",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[2][1]=Helper.getEnergy();
            oslavMatrix[2][1]=Helper.getOslav();
        }
    else if(Approach.getValue() == ("LrMmt")){
            System.out.println("LrMmt");
            LrMmt.main(args);
            simulationTable.getItems().add(new SimulationLine
        ("LrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[2][2]=Helper.getEnergy();
            oslavMatrix[2][2]=Helper.getOslav();
    }else if(Approach.getValue() == ("MadMu")){
        MadMu.main(args);
        simulationTable.getItems().add(new SimulationLine
        ("MadMu",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
        energyMatrix[2][3]=Helper.getEnergy();
        oslavMatrix[2][3]=Helper.getOslav();
    }else if(Approach.getValue().equals("ThrRs")){
        ThrRs.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("ThrRs",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[2][4]=Helper.getEnergy();
         oslavMatrix[2][4]=Helper.getOslav();
    }else if(Approach.getValue().equals("LrrMmt")){
        LrrMmt.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("LrrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[2][5]=Helper.getEnergy();
         oslavMatrix[2][5]=Helper.getOslav();
          } else if(Approach.getValue().equals("IqrMmt")){
        IqrMmt.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("IqrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[2][6]=Helper.getEnergy();
         oslavMatrix[2][6]=Helper.getOslav();
        }else{
            System.out.println("NULL");
        }
        }else
        //if (vmNumber == 250 && pmNumber == 200) {
          if(VmPmChoiceBox.getValue().equals("250/200")){
            RandomConstants.setNUMBER_OF_VMS(250);
            RandomConstants.setNUMBER_OF_HOSTS(200);
             if(Approach.getValue().equals("NfIqrGpaMmt")){
            System.out.println("NfIqrGpaMmt");
            NfIqrGpaMmt.main(args);
            simulationTable.getItems().add(new SimulationLine("NfIqrGpaMmt",Helper.getNbVm(),Helper.getNbPm(),
                    Helper.getEnergy(),Helper.getTime(),Helper.getOslav(), Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[3][0]=Helper.getEnergy();
            oslavMatrix[3][0]=Helper.getOslav();
        }
        else if(Approach.getValue() == ("IqrMc")){
            System.out.println("IqrMc");
            IqrMc.main(args);
            simulationTable.getItems().add(new SimulationLine
        ("IqrMc",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[3][1]=Helper.getEnergy();
            oslavMatrix[3][1]=Helper.getOslav();
        }
    else if(Approach.getValue() == ("LrMmt")){
            System.out.println("LrMmt");
            LrMmt.main(args);
            simulationTable.getItems().add(new SimulationLine
        ("LrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[3][2]=Helper.getEnergy();
            oslavMatrix[3][2]=Helper.getOslav();
    }else if(Approach.getValue() == ("MadMu")){
        MadMu.main(args);
        simulationTable.getItems().add(new SimulationLine
        ("MadMu",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
        energyMatrix[3][3]=Helper.getEnergy();
        oslavMatrix[3][3]=Helper.getOslav();
    }else if(Approach.getValue().equals("ThrRs")){
        ThrRs.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("ThrRs",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[3][4]=Helper.getEnergy();
         oslavMatrix[3][4]=Helper.getOslav();
    }else if(Approach.getValue().equals("LrrMmt")){
        LrrMmt.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("LrrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[3][5]=Helper.getEnergy();
         oslavMatrix[3][5]=Helper.getOslav();
          } else if(Approach.getValue().equals("IqrMmt")){
        IqrMmt.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("IqrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[3][6]=Helper.getEnergy();
         oslavMatrix[3][6]=Helper.getOslav();
        }else{
            System.out.println("NULL");
        }
            
        }else
        //if (vmNumber == 350 && pmNumber == 250) {
             if (VmPmChoiceBox.getValue().equals("350/250")) {
            RandomConstants.setNUMBER_OF_VMS(350);
            RandomConstants.setNUMBER_OF_HOSTS(250);
             if(Approach.getValue() == ("NfIqrGpaMmt")){
            System.out.println("NfIqrGpaMmt");
            NfIqrGpaMmt.main(args);
            simulationTable.getItems().add(new SimulationLine("NfIqrGpaMmt",Helper.getNbVm(),Helper.getNbPm(),
                    Helper.getEnergy(),Helper.getTime(),Helper.getOslav(), Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[4][0]=Helper.getEnergy();
            oslavMatrix[4][0]=Helper.getOslav();
        }
        else if(Approach.getValue() == ("IqrMc")){
            System.out.println("IqrMc");
            IqrMc.main(args);
            simulationTable.getItems().add(new SimulationLine
        ("IqrMc",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[4][1]=Helper.getEnergy();
            oslavMatrix[4][1]=Helper.getOslav();
        }
    else if(Approach.getValue() == ("LrMmt")){
            System.out.println("LrMmt");
            LrMmt.main(args);
            simulationTable.getItems().add(new SimulationLine
        ("LrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[4][2]=Helper.getEnergy();
            oslavMatrix[4][2]=Helper.getOslav();
    }else if(Approach.getValue() == ("MadMu")){
        MadMu.main(args);
        simulationTable.getItems().add(new SimulationLine
        ("MadMu",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
        energyMatrix[4][3]=Helper.getEnergy();
        oslavMatrix[4][3]=Helper.getOslav();
    }else if(Approach.getValue().equals("ThrRs")){
        ThrRs.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("ThrRs",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[4][4]=Helper.getEnergy();
         oslavMatrix[4][4]=Helper.getOslav();
    }else if(Approach.getValue().equals("LrrMmt")){
        LrrMmt.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("LrrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[4][5]=Helper.getEnergy();
         oslavMatrix[4][5]=Helper.getOslav();
          } else if(Approach.getValue().equals("IqrMmt")){
        IqrMmt.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("IqrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[4][6]=Helper.getEnergy();
         oslavMatrix[4][6]=Helper.getOslav();
        }else{
            System.out.println("NULL");
        }
            
        }else
        //if (vmNumber == 400 && pmNumber == 350) {
          if (VmPmChoiceBox.getValue().equals("400/350")) {
             RandomConstants.setNUMBER_OF_VMS(400);
             RandomConstants.setNUMBER_OF_HOSTS(350);
             if(Approach.getValue() == ("NfIqrGpaMmt")){
            System.out.println("NfIqrGpaMmt");
            NfIqrGpaMmt.main(args);
            simulationTable.getItems().add(new SimulationLine("NfIqrGpaMmt",Helper.getNbVm(),Helper.getNbPm(),
                    Helper.getEnergy(),Helper.getTime(),Helper.getOslav(), Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[5][0]=Helper.getEnergy();
            oslavMatrix[5][0]=Helper.getOslav();
        }
        else if(Approach.getValue() == ("IqrMc")){
            System.out.println("IqrMc");
            IqrMc.main(args);
            simulationTable.getItems().add(new SimulationLine
        ("IqrMc",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[5][1]=Helper.getEnergy();
            oslavMatrix[5][1]=Helper.getOslav();
        }
    else if(Approach.getValue() == ("LrMmt")){
            System.out.println("LrMmt");
            LrMmt.main(args);
            simulationTable.getItems().add(new SimulationLine
        ("LrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[5][2]=Helper.getEnergy();
            oslavMatrix[5][2]=Helper.getOslav();
    }else if(Approach.getValue() == ("MadMu")){
        MadMu.main(args);
        simulationTable.getItems().add(new SimulationLine
        ("MadMu",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
        energyMatrix[5][3]=Helper.getEnergy();
        oslavMatrix[5][3]=Helper.getOslav();
    }else if(Approach.getValue().equals("ThrRs")){
        ThrRs.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("ThrRs",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[5][4]=Helper.getEnergy();
         oslavMatrix[5][4]=Helper.getOslav();
    }else if(Approach.getValue().equals("LrrMmt")){
        LrrMmt.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("LrrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[5][5]=Helper.getEnergy();
         oslavMatrix[5][5]=Helper.getOslav();
          } else if(Approach.getValue().equals("IqrMmt")){
        IqrMmt.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("IqrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[5][6]=Helper.getEnergy();
         oslavMatrix[5][6]=Helper.getOslav();
        }else{
            System.out.println("NULL");
        }
            
        }else
        //if (vmNumber == 450 && pmNumber == 350) {
          if(VmPmChoiceBox.getValue().equals("450/350")){
            RandomConstants.setNUMBER_OF_VMS(450);
            RandomConstants.setNUMBER_OF_HOSTS(350);
             if(Approach.getValue() == ("NfIqrGpaMmt")){
            System.out.println("NfIqrGpaMmt");
            NfIqrGpaMmt.main(args);
            simulationTable.getItems().add(new SimulationLine("NfIqrGpaMmt",Helper.getNbVm(),Helper.getNbPm(),
                    Helper.getEnergy(),Helper.getTime(),Helper.getOslav(), Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[6][0]=Helper.getEnergy();
            oslavMatrix[6][0]=Helper.getOslav();
        }
        else if(Approach.getValue() == ("IqrMc")){
            System.out.println("IqrMc");
            IqrMc.main(args);
            simulationTable.getItems().add(new SimulationLine
        ("IqrMc",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[6][1]=Helper.getEnergy();
            oslavMatrix[6][1]=Helper.getOslav();
        }
    else if(Approach.getValue() == ("LrMmt")){
            System.out.println("LrMmt");
            LrMmt.main(args);
            simulationTable.getItems().add(new SimulationLine
        ("LrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(), Helper.getEmigr(),Helper.getTmigr()));
            energyMatrix[6][2]=Helper.getEnergy();
            oslavMatrix[6][2]=Helper.getOslav();
    }else if(Approach.getValue() == ("MadMu")){
        MadMu.main(args);
        simulationTable.getItems().add(new SimulationLine
        ("MadMu",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
        energyMatrix[6][3]=Helper.getEnergy();
        oslavMatrix[6][3]=Helper.getOslav();
    }else if(Approach.getValue().equals("ThrRs")){
        ThrRs.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("ThrRs",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[6][4]=Helper.getEnergy();
         oslavMatrix[6][4]=Helper.getOslav();
    }else if(Approach.getValue().equals("LrrMmt")){
        LrrMmt.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("LrrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[6][5]=Helper.getEnergy();
         oslavMatrix[6][5]=Helper.getOslav();
          } else if(Approach.getValue().equals("IqrMmt")){
        IqrMmt.main(args);
         simulationTable.getItems().add(new SimulationLine
        ("IqrMmt",Helper.getNbVm(),Helper.getNbPm(),Helper.getEnergy(),Helper.getTime(),Helper.getOslav(),Helper.getNbMigr(),Helper.getEmigr(),Helper.getTmigr()));
         energyMatrix[6][6]=Helper.getEnergy();
         oslavMatrix[6][6]=Helper.getOslav();
        }else{
            System.out.println("NULL");
        }
            
        }
       
        lines = simulationTable.getItems();
    }
    
    @FXML
    public void goToChart()throws IOException{
         Parent root = FXMLLoader.load(getClass().getResource("/GUI/fxml/ViewChart.fxml"));
        Stage window = new Stage();
        Scene scene = new Scene(root);
        window.setScene(scene);
        window.setTitle("Charts");
        window.getIcons().add(new Image("/GUI/icons/MainIcon.png"));
        window.setResizable(false);
        window.show();
    }
    
       @FXML
    public void goToOslavBarChart()throws IOException{
         Parent root = FXMLLoader.load(getClass().getResource("/GUI/fxml/OslavBarChart.fxml"));
        Stage window = new Stage();
        Scene scene = new Scene(root);
        window.setScene(scene);
        window.setTitle("OSLAV Bar Chart");
        window.getIcons().add(new Image("/GUI/icons/MainIcon.png"));
        window.setResizable(false);
        window.show();
    }
    
    public static ObservableList<SimulationLine> getLines() {
        return lines;
    }

    public static void setLines(ObservableList<SimulationLine> lines) {
        SimulationConfigController.lines = lines;
    }
    
    public static double getEnergyMatrix(int i,int j){
        return energyMatrix[i][j];
    }
    
    public static double getOslavMatrix(int i,int j){
        return oslavMatrix[i][j];
    }
}
