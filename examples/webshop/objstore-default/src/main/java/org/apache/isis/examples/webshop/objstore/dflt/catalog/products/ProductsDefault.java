package org.apache.isis.examples.webshop.objstore.dflt.catalog.products;

import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.value.Money;
import org.apache.isis.examples.webshop.dom.catalog.categories.ProductCategory;
import org.apache.isis.examples.webshop.dom.catalog.products.Product;
import org.apache.isis.examples.webshop.dom.catalog.products.Products;

public class ProductsDefault extends AbstractFactoryAndRepository implements Products {

    // {{ Id, iconName
    @Override
    public String getId() {
        return "products";
    }

    public String iconName() {
        return "Product";
    }
    // }}

    @Override
    public Product newProduct(String name, ProductCategory category, Money price, String imagePath) {
        final Product product = newTransientInstance(Product.class);
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setImageUrl(imagePath);
        persistIfNotAlready(product);
        return product;
    }

    @Override
    public List<Product> all() {
        return allInstances(Product.class);
    }

    @Override
    public List<Product> all(final ProductCategory productCategory) {
        return allMatches(Product.class, new Filter<Product>() {

            @Override
            public boolean accept(Product t) {
                return t.inCategory(productCategory);
            }
        });
    }


}
