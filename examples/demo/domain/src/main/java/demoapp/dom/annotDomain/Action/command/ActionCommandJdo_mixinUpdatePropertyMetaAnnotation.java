package demoapp.dom.annotDomain.Action.command;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

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
public class ActionCommandJdo_mixinUpdatePropertyMetaAnnotation {
    // ...
//end::class[]

    private final ActionCommandJdo actionCommandJdo;

    public ActionCommandJdo_mixinUpdatePropertyMetaAnnotation(ActionCommandJdo actionCommandJdo) {
        this.actionCommandJdo = actionCommandJdo;
    }

//tag::class[]
    public ActionCommandJdo act(final String value) {
        actionCommandJdo.setPropertyMetaAnnotated(value);
        return actionCommandJdo;
    }
    public String default0Act() {
        return actionCommandJdo.getPropertyMetaAnnotated();
    }
}
//end::class[]
