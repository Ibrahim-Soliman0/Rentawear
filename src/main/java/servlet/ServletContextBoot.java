package servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import mapper.ProductMapper;
import mapper.ProductMapperImpl;
import repository.impl.ProductImageRepositoryImpl;
import repository.impl.ProductRepositoryImpl;
import repository.impl.ProductVariantRepositoryImpl;
import service.*;
import util.EnvLoaderUtil;
import util.JPAUtil;

public class ServletContextBoot implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        EnvLoaderUtil.load();
        System.out.println("Initialized Environment Variables...");
        JPAUtil.getEntityManagerFactory();
        System.out.println("Initialized JPA EntityManagerFactory...");
        ServletContext ctx = sce.getServletContext();

        ProductRepositoryImpl productRepo = new ProductRepositoryImpl();
        ProductVariantRepositoryImpl variantRepo = new ProductVariantRepositoryImpl();
        ProductImageRepositoryImpl imageRepo   = new ProductImageRepositoryImpl();

        ProductService productService = new ProductService(productRepo);
        ProductVariantService variantService = new ProductVariantService(variantRepo);
        ProductImageService imageService   = new ProductImageService(imageRepo);
        ProductMapper mapper = new ProductMapperImpl();
        CategoryService categoryService = new CategoryService();

        ProductFacadeService facade = new ProductFacadeService(
                productService, variantService, imageService, mapper,categoryService);
        System.out.println("[AppContextListener] Dependency graph initialized");
        ctx.setAttribute("productFacadeService", facade);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        EnvLoaderUtil.unload();
        System.out.println("Cleared Environment Variables...");
        JPAUtil.close();
        System.out.println("Closed JPA EntityManagerFactory...");
    }
}
