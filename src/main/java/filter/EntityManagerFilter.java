package filter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import util.EntityManagerContext;
import util.JPAUtil;

import java.io.IOException;

@WebFilter("/*")
public class EntityManagerFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        EntityManager em = JPAUtil.getEntityManager();

        EntityManagerContext.set(em);

        HttpServletRequest httpReq = (HttpServletRequest) request;

        boolean writeRequest = !httpReq.getMethod().equalsIgnoreCase("GET");

        EntityTransaction tx = null;

        try {
            if (writeRequest) {
                tx = em.getTransaction();
                tx.begin();
            }

            chain.doFilter(request, response);

            if (writeRequest) {
                tx.commit();
            }

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new ServletException(e);
        } finally {
            em.close();
            EntityManagerContext.clear();
        }
    }
}