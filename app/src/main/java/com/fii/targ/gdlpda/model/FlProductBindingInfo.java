package com.fii.targ.gdlpda.model;
public class FlProductBindingInfo {
    private String flProduct;
    private String cellID;



    public FlProductBindingInfo(String flProduct, String cellID) {
        this.flProduct = flProduct;
        this.cellID = cellID;
    }

    public String getFlProduct() {
        return flProduct;
    }

    public void setFlProduct(String flProduct) {
        this.flProduct = flProduct;
    }

    public String getCellID() {
        return cellID;
    }

    public void setCellID(String cellID) {
        this.cellID = cellID;
    }

    @Override
    public String toString() {
        return "FlProductBindingInfo{" +
                "flProduct='" + flProduct + '\'' +
                ", cellID='" + cellID + '\'' +
                '}';
    }
}