package org.apache.isis.applib.services.navparent;

import org.apache.isis.applib.annotation.Programmatic;

/**
 * 
 * @author ahuber@apache.org
 * @since 2.0.0
 */
public interface NavigableParentService {

    /**
     * Return the navigable parent (a domain-object or a domain-view-model) of the object, 
     * used to build a navigable parent chain as required by the 'where-am-I' feature.
     * 
     */
    @Programmatic
    public Object navigableParentOf(Object domainObject);
	
}
