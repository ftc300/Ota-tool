package inshow.carl.com.csd.csd.core;

import inshow.carl.com.csd.tools.L;

/**
 * Created by chendong on 2018/7/30.
 */

public class ConvertDataMgr
{



    public static int getCurrentTime(byte[] b) {
        return (b[0] & 0x0FF) + ((b[1] & 0x0FF) << 8) + ((b[2] & 0x0FF) << 16) + ((b[3] & 0x0FF) << 24);
    }


    public static byte[] setCurrentTime(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0x0FF);
        src[2] = (byte) ((value >> 16) & 0x0FF);
        src[1] = (byte) ((value >> 8) & 0x0FF);
        src[0] = (byte) (value & 0x0FF);
        return src;
    }

    public static int getCurrentStep(byte[] b) {
//        int[] ret = new int[2];
//        ret[0] = (b[0] & 0x0FF) + ((b[1] & 0x0FF) << 8) + ((b[2] & 0x0FF) << 16) + ((b[3] & 0x0FF) << 24);
//        ret[1] = (b[4] & 0x0FF) + ((b[5] & 0x0FF) << 8) + ((b[6] & 0x0FF) << 16) + ((b[7] & 0x0FF) << 24);
        return (b[0] & 0x0FF) + ((b[1] & 0x0FF) << 8) + ((b[2] & 0x0FF) << 16) + ((b[3] & 0x0FF) << 24);
    }

    public static String getFirmVersion(){
        return "";
    }

    public static byte[] setStepData(int value){
        byte[] src = new byte[1];
        src[0] = (byte) (value & 0x0FF);
        return src;
    }

    public static byte[] setTimeData(int value){
        byte[] src = new byte[1];
        src[0] = (byte) (value & 0x0FF);
        return src;
    }

    public static byte[] setVibrateData(){
        byte[] src = new byte[1];
        src[0] = (byte) (2 & 0x0FF);
        return src;
    }

    public static byte[] setControlData(int[] value) {
        byte[] src = new byte[4];
        src[3] = (byte) (value[3] & 0x0FF);
        src[2] = (byte) (value[2] & 0x0FF);
        src[1] = (byte) (value[1] & 0x0FF);
        src[0] = (byte) (value[0] & 0x0FF);
        return src;
    }


    public static byte[] set0E(){
        byte[] src = new byte[4];
        src[0] = 0;
        src[1] = 0x0E;
        src[2] = 0;
        src[3] = 0;
        return src;
    }

    public static int getSumPress(byte[] b){
        return (b[0] & 0x0FF) + ((b[1] & 0x0FF) << 8) + ((b[2] & 0x0FF) << 16) + ((b[3] & 0x0FF) << 24);
    }

    public static String bytes2Char(byte[] bytes) {
        if (bytes == null) return "";
        String result = "";
        for (int i = 0; i < bytes.length; i++) {
             char x = (char) bytes[i];
             result += x;
        }
        return result;
    }

    public static int[] getPowerConsumption(byte[] b) {
        int[] result = new int[4];
        result[0] = b[0] & 0x0FF;
        result[1] = (b[1] & 0x0FF) + ((b[2] & 0x0FF) << 8) + ((b[3] & 0x0FF) << 16) + ((b[4] & 0x0FF) << 24);
        result[2] = b[5] & 0x0FF;
        result[3] =(b[6] & 0x0FF) + ((b[7] & 0x0FF) << 8) + ((b[8] & 0x0FF) << 16) + ((b[9] & 0x0FF) << 24);
        return result;
    }

    public static String bytes2HexString(byte[] bytes) {
        if (bytes == null) return "";
        String result = "";
        for (int i = 0; i < bytes.length; i++) {
            String hexString = Integer.toHexString(bytes[i] & 0x0FF);
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result += hexString.toUpperCase();
        }
        return result;
    }




}
