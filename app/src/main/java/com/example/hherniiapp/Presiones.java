package com.example.hherniiapp;

public class Presiones {

    public String sbp, dbp, hr, ptt;

    public Presiones(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Presiones(String sbp, String dbp, String hr, String ptt) {
        this.sbp = sbp;
        this.dbp = dbp;
        this.hr = hr;
        this.ptt = ptt;
    }

    public String getSbp() {
        return sbp;
    }

    public void setSbp(String sbp) {
        this.sbp = sbp;
    }

    public String getDbp() {
        return dbp;
    }

    public void setDbp(String dbp) {
        this.dbp = dbp;
    }

    public String getHr() {
        return hr;
    }

    public void setHr(String hr) { this.hr = hr;}

    public String getPtt() {
        return ptt;
    }

    public void setPtt(String ptt) {
        this.ptt = ptt;
    }
}
