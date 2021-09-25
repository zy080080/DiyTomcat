package con.zzy.diytomcat;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import con.zzy.diytomcat.http.Request;
import con.zzy.diytomcat.http.Response;
import con.zzy.diytomcat.util.Constant;

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
                Request request = new Request(socket);
                System.out.println("ブラウザインプット情報：\r\n" + request.getRequestString());
                System.out.println("uri:" + request.getUri());
                // ヘッダーとメッセージボディを分ける理由：
                // これからの作業でヘッダに対して複雑な処理を行う。ボディーに対しても二進のファイルやgzip圧縮の処理を行うため。
                Response response = new Response();
                String html = "Hello DIY Tomcat from zzy";
                response.getWriter().println(html);

                handle200(socket, response);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private static void handle200(Socket socket, Response response) throws IOException{
        String contentType = response.getContentType();
        String headText = Constant.response_head_202;
        headText = StrUtil.format(headText, contentType);
        byte[] head = headText.getBytes();
        byte[] body = response.getBody();
        byte[] responseBytes = new byte[head.length + body.length];

        ArrayUtil.copy(head, 0, responseBytes, 0, head.length);
        ArrayUtil.copy(body, 0, responseBytes, head.length, body.length);

        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(responseBytes);

        // close()で自動的にflush()
        socket.close();
    }
}
