package org.apache.isis.viewer.restful.viewer.resources;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import nu.xom.Attribute;
import nu.xom.Element;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.lang.CastUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetFilters;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.persistence.PersistenceSession;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.core.runtime.persistence.oidgenerator.OidGenerator;
import org.apache.isis.viewer.restful.viewer.facets.TableColumnFacetDisabling;
import org.apache.isis.viewer.restful.viewer.facets.TableColumnFacetFacetType;
import org.apache.isis.viewer.restful.viewer.facets.TableColumnFacetHiding;
import org.apache.isis.viewer.restful.viewer.facets.TableColumnFacetImplementation;
import org.apache.isis.viewer.restful.viewer.facets.TableColumnFacetValidating;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.util.ListUtils;
import org.apache.isis.viewer.restful.viewer.util.OidUtils;
import org.apache.isis.viewer.restful.viewer.xom.DtDd;
import org.apache.isis.viewer.restful.viewer.xom.ElementBuilderXom;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;
import org.apache.isis.viewer.restful.viewer.xom.TableColumn;
import org.apache.isis.viewer.restful.viewer.xom.XhtmlRendererXom;


public abstract class ResourceAbstract {

    public final static ActionType[] ACTION_TYPES = { 
    	ActionType.USER,
        ActionType.DEBUG, 
        ActionType.EXPLORATION,
        // SET is excluded; we simply flatten contributed actions.
    };

    protected final XhtmlRendererXom xhtmlRenderer;
    protected final ElementBuilderXom elementBuilder;
    
    @Context
    HttpHeaders httpHeaders;

    @Context
    UriInfo uriInfo;
    
    @Context
    Request request;

    @Context
	HttpServletRequest httpServletRequest;

    @Context
    HttpServletResponse httpServletResponse;

    @Context
    SecurityContext securityContext;

    private ResourceContext resourceContext;

    protected ResourceAbstract() {
    	this.xhtmlRenderer = new XhtmlRendererXom();
    	this.elementBuilder = new ElementBuilderXom();
    }
    
    protected void init() {
        this.resourceContext = 
        	new ResourceContext(httpHeaders, uriInfo, request, httpServletRequest, httpServletResponse, securityContext);
    }

	protected ResourceContext getResourceContext() {
		return resourceContext;
	}

    ////////////////////////////////////////////////////////////////
    // Isis integration
    ////////////////////////////////////////////////////////////////

    protected static ObjectSpecification getSpecification(final String specFullName) {
        return getSpecificationLoader().loadSpecification(specFullName);
    }

	protected ObjectAdapter getNakedObject(final String oidEncodedStr) {
		return OidUtils.getNakedObject(oidEncodedStr, getOidStringifier());
    }

	protected String getOidStr(final ObjectAdapter nakedObject) {
		return OidUtils.getOidStr(nakedObject, getOidStringifier());
    }

	
    ////////////////////////////////////////////////////////////////
    // Rendering
    ////////////////////////////////////////////////////////////////

	protected Element asDivNofSession() {
        final Element div = xhtmlRenderer.div_p("Logged in as", null);

        final Element ul = xhtmlRenderer.ul(HtmlClass.SESSION);
        ul.appendChild(xhtmlRenderer.li_a("/user", getSession().getUserName(), "user", "resource", HtmlClass.USER));
        div.appendChild(ul);
        
        return div;
    }

	protected Element resourcesDiv() {
		final Element div = xhtmlRenderer.div_p("Resources", HtmlClass.SECTION);

        final Element ul = xhtmlRenderer.ul(HtmlClass.RESOURCES);
        
        ul.appendChild(xhtmlRenderer.li_a("services", "Services", "services", "resources", HtmlClass.RESOURCE));
        ul.appendChild(xhtmlRenderer.li_a("specs", "Specifications (MetaModel)", "specs", "resources", HtmlClass.RESOURCE));
        ul.appendChild(xhtmlRenderer.li_a("user", "User (Security)", "user", "resources", HtmlClass.RESOURCE));
        
        div.appendChild(ul);
		return div;
	}
    
    protected Element asDivTableFacets(final FacetHolder facetHolder, final String pathPrefix) {
        final Element div = xhtmlRenderer.div_p("Facets", HtmlClass.FACETS);
        final List<Facet> rows = ListUtils.toList(facetHolder.getFacets(FacetFilters.ANY));

        final List<TableColumn<Facet>> columns = new ArrayList<TableColumn<Facet>>();
        columns.add(new TableColumnFacetFacetType(pathPrefix, getResourceContext()));
        columns.add(new TableColumnFacetImplementation(getResourceContext()));
        columns.add(new TableColumnFacetHiding(getResourceContext()));
        columns.add(new TableColumnFacetDisabling(getResourceContext()));
        columns.add(new TableColumnFacetValidating(getResourceContext()));

        final Element table = xhtmlRenderer.table(columns, rows, HtmlClass.FACETS);
        div.appendChild(table);
        return div;
    }

