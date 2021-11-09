package com.zzy.diytomcat.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.zzy.diytomcat.http.Request;
import com.zzy.diytomcat.http.Response;
import com.zzy.diytomcat.http.StandardSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

public class SessionManager {
    private static Map<String, StandardSession> sessionMap = new HashMap<>();
    private static int defaultTimeout = getTimeout();
    static{
        // クラスがロードされた時に実行される。コンストラクタよりも優先的に呼ばれる。
        // 30分ごとにcheckOutDateSessionを呼び出すメソッド
        startSessionOutDateCheckThread();
    }

    public static HttpSession getSession(String jsessionid, Request request, Response response){
        if(null == jsessionid) {
            return newSession(request, response);
        }else{
            StandardSession currentSession = sessionMap.get(jsessionid);
            if(null == currentSession){
                return newSession(request, response);
            }else{
                currentSession.setLastAccessedTime(System.currentTimeMillis());
                createCookieBySession(currentSession, request, response);
                return currentSession;
            }
        }
    }

    private static void createCookieBySession(HttpSession session, Request request, Response response){
        Cookie cookie = new Cookie("JSESSIONID", session.getId());
        cookie.setMaxAge((session.getMaxInactiveInterval()));
        cookie.setPath(request.getContext().getPath());
        response.addCookie(cookie);
    }

    private static HttpSession newSession(Request request, Response response){
        ServletContext servletContext = request.getServletContext();
        String sid = generateSessionId();
        StandardSession session = new StandardSession(sid, servletContext);
        session.setMaxInactiveInterval(defaultTimeout);
        sessionMap.put(sid, session);
        createCookieBySession(session, request, response);
        return session;
    }

    private static int getTimeout(){
        int defaultResult = 30;
        try{
            Document d = Jsoup.parse(Constant.webXmlFile, "utf-8");
            Elements es = d.select("session-config session-timeout");
            if(es.isEmpty()) return defaultResult;
            return Convert.toInt(es.get(0).text());
        }catch(IOException e){
            return defaultResult;
        }
    }

    private static void checkOutDateSession(){
        Set<String> jsessionids = sessionMap.keySet();
        List<String> outdateJsessionIds = new ArrayList<>();

        for(String jsessionid : jsessionids){
            StandardSession session = sessionMap.get(jsessionid);
            long interval = System.currentTimeMillis() - session.getLastAccessedTime();
            if(interval > session.getMaxInactiveInterval() * 1000){
                outdateJsessionIds.add(jsessionid);
            }
        }

        for(String jsessionid : outdateJsessionIds){
            sessionMap.remove(jsessionid);
        }
    }

    private static void startSessionOutDateCheckThread(){
        new Thread(){
            @Override
            public void run() {
                while(true){
                    checkOutDateSession();
                    ThreadUtil.sleep(1000 * 30 * 60);
                }
            }
        }.start();
    }

    public static synchronized String generateSessionId(){
        String result = null;
        byte[] bytes = RandomUtil.randomBytes(16);
        result = new String(bytes);
        result = SecureUtil.md5(result);
        result = result.toUpperCase();
        return result;
    }
}
