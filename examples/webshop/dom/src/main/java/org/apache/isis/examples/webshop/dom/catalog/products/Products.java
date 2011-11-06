package org.apache.isis.examples.webshop.dom.catalog.products;

import java.util.List;

import org.apache.isis.applib.annotation.Exploration;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.QueryOnly;
import org.apache.isis.applib.value.Money;
import org.apache.isis.examples.webshop.dom.catalog.categories.ProductCategory;


@Named("Products")
public interface Products {

    Product newProduct(
        @Named("Name")
        String name, 
        ProductCategory category, 
        @Named("Price")
        Money price, 
        @Named("ImagePath")
        String imagePath);

    @QueryOnly
    List<Product> all(ProductCategory productCategory);

    @QueryOnly
    @Exploration
    List<Product> all();

}
