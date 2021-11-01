package com.zzy.diytomcat.http;

import com.zzy.diytomcat.catalina.Context;

import java.io.File;
import java.util.*;

// Interface ServletContext:
// Defines a set of methods that a servlet uses to communicate with its servlet container,
// for example, to get the MIME type of a file, dispatch requests, or write to a log file.
// ServletContextは，getRealPath()を提供するだけではなく，servlet filterなどのコアコンポーネントの重要な要素であり，後の機能を開発するのに重要である。
public class ApplicationContext extends BaseServletContext{
    private Map<String, Object> attributesMap;
    private Context context;

    public ApplicationContext(Context context){
        this.attributesMap = new HashMap<>();
        this.context = context;
    }

    public void removeAttribute(String name){
        attributesMap.remove(name);
    }

    public void setAttribute(String name, Object value){
        attributesMap.put(name, value);
    }

    public Object getAttribute(String name){
        return attributesMap.get(name);
    }

    public Enumeration<String> getAttributeName(){
        Set<String> keys = attributesMap.keySet();
        return Collections.enumeration(keys);
    }

    public String getRealPath(String path) {
        return new File(context.getDocBase(), path).getAbsolutePath();
    }
}
