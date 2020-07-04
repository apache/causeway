package demoapp.dom.types.isisext.asciidocs.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

@Service
public class IsisAsciiDocJdoEntities {

    public Optional<IsisAsciiDocJdo> find(final AsciiDoc readOnlyProperty) {
        return repositoryService.firstMatch(IsisAsciiDocJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<IsisAsciiDocJdo> all() {
        return repositoryService.allInstances(IsisAsciiDocJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
