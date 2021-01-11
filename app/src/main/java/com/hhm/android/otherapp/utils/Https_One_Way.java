package com.hhm.android.otherapp.utils;

import android.app.Application;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class Https_One_Way {

    private static final String KEY_STORE_TYPE_BKS = "bks"; // 服务器端
    private static final String KEY_STORE_TYPE_P12 = "PKCS12"; // 客户端
    public static final String BKS_STORE_PASSWORD = "123456";//BKS文件密码
    public static final String KEY_STORE_PASSWORD = "123456";//P12文件密码
    public static SSLSocketFactory sSLSocketFactory;
    public static X509TrustManager trustManager;

     /** 单向校验中SSLSocketFactory X509TrustManager 参数的生成
     * 通常单向校验一般都是服务器不校验客户端的真实性，客户端去校验服务器的真实性
     * @param context
     */
    public static SSLSocketFactory getSSLCertifcation_one(Context context) {
        try {
            InputStream bksStream = context.getAssets().open("xxxx.bks"); // 客户端信任的服务器端证书流
            // 客户端信任的服务器端证书
            KeyStore trustStore = KeyStore.getInstance(KEY_STORE_TYPE_BKS);
            try {
                trustStore.load(bksStream, BKS_STORE_PASSWORD.toCharArray()); // 加载客户端信任的服务器证书
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } finally {
                try {
                    bksStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
            trustManagerFactory.init(trustStore);
            trustManager = chooseTrustManager(trustManagerFactory.getTrustManagers()); // 生成用来校验服务器真实性的trustManager
            //SSLContext sslContext = SSLContext.getInstance("TLSv1", "AndroidOpenSSL");
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
             // 初始化SSLContext
            sSLSocketFactory = sslContext.getSocketFactory(); // 通过sslContext获取到SocketFactory
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sSLSocketFactory;
    }

    /**
     * 双向校验中SSLSocketFactory X509TrustManager 参数的生成
     * @param context
     */
    public static SSLSocketFactory getSSLCertifcation_two(Context context) {
        try {
            InputStream bksStream = context.getAssets().open("xxxx.bks"); // 客户端信任的服务器端证书流
            InputStream p12Stream = context.getAssets().open("xxxx.p12"); // 服务器需要验证的客户端证书流
            // 客户端信任的服务器端证书
            KeyStore trustStore = KeyStore.getInstance(KEY_STORE_TYPE_BKS);
            // 服务器端需要验证的客户端证书
            KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE_P12);
            try {
                trustStore.load(bksStream, BKS_STORE_PASSWORD.toCharArray()); // 加载客户端信任的服务器证书
                keyStore.load(p12Stream, KEY_STORE_PASSWORD.toCharArray()); // 加载服务器信任的客户端证书
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } finally {
                try {
                    bksStream.close();
                    p12Stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            SSLContext sslContext = SSLContext.getInstance("TLS");
            // TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
            trustManagerFactory.init(trustStore);
            trustManager = chooseTrustManager(trustManagerFactory.getTrustManagers()); // 生成用来校验服务器真实性的trustManager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
            keyManagerFactory.init(keyStore, KEY_STORE_PASSWORD.toCharArray()); // 生成服务器用来校验客户端真实性的KeyManager
            // 初始化SSLContext
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            sSLSocketFactory = sslContext.getSocketFactory(); // 通过sslContext获取到SocketFactory
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sSLSocketFactory;
    }

    private static X509TrustManager chooseTrustManager(TrustManager[] trustManagers) {
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        return null;
    }


    public static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

}
