package org.apache.isis.examples.webshop.dom.catalog.products;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Ignore;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.value.Money;
import org.apache.isis.examples.webshop.dom.catalog.categories.ProductCategory;

public class Product extends AbstractDomainObject {

    
	// {{ Name (title)
	private String name;

	@Title
	@Hidden
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}
	// }}
	
	// {{ Category
    private ProductCategory category;

    @MemberOrder(sequence = "2.0")
    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(final ProductCategory category) {
        this.category = category;
    }
    
    @Ignore
    public boolean inCategory(ProductCategory productCategory) {
        return productCategory == getCategory();
    }
    // }}

    // {{ Price
    private Money price;

    @MemberOrder(sequence = "3.0")
    public Money getPrice() {
        return price;
    }

    public void setPrice(final Money price) {
        this.price = price;
    }
    // }}

    // {{ ImageUrl
    private String imageUrl;

    @MemberOrder(sequence = "4.0")
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(final String imageUrl) {
        this.imageUrl = imageUrl;
    }
    // }}



}
