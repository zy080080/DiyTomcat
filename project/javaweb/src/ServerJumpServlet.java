import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServerJumpServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            req.setAttribute("name", "zzy redirect");
            req.getRequestDispatcher("hello.jsp").forward(req, resp);
        }catch(ServletException e){
            e.printStackTrace();
        }
    }
}
