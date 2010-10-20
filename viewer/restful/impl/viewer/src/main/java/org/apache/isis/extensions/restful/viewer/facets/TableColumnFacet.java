/**
 * 
 */
package org.apache.isis.extensions.restful.viewer.facets;


import org.apache.isis.extensions.restful.viewer.xom.ResourceContext;
import org.apache.isis.extensions.restful.viewer.xom.TableColumnAbstract;
import org.apache.isis.metamodel.facets.Facet;


public abstract class TableColumnFacet extends TableColumnAbstract<Facet> {

    TableColumnFacet(final String headerText, final ResourceContext resourceContext) {
        super(headerText, resourceContext);
    }

}
