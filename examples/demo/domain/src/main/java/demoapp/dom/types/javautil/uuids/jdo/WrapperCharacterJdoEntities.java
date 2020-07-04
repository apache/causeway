package demoapp.dom.types.javautil.uuids.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class WrapperCharacterJdoEntities {

    public Optional<WrapperCharacterJdo> find(final Character readOnlyProperty) {
        return repositoryService.firstMatch(WrapperCharacterJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<WrapperCharacterJdo> all() {
        return repositoryService.allInstances(WrapperCharacterJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
