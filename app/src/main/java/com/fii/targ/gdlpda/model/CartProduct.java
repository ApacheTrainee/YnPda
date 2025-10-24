package com.fii.targ.gdlpda.model;

import com.fii.targ.gdlpda.ui.TrolleyProduct;

import java.util.ArrayList;
import java.util.List;

public class CartProduct {

    private String productSN;
    private String position;

    public CartProduct(String position, String productSN) {
        this.productSN = productSN;
        this.position = position;
    }

    public String getProductSN() {
        return this.productSN;
    }

    public String getPosition() {
        return this.position;
    }
}
