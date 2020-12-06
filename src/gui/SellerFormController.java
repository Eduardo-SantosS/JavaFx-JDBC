package gui;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.DepartmentService;
import model.services.SellerService;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class SellerFormController implements Initializable {

    private Seller entity;

    private SellerService sellerService;

    private DepartmentService departmentService;

    private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

    @FXML
    private TextField txtId;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtEmail;
    @FXML
    private DatePicker dpBirthDate;
    @FXML
    private TextField txtBaseSalary;

    @FXML
    private ComboBox<Department> departmentComboBox;

    @FXML
    private Label labelErrorName;
    @FXML
    private Label labelErrorEmail;
    @FXML
    private Label labelErrorBirthDate;
    @FXML
    private Label labelErrorBaseSalary;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private ObservableList<Department> obsList;

    public void setSeller(Seller entity){
        this.entity = entity;
    }

    public void setServices(SellerService sellerService, DepartmentService departmentService){
        this.sellerService = sellerService;
        this.departmentService = departmentService;
    }

    public void subscribeDataChangeListener(DataChangeListener listener){
        dataChangeListeners.add(listener);
    }

    @FXML
    public void onBtnSaveAction(ActionEvent event){
        if (entity == null){
            throw new IllegalStateException("Entity was null");
        }
        if (sellerService == null){
            throw new IllegalStateException("Service was null");
        }
        try {
            entity = getFormData();
            sellerService.saveOrUpdate(entity);
            notifyDataChangeListener();
            Utils.currentStage(event).close();
        }catch (ValidationException e){
            setErrorMessages(e.getErrors());
        }catch (DbException e){
            Alerts.showAlert("Error saving obj", null, e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void notifyDataChangeListener() {
        for (DataChangeListener listener : dataChangeListeners){
            listener.onDataChanged();
        }
    }

    private Seller getFormData() {
        Seller dep = new Seller();

        ValidationException exception = new ValidationException("Validation error");
        dep.setId(Utils.tryParseToInt(txtId.getText()));
        if (txtName.getText() == null || txtName.getText().trim().equals("")){
            exception.addError("Name", "Field can't be empty");
        }
        dep.setName(txtName.getText());

        if (exception.getErrors().size() > 0){
            throw exception;
        }

        return dep;
    }

    @FXML
    public void onBtnCancelAction(ActionEvent event){
        Utils.currentStage(event).close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeNodes();
    }

    private void initializeNodes(){
        Constraints.setTextFieldInteger(txtId);
        Constraints.setTextFieldMaxLength(txtName, 60);
        Constraints.setTextFieldMaxLength(txtEmail, 50);
        Constraints.setTextFieldDouble(txtBaseSalary);
        Utils.formatDatePicker(dpBirthDate, "dd/MM/yyyy");
        initializeComboBoxDepartment();
    }

    public void updateFormData(){
        if (entity == null){
            throw new IllegalStateException("Entity was null");
        }
        txtId.setText(String.valueOf(entity.getId()));
        txtName.setText(entity.getName());
        txtEmail.setText(entity.getEmail());
        if (entity.getBirthDate() != null) {
            dpBirthDate.setValue(LocalDate.ofInstant(entity.getBirthDate().toInstant(), ZoneId.systemDefault()));
        }
        Locale.setDefault(Locale.US);
        txtBaseSalary.setText(String.format("%.2f",entity.getBaseSalary()));
        if (entity.getDepartment() == null){
            departmentComboBox.getSelectionModel().selectFirst();
        }else {
            departmentComboBox.setValue(entity.getDepartment());
        }
    }

    public void loadAssociatedObjects(){
        if (departmentService == null){
            throw new IllegalStateException("Department service was null");
        }
        List<Department> list = departmentService.findAll();
        obsList = FXCollections.observableArrayList(list);
        departmentComboBox.setItems(obsList);
    }

    private void setErrorMessages(Map<String, String > errors){
        Set<String> fields = errors.keySet();

        if (fields.contains("Name")){
            labelErrorName.setText(errors.get("Name"));
        }
    }

    private void initializeComboBoxDepartment(){
        Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<>(){
            @Override
            protected void updateItem(Department department, boolean b) {
                super.updateItem(department, b);
                setText(b ? "" : department.getName());
            }
        };
        departmentComboBox.setCellFactory(factory);
        departmentComboBox.setButtonCell(factory.call(null));
    }
}
