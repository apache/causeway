package org.apache.isis.core.commons.internal.strings;


import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class FormatTest {

    // copy of code in scripts/generateConfigDocs.groovy
    public static String format(final String str, int len) {
        if(str.length() <= len) {
            return str;
        }

        final StringBuilder buf = new StringBuilder();
        String remaining = str;

        while(remaining.length() > 0) {
            int lastDot = remaining.substring(0, len).lastIndexOf('.');
            int lastDash = remaining.substring(0, len).lastIndexOf('-');
            int splitAt = lastDot > 0
                                ? lastDot + 1
                                : lastDash > 0
                                    ? lastDash + 1
                                    : len;
            if(buf.length() > 0) {
                buf.append("\n");
            }
            buf.append(remaining, 0, splitAt);
            remaining = remaining.substring(splitAt);

            if(remaining.length() <= len) {
                buf.append("\n").append(remaining);
                remaining = "";
            }
        }
        return buf.toString();
    }

    @Test
    public void split_once() {
        Assertions.assertEquals("abc.def.ghi.jkl.mno.\npqr.stu.vwx.yza", format("abc.def.ghi.jkl.mno.pqr.stu.vwx.yza", 20));
    }

    @Test
    public void split_twice() {
        Assertions.assertEquals("abc.def.ghi.jkl.\nmno.pqr.stu.vwx.\nyza", format("abc.def.ghi.jkl.mno.pqr.stu.vwx.yza", 16));
    }

    @Test
    public void split_not_on_divider() {
        Assertions.assertEquals("abc.def.ghi.\njkl.mno.pqr.\nstu.vwx.yza", format("abc.def.ghi.jkl.mno.pqr.stu.vwx.yza", 15));
    }

    @Test
    public void no_divider() {
        Assertions.assertEquals("abcdefghij\nklmnopqrst\nuvwxyza", format("abcdefghijklmnopqrstuvwxyza", 10));
    }

    @Test
    public void real_world() {
        Assertions.assertEquals("isis.persistor.\ndatanucleus.\nclass-metadata-\nloaded-listener", format("isis.persistor.datanucleus.class-metadata-loaded-listener", 20));
    }


}
