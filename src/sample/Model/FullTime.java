// This part of the project is written by Nguyen Duc Trong

package sample.Model;

import java.text.DecimalFormat;

public class FullTime extends Employee {
    private double base_salary;  // lương cơ bản
    private int extra_hours;  // số giờ làm thêm
    private String salary;   // lương

    public FullTime(String name, String cmt, String department, String time_start,
                    double base_salary, int extra_hours) {
        super(name, cmt, department, time_start);
        this.base_salary = base_salary;
        this.extra_hours = extra_hours;
        this.type = "Full Time";
        DecimalFormat decimalFormat = new DecimalFormat("#0.0");
        this.salary = decimalFormat.format(2*base_salary+0.2*extra_hours) + " triệu";
    }

    @Override
    public void info() {
        super.info();
        System.out.println("Base Salary: " + base_salary);
        System.out.println("Extra Hours: " + extra_hours);
    }

    public void setBase_salary(int base_salary) {
        this.base_salary = base_salary;
    }

    public void setExtra_hours(int extra_hours) {
        this.extra_hours = extra_hours;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public double getBase_salary() {
        return base_salary;
    }

    public int getExtra_hours() {
        return extra_hours;
    }

    public String getSalary() {
        return salary;
    }
}
