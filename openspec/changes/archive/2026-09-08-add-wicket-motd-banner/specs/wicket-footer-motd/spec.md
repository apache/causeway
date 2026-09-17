## ADDED Requirements

### Requirement: Application-provided message of the day
The system SHALL expose a regular applib SPI through which an application can provide zero or one message of the day.
A message SHALL contain a plain-text title, trusted HTML detail, a display start `Instant`, and a strictly positive display `Duration`.

#### Scenario: No provider is registered
- **WHEN** no message-of-the-day provider is registered
- **THEN** the Wicket footer renders no MOTD banner

#### Scenario: Provider supplies no message
- **WHEN** the registered provider returns no message
- **THEN** the Wicket footer renders no MOTD banner

#### Scenario: Valid message is supplied
- **WHEN** the registered provider returns a message with a title, HTML detail, display start, and positive display duration
- **THEN** the system makes that single message available for activity evaluation

#### Scenario: Non-positive duration is supplied
- **WHEN** a message is constructed with a zero or negative display duration
- **THEN** the system rejects the invalid message

### Requirement: Scheduled message activity
The system SHALL derive a message's display end by adding its display duration to its display start.
The system SHALL consider the message active from its display start inclusive until its derived display end exclusive.
The Wicket viewer SHALL obtain the current instant through `ClockService`.

#### Scenario: Before the display start
- **WHEN** the current instant is before the message display start
- **THEN** the message is inactive and the Wicket footer does not show its banner

#### Scenario: At the display start
- **WHEN** the current instant equals the message display start
- **THEN** the message is active and the Wicket footer shows its banner

#### Scenario: During the display duration
- **WHEN** the current instant is after the display start and before the derived display end
- **THEN** the message is active and the Wicket footer shows its banner

#### Scenario: At the derived display end
- **WHEN** the current instant equals the derived display end
- **THEN** the message is inactive and the Wicket footer does not show its banner

### Requirement: MOTD title banner
The Wicket footer SHALL display the active message's title as escaped plain text in a compact, keyboard-accessible clickable control.
The title region SHALL constrain overflowing content so that it does not displace the footer's other controls unnecessarily.

#### Scenario: Active message is rendered
- **WHEN** an active message is available during page rendering
- **THEN** the footer shows its escaped title as a clickable MOTD banner

#### Scenario: Title exceeds the available footer width
- **WHEN** the active message title is wider than the available MOTD region
- **THEN** the footer constrains the displayed title while preserving access to the message detail

### Requirement: MOTD detail dialog
Activating the MOTD title SHALL open a modal dialog whose heading is the message title and whose body renders the message detail as trusted, unescaped HTML.
The framework SHALL document that the SPI does not sanitize the detail HTML.

#### Scenario: User opens the active message
- **WHEN** the user activates the MOTD title using a pointer or keyboard
- **THEN** a modal dialog opens with the message title and rendered HTML detail

#### Scenario: Detail contains HTML formatting and links
- **WHEN** the message detail contains trusted HTML formatting or links
- **THEN** the dialog renders that markup rather than displaying escaped HTML source

### Requirement: Render-time refresh
The Wicket footer SHALL consult the provider and evaluate message activity during each page render.
The system SHALL NOT require polling or push updates while a rendered browser page remains idle.

#### Scenario: Candidate message changes between renders
- **WHEN** the provider returns a different candidate message on a subsequent page render
- **THEN** the footer reflects the newly returned message and its current activity

#### Scenario: Message expires between renders
- **WHEN** a previously active message has reached its derived display end before the next page render
- **THEN** the footer hides the MOTD banner on that render

### Requirement: Footer history dropdown defaults to hidden
The existing `causeway.viewer.wicket.bookmarked-pages.show-drop-down-on-footer` property SHALL remain available and SHALL default to `false`.
Setting the property to `true` SHALL display the existing footer history dropdown.

#### Scenario: Property is not configured
- **WHEN** an application does not configure `show-drop-down-on-footer`
- **THEN** the Wicket footer does not show the history dropdown

#### Scenario: Property is explicitly enabled
- **WHEN** an application configures `show-drop-down-on-footer=true`
- **THEN** the Wicket footer shows the existing history dropdown

### Requirement: MOTD and history coexistence
An explicitly enabled footer history dropdown SHALL coexist with an active MOTD rather than replacing or suppressing it.

#### Scenario: Both features are active
- **WHEN** `show-drop-down-on-footer=true` and the provider supplies an active message
- **THEN** the Wicket footer displays both the history dropdown and the MOTD title
