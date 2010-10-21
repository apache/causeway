/**
 * 
 */
package org.apache.isis.extensions.restful.viewer.xom;

import nu.xom.Element;


public interface TableColumn<T> {

    public String getHeaderText();

    public Element th();

    public Element td(T t);

}
