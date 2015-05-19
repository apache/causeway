Title: @Exploration

[//]: # (content copied to _user-guide_xxx)

> Deprecated, use instead [@Action#restrictTo()](./Action.html).

The `@Exploration` annotation marks an action method as available in
exploration mode only, and therefore not intended for use in the
production system. 


For example:

    public class Customer {
        public Order placeNewOrder() { ... }
        @Exploration
        public List<Order> listRecentOrders() { ... }
        ...
    }

    
#### See also

See also the [@Prototype](./Prototype-deprecated.html) annotation.
