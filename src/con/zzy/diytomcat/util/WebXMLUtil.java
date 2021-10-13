package con.zzy.diytomcat.util;

import cn.hutool.core.io.FileUtil;
import con.zzy.diytomcat.catalina.Context;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;

public class WebXMLUtil {
    public static String getWelcomeFile(Context context){
        String xml = FileUtil.readUtf8String(Constant.webXmlFile);
        Document d = Jsoup.parse(xml);
        Elements es = d.select("welcome-file");
        for(Element e : es){
            String welcomeFileName = e.text();
            File f = new File(context.getDocBase(), welcomeFileName);
            if(f.exists()) return f.getName();
        }
        return "index.html";
    }
}
