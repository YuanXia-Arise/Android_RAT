package com.hhm.android.otherapp;

/*
异或，密钥 KEY
 */
public class XorUtil {
    public static final String TAG = "XorUtil";

    private static final String KEY = "FIRETECH";
    private static final byte[] KEY_BYTES = KEY.getBytes();
    private static final int LENGTH = KEY_BYTES.length;

    public static String xor(String message) {
        byte[] origin = message.getBytes();
        byte[] master = new byte[origin.length];
        for (int i = 0, len = origin.length; i < len; i++) {
            master[i] = (byte) (origin[i] ^ KEY_BYTES[i % LENGTH]);
        }
        //System.out.println(new String(master));
        return new String(master);
    }

    public static String[] xorArray(String[] messages){
        String[] resultStrArray = new String[messages.length];
        for (int i=0;i<messages.length;i++){
            resultStrArray[i] = xor(messages[i]);
        }
        return resultStrArray;
    }
}
