package com.hhm.android.otherapp.utils;

import android.content.Context;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class Https_No_Way {

    /**
     * 默认信任所有的证书
     */
    public static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory sslSocketFactory = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{createTrustAllManager()}, new SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslSocketFactory;
    }


    public static X509TrustManager createTrustAllManager() {
        X509TrustManager tm = null;
        try {
            tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                    //do nothing，接受任意客户端证书
                }
                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                    //do nothing，接受任意服务端证书
                }
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tm;
    }

    public static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    /*信任所有证书
    .sslSocketFactory(Https_No_Way.createSSLSocketFactory(), Https_No_Way.createTrustAllManager())
    .hostnameVerifier(new Https_No_Way.TrustAllHostnameVerifier())*/

    /***
     * 读取*.cer公钥证书文件， 获取公钥证书信息
     * @author hc
     */
    // 证书版本:3
    // 证书序列号:27c392de
    // 证书生效日期:2020/12/02
    // 证书失效日期:2045/11/26
    // 证书拥有者:CN=Frat
    // 证书颁发者:CN=Frat
    // 证书签名算法:SHA256withRSA
    public static void testReadX509CerFile(Context context){
        try {
            // 读取证书文件
            InputStream inStream = context.getAssets().open("insertFrat.cer");
            // 创建X509工厂类
            //CertificateFactory cf = CertificateFactory.getInstance("X.509");
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            // 创建证书对象
            X509Certificate oCert = (X509Certificate) cf.generateCertificate(inStream);
            inStream.close();
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd");
            String info = null;
            // 获得证书版本
            info = String.valueOf(oCert.getVersion());
            System.out.println("证书版本:" + info);
            // 获得证书序列号
            info = oCert.getSerialNumber().toString(16);
            System.out.println("证书序列号:" + info);
            // 获得证书有效期
            Date beforedate = oCert.getNotBefore();
            info = dateformat.format(beforedate);
            System.out.println("证书生效日期:" + info);
            Date afterdate = oCert.getNotAfter();
            info = dateformat.format(afterdate);
            System.out.println("证书失效日期:" + info);
            // 获得证书主体信息
            info = oCert.getSubjectDN().getName();
            System.out.println("证书拥有者:" + info);
            // 获得证书颁发者信息
            info = oCert.getIssuerDN().getName();
            System.out.println("证书颁发者:" + info);
            // 获得证书签名算法名称
            info = oCert.getSigAlgName();
            System.out.println("证书签名算法:" + info);
        } catch (Exception e) {
            System.out.println("解析证书出错！");
            e.printStackTrace();
        }
    }

}
