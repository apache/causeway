package org.apache.isis.examples.webshop.fixture.catalog.categories;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.examples.webshop.dom.catalog.categories.ProductCategories;


public class ProductCategoriesFixture extends AbstractFixture {

    @Override
    public void install() {
        categories.newCategory("CAM", "Camera & Photo");
        categories.newCategory("CAR", "Sat Nav & Car Accessories");
        categories.newCategory("SWR", "Software");
        categories.newCategory("HWR", "Computer Components");
    }
    
    // {{ injected: ProductCategories
    private ProductCategories categories;

    public void setProductCategories(final ProductCategories categories) {
        this.categories = categories;
    }
    // }}


    
}
