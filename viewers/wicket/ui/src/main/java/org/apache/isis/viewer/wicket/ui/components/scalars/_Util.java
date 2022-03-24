package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.util.List;
import java.util.Optional;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ManagedObject;
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

    IValidator<Object> createValidatorFor(final ScalarModel scalarModel) {
        return new IValidator<Object>() {
            private static final long serialVersionUID = 1L;
            @Override
            public void validate(final IValidatable<Object> validatable) {
                recoverProposedValue(validatable, scalarModel)
                .ifPresent(proposedAdapter->{
                    _Strings.nonEmpty(scalarModel.validate(proposedAdapter))
                    .ifPresent(validationFeedback->
                        validatable.error(new ValidationError(validationFeedback)));
                });
            }
        };
    }

    private Optional<ManagedObject> recoverProposedValue(
            final IValidatable<Object> validatable,
            final ScalarModel scalarModel){

        System.err.printf("%s%n", "RECOVERPROPOSEDVALUE");

        return Optional.ofNullable(scalarModel.getObjectManager()
                .adapt(validatable.getValue()));
    }

    private Optional<ManagedObject> recoverProposedValue2(
            final IValidatable<Object> validatable,
            final ScalarModel scalarModel){
        return mementoForProposedValue(validatable, scalarModel)
                .map(scalarModel.getCommonContext()::reconstructObject);
    }

    private Optional<ObjectMemento> mementoForProposedValue(
            final IValidatable<Object> validatable,
            final ScalarModel scalarModel) {
        final Object proposedValueObj = validatable.getValue();

        if (proposedValueObj instanceof List) {
            @SuppressWarnings("unchecked")
            val proposedValueObjAsList = (List<ObjectMemento>) proposedValueObj;
            if (proposedValueObjAsList.isEmpty()) {
                return Optional.empty();
            }
            val memento = proposedValueObjAsList.get(0);
            val logicalType = memento.getLogicalType();
            return Optional.of(ObjectMemento.pack(proposedValueObjAsList, logicalType));
        } else {
            return Optional.of((ObjectMemento) proposedValueObj);
        }

    }

}
