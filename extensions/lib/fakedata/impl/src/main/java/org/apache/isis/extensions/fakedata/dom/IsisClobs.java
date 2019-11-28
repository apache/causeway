package org.apache.isis.extensions.fakedata.dom;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;

import org.apache.isis.applib.value.Clob;

public class IsisClobs extends AbstractRandomValueGenerator{

    public IsisClobs(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    private final static List<String> fileNames = Arrays.asList(
            "a_and_c.xml",
            "all_well.xml",
            "as_you.xml",
            "com_err.xml",
            "coriolan.xml",
            "cymbelin.xml",
            "dream.xml",
            "hamlet.xml",
            "hen_iv_1.xml",
            "hen_iv_2.xml",
            "hen_v.xml",
            "hen_vi_1.xml",
            "hen_vi_2.xml",
            "hen_vi_3.xml",
            "hen_viii.xml",
            "j_caesar.xml",
            "john.xml",
            "lear.xml",
            "lll.xml",
            "m_for_m.xml",
            "m_wives.xml",
            "macbeth.xml",
            "merchant.xml",
            "much_ado.xml",
            "othello.xml",
            "pericles.xml",
            "r_and_j.xml",
            "rich_ii.xml",
            "rich_iii.xml",
            "t_night.xml",
            "taming.xml",
            "tempest.xml",
            "timon.xml",
            "titus.xml",
            "troilus.xml",
            "two_gent.xml",
            "win_tale.xml",
            "config.rtf",
            "RTF-Spec-1.7.rtf",
            "sample.rtf",
            "testrtf.rtf");

    public Clob any() {
        final List<String> fileNames = IsisClobs.fileNames;
        return asClob(fileNames);
    }

    public Clob anyXml() {
        return asClob(fileNamesEndingWith(".xml"));
    }

    public Clob anyRtf() {
        return asClob(fileNamesEndingWith(".rtf"));
    }

    private static List<String> fileNamesEndingWith(final String suffix) {
        return IsisClobs.fileNames.stream()
                .filter(input -> input.endsWith(suffix))
                .collect(Collectors.toList());
    }

    private Clob asClob(final List<String> fileNames) {
        final int randomIdx = fake.ints().upTo(fileNames.size());
        final String randomFileName = fileNames.get(randomIdx);
        return asClob(randomFileName);
    }

    private static Clob asClob(final String fileName) {
        final URL resource = Resources.getResource(IsisBlobs.class, "clobs/" + fileName);
        final CharSource charSource = Resources.asCharSource(resource, Charsets.US_ASCII);
        final String chars;
        try {
            chars = charSource.read();
            return new Clob(fileName, mimeTypeFor(fileName), chars);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String mimeTypeFor(final String fileName) {
        if(fileName.endsWith("xml")) {
            return "text/xml";
        }
        return "application/rtf";
    }

}
