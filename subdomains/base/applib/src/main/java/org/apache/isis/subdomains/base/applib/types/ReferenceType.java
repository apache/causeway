package org.apache.isis.subdomains.base.applib.types;

public class ReferenceType {

    private ReferenceType() {}

    public static class Meta {

        public static final int MAX_LEN = 24;

        public static final String REGEX = "[ -/_A-Z0-9]+";
        public static final String REGEX_DESCRIPTION = "Only capital letters, numbers and 3 symbols being: \"_\" , \"-\" and \"/\" are allowed";

        private Meta() {}

    }

}
