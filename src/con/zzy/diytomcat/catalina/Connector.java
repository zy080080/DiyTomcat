package con.zzy.diytomcat.catalina;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import con.zzy.diytomcat.http.Request;
import con.zzy.diytomcat.http.Response;
import con.zzy.diytomcat.util.Constant;
import con.zzy.diytomcat.util.ThreadPoolUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Connector implements Runnable {
    int port;
    private Service service;

    public Connector(Service service) {
        this.service = service;
    }

    public Service getService() {
        return service;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(port);

            while (true) {
                Socket s = ss.accept();
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Request request = new Request(s, service);
                            Response response = new Response();
                            HttpProcessor processor = new HttpProcessor();
                            processor.execute(s, request, response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (!s.isClosed())
                                try {
                                    s.close();
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

    public void init() {
        LogFactory.get().info("Initializing ProtocolHandler [http-bio-{}]", port);
    }

    public void start() {
        LogFactory.get().info("Starting ProtocolHandler [http-bio-{}", port);
        new Thread(this).start();
    }
}
