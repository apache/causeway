package org.apache.isis.applib.services.publish;

import org.apache.isis.applib.annotation.PublishedObject.PayloadFactory;


/**
 * Decouples the {@link PayloadFactory payload} {@link org.apache.isis.applib.annotation.PublishedAction.PayloadFactory factory}
 * implementations from the framework for obtaining an string form (OID) and the class name of objects.
 */
public interface ObjectStringifier {
    public String classNameOf(Object object);
    public String toString(Object object);
}