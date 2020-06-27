package demoapp.dom.types.wrapper.characters.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class WrapperCharacterJdoEntities {

    public Optional<WrapperCharacterJdoEntity> find(final Character readOnlyProperty) {
        return repositoryService.firstMatch(WrapperCharacterJdoEntity.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<WrapperCharacterJdoEntity> all() {
        return repositoryService.allInstances(WrapperCharacterJdoEntity.class);
    }

    @Inject
    RepositoryService repositoryService;

}
