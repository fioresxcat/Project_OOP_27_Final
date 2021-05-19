// This part of the project is written by Nguyen Duc Long

package sample.DAO;

import javafx.scene.control.Alert;
import sample.Model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import sample.AppConfig;

public class DatabaseConnect {
    private List<Employee> employList;
    private AlertDisplay alertDisplay;

    public DatabaseConnect() throws SQLException {
        alertDisplay = new AlertDisplay();
        populateEmployList();
    }

    public List<Employee> getEmployList() {
        return employList;
    }

    public List<Employee> populateEmployList() {
        String sql_command = "SELECT * FROM EMPLOYEE";
        try (Connection conn = DriverManager.getConnection(
                AppConfig.DATABASE_URL,AppConfig.USERNAME , AppConfig.PASSWORD);
             PreparedStatement preparedStatement = conn.prepareStatement(sql_command);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            employList = new ArrayList<Employee>();
            while (resultSet.next()) {
                Double base_salary = resultSet.getDouble("base_salary");
                int extra_hour = resultSet.getInt("extra_hour");
                int sold_insurance = resultSet.getInt("sold_insurance");
                String department = resultSet.getString("department");
                String type = resultSet.getString("type");
                String name = resultSet.getString("name");
                String time_start = resultSet.getString("time_start");
                String cmt = resultSet.getString("cmt");

                if(type.equals("Full Time")) {
                    FullTime employee = new FullTime(name, cmt, department, time_start, base_salary, extra_hour);
                    employee.setType("Full Time");
                    employList.add(employee);
                } else if(type.equals("Part Time")) {
                    PartTime employee = new PartTime(name, cmt, department, time_start, sold_insurance);
                    employee.setType("Part Time");
                    employList.add(employee);
                }
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        }
        return employList;
    }

    public int addEmployee(Employee employee) {
        String sql_command = "INSERT INTO EMPLOYEE (name, cmt, type, department, time_start, base_salary, extra_hour, sold_insurance ) VALUES (?,?,?,?,?,?,?,?)";
        String name = employee.getName();
        String cmt = employee.getCmt();
        String department = employee.getDepartment();
        String time = employee.getTime_start();
        String type = employee.getType();

        try (Connection conn = DriverManager.getConnection(
                AppConfig.DATABASE_URL, AppConfig.USERNAME, AppConfig.PASSWORD);
             PreparedStatement preparedStatement = conn.prepareStatement(sql_command)) {
            preparedStatement.setString(1,name);
            preparedStatement.setString(2, cmt);
            preparedStatement.setString(3, type);
            preparedStatement.setString(4, department);
            preparedStatement.setString(5, time);

            if(type.equals("Full Time")) {
                double base = ((FullTime) employee).getBase_salary();
                int extra = ((FullTime) employee).getExtra_hours();
                preparedStatement.setDouble(6,base);
                preparedStatement.setInt(7, extra);
                preparedStatement.setNull(8, Types.INTEGER); // Có thể setNull cho một trường giá trị trong preparedStatement.
            } else if(type.equals("Part Time")) {
                int sold_insurance = ((PartTime) employee).getSold_insurance();
                preparedStatement.setNull(6, Types.INTEGER);
                preparedStatement.setNull(7, Types.INTEGER);
                preparedStatement.setInt(8,sold_insurance);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Can't insert!");
            e.printStackTrace();
            alertDisplay.showWarningAlert("Insert Failed");
            return -1;
        }
        System.out.println("Insert OK!");
        alertDisplay.showInfoAlert("Insert Successfully");
        return 0;
    }

    public int deleteEmployee(List<Employee> delList) {
        for (Employee employee:delList) {
            String sql_command = "DELETE FROM EMPLOYEE WHERE cmt = ?";
            try (Connection conn = DriverManager.getConnection(AppConfig.DATABASE_URL, AppConfig.USERNAME, AppConfig.PASSWORD);
                 PreparedStatement preparedStatement = conn.prepareStatement(sql_command);
            ) {
                preparedStatement.setString(1, employee.getCmt());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Can't delete");
                e.printStackTrace();
                alertDisplay.showWarningAlert("Delete failed");
                return -1;
            }
        }
        alertDisplay.showInfoAlert("Delete Successfully!");
        return 0;
    }

    public void displayEmployee() {
        for (Employee employee:employList) {
            System.out.println(employee.getName());
        }
    }

    public int updateFullTime(String name, String cmt, String type, String department, String time, double base, int extra) {
        String sql_command = String.format("UPDATE EMPLOYEE " +
                "SET \"name\" = '%s', cmt = '%s', \"type\" = '%s', department = '%s', time_start = '%s', base_salary = %f, extra_hour = %d" +
                " WHERE cmt = '%s'", name, cmt, type, department, time,base, extra, cmt);
        try (Connection conn = DriverManager.getConnection(AppConfig.DATABASE_URL, AppConfig.USERNAME, AppConfig.PASSWORD);
             PreparedStatement preparedStatement = conn.prepareStatement(sql_command);
        ) {
            System.out.println("Updating FullTime Employee...");
            preparedStatement.executeUpdate();
            System.out.println("Update Successfully");
            alertDisplay.showInfoAlert("Update Successfully");
        } catch (SQLException e) {
            System.out.println("Can't update FullTime");
            alertDisplay.showWarningAlert("Update failed");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int updatePartTime(String name, String cmt, String type, String department, String time, int sold_insurance) {
        String sql_command = String.format("UPDATE EMPLOYEE " +
                "SET \"name\" = '%s', cmt = '%s', \"type\" = '%s', department = '%s', time_start = '%s', sold_insurance = %d" +
                " WHERE cmt = '%s'", name,cmt, type, department, time, sold_insurance, cmt);

        try (Connection conn = DriverManager.getConnection(AppConfig.DATABASE_URL, AppConfig.USERNAME, AppConfig.PASSWORD);
             PreparedStatement preparedStatement = conn.prepareStatement(sql_command);
        ) {
            System.out.println("Updating Part Time Employee...");
            preparedStatement.executeUpdate();
            System.out.println("Update Successfully");
            alertDisplay.showInfoAlert("Update Successfully");
        } catch (SQLException e) {
            System.out.println("Can't update PartTime");
            alertDisplay.showWarningAlert("Update Failed");
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

}
