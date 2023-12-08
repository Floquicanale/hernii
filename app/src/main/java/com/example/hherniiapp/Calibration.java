package com.example.hherniiapp;

public class Calibration {
    public String a_sbp, b_sbp, a_dbp, b_dbp;
    public Calibration() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Calibration(String a_sbp, String b_sbp, String a_dbp, String b_dbp) {
        this.a_sbp = a_sbp;
        this.b_sbp = b_sbp;
        this.a_dbp = a_dbp;
        this.b_dbp = b_dbp;
    }

    public String getA_sbp() {
        return a_sbp;
    }

    public void setA_sbp(String a_sbp) {
        this.a_sbp = a_sbp;
    }

    public String getB_sbp() {
        return b_sbp;
    }

    public void setB_sbp(String b_sbp) {
        this.b_sbp = b_sbp;
    }

    public String getA_dbp() {
        return a_dbp;
    }

    public void setA_dbp(String a_dbp) {
        this.a_dbp = a_dbp;
    }

    public String getB_dbp() {
        return b_dbp;
    }

    public void setB_dbp(String b_dbp) {
        this.b_dbp = b_dbp;
    }
}
