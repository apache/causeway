package org.apache.isis.applib.value;

import java.io.IOException;
import java.io.Writer;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import com.google.common.io.CharStreams;
import com.google.common.io.OutputSupplier;

public final class Clob {

    private final String name;
    private final MimeType mimeType;
    private final CharSequence chars;
    
    public Clob(String name, String primaryType, String subType, char[] chars) {
        this(name, primaryType, subType, new String(chars));
    }

    public Clob(String name, String mimeTypeBase, char[] chars) {
        this(name, mimeTypeBase, new String(chars));
    }

    public Clob(String name, MimeType mimeType, char[] chars) {
        this(name, mimeType, new String(chars));
    }

    public Clob(String name, String primaryType, String subType, CharSequence chars) {
        this(name, newMimeType(primaryType, subType), chars);
    }

    public Clob(String name, String mimeTypeBase, CharSequence chars) {
        this(name, newMimeType(mimeTypeBase), chars);
    }

    public Clob(String name, MimeType mimeType, CharSequence chars) {
        if(name.contains(":")) {
            throw new IllegalArgumentException("Name cannot contain ':'");
        }
        this.name = name;
        this.mimeType = mimeType;
        this.chars = chars;
    }

    private static MimeType newMimeType(String baseType) {
        try {
            return new MimeType(baseType);
        } catch (MimeTypeParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static MimeType newMimeType(String primaryType, String subType) {
        try {
            return new MimeType(primaryType, subType);
        } catch (MimeTypeParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getName() {
        return name;
    }
    
    public MimeType getMimeType() {
        return mimeType;
    }

    public CharSequence getChars() {
        return chars;
    }
    
    public void writeCharsTo(final Writer wr) throws IOException {
        CharStreams.write(chars, new OutputSupplier<Writer>() {
            @Override
            public Writer getOutput() throws IOException {
                return wr;
            }
        });
    }
}
