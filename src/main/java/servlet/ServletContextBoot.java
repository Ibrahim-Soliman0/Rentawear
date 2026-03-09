package servlet;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import util.EnvLoaderUtil;
import util.JPAUtil;

public class ServletContextBoot implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        EnvLoaderUtil.load();
        System.out.println("Intialized Enviroment Variables...");
        JPAUtil.getEntityManagerFactory();
        System.out.println("Intialized JPA EntityManagerFactory...");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        EnvLoaderUtil.load();
        System.out.println("Cleared Enviroment Variables...");
        JPAUtil.close();
        System.out.println("Closed JPA EntityManagerFactory...");
    }
}
