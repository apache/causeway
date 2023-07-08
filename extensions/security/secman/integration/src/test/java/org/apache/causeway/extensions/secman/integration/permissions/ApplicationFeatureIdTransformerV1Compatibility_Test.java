package org.apache.causeway.extensions.secman.integration.permissions;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureSort;
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
    void member_when_its_owning_type_exists() {
        ApplicationFeatureId input = ApplicationFeatureId.newMember("customer.Customer", "lastName");
        ApplicationFeatureId transformed = transformer.transform(input);

        assertThat(transformed.getSort()).isEqualTo(ApplicationFeatureSort.MEMBER);
        assertThat(transformed.getLogicalTypeName()).isEqualTo(Customer.class.getName());
        assertThat(transformed.getLogicalMemberName()).isEqualTo("lastName");
    }

    @Test
    void member_when_its_owning_type_does_not_exist() {
        ApplicationFeatureId input = ApplicationFeatureId.newMember("order.Order", "placedOn");
        ApplicationFeatureId transformed = transformer.transform(input);

        assertThat(transformed.getSort()).isEqualTo(ApplicationFeatureSort.MEMBER);
        assertThat(transformed.getLogicalTypeName()).isEqualTo("order.Order");
        assertThat(transformed.getLogicalMemberName()).isEqualTo("placedOn");
    }

    @Test
    void type_when_exists() {
        ApplicationFeatureId input = ApplicationFeatureId.newType("customer.Customer");
        ApplicationFeatureId transformed = transformer.transform(input);

        assertThat(transformed.getSort()).isEqualTo(ApplicationFeatureSort.TYPE);
        assertThat(transformed.getLogicalTypeName()).isEqualTo(Customer.class.getName());
        assertThat(transformed.getLogicalMemberName()).isNull();
    }


    @Test
    void type_when_does_not_exist() {
        ApplicationFeatureId input = ApplicationFeatureId.newType("order.Order");
        ApplicationFeatureId transformed = transformer.transform(input);

        assertThat(transformed.getSort()).isEqualTo(ApplicationFeatureSort.TYPE);
        assertThat(transformed.getLogicalTypeName()).isEqualTo("order.Order");
        assertThat(transformed.getLogicalMemberName()).isNull();
    }

}