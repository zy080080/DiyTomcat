package com.zzy.diytomcat.http;

import cn.hutool.core.util.StrUtil;
import com.zzy.diytomcat.catalina.Context;
import com.zzy.diytomcat.catalina.Engine;
import com.zzy.diytomcat.catalina.Service;
import com.zzy.diytomcat.util.MiniBrowser;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Request extends BaseRequest {
    // RequestをServletに渡すためにHttpServletRequestを継承する
    private String requestString;
    private String uri;
    private Socket socket;
    private Context context;
    private Service service;
    private String method;

    public Request(Socket socket, Service service) throws IOException {
        this.socket = socket;
        this.service = service;
        parseHttpRequest();
        if(StrUtil.isEmpty(requestString)) return;
        parseUri();
        parseContext();
        parseMethod();

        // uri="/a/index.html" -> uri="/index"
        if(!"/".equals(context.getPath())){
            uri = StrUtil.removePrefix(uri, context.getPath());
            if(StrUtil.isEmpty(uri)) uri = "/";
        }
    }

    private void parseContext(){
        Engine engine = service.getEngine();
        context = engine.getDefaultHost().getContext(uri);
        if(null != context) return;
        String path = StrUtil.subBetween(uri,"/","/");
        if(null == path){
            path = "/";
        }else{
            path = "/" + path;
        }

        context = engine.getDefaultHost().getContext(path);
        if(null == context){
            context = engine.getDefaultHost().getContext("/");
        }
    }

    private void parseHttpRequest() throws IOException{
        InputStream inputStream = this.socket.getInputStream();
        // HTTP1.1のデフォルトでは継続的接続のため，trueにするとreadBytes中のwhile文が止まらなくなる。
        byte[] bytes = MiniBrowser.readBytes(inputStream, false);
        requestString = new String(bytes, "utf-8");
        /**
         * 例：
         * GET /b/index.html HTTP/1.1
         * Host: 127.0.0.1:18080
         * Upgrade-Insecure-Requests: 1
         * Accept: text/html,application/xhtml+xml,application/xml;q=0.9,asterisk/asterisk;q = 0.8
         * User - Agent:Mozilla / 5.0 (Macintosh; Intel Mac OS X 10_15_7)AppleWebKit / 605.1 .15 (KHTML, like
            Gecko)Version / 14.1 .2 Safari / 605.1 .15
         * Accept - Language:zh - cn
         * Accept - Encoding:gzip, deflate
         * Connection:keep - alive
         */
    }

    private void parseUri(){
        String temp;

        temp = StrUtil.subBetween(requestString, " ", " ");
        if(!StrUtil.contains(temp, '?')){
            uri = temp;
            return;
        }
        temp = StrUtil.subBefore(temp, '?', false);
        uri = temp;
    }

    public ServletContext getServletContext(){
        return context.getServletContext();
    }

    public String getRealPath(String path){
        return getServletContext().getRealPath(path);
    }

    private void parseMethod(){
        method = StrUtil.subBefore(requestString," ",false);
    }

    public Context getContext() {
        return context;
    }

    public String getUri(){
        return uri;
    }

    public String getRequestString(){
        return requestString;
    }

    @Override
    public String getMethod() {
        return method;
    }
}
