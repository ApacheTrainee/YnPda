package com.fii.targ.gdlpda.ui;


public class TrolleyProduct {
   private String position;
   private String productSN;

   public TrolleyProduct(String position, String productSN) {
      this.position = position;
      this.productSN = productSN;
   }

   public String getPosition() {
      return position;
   }

   public String getProductSN() {
      return productSN;
   }

   public void setProductSN(String productSN) {
      this.productSN = productSN;
   }

}