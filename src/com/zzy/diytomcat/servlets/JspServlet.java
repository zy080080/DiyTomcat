package com.zzy.diytomcat.servlets;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.zzy.diytomcat.catalina.Context;
import com.zzy.diytomcat.http.Request;
import com.zzy.diytomcat.http.Response;
import com.zzy.diytomcat.util.Constant;
import com.zzy.diytomcat.util.JspUtil;
import com.zzy.diytomcat.util.WebXMLUtil;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class JspServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static JspServlet instance = new JspServlet();

    public static synchronized JspServlet getInstance() {
        return instance;
    }

    private JspServlet() {
    }

    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        try {
            Request request = (Request) httpServletRequest;
            Response response = (Response) httpServletResponse;

            String uri = request.getRequestURI();
            if ("/".equals(uri)) {
                uri = WebXMLUtil.getWelcomeFile(request.getContext());
            }

            String fileName = StrUtil.removePrefix(uri, "/");
            File file = FileUtil.file(request.getRealPath(fileName));

            File jspFile = file;
            // コンパイルタイミング
            if (jspFile.exists()) {
                Context context = request.getContext();
                String path = context.getPath();
                String subFolder;
                if ("/".equals(path)) {
                    // "_"はworkディレクトリを表す
                    subFolder = "_";
                } else {
                    subFolder = StrUtil.subAfter(path, "/", false);
                }
                String servletClassPath = JspUtil.getServletClassPath(uri, subFolder);
                File jspServletClassFile = new File(servletClassPath);
                if (!jspServletClassFile.exists()) {
                    // まだコンパイルされていないときはコンパイルする
                    JspUtil.compileJsp(context, jspFile);
                } else if (jspFile.lastModified() > jspServletClassFile.lastModified()) {
                    // jspファイルが改修されたときはリコンパイルする
                    JspUtil.compileJsp(context, jspFile);
                }

                String extName = FileUtil.extName(file);
                String mimeType = WebXMLUtil.getMimeType(extName);
                response.setContentType(mimeType);

                byte[] body = FileUtil.readBytes(file);
                response.setBody(body);
                response.setStatus(Constant.CODE_200);

            } else {
                response.setStatus(Constant.CODE_404);
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
