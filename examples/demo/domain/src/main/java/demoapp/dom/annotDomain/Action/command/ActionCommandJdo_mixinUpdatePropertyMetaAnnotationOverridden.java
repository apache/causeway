package demoapp.dom.annotDomain.Action.command;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.CommandReification;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

//tag::class[]
@ActionCommandDisabledMetaAnnotation        // <.>
@Action(
    command = CommandReification.ENABLED    // <.>
    , semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "propertyMetaAnnotatedOverridden"
    , associateWithSequence = "2"
)
@ActionLayout(
    named = "Mixin Update Property"
    , describedAs =
        "@ActionCommandDisabledMetaAnnotation " +
        "@Action(command = ENABLED)"
)
@RequiredArgsConstructor
public class ActionCommandJdo_mixinUpdatePropertyMetaAnnotationOverridden {
    // ...
//end::class[]

    private final ActionCommandJdo actionCommandJdo;

    public ActionCommandJdo act(final String value) {
        actionCommandJdo.setPropertyMetaAnnotatedOverridden(value);
        return actionCommandJdo;
    }
    public String default0Act() {
        return actionCommandJdo.getPropertyMetaAnnotatedOverridden();
    }
//tag::class[]
}
//end::class[]
