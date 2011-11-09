package org.apache.isis.examples.webshop.fixture.catalog.categories;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.examples.webshop.dom.catalog.categories.ProductCategory;


public class ProductCategoriesFixture extends AbstractFixture {

    @Override
    public void install() {
        newCategory("CAM", "Camera & Photo");
        newCategory("CAR", "Sat Nav & Car Accessories");
        newCategory("SWR", "Software");
        newCategory("HWR", "Computer Components");
    }
    
    private ProductCategory newCategory(String code, String name) {
        final ProductCategory category = newTransientInstance(ProductCategory.class);
        category.setCode(code);
        category.setName(name);
        persistIfNotAlready(category);
        return category;
    }

    
}
