package con.zzy.diytomcat.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;
import con.zzy.diytomcat.http.Request;
import con.zzy.diytomcat.http.Response;
import con.zzy.diytomcat.util.Constant;
import con.zzy.diytomcat.util.ThreadPoolUtil;
import con.zzy.diytomcat.util.WebXMLUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class Server {
    private Service service;

    public Server() {
        this.service = new Service(this);
    }

    public void start() {
        logJVM();
        init();
    }

    private void init() {
        try {
            int port = 18080;
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Request request = new Request(socket, service);
                            System.out.println("ブラウザインプット情報：\r\n" + request.getRequestString());
                            System.out.println("uri:" + request.getUri());

                            Context context = request.getContext();

                            // ヘッダーとメッセージボディを分ける理由：
                            // これからの作業でヘッダに対して複雑な処理を行う。ボディーに対しても二進のファイルやgzip圧縮の処理を行うため。
                            Response response = new Response();
                            String uri = request.getUri();
                            if (null == uri) return;
                            System.out.println(uri);

                            if ("/500.html".equals(uri)) {
                                throw new Exception("this is a deliberately created exception");
                            }

                            if ("/".equals(uri))
                                uri = WebXMLUtil.getWelcomeFile(request.getContext());

                            String fileName = StrUtil.removePrefix(uri, "/");
                            File file = FileUtil.file(context.getDocBase(), fileName);
                            if (file.exists()) {
                                String fileContent = FileUtil.readUtf8String(file);
                                response.getWriter().println(fileContent);

                                if (fileName.equals("timeConsume.html")) {
                                    // この後のマルチスレッドのために用意したもの，
                                    // 実際の場合はWebページにアクセスするときに，データベースに接続など，時間がかかる作業がある。
                                    // ここでtimeConsume.htmlにアクセスするために1秒かかると想定
                                    ThreadUtil.sleep(1000);
                                }
                            } else {
                                handle404(socket, uri);
                                return;
                            }

                            handle200(socket, response);
                        } catch (Exception e) {
                            LogFactory.get().error(e);
                            handle500(socket, e);
                        } finally {
                            try {
                                if (!socket.isClosed()) socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                ThreadPoolUtil.run(r);
            }
        } catch (IOException e) {
            LogFactory.get().error(e);
            e.printStackTrace();
        }
    }

    private static void logJVM() {
        Map<String, String> infos = new LinkedHashMap<>();
        infos.put("Server version", "zzy DiyTomcat/1.0.1");
        infos.put("Server built", "2021-09-26 15:28:13");
        infos.put("Server number", "1.0.1");
        infos.put("OS Name\t", SystemUtil.get("os.name"));
        infos.put("OS Version", SystemUtil.get("os.version"));
        infos.put("Architecture", SystemUtil.get("os.arc"));
        infos.put("Java Home", SystemUtil.get("java.home"));
        infos.put("JVM Version", SystemUtil.get("java.runtime.version"));
        infos.put("JVM Vendor", SystemUtil.get("java.vm.specification.vendor"));

        Set<String> keys = infos.keySet();
        for (String key : keys) {
            LogFactory.get().info(key + "\t\t" + infos.get(key));
        }
    }

    private static void handle200(Socket socket, Response response) throws IOException {
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
            StackTraceElement[] stackTraceElement = e.getStackTrace();
            StringBuffer sb = new StringBuffer();
            sb.append(e.toString());
            sb.append("\r\n");
            for (StackTraceElement ste : stackTraceElement) {
                sb.append("\t");
                sb.append(ste.toString());
                sb.append("\r\n");
            }

            // 表示しやすいように19文字まで
            String message = e.getMessage();
            if (null != message && message.length() > 20) {
                message = message.substring(0, 19);
            }

            String text = StrUtil.format(Constant.textFormat_500, message, e.toString(), sb.toString());
            text = Constant.response_head_500 + text;
            byte[] responseBytes = text.getBytes("utf-8");
            os.write(responseBytes);
        } catch (IOException e1) {
            e.printStackTrace();
        }

    }
}
