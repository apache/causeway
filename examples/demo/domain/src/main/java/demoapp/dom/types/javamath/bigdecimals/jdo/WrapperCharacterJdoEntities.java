package demoapp.dom.types.javamath.bigdecimals.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class JavaMathBigDecimalJdoEntities {

    public Optional<JavaMathBigDecimalJdo> find(final Character readOnlyProperty) {
        return repositoryService.firstMatch(JavaMathBigDecimalJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<JavaMathBigDecimalJdo> all() {
        return repositoryService.allInstances(JavaMathBigDecimalJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
