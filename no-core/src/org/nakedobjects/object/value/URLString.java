/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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

package org.nakedobjects.object.value;


import java.net.MalformedURLException;
import java.net.URL;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.Title;
import org.nakedobjects.object.ValueParseException;


/**
 * value object to represent an URL.
 * <p>
 * NOTE: this class currently does not support about listeners
 * </p>
 */
public class URLString extends AbstractNakedValue {
	private static final long serialVersionUID = 1L;
	private String urlString;
	
    public URLString() {
        this("");
    }

    public URLString(String urlString) {
        this.urlString = urlString;
    }

    public URLString(URLString urlString) {
        this.urlString = new String(urlString.toString());
    }

    public void clear() {
        urlString = null;
    }

    /**
     Copies the specified object's contained data to this instance.
     param object the object to copy the data from
     */
    public void copyObject(Naked object) {
        if (!(object instanceof URLString)) {
            throw new IllegalArgumentException("Can only copy the value of  a URLString object");
        }
        urlString = ((URLString) object).urlString;
    }

    public String getObjectHelpText() {
        return "A URLString object.";
    }

    public boolean isEmpty() {
        return urlString == null;
    }

	/**
	 * Compares the url string to see if the contain the same text if the specified object is a <code>URLString</code> object else returns false.
	 * @see org.nakedobjects.object.Naked#isSameAs(Naked)
	 */
	public boolean isSameAs(Naked object) {
		if (object instanceof URLString) {
			return ((URLString)object).urlString.equals(urlString);
		} else {
			return false;
		}

	}

    public void parse(String urlString) throws ValueParseException {
        try {
            new URL(urlString);
            this.urlString = urlString;
        } catch (MalformedURLException e) {
            throw new ValueParseException(e, "Invalid URL");
        }
    }

	/**
	 * Reset this url string so it contains an empty string, i.e. "".
	 * @see org.nakedobjects.object.NakedValue#reset()
	 */
	public void reset() {
        urlString ="";
	}
	

    /**
     @deprecated   use parse instead as this will check for a well formed url
     @see #parse(String)
     */
    public void set(String urlString) {
        this.urlString = urlString;
    }

	/**
	 * @deprecated replaced by setValue
	 */
    public void setText(String urlString) {
        this.urlString = urlString;
    }

    public void setValue(String urlString) {
        this.urlString = urlString;
    }

    public void setValue(URLString urlString) {
        this.urlString = urlString.urlString;
    }


    public String stringValue() {
        return urlString;
    }

    public Title title() {
        return new Title(urlString);
    }

    public void update(NakedValue object) {
        urlString = ((URLString) object).urlString;
    }
   
	public void restoreString(String data) {
		urlString = data;
	}

	public String saveString() {
		return urlString;
	}

}
