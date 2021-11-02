package com.zzy.diytomcat.test;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.zzy.diytomcat.util.MiniBrowser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestTomcat {
    private static int port = 18081;
    private static String ip = "127.0.0.1";

    @BeforeClass
    public static void beforeClass() {
        if (NetUtil.isUsableLocalPort(port)) {
            System.err.println("ユニットテストのために，" + port + "番号のポートを使用するDiyTomcatを起動してください。");
            System.exit(1);
        }else{
            System.out.println("DiyTomcatの起動状態を確認しました。ユニットテストを始めます。");
        }
    }

    @Test
    public void testHelloTomcat(){
        String html = getContentString("/");
        Assert.assertEquals(html, "Hello DIY Tomcat from zzy");
    }

    @Test
    public void testaHtml(){
        String html = getContentString("/a.html");
        Assert.assertEquals(html, "Hello DIY Tomcat from a.html");
    }

    @Test
    public void testTimeConsumeHtml() throws InterruptedException{
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20, 20, 60,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10));
        TimeInterval timeInterval = DateUtil.timer();

        for(int i = 0; i < 3; i++){
            threadPool.execute(new Runnable(){
                public void run(){
                    getContentString("/timeConsume.html");
                }
            });
        }
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);

        long duration = timeInterval.intervalMs();
        Assert.assertTrue(duration < 3000);
    }

    @Test
    public void testaIndex(){
//        String html = getContentString("/a/index.html");
        String html = getContentString("/a");
        Assert.assertEquals(html, "Hello DIY Tomcat from index.html@a");
    }

    @Test
    public void testbIndex(){
        String html = getContentString("/b");
        Assert.assertEquals(html, "Hello DIY Tomcat from index.html@b");
    }

    @Test
    public void test404(){
        String response = getHttpString("/not_exist.html");
        containAssert(response, "HTTP/1.1 404 Not Found");
    }

    @Test
    public void test500(){
        String response = getHttpString("/500.html");
        containAssert(response, "HTTP/1.1 500 Internal Server Error");
    }

    @Test
    public void testaTxt(){
        String response = getHttpString("/a.txt");
        containAssert(response, "Content-Type: text/plain");
    }

    @Test
    public void testPNG(){
        byte[] bytes = getContentBytes("/sample.png");
        int pngFileLength = 1157741;
        Assert.assertEquals(pngFileLength, bytes.length);
    }

    @Test
    public void testPDF() {
        byte[] bytes = getContentBytes("/guideline.pdf");
        int pngFileLength = 1029699;
        Assert.assertEquals(pngFileLength, bytes.length);
    }

    @Test
    public void testhello(){
        String html = getContentString("/j2ee/hello");
        Assert.assertEquals(html, "Hello DIY Tomcat from HelloServlet");
    }

    @Test
    public void testJavawebHello(){
        String html = getContentString("/javaweb/hello");
        containAssert(html, "Hello DIY Tomcat from HelloServlet@javaweb");
    }

    @Test
    public void testJavawebHelloSingleton(){
        String html1 = getContentString("/javaweb/hello");
        String html2 = getContentString("/javaweb/hello");
        Assert.assertEquals(html1, html2);
    }

    private byte[] getContentBytes(String uri){
        return getContentBytes(uri, false);
    }

    private byte[] getContentBytes(String uri, boolean zip){
        String url = StrUtil.format("http://{}:{}{}", ip, port, uri);
        return MiniBrowser.getContentBytes(url, false);
    }

    private String getContentString(String uri){
        String url = StrUtil.format("http://{}:{}{}", ip, port, uri);
        String content = MiniBrowser.getContentString(url);
        return content;
    }

    private String getHttpString(String uri){
        String url = StrUtil.format("http://{}:{}{}", ip, port, uri);
        String http = MiniBrowser.getHttpString(url);
        return http;
    }

    private void containAssert(String html, String string){
        boolean match = StrUtil.containsAny(html, string);
        Assert.assertTrue(match);
    }
}
