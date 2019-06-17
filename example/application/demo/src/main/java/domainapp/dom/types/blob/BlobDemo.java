package domainapp.dom.types.blob;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.resources._Resources;

import domainapp.utils.DemoStub;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, editing=Editing.ENABLED)
@Log4j2
public class BlobDemo extends DemoStub {

    @Override
    public void initDefaults() {

        log.info("BlobDemo::initDefaults");
        
        try {
	        val bytes = _Bytes.of(_Resources.load(BlobDemo.class, "avatar.png"));
			avatar = new Blob("family", "image/png", bytes);
        } catch (Exception e) {
			// TODO: handle exception
		}
        
    }
    
    // -- EDITABLE
    
    @Property(editing=Editing.ENABLED) //TODO should not be required, https://issues.apache.org/jira/browse/ISIS-1970
    @XmlElement @XmlJavaTypeAdapter(DemoBlobStore.BlobAdapter.class)
    @Getter @Setter private Blob avatar;
    
    // -- READONLY
    
//    @Property(editing=Editing.DISABLED)
//    @XmlElement @Getter @Setter private Blob blobReadonly;
//    
//    @Property(editing=Editing.DISABLED)
//    @PropertyLayout(multiLine=3)
//    @XmlElement @Getter @Setter private String stringMultilineReadonly;
    
}
