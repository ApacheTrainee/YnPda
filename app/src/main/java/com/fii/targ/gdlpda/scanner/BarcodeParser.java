package com.fii.targ.gdlpda.scanner;
import java.util.HashMap;
import java.util.Map;
public class BarcodeParser {

    /**
     * 解析标准格式的条码数据
     * 格式示例: "P1A52TAF00-600-G-A,Q30,M1A52TAF00-600-G-A,D20231225,L20231225,SSVN01000321730002"
     * @param barcodeData 条码字符串
     * @return 解析后的键值对数据
     */
    public static Map<String, String> parseBarcode(String barcodeData) {
        Map<String, String> result = new HashMap<>();
        if (barcodeData == null || barcodeData.isEmpty()) {
            return result;
        }

        // 按逗号分割字符串
        String[] segments = barcodeData.split(",");

        for (String segment : segments) {
            segment = segment.trim(); // 去除前后空格
            if (segment.isEmpty()) {
                continue;
            }

            // 处理不同类型的段
            try {
                if (segment.startsWith("P")) {
                    // 产品型号
                    result.put("productModel", segment.substring(1));
                } else if (segment.startsWith("Q")) {
                    // 数量
                    result.put("quantity", segment.substring(1));
                } else if (segment.startsWith("M")) {
                    // 物料编码
                    result.put("materialCode", segment.substring(1));
                } else if (segment.startsWith("D")) {
                    // 生产日期
                    result.put("productionDate", segment.substring(1));
                } else if (segment.startsWith("L")) {
                    // 失效日期
                    result.put("expiryDate", segment.substring(1));
                } else if (segment.startsWith("S")) {
                    // 序列号
                    result.put("serialNumber", segment.substring(1));
                } else {
                    // 未知类型，整段保存
                    result.put("unknown_" + System.currentTimeMillis(), segment);
                }
            } catch (Exception e) {
                // 防止解析单个段时出错导致整个解析失败
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 格式化日期字符串
     * @param dateStr 原始日期字符串 (如: 20231225)
     * @return 格式化后的日期 (如: 2023-12-25)
     */
    public static String formatDate(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) {
            return dateStr;
        }
        return dateStr.substring(0, 4) + "-" +
                dateStr.substring(4, 6) + "-" +
                dateStr.substring(6, 8);
    }
}
