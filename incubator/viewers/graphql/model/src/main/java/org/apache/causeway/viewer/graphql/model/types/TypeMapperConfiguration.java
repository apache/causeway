package org.apache.causeway.viewer.graphql.model.types;

import org.apache.causeway.core.config.CausewayConfiguration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TypeMapperConfiguration {

    @Bean
    @ConditionalOnMissingBean(TypeMapper.class)
    public TypeMapper defaultTypeMapper(final CausewayConfiguration causewayConfiguration) {
        return new TypeMapperDefault(causewayConfiguration);
    }
}
