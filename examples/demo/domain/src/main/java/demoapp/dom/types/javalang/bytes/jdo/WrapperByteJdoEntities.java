package demoapp.dom.types.javalang.bytes.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class WrapperByteJdoEntities {

    public Optional<WrapperByteJdo> find(final Byte readOnlyProperty) {
        return repositoryService.firstMatch(WrapperByteJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<WrapperByteJdo> all() {
        return repositoryService.allInstances(WrapperByteJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
