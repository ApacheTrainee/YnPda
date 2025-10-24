package com.fii.targ.gdlpda.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import com.fii.targ.gdlpda.conn.ApiResponse;
import com.fii.targ.gdlpda.conn.HttpClient;
import com.fii.targ.gdlpda.model.CartBindingInfo;
import com.fii.targ.gdlpda.model.FlProductBindingInfo;
import com.fii.targ.gdlpda.model.GroundBindingInfo;
import com.fii.targ.gdlpda.model.QueryProductResponseBody;
import com.fii.targ.gdlpda.ui.TrolleyProduct;
import com.google.gson.Gson;

import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import com.fii.targ.gdlpda.model.CartProduct;

public class BindOperation {
    private static final String PREFS_NAME = "QueryPrefs";
    private static final String KEY_STATION = "station";
    public static boolean bindGround(String cartCode, String storageCode) {
        try {
            BindPositionRequestBody requestBody = new BindPositionRequestBody(cartCode, storageCode);
            ApiResponse<BindPositionResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.BIND_GROUND, requestBody, BindPositionResponseBody.class);
            return response.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean bindGround(String cartCode, String storageCode,String productCode, String boxSize) {
        try {
            BindPositionRequestBody requestBody = new BindPositionRequestBody(cartCode, storageCode,productCode, boxSize);
            ApiResponse<BindPositionResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.BIND_GROUND, requestBody, BindPositionResponseBody.class);
            return response.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    
    public static boolean unbindGround(String cartCode) {
        try {
            UnBindPositionRequestBody requestBody = new UnBindPositionRequestBody(cartCode);
            ApiResponse<BindPositionResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.UNBIND_GROUND, requestBody, BindPositionResponseBody.class);
            return response.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean bindFlProduct(String cellID, String productSn, String materialNum, int number) {
        try {
            BindFlProductRequestBody requestBody = new BindFlProductRequestBody(cellID, productSn, materialNum,number);
            ApiResponse<BindPositionResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.BIND_FL_PRODUCT, requestBody, BindPositionResponseBody.class);
            return response.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean unbindFlProduct(String cellID) {
        try {
            UnBindFlProductRequestBody requestBody = new UnBindFlProductRequestBody(cellID);
            ApiResponse<BindPositionResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.UNBIND_FL_PRODUCT, requestBody, BindPositionResponseBody.class);
            return response.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean bindProducts(String cartCode, List<CartProduct> cartProducts) {
        try {
            BindProductRequestBody requestBody = new BindProductRequestBody(cartCode, cartProducts);
            ApiResponse<BindProductResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.BIND_PRODUCTS, requestBody, BindProductResponseBody.class);
            return response.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean unbindProducts(String cartCode) {
        try {
            UnBindProductRequestBody requestBody = new UnBindProductRequestBody(cartCode);
            ApiResponse<BindProductResponseBody> response = HttpClient.post(Constants.getBaseUrl() + Constants.UNBIND_PRODUCTS, requestBody, BindProductResponseBody.class);
            return response.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    public static List<CartProduct> convertToCartProductList(List<TrolleyProduct> trolleyProducts) {
        List<CartProduct> cartProducts = new ArrayList<>();
        if (trolleyProducts == null) return cartProducts;
        for (TrolleyProduct trolleyProduct : trolleyProducts) {
            cartProducts.add(new CartProduct(trolleyProduct.getPosition(), trolleyProduct.getProductSN()));
        }
        return cartProducts;
    }

    public static CartBindingInfo queryCartBindingInfo(String cartCode) {
        try {
            QueryCartBindingInfoRequest requestBody = new QueryCartBindingInfoRequest(cartCode);
            ApiResponse<QueryCartBindingInfoResponse> response = HttpClient.post(Constants.getBaseUrl() + Constants.QUERY_BINDING, requestBody, QueryCartBindingInfoResponse.class);
            return response.getData().getData();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static QueryGroundBindingInfoResponse queryGroundBindingInfo(String groundCode, Context context) {
        try {
            QueryGroundBindingInfoRequest requestBody = new QueryGroundBindingInfoRequest(groundCode);
            ApiResponse<QueryGroundBindingInfoResponse> response = HttpClient.post(Constants.getBaseUrl() + Constants.QUERY_GROUND_BINDING, requestBody, QueryGroundBindingInfoResponse.class);
            saveStation(response.getData().getStation(), context);
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            QueryGroundBindingInfoResponse errorResponse = new QueryGroundBindingInfoResponse();
            errorResponse.setCode("500");
            errorResponse.setMessage(e.getMessage());
            return null;
        }
    }

    private static void saveStation(String station, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_STATION, station);
        editor.apply();
    }


    public static FlProductBindingInfo queryFlProductBindingInfo(String cellID) {
        try {
            QueryFlproductBindingInfoRequest requestBody = new QueryFlproductBindingInfoRequest(cellID);
            ApiResponse<QueryFlproductBindingInfoResponse> response = HttpClient.post(Constants.getBaseUrl() + Constants.QUERY_FL_BINDING, requestBody, QueryFlproductBindingInfoResponse.class);
            Log.d("queryFlProductBindingInfo", "" + response);
            return response.getData().getData();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        

    }

}



class QueryFlproductBindingInfoRequest {
    private String cellID;

    public QueryFlproductBindingInfoRequest(String cellID) {
        this.cellID = cellID;
    }

}



class QueryFlproductBindingInfoResponse {
    private String code;
    private String message;
    private FlProductBindingInfo data;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public FlProductBindingInfo getData() {
        return data;
    }

}

class QueryCartBindingInfoRequest {
    private String cartCode;

    public QueryCartBindingInfoRequest(String cartCode) {
        this.cartCode = cartCode;
    }

    public String getCartCode() {
        return cartCode;
    }
}


class QueryCartBindingInfoResponse {
    private String code;
    private String message;
    private CartBindingInfo data;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public CartBindingInfo getData() {
        return data;
    }

}

class QueryGroundBindingInfoRequest {
    private String groundCode;

    public QueryGroundBindingInfoRequest(String groundCode) {
        this.groundCode = groundCode;
    }

    public String getGroundCode() {
        return groundCode;
    }
}


class BindRequestBody {
    private String cartCode;
    private List<CartProduct> cartProducts;

    public BindRequestBody(String cartCode, List<CartProduct> cartProducts) {
        this.cartCode = cartCode;
        this.cartProducts = cartProducts;
    }
}


    class BindResponseBody {
    private String code;
    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

class BindProductRequestBody {
    private String cartCode;
    private List<CartProduct> cartProducts;

    public BindProductRequestBody(String cartCode, List<CartProduct> cartProducts) {
        this.cartCode = cartCode;
        this.cartProducts = cartProducts;
    }
}


class BindProductResponseBody {
    private String code;
    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

class UnBindProductRequestBody {
    private String cartCode;

    public UnBindProductRequestBody(String cartCode) {
        this.cartCode = cartCode;
    }
}

class BindPositionRequestBody {
    private String cartCode;
    //private String storageCode;
    private String cellID;

    private String cartProductSN ;
    private String boxSize ;

    //public BindPositionRequestBody(String cartCode, String storageCode) {
    public BindPositionRequestBody(String cartCode, String cellID) {
        this.cartCode = cartCode;
        //this.storageCode = storageCode;
        this.cellID = cellID;
    }
    public BindPositionRequestBody(String cartCode, String cellID, String productID, String boxSize) {
        this.cartCode = cartCode;
        //this.storageCode = storageCode;
        this.cellID = cellID;
        this.cartProductSN = productID;
        this.boxSize = boxSize;
    }
}

class BindFlProductRequestBody {
    private String flProductSN;
    private String cellID;
    private String materialNum;
    private int number;

    public BindFlProductRequestBody(String cellID, String flProductSN, String materialNum, int number) {
        this.flProductSN = flProductSN;
        this.cellID = cellID;
        this.materialNum = materialNum;
        this.number = number;
    }
}

class UnBindFlProductRequestBody {
    private String cellID;

    public UnBindFlProductRequestBody(String cellID) {
        this.cellID = cellID;
    }
}

class UnBindPositionRequestBody {
    private String cartCode;

    public UnBindPositionRequestBody(String cartCode) {
        this.cartCode = cartCode;
    }
}
class BindPositionResponseBody {
    private String code;
    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}


