package org.apache.isis.extensions.executionoutbox.applib.restapi;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.extensions.executionoutbox.applib.IsisModuleExtExecutionOutboxApplib;
import org.apache.isis.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlRootElement
@XmlType(
        propOrder = {
                "executions"
        }
)
@Named(OutboxEvents.LOGICAL_TYPE_NAME)
@DomainObject(nature = Nature.VIEW_MODEL)
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
public class OutboxEvents  {

    static final String LOGICAL_TYPE_NAME = IsisModuleExtExecutionOutboxApplib.NAMESPACE + ".OutboxEvents";

    public String title() {
        return String.format("%d executions", executions.size());
    }

    @Collection
    @CollectionLayout(defaultView = "table")
    @XmlElementWrapper()
    @XmlElement(name="event")
    @Getter @Setter
    private List<ExecutionOutboxEntry> executions = new ArrayList<>();

}
