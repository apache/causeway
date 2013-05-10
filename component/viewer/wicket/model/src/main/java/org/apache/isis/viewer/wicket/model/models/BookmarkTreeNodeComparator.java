package org.apache.isis.viewer.wicket.model.models;

import java.util.Comparator;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;

final class BookmarkTreeNodeComparator implements Comparator<BookmarkTreeNode> {
    
    @Override
    public int compare(BookmarkTreeNode o1, BookmarkTreeNode o2) {
        PageType pageType1 = PageParameterNames.PAGE_TYPE.getEnumFrom(o1.pageParameters, PageType.class);
        PageType pageType2 = PageParameterNames.PAGE_TYPE.getEnumFrom(o2.pageParameters, PageType.class);
        
        final int pageTypeComparison = pageType1.compareTo(pageType2);
        if(pageTypeComparison != 0) {
            return pageTypeComparison;
        }
        
        if(pageType1 == PageType.ENTITY) {
            // sort by entity type
            final String className1 = classNameOf(o1.pageParameters);
            final String className2 = classNameOf(o2.pageParameters);
            
            final int classNameComparison = className1.compareTo(className2);
            if(classNameComparison != 0) {
                return classNameComparison;
            }
        }
        String title1 = PageParameterNames.PAGE_TITLE.getStringFrom(o1.pageParameters);
        String title2 = PageParameterNames.PAGE_TITLE.getStringFrom(o2.pageParameters);
        return title1.compareTo(title2);
    }

    private String classNameOf(PageParameters pp) {
        String oidStr = PageParameterNames.OBJECT_OID.getStringFrom(pp);
        RootOid oid = getOidMarshaller().unmarshal(oidStr, RootOid.class);
        ObjectSpecId objectSpecId = oid.getObjectSpecId();
        final String className = getSpecificationLoader().lookupBySpecId(objectSpecId).getIdentifier().getClassName();
        return className;
    }
    
    //////////////////////////////////////////////////
    // Dependencies (from context)
    //////////////////////////////////////////////////
    
    protected OidMarshaller getOidMarshaller() {
        return IsisContext.getOidMarshaller();
    }
    
    protected SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

}