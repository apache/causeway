<span class="version-reference">(since 1.16)</span>

The framework allows an action to be associated with other properties or collections of the same domain object. For complete reference see the [Apache Isis Reference Guide](https://isis.apache.org/guides/rgant/rgant.html#_rgant-Action_associateWith)

We setup a simple `DemoItem` class to demonstrate this.

```java
@DomainObject 
public class DemoItem {
	
    public String title() {
        return String.format("DemoItem '%s'", getName());
    }
    
    @Property
	@Getter @Setter	private String name;

}
```

Now following demo view model holds `items` a collection of element type `DemoItem`. The action method `doSomethingWithItems(...)` can be thought of as associated with the `items` collection, because it expresses this  association using the annotation `@Action(associateWith="items")`.

```java
@DomainObject(nature=Nature.VIEW_MODEL)
public class AssociatedActionDemo extends DemoStub {
    
    @Inject MessageService messageService;
    
    @Getter private final Set<DemoItem> items;
    
    @Action(associateWith="items")
    public AssociatedActionDemo doSomethingWithItems(Set<DemoItem> items) {
        if(items!=null) {
            items.forEach(item->messageService.informUser(item.getName()));    
        }
        return this;
    }

}
```
					
See the associated action demo [sources](${SOURCES_DEMO}/domainapp/dom/actions/assoc).
