package org.apache.isis.extensions.restful.viewer.xom;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;


public class ElementBuilderXom {

    /**
     * The element being built.
     */
    private Element element;

    public ElementBuilderXom(final String name) {
        el(name);
    }

    /**
     * Creates an uninitialized builder.
     * 
     * <p>
     * Must be followed by a call to {@link #el(String)}.
     */
    public ElementBuilderXom() {}

    public ElementBuilderXom el(final String name) {
        assertNotInitialized();
        this.element = new Element(name);
        return this;
    }

    public ElementBuilderXom attr(final String name, final String value) {
        assertInitialized();
        element.addAttribute(new Attribute(name, value));
        return this;
    }

    public ElementBuilderXom classAttr(final String htmlClassAttribute) {
        if (htmlClassAttribute != null) {
            element.addAttribute(new Attribute("class", htmlClassAttribute));
        }
        return this;
    }

    public ElementBuilderXom idAttr(final String idAttribute) {
        if (idAttribute != null) {
            element.addAttribute(new Attribute("id", idAttribute));
        }
        return this;
    }

    public ElementBuilderXom append(final Node childNode) {
        element.appendChild(childNode);
        return this;
    }

    public ElementBuilderXom append(final String childText) {
        if (childText != null) {
            element.appendChild(childText);
        }
        return this;
    }

    public Element build() {
        return element;
    }

    private void assertInitialized() {
        if (element == null) {
            throw new IllegalStateException("Element name not specified");
        }
    }

    private void assertNotInitialized() {
        if (element != null) {
            throw new IllegalStateException("Element already specified");
        }
    }

}
