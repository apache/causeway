package demoapp.dom._infra;

import java.util.Map;

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

        if (line.startsWith(String.format("//tag::%s[]", tagsValue))) {
            within = true;
            return null;
        }

        if (line.startsWith(String.format("//end::%s[]", tagsValue))) {
            within = false;
            return null;
        }

        return within ? line : null;
    }
}
