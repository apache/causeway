package org.nakedobjects.example.ecs;

import org.nakedobjects.object.Title;
import org.nakedobjects.object.control.Validity;
import org.nakedobjects.object.value.TextString;


public class CreditCard extends PaymentMethod {
    private static final long serialVersionUID = 1L;
    private final TextString nameOnCard;
    private final TextString number;
    private final TextString expires;

    public CreditCard() {
        expires = new TextString();
        nameOnCard = new TextString();
        number = new TextString();
    }

    public final TextString getExpires() {
        return expires;
    }
    
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
 

    public final TextString getNameOnCard() {
        return nameOnCard;
    }

    public void validNameOnCard(Validity validity) {
        validity.cannotBeEmpty();
     }
    
    public final TextString getNumber() {
        return number;
    }

    public void validNumber(Validity validity) {
        validity.cannotBeEmpty();
        int len = number.stringValue().length();
        validity.invalidOnCondition(len != 16, "Card numbers are 16 digits (not " + len + ")");
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
}

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
