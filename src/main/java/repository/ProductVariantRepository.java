package repository;

import entity.ProductVariant;

import java.util.List;

public interface ProductVariantRepository extends Repository<ProductVariant> {

    // All variants for a product — used by PDP and QV size selectors
    List<ProductVariant> findByProductId(int productId);

    // Distinct color strings — used by upload validation to confirm
    // a color exists before accepting image uploads for it
    List<String> findDistinctColorsByProductId(int productId);

    // Sizes for one specific color — drives the size selector when user
    // switches color in QV or PDP (only show sizes available for that color)
    List<String> findSizesByProductIdAndColor(int productId, String color);


    // Deletes all size rows for a color — called by deleteColorImages in service
    void deleteByProductIdAndColor(int productId, String color);
}
