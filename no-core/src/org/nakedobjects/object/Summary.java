package org.nakedobjects.object;

public class Summary {
    private StringBuffer string = new StringBuffer();
   
    /**
     * Creates a new Title objects with no text in it.
     */
    public Summary() {
        super();
    }

    /**
       Create a new Summary object containing the specified text.
       @param text for the title to contain.
     */
    public Summary(String text) {
        concat(text);
    }

    /**
       Create a new Summary object containing the title of the specified object.
       @param object  the object who's title text to copy
     */
    public Summary(Naked object) {
        concat(object);
    }

    /**
       Create a new Summary object containing the title of the specified object or the default text if the object reference is null
       @param object  the object who's title is to be used.
       @param defaultValue text for the title to be set to if the former parameter is null.
     */
    public Summary(Naked object, String defaultValue) {
        concat(object, defaultValue);
    }


    public Summary append(int number) {
        append(String.valueOf(number));

        return this;
    }

    /**
       Appends a space (if there is already some text in this title object) and then the specified text.
       @return a reference to the called object (itself).
     */
    public Summary append(String text) {
        if (!text.equals("")) {
            appendSpace();
            string.append(text);
        }

        return this;
    }

    /**
       Appends the joiner text, a space, and the text to the text of this Summary object.  If no text yet exists in the object then
       the joiner text and space are omiited.
       @return a reference to the called object (itself).
     */
    public Summary append(String joiner, String text) {
        if (!text.equals("")) {
            if (string.length() > 0) {
                concat(joiner);
            }

            appendSpace();
            string.append(text);
        }

        return this;
    }

    /**
       Append the <code>joiner</code> text, a space, and the title of the specified naked object
       (<code>object</code>) (got by calling the objects title() method) to
       the text of this Summary object.  If the title of the specified object is null then
       use the <code>defaultValue</code> text.
       @param joiner  text to append before the title
       @param object  object whose title needs to be appended
       @return a reference to the called object (itself).
     */
    public Summary append(String joiner, Naked object) {
        append(joiner, object, "");

        return this;
    }

    /**
       Append the <code>joiner</code> text, a space, and the title of the specified naked object
       (<code>object</code>) (got by calling the objects title() method) to
       the text of this Summary object.  If the title of the specified object is null then use the <code>defaultValue</code> text.
       If both the objects title and the default value are null or equate to a zero-length string then no text will be appended
       ; not even the joiner text.
       @param joiner  text to append before the title
       @param object  object whose title needs to be appended
       @param defaultValue  the text to use if the the object's title is null.
       @return a reference to the called object (itself).
     */
    public Summary append(String joiner, Naked object, String defaultValue) {
        if (string.length() > 0 && (object != null && object.title().toString().length() > 0) ||
                (defaultValue != null && defaultValue.length() > 0)) {
            concat(joiner);
            appendSpace();
        }
		concat(object, defaultValue);

        return this;
    }

    /**
       Appends the title text of the specified object.  If <code>object</code> or <code>object.title()</code> is
       <code>null</code> or the <code>object.title().toString()</code> returns an empty string then nothing will
       be appended.
       @param object  the object whose we want to append to this title object.
       @return a reference to the called object (itself).
     */
    public Summary append(Naked object) {
        if (object != null && object.title() != null && !object.title().toString().equals("")) {
            appendSpace();
            string.append(object.title());
        }

        return this;
    }

    /**
       Appends the title of the specified object, or the specified text if the objects title is null, and prepends a space
       if there is already some text in this title object.
       @param  object  the object whose title is to be appended to this title.
       @param  defaultValue  a textual value to be used if the object's title is null.
       @return a reference to the called object (itself).
     */
    public Summary append(Naked object, String defaultValue) {
        appendSpace();
        concat(object, defaultValue);

        return this;
    }

    /**
       Append a space to the text of this Summary object if, and only if, there is some existing text i.e., a
       space is only added to existing text and will not create a text entry consisting of only one space.
       @return a reference to the called object (itself).
     */
    public Summary appendSpace() {
        if (string.length() > 0) {
            string.append(" ");
        }

        return this;
    }

    /**
       Concatenate the specified text on to the end of the text of this Summary.
       @param text text to append
       @return a reference to the called object (itself).
     */
    public final Summary concat(String text) {
        string.append(text);

        return this;
    }

	public final Summary concat(String joiner, Naked object) {
		if (string.length() > 0 && (object != null && object.title().toString().length() > 0)) {
			concat(joiner);
		}
		concat(object, "");

		return this;
	}


    /**
       Concatenate the the title value (the result of calling an objects title() method) to this Summary object.
       If the value is null the no text is added.
       @param object  the naked object to get a title from
       @return a reference to the called object (itself).
     */
    public final Summary concat(Naked object) {
        concat(object, "");

        return this;
    }

	public final Summary concat(String joiner, Naked object, String defaultValue) {
		if (string.length() > 0 && (object != null && object.title().toString().length() > 0)) {
			concat(joiner);
		}
		concat(object, defaultValue);

		return this;
	}

    /**
       Concatenate the the title value (the result of calling an objects title() method), or the specified default value if the
       title is equal to null or is empty, to this Summary object.
       @param object  the naked object to get a title from
       @param defaultValue  the default text to use when the naked object is null
       @return a reference to the called object (itself).
     */
    public final Summary concat(Naked object, String defaultValue) {
        if (object == null || object.isEmpty()) {
            string.append(defaultValue);
        } else {
            string.append(object.title());
        }

        return this;
    }

    /**
       Returns a String that represents the value of this object.
       @return a string representation of the receiver
     */
    public String toString() {
        return string.toString();
    }

    /**
     * Truncates this title so it has a maximum number of words.  Spaces are used to 
     * determine words, thus two spaces in a title will cause two words to be mistakenly
     * identified.
     * @param noWords  the number of words to show
     * @return a reference to the called object (itself).
     */
    public Summary truncate(int noWords) {
    	if(noWords < 1){
    		throw new IllegalArgumentException("Truncation must be to one or more words");
    	}
        int pos = 0;
        int spaces = 0;

        while (pos < string.length() && spaces < noWords) {
            if (string.charAt(pos) == ' ') {
                spaces++;
            }
            pos++;
        }

		if(pos < string.length()) {
            string.setLength(pos - 1); //string.delete(pos - 1, string.length());
        	string.append("...");
		}

        return this;
    }

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/