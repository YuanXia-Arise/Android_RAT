package com.hhm.android.otherapp.utils;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;


/**
 * HTTPS 双向认证
 */
public class Https_Two_Way {
    private final static String CLIENT_PRI_KEY = "client.bks";
    private final static String TRUSTSTORE_PUB_KEY = "truststore.bks";
    private final static String CLIENT_BKS_PASSWORD = "123456";
    private final static String TRUSTSTORE_BKS_PASSWORD = "123456";
    private final static String KEYSTORE_TYPE = "BKS";
    private final static String PROTOCOL_TYPE = "TLS";
    private final static String CERTIFICATE_FORMAT = "X509";

    public static SSLSocketFactory getSSLCertifcation(Context context) {
        SSLSocketFactory sslSocketFactory = null;
        try {
             // 服务器端需要验证的客户端证书，其实就是客户端的keystore
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE); // 客户端信任的服务器端证书
            KeyStore trustStore = KeyStore.getInstance(KEYSTORE_TYPE); // 读取证书
            InputStream ksIn = context.getAssets().open(CLIENT_PRI_KEY);
            InputStream tsIn = context.getAssets().open(TRUSTSTORE_PUB_KEY); // 加载证书
            keyStore.load(ksIn, CLIENT_BKS_PASSWORD.toCharArray());
            trustStore.load(tsIn, TRUSTSTORE_BKS_PASSWORD.toCharArray());
            ksIn.close();
            tsIn.close();
            SSLContext sslContext = SSLContext.getInstance(PROTOCOL_TYPE); // 初始化SSLContext
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(CERTIFICATE_FORMAT);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(CERTIFICATE_FORMAT);
            trustManagerFactory.init(trustStore);
            keyManagerFactory.init(keyStore, CLIENT_BKS_PASSWORD.toCharArray());
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (KeyStoreException | IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return sslSocketFactory;
    }


    private class UnSafeHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true; // 自行添加判断逻辑，true->Safe，false->unsafe
        }
    }

    /*OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .sslSocketFactory(SSLHelper.getSSLCertifcation(context)) // 获取SSLSocketFactory
            .hostnameVerifier(new UnSafeHostnameVerifier()) // 添加hostName验证器
            .build();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(url) // 填写自己服务器IP
            .addConverterFactory(GsonConverterFactory.create()) // 添加 json 转换器
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create()) // 添加 RxJava 适配器
            .client(okHttpClient)
            .build();*/
}
