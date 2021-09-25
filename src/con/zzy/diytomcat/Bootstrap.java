package con.zzy.diytomcat;

import cn.hutool.core.util.NetUtil;
import con.zzy.diytomcat.http.Request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Bootstrap {
    public static void main(String[] args){
        try{
            int port = 18080;
            if(!NetUtil.isUsableLocalPort(port)){
                System.out.println("ポートがすでに使用されている。");
                return;
            }
            ServerSocket serverSocket = new ServerSocket(port);
            while(true){
                Socket socket = serverSocket.accept();
//                InputStream inputStream = socket.getInputStream();
//                int bufferSize = 1024;
//                byte[] buffer = new byte[bufferSize];
//                inputStream.read(buffer);
//                String requestString = new String(buffer, StandardCharsets.UTF_8);
                Request request = new Request(socket);
//                System.out.println("ブラウザインプット情報：\r\n" + requestString
//                );
                System.out.println("ブラウザインプット情報：\r\n" + request.getRequestString());
                System.out.println("uri:" + request.getUri());

                OutputStream outputStream = socket.getOutputStream();
                String response_head = "HTTP/1.1 200 OK\r\n" + "Content-Type:text/html\r\n\r\n";
                String responseString = "Hello DIY Tomcat from zzy";
                responseString = response_head + responseString;
                outputStream.write(responseString.getBytes());
                outputStream.flush();
                socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