    public Element divFacetElements(final String facetTypeName, final FacetHolder facetHolder) throws ClassNotFoundException,
            IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final Element div = xhtmlRenderer.div_p("Facet Elements", HtmlClass.FACET_ELEMENTS);
        final Class<? extends Facet> facetType = CastUtils.cast(Class.forName(facetTypeName));
        final Facet facet = facetHolder.getFacet(facetType);
        final Class<? extends Facet> facetImplClass = facet.getClass();
        final BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(facetImplClass);
        final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        final Element dl = xhtmlRenderer.dl(HtmlClass.FACET_ELEMENTS);
        for (final PropertyDescriptor descriptor : propertyDescriptors) {
            final String name = descriptor.getName();
            final Method readMethod = descriptor.getReadMethod();
            if (readMethod != null) {
                final Object value = readMethod.invoke(facet);
                String ddText = value!=null?value.toString():"(null)";
				final DtDd dt_dd = xhtmlRenderer.dt_dd(name, ddText, HtmlClass.FACET_ELEMENT);
                dt_dd.appendTo(dl);
            }
        }
        div.appendChild(dl);
        return div;
    }

    protected Element asDivTableObjectDetails(final ObjectAdapter nakedObject) {

        final ObjectSpecification noSpec = nakedObject.getSpecification();
        final String oidStr = getOidStr(nakedObject);

        final Element div = new Element("div");
        final Element table = new Element("table");
        table.addAttribute(new Attribute("border", "1"));
        div.appendChild(table);

        Element value;

        value = xhtmlRenderer.p(nakedObject.titleString(), null);
        createRow(table, "Object title", value, HtmlClass.TITLE);

        value = xhtmlRenderer.aHref(MessageFormat.format("{0}/object/{1}", getServletRequest().getContextPath(), oidStr), oidStr, "object", "object", HtmlClass.OID);
        createRow(table, "OID", value, HtmlClass.OID);
        final String noSpecFullName = noSpec.getFullIdentifier();

        final String uri = MessageFormat.format("{0}/specs/{1}", getServletRequest().getContextPath(), noSpecFullName);
        value = xhtmlRenderer.aHref(uri, noSpecFullName, "spec", "object", HtmlClass.SPECIFICATION);
        createRow(table, "Specification", value, HtmlClass.SPECIFICATION);

        return div;
    }

    private void createRow(final Element table, final String key, final Element value, final String htmlClassAttribute) {
        Element tr;
        Element td;
        tr = new Element("tr");
        table.appendChild(tr);

        td = new Element("td");
        td.appendChild(key);
        tr.appendChild(td);

        td = new Element("td");
        td.appendChild(value);
        td.addAttribute(new Attribute("class", htmlClassAttribute));
        tr.appendChild(td);
    }

    ////////////////////////////////////////////////////////////////
    // Responses
    ////////////////////////////////////////////////////////////////

	protected Response responseOfOk() {
		return Response.ok().build();
	}
	
	protected Response responseOfGone(String reason) {
		return Response.status(Status.GONE).header("nof-reason", reason).build();
	}
	
	protected Response responseOfBadRequest(final Consent consent) {
		return responseOfBadRequest(consent.getReason());
	}

	protected Response responseOfNoContent(String reason) {
		return Response.status(Status.NO_CONTENT).header("nof-reason", reason).build();
	}

	protected Response responseOfBadRequest(String reason) {
		return Response.status(Status.BAD_REQUEST).header("nof-reason", reason).build();
	}

	protected Response responseOfNotFound(final IllegalArgumentException e) {
		return responseOfNotFound(e.getMessage());
	}

	protected Response responseOfNotFound(String reason) {
		return Response.status(Status.NOT_FOUND).header("nof-reason", reason).build();
	}

	protected Response responseOfInternalServerError(final Exception ex) {
		return responseOfInternalServerError(ex.getMessage());
	}

	protected Response responseOfInternalServerError(String reason) {
		return Response.status(Status.INTERNAL_SERVER_ERROR).header("nof-reason", reason).build();
	}
    
    ////////////////////////////////////////////////////////////////
    // Dependencies (from singletons)
    ////////////////////////////////////////////////////////////////
    
    protected static AuthenticationSession getSession() {
        return IsisContext.getAuthenticationSession();
    }

    private static SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

	public static AdapterManager getAdapterManager() {
		return getPersistenceSession().getAdapterManager();
	}

	protected static PersistenceSession getPersistenceSession() {
		return IsisContext.getPersistenceSession();
	}

	private static OidGenerator getOidGenerator() {
		return getPersistenceSession().getOidGenerator();
	}

	private static OidStringifier getOidStringifier() {
		return getOidGenerator().getOidStringifier();
	}
	

    ////////////////////////////////////////////////////////////////
    // Dependencies (injected via @Context)
    ////////////////////////////////////////////////////////////////

	protected HttpServletRequest getServletRequest() {
		return getResourceContext().getHttpServletRequest();
	}

	
}
