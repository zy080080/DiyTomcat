package con.zzy.diytomcat.util;

import cn.hutool.system.SystemUtil;

import java.io.File;

public class Constant {
    // レスポンスヘッダメッセージテンプレート
    public final static String response_head_202 = "HTTP/1.1 200 OK\r\n" + "Content-Type: {}\r\n\r\n";
    // String System.getProperty("user.dir")　実行時のパスを動的に取得  user.dir: working directory
    public final static File webappsFolder = new File(SystemUtil.get("user.dir"),"webapps");
    public final static File rootFolder = new File(webappsFolder, "ROOT");

}
