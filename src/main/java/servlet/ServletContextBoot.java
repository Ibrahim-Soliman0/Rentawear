package servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import mapper.ProductMapper;
import mapper.ProductMapperImpl;
import repository.impl.*;
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

        // ── Repositories ──────────────────────────────────────────────────────
        ProductRepositoryImpl        productRepo      = new ProductRepositoryImpl();
        ProductVariantRepositoryImpl variantRepo      = new ProductVariantRepositoryImpl();
        ProductImageRepositoryImpl   imageRepo        = new ProductImageRepositoryImpl();
        CartItemRepositoryImpl       cartItemRepo     = new CartItemRepositoryImpl();
        UserCategoryRepositoryImpl   userCategoryRepo = new UserCategoryRepositoryImpl();

        // ── Services ──────────────────────────────────────────────────────────
        ProductService        productService  = new ProductService(productRepo);
        ProductVariantService variantService  = new ProductVariantService(variantRepo);
        ProductImageService   imageService    = new ProductImageService(imageRepo);
        CartItemService       cartItemService = new CartItemService(cartItemRepo); // built before facade
        CategoryService       categoryService = new CategoryService();

        ProductMapper mapper = new ProductMapperImpl();
        
        ProductFacadeService facade = new ProductFacadeService(
                productService,
                variantService,
                imageService,
                mapper,
                categoryService,
                cartItemService
        );
        System.out.println("[AppContextListener] Dependency graph initialized");

        // ── Publish to ServletContext ─────────────────────────────────────────
        ctx.setAttribute("productFacadeService", facade);

        UserCategoryService userCategoryService =
                new UserCategoryService(userCategoryRepo);
        ctx.setAttribute("userCategoryService", userCategoryService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        EnvLoaderUtil.unload();
        System.out.println("Cleared Environment Variables...");
        JPAUtil.close();
        System.out.println("Closed JPA EntityManagerFactory...");
    }
}