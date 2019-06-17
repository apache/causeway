<span class="version-reference">(since 1.16)</span>

The framework supports following temporal values from the Java Time API (and joda.org):

1) Date only
- java.sql.**Date**
- java.time.**LocalDate**; <span class="version-reference">(since 2.0.0-M1)</span>
- org.joda.time.**LocalDate** 

2) Date and Time
- java.util.**Date**
- java.sql.**Timestamp**
- java.time.**LocalDateTime** <span class="version-reference">(since 2.0.0-M1)</span>
- java.time.**OffsetDateTime** <span class="version-reference">(since 2.0.0-M1)</span>
- org.joda.time.**DateTime**
- org.joda.time.**LocalDateTime**

If used with JAXB View Models, you need to specify specific XmlAdapters as provided by `org.apache.isis.applib.util.JaxbAdapters.*`:

```java
@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL)
public class TemporalDemo extends DemoStub {

    // -- DATE ONLY (LOCAL TIME)
    
    @XmlElement @XmlJavaTypeAdapter(SqlDateAdapter.class)
    @Getter @Setter private java.sql.Date javaSqlDate;
    
    @XmlElement @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @Getter @Setter private LocalDate javaLocalDate;
    
    // -- DATE AND TIME (LOCAL TIME)
    
    @XmlElement @XmlJavaTypeAdapter(DateAdapter.class)
    @Getter @Setter private Date javaUtilDate;
    
    @XmlElement @XmlJavaTypeAdapter(SqlTimestampAdapter.class)
    @Getter @Setter private java.sql.Timestamp javaSqlTimestamp;
        
    @XmlElement @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @Getter @Setter private LocalDateTime javaLocalDateTime;
    
    // -- DATE AND TIME (WITH TIMEZONE OFFSET)

    @XmlElement @XmlJavaTypeAdapter(OffsetDateTimeAdapter.class)
    @Getter @Setter private OffsetDateTime javaOffsetDateTime;
    
    // --
    
    @Override
    public void initDefaults() {
        
        log.info("TemporalDemo::initDefaults");
        
        javaUtilDate = new Date();
        javaSqlDate = new java.sql.Date(System.currentTimeMillis());
        javaSqlTimestamp = new java.sql.Timestamp(System.currentTimeMillis());
        
        javaLocalDate = LocalDate.now();
        javaLocalDateTime = LocalDateTime.now();
        javaOffsetDateTime = OffsetDateTime.now();
    }
    
}
```
					
See the temporal demo [sources](${SOURCES_DEMO}/domainapp/dom/scalars).
