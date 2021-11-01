package com.zzy.diytomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.zzy.diytomcat.classloader.WebappClassLoader;
import com.zzy.diytomcat.exception.WebConfigDuplicatedException;
import com.zzy.diytomcat.http.ApplicationContext;
import com.zzy.diytomcat.util.ContextXMLUtil;
import com.zzy.diytomcat.watcher.ContextFileChangeWatcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.*;

public class Context {
    private String path;
    private String docBase;
    private File contextWebXmlFile;
    private Host host;
    private boolean reloadable;
    private ServletContext servletContext;

    private Map<String, String> url_servletClassName;
    private Map<String, String> url_servletName;
    private Map<String, String> servletName_className;
    private Map<String, String> className_servletName;

    private WebappClassLoader webappClassLoader;
    private ContextFileChangeWatcher contextFileChangeWatcher;

    public Context(String path, String docBase, Host host, boolean reloadable) {
        TimeInterval timeInterval = DateUtil.timer();
        this.path = path;
        this.docBase = docBase;
        this.host = host;
        this.reloadable = reloadable;
        this.contextWebXmlFile = new File(docBase, ContextXMLUtil.getWatchedResource());
        this.servletContext = new ApplicationContext(this);

        this.url_servletClassName = new HashMap<>();
        this.url_servletName = new HashMap<>();
        this.servletName_className = new HashMap<>();
        this.className_servletName = new HashMap<>();

        ClassLoader commonClassLoader = Thread.currentThread().getContextClassLoader();
        this.webappClassLoader = new WebappClassLoader(docBase, commonClassLoader);

        LogFactory.get().info("Deploying web application directory {}", this.docBase);
        deploy();
        LogFactory.get().info("Deployment of web application directory {} has finished in {} ms", this.docBase,timeInterval.intervalMs());
    }

    private void deploy() {
        TimeInterval timeInterval = DateUtil.timer();
        init();
        if(reloadable){
            contextFileChangeWatcher = new ContextFileChangeWatcher(this);
            contextFileChangeWatcher.start();
        }
    }

    private void init() {
        if (!contextWebXmlFile.exists()) return;

        // web.xmlの解析
        try {
            checkDuplicated();
        } catch (WebConfigDuplicatedException e) {
            e.printStackTrace();
            return;
        }

        String xml = FileUtil.readUtf8String(contextWebXmlFile);
        Document d = Jsoup.parse(xml);
        parseServletMapping(d);
    }

    public void stop(){
        webappClassLoader.stop();
        contextFileChangeWatcher.stop();
    }

    public void reload(){
        host.reload(this);
    }

    private void parseServletMapping(Document d) {
        Elements mappingurlElements = d.select("servlet-mapping url-pattern");
        for (Element mappingurlElement : mappingurlElements) {
            String urlPattern = mappingurlElement.text();
            String servletName = mappingurlElement.parent().select("servlet-name").first().text();
            url_servletName.put(urlPattern, servletName);
        }

        Elements servletNameElements = d.select("servlet servlet-name");
        for (Element servletNameElement : servletNameElements) {
            String servletName = servletNameElement.text();
            String servletClass = servletNameElement.parent().select("servlet-class").first().text();
            servletName_className.put(servletName, servletClass);
            className_servletName.put(servletClass, servletName);
        }

        Set<String> urls = url_servletName.keySet();
        for(String url : urls){
            String servletName = url_servletName.get(url);
            String servletClassName = servletName_className.get(servletName);
            url_servletClassName.put(url, servletClassName);
        }
    }

    private void checkDuplicated(Document d, String mapping, String desc) throws WebConfigDuplicatedException{
        Elements elements = d.select(mapping);
        List<String> contents = new ArrayList<>();
        for(Element e : elements){
            contents.add(e.text());
        }

        Collections.sort(contents);
        for(int i = 0; i < contents.size() - 1; i++){
            String contentPre = contents.get(i);
            String contentNext = contents.get(i + 1);
            if(contentPre.equals(contentNext)){
                throw new WebConfigDuplicatedException(StrUtil.format(desc, contentPre));
            }
        }
    }

    private void checkDuplicated() throws WebConfigDuplicatedException{
        String xml = FileUtil.readUtf8String(contextWebXmlFile); // WEB-INF/web.xml
        Document d = Jsoup.parse(xml);

        checkDuplicated(d, "servlet-mapping url-pattern", "同じservlet urlが複数あります：{}");
        checkDuplicated(d, "servlet servlet-name", "同じservletが複数あります：{}");
        checkDuplicated(d, "servlet servlet-class", "同じservlet classが複数あります：{}");
    }

    public boolean isReloadable(){
        return reloadable;
    }

    public void setReloadable(boolean reloadable) {
        this.reloadable = reloadable;
    }

    public String getServletClassName(String uri){
        return url_servletClassName.get(uri);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDocBase() {
        return docBase;
    }

    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }

    public WebappClassLoader getWebappClassLoader() {
        return webappClassLoader;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }
}
