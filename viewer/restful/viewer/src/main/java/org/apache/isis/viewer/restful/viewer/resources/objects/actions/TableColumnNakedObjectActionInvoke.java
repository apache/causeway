package org.apache.isis.viewer.restful.viewer.resources.objects.actions;

import java.text.MessageFormat;
import java.util.List;

import nu.xom.Attribute;
import nu.xom.Element;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnNakedObjectActionInvoke extends TableColumnNakedObjectAction {

    private final static String INPUT_FIELD_NAME_PREFIX = "arg";

    public TableColumnNakedObjectActionInvoke(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super("Invoke", session, nakedObject, resourceContext);
    }

    @Override
    public Element doTd(final ObjectAction action) {
        final Identifier actionIdentifier = action.getIdentifier();
        final String actionId = actionIdentifier.toNameParmsIdentityString();

        final String formName = "action-" + actionId;
        final String uri = MessageFormat.format("{0}/object/{1}/action/{2}",
                resourceContext.getHttpServletRequest().getContextPath(), getOidStrRealTarget(action), actionId);
        final List<ObjectActionParameter> parameters = action.getParameters();

        final Element div = xhtmlRenderer.div(HtmlClass.ACTION);
        div.appendChild(renderForm(formName, uri, parameters));
        return div;
    }

	private Element renderForm(final String formName, final String uri, final List<ObjectActionParameter> parameters) {

        final Element form = xhtmlRenderer.form(formName, HtmlClass.ACTION);
        form.addAttribute(new Attribute("method", "POST"));
        form.addAttribute(new Attribute("action", uri));

        renderInputFieldsForParameters(parameters, form);

        renderInputButton(form);

        return form;
    }

   	private void renderInputFieldsForParameters(final List<ObjectActionParameter> parameters, final Element form) {
        for (int i = 0; i < parameters.size(); i++) {
            final ObjectActionParameter parameter = parameters.get(i);
            final String inputFieldName = INPUT_FIELD_NAME_PREFIX + i;

            final Element inputLabel = new Element("p");
            inputLabel.appendChild(parameter.getName());
            form.appendChild(inputLabel);

            final Element inputValue = new Element("input");
            inputValue.addAttribute(new Attribute("type", "value"));
            inputValue.addAttribute(new Attribute("name", inputFieldName));
            form.appendChild(inputValue);
        }
    }

    private void renderInputButton(final Element form) {
        final Element inputButton = new Element("input");
        inputButton.addAttribute(new Attribute("type", "submit"));
        inputButton.addAttribute(new Attribute("value", "Invoke"));
        form.appendChild(inputButton);
    }


}
