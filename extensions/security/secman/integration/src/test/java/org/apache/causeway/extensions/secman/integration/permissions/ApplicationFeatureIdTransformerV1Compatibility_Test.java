package org.apache.causeway.extensions.secman.integration.permissions;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ApplicationFeatureIdTransformerV1Compatibility_Test {

    public static class Customer {}

    @Mock SpecificationLoader mockSpecificationLoader;
    @Mock ObjectSpecification mockSpecificationForCustomerClass;

    ApplicationFeatureIdTransformer transformer;

    @BeforeEach
    void setup() {
        transformer = new ApplicationFeatureIdTransformerV1Compatibility(mockSpecificationLoader);

        lenient().when(mockSpecificationLoader.specForLogicalTypeName("customer.Customer")).thenReturn(Optional.of(mockSpecificationForCustomerClass));
        lenient().when(mockSpecificationForCustomerClass.getCorrespondingClass()).then(__ -> Customer.class);
    }

    @Test
    void happy_case() {
        ApplicationFeatureId input = ApplicationFeatureId.newMember("customer.Customer", "lastName");
        ApplicationFeatureId transform = transformer.transform(input);

        assertThat(transform.getLogicalTypeName()).isEqualTo(Customer.class.getName());
    }

}