package com.fii.targ.gdlpda.scanner;

import static com.fii.targ.gdlpda.scanner.BarcodeParser.parseBarcode;

import android.util.Log;

import java.util.Map;
import java.util.Objects;

public class ScannerParser {
    private static String TAG = "ScannerParser";
    public static String StdBarcodeParser (String data) throws IllegalArgumentException {
        if (data == null || data.length() != 14) {
            throw new IllegalArgumentException("Invalid data length");
        }

        String xValue = data.substring(0, 6);
        String middle = data.substring(6, 8);
        String yValue = data.substring(8, 14);


        if (!"DH".equals(middle)) {
            throw new IllegalArgumentException("Invalid middle characters");
        }

        // 保留后6位
        return yValue;
    }

    public static String GroundCodeParser (String data) throws IllegalArgumentException {
        if (data == null ) {
            throw new IllegalArgumentException("Invalid data length");
        }

        Log.d(TAG, "data: " + data);
        if (data.length() == 3) {
           return data;
        }

        if (data.startsWith("STA") || data.startsWith("BUF")) {
           return data;
        }

        Log.d(TAG, "Serial Number not found in data");
        throw new IllegalArgumentException("Serial Number not found in data");

    }

    public static String vnGroundCodeParser (String data) throws IllegalArgumentException {
        Log.d(TAG, "Raw groundCode:" + data);
        String letterPart = data.substring(0, 2);
        //String numberPart = data.substring(2);
        if (data.length() >= 3 && data.length() <= 10 && letterPart.matches("CX|ZZ|CY|JG")) {
            Log.d(TAG, "data: " + data);
            return data;
        } else {
            data = "";
            Log.d(TAG, "没找到对应的地码");
            throw new IllegalArgumentException("没找到对应的地码");
        }
    }


    public static String L10CartBarcodeParser (String data) throws IllegalArgumentException {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Invalid data");
        }
        if (data.startsWith("AGV")) {
            return data;
        }
        String[] lines = data.split("\n");
        for (String line : lines) {
            if (line.startsWith("Serial Number")) {
                return line.split(":")[1].trim();
            }
        }
        Log.d(TAG, "Serial Number not found in data");
        throw new IllegalArgumentException("Serial Number not found in data");
        
    }
    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        try {
            // 若想限制数值范围在 int 区间，可使用 Integer.parseInt
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public static String vnCartBarcodeParser (String data) throws IllegalArgumentException {
        Log.d(TAG, "Raw cartCode:" + data);
        if (data.length() <= 2 && isInteger(data)) {
            return data;
        } else {
            data = "";
            Log.d(TAG, "没找到对应的货架码");
            throw new IllegalArgumentException("没找到对应的货架码");
        }
    }

    public static String vnProductBarcodeParser (String data) throws IllegalArgumentException {
        Map<String, String> productSN = parseBarcode(data);
        Log.d(TAG, "Raw productModel:" + productSN.get("productModel"));
        if (Objects.equals(productSN.get("productModel"), null)) {
            throw new IllegalArgumentException("无效产品SN");
            // return "";
        } else {
            String productCode = productSN.get("productModel");
            Log.d(TAG, "productModel:" + productCode);
            return productCode;
        }
    }

    public static String ProductBarcodeParser (String data) throws IllegalArgumentException {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Invalid length");
        }

        String productSN = "";
        String[] lines = data.split("\n");
        for (String line : lines) {
            if (line.startsWith("Data")) {
                productSN = line.split(":")[1].trim();
                if (productSN.length() != 16 || !data.startsWith("P") || !data.startsWith("R")) {
                    throw new IllegalArgumentException("Invalid data");
                }
                return productSN;
            }
        }

        return data;
    }

    //M1128940-001
    public static String L11ProductBarcodeParser (String data) throws IllegalArgumentException {
        if (data == null || data.length() != 12) {
            throw new IllegalArgumentException("Invalid data");
        }

        if (data.startsWith("M")) {
            return data;
        }

        throw new IllegalArgumentException("Format error");
    }

    //R185543530002039
    public static String L10ProductBarcodeParser (String data) throws IllegalArgumentException {
        if (data == null || data.length() != 16) {
            throw new IllegalArgumentException("Invalid data");
        }

        if (data.startsWith("R")) {
            return data;
        }

       throw new IllegalArgumentException("Format error");
        
    }
}
