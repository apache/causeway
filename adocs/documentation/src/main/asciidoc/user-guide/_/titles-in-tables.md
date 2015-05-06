Title: Titles in Tables

[//]: # (content copied to _user-guide_xxx)

Object titles can often be quite long if the intention is to uniquely identify the object.  While this is appropriate for the object view, it can be cumbersome within tables.

Isis' Wicket viewer implements two features to shorten these titles.

### Excluding parent context (@Title annotation)

The first feature to shorten the title in tables is to automatically exclude the title of the parent object.  This can be done when the `@Title` annotation is used.

For example, suppose we have:

![](images/cust-order-product.png)

where the `Order` class references both `Customer` and `Product`.  It's title might involve each of these:

  public class Order {
    @Title(sequence="1")
    public Customer getCustomer() { ... }
    
    @Title(sequence="2")
    public Product getProduct() { ... }
    
    @Title(sequence="3")
    public String getOtherInfo() { ... }
    
    ...
  }

In this case, if we view the `Order` from the context of `Customer` (that is, within a parented collection's table) then the customer's property will be automatically excluded from the title of the `Order`.

Incidentally, this feature is closely related to the 
`@Hidden(where=Where.REFERENCES_PARENT)` annotation, which will cause the property itself to be hidden as a column in the table.  An Isis idiom is therefore:

  public class Order {
    @Title(sequence="1")
    @Hidden(where=Where.REFERENCES_PARENT)
    public Customer getCustomer() { ... }
    
    ...
  }

The above annotations mean that titles usually "just work", altering according to the context in which they are viewed.

### Abbreviating titles

The second feature that the Wicket viewer supports is to truncate titles longer than a certain length.  This is done using properties specific to the Wicket viewer.

#### Viewer-specific configuration file

Because these configuration properties to be set up *are* specific to the Wicket viewer, it is good practice to put them into their own configuration file, `WEB-INF/viewer_wicket.properties`.  (You can put them into the regular `WEB-INF/isis.properties` file if you wish, though).

To have Isis pick up this configuration file, add the following to `WEB-INF/web.xml`:

    <context-param>
        <param-name>isis.viewers</param-name>
        <param-value>wicket</param-value>
    </context-param>

If there is more than one viewer configured in the webapp, specify them as a comma-separated list, eg `wicket,restfulobjects`.


#### The configuration properties

The properties themselves are:

    isis.viewer.wicket.maxTitleLengthInStandaloneTables=20
    isis.viewer.wicket.maxTitleLengthInParentedTables=8

If you wish to use the same value in both cases, you can also specify just:

    isis.viewer.wicket.maxTitleLengthInTables=15

This is used as a fallback if the more specific properties are not provided.

If no properties are provided, then the Wicket viewer defaults to abbreviating titles to a length of `12`.
