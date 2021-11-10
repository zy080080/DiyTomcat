package com.zzy.diytomcat.catalina;

import cn.hutool.log.LogFactory;
import com.zzy.diytomcat.http.Request;
import com.zzy.diytomcat.http.Response;
import com.zzy.diytomcat.util.ThreadPoolUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Connector implements Runnable {
    int port;
    private Service service;

    // compression=on:gzip圧縮を有効にする
    private String compression;
    // 出力が圧縮される前に最小限のデータ量を指定する
    private int compressionMinSize;
    // 圧縮を使用しないHTTPクライアント
    private String noCompressionUserAgents;
    // HTTP圧縮が使用されるMIMEタイプ
    private String compressableMimeType;

    public Connector(Service service) {
        this.service = service;
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
                            Request request = new Request(s, Connector.this);
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

    public Service getService() {
        return service;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    public int getCompressionMinSize() {
        return compressionMinSize;
    }

    public void setCompressionMinSize(int compressionMinSize) {
        this.compressionMinSize = compressionMinSize;
    }

    public String getNoCompressionUserAgent() {
        return noCompressionUserAgents;
    }

    public void setNoCompressionUserAgents(String noCompressionUserAgents) {
        this.noCompressionUserAgents = noCompressionUserAgents;
    }

    public String getCompressableMimeType() {
        return compressableMimeType;
    }

    public void setCompressableMimeType(String compressableMimeType) {
        this.compressableMimeType = compressableMimeType;
    }
}
