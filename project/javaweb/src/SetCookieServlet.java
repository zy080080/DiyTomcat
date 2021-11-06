import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SetCookieServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            Cookie c = new Cookie("name", "zhiyong(cookie)");
            c.setMaxAge(60 * 24 * 60);
            c.setPath("/");
            resp.addCookie(c);
            resp.getWriter().println("set cookie successfully!");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
