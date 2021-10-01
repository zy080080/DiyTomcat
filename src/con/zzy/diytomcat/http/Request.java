package con.zzy.diytomcat.http;

import cn.hutool.core.util.StrUtil;
import con.zzy.diytomcat.Bootstrap;
import con.zzy.diytomcat.catalina.Context;
import con.zzy.diytomcat.util.MiniBrowser;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Request {
    private String requestString;
    private String uri;
    private Socket socket;
    private Context context;

    public Request(Socket socket) throws IOException {
        this.socket = socket;
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

        context = Bootstrap.contextMap.get(path);
        if(null == context){
            context = Bootstrap.contextMap.get("/");
        }
    }

    private void parseHttpRequest() throws IOException{
        InputStream inputStream = this.socket.getInputStream();
        byte[] bytes = MiniBrowser.readBytes(inputStream);
        requestString = new String(bytes, "utf-8");
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
