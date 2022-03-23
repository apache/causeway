package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.util.Optional;

import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.LinkAndLabelFactory;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.ActionLink;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _Util {

    boolean canPropertyEnterInlineEditDirectly(final ScalarModel scalarModel) {
        return scalarModel.getPromptStyle().isInline()
                && scalarModel.canEnterEditMode();
    }

    Optional<ActionLink> lookupPropertyActionForInlineEdit(final ScalarModel scalarModel) {
        val inlineActionIfAny =
                scalarModel.getAssociatedActions().getFirstAssociatedWithInlineAsIfEdit();

        if(canPropertyEnterInlineEditDirectly(scalarModel)) {
            return Optional.empty();
        }

        // not editable property, but maybe one of the actions is.
        return inlineActionIfAny
        .map(LinkAndLabelFactory.forPropertyOrParameter(scalarModel))
        .map(LinkAndLabel::getUiComponent)
        .map(ActionLink.class::cast)
        .filter(ActionLink::isVisible)
        .filter(ActionLink::isEnabled);
    }

}
