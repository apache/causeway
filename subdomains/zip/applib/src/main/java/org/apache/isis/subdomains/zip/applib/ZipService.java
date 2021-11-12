package org.apache.isis.subdomains.zip.applib;

import java.io.File;
import java.util.List;

import lombok.Data;

public interface ZipService {

    /**
     * Rather than use the name of the file (which might be temporary files, for example)
     * we explicitly provide the name to use (in the ZipEntry).
     */
    byte[] zipNamedFiles(List<FileAndName> fileAndNameList);

    /**
     * As per {@link #zipNamedFiles(List)},
     * but using each file's name as the zip entry (rather than providing it).
     */
    byte[] zipFiles(List<File> fileList);

    /**
     * Similar to {@link #zipNamedFiles(List)}, but uses simple byte[] as the input, rather than files.
     *
     * @param bytesAndNameList
     */
    byte[] zipNamedBytes(List<BytesAndName> bytesAndNameList);

    @Data
    public static class FileAndName {
        private final String name;
        private final File file;
    }

    @Data
    public static class BytesAndName {
        private final String name;
        private final byte[] bytes;
    }

}