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
package org.apache.causeway.applib.util.schema;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.commons.io.DtoMapper;
import org.apache.causeway.commons.io.JaxbUtils;
import org.apache.causeway.commons.io.YamlUtils;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.MapDto;
import org.apache.causeway.schema.cmd.v2.ParamsDto;
import org.apache.causeway.schema.common.v2.OidsDto;
import org.apache.causeway.schema.common.v2.PeriodDto;

import lombok.experimental.UtilityClass;

/**
 * @since 1.x {@index}
 */
@UtilityClass
public final class CommandDtoUtils {

    public void init() {
        dtoMapper.get();
    }

    private _Lazy<DtoMapper<CommandDto>> dtoMapper = _Lazy.threadSafe(
            ()->JaxbUtils.mapperFor(CommandDto.class));

    public DtoMapper<CommandDto> dtoMapper() {
        return dtoMapper.get();
    }

    public OidsDto targetsFor(final CommandDto dto) {
        OidsDto targets = dto.getTargets();
        if(targets == null) {
            targets = new OidsDto();
            dto.setTargets(targets);
        }
        return targets;
    }

    public ParamsDto parametersFor(final ActionDto actionDto) {
        ParamsDto parameters = actionDto.getParameters();
        if(parameters == null) {
            parameters = new ParamsDto();
            actionDto.setParameters(parameters);
        }
        return parameters;
    }

    public PeriodDto timingsFor(final CommandDto commandDto) {
        PeriodDto timings = commandDto.getTimings();
        if(timings == null) {
            timings = new PeriodDto();
            commandDto.setTimings(timings);
        }
        return timings;
    }

    public String getUserData(final CommandDto dto, final String key) {
        if(dto == null || key == null) {
			return null;
		}
        return CommonDtoUtils.getMapValue(dto.getUserData(), key);
    }

    public void setUserData(
            final CommandDto dto, final String key, final String value) {
        if(dto == null || key == null || _Strings.isNullOrEmpty(value)) {
			return;
		}
        final MapDto userData = userDataFor(dto);
        CommonDtoUtils.putMapKeyValue(userData, key, value);
    }

    public void setUserData(
            final CommandDto dto, final String key, final Bookmark bookmark) {
        if(dto == null || key == null || bookmark == null) {
			return;
		}
        setUserData(dto, key, bookmark.toString());
    }

    public void clearUserData(
            final CommandDto dto, final String key) {
        if(dto == null || key == null) {
			return;
		}
        userDataFor(dto).getEntry().removeIf(x -> x.getKey().equals(key));
    }

    private MapDto userDataFor(final CommandDto commandDto) {
        MapDto userData = commandDto.getUserData();
        if(userData == null) {
            userData = new MapDto();
            commandDto.setUserData(userData);
        }
        return userData;
    }

    // -- YAML SUPPORT

    /**
     * Uses (regular) YAML-list format to represent a collection of {@link CommandDto} entries.
     */
    public String toYaml(final Iterable<CommandDto> commandDtos) {
    	var yamlWriteCustomizer = CommandDtoJacksonSupport.yamlWriteCustomizer();
        return YamlUtils.toStringUtf8(
            _NullSafe.stream(commandDtos)
            	.toList(),
            yamlWriteCustomizer);
    }
    
    /**
     * Uses multi-doc YAML format to represent a collection of {@link CommandDto} entries.
     */
    public String toMultiDocYaml(final Iterable<CommandDto> commandDtos) {
    	var yamlWriteCustomizer = CommandDtoJacksonSupport.yamlWriteCustomizer();
    	return YamlUtils.writeMultiDoc(_NullSafe.stream(commandDtos)
			.map(commandDto->YamlUtils.toStringUtf8(commandDto, yamlWriteCustomizer)));
    }

    /**
     * Uses multi-doc YAML format to represent a collection of {@link CommandExportDto} entries.
     *
     * <p>Each document contains an embedded {@code command} and, when available, a {@code result}
     * with {@code type} and {@code id} fields. Null results are omitted and the legacy
     * {@code returnedObject} field is never emitted.
     *
     * @since 4.0 {@index}
     */
    public String toYamlExport(final Iterable<CommandExportDto> commandExports) {
        var yamlWriteCustomizer = CommandDtoJacksonSupport.yamlWriteCustomizer();
        return YamlUtils.writeMultiDoc(_NullSafe.stream(commandExports)
                .map(commandExport -> YamlUtils.toStringUtf8(commandExport, yamlWriteCustomizer)));
    }

    /**
     * Either parses from (regular) YAML-list format or from multi-doc YAML format,
     * any representing a collection of {@link CommandDto} entries.
     */
    public List<CommandDto> fromYaml(final DataSource commandDtosYaml) {
    	var yamlReadCustomizer = CommandDtoJacksonSupport.yamlReadCustomizer();
    	return YamlUtils.tryReadAsList(CommandDto.class, commandDtosYaml, yamlReadCustomizer)
			.getValue()
			.orElseGet(Collections::emptyList);
    }

