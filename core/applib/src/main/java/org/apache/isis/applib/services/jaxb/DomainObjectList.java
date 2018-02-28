package org.apache.isis.applib.services.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.schema.utils.jaxbadapters.PersistentEntitiesAdapter;

@XmlRootElement(name = "list")
@XmlType(
        propOrder = {
                "title",
                "elementType",
                "objects"
        }
)
@XmlAccessorType(XmlAccessType.FIELD)
public class DomainObjectList {

    public DomainObjectList() {
    }
    public DomainObjectList(final String title, final Class<?> elementType) {
        this.title = title;
        this.elementType = elementType.getCanonicalName();
    }

    private String title;
    public String title() { return title; }

    private String elementType;
    @Property(editing = Editing.DISABLED)
    public String getElementType() {
        return elementType;
    }


    @XmlJavaTypeAdapter(PersistentEntitiesAdapter.class)
    private List<Object> objects = Lists.newArrayList();

    @Collection(editing = Editing.DISABLED)
    public List<Object> getObjects() {
        return objects;
    }

    public void setObjects(final List<Object> objects) {
        this.objects = objects;
    }

}
