package com.zzy.diytomcat.util;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MiniBrowser {
    public static void main(String[] args) {
        String url = "http://static.how2j.cn/diytomcat.html";
        String contentString = getContentString(url, false);
        System.out.println(contentString);
        String httpString = getHttpString(url,false);
        System.out.println(httpString);
    }

    public static byte[] getContentBytes(String url){
        return getContentBytes(url, false);
    }

    public static String getContentString(String url){
        return getContentString(url, false);
    }

    /**
     *
     * @param url
     * @param gzip 圧縮されたデータを取得か
     * @return 文字列型のHTTPレスポンスのコンテンツ部分
     */
    public static String getContentString(String url, boolean gzip){
        byte[] result = getContentBytes(url, gzip);
        if(null == result){return null;}
        try{
            return new String(result, "utf-8").trim();
        }catch(UnsupportedEncodingException e){
            return null;
        }
    }

    /**
     *
     * @param url
     * @param gzip　圧縮されたデータを取得か
     * @return byte配列のHTTPレスポンスのコンテンツ部分
     */
    public static byte[] getContentBytes(String url, boolean gzip){
        byte[] response = getHttpBytes(url, gzip);
        byte[] doubleReturn = "\r\n\r\n".getBytes();

        /**
         * HTTPレスポンスのメッセージ構成
         * ステータス行　HTTP/1.1　200　OK\r\n
         * メッセージヘッダ（複数存在）　Content-type: text/html\r\n
         * 空白行 \r\n
         * メッセージボディ　<html><head>hello<head/><body>hello world</body><html/>
         */

        int pos = -1;
        for(int i = 0; i < response.length - doubleReturn.length; i++){
            byte[] temp = Arrays.copyOfRange(response, i, i + doubleReturn.length);
            if(Arrays.equals(temp, doubleReturn)){
                pos = i;
                break;
            }
        }
        if(-1 == pos){return null;}

        pos += doubleReturn.length;
        return Arrays.copyOfRange(response, pos, response.length);
    }

    public static String getHttpString(String url, boolean gzip){
        byte[] bytes = getHttpBytes(url, gzip);
        return new String(bytes).trim();
    }

    /**
     *
     * @param url
     * @return 文字列型のHTTPレスポンスデータ
     */
    public static String getHttpString(String url){
        return getHttpString(url, false);
    }

    /**
     * @param url
     * @param gzip 圧縮されたデータを取得か
     * @return byte配列のHTTPレスポンスデータ
     */
    public static byte[] getHttpBytes(String url, boolean gzip){
        byte[] result = null;
        try{
            URL u = new URL(url);
            Socket clinet = new Socket();
            int port = u.getPort();
            if(-1 == port){
                port = 80;
            }
            InetSocketAddress inetSocketAddress = new InetSocketAddress(u.getHost(), port);
            clinet.connect(inetSocketAddress, 1000);
            Map<String, String> requestHeaders = new HashMap<>();

            requestHeaders.put("Host", u.getHost() + ":" + port);
            requestHeaders.put("Accept", "text/html");
            requestHeaders.put("Connection", "close");
            requestHeaders.put("User-Agent", "zzy mini browser / java1.8");

            if(gzip){
                requestHeaders.put("Accept-Encoding", "gzip");
            }

            String path = u.getPath();
            if(path.length() == 0){
                path = "/";
            }

            String firstLine = "GET " + path + " HTTP/1.1\r\n";

            StringBuffer httpRequestString = new StringBuffer();
            httpRequestString.append(firstLine);
            Set<String> headers = requestHeaders.keySet();
            for(String header : headers){
                String headerLine = header + ":" + requestHeaders.get(header) + "\r\n";
                httpRequestString.append(headerLine);
            }

            PrintWriter printWriter = new PrintWriter(clinet.getOutputStream(), true);
            printWriter.println(httpRequestString);
            InputStream inputStream = clinet.getInputStream();

//            int buffer_size = 1024;
//
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            byte buffer[] = new byte[buffer_size];
//            while(true) {
//                int length = inputStream.read(buffer);
//                if(-1 == length){break;}
//                baos.write(buffer, 0, length);
//                if(length != buffer_size){break;}
//            }

//            result = baos.toByteArray();
            result = readBytes(inputStream, true);
            clinet.close();
        }catch(Exception e){
            e.printStackTrace();
            try{
                result = e.toString().getBytes("utf-8");
            }catch (UnsupportedEncodingException e1){
                e1.printStackTrace();
            }
        }

        return result;
    }

    public static byte[] readBytes(InputStream inputStream, boolean fully) throws IOException {
        int buffer_size = 1024;
        byte[] buffer = new byte[buffer_size];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while(true){
            int length = inputStream.read(buffer);
            if(-1 == length) break;
            baos.write(buffer, 0, length);
            // fullyがtrueのときに読み込んだデータのサイズがbuffer_sizeより短くても続く。
            // 大きなファイルを転送するときに一回1024バイトで送らない場合もあるため。
            if(!fully && length != buffer_size) break;
        }

        return baos.toByteArray();
    }
}
