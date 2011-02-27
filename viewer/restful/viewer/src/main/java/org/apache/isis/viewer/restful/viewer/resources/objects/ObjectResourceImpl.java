package org.apache.isis.viewer.restful.viewer.resources.objects;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

import nu.xom.Element;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.viewer.restful.applib.resources.ObjectResource;
import org.apache.isis.viewer.restful.viewer.Constants;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.html.XhtmlTemplate;
import org.apache.isis.viewer.restful.viewer.resources.ResourceAbstract;
import org.apache.isis.viewer.restful.viewer.resources.objects.actions.TableColumnNakedObjectActionInvoke;
import org.apache.isis.viewer.restful.viewer.resources.objects.actions.TableColumnNakedObjectActionName;
import org.apache.isis.viewer.restful.viewer.resources.objects.actions.TableColumnNakedObjectActionParamCount;
import org.apache.isis.viewer.restful.viewer.resources.objects.actions.TableColumnNakedObjectActionRealTarget;
import org.apache.isis.viewer.restful.viewer.resources.objects.actions.TableColumnNakedObjectActionReturnType;
import org.apache.isis.viewer.restful.viewer.resources.objects.actions.TableColumnNakedObjectActionType;
import org.apache.isis.viewer.restful.viewer.resources.objects.collections.TableColumnOneToManyAssociationAccess;
import org.apache.isis.viewer.restful.viewer.resources.objects.collections.TableColumnOneToManyAssociationAddTo;
import org.apache.isis.viewer.restful.viewer.resources.objects.collections.TableColumnOneToManyAssociationInvalidReason;
import org.apache.isis.viewer.restful.viewer.resources.objects.collections.TableColumnOneToManyAssociationName;
import org.apache.isis.viewer.restful.viewer.resources.objects.collections.TableColumnOneToManyAssociationRemoveFrom;
import org.apache.isis.viewer.restful.viewer.resources.objects.properties.TableColumnOneToOneAssociationAccess;
import org.apache.isis.viewer.restful.viewer.resources.objects.properties.TableColumnOneToOneAssociationClear;
import org.apache.isis.viewer.restful.viewer.resources.objects.properties.TableColumnOneToOneAssociationInvalidReason;
import org.apache.isis.viewer.restful.viewer.resources.objects.properties.TableColumnOneToOneAssociationModify;
import org.apache.isis.viewer.restful.viewer.resources.objects.properties.TableColumnOneToOneAssociationName;
import org.apache.isis.viewer.restful.viewer.resources.objects.properties.TableColumnOneToOneAssociationParseable;
import org.apache.isis.viewer.restful.viewer.util.ActionUtils;
import org.apache.isis.viewer.restful.viewer.util.InputStreamUtil;
import org.apache.isis.viewer.restful.viewer.util.ListUtils;
import org.apache.isis.viewer.restful.viewer.util.UrlDecoderUtils;
import org.apache.isis.viewer.restful.viewer.xom.TableColumn;

/**
 * Implementation note: it seems to be necessary to annotate the implementation with {@link Path} rather than
 * the interface (at least under RestEasy 1.0.2 and 1.1-RC2).
 */
@Path("/object")
public class ObjectResourceImpl extends ResourceAbstract implements ObjectResource {

