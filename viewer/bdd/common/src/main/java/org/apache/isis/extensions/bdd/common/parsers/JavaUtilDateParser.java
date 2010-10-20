package org.apache.isis.extensions.bdd.common.parsers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JavaUtilDateParser {
    public java.util.Date parse(final String dateStr) {
        try {
            final SimpleDateFormat parser = new SimpleDateFormat(
                    "d MMM yyyy hh:mm");
            final Date parsed = parser.parse(dateStr);
            return parsed;
        } catch (final ParseException e) {
            return null;
        }
    }

}
