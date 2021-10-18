package con.zzy.diytomcat.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import con.zzy.diytomcat.http.Request;
import con.zzy.diytomcat.http.Response;
import con.zzy.diytomcat.util.Constant;
import con.zzy.diytomcat.util.WebXMLUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class HttpProcessor {
    public void execute(Socket s, Request request, Response response){
        try {
            String uri = request.getUri();
            if(null == uri) return;

            Context context = request.getContext();

            if("/500.html".equals(uri)){
                throw new Exception("this is a deliberately created exception");
            }
            if("/".equals(uri)){
                uri = WebXMLUtil.getWelcomeFile(request.getContext());
            }

            String fileName = StrUtil.removePrefix(uri, "/");
            File file = FileUtil.file(context.getDocBase(), fileName);

            if(file.exists()){
                String extName = FileUtil.extName(file);
                String mimeType = WebXMLUtil.getMimeType(extName);
                response.setContentType(mimeType);

                byte[] body = FileUtil.readBytes(file);
                response.setBody(body);

                if(fileName.equals("timeConsume.html")){
                    ThreadUtil.sleep(1000);
                }
            }else{
                handle404(s, uri);
                return;
            }
            handle200(s, response);
        } catch (Exception e) {
            LogFactory.get().error(e);
            handle500(s, e);
        }finally{
            try{
                if(!s.isClosed()) s.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    private static void handle200(Socket s, Response response) throws IOException {
        String contentType = response.getContentType();
        String headText = Constant.response_head_202;
        headText = StrUtil.format(headText, contentType);

        byte[] head = headText.getBytes();
        byte[] body = response.getBody();
        byte[] responseBytes = new byte[head.length + body.length];

        ArrayUtil.copy(head, 0, responseBytes, 0, head.length);
        ArrayUtil.copy(body, 0, responseBytes, head.length, body.length);

        OutputStream os = s.getOutputStream();
        os.write(responseBytes);
    }

    protected void handle404(Socket s, String uri) throws IOException {
        OutputStream os = s.getOutputStream();
        String responseText = StrUtil.format(Constant.textFormat_404, uri, uri);
        responseText = Constant.response_head_404 + responseText;
        byte[] responseByte = responseText.getBytes("utf-8");
        os.write(responseByte);
    }

    protected void handle500(Socket s, Exception e) {
        try {
            OutputStream os = s.getOutputStream();
            StackTraceElement stes[] = e.getStackTrace();
            StringBuffer sb = new StringBuffer();
            sb.append(e.toString());
            sb.append("\r\n");
            for (StackTraceElement ste : stes) {
                sb.append("\t");
                sb.append(ste.toString());
                sb.append("\r\n");
            }

            String message = e.getMessage();

            if (null != message && message.length() > 20) {
                message = message.substring(0, 19);
            }

            String text = StrUtil.format(Constant.textFormat_500, message, e.toString(), sb.toString());
            text = Constant.response_head_500 + text;
            byte[] responseBytes = text.getBytes("utf-8");
            os.write(responseBytes);
        }catch(IOException e1){
            e1.printStackTrace();
        }
    }
}
