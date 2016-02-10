package org.apache.isis.applib.layout.component;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by Dan on 10/02/2016.
 */
public interface HasCssClass {
    @XmlAttribute(required = false) String getCssClass();

    void setCssClass(String cssClass);
}
