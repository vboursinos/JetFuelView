package demo;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Created by Deepak on 27/07/2018.
 */
public class Launcher {
    public static void main(String[] args) {
        System.out.println("Server starting...");

        ServletContextHandler contextHandler =
                new ServletContextHandler(null, "/", true, false);
        contextHandler.setSessionHandler(new SessionHandler());
        contextHandler.addServlet(new ServletHolder(AppServlet.class), "/*");

        Server embeddedServer = new Server(8080);
        embeddedServer.setHandler(contextHandler);

        try {
            embeddedServer.start();
            embeddedServer.join();
        } catch (Exception e) {
            System.err.println("Server error:\n" + e);
        }
        System.out.println("Server stopped");
    }
}
