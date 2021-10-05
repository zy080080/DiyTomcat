package con.zzy.diytomcat;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;
import con.zzy.diytomcat.catalina.Context;
import con.zzy.diytomcat.catalina.Engine;
import con.zzy.diytomcat.catalina.Host;
import con.zzy.diytomcat.http.Request;
import con.zzy.diytomcat.http.Response;
import con.zzy.diytomcat.util.Constant;
import con.zzy.diytomcat.util.ServerXMLUtil;
import con.zzy.diytomcat.util.ThreadPoolUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Bootstrap {
    public static Map<String, Context> contextMap = new HashMap<>();

    public static void main(String[] args) {
        try {
            logJVM();

            Engine engine = new Engine();
            int port = 18080;
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Request request = new Request(socket, engine);
                            System.out.println("ブラウザインプット情報：\r\n" + request.getRequestString());
                            System.out.println("uri:" + request.getUri());

                            Context context = request.getContext();

                            // ヘッダーとメッセージボディを分ける理由：
                            // これからの作業でヘッダに対して複雑な処理を行う。ボディーに対しても二進のファイルやgzip圧縮の処理を行うため。
                            Response response = new Response();
                            String uri = request.getUri();
                            if (null == uri) return;
                            System.out.println(uri);

                            if ("/".equals(uri)) {
                                String html = "Hello DIY Tomcat from zzy";
                                response.getWriter().println(html);
                            } else {
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
                                    response.getWriter().println("File Not Found");
                                }
                            }

                            handle200(socket, response);
                        } catch (IOException e) {
                            e.printStackTrace();
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

    private static void loadContext(File folder){
        String path = folder.getName();
        if("ROOT".equals(path)){
            path = "/";
        }else{
            path = "/" + path;
        }
        String docBase = folder.getAbsolutePath();
        Context context = new Context(path, docBase);
        contextMap.put(context.getPath(), context);
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
}
