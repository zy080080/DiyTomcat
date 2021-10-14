package con.zzy.diytomcat.http;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

public class Response {
    // 返却するHTML文書を保存
    private StringWriter stringWriter;
    // getWriter()を提供，HttpServletResponseのようにresponse.getWriter().println()が書ける。
    // stringWriterを変数で渡すので，println()で書き込んだデータはstringWriterに入る。
    private PrintWriter printWriter;
    // レスポンスヘッダのContent-typeに対応，デフォルトは　text/html
    private String contentType;

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

    public byte[] getBody() throws UnsupportedEncodingException{
        String content = stringWriter.toString();
        return content.getBytes("utf-8");
    }
}
