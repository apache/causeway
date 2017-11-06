package org.apache.isis.applib.layout.menubars;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.isis.applib.annotation.Programmatic;

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


}
