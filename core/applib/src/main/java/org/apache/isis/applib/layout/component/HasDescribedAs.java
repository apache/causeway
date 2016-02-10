package org.apache.isis.applib.layout.component;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by Dan on 10/02/2016.
 */
public interface HasDescribedAs {
    @XmlElement(required = false) String getDescribedAs();

    void setDescribedAs(String describedAs);
}
