package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.util.List;
import java.util.Optional;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.schema.common.v2.ValueType;
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

    Optional<ActionLink> lookupPropertyActionForCompositeUpdate(final ScalarModel scalarModel) {
        if(!canPropertyEnterInlineEditDirectly(scalarModel)) {
            return Optional.empty();
        }
        val compositeValueSemantics = lookupCompositeValueSemantics(scalarModel)
        .orElse(null);

        //TODO get compositeValueUpdateMixin from compositeValueSemantics

        ObjectAction compositeValueUpdateMixin = null;

        return toActionLink(compositeValueUpdateMixin, scalarModel);
    }

    Optional<ActionLink> lookupPropertyActionForInlineEdit(final ScalarModel scalarModel) {
        if(canPropertyEnterInlineEditDirectly(scalarModel)) {
            return Optional.empty();
        }
        // not editable property, but maybe one of the actions is.
        return scalarModel.getAssociatedActions()
                .getFirstAssociatedWithInlineAsIfEdit()
                .flatMap(action->toActionLink(action, scalarModel));
    }

    private Optional<ValueSemanticsProvider<?>> lookupCompositeValueSemantics(final ScalarModel scalarModel) {
        return scalarModel.lookupDefaultValueSemantics()
                .filter(valueSemantics->valueSemantics.getSchemaValueType()==ValueType.COMPOSITE);
    }

    private Optional<ActionLink> toActionLink(
            final @Nullable ObjectAction action,
            final ScalarModel scalarModel) {

        return Optional.ofNullable(action)
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

        //XXX debug
        System.err.printf("RECOVERPROPOSEDVALUE %s%n", validatable.getValue());

        return Optional.ofNullable(scalarModel.getObjectManager()
                .adapt(validatable.getValue()));
    }

    // -- PROBABLY NO LONGER NEEDED

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
