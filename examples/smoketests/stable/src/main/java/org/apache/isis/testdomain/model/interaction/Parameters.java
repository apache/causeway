package org.apache.isis.testdomain.model.interaction;

import lombok.Value;
import lombok.experimental.Accessors;

final class Parameters {
    
    @Value @Accessors(fluent = true)
    public static class BiInt {
        int a;
        int b;
    }
    
    @Value @Accessors(fluent = true)
    public static class TriInt {
        int a;
        int b;
        int c;
    }
    
    @Value @Accessors(fluent = true)
    public static class TriEnum {
        DemoEnum a;
        DemoEnum b;
        DemoEnum c;
    }
    
}
