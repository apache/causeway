<span class="version-reference">(since 1.)</span>

The framework supports text values as:
- Single-line 
- Multi-line

For multi-line rendering use `@PropertyLayout(multiLine=...)`:

```java
public class TextDemo {

    @Property
    @Getter @Setter private String string; // rendered as single line field
    
    @Property
    @PropertyLayout(multiLine=3) 
    @Getter @Setter private String stringMultiline; // rendered as multi-line field (3 lines)
    
}
```
					
See the text demo [sources](${SOURCES_DEMO}/domainapp/dom/scalars).
