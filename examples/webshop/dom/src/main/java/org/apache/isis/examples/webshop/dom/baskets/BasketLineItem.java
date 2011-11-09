package org.apache.isis.examples.webshop.dom.baskets;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.examples.webshop.dom.catalog.products.Product;

public class BasketLineItem extends AbstractDomainObject {

    // {{ Basket (property)
    private Basket basket;

    @MemberOrder(sequence = "1")
    @Disabled
    public Basket getBasket() {
        return basket;
    }

    public void setBasket(final Basket basket) {
        this.basket = basket;
    }
    // }}

    // {{ Product (property, title)
    private Product product;

    @Title(sequence="2", abbreviatedTo=12)
    @MemberOrder(sequence = "2")
    @Disabled
    public Product getProduct() {
        return product;
    }

    public void setProduct(final Product product) {
        this.product = product;
    }
    // }}

    // {{ Quantity (property, title)
    private Integer quantity;

    @Title(sequence="1", append="x")
    @MemberOrder(sequence = "3")
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }
    public List<Integer> choicesQuantity() {
        return Arrays.asList(1, 2, 3, 5, 10, 20, 50, 100);
    }
    public String validateQuantity(final Integer quantity) {
        return quantity>0?null:"Quantity must be positive";
    }
    // }}
    
    // {{ increment (action)
    @MemberOrder(sequence = "4")
    public void increment() {
        setQuantity(getQuantity()+1);
    }
    // }}

    // {{ decrement (action)
    @MemberOrder(sequence = "5")
    public void decrement() {
        setQuantity(getQuantity()-1);
        if(getQuantity() == 0) {
            getBasket().getLineItems().remove(this);
            setBasket(null);
            getContainer().remove(this);
        }
    }
    // }}

}
