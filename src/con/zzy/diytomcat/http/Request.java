package con.zzy.diytomcat.http;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.OS;
import con.zzy.diytomcat.Bootstrap;
import con.zzy.diytomcat.catalina.Context;
import con.zzy.diytomcat.catalina.Engine;
import con.zzy.diytomcat.catalina.Host;
import con.zzy.diytomcat.catalina.Service;
import con.zzy.diytomcat.util.MiniBrowser;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Request {
    private String requestString;
    private String uri;
    private Socket socket;
    private Context context;
    private Service service;

    public Request(Socket socket, Service service) throws IOException {
        this.socket = socket;
        this.service = service;
        parseHttpRequest();
        if(StrUtil.isEmpty(requestString)) return;
        parseUri();
        parseContext();

        // uri="/a/index.html" -> uri="/index"
        if(!"/".equals(context.getPath())){
            uri = StrUtil.removePrefix(uri, context.getPath());
        }
    }

    private void parseContext(){
        String path = StrUtil.subBetween(uri,"/","/");
        if(null == path){
            path = "/";
        }else{
            path = "/" + path;
        }

        context = service.getEngine().getDefaultHost().getContext(path);
        if(null == context){
            context = service.getEngine().getDefaultHost().getContext("/");
        }
    }

    private void parseHttpRequest() throws IOException{
        InputStream inputStream = this.socket.getInputStream();
        byte[] bytes = MiniBrowser.readBytes(inputStream);
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

    public Context getContext() {
        return context;
    }

    public String getUri(){
        return uri;
    }

    public String getRequestString(){
        return requestString;
    }
}
