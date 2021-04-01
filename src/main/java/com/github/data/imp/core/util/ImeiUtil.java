package com.github.data.imp.core.util;

public class ImeiUtil {

    /**
     * 14位构建
     * @param imei
     * @return
     */
    private static String getImei15(String imei) {
        if (imei.length() == 14) {
            char[] imeiChar = imei.toCharArray();
            int resultInt = 0;
            for (int i = 0; i < imeiChar.length; i++) {
                int a = Integer.parseInt(String.valueOf(imeiChar[i]));
                i++;
                final int temp = Integer.parseInt(String.valueOf(imeiChar[i])) * 2;
                final int b = temp < 10 ? temp : temp - 9;
                resultInt += a + b;
            }
            resultInt %= 10;
            resultInt = resultInt == 0 ? 0 : 10 - resultInt;
            return resultInt + "";
        } else {
            return null;
        }
    }


}


