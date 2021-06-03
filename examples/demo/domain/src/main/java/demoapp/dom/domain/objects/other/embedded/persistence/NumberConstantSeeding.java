package demoapp.dom.domain.objects.other.embedded.persistence;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.other.embedded.ComplexNumber;

@Service
public class NumberConstantSeeding
extends SeedServiceAbstract {

    @Inject
    public NumberConstantSeeding(ValueHolderRepository<ComplexNumber, ? extends NumberConstantEntity> entities) {
        super(entities);
    }

}
