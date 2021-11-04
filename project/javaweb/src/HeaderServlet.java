import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HeaderServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            String userAgent = req.getHeader("User-Agent");
            resp.getWriter().println(userAgent);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
