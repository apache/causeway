/**
 * 
 */
package org.apache.isis.viewer.restful.viewer.facets;


import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;
import org.apache.isis.viewer.restful.viewer.xom.TableColumnAbstract;


public abstract class TableColumnFacet extends TableColumnAbstract<Facet> {

    TableColumnFacet(final String headerText, final ResourceContext resourceContext) {
        super(headerText, resourceContext);
    }

}
