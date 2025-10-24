package com.fii.targ.gdlpda.ui;

public class TrolleyInfo {
    private String sn;
    private String cellId;

    public TrolleyInfo(String sn, String cellId) {
        this.cellId = cellId;
        this.sn = sn;
    }

    public String getSn() {
        return this.sn;
    }
    public String getCellId() { return this.cellId; }

    public void setSn(String sn) {
        this.sn = sn;
    }
    public void setCellId(String cellId) { this.cellId = cellId; }
}