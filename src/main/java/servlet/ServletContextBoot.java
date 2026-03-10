package servlet;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import util.EnvLoaderUtil;
import util.JPAUtil;

public class ServletContextBoot implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        EnvLoaderUtil.load();
        System.out.println("Initialized Environment Variables...");
        JPAUtil.getEntityManagerFactory();
        System.out.println("Initialized JPA EntityManagerFactory...");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        EnvLoaderUtil.unload();
        System.out.println("Cleared Environment Variables...");
        JPAUtil.close();
        System.out.println("Closed JPA EntityManagerFactory...");
    }
}
