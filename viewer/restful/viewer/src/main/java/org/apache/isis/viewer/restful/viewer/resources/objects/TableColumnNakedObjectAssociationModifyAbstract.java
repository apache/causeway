package org.apache.isis.viewer.restful.viewer.resources.objects;

import java.text.MessageFormat;

import nu.xom.Attribute;
import nu.xom.Element;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public abstract class TableColumnNakedObjectAssociationModifyAbstract<T extends ObjectAssociation> extends
        TableColumnNakedObjectAssociation<T> {

    private final boolean inputField;

    public TableColumnNakedObjectAssociationModifyAbstract(
            final String columnName,
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        this(columnName, session, nakedObject, resourceContext, true);
    }

    /**
     * @param field
     *            - whether to include an input field.
     */
    public TableColumnNakedObjectAssociationModifyAbstract(
            final String columnName,
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext,
            final boolean field) {
        super(columnName, session, nakedObject, resourceContext);
        this.inputField = field;
    }

    @Override
    public Element doTd(final T association) {
        if (!association.isVisible(getSession(), getNakedObject()).isAllowed()) {
            return xhtmlRenderer.p(null, getHtmlClassAttribute());
        }
        if (!association.isUsable(getSession(), getNakedObject()).isAllowed()) {
            return xhtmlRenderer.p(null, getHtmlClassAttribute());
        }

        final Element div = xhtmlRenderer.div(getHtmlClassAttribute());

        div.appendChild(form(association));
        return div;
    }

    private Element form(final T association) {
        final String associationId = association.getId();
        final String formName = getFormNamePrefix() + associationId;
        final Element form = xhtmlRenderer.form(formName, getHtmlClassAttribute());

        final String inputFieldName = "proposedValue";
        if (inputField) {
            final Element inputValue = new Element("input");
            inputValue.addAttribute(new Attribute("type", "value"));
            inputValue.addAttribute(new Attribute("name", inputFieldName));
            form.appendChild(inputValue);
        }

        final Element inputButton = new Element("input");
        inputButton.addAttribute(new Attribute("type", "button"));
        inputButton.addAttribute(new Attribute("value", getFormButtonLabel()));
        final String servletContextName = getContextPath();
        final String url = MessageFormat.format("{0}/object/{1}", servletContextName, getOidStr());
        inputButton.addAttribute(new Attribute("onclick", invokeJavascript(url, associationId, inputFieldName)));
        form.appendChild(inputButton);

        return form;
    }

    protected abstract String getFormButtonLabel();

    /**
     * Used to construct the <tt>&lt;form name=&quot;xxx&quot;&gt;</tt> that holds the value used to make the
     * change.
     * 
     * @return
     */
    protected abstract String getFormNamePrefix();

    /**
     * Used HTML Class attribute used variously throughout the rendered HTML form.
     * 
     * @return
     */
    protected abstract String getHtmlClassAttribute();

    /**
     * Invoke the appropriate Javascript function from <tt>nof-rest-support.js</tt>.
     * 
     * @param url
     * @param associationId
     * @param inputFieldName
     * @return
     */
    protected abstract String invokeJavascript(String url, String associationId, String inputFieldName);

}
