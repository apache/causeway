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
package org.apache.causeway.commons.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBContextFactory;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.codec._DocumentFactories;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.functions._Functions;
import org.apache.causeway.commons.internal.reflection._ClassCache;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * Utilities to convert from and to JAXB-XML format.
 * @implNote instead of using {@link JAXBContext#newInstance(Class...)},
 *      which does lookup the JaxbContextFactory on each call,
 *      and which - depending on system-properties - could change during the lifetime of an application,
 *      we rather utilize the com.sun.xml.bind.v2.ContextFactory directly.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
public class JaxbUtils {

    private final static Class<?> JAXB_CONTEXT_FACTORY = com.sun.xml.bind.v2.ContextFactory.class;
    private final static Map<String,Object> JAXB_CONTEXT_FACTORY_PROPS = Collections.<String,Object>emptyMap();
    private final static MethodHandle JAXB_CONTEXT_FACTORY_METHOD_HANDLE = jaxbContextFactoryMethodHandle();

    @SneakyThrows
    private static MethodHandle jaxbContextFactoryMethodHandle() {
        return MethodHandles.publicLookup()
                .findStatic(JAXB_CONTEXT_FACTORY, "createContext",
                        MethodType.methodType(JAXBContext.class, Class[].class, Map.class));
    }

    @Data @Builder
    public static class JaxbOptions {
        private final @Builder.Default boolean useContextCache = true;
        private final @Builder.Default boolean allowMissingRootElement = false;
        private final @Builder.Default boolean formattedOutput = true;
        private final @Singular Map<String, Object> properties;
        private final @Builder.Default @NonNull Consumer<Marshaller> marshallerConfigurer = _Functions.noopConsumer();
        private final @Builder.Default @NonNull Consumer<Unmarshaller> unmarshallerConfigurer = _Functions.noopConsumer();
        private final @Nullable JAXBContext jaxbContextOverride;
        public static JaxbOptions defaults() {
            return JaxbOptions.builder().build();
        }

        // -- HELPER

        private boolean shouldMissingXmlRootElementBeHandledOn(final Class<?> mappedType) {
            return isAllowMissingRootElement()
                    // looking for presence of XmlRootElement annotation
                    && !_ClassCache.getInstance().hasJaxbRootElementSemantics(mappedType);
        }
        @SneakyThrows
        private JAXBContext jaxbContext(final Class<?> mappedType) {
            return jaxbContextOverride!=null
                    ? jaxbContextOverride
                    : jaxbContextFor(mappedType, useContextCache);
        }
        @SneakyThrows
        private Marshaller marshaller(final JAXBContext jaxbContext, final Class<?> mappedType) {
            var marshaller = jaxbContext.createMarshaller();
            if(properties!=null) {
                for(var entry : properties.entrySet()) {
                    marshaller.setProperty(entry.getKey(), entry.getValue());
                }
            }
            if(isFormattedOutput()) {
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            }
            return marshaller;
        }
        @SneakyThrows
        private Unmarshaller unmarshaller(final JAXBContext jaxbContext, final Class<?> mappedType) {
            var unmarshaller = jaxbContext.createUnmarshaller();
            if(properties!=null) {
                for(var entry : properties.entrySet()) {
                    unmarshaller.setProperty(entry.getKey(), entry.getValue());
                }
            }
            return unmarshaller;
        }
        @SneakyThrows
        private <T> T unmarshal(final Unmarshaller unmarshaller, final Class<T> mappedType, final InputStream is) {
            unmarshallerConfigurer.accept(unmarshaller);
            return shouldMissingXmlRootElementBeHandledOn(mappedType)
                    ? unmarshalTypesafe(unmarshaller, mappedType, is)
                    : _Casts.castTo(mappedType, unmarshaller.unmarshal(is))
                        .orElseGet(()->unmarshalTypesafe(unmarshaller, mappedType, is));
        }
        @SneakyThrows
        private <T> T unmarshalTypesafe(final Unmarshaller unmarshaller, final Class<T> mappedType, final InputStream is) {
            var xsr = _DocumentFactories.xmlInputFactory().createXMLStreamReader(is);
            final JAXBElement<T> userElement = unmarshaller.unmarshal(xsr, mappedType);
            return userElement.getValue();
        }
        @SneakyThrows
        private <T> void marshal(final Marshaller marshaller, final T pojo, final OutputStream os) {
            marshallerConfigurer.accept(marshaller);
            @SuppressWarnings("unchecked")
            var mappedType = (Class<T>)pojo.getClass();
            if(shouldMissingXmlRootElementBeHandledOn(mappedType)) {
                var qName = new QName("", mappedType.getSimpleName());
                var jaxbElement = new JAXBElement<T>(qName, mappedType, null, pojo);
                marshaller.marshal(jaxbElement, os);
            } else {
                marshaller.marshal(pojo, os);
            }
        }
        private <T> T unmarshal(final JAXBContext jaxbContext, final Class<T> mappedType, final InputStream is) {
            return unmarshal(unmarshaller(jaxbContext, mappedType), mappedType, is);
        }
        private <T> void marshal(final JAXBContext jaxbContext, final T pojo, final OutputStream os) {
            @SuppressWarnings("unchecked")
            var mappedType = (Class<T>)pojo.getClass();
            marshal(marshaller(jaxbContext, mappedType), pojo, os);
        }
        private <T> T unmarshal(final Class<T> mappedType, final InputStream is) {
            return unmarshal(jaxbContext(mappedType), mappedType, is);
        }
        private <T> void marshal(final T pojo, final OutputStream os) {
            @SuppressWarnings("unchecked")
            var mappedType = (Class<T>)pojo.getClass();
            marshal(jaxbContext(mappedType), pojo, os);
        }
    }

    @FunctionalInterface
    public interface JaxbCustomizer extends UnaryOperator<JaxbOptions.JaxbOptionsBuilder> {
    }

    /**
     * Optionally extends the {@link JaxbCustomizer} to also customize the {@link TransformerFactory}, for pretty
     * printing (JDK9+ backwards compatibility, as per <a href="https://bugs.openjdk.org/browse/JDK-8262285">JDK-8262285</a>).
     *
     * <p>
     *     This is only ever used if the {@link JaxbCustomizer}s leave the {@link JaxbOptions}
     *     with {@link JaxbOptions#isFormattedOutput() formattedOutput} set to <code>true</code> (the default).
     * </p>
     */
    public interface TransformerFactoryCustomizer extends JaxbCustomizer {
        void apply(TransformerFactory transformerFactory);
    }

    // -- MAPPER

    public <T> DtoMapper<T> mapperFor(final @NonNull Class<T> mappedType, final JaxbUtils.JaxbCustomizer ... customizers) {

        var opts = createOptions(customizers);
        var jaxbContext = opts.jaxbContext(mappedType); // cached with this instance of DtoMapper

        return new DtoMapper<T>() {

            @Override
            public T read(final DataSource source) {
                return source.tryReadAll((final InputStream is)->{
                    return Try.call(()->opts.unmarshal(jaxbContext, mappedType, is));
                })
                .ifFailureFail()
                .getValue().orElseThrow();
            }

            @Override
            public void write(final T dto, final DataSink sink) {
                if(dto==null) return;
                sink.writeAll(os->Try.run(()->opts.marshal(jaxbContext, dto, os)));
            }

        };
    }

    // -- READING

    /**
     * Tries to deserialize JAXB-XML content from given UTF8 encoded {@link String}
     * into an instance of given {@code mappedType}.
     */
    public <T> Try<T> tryRead(
            final @NonNull Class<T> mappedType,
            final @Nullable String stringUtf8,
            final JaxbUtils.JaxbCustomizer ... customizers) {
        return tryRead(mappedType, DataSource.ofStringUtf8(stringUtf8), customizers);
    }

    /**
     * Tries to deserialize JAXB-XML content from given {@link DataSource} into an instance of
     * given {@code mappedType}.
     */
    public <T> Try<T> tryRead(
            final @NonNull Class<T> mappedType,
            final @NonNull DataSource source,
            final JaxbUtils.JaxbCustomizer ... customizers) {
        return source.tryReadAll((final InputStream is)->{
            var opts = createOptions(customizers);
            return Try.call(()->opts.unmarshal(mappedType, is))
                    .mapFailure(cause->verboseException("unmarshalling XML", mappedType, cause));
        });
    }

    // -- WRITING

    /**
     * Writes given {@code pojo} to given {@link DataSink}.
     */
    public <T> void write(
            final @Nullable T pojo,
            final @NonNull DataSink sink,
            final JaxbUtils.JaxbCustomizer ... customizers) {
        if(pojo==null) return;
        var opts = createOptions(customizers);
        try {
            sink.writeAll(os->opts.marshal(pojo, os));
        } catch (Exception cause) {
            throw verboseException("marshalling domain object to XML", pojo.getClass(), cause);
        }
    }

    /**
     * Converts given {@code pojo} to an UTF8 encoded {@link String}.
     * @return <code>null</code> if pojo is <code>null</code>
     */
    @Nullable
    public <T> String toStringUtf8(
            final @Nullable T pojo,
            final JaxbUtils.JaxbCustomizer ... customizers) {
        if(pojo==null) return null;
        var sb = new StringBuilder();
        write(pojo, DataSink.ofStringUtf8Consumer(sb), customizers);

        var xml = sb.toString();
        if (isFormattedOutput(customizers)) {
            return prettyPrint(customizers, xml);
        }

        return xml;
    }

    private static String prettyPrint(final JaxbCustomizer[] customizers, final String xml) {
        try {
            var transformerFactory = _DocumentFactories.transformerFactory();
            transformerFactory.setAttribute("indent-number", 4); // default, but can be overwritten by customizers.
            apply(customizers, transformerFactory);

            var xmlInput = new StreamSource(new StringReader(xml));
            var xsltSource = new StreamSource(JaxbUtils.class.getResourceAsStream("prettyprint.xslt"));
            var stringWriter = new StringWriter();
            var xmlOutput = new StreamResult(stringWriter);

            var transformer = transformerFactory.newTransformer(xsltSource);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            transformer.transform(xmlInput, xmlOutput);
            try (var writer = xmlOutput.getWriter()) {
                return writer.toString();
            }
        } catch (TransformerException | IOException e) {
            return xml;
        }
    }

    private static void apply(final JaxbCustomizer[] customizers, final TransformerFactory transformerFactory) {
        for (var customizer : customizers) {
            if (customizer instanceof  TransformerFactoryCustomizer) {
                var transformerFactoryCustomizer = (TransformerFactoryCustomizer) customizer;
                transformerFactoryCustomizer.apply(transformerFactory);
            }
        }
    }

    private static boolean isFormattedOutput(final JaxbCustomizer[] customizers) {
        JaxbOptions.JaxbOptionsBuilder builder = JaxbOptions.builder();
        for (var customizer : customizers) {
            customizer.apply(builder);
        }
        var jaxbOptions = builder.build();
        boolean formattedOutput = jaxbOptions.formattedOutput;
        return formattedOutput;
    }

    // -- MAPPER FACTORY

    private JaxbOptions createOptions(
            final JaxbUtils.JaxbCustomizer ... customizers) {
        var opts = JaxbOptions.builder();
        for(var customizer : customizers) {
            opts = Optional.ofNullable(customizer.apply(opts))
                    .orElse(opts);
        }
        return opts.build();
    }

    // -- GENERATE XSD

    /**
     * Generates the schema documents for given {@link JAXBContext} and writes them to given
     * {@link DataSink}.
     */
    public void generateSchema(final @NonNull JAXBContext jaxbContext, final DataSink dataSink) {
        dataSink.writeAll(os->{
            var schemaOutputResolver = new SchemaOutputResolver() {
                @Override
                public Result createOutput(final String namespaceURI, final String suggestedFileName) {
                    return new StreamResult(os);
                }
            };
            jaxbContext.generateSchema(schemaOutputResolver);
        });
    }

    // -- JAXB CONTEXT FACTORIES AND CACHING

    /** not cached */
    public static JAXBContext jaxbContextFor(final @NonNull Class<?> primaryClass, final Class<?> ... additionalClassesToBeBound) {
        return contextOf(_Arrays.combine(primaryClass, additionalClassesToBeBound));
    }

    private static Map<Class<?>, JAXBContext> jaxbContextByClass = _Maps.newConcurrentHashMap();

    public static JAXBContext jaxbContextFor(final Class<?> dtoClass, final boolean useCache)  {
        return useCache
                ? jaxbContextByClass.computeIfAbsent(dtoClass, JaxbUtils::contextOf)
                : contextOf(dtoClass);
    }

    @SneakyThrows
    private static <T> JAXBContext contextOf(final Class<?> ... classesToBeBound) {
        try {
            return (JAXBContext) JAXB_CONTEXT_FACTORY_METHOD_HANDLE.invoke(open(classesToBeBound), JAXB_CONTEXT_FACTORY_PROPS);
        } catch (Exception e) {
            var msg = String.format("obtaining JAXBContext for classes (to be bound) {%s}", _NullSafe.stream(classesToBeBound)
                    .map(Class::getName)
                    .collect(Collectors.joining(", ")));
            throw verboseException(msg, classesToBeBound[0], e); // assuming we have at least one argument
        }
    }

    /**
     * Clone of org.glassfish.jaxb.runtime.v2.MUtils.open(Class[]).
     * @param classes used to resolve module for {@linkplain Module#addOpens(String, Module)}
     * @throws JAXBException if any of a classes package is not open to our module.
     */
    private static Class<?>[] open(final Class<?>[] classes) throws JAXBException {
        final Module coreModule = JAXB_CONTEXT_FACTORY.getClass().getModule();
        final Module rtModule = JAXBContextFactory.class.getModule();
        if (rtModule == coreModule || !rtModule.isNamed()) {
            //we're either in a bundle or on the classpath
            return classes;
        }
        for (Class<?> cls : classes) {
            Class<?> jaxbClass = cls.isArray() ? cls.getComponentType() : cls;
            final Module classModule = jaxbClass.getModule();
            //no need for unnamed and java.base types
            if (!classModule.isNamed() || "java.base".equals(classModule.getName())) {
                continue;
            }
            final String packageName = jaxbClass.getPackageName();

            if (classModule.isOpen(packageName, rtModule)) {
                classModule.addOpens(packageName, coreModule);
            } else {
                throw new JAXBException(java.text.MessageFormat.format(
                        "Package {0} with class {1} defined in a module {2} must be open to at least {3} module.",
                        packageName, jaxbClass.getName(), classModule.getName(), rtModule.getName()));
            }
        }
        return classes;
    }

    // -- ENHANCE EXCEPTION MESSAGE IF POSSIBLE

    private static RuntimeException verboseException(final String doingWhat, @Nullable final Class<?> dtoClass, final Throwable cause) {

        var dtoClassName = Optional.ofNullable(dtoClass).map(Class::getName).orElse("unknown");

        if(isIllegalAnnotationsException(cause)) {
            // report a better error if possible
            // this is done reflectively because on JDK 8 this exception type is only provided by Oracle JDK
            try {

                var errors = _Casts.<List<? extends Exception>>uncheckedCast(
                        cause.getClass().getMethod("getErrors").invoke(cause));

                if(_NullSafe.size(errors)>0) {

                    return _Exceptions.unrecoverable(cause,
                            "Error %s, "
                            + "due to illegal annotations on object class '%s'; "
                            + "%d error(s) reported: %s",
                            doingWhat,
                            dtoClassName,
                            errors.size(),
                            errors.stream()
                                .map(Exception::getMessage)
                                .collect(Collectors.joining("; ")));
                }

            } catch (Exception ex) {
                // just fall through if we hit any issues
            }
        }

        return _Exceptions.unrecoverable(cause,
                "Error %s; object class is '%s'", doingWhat, dtoClassName);
    }

    private static boolean isIllegalAnnotationsException(final Throwable cause) {
        /*sonar-ignore-on*/
        return "com.sun.xml.bind.v2.runtime.IllegalAnnotationsException".equals(cause.getClass().getName());
        /*sonar-ignore-off*/
    }

}
