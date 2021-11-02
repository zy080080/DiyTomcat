import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HelloServlet extends HttpServlet {
    public HelloServlet(){
        System.out.println(this + " Constructor");
    }

    public void init(ServletConfig config){
        String author = config.getInitParameter("author");
        String site = config.getInitParameter("site");

        System.out.println(this + " init()");
        System.out.println("got parameter author: " + author);
        System.out.println("got parameter site: " + site);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            System.out.println(this + " doGet()");
            resp.getWriter().println("Hello DIY Tomcat from HelloServlet@javaweb" + this);

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void destroy() {
        System.out.println(this + " destroy()");
    }
}
