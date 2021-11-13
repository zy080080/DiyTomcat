package com.zzy.diytomcat.http;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Response extends BaseResponse {
    // 返却するHTML文書を保存
    private StringWriter stringWriter;
    // getWriter()を提供，HttpServletResponseのようにresponse.getWriter().println()が書ける。
    // stringWriterを変数で渡すので，println()で書き込んだデータはstringWriterに入る。
    private PrintWriter printWriter;
    // レスポンスヘッダのContent-typeに対応，デフォルトは　text/html
    private String contentType;
    private byte[] body;
    private int status;
    private String redirectPath;

    private List<Cookie> cookies;

    public Response(){
        this.stringWriter = new StringWriter();
        this.printWriter = new PrintWriter(stringWriter);
        this.contentType = "text/html";
        this.cookies = new ArrayList<>();
    }

    public byte[] getBody() throws UnsupportedEncodingException{
        if(null == body){
            String content = stringWriter.toString();
            body = content.getBytes();
        }
        return body;
    }

    public String getCookiesHeader(){
        if(null == cookies) return "";
        String pattern = "EEE, d MMM yyy HH:mm:ss 'GMT'";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
        StringBuffer sb = new StringBuffer();
        for(Cookie cookie : getCookies()){
            sb.append("\r\n");
            sb.append("Set-Cookie: ");
            sb.append(cookie.getName() + "=" + cookie.getValue() + ";");
            if(-1 != cookie.getMaxAge()){// -1 indicates that the cookie will persist until browser shutdown.
                sb.append("Expires=");
                Date now = new Date();
                Date expire = DateUtil.offset(now, DateField.MINUTE, cookie.getMaxAge());
                sb.append(sdf.format(expire));
                sb.append(";");
            }
            if(null != cookie.getPath()){
                sb.append("Path=" + cookie.getPath());
            }
        }
        return sb.toString();
    }

    public String getContentType(){
        return contentType;
    }

    public void setContentType(String contentType){
        this.contentType = contentType;
    }

    public PrintWriter getWriter(){
        return printWriter;
    }

    public void setBody(byte[] body){
        this.body = body;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    public void addCookie(Cookie cookie){
        cookies.add(cookie);
    }

    public List<Cookie> getCookies(){
        return this.cookies;
    }

    public String getRedirectPath() {
        return redirectPath;
    }

    public void sendRedirect(String redirect) throws IOException {
        this.redirectPath = redirect;
    }
}
