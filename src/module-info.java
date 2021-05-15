module Project_OOP_27 {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires java.sql;
    opens sample;
    opens sample.DAO;
    opens sample.Model;
    opens sample.Controller;
}