package con.zzy.diytomcat.http;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
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

    public Response(){
        this.stringWriter = new StringWriter();
        this.printWriter = new PrintWriter(stringWriter);
        this.contentType = "text/html";
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

    public byte[] getBody() throws UnsupportedEncodingException{
        if(null == body){
            String content = stringWriter.toString();
            body = content.getBytes();
        }
        return body;
    }
}
