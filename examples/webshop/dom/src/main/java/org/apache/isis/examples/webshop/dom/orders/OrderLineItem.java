package org.apache.isis.examples.webshop.dom.orders;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.value.Money;
import org.apache.isis.examples.webshop.dom.catalog.products.Product;

public class OrderLineItem extends AbstractDomainObject {

    // {{ Order (property)
    private Order order;

    @Disabled
    @MemberOrder(sequence = "1")
    public Order getOrder() {
        return order;
    }

    public void setOrder(final Order order) {
        this.order = order;
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
    @Disabled
    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    // }}
    
    // {{ Price (property)
    private Money price;

    @MemberOrder(sequence = "1")
    @Disabled
    public Money getPrice() {
        return price;
    }

    public void setPrice(final Money price) {
        this.price = price;
    }
    // }}


}
