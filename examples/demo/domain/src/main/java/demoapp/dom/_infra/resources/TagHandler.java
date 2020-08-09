package demoapp.dom._infra.resources;

import java.util.Map;
import java.util.regex.Pattern;

class TagHandler {

    private final Object tagsValue;
    private boolean within = false;

    TagHandler(final Map<String, Object> attributes) {
        tagsValue = attributes.get("tags");
    }

    public String handle(final String line) {
        if(tagsValue == null) {
            return line;
        }

        if (matches(line, "tag")) {
            within = true;
            return null;
        }

        if (matches(line, "end")) {
            within = false;
            return null;
        }

        return within ? line : null;
    }

    private boolean matches(String line, String macro) {
        final Pattern pattern = Pattern.compile("//\\s*" +
                macro +
                "::" +
                tagsValue +
                "\\[]\\s*");
        return pattern.matcher(line).matches();
    }

}
