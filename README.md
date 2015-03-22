Apache Isis
===========

*[Apache Isis](http://isis.apache.org)™ software is a framework for rapidly developing domain-driven apps in Java.  Write your business logic in entities, domain services and repositories, and the framework dynamically generates a representation of that domain model as a webapp or a RESTful API.*

Get started using the [Maven archetype](http://isis.apache.org/intro/getting-started/simple-archetype.html).

For help and support, join the [mailing lists](http://isis.apache.org/support.html).  

## Core Features

Isis automatically generates the UI from the domain classes.  The following screenshots are taken from the Isis Addons' [todoapp example](http://github.com/isisaddons/isis-app-todoapp), which you are free to fork and use as you will.   The corresponding domain classes from which this UI was built can be found [here](https://github.com/isisaddons/isis-app-todoapp/tree/0669d6e2acc5bcad1d9978a4514a17bcf7beab1f/dom/src/main/java/todoapp/dom/module/todoitem). 

The todoapp also integrates with a number of other [Isis Addons](http://www.isisaddons.org) modules.  (Please note that the Isis Addons are not part of ASF, but they are all licensed under Apache License 2.0 and are maintained by the Isis committers).

### Sign-in

Apache Isis integrates with [Apache Shiro](http://shiro.apacheorg)™.  The core framework supports file-based realms, while the Isis Addons [security module](http://github.com/isisaddons/isis-module-security) provides a well-features subdomain of users, roles and permissions against features derived from the Isis metamodel.  The example todoapp integrates with the security module.

![](https://raw.github.com/apache/isis/master/images/010-login.png)

### Install Fixtures

Apache Isis has lots of features to help you prototype and then fully test your application.  One such are fixture scripts, which allow pre-canned data to be installed in the running application.  This is great to act as the starting point for identifying new stories; later on when the feature is being implemented, the same fixture script can be re-used within that feature's integration tests.  (More on tests later).

![](https://raw.github.com/apache/isis/master/images/020-install-fixtures.png)

### Dashboard and View Models

Most of the time the end-user interacts with representations of persistent domain entities, but Isis also supports view models which can aggregate data from multiple sources.  The todoapp example uses a "dashboard" view model to list todo items not yet done vs those completed.

![](https://raw.github.com/apache/isis/master/images/030-dashboard-view-model.png)

In general we recommend to initially focus only on domain entities; this will help drive out a good domain model.  Later on view models can be introduced in support of specific use cases.

### Domain Entity

The screenshot below is of the todoapp's `ToDoItem` domain entity.  Like all web pages, this UI is generated at runtime, directly from the domain object itself.  There are no controllers or HTML to write.

![](https://raw.github.com/apache/isis/master/images/040-domain-entity.png)

In addition to the domain entity, Apache Isis allows layout metadata hints to be provided, for example to specify the grouping of properties, the positioning of those groups into columns, the association of actions (the buttons) with properties or collections, the icons on the buttons, and so on.  This metadata can be specified either as annotations or in JSON form; the benefit of the latter is that it can be updated (and the UI redrawn) without restarting the app.

Any production-ready app will require this metadata but (like the view models discussed above) this metadata can be added gradually on top of the core domain model.

### Edit properties

By default properties on domain entities are editable, meaning they can be changed directly.  In the todoapp example, the `ToDoItem`'s description is one such editable property:

![](https://raw.github.com/apache/isis/master/images/050-edit-property.png)

Note that some of the properties are read-only even in edit mode; individual properties can be made non-editable.  It is also possible to make all properties disabled and thus enforce changes only through actions (below).

### Actions

The other way to modify an entity is to an invoke an action.  In the screenshot below the `ToDoItem`'s category and subcategory can be updated together using an action:

![](https://raw.github.com/apache/isis/master/images/060-invoke-action.png)

There are no limitations on what an action can do; it might just update a single object, it could update multiple objects.  Or, it might not update any objects at all, but could instead perform some other activity, such as sending out email or printing a document.

In general though, all actions are associated with some object, and are (at least initially) also implemented by that object: good old-fashioned encapsulation.  We sometimes use the term "behaviourally complete" for such domain objects.

### Contributions

As an alternative to placing actions (business logic) on a domain object, it can instead be placed on an (application-scoped, stateless) domain service.  When an object is rendered by Apache Isis, it will automatically render all "contributed" behaviour; rather like traits or aspect-oriented mix-ins).

In the screenshot below the highlighted "export as xml" action, the "relative priority" property (and "previous" and "next" actions) and also the "similar to" collection are all contributed:

![](https://raw.github.com/apache/isis/master/images/065-contributions.png)

Contributions are defined by the signature of the actions on the contributing service.  The code snippet below shows how this works for the "export as xml" action:

![](https://raw.github.com/apache/isis/master/images/067-contributed-action.png)

## Extensible Views

The Apache Isis viewer is implemented using [Apache Wicket](http://wicket.apache.org)™, and has been architected to be extensible.  For example, when a collection of objects is rendered, this is just one several views, as shown in the selector drop-down:

![](https://raw.github.com/apache/isis/master/images/070-pluggable-views.png)

The Isis Addons' [gmap3 component](https://github.com/isisaddons/isis-wicket-gmap3) will render any domain entity (such as `ToDoItem`) that implements its `Locatable` interface:

![](https://raw.github.com/apache/isis/master/images/080-gmap3-view.png)

Simiarly the Isis Addons' [fullcalendar2 component](https://github.com/isisaddons/isis-wicket-fullcalendar2) will render any domain entity (such as `ToDoItem`) that implements its `Calendarable` interface:

![](https://raw.github.com/apache/isis/master/images/090-fullcalendar2-view.png)

Yet another "view" (though this one is rather simpler is that provided by the Isis Addons [excel component](https://github.com/isisaddons/isis-wicket-excel).  This provides a download button to the table as a spreadsheet:

![](https://raw.github.com/apache/isis/master/images/100-excel-view-and-docx.png)

The screenshot above also shows an "export to Word" action.  This is *not* a view but instead is a (contributed) action that uses the Isis Addons [docx module](https://github.com/isisaddons/isis-module-docx) to perform a "mail-merge":

![](https://raw.github.com/apache/isis/master/images/110-docx.png)

## Security, Auditing and other Services

As well as providing extensions to the UI, the Isis addons provides a rich set of modules to support various cross-cutting concerns.

Under the activity menu are four sets of services which provide support for [user session logging/auditing](https://github.com/isisaddons/isis-module-sessionlogger), [command profiling](https://github.com/isisaddons/isis-module-command), [(object change) auditing](https://github.com/isisaddons/isis-module-audit) (shown) and (inter-system) [event publishing](https://github.com/isisaddons/isis-module-publishing):

![](https://raw.github.com/apache/isis/master/images/120-auditing.png)

In the security menu is access to the rich set of functionality provided by the Isis addons [security module](https://github.com/isisaddons/isis-module-security):

![](https://raw.github.com/apache/isis/master/images/130-security.png)

In the prototyping menu is the ability to download a GNU gettext `.po` file for translation.  This file can then be translated into multiple languages so that your app can support different locales.  Note that this feature is part of Apache Isis core (it is not in Isis Addons):

![](https://raw.github.com/apache/isis/master/images/140-i18n.png)

The Isis addons also provides a module for managing application and user [settings](https://github.com/isisaddons/isis-module-settings).  Most apps (the todoapp example included) won't expose these services directly, but will usually wrap them in their own app-specific settings service that trivially delegates to the settings module's services:

![](https://raw.github.com/apache/isis/master/images/150-appsettings.png)

### Multi-tenancy support

Of the various Isis addons, the [security module](https://github.com/isisaddons/isis-module-security) has the most features.  One significant feature is the ability to associate users and objects with a "tenancy".  The todoapp uses this feature so that different users' list of todo items are kept separate from one another.  A user with administrator is able to switch their own "tenancy" to the tenancy of some other user, in order to access the objects in that tenancy:

![](https://raw.github.com/apache/isis/master/images/160-switch-tenancy.png)

For more details, see the [security module](https://github.com/isisaddons/isis-module-security) README.

### Me

Most of the [security module](https://github.com/isisaddons/isis-module-security)'s services are on the security module, which would normally be provided only to administrators.  Kept separate is the "me" action:

![](https://raw.github.com/apache/isis/master/images/170-me.png)

Assuming they have been granted permissions, this allows a user to access an entity representing their own user account:

![](https://raw.github.com/apache/isis/master/images/180-app-user-entity.png)

If not all of these properties are required, then they can be hidden either using security or though Isis' internal event bus (described below).  Conversely, additional properties can be "grafted onto" the user using the contributed properties/collections discussed previously.

### Themes

Apache Isis' Wicket viewer uses [Twitter Bootstrap](http://getbootstrap.com), which means that it can be themed.  If more than one theme has been configured for the app, then the viewer allows the end-user to switch their theme:

![](https://raw.github.com/apache/isis/master/images/190-switch-theme.png)

## REST API

In addition to Isis' Wicket viewer, it also provides a fully fledged REST API, as an implementation of the [Restful Objects](http://restfulobjects.org) specification.  The screenshot below shows accessing this REST API using a Chrome plugin:

![](https://raw.github.com/apache/isis/master/images/200-rest-api.png)

Like the Wicket viewer, the REST API is generated automatically from the domain objects (entities and view models).

## Integration Testing Support

Earlier on we noted that Apache Isis allows fixtures to be installed through the UI.  These same fixture scripts can be reused within integration tests.  For example, the code snippet below shows how the  `FixtureScripts` service injected into an integration test can then be used to set up data:

![](https://raw.github.com/apache/isis/master/images/210-fixture-scripts.png)

The tests themselves are run in junit.  While these are integration tests (so talking to a real database), they are no more complex than a regular unit test:

![](https://raw.github.com/apache/isis/master/images/220-testing-happy-case.png)

To simulate the business rules enforced by Apache Isis, the domain object can be "wrapped" in a proxy.  For example, if using the Wicket viewer then Apache Isis will enforce the rule (implemented in the `ToDoItem` class itself) that a completed item cannot have the "completed" action invoked upon it.  The wrapper simulates this by throwing an appropriate exception:

![](https://raw.github.com/apache/isis/master/images/230-testing-wrapper-factory.png)

## Internal Event Bus

Contributions, discussed earlier, are an important tool in ensuring that the packages within your Isis application are decoupled; by extracting out actions the order of dependency between packages can effectively be reversed.

Another important tool to ensure your codebase remains maintainable is Isis' internal event bus.  It is probably best explained by example; the code below says that the "complete" action should emit a `ToDoItem.Completed` event:

![](https://raw.github.com/apache/isis/master/images/240-domain-events.png)

Domain service (application-scoped, stateless) can then subscribe to this event:

![](https://raw.github.com/apache/isis/master/images/250-domain-event-subscriber.png)

And this test verifies that completing an action causes the subscriber to be called:

![](https://raw.github.com/apache/isis/master/images/260-domain-event-test.png)

In fact, the domain event is fired not once, but (up to) 5 times.  It is called 3 times prior to execution, to check that the action is visible, enabled and that arguments are valid.  It is then additionally called prior to execution, and also called after execution.  What this means is that a subscriber can in either veto access to an action of some publishing object, and/or it can perform cascading updates if the action is allowed to proceed.

Moreover, domain events are fired for all properties and collections, not just actions.  Thus, subscribers can therefore switch on or switch off different parts of an application.  Indeed, the example todoapp demonstrates this.

## Learning More

The Apache Isis [website](http://isis.apache.org) has lots of useful information and is being continually updated.

Or, you can just start coding using the [Maven archetype](http://isis.apache.org/intro/getting-started/simple-archetype.html).

And if you need help or support, join the [mailing lists](http://isis.apache.org/support.html).  