    /**
     * Decodes the explicit multi-document formats accepted by command replay import.
     *
     * <p>The canonical {@link CommandExportDto} envelope is attempted first so optional
     * result bookmarks remain available to the importer. Legacy multi-document
     * {@link CommandDto} input remains supported, while YAML list roots are deliberately
     * rejected. The more permissive {@link #fromYaml(DataSource)} API is unchanged.
     *
     * @since 4.0 {@index}
     */
    public List<ImportedCommandDto> fromYamlForReplay(final DataSource commandDtosYaml) {
        final String yaml = commandDtosYaml.tryReadAsStringUtf8()
                .ifFailureFail()
                .getValue()
                .orElse("");
        failIfYamlListRoot(yaml);

        Try<List<CommandExportDto>> wrappedAttempt = tryReadMultiDocument(CommandExportDto.class, yaml);
        final var wrapped = wrappedAttempt.getValue().orElseGet(Collections::emptyList);
        if (wrapped.stream().anyMatch(export -> export != null && export.getCommand() != null)) {
            return wrapped.stream()
                    .filter(Objects::nonNull)
                    .filter(export -> export.getCommand() != null)
                    .map(export -> ImportedCommandDto.of(
                            export.getCommand(),
                            export.getResult() != null ? export.getResult().toBookmark() : null))
                    .toList();
        }
        if (wrappedAttempt.isSuccess()) {
            wrappedAttempt = Try.failure(new IllegalArgumentException(
                    "YAML does not contain any CommandExportDto documents with embedded commands"));
        }

        final Try<List<CommandDto>> legacyAttempt = tryReadMultiDocument(CommandDto.class, yaml);
        if (legacyAttempt.isSuccess()) {
            return legacyAttempt.getValue().orElseGet(Collections::emptyList).stream()
                    .filter(Objects::nonNull)
                    .map(command -> ImportedCommandDto.of(command, null))
                    .toList();
        }

        final Throwable legacyFailure = legacyAttempt.getFailure()
                .orElseGet(() -> new IllegalArgumentException("Unable to decode command replay YAML"));
        wrappedAttempt.getFailure().ifPresent(legacyFailure::addSuppressed);
        if (legacyFailure instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new IllegalArgumentException("Unable to decode command replay YAML", legacyFailure);
    }

    private <T> Try<List<T>> tryReadMultiDocument(
            final Class<T> elementType,
            final String yaml) {
        final var yamlReadCustomizer = CommandDtoJacksonSupport.yamlReadCustomizer();
        return YamlUtils.tryReadMultiDoc(DataSource.ofStringUtf8(yaml))
                .mapSuccessAsNullable(documents -> documents
                        .map(document -> YamlUtils.tryRead(
                                elementType,
                                DataSource.ofStringUtf8(document),
                                yamlReadCustomizer)
                                .ifFailureFail()
                                .getValue()
                                .orElse(null))
                        .filter(Objects::nonNull)
                        .toList());
    }

    private void failIfYamlListRoot(final String yaml) {
        final String stripped = yaml.stripLeading();
        if (stripped.startsWith("- ") || stripped.startsWith("-\n") || stripped.startsWith("-\r\n")) {
            throw new IllegalArgumentException(
                    "Command replay import requires multi-document YAML, not a YAML list");
        }
    }

    /**
     * Creates a structurally independent copy using the complete JAXB command schema mapping.
     *
     * @since 4.0 {@index}
     */
    public CommandDto copy(final CommandDto commandDto) {
        if(commandDto == null) {
            return null;
        }
        try {
            final JAXBContext jaxbContext = JaxbUtils.jaxbContextFor(CommandDto.class);
            final Marshaller marshaller = jaxbContext.createMarshaller();
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            final var writer = new StringWriter();
            marshaller.marshal(commandDto, writer);
            try (final var reader = new StringReader(writer.toString())) {
                return (CommandDto) unmarshaller.unmarshal(reader);
            }
        } catch (JAXBException e) {
            throw new IllegalStateException("Failed to deep-copy CommandDto", e);
        }
    }

    /**
     * Command DTO and optional result bookmark after decoding a portable command export.
     *
     * @since 4.0 {@index}
     */
    public static class ImportedCommandDto {

        private CommandDto command;
        private Bookmark result;

        public static ImportedCommandDto of(
                final CommandDto command,
                final Bookmark result) {
            final var importedCommandDto = new ImportedCommandDto();
            importedCommandDto.setCommand(command);
            importedCommandDto.setResult(result);
            return importedCommandDto;
        }

        public CommandDto getCommand() {
            return command;
        }

        public void setCommand(final CommandDto command) {
            this.command = command;
        }

        public Bookmark getResult() {
            return result;
        }

        public void setResult(final Bookmark result) {
            this.result = result;
        }
    }

    /**
     * Portable command DTO envelope with optional {@link BookmarkDto result} metadata.
     *
     * <p>The YAML representation uses {@code command} and {@code result}; the result contains
     * {@code type} and {@code id}. Unknown input fields are ignored, but the legacy
     * {@code returnedObject} name is not an alias for {@code result}.
     *
     * @since 4.0 {@index}
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommandExportDto {

        private CommandDto command;
        private BookmarkDto result;

        public static CommandExportDto of(
                final CommandDto command,
                final Bookmark result) {
            final var commandExportDto = new CommandExportDto();
            commandExportDto.setCommand(command);
            commandExportDto.setResult(BookmarkDto.of(result));
            return commandExportDto;
        }

        public CommandDto getCommand() {
            return command;
        }

        public void setCommand(final CommandDto command) {
            this.command = command;
        }

        public BookmarkDto getResult() {
            return result;
        }

        public void setResult(final BookmarkDto result) {
            this.result = result;
        }
    }

    /**
     * Portable bookmark identity represented by logical type name and identifier.
     *
     * @since 4.0 {@index}
     */
    public static class BookmarkDto {

        private String type;
        private String id;

        public static BookmarkDto of(final Bookmark bookmark) {
            if(bookmark == null) {
                return null;
            }
            final var bookmarkDto = new BookmarkDto();
            bookmarkDto.setType(bookmark.logicalTypeName());
            bookmarkDto.setId(bookmark.identifier());
            return bookmarkDto;
        }

        public String getType() {
            return type;
        }

        public void setType(final String type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        /**
         * Converts the portable identity without resolving the bookmark.
         */
        public Bookmark toBookmark() {
            return Bookmark.forLogicalTypeNameAndIdentifier(type, id);
        }
    }
 
}