	@Override
    public String object(
			final String oidEncodedStr) {
        init();
        String oidStr = UrlDecoderUtils.urlDecode(oidEncodedStr);

        final ObjectAdapter nakedObject = getNakedObject(oidStr);
        if (nakedObject == null) {
            throw new WebApplicationException(responseOfGone("could not determine object"));
        }

        String javascriptFile = isJavascriptDebug()?
        		Constants.XMLHTTP_REQUEST_SRC_JS:Constants.XMLHTTP_REQUEST_JS;
        // html template
        final XhtmlTemplate xhtml = new XhtmlTemplate(nakedObject.titleString(), getServletRequest(), 
        		javascriptFile, Constants.NOF_REST_SUPPORT_JS);
        
        xhtml.appendToBody(asDivNofSession());
        xhtml.appendToBody(resourcesDiv());
        
        // object div
        final Element objectDiv = div(HtmlClass.OBJECT);
        xhtml.appendToBody(objectDiv);

        // title & Oid
        final Element objectSpecsDiv = asDivTableObjectDetails(nakedObject);
        xhtml.appendToDiv(objectDiv, objectSpecsDiv);
        //xhtml.appendToBody(div);

        // properties (in line table)
        final Element propertiesTableEl = asDivTableProperties(getSession(), nakedObject);
        xhtml.appendToDiv(objectDiv, propertiesTableEl);
        //xhtml.appendToBody(propertiesTableEl);

        // collections
        final Element collectionsDivEl = asDivTableCollections(getSession(), nakedObject);
        xhtml.appendToDiv(objectDiv, collectionsDivEl);
        //xhtml.appendToBody(collectionsDivEl);

        // actions
        final Element actionsDivEl = asDivTableActions(getSession(), nakedObject);
        xhtml.appendToDiv(objectDiv, actionsDivEl);
        //xhtml.appendToBody(actionsDivEl);

        return xhtml.toXML();
    }

