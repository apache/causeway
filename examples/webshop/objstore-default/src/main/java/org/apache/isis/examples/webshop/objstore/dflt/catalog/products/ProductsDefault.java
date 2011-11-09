package org.apache.isis.examples.webshop.objstore.dflt.catalog.products;

import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.MemberOrder;
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
    @MemberOrder(sequence = "1.0")
    public Product newProduct(String name, ProductCategory category, Money price, String imageUrl) {
        final Product product = newTransientInstance(Product.class);
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setImageUrl(imageUrl);
        persistIfNotAlready(product);
        return product;
    }

    @Override
    @MemberOrder(sequence = "2.0")
    public List<Product> all(final ProductCategory productCategory) {
        return allMatches(Product.class, new Filter<Product>() {

            @Override
            public boolean accept(Product t) {
                return t.inCategory(productCategory);
            }
        });
    }

    @Override
    @MemberOrder(sequence = "3.0")
    public List<Product> all() {
        return allInstances(Product.class);
    }


}
