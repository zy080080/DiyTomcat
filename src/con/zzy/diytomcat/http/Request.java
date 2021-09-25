package con.zzy.diytomcat.http;

import cn.hutool.core.util.StrUtil;
import con.zzy.diytomcat.util.MiniBrowser;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Request {
    private String requestString;
    private String uri;
    private Socket socket;

    public Request(Socket socket) throws IOException {
        this.socket = socket;
        parseHttpRequest();
        if(StrUtil.isEmpty(requestString)) return;
        parseUri();
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

    public String getUri(){
        return uri;
    }

    public String getRequestString(){
        return requestString;
    }
}
