package com.bank.model;

import java.time.LocalDate;

/**
 * Customer - Concrete subclass of Person.
 */
public class Customer extends Person {
    private LocalDate dob;
    private String gender;
    private String address;
    private String panNumber;
    private int branchId;

    public Customer(int customerId, String name, LocalDate dob, String gender, 
                    String phone, String email, String address, String panNumber, int branchId) {
        super(customerId, name, email, phone);
        this.dob = dob;
        this.gender = gender;
        this.address = address;
        this.panNumber = panNumber;
        this.branchId = branchId;
    }

    public LocalDate getDob() { return dob; }
    public String getGender() { return gender; }
    public String getAddress() { return address; }
    public String getPanNumber() { return panNumber; }
    public int getBranchId() { return branchId; }

    public void setDob(LocalDate dob) { this.dob = dob; }
    public void setGender(String gender) { this.gender = gender; }
    public void setAddress(String address) { this.address = address; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }
    public void setBranchId(int branchId) { this.branchId = branchId; }

    @Override
    public String getRole() {
        return "CUSTOMER";
    }
}
