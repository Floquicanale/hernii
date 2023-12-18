package com.example.hherniiapp;

public class Calibration {
    public String a_sbp, b_sbp, a_dbp, b_dbp, ptt1, ptt2, ptt3, sbp1, sbp2, sbp3, dbp1, dbp2, dbp3;
    public Calibration() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Calibration(String a_sbp, String b_sbp, String a_dbp, String b_dbp, String sbp1, String sbp2, String sbp3, String dbp1, String dbp2, String dbp3, String ptt1, String ptt2, String ptt3) {
        this.a_sbp = a_sbp;
        this.b_sbp = b_sbp;
        this.a_dbp = a_dbp;
        this.b_dbp = b_dbp;
        this.sbp1 = sbp1;
        this.sbp2 = sbp2;
        this.sbp3 = sbp3;
        this.dbp1 = dbp1;
        this.dbp2 = dbp2;
        this.dbp3 = dbp3;
        this.ptt1 = ptt1;
        this.ptt2 = ptt2;
        this.ptt3 = ptt3;
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

    public String getDbp1() {
        return dbp1;
    }

    public void setDbp1(String dbp1) {
        this.dbp1 = dbp1;
    }

    public String getDbp2() {
        return dbp2;
    }

    public void setDbp2(String dbp2) {
        this.dbp2 = dbp2;
    }

    public String getDbp3() {
        return dbp3;
    }

    public void setDbp3(String dbp3) {
        this.dbp3 = dbp3;
    }

    public String getSbp1() {
        return sbp1;
    }

    public void setSbp1(String sbp1) {
        this.sbp1 = sbp1;
    }

    public String getSbp2() {return sbp2;}

    public void setSbp2(String sbp2) {
        this.sbp2 = sbp2;
    }

    public String getSbp3() {
        return sbp3;
    }

    public void setSbp3(String sbp3) { this.sbp3 = sbp3; }

    public String getPtt1() {
        return ptt1;
    }

    public void setPtt1(String ptt1) {
        this.ptt1 = ptt1;
    }

    public String getPtt2() {
        return ptt2;
    }

    public void setPtt2(String ptt2) { this.ptt2 = ptt2; }

    public String getPtt3() {
        return ptt3;
    }

    public void setPtt3(String ptt3) {
        this.ptt3 = ptt3;
    }
}
