package demoapp.dom.annotDomain.Action.publishing;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.SemanticsOf;

//tag::class[]
@Action(
    publishing = Publishing.ENABLED
    , semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "property"
    , associateWithSequence = "3"
)
@ActionLayout(
    describedAs = "@Action(publishing = ENABLED)"
)
public class ActionPublishingJdo_mixinUpdateProperty {
    // ...
//end::class[]

    private final ActionPublishingJdo actionPublishingJdo;

    public ActionPublishingJdo_mixinUpdateProperty(ActionPublishingJdo actionPublishingJdo) {
        this.actionPublishingJdo = actionPublishingJdo;
    }

//tag::class[]
    public ActionPublishingJdo act(final String value) {
        actionPublishingJdo.setProperty(value);
        return actionPublishingJdo;
    }
    public String default0Act() {
        return actionPublishingJdo.getProperty();
    }
}
//end::class[]
