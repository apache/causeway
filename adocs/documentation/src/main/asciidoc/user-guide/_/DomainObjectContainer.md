title: DomainObjectContainer interface

[//]: # (content copied to _user-guide_xxx)

> Provides a single point of contact from domain objects into the
> *Apache Isis* framework.

The `DomainObjectContainer` interface provides a single point of contact
from domain objects into the *Isis* framework. It can also be used as a
lightweight general purpose repository during prototyping.

The following table lists the most important (ie non deprecated) methods in this interface.

<table class="table table-striped table-bordered table-condensed">
<tr>
    <th>Category</th>
    <th>Method</th>
    <th>Description</th>
</tr>
<tr>
    <td>Object creation</td>
    <td>newTransientInstance(Class&lt;T>)</td>
    <td>Creates new non-persisted object.  
        <p>While it is also possible to simply <i>new()</i> up an object, that object will not have any services injected into it, nor will any properties be <a href="../how-tos/how-to-03-017-How-to-specify-default-value-of-an-object-property.html">defaulted</a> nor will any <a href="./object-lifecycle-callbacks.html"><i>created()</i></a> callback be invoked.</td>
</tr>
<tr>
    <td>Validation</td>
    <td>isValid(Object)</td>
    <td>whether object is valid</td>
</tr>
<tr>
    <td></td>
    <td>validate(Object)</td>
    <td>reason why object is invalid (if any)</td>
</tr>
<tr>
    <td>Generic Repository</td>
    <td>allInstances(Class&lt;T>)</td>
    <td>All persisted instances of specified type</td>
</tr>
<tr>
    <td></td>
    <td>allMatches(Class&lt;T>, Predicate&lt;T>)</td>
    <td>All persistenced instances of specified type matching predicate</td>
</tr>
<tr>
    <td></td>
    <td>allMatches(Class&lt;T>, String)</td>
    <td>All persisted instances with the specified string as their title</td>
</tr>
<tr>
    <td></td>
    <td>allMatches(Class&lt;T>, Object)</td>
    <td>All persisted instances matching object (query-by-example)</td>
</tr>
<tr>
    <td></td>
    <td>allMatches(Query&lt;T>)</td>
    <td>All instances satisfying the provided query</td>
</tr>
<tr>
    <td></td>
    <td>firstMatch(...)</td>
    <td>As for allMatches(...), but returning first instance</td>
</tr>
<tr>
    <td></td>
    <td>uniqueMatch(...)</td>
    <td>As for firstMatch(...), but requiring there to be only one match</td>
</tr>
<tr>
    <td>Object persistence</td>
    <td>isPersistent(Object)</td>
    <td>whether object is persistent</td>
</tr>
<tr>
    <td></td>
    <td>persist(Object)</td>
    <td>persist the transient object</td>
</tr>
<tr>
    <td></td>
    <td>persistIfNotAlready(Object)</td>
    <td>persist the object (provided is not already persisted)</td>
</tr>
<tr>
    <td></td>
    <td>remove(Object)</td>
    <td>remove the persisted object</td>
</tr>
<tr>
    <td></td>
    <td>removeIfNotAlready(Object)</td>
    <td>remove the object (provided is not already transient)</td>
</tr>
<tr>
    <td>Presentation</td>
    <td>titleOf(Object)</td>
    <td>Returns the title of the object.</td>
</tr>
<tr>
    <td>Messages and warnings</td>
    <td>informUser(String)</td>
    <td>Inform the user</td>
</tr>
<tr>
    <td></td>
    <td>warnUser(String)</td>
    <td>Warn the user about a situation, requiring acknowledgement.</td>
</tr>
<tr>
    <td></td>
    <td>raiseError(String)</td>
    <td>Notify user of a serious application error, typically requiring further action on behalf of the user</td>
</tr>
<tr>
    <td>Security</td>
    <td>getUser()</td>
    <td>The currently-logged on user</td>
</tr>
<tr>
    <td>Properties</td>
    <td>getProperty(String)</td>
    <td>Value of configuration property</td>
</tr>
<tr>
    <td></td>
    <td>getPropertyNames()</td>
    <td>All configuration properties available</td>
</tr>
<tr>
    <td>Object store control</td>
    <td>flush()</td>
    <td>Flush all pending changes to object store</td>
</tr>
</table>


