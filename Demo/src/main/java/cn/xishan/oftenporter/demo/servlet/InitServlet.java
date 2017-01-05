package cn.xishan.oftenporter.demo.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.PropertyConfigurator;

@WebServlet(loadOnStartup = 1)
public class InitServlet extends HttpServlet {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException {
        super.init();
        PropertyConfigurator
                .configure(getClass().getResourceAsStream("/log4j.properties"));
    }

}
