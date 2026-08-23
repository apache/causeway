/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package demoapp.dom;

import java.math.BigDecimal;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.CausewayModuleApplibChangeAndExecutionLoggers;
import org.apache.causeway.applib.CausewayModuleApplibMixins;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.valuesemantics.BigDecimalValueSemantics;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.extensions.docgen.help.CausewayModuleExtDocgenHelp;
import org.apache.causeway.testing.fixtures.applib.CausewayModuleTestingFixturesApplib;

import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmClassLoadingMeterConventions;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmCpuMeterConventions;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmMemoryMeterConventions;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmThreadMeterConventions;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.registry.otlp.OtlpConfig;

@Configuration(proxyBeanMethods = false)
@Import({
    CausewayModuleApplibChangeAndExecutionLoggers.class,
    CausewayModuleApplibMixins.class,
    CausewayModuleCoreRuntimeServices.class,
    CausewayModuleExtDocgenHelp.class,
    CausewayModuleTestingFixturesApplib.class,
})
@PropertySources({
    @PropertySource(CausewayPresets.NoTranslations),
    @PropertySource(CausewayPresets.SilenceWicket),
})
@ComponentScan(
        basePackageClasses= {
                ReferenceModuleCommon.class
        })
public class ReferenceModuleCommon {

    /**
     * value semantics for demo
     *
     * <p>Example usage:
     * <pre>@ValueSemantics(provider="euro", maxIntegerDigits = 4, minFractionalDigits = 2, maxFractionalDigits = 2)</pre>
     */
    @Component
    @Qualifier("euro")
    public static class BigDecimalEuro extends BigDecimalValueSemantics.LocaleGroupingAll {
        @Override public String htmlPresentation(final Context context, final BigDecimal value) {
            return renderTitle(value, pipe(
                getNumberFormat(context, FormatUsageFor.RENDERING_AS_HTML)::format,
                html->value.signum() == -1
                    ? toLightFontWithStyle(html, "color: red")
                    : toLightFontWithStyle(html, "")));
        }
        private final String toLightFontWithStyle(final String html, final String style) {
            return """
                <span class="fw-light" style="%s">€ %s</span>""".formatted(style, html);
        }
    }

//OpenTelemetry demo
    @Bean
    OtlpConfig otlpConfig() {
        return new OtlpConfig() {
            @Override public @Nullable String get(final String key) { return null;}
            @Override public boolean enabled() { return false; }
        };
    }

    @Bean
    OpenTelemetryJvmCpuMeterConventions openTelemetryJvmCpuMeterConventions() {
        return new OpenTelemetryJvmCpuMeterConventions(Tags.empty());
    }

    @Bean
    ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics(List.of(), new OpenTelemetryJvmCpuMeterConventions(Tags.empty()));
    }

    @Bean
    JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics(List.of(), new OpenTelemetryJvmMemoryMeterConventions(Tags.empty()));
    }

    @Bean
    JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics(List.of(), new OpenTelemetryJvmThreadMeterConventions(Tags.empty()));
    }

    @Bean
    ClassLoaderMetrics classLoaderMetrics() {
        return new ClassLoaderMetrics(new OpenTelemetryJvmClassLoadingMeterConventions());
    }

}
