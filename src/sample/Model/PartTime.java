package sample.Model;


import java.text.DecimalFormat;

public class PartTime extends Employee {
    private int sold_insurance;  // số bảo hiểm bán được
    private String salary;   // lương

    public PartTime(String name, String cmt, String department, String time_start, int sold_insurance) {
        super(name, cmt, department, time_start);
        this.sold_insurance = sold_insurance;
        this.type = "Part Time";
        DecimalFormat decimalFormat = new DecimalFormat("#0.0");
        this.salary = decimalFormat.format(0.2*sold_insurance) + " triệu";
    }

    @Override
    public void info() {
        super.info();
        System.out.println("Num_of_sold_insurance: " + sold_insurance);
    }

    public int getSold_insurance() {
        return sold_insurance;
    }

    public void setSold_insurance(int sold_insurance) {
        this.sold_insurance = sold_insurance;
    }

    public String getSalary() {
        return salary;
    }
}