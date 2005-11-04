package org.nakedobjects.xat;

import org.nakedobjects.application.control.Validity;

import java.util.Vector;

import test.org.nakedobjects.object.repository.application.value.Money;
import test.org.nakedobjects.object.repository.application.value.TextString;


public class TestObjectExample {

    public final Money amount = new Money();
    private final Vector collection = new Vector();
    private TestObjectExample fourDefault;
    private final String oneModifiable;
    private String result = null;
/*
    public void aboutActionEightInvisible(ActionAbout about, TestObjectExample param1, TestObjectExample param2, TextString param3) {
        about.invisible();
    }

    public void aboutActionFiveInvisible(ActionAbout about, TestObjectExample param) {
        about.invisible();
    }

    public void aboutActionFourUnusable(ActionAbout about) {
        about.unusable();
    }

    public void aboutActionNineInvisible(ActionAbout about, TestObjectExample object) {
        about.invisible();
    }

    public void aboutActionSixUnusable(ActionAbout about, TestObjectExample param) {
        about.unusable();
    }

    public void aboutActionTenUnusable(ActionAbout about, TestObjectExample param, TestObjectExample param2, TextString param3) {
        about.unusable();
    }

    public void aboutActionThreeInvisible(ActionAbout about) {
        about.invisible();
    }

    public void aboutFiveUnmodifiable(FieldAbout about, TestObjectExample e) {
        about.unusable();
        about.invisible();
    }

    public void aboutSevenUnusable(FieldAbout about, TestObjectExample e) {
        about.unusable();
    }

    public void aboutSixInvisible(FieldAbout about, TestObjectExample e) {
        about.invisible();
    }

    public void aboutThreeInvisible(FieldAbout about) {
        about.invisible();
    }

    public void aboutTwoUnmodifiable(FieldAbout about) {
        about.unusable();
    }
*/
    public void actionEightInvisible(TestObjectExample param1, TestObjectExample param2, String param3) {}

    public void actionFiveInvisible(TestObjectExample param) {}

    public void actionFourUnusable() {}

    public void actionNineInvisible(TestObjectExample object) {}

    public void actionOneDefault() {
        result = "one";
    }

    public void actionSeven(TestObjectExample param1, TestObjectExample param2, String value) {
        result = value.stringValue();
    }

    public void actionSixUnusable(TestObjectExample param) {}

    public void actionTenUnusable(TestObjectExample param, TestObjectExample param2, String param3) {}

    public void actionThreeInvisible() {}

    public void actionTwoDefault(TestObjectExample param) {
        result = "two";
    }

    public void addToCollection(TestElement element) {
        collection.addElement(element);
    }

    public void associateFourDefault(TestObjectExample e) {
        result = e.toString();
        fourDefault = e;
    }

    public float getAmount() {
        return amount;
    }

    public Vector getCollection() {
        return collection;
    }

    public TestObjectExample getFiveUnmodifiable() {
        //       throw new NakedAssertionFailedError();
        return null;
    }

    public TestObjectExample getFourDefault() {
        return fourDefault;
    }

    public TextString getOneModifiable() {
        return oneModifiable;
    }

    public TestObjectExample getSevenUnusable() {
        return null;
    }

    public TestObjectExample getSixInvisible() {
        return null;
    }

    public DummyNakedValue getThreeInvisible() {
        return new DummyNakedValue();
    }

    public DummyNakedValue getTwoUnmodifiable() {
        return new DummyNakedValue();
    }

    public void removeFromCollection(TestElement element) {
        collection.removeElement(element);
    }

    public String result() {
        return result;
    }

    public void setFiveUnmodifiable(TestObjectExample e) {
        throw new NakedAssertionFailedError();
    }

    public void setFourDefault(TestObjectExample e) {
        throw new NakedAssertionFailedError();
    }

    public void setSevenUnusable(TestObjectExample e) {
        throw new NakedAssertionFailedError("Invalid associate call");
    }

    public void setSixInvisible(TestObjectExample e) {
        throw new NakedAssertionFailedError();
    }

    public void validAmount(Validity validity) {
        validity.invalidOnCondition(amount.doubleValue() < 0.0, "amount must be positive");
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */