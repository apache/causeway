package demoapp.dom;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@ConfigurationProperties(AppConfiguration.ROOT_PREFIX)
@Data
@Validated
@Component
public class AppConfiguration {
    public static final String ROOT_PREFIX = "app";

    private final Geoapify geoapify = new Geoapify();
    @Data
    public static class Geoapify {
        private String apiKey = "e9c7fb3f3255479bbeb1b9dc8a8fab86";
    }
}
