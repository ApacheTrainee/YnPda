package com.fii.targ.gdlpda.model;

import com.fii.targ.gdlpda.ui.TrolleyProduct;
import com.google.gson.Gson;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CartBindingInfo {

    private String groundCode;
    private List<CartProduct> cartProducts;

    public String getGroundCode() {
        return groundCode;
    }

    public List<CartProduct> getCartProducts() {
        return cartProducts;
    }

    public List<TrolleyProduct> convertToTrolleyProductList(List<CartProduct> cartProducts) {
        List<TrolleyProduct> trolleyProducts = new ArrayList<>();
        if (cartProducts == null) return trolleyProducts;
        for (CartProduct cartProduct : cartProducts) {
            trolleyProducts.add(new TrolleyProduct(cartProduct.getPosition(), cartProduct.getProductSN()));
        }
        return trolleyProducts;
    }
}
