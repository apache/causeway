package org.apache.isis.viewer.restful.viewer.resources.objects.properties;

import java.text.MessageFormat;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.resources.objects.TableColumnNakedObjectAssociationModifyAbstract;
import org.apache.isis.viewer.restful.viewer.util.StringUtil;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnOneToOneAssociationClear extends
        TableColumnNakedObjectAssociationModifyAbstract<OneToOneAssociation> {

    public TableColumnOneToOneAssociationClear(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super("Clear", session, nakedObject, resourceContext, false);
    }

    @Override
    protected String getFormNamePrefix() {
        return "property-";
    }

    @Override
    protected String getHtmlClassAttribute() {
        return HtmlClass.PROPERTY;
    }

    @Override
    protected String getFormButtonLabel() {
        return "Clear";
    }

    /**
     * Calls the <tt>putProperty()</tt> Javascript function that lives in <tt>nof-rest-support.js</tt>
     * 
     * @param url
     * @param associationId
     * @param inputFieldName
     *            - the name of the field in the form to read the value.
     * @return
     */
    @Override
    protected String invokeJavascript(final String url, final String associationId, final String inputFieldName) {
        return MessageFormat.format("clearProperty({0},{1});", StringUtil.quote(url), StringUtil.quote(associationId));
    }

}
