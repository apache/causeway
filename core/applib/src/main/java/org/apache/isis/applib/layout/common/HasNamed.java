package org.apache.isis.applib.layout.common;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Created by Dan on 10/02/2016.
 */
public interface HasNamed {
    @XmlElement(required = false) String getNamed();

    void setNamed(String named);

    @XmlAttribute(required = false) Boolean getNamedEscaped();

    void setNamedEscaped(Boolean namedEscaped);
}
