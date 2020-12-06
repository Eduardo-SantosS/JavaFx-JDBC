package gui;

import db.DbIntegrityException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entities.Department;
import model.services.DepartmentService;
import sample.Main;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DepartmentListController implements Initializable, DataChangeListener {

    private DepartmentService service;

    @FXML
    private TableView<Department> departmentTableView;
    @FXML
    private TableColumn<Department, Integer> tableColumnId;
    @FXML
    private TableColumn<Department, String> tableColumnName;
    @FXML
    private TableColumn<Department, Department> tableColumnEdit;
    @FXML
    private TableColumn<Department, Department> tableColumnRemove;
    @FXML
    private Button btnNew;
    private ObservableList<Department> obsList;

    @FXML
    public void onBtnNewAction(ActionEvent event){
        Stage parentStage = Utils.currentStage(event);
        Department dep = new Department();
        createDialogForm(dep,"/gui/DepartmentForm.fxml", parentStage);
    }

    public void setDepartmentService(DepartmentService service){
        this.service = service;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeNodes();

    }

    private void initializeNodes() {
        tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        Stage stage = (Stage) Main.getMainScene().getWindow();
        departmentTableView.prefHeightProperty().bind(stage.heightProperty());
    }

    public void updateTableView(){
        if (service == null){
            throw new IllegalStateException("Service was null");
        }
        List<Department> list = service.findAll();
        obsList = FXCollections.observableArrayList(list);
        departmentTableView.setItems(obsList);
        initEditButtons();
        initRemoveButtons();
    }

    private void createDialogForm(Department department ,String absoluteName,Stage parentStage){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
            Pane pane = loader.load();

            DepartmentFormController controller = loader.getController();
            controller.setDepartment(department);
            controller.setDepartmentService(new DepartmentService());
            controller.subscribeDataChangeListener(this);
            controller.updateFormData();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Enter Department data");
            dialogStage.setScene(new Scene(pane));
            dialogStage.setResizable(false);
            dialogStage.initOwner(parentStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.showAndWait();

        }catch (IOException e){
            Alerts.showAlert("IO exception", "Error loading view", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void initEditButtons(){
        tableColumnEdit.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        tableColumnEdit.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("edit");

            @Override
            protected void updateItem(Department department, boolean b) {
                super.updateItem(department, b);

                if (department == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(btn);
                btn.setOnAction(
                        event -> createDialogForm(department, "/gui/DepartmentForm.fxml", Utils.currentStage(event)));
            }
        });
    }

    private void initRemoveButtons(){
        tableColumnRemove.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        tableColumnRemove.setCellFactory(param -> new TableCell<>(){
            private final Button btn = new Button("remove");

            @Override
            protected void updateItem(Department department, boolean b) {
                super.updateItem(department, b);

                if (department == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(btn);
                btn.setOnAction(event -> removeEntity(department));
            }
        });
    }

    private void removeEntity(Department department) {
        Optional<ButtonType> result = Alerts.showConfirmation("Confirmation", "Are you sure to delete?");

        if (result.get() == ButtonType.OK){
            if (service == null){
                throw new IllegalStateException("Service was null");
            }
            try {
                service.remove(department);
                updateTableView();
            }catch (DbIntegrityException e){
                Alerts.showAlert("Error removing object", null, e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @Override
    public void onDataChanged() {
        updateTableView();
    }
}
