package org.apache.isis.applib.layout.menubars;

import java.io.Serializable;
import java.util.LinkedHashMap;

import javax.xml.bind.annotation.XmlTransient;

import com.google.common.collect.Maps;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.component.ServiceActionLayoutData;

@XmlTransient // ignore this class
public abstract class MenuBarsAbstract implements MenuBars, Serializable {

    private String tnsAndSchemaLocation;

    @Programmatic
    @XmlTransient
    public String getTnsAndSchemaLocation() {
        return tnsAndSchemaLocation;
    }

    @Programmatic
    public void setTnsAndSchemaLocation(final String tnsAndSchemaLocation) {
        this.tnsAndSchemaLocation = tnsAndSchemaLocation;
    }

    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, ServiceActionLayoutData> getAllServiceActionsByObjectTypeAndId() {
        final LinkedHashMap<String, ServiceActionLayoutData> serviceActionsByObjectTypeAndId = Maps.newLinkedHashMap();
        visit(new MenuBars.Visitor() {
            public void visit(final ServiceActionLayoutData serviceActionLayoutData) {
                serviceActionsByObjectTypeAndId.put(serviceActionLayoutData.getObjectTypeAndId(), serviceActionLayoutData);
            }
        });
        return serviceActionsByObjectTypeAndId;
    }


}
