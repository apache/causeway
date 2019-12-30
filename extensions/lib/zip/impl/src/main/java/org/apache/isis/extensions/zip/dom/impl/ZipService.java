package org.apache.isis.extensions.zip.dom.impl;

import lombok.Data;
import lombok.var;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.isis.applib.FatalException;
import org.springframework.stereotype.Service;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;

@Service
public class ZipService {

    @Data
    public static class FileAndName {
        private final String name;
        private final File file;
    }

    /**
     * Rather than use the name of the file (which might be temporary files, for example)
     * we explicitly provide the name to use (in the ZipEntry).
     */
    public byte[] zip(final List<FileAndName> fileAndNameList) {

        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(baos);

            for (final FileAndName fan : fileAndNameList) {
                zos.putNextEntry(new ZipEntry(fan.getName()));
                final ByteSource byteSource = Files.asByteSource(fan.getFile());
                zos.write(byteSource.read());
                zos.closeEntry();
            }
            zos.close();
            return baos.toByteArray();
        } catch (final IOException ex) {
            throw new FatalException("Unable to create zip", ex);
        }
    }

    /**
     * As per {@link #zip(List)}, but using each file's name as the zip entry (rather than providing it).
     */
    public byte[] zipFiles(final List<File> fileList) {
        return zip(fileList.stream()
                           .map(file -> new FileAndName(file.getName(), file))
                           .collect(Collectors.toList())
                );
    }
}
