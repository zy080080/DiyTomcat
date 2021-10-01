package con.zzy.diytomcat.util;

import cn.hutool.system.SystemUtil;

import java.io.File;

public class Constant {
    // レスポンスヘッダメッセージテンプレート
    public static final String response_head_202 = "HTTP/1.1 200 OK\r\n" + "Content-Type: {}\r\n\r\n";
    // String System.getProperty("user.dir")　実行時のパスを動的に取得  user.dir: working directory
    public static final File webappsFolder = new File(SystemUtil.get("user.dir"), "webapps");
    public static final File rootFolder = new File(webappsFolder, "ROOT");

    public static final File confFolder = new File(SystemUtil.get("user.dir"), "conf");
    public static final File serverXmlFile = new File(confFolder, "server.xml");
}
