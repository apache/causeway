package org.apache.isis.codegen.bytebuddy;

import org.apache.isis.commons.IsisModuleCommons;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        // modules
        IsisModuleCommons.class,
})
public class IsisModuleCodegenByteBuddy {
}
