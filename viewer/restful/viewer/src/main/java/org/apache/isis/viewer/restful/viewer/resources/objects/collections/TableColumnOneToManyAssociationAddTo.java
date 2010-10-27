package org.apache.isis.viewer.restful.viewer.resources.objects.collections;

import java.text.MessageFormat;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.resources.objects.TableColumnNakedObjectAssociationModifyAbstract;
import org.apache.isis.viewer.restful.viewer.util.StringUtil;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnOneToManyAssociationAddTo extends
        TableColumnNakedObjectAssociationModifyAbstract<OneToManyAssociation> {

    public TableColumnOneToManyAssociationAddTo(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super("AddTo", session, nakedObject, resourceContext);
    }

    @Override
    protected String getFormNamePrefix() {
        return "collection-";
    }

    @Override
    protected String getHtmlClassAttribute() {
        return HtmlClass.COLLECTION;
    }

    @Override
    protected String getFormButtonLabel() {
        return "Add";
    }

    /**
     * Calls the <tt>addToCollection()</tt> Javascript function that lives in <tt>nof-rest-support.js</tt>
     * 
     * @param url
     * @param associationId
     * @param inputFieldName
     *            - the name of the field in the form to read the value.
     * @return
     */
    @Override
    protected String invokeJavascript(final String url, final String associationId, final String inputFieldName) {
        return MessageFormat.format("addToCollection({0}, {1}, {2}.value);", StringUtil.quote(url), StringUtil
                .quote(associationId), inputFieldName);
    }

}
