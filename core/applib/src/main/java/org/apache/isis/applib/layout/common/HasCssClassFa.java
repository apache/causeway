package org.apache.isis.applib.layout.common;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by Dan on 10/02/2016.
 */
public interface HasCssClassFa {
    @XmlAttribute(required = false) String getCssClassFa();

    void setCssClassFa(String cssClassFa);

    @XmlAttribute(required = false) org.apache.isis.applib.annotation.ActionLayout.CssClassFaPosition getCssClassFaPosition();

    void setCssClassFaPosition(org.apache.isis.applib.annotation.ActionLayout.CssClassFaPosition cssClassFaPosition);
}
