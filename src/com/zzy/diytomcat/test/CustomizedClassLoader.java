package com.zzy.diytomcat.test;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.lang.reflect.Method;

public class CustomizedClassLoader extends ClassLoader{
    private File classesFolder = new File(System.getProperty("user.dir"), "classes_4_test");

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] data = loadClassData(name);
        return defineClass(name, data, 0, data.length);
    }

    private byte[] loadClassData(String fullQualifiedName) throws ClassNotFoundException{
        String fileName = StrUtil.replace(fullQualifiedName, ".", "/") + ".class";
        File classFile = new File(classesFolder, fileName);
        if(!classFile.exists()){
            throw new ClassNotFoundException(fullQualifiedName);
        }

        return FileUtil.readBytes(classFile);
    }

    public static void main(String[] args) throws Exception{
        CustomizedClassLoader loader = new CustomizedClassLoader();
        Class<?> zzy = loader.loadClass("com.zzy.diytomcat.test.ZZY");
        Object o = zzy.getDeclaredConstructor().newInstance();
        Method m = zzy.getMethod("hello");
        m.invoke(o);
        System.out.println(zzy.getClassLoader());

        CustomizedClassLoader loader2 = new CustomizedClassLoader();
        Class<?> zzy2 = loader2.loadClass("com.zzy.diytomcat.test.ZZY");
        System.out.println(zzy == zzy2);
        // 一つのJVMにおいて，一つのクラスローダーの下では，一つのクラスに対応するクラスオブジェクトは一つしかない
        // 複数のクラスローダーによって同じクラスをロードした場合，それぞれが異なるクラスオブジェクトとなる。

        /*
        parent delegation
        クラスをロードするときに，まずBootstrap Class Loaderから探し，なかったらExtension Class Loader，Application Class Loader，
        最後にUser Class Loaderの順になっている。
        詳しく：https://programmer.help/blogs/jvm-series-parent-delegation-model-for-java-class-loading-mechanism.html
         */
    }
}
