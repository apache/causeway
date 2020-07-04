package demoapp.dom.types.isis.asciidoc.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class IsisAsciiDocJdoEntities {

    public Optional<IsisAsciiDocJdo> find(final Character readOnlyProperty) {
        return repositoryService.firstMatch(IsisAsciiDocJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<IsisAsciiDocJdo> all() {
        return repositoryService.allInstances(IsisAsciiDocJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
