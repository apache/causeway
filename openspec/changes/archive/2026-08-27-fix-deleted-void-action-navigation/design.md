## Context

The interaction controller publishes a successful semantic result for a void action and refreshes the owning object context when the action is mutating.
The HTMX host also reloads the current route so ordinary void mutations display refreshed object state while preserving the shell result.
If the action deleted the target, that reload creates a new context for an identity that no longer resolves, leaving the user on an error-filled object page.

## Goals / Non-Goals

**Goals:**

- Preserve same-object refresh for ordinary successful void actions.
- Route to the configured viewer home path when the post-void refresh establishes that the current object was deleted.
- Keep the successful action result visible long enough to confirm completion.
- Cover the behavior through the existing destructive Petclinic browser journey.

**Non-Goals:**

- Change semantic void-result payloads or require domain actions to declare that they delete their target.
- Redirect arbitrary direct navigation to missing objects.
- Change navigation for object, collection, or scalar action results.

## Decisions

The HTMX host will remember that a void-result route refresh is pending and associate that state with the refreshed route generation.
When the refreshed route's object context reaches a terminal `NOT_FOUND` state, the host will navigate to the normalized viewer base path.
The marker will be cleared when the refreshed object becomes ready, partially ready, or fails for a reason other than not-found, so unrelated later failures cannot cause a home redirect.
This uses runtime evidence from the refreshed object rather than action names such as `delete`, which keeps the policy generic and avoids coupling the viewer to domain conventions.

The Petclinic Playwright journey will assert navigation to `/htmx` after `PetOwner#delete`, while retaining its repository assertion that the object was actually removed.
A focused browser-side test is not introduced because the shell module currently depends on live DOM and HTMX wiring; the existing end-to-end journey directly covers the reported regression.

## Risks / Trade-offs

- [Risk] The original object context may publish `NOT_FOUND` while the route refresh is in flight. → Bind recovery to the navigation generation and only act on a context contained by the current route region.
- [Risk] A transient or authorization failure could be mistaken for deletion. → Redirect only for the explicit `NOT_FOUND` classification and retain existing bounded handling for all other failures.
- [Risk] The successful result message is cleared by the home navigation. → Preserve it during the recovery navigation so users retain confirmation that the action completed.
