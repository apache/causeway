package org.nakedobjects.example.ecs;

import org.nakedobjects.application.Title;
import org.nakedobjects.application.control.ClassAbout;
import org.nakedobjects.application.control.FieldAbout;
import org.nakedobjects.application.control.Validity;
import org.nakedobjects.application.valueholder.Color;
import org.nakedobjects.application.valueholder.TextString;


public class CreditCard implements PaymentMethod {
    private final TextString nameOnCard;
    private final TextString number;
    private final TextString expires;
    private final Color color;
    
    public static void aboutCreditCard(ClassAbout about) {
        about.instancesUnavailable();
    }
    
     public CreditCard() {
        expires = new TextString();
        nameOnCard = new TextString();
        number = new TextString();
        color = new Color();
        
        number.setMaximumLength(16);
        expires.setMinimumLength(4);
    }

    public final TextString getExpires() {
        return expires;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void aboutExpires(FieldAbout about, TextString value) {
       // validity.cannotBeEmpty();
        if (value == null || value.isEmpty()) {
            about.invalid("Cannot be empty");
        } else {
            String s = value.stringValue().trim();
            boolean valid = true;
            if (s.length() == 5) {
                valid &= Character.isDigit(s.charAt(0));
                valid &= Character.isDigit(s.charAt(1));
                valid &= '/' == s.charAt(2);
                valid &= Character.isDigit(s.charAt(3));
                valid &= Character.isDigit(s.charAt(4));
            } else {
                valid = false;
            }
            about.invalidOnCondition(!valid, "date must be MM/YY e.g. 03/05");
        }
    }
    
    /*
    public void validExpires(Validity validity) {
        validity.cannotBeEmpty();
        String s = expires.stringValue().trim();
            boolean valid = true;
        if(s.length() == 5) {
            valid &= Character.isDigit(s.charAt(0));
            valid &= Character.isDigit(s.charAt(1));
            valid &= '/' == s.charAt(2);
            valid &= Character.isDigit(s.charAt(3));
            valid &= Character.isDigit(s.charAt(4));
        } else {
            valid = false;
        }
        validity.invalidOnCondition(!valid, "date must be MM/YY e.g. 03/05");
     }
 */

    public final TextString getNameOnCard() {
        return nameOnCard;
    }

    public void validNameOnCard(Validity validity) {
        validity.cannotBeEmpty();
     }
    
    public final TextString getNumber() {
        return number;
    }

    public void aboutNumber(FieldAbout about, TextString number) {
        //validity.cannotBeEmpty();
        if(number == null) {
            about.invalid("Cannot be empty");
        } else {
	        int len = number.stringValue().length();
	        about.invalidOnCondition(len != 16, "Card numbers are 16 digits (not " + len + ")");
        }
    }
    
    public Title title() {
    	if(getNumber().isEmpty()) {
    		return new Title();
    	} else {
	    	
	        String num = getNumber().stringValue();
	        int pos = Math.max(0, num.length() - 5);
	
	        return new Title("*****************".substring(0, pos)).concat(
	                       num.substring(pos));
    	}
    }
    
    public String titleString() {
    	if(getNumber().isEmpty()) {
    		return "";
    	} else {
	        String num = getNumber().stringValue();
	        int pos = Math.max(0, num.length() - 5);
	        return "*****************".substring(0, pos) + num.substring(pos);
    	}
    }
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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
