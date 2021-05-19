// This part of the project is written by Tran Xuan Tung

package sample.Controller;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import sample.DAO.DatabaseConnect;
import sample.Model.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

public class MainController implements Initializable {
    @FXML
    private Button updatebt, addbt, clearbt, calbt, delbt, loadbt;

    @FXML
    private TableView<Employee> table;

    @FXML
    private TableColumn<FullTime, Integer> extracol;

    @FXML
    private TableColumn<FullTime, Double> basecol;

    @FXML
    private TableColumn<PartTime, Integer> soldcol;

    @FXML
    private TableColumn<Employee, String> namecol, idcol, cmtcol, departcol, timecol, typecol, salarycol;

    @FXML
    private TextField searchtext, nametext, cmttext, basetext, extratext, insurancetext;

    @FXML
    private DatePicker timetext;

    @FXML
    private ComboBox comboBox;

    @FXML
    private RadioButton radioFull, radioPart;

    @FXML
    private VBox vBox;

    private DatabaseConnect databaseConnect;

    private ObservableList<Employee> employeeList;

    private AlertDisplay alertDisplay;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Khởi tạo đối tượng AlertDisplay để hiển thị các thông báo
        alertDisplay = new AlertDisplay();

        // Ẩn nút Update, khi nào bấm vào nhân viên nào thì mới hiện ra
        updatebt.setVisible(false);

        // ---------------------------------------------------- Setup bảng nhân viên ---------------------------------------------------------

        // Cho phép chọn nhiêu dòng cùng 1 lúc
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Gán cột nào sẽ hiển thị thuộc tính nào của Employee
        idcol.setCellValueFactory(p -> new ReadOnlyObjectWrapper(table.getItems().indexOf(p.getValue()) + 1 + "")); // cột này chỉ đơn giản là số thứ tự
        idcol.setSortable(false);
        typecol.setCellValueFactory(new PropertyValueFactory<Employee, String>("type"));
        namecol.setCellValueFactory(new PropertyValueFactory<Employee, String>("name"));
        cmtcol.setCellValueFactory(new PropertyValueFactory<Employee, String>("cmt"));
        departcol.setCellValueFactory(new PropertyValueFactory<Employee, String>("department"));
        timecol.setCellValueFactory(new PropertyValueFactory<Employee, String>("time_start"));
        basecol.setCellValueFactory(p -> {
            if(p.getValue() instanceof FullTime) {
                return new ReadOnlyObjectWrapper<>(p.getValue().getBase_salary());
            }
            return null;
        });
        extracol.setCellValueFactory(p -> {
            if(p.getValue() instanceof FullTime) {
                return new ReadOnlyObjectWrapper<>(p.getValue().getExtra_hours());
            }
            return null;
        });
        soldcol.setCellValueFactory(p -> {
            if(p.getValue() instanceof PartTime) {
                return new ReadOnlyObjectWrapper<>(p.getValue().getSold_insurance());
            }
            return null;
        });
        salarycol.setCellValueFactory(new PropertyValueFactory<Employee, String>("salary"));

