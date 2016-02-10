package org.apache.isis.applib.layout.component;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.isis.applib.annotation.Where;

/**
 * Created by Dan on 10/02/2016.
 */
public interface HasHidden {
    @XmlAttribute(required = false) Where getHidden();

    void setHidden(Where hidden);
}
