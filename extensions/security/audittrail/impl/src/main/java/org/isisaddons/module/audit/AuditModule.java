package org.isisaddons.module.audit;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.isis.applib.ModuleAbstract;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.fixturescripts.teardown.TeardownFixtureAbstract;

import org.isisaddons.module.audit.dom.AuditEntry;

@XmlRootElement(name = "module")
public class AuditModule extends ModuleAbstract {

    public abstract static class ActionDomainEvent<S> extends org.apache.isis.applib.services.eventbus.ActionDomainEvent<S> { }

    public abstract static class CollectionDomainEvent<S,T> extends org.apache.isis.applib.services.eventbus.CollectionDomainEvent<S,T> { }

    public abstract static class PropertyDomainEvent<S,T> extends org.apache.isis.applib.services.eventbus.PropertyDomainEvent<S,T> { }

    @Override
    public FixtureScript getTeardownFixture() {
        return new TeardownFixtureAbstract() {
            @Override
            protected void execute(final FixtureScript.ExecutionContext executionContext) {
                deleteFrom(AuditEntry.class);
            }
        };
    }

}