        // Load bảng dữ liệu từ database
        try {
            loadTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Đặt Listener cho dòng của bảng
        // Khi bấm 1 lần vào dòng, sẽ hiện nút update để chỉnh sửa thông tin Employee ở dòng đó
        // Khi bấm vào dòng đó 1 lần nữa -> bỏ chọn dòng
        table.setRowFactory(table -> {
            final TableRow<Employee> row = new TableRow<>();
            row.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                final int index = row.getIndex();
                if (index >= 0 && index < table.getItems().size()) {
                    if (table.getSelectionModel().isSelected(index)) { // nếu dòng đó đang được chọn rồi
                        // Xóa các selection
                        table.getSelectionModel().clearSelection();
                        clear(new ActionEvent());
                        event.consume();
                    } else {
                        // Cho nút Update hiện ra
                        updatebt.setVisible(true);
                        // Đưa thông tin của Student được chọn vào các TextField
                        System.out.println("Đang chọn: " + row.getItem().getName());
                        nametext.setText(row.getItem().getName());
                        cmttext.setText(row.getItem().getCmt());
                        comboBox.setValue(String.valueOf(row.getItem().getDepartment()));
                        timetext.getEditor().setText(row.getItem().getTime_start());

                        if(row.getItem().getType().equals("Full Time")) {
                            radioFull.setSelected(true);
                            basetext.setText(String.valueOf(((FullTime) row.getItem()).getBase_salary()));
                            extratext.setText(String.valueOf(((FullTime)row.getItem()).getExtra_hours()));
                            insurancetext.clear();
                        } else {
                            radioPart.setSelected(true);
                            basetext.clear();
                            extratext.clear();
                            insurancetext.setText(String.valueOf(((PartTime)row.getItem()).getSold_insurance()));
                        }
                    }
                }
            });
            return row;
        });

        // Setup để nut update chỉ hiện khi có duy nhất 1 dòng trong table được chọn
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updatebt.setVisible(table.getSelectionModel().getSelectedItems().size() == 1);
        });

        // ------------------------------------------------------ Input Validation -------------------------------------------------------

        // Chỉ chấp nhận các chữ số cho mục cmt, base_salary, extra_hour, sold_insurance
        cmttext.setTextFormatter(new TextFormatter<>(change ->
                (change.getControlNewText().matches("[0-9]*")) ? change : null));
        extratext.setTextFormatter(new TextFormatter<>(change ->
                (change.getControlNewText().matches("[0-9]*")) ? change : null));
        insurancetext.setTextFormatter(new TextFormatter<>(change ->
                (change.getControlNewText().matches("[0-9]*")) ? change : null));


        // ---------------------------------------------------- Listener cho các Controls-----------------------------------------------

        // Set Time Converter for Date Picker
        timetext.setEditable(false);

        timetext.setConverter(new StringConverter<LocalDate>() {
            private DateTimeFormatter dateTimeFormatter= DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            public String toString(LocalDate localDate) {
                if(localDate==null)
                    return "";
                return dateTimeFormatter.format(localDate);
            }

            @Override
            public LocalDate fromString(String dateString) {
                if(dateString==null || dateString.trim().isEmpty())
                {
                    return null;
                }
                return LocalDate.parse(dateString,dateTimeFormatter);
            }
        });

        // set up vBox (là phần bên trái của giao diện, bao gồm ô Search, các trường thông tin v.v)
        // Khi bấm vào phần bên trái này sẽ tự động xóa các selection bên table
        vBox.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            table.getSelectionModel().clearSelection();
        });

        // Set up combo Box (để chọn đơn vị, phòng ban)
        comboBox.getItems().add("Sale");
        comboBox.getItems().add("Technical");
        comboBox.getItems().add("Marketing");

        // Set up radio Button (để chọn loại nhân viên)
        ToggleGroup toggleGroup = new ToggleGroup();
        radioFull.setToggleGroup(toggleGroup);
        radioPart.setToggleGroup(toggleGroup);
        radioPart.selectedProperty().addListener((obs, wasSelected, nowSelected) -> {
            // Nếu nhân viên là PartTime, disable 2 ô là base_salary và extra_hour
            if(nowSelected) {
                basetext.clear();
                extratext.clear();
                basetext.setDisable(true);
                extratext.setDisable(true);
                insurancetext.setDisable(false);
            }
        });
        radioFull.selectedProperty().addListener((obs, wasSelected, nowSelected) -> {
            if(nowSelected) {
                // Nếu nhân viên là FullTime, disable ô "sold_insurance"
                insurancetext.clear();
                insurancetext.setDisable(true);
                basetext.setDisable(false);
                extratext.setDisable(false);
            }
        });

        // Đặt listener cho Load button (Khi bấm vào button này thì sẽ thực hiện hàm loadTable()
        loadbt.setOnAction(event -> {
            try {
                loadTable();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            clear(event);
        });

        // Đặt listener cho Update button
        updatebt.setOnAction(event -> {
            String name = nametext.getText();
            if(name.matches(".*\\d.*")) {
                alertDisplay.showWarningAlert("Name must not contain number!");
                return;
            }
            String cmt = cmttext.getText();
            String type = ((RadioButton) toggleGroup.getSelectedToggle()).getText();
            System.out.println(type);
            String department = (String) comboBox.getValue();
            String time = timetext.getEditor().getText();

            int flag = 2;

            Employee employee = null;
            if(type.equals("Full Time")) {
                Double base_salary = null;
                try {
                    base_salary = Double.parseDouble(basetext.getText());
                } catch (Exception e) {
                    alertDisplay.showWarningAlert("Base Salary must be a valid decimal number!");
                    return;
                }
                int extra = Integer.parseInt(extratext.getText());
                // Add to database
                flag = databaseConnect.updateFullTime(name, cmt, type, department, time, base_salary, extra);
                // Initialize an employee instance to update to the employeeList
                employee = new FullTime(name,cmt,department,time,base_salary,extra);
            } else if (type.equals("Part Time")) {
                if(insurancetext.getText().isEmpty()) {
                    alertDisplay.showWarningAlert("Sold Insurance must be filled");
                }
                int sold_insurance = Integer.parseInt(insurancetext.getText());
                // Add to database
                flag = databaseConnect.updatePartTime(name, cmt, type, department, time, sold_insurance);
                // Initialize an employee instance to update to the employeeList
                employee = new PartTime(name,cmt,department,time,sold_insurance);
            }

            // Modify employeeList to update Table
            if(flag==0) {
                for (final ListIterator<Employee> i = employeeList.listIterator(); i.hasNext();) {
                    final Employee empl = i.next();
                    if (empl.equals(table.getSelectionModel().getSelectedItem())) {
                        System.out.println("OK đã tìm thấy");
                        i.set(employee);
                    }
                }
            }
        });

        // Đặt listener cho Clear button
        clearbt.setOnAction(this::clear);

        // Đặt listener cho Add button
        addbt.setOnAction(event -> {
            String name = nametext.getText();
            if(name.matches(".*\\d.*")) {
                alertDisplay.showWarningAlert("Name must not contain number!");
                return;
            }
            String cmt = cmttext.getText();
            String type = ((RadioButton) toggleGroup.getSelectedToggle()).getText();
            String department = (String) comboBox.getValue();
            String time = timetext.getEditor().getText();

            if(type.equals("Full Time")) {
                Double base_salary = null;
                try {
                    base_salary = Double.parseDouble(basetext.getText());
                } catch (Exception e) {
                    alertDisplay.showWarningAlert("Base Salary must be a valid decimal number!");
                    return;
                }
                int extra_hour = Integer.parseInt(extratext.getText());
                Employee employee = new FullTime(name,cmt,department,time,base_salary,extra_hour);
                System.out.println("Inserting FullTime Employee...");
                if(databaseConnect.addEmployee(employee) == 0) {
                    employeeList.add(employee);
                }
            } else if(type.equals("Part Time")) {
//                if(insurancetext.getText().isEmpty()) {
//                    alertDisplay.showWarningAlert("Sold Insurance must be filled");
//                }
                int sold_insurance = Integer.parseInt(insurancetext.getText());
                Employee employee = new PartTime(name, cmt, department, time, sold_insurance);
                System.out.println("Inserting PartTime Employee...");
                if(databaseConnect.addEmployee(employee) == 0){
                    employeeList.add(employee);
                }
            }
        });

        // Cài đặt để chỉ khi điền đủ thông tin cần thiết mới thì mới bấm được nút Add
        // Tham số 1 của hàm createBooleanBinding trả về boolean. Nút add sẽ bị disable khi cái đó đúng.
        // Tham số 2 là những giá trị mà nó phục thuộc vào (có kiểu Observable).
        addbt.disableProperty().bind(
                Bindings.createBooleanBinding( () ->
                        nametext.getText().trim().isEmpty(), nametext.textProperty()
                ).or(Bindings.createBooleanBinding( () ->
                        cmttext.getText().trim().isEmpty(), cmttext.textProperty())
                ).or(Bindings.createBooleanBinding(() ->
                        !table.getSelectionModel().getSelectedItems().isEmpty(), table.getSelectionModel().selectedItemProperty())
                ).or(Bindings.createBooleanBinding( () ->
                        timetext.getEditor().getText().trim().isEmpty(), timetext.getEditor().textProperty())
                ).or(Bindings.createBooleanBinding( () -> {
                    if (toggleGroup.getSelectedToggle() == null) {
                        return true;
                    }
                    String type = ((RadioButton) toggleGroup.getSelectedToggle()).getText();
                    if(type.equals("Full Time")) return basetext.getText().isEmpty() || extratext.getText().isEmpty();
                    return insurancetext.getText().isEmpty();
                }, basetext.textProperty(), extratext.textProperty(), insurancetext.textProperty())
                )
        );

        // Cài đặt để chỉ khi điền đủ thông tin cần thiết mới thì mới bấm được nút Update
        updatebt.disableProperty().bind(
                Bindings.createBooleanBinding( () ->
                        nametext.getText().trim().isEmpty(), nametext.textProperty()
                ).or(Bindings.createBooleanBinding( () ->
                        cmttext.getText().trim().isEmpty(), cmttext.textProperty())
                ).or(Bindings.createBooleanBinding( () ->
                        timetext.getEditor().getText().trim().isEmpty(), timetext.getEditor().textProperty())
                ).or(Bindings.createBooleanBinding( () -> {
                            if (toggleGroup.getSelectedToggle() == null) {
                                return true;
                            }
                            String type = ((RadioButton) toggleGroup.getSelectedToggle()).getText();
                            if(type.equals("Full Time")) return basetext.getText().isEmpty() || extratext.getText().isEmpty();
                            return insurancetext.getText().isEmpty();
                        }, basetext.textProperty(), extratext.textProperty(), insurancetext.textProperty())
                )
        );

        // Cài đặt để khi có ít nhất một nhân viên được chọn mới có thể ấn nút xóa
        delbt.disableProperty().bind(Bindings.createBooleanBinding( () ->
                table.getSelectionModel().getSelectedItems().isEmpty(), table.getSelectionModel().selectedItemProperty()
        ));

        // Đặt listener cho nút xóa
        delbt.setOnAction(event -> {
            List<Employee> delList = table.getSelectionModel().getSelectedItems();
            if(databaseConnect.deleteEmployee(delList) == 0){
                employeeList.removeAll(delList);
            }
        });

        // Đặt listener cho nút Calculate Salary
        calbt.setOnAction(event -> {
            try {
                URL url_file = new File("src/sample/View/AverageSalary.fxml").toURI().toURL();
                Parent root = FXMLLoader.load(url_file);
                // Parent root = FXMLLoader.load(getClass().getResource("View/AverageSalary.fxml"));
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Calculate Average Salary");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // --------------------------------------------- Cài đặt chức năng tìm kiếm ------------------------------------------------
        FilteredList<Employee> filteredList = new FilteredList<>(employeeList, e->true);

        searchtext.textProperty().addListener((observableValue, oldVal, newVal) -> {
            filteredList.setPredicate((Predicate<? super Employee>) employee -> {
                if(newVal == null || newVal.isEmpty()) {
                    return true;
                }
                String lowerNewVal = newVal.toLowerCase();
                if(employee.getName().toLowerCase().contains(lowerNewVal)) {
                    return true;
                } else if(employee.getCmt().toLowerCase().contains(lowerNewVal)) {
                    return true;
                } else return employee.getTime_start().toLowerCase().contains(lowerNewVal);
            });
            SortedList<Employee> sortedList = new SortedList<>(filteredList);
            sortedList.comparatorProperty().bind(table.comparatorProperty());
            table.setItems(sortedList);
        } );

    }

    public void loadTable() throws SQLException {
        databaseConnect = new DatabaseConnect();
        List<Employee> tempList = databaseConnect.getEmployList();
        this.employeeList = FXCollections.observableList(tempList);
        table.setItems(employeeList);
    }

    public void clear(ActionEvent event) {
        nametext.clear();
        cmttext.clear();
        timetext.getEditor().clear();
        basetext.clear();
        extratext.clear();
        insurancetext.clear();
    }
}



