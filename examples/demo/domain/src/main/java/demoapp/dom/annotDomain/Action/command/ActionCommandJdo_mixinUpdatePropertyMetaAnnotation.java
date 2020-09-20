package demoapp.dom.annotDomain.Action.command;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

//tag::class[]
@ActionCommandEnabledMetaAnnotation     // <.>
@Action(
    semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "propertyMetaAnnotated"
    , associateWithSequence = "2"
)
@ActionLayout(
    named = "Mixin Update Property"
    , describedAs = "@ActionPublishingEnabledMetaAnnotation"
)
@RequiredArgsConstructor
public class ActionCommandJdo_mixinUpdatePropertyMetaAnnotation {

    private final ActionCommandJdo actionCommandJdo;

    public ActionCommandJdo act(final String value) {
        actionCommandJdo.setPropertyMetaAnnotated(value);
        return actionCommandJdo;
    }
    public String default0Act() {
        return actionCommandJdo.getPropertyMetaAnnotated();
    }
}
//end::class[]
