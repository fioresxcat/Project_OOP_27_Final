// This part of the project is written by Tran Xuan Tung

package sample.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sample.DAO.DatabaseConnect;
import sample.Model.PartTime;

import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import sample.Model.*;

public class AverageSalaryController implements Initializable {
    private List<Employee> employeeList;
    private DatabaseConnect databaseConnect;
    private AlertDisplay alertDisplay;

    @FXML
    private Button calavgbt, cancelbt;

    @FXML
    private TextField avgsal;

    @FXML
    private DatePicker datepicker;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        alertDisplay = new AlertDisplay();
        datepicker.setEditable(false);

        try {
            databaseConnect = new DatabaseConnect();
            employeeList = databaseConnect.getEmployList();
            System.out.println("Retrieve employees successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        calavgbt.setOnAction(event -> {
            LocalDate date = datepicker.getValue();
            Double sum = 0.0;
            int count = 0;
            for (Employee employee:employeeList) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate temp_date = LocalDate.parse(employee.getTime_start(), formatter);
                if(temp_date.isEqual(date) || temp_date.isBefore(date)) {
                    if(employee.getType().equals("Full Time")) {
                        sum += Double.parseDouble(((FullTime) employee).getSalary().replace(" triệu", ""));
                    } else if(employee.getType().equals("Part Time")) {
                        sum += Double.parseDouble(((PartTime) employee).getSalary().replace(" triệu", ""));
                    }
                    count++;
                }
            }

            if(sum==0.0 && count==0) {
                alertDisplay.showInfoAlert("Tại thời điểm này, công ty chưa có nhân viên nào!");
            } else {
                Double average_salary = sum / count;
                DecimalFormat numberFormat = new DecimalFormat("#.00");
                String average_salary_converted = numberFormat.format(average_salary) + " triệu";
                alertDisplay.showInfoAlert(String.format("Tại thời điểm này, công ty có %d nhân viên với mức lương trung bình là %s", count, average_salary_converted));
            }
        });

        cancelbt.setOnAction(event -> {
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.close();
        });
    }


}
