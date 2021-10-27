package com.zzy.diytomcat;

import com.zzy.diytomcat.catalina.*;
import com.zzy.diytomcat.classloader.CommonClassLoader;

import java.lang.reflect.Method;

public class Bootstrap {

    public static void main(String[] args) throws Exception{
        CommonClassLoader commonClassLoader = new CommonClassLoader();
        Thread.currentThread().setContextClassLoader(commonClassLoader);
        String serverClassName = "com.zzy.diytomcat.catalina.Server";
        Class<?> serverClass = commonClassLoader.loadClass(serverClassName);
        Object serverObject = serverClass.newInstance();
        Method m = serverClass.getMethod("start");
        m.invoke(serverObject);
        System.out.println(serverClass.getClassLoader());
        /**
         * Bootstrap Class Loader
         * Extension Class Loader
         * Application Class Loader
         * ここからはTomcatのクラスローダー
         * (Catalina Class Loader　%tomcat_home%/catalina/)  Common Class Loader　%tomcat_home%/lib中のクラスとjarをロード  (Share Class Loader　%tomcat_home%/share)　
         * Webapp Class Loader 　Webアプリをロードするため，app/WEB-INF/classes,/WEB_INF/lib
         * Jsp Class Loader
         */
    }
}
