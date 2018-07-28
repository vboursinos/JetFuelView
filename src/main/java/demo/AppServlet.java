package demo;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

import javax.servlet.annotation.WebServlet;

/**
 * Created by Deepak on 27/07/2018.
 */
@WebServlet(urlPatterns = "/*", name = "AppServlet")
@VaadinServletConfiguration(ui = AppUI.class, productionMode = false)
public class AppServlet extends VaadinServlet {
}
