package com.bank.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Employee - Concrete subclass of Person.
 */
public class Employee extends Person {
    private String designation;
    private BigDecimal salary;
    private int branchId;
    private LocalDate hireDate;

    public Employee(int empId, String name, String designation, BigDecimal salary,
                    String phone, String email, int branchId, LocalDate hireDate) {
        super(empId, name, email, phone);
        this.designation = designation;
        this.salary = salary;
        this.branchId = branchId;
        this.hireDate = hireDate;
    }

    public String getDesignation() { return designation; }
    public BigDecimal getSalary() { return salary; }
    public int getBranchId() { return branchId; }
    public LocalDate getHireDate() { return hireDate; }

    @Override
    public String getRole() {
        return designation;
    }
}
