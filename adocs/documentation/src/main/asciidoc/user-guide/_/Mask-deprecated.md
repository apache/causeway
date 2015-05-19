Title: @Mask

[//]: # (content copied to _user-guide_xxx)

> Deprecated, never supported.


The `@Mask` annotation may be applied to any property, or to any
parameter within an action method, that allows the user to type in text
as input. It can also annotate a string-based value type, and thereby
apply to all properties or parameters of that type.

The mask serves to validate, and potentially to normalise, the format of
the input. The characters that can be used are based on Swing's
`javax.swing.text.MaskFormatter`, and also Java's
`java.util.SimpleDateFormat`.

For example, on a property:

    public class Email {
        @Mask("(NNN)NNN-NNNN")
        public String getTelephoneNumber() {...}
        public void setTelephoneNumber(String telNo) {...}
        ...
    }

Or, on an action parameter:

    public void ContactRepository {
        public void newContact(
                @Named("Contact Name") String contactName
               ,@Mask("(NNN)NNN-NNNN") 
                @Named("Telephone Number") String telNo) { 
            ...
        }
        ... 
    }

Or, on a value type:

    @Value(...)
    @MaxLength(30)
    public class CustomerFirstName {
        ...
    }

See also `@RegEx` annotation <!--, ?-->.