    public Element asDivTableProperties(final AuthenticationSession session, final ObjectAdapter nakedObject) {
        final Element div = xhtmlRenderer.div_p("Properties", HtmlClass.PROPERTIES);

        final ObjectSpecification noSpec = nakedObject.getSpecification();
        final List<OneToOneAssociation> rows = noSpec.getProperties();

        final List<TableColumn<OneToOneAssociation>> columns = new ArrayList<TableColumn<OneToOneAssociation>>();
        columns.add(new TableColumnOneToOneAssociationName(noSpec, session, nakedObject, getResourceContext()));
        columns.add(new TableColumnNakedObjectAssociationType<OneToOneAssociation>(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnNakedObjectMemberHidden<OneToOneAssociation>(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnOneToOneAssociationAccess(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnNakedObjectMemberDisabled<OneToOneAssociation>(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnNakedObjectMemberDisabledReason<OneToOneAssociation>(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnOneToOneAssociationParseable(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnOneToOneAssociationModify(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnOneToOneAssociationClear(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnOneToOneAssociationInvalidReason(session, nakedObject, getResourceContext()));

        final Element table = xhtmlRenderer.table(columns, rows, HtmlClass.FACETS);
        div.appendChild(table);
        return div;
    }

    public Element asDivTableCollections(final AuthenticationSession session, final ObjectAdapter nakedObject) {
        final Element div = xhtmlRenderer.div_p("Collections", HtmlClass.COLLECTIONS);

        final ObjectSpecification noSpec = nakedObject.getSpecification();
        final List<OneToManyAssociation> rows = noSpec.getCollections();

        final List<TableColumn<OneToManyAssociation>> columns = new ArrayList<TableColumn<OneToManyAssociation>>();
        columns.add(new TableColumnOneToManyAssociationName(noSpec, session, nakedObject, getResourceContext()));
        columns.add(new TableColumnNakedObjectAssociationType<OneToManyAssociation>(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnNakedObjectMemberHidden<OneToManyAssociation>(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnNakedObjectMemberDisabled<OneToManyAssociation>(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnOneToManyAssociationAccess(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnOneToManyAssociationAddTo(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnOneToManyAssociationRemoveFrom(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnOneToManyAssociationInvalidReason(session, nakedObject, getResourceContext()));

        final Element table = xhtmlRenderer.table(columns, rows, HtmlClass.FACETS);
        div.appendChild(table);
        return div;
    }

    private Element asDivTableActions(final AuthenticationSession session, final ObjectAdapter nakedObject) {

        final Element div = xhtmlRenderer.div_p("Actions", HtmlClass.ACTIONS);
        final ObjectSpecification noSpec = nakedObject.getSpecification();

        final List<ObjectAction> actions = new ArrayList<ObjectAction>();
        for (final ActionType type : ResourceAbstract.ACTION_TYPES) {
            final List<ObjectAction> actionsForType = ActionUtils.flattened(noSpec.getObjectActions(type));
            actions.addAll(actionsForType);
        }
        final List<ObjectAction> rows = ListUtils.toList(actions.toArray(new ObjectAction[0]));

        final List<TableColumn<ObjectAction>> columns = new ArrayList<TableColumn<ObjectAction>>();
        columns.add(new TableColumnNakedObjectActionName(noSpec, session, nakedObject, getResourceContext()));
        columns.add(new TableColumnNakedObjectActionType(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnNakedObjectActionReturnType(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnNakedObjectActionParamCount(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnNakedObjectMemberHidden<ObjectAction>(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnNakedObjectMemberDisabled<ObjectAction>(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnNakedObjectMemberDisabledReason<ObjectAction>(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnNakedObjectActionRealTarget(session, nakedObject, getResourceContext()));
        columns.add(new TableColumnNakedObjectActionInvoke(session, nakedObject, getResourceContext()));

        final Element table = xhtmlRenderer.table(columns, rows, HtmlClass.FACETS);
        div.appendChild(table);
        return div;
    }

    // /////////////////////////////////////////////////////////////////////
    // properties
    // /////////////////////////////////////////////////////////////////////

    @Override
    public String modifyProperty(
            final String oidEncodedStr,
            final String propertyEncodedId,
            final String proposedEncodedValue) {
        init();
        String oidStr = UrlDecoderUtils.urlDecode(oidEncodedStr);
        String propertyId = UrlDecoderUtils.urlDecode(propertyEncodedId);
        String proposedValue = UrlDecoderUtils.urlDecode(proposedEncodedValue);

        final ObjectAdapter nakedObject = getNakedObject(oidStr);
        if (nakedObject == null) {
            throw new WebApplicationException(responseOfGone("could not determine object"));
        }

        final ObjectSpecification noSpec = nakedObject.getSpecification();

        final OneToOneAssociation property = (OneToOneAssociation) noSpec.getAssociation(propertyId);

        final ObjectAdapter proposedValueNO = getObjectAdapter(proposedValue, nakedObject, property);

        // make sure we have a value (should be using clear otherwise)
        if (proposedValueNO == null) {
        	throw new WebApplicationException(responseOfBadRequest("null argument"));
        }

        // validate
        final Consent consent = property.isAssociationValid(nakedObject, proposedValueNO);
        if (consent.isVetoed()) {
        	throw new WebApplicationException(responseOfBadRequest(consent));
        }

        
        // html template
        final XhtmlTemplate xhtml = new XhtmlTemplate(nakedObject.titleString() + "." + propertyId, getServletRequest());
        xhtml.appendToBody(asDivNofSession());
        xhtml.appendToBody(resourcesDiv());


        // title & Oid
        Element div = asDivTableObjectDetails(nakedObject);
        xhtml.appendToBody(div);


        // if valid, then set
        property.setAssociation(nakedObject, proposedValueNO);

        return xhtml.toXML();
    }

    @Override
    public String clearProperty(
    		final String oidEncodedStr, 
    		final String propertyEncodedId) {
        init();
        String oidStr = UrlDecoderUtils.urlDecode(oidEncodedStr);
        String propertyId = UrlDecoderUtils.urlDecode(propertyEncodedId);

        final ObjectAdapter nakedObject = getNakedObject(oidStr);
        if (nakedObject == null) {
        	throw new WebApplicationException(responseOfGone("could not determine object"));
        }

        final ObjectSpecification noSpec = nakedObject.getSpecification();

        final OneToOneAssociation property = (OneToOneAssociation) noSpec.getAssociation(propertyId);

        // validate
        final Consent consent = property.isAssociationValid(nakedObject, null);
        if (consent.isVetoed()) {
        	throw new WebApplicationException(responseOfBadRequest(consent));
        }

        // html template
        final XhtmlTemplate xhtml = new XhtmlTemplate(nakedObject.titleString() + "." + propertyId, getServletRequest());
        xhtml.appendToBody(asDivNofSession());
        xhtml.appendToBody(resourcesDiv());


        // title & Oid
        Element div = asDivTableObjectDetails(nakedObject);
        xhtml.appendToBody(div);



        // if valid, then clear
        property.clearAssociation(nakedObject);

        return xhtml.toXML();
    }

    // /////////////////////////////////////////////////////////////////////
    // collections
    // /////////////////////////////////////////////////////////////////////

    private enum CollectionModificationType {
        ADD_TO, REMOVE_FROM
    }

    @Override
    public String accessCollection(
    		final String oidEncodedStr, 
    		final String collectionEncodedId) {
        init();
        String oidStr = UrlDecoderUtils.urlDecode(oidEncodedStr);
        String collectionId = UrlDecoderUtils.urlDecode(collectionEncodedId);

        final ObjectAdapter nakedObject = getNakedObject(oidStr);
        if (nakedObject == null) {
        	throw new WebApplicationException(responseOfGone("could not determine object"));
        }

        final ObjectSpecification noSpec = nakedObject.getSpecification();
        final ObjectAssociation association = noSpec.getAssociation(collectionId);
        if (!association.isOneToManyAssociation()) {
        	throw new WebApplicationException(responseOfBadRequest("Not a collection"));
        }

        // html template
        final XhtmlTemplate xhtml = new XhtmlTemplate(nakedObject.titleString() + "." + collectionId, getServletRequest());
        xhtml.appendToBody(asDivNofSession());
        xhtml.appendToBody(resourcesDiv());


        // title & Oid
        Element div = asDivTableObjectDetails(nakedObject);
        xhtml.appendToBody(div);

        // collection name & contents
        final OneToManyAssociation collection = (OneToManyAssociation) association;

        div = xhtmlRenderer.div_p(collectionId, HtmlClass.COLLECTION);

        final Element ul = xhtmlRenderer.ul(HtmlClass.COLLECTION);
        div.appendChild(ul);

        final ObjectAdapter collectionObj = collection.get(nakedObject);

        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collectionObj);
        for (final Iterator<ObjectAdapter> iter = facet.iterator(collectionObj); iter.hasNext();) {
            final ObjectAdapter associatedNO = iter.next();
            ul.appendChild(toLiAHref(associatedNO));
        }

        xhtml.appendToBody(div);
        return xhtml.toXML();
    }

    @Override
    public String addToCollection(
            final String oidStr,
            final String collectionEncodedId,
            final String proposedValueEncodedOidStr) {
        return modifyCollection(oidStr, collectionEncodedId, proposedValueEncodedOidStr, CollectionModificationType.ADD_TO);
    }

    @Override
    public String removeFromCollection(
            final String oidStr,
            final String collectionEncodedId,
            final String proposedValueEncodedOidStr) {
        return modifyCollection(oidStr, collectionEncodedId, proposedValueEncodedOidStr, CollectionModificationType.REMOVE_FROM);
    }

    private String modifyCollection(
            final String oidEncodedStr,
            final String collectionEncodedId,
            final String proposedValueEncodedOidStr,
            final CollectionModificationType modification) {
        init();
        String oidStr = UrlDecoderUtils.urlDecode(oidEncodedStr);
        String collectionId = UrlDecoderUtils.urlDecode(collectionEncodedId);
        String proposedValueOidStr = UrlDecoderUtils.urlDecode(proposedValueEncodedOidStr);

        final ObjectAdapter nakedObject = getNakedObject(oidStr);
        if (nakedObject == null) {
        	throw new WebApplicationException(responseOfGone("could not determine object"));
        }

        final ObjectSpecification noSpec = nakedObject.getSpecification();

        final OneToManyAssociation collection = (OneToManyAssociation) noSpec.getAssociation(collectionId);

        ObjectAdapter proposedValueNO = null;
        proposedValueNO = getNakedObject(proposedValueOidStr);

        if (proposedValueNO == null) {
        	throw new WebApplicationException(responseOfGone("could not determine proposed value"));
        }

        // validate
        final Consent consent = modification == CollectionModificationType.ADD_TO ? collection.isValidToAdd(nakedObject,
                proposedValueNO) : collection.isValidToRemove(nakedObject, proposedValueNO);
        if (consent.isVetoed()) {
        	throw new WebApplicationException(responseOfBadRequest(consent));
        }


        // html template
        final XhtmlTemplate xhtml = new XhtmlTemplate(nakedObject.titleString() + "." + collectionId, getServletRequest());
        xhtml.appendToBody(asDivNofSession());
        xhtml.appendToBody(resourcesDiv());


        // title & Oid
        Element div = asDivTableObjectDetails(nakedObject);
        xhtml.appendToBody(div);


        // if valid, then set
        if (modification == CollectionModificationType.ADD_TO) {
            collection.addElement(nakedObject, proposedValueNO);
        } else {
            collection.removeElement(nakedObject, proposedValueNO);
        }

        return xhtml.toXML();
    }

    // /////////////////////////////////////////////////////////////////////
    // actions
    // /////////////////////////////////////////////////////////////////////

    @Override
    public String invokeAction(
            final String oidEncodedStr,
            final String actionEncodedId,
            final InputStream body) {
		init();
		
        String oidStr = UrlDecoderUtils.urlDecode(oidEncodedStr);
        String actionId = UrlDecoderUtils.urlDecode(actionEncodedId);
		final List<String> argsEncodedArray = InputStreamUtil.getArgs(body);
		String[] argsEncoded = argsEncodedArray.toArray(new String[] {});
		final String[] args = urlDecode(argsEncoded);
		
		final ObjectAdapter nakedObject = getNakedObject(oidStr);
		if (nakedObject == null) {
			throw new WebApplicationException(responseOfGone("could not determine object"));
		}
		
		final ObjectSpecification noSpec = nakedObject.getSpecification();
		final ObjectAction action = getObjectAction(noSpec, actionId);
		
		final List<ObjectActionParameter> parameters = action.getParameters();
		final int parameterCount = parameters.size();
		final int argumentCount = args.length;
		if (parameterCount > argumentCount) {
		    // this isn't an != check because JAX-RS will always give us 10 args, but some/all will be null.
		    String reason = MessageFormat.format("provided {0} parameters but {1} arguments", parameterCount, argumentCount);
		    throw new WebApplicationException(responseOfBadRequest(reason));
		}
		
		final ObjectAdapter[] proposedArguments = new ObjectAdapter[parameterCount];
		for (int i = 0; i < parameters.size(); i++) {
		    final String proposedArg = args[i];
		    if (proposedArg != null) {
		        final ObjectAdapter argNO = getObjectAdapter(proposedArg, nakedObject, parameters.get(i));
		
		        if (argNO == null) {
		        	throw new WebApplicationException(responseOfGone("could not determine proposed value"));
		        }
		        proposedArguments[i] = argNO;
		    }
		}
		
		// html template
		final XhtmlTemplate xhtml = new XhtmlTemplate(nakedObject.titleString() + "." + actionId, getServletRequest());
		xhtml.appendToBody(asDivNofSession());
		
		
		// title & Oid
		Element div = asDivTableObjectDetails(nakedObject);
		xhtml.appendToBody(div);
		
		// action Name
		div = xhtmlRenderer.div_p(actionId, HtmlClass.COLLECTION);
		xhtml.appendToBody(div);
		
		
		// TODO: should be checking if enabled, or indeed, if visible?
		// probably for modifying properties and collections too.
		
		
		// validate
		final Consent consent = action.isProposedArgumentSetValid(nakedObject, proposedArguments);
		if (consent.isVetoed()) {
			throw new WebApplicationException(responseOfBadRequest(consent));
		}
		
		// invoke the action
		final ActionInvocationFacet actionInvocationFacet = action.getFacet(ActionInvocationFacet.class);
		final ObjectAdapter result = actionInvocationFacet.invoke(nakedObject, proposedArguments);
		if (result == null) {
			// do nothing; 
			// NB: this is not a response of NO_CONTENT 
			// (not sure if it ought to be, can't see how you would return it)
		} else {
		    final Object object = result.getObject();
		    xhtml.appendToBody(actionResult(object));
		}
		
		return xhtml.toXML();
    }

    private static String[] urlDecode(String[] encodedStrings) {
    	String[] strings = new String[encodedStrings.length];
    	for (int i = 0; i < encodedStrings.length; i++) {
			strings[i] = UrlDecoderUtils.urlDecode(encodedStrings[i]);
		}
		return strings;
	}

	// TODO: this is horrid - shouldn't have to search in this way...
    private ObjectAction getObjectAction(final ObjectSpecification noSpec, final String actionId) {
        ObjectAction action = null;
        action = noSpec.getObjectAction(ActionType.USER, actionId);
        if (action != null) {
            return action;
        }
        action = noSpec.getObjectAction(ActionType.EXPLORATION, actionId);
        if (action != null) {
            return action;
        }
        action = noSpec.getObjectAction(ActionType.DEBUG, actionId);
        if (action != null) {
            return action;
        }
        return null;
    }

    private Element actionResult(final Object result) {
        final Element div_p = xhtmlRenderer.div_p("Action Results", HtmlClass.ACTION_RESULT);
        div_p.appendChild(actionResultContent(result));
        return div_p;
    }

    private Element actionResultContent(final Object result) {
        if (result == null) {
            return xhtmlRenderer.p(null, HtmlClass.ACTION_RESULT);
        }

        // deal with as collection
        if (result instanceof Collection<?>) {
            final Collection<?> collection = (Collection<?>) result;
            final Element ul = xhtmlRenderer.ul(HtmlClass.ACTION_RESULT);
            for (final Object object : collection) {
                ul.appendChild(toLiAHref(object));
            }
            return ul;
        }

        // deal with as object
        return toAHref(result);
    }

    
    ////////////////////////////////////////////////////////////////
    // Helpers
    ////////////////////////////////////////////////////////////////
    
    private Element toLiAHref(final Object object) {
        final Element li = new Element("li");
        li.appendChild(toAHref(object));
        return li;
    }

    private Element toLiAHref(final ObjectAdapter nakedObject) {
        final Element li = new Element("li");
        li.appendChild(toAHref(nakedObject));
        return li;
    }

    private Element toAHref(final Object object) {
        final ObjectAdapter nakedObject = getAdapterManager().getAdapterFor(object);
        return toAHref(nakedObject);
    }

    private Element toAHref(final ObjectAdapter nakedObject) {
        final String uri = MessageFormat.format("{0}/object/{1}", getServletRequest().getContextPath(), getOidStr(nakedObject));
        return xhtmlRenderer.aHref(uri, nakedObject.titleString(), "object", "results", HtmlClass.ACTION_RESULT);
    }

    private ObjectAdapter getObjectAdapter(
            final String proposedValue,
            final ObjectAdapter nakedObject,
            final ObjectFeature nakedObjectFeature) {
    	ObjectAdapter proposedValueNO = null;
        ParseableFacet parseable = nakedObjectFeature.getFacet(ParseableFacet.class);
        if (parseable == null) {
            parseable = nakedObjectFeature.getSpecification().getFacet(ParseableFacet.class);
        }

        if (parseable != null) {
            proposedValueNO = parseable.parseTextEntry(nakedObject, proposedValue);
        } else {
            final String proposedValueOidStr = proposedValue;
            proposedValueNO = getNakedObject(proposedValueOidStr);
        }
        return proposedValueNO;
    }

	private boolean isJavascriptDebug() {
		return IsisContext.getConfiguration().getBoolean(Constants.JAVASCRIPT_DEBUG_KEY, true);
	}

}
