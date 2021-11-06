import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GetCookieServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Cookie[] cookies = req.getCookies();
            if (null != cookies) {
                for (int d = 0; d <= cookies.length - 1; d++) {
                    resp.getWriter().print(cookies[d].getName() + ":" + cookies[d].getValue() + "<br>");
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
