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
package org.apache.causeway.applib.value;

import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * @since 1.x {@index}
 */
public sealed interface NamedWithMimeType
extends
    Serializable,
    Comparable<NamedWithMimeType>
permits Blob, Clob {

    String name();
    MimeType mimeType();

    @Override
    default int compareTo(final NamedWithMimeType o) {
        int c = _Strings.compareNullsFirst(
                this.name(),
                o!=null
                    ? o.name()
                    : null);
        if(c!=0) return c;

        return _Strings.compareNullsFirst(
                this.mimeType().getBaseType(),
                o!=null
                    ? o.mimeType().getBaseType()
                    : null);
    }

    /**
     * Subset of MimeTypes most commonly used.
     *
     * @since 2.0
     */
    public enum CommonMimeType {

        // see
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Complete_list_of_MIME_types

        AAC("audio/aac"),
        ABW("application/x-abiword"),
        ARC("application/x-freearc"),
        AVI("video/x-msvideo"),
        AZW("application/vnd.amazon.ebook"),
        BIN("application/octet-stream"),
        BMP("image/bmp"),
        BZ("application/x-bzip"),
        BZ2("application/x-bzip2"),
        CSH("application/x-csh"),
        CSS("text/css"),
        CSV("text/csv"),
        EOT("application/vnd.ms-fontobject"),
        EPUB("application/epub+zip"),
        GZ("application/gzip"),
        GIF("image/gif"),
        HTML("text/html", "htm"),
        ICO("image/vnd.microsoft.icon"),
        ICS("text/calendar"),
        JAR("application/java-archive"),
        JPEG("image/jpeg", "jpg"),
        JS("text/javascript"),
        JSON("application/json"),
        JSONLD("application/ld+json"),
        MIDI("audio/midi", "mid"),
        MJS("text/javascript"),
        MP3("audio/mpeg"),
        MPEG("video/mpeg"),
        MPKG("application/vnd.apple.installer+xml"),
        ODP("application/vnd.oasis.opendocument.presentation"),
        ODS("application/vnd.oasis.opendocument.spreadsheet"),
        ODT("application/vnd.oasis.opendocument.text"),
        OGA("audio/ogg"),
        OGV("video/ogg"),
        OGX("application/ogg"),
        OPUS("audio/opus"),
        OTF("font/otf"),
        PNG("image/png"),
        PDF("application/pdf"),
        PHP("application/php"),
        RAR("application/x-rar-compressed"),
        RTF("application/rtf"),
        SH("application/x-sh"),
        SVG("image/svg+xml"),
        SWF("application/x-shockwave-flash"),
        TAR("application/x-tar"),
        TIFF("image/tiff", "tif"),
        TS("video/mp2t"),
        TTF("font/ttf"),
        TXT("text/plain"), /*aliases*/ LOG("text/plain"), PROPERTIES("text/plain"),
        VSD("application/vnd.visio"),
        WAV("audio/wav"),
        WEBA("audio/webm"),
        WEBM("video/webm"),
        WEBP("image/webp"),
        WOFF("font/woff"),
        WOFF2("font/woff2"),
        XHTML("application/xhtml+xml"),
        XML("application/xml"), /*alias*/ XSD("application/xml"),

        XUL("application/vnd.mozilla.xul+xml"),
        YAML("text/vnd.yaml", "yaml", "yml"),
        ZIP("application/zip"),
        _7Z("application/x-7z-compressed", "7z"),

        // see
        // https://stackoverflow.com/questions/4212861/what-is-a-correct-mime-type-for-docx-pptx-etc

        DOC("application/msword"),
        DOT("application/msword"),

        DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        DOTX("application/vnd.openxmlformats-officedocument.wordprocessingml.template"),
        DOCM("application/vnd.ms-word.document.macroEnabled.12"),
        DOTM("application/vnd.ms-word.template.macroEnabled.12"),

        XLS("application/vnd.ms-excel"),
        XLT("application/vnd.ms-excel"),
        XLA("application/vnd.ms-excel"),

        XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        XLTX("application/vnd.openxmlformats-officedocument.spreadsheetml.template"),
        XLSM("application/vnd.ms-excel.sheet.macroEnabled.12"),
        XLTM("application/vnd.ms-excel.template.macroEnabled.12"),
        XLAM("application/vnd.ms-excel.addin.macroEnabled.12"),
        XLSB("application/vnd.ms-excel.sheet.binary.macroEnabled.12"),

        PPT("application/vnd.ms-powerpoint"),
        POT("application/vnd.ms-powerpoint"),
        PPS("application/vnd.ms-powerpoint"),
        PPA("application/vnd.ms-powerpoint"),

        PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation"),
        POTX("application/vnd.openxmlformats-officedocument.presentationml.template"),
        PPSX("application/vnd.openxmlformats-officedocument.presentationml.slideshow"),
        PPAM("application/vnd.ms-powerpoint.addin.macroEnabled.12"),
        PPTM("application/vnd.ms-powerpoint.presentation.macroEnabled.12"),
        POTM("application/vnd.ms-powerpoint.template.macroEnabled.12"),
        PPSM("application/vnd.ms-powerpoint.slideshow.macroEnabled.12"),

        MDB("application/vnd.ms-access"),

        ;

        private CommonMimeType(final String primaryType, final String ... proposedFileExtensions) {
            this.mimeType = newMimeType(primaryType);
            this.proposedFileExtensions =
                    proposedFileExtensions.length>0
                        ? Can.ofArray(proposedFileExtensions)
                        : Can.ofSingleton(name().toLowerCase()); // default
        }

        @Getter @Accessors(fluent=true) final MimeType mimeType;
        @Getter @Accessors(fluent=true) final Can<String> proposedFileExtensions;

        public String baseType() {
            return mimeType().getBaseType();
        }

        static MimeType newMimeType(final String primaryType, final String subtype) {
            try {
                return new MimeType(primaryType, subtype);
            } catch (MimeTypeParseException e) {
                throw new IllegalArgumentException(e);
            }
        }

        static MimeType newMimeType(final String baseType) {
            try {
                return new MimeType(baseType);
            } catch (MimeTypeParseException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public boolean matches(final MimeType otherMimeType) {
            return mimeType.match(otherMimeType);
        }

        /**
         * Tries to match mimeType with any {@link CommonMimeType}.
         */
        public static Optional<CommonMimeType> valueOf(final @Nullable MimeType mimeType) {
            if(mimeType==null) return Optional.empty();
            return Stream.of(CommonMimeType.values())
                    .filter(mime->mime.matches(mimeType))
                    .findFirst();
        }

        /**
         * Tries to match fileExt with any {@link CommonMimeType}.
         */
        public static Optional<CommonMimeType> valueOfFileExtension(final @Nullable String fileExt) {
            if(_Strings.isNullOrEmpty(fileExt)) return Optional.empty();

            var fileExtLower = fileExt.toLowerCase();
            return Stream.of(CommonMimeType.values())
                    .filter(mime->mime.proposedFileExtensions().contains(fileExtLower))
                    .findFirst();
        }

        /**
         * Parses fileName for its extension and tries to match with any {@link CommonMimeType}.
         */
        public static Optional<CommonMimeType> valueOfFileName(final @Nullable String fileName) {
            if(_Strings.isNullOrEmpty(fileName)) return Optional.empty();

            final int p = fileName.lastIndexOf('.');
            if(p<0) return Optional.empty();

            final int beginIndex = p + 1;
            if ((fileName.length() - beginIndex) < 0) return Optional.empty();

            return valueOfFileExtension(fileName.substring(beginIndex));
        }

    }

}
