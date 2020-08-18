package demoapp.dom.annotDomain.Action.publishing;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.SemanticsOf;

//tag::class[]
@ActionPublishingEnabledMetaAnnotation     // <.>
@Action(
    semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "propertyMetaAnnotated"
    , associateWithSequence = "2"
)
@ActionLayout(
    named = "Mixin Update Property"
    , describedAs = "@ActionPublishingEnabledMetaAnnotation"
)
public class ActionPublishingJdo_mixinUpdatePropertyMetaAnnotation {
    // ...
//end::class[]

    private final ActionPublishingJdo actionPublishingJdo;

    public ActionPublishingJdo_mixinUpdatePropertyMetaAnnotation(ActionPublishingJdo actionPublishingJdo) {
        this.actionPublishingJdo = actionPublishingJdo;
    }

//tag::class[]
    public ActionPublishingJdo act(final String value) {
        actionPublishingJdo.setPropertyMetaAnnotated(value);
        return actionPublishingJdo;
    }
    public String default0Act() {
        return actionPublishingJdo.getPropertyMetaAnnotated();
    }
}
//end::class[]
