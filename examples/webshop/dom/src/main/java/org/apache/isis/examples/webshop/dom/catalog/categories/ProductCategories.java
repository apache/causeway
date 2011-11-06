package org.apache.isis.examples.webshop.dom.catalog.categories;

import java.util.List;

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.QueryOnly;

@Hidden
@Named("Product Categories")
public interface ProductCategories {

    @QueryOnly
    @MemberOrder(sequence="1")
    public List<ProductCategory> list();

    public ProductCategory newCategory(
        @Named("Code") String code,
        @Named("Name") String name);

    @QueryOnly
    public ProductCategory findByCode(
        @Named("Code") String code);

}
