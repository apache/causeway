package org.apache.isis.viewer.restful.viewer.xom;

import nu.xom.Element;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.oidgenerator.OidGenerator;
import org.apache.isis.viewer.restful.viewer.util.OidUtils;


public abstract class TableColumnAbstract<T> implements TableColumn<T> {
    private final String headerText;

    protected final ResourceContext resourceContext;
    protected final XhtmlRendererXom xhtmlRenderer;

    protected TableColumnAbstract(final String headerText, final ResourceContext resourceContext) {
        this.headerText = headerText;
        this.resourceContext = resourceContext;
        this.xhtmlRenderer = new XhtmlRendererXom();
    }

    protected ElementBuilderXom builder() {
        return new ElementBuilderXom();
    }

    protected String getContextPath() {
        return resourceContext.getHttpServletRequest().getContextPath();
    }

    public String getHeaderText() {
        return headerText;
    }

    public Element th() {
        final Element th = new Element("th");
        th.appendChild(headerText);
        return th;
    }

    public Element td(final T t) {
        final Element td = new Element("td");
        final Element doTd = doTd(t);
        if (doTd != null) {
            td.appendChild(doTd);
        }
        return td;
    }

    protected abstract Element doTd(T t);


	protected String getOidStr(ObjectAdapter adapter) {
		return OidUtils.getOidStr(adapter, getOidStringifier());
	}

	
    ////////////////////////////////////////////////////////////////
    // Dependencies (from singletons)
    ////////////////////////////////////////////////////////////////
    
	private static PersistenceSession getPersistenceSession() {
		return IsisContext.getPersistenceSession();
	}

	private static OidGenerator getOidGenerator() {
		return getPersistenceSession().getOidGenerator();
	}

	private static OidStringifier getOidStringifier() {
		return getOidGenerator().getOidStringifier();
	}
	

}
