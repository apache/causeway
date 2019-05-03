package domainapp.dom.scalars;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.util.JaxbAdapters.DateAdapter;
import org.apache.isis.applib.util.JaxbAdapters.LocalDateAdapter;
import org.apache.isis.applib.util.JaxbAdapters.LocalDateTimeAdapter;
import org.apache.isis.applib.util.JaxbAdapters.OffsetDateTimeAdapter;
import org.apache.isis.applib.util.JaxbAdapters.SqlDateAdapter;
import org.apache.isis.applib.util.JaxbAdapters.SqlTimestampAdapter;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

import domainapp.utils.DemoStub;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, editing=Editing.ENABLED)
@Log
public class TemporalDemo extends DemoStub {

    public String title() {
        return "Temporal Demo";
    }

    // -- DATE ONLY (LOCAL TIME)
    
    @Property(editing=Editing.ENABLED) //TODO should not be required, https://issues.apache.org/jira/browse/ISIS-1970
    @PropertyLayout(describedAs="java.sql.Date")
    @XmlElement @XmlJavaTypeAdapter(SqlDateAdapter.class)
    @Getter @Setter private java.sql.Date javaSqlDate;
    
    @Property(editing=Editing.ENABLED) //TODO should not be required, https://issues.apache.org/jira/browse/ISIS-1970
    @PropertyLayout(describedAs="java.time.LocalDate")
    @XmlElement @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @Getter @Setter private LocalDate javaLocalDate;
    
    // -- DATE AND TIME (LOCAL TIME)
    
    @Property(editing=Editing.ENABLED) //TODO should not be required, https://issues.apache.org/jira/browse/ISIS-1970
    @PropertyLayout(describedAs="java.util.Date")
    @XmlElement @XmlJavaTypeAdapter(DateAdapter.class)
    @Getter @Setter private Date javaUtilDate;
    
    @Property(editing=Editing.ENABLED) //TODO should not be required, https://issues.apache.org/jira/browse/ISIS-1970
    @PropertyLayout(describedAs="java.sql.Timestamp")
    @XmlElement @XmlJavaTypeAdapter(SqlTimestampAdapter.class)
    @Getter @Setter private java.sql.Timestamp javaSqlTimestamp;
        
    @Property(editing=Editing.ENABLED) //TODO should not be required, https://issues.apache.org/jira/browse/ISIS-1970
    @PropertyLayout(describedAs="java.time.LocalDateTime")
    @XmlElement @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @Getter @Setter private LocalDateTime javaLocalDateTime;
    
    // -- DATE AND TIME (WITH TIMEZONE OFFSET)
    
    @Property(editing=Editing.ENABLED) //TODO should not be required, https://issues.apache.org/jira/browse/ISIS-1970
    @PropertyLayout(describedAs="java.time.OffsetDateTime")
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
