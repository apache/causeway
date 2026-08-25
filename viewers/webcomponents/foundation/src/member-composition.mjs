/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

const memberCompositions = new WeakMap();

export function connectMemberComposition(host) {
  let composition = memberCompositions.get(host);
  if (!composition) {
    const primary = globalThis.document.createElement('div');
    primary.setAttribute('class', 'causeway-member-primary');
    primary.setAttribute('data-causeway-member-primary', '');
    host.insertBefore(primary, host.firstChild ?? null);
    composition = {primary, actions: Object.freeze([]), observer: null};
    memberCompositions.set(host, composition);
  }
  if (!composition.observer && typeof globalThis.MutationObserver === 'function') {
    composition.observer = new MutationObserver(records => {
      if (records.some(record => record.type === 'childList')) {
        refreshMemberComposition(host);
      }
    });
    composition.observer.observe(host, {childList: true});
  }
  refreshMemberComposition(host);
  queueMicrotask(() => {
    if (host.isConnected) {
      refreshMemberComposition(host);
    }
  });
  return composition.primary;
}

export function disconnectMemberComposition(host) {
  const composition = memberCompositions.get(host);
  composition?.observer?.disconnect();
  if (composition) {
    composition.observer = null;
  }
}

export function memberPrimary(host) {
  return memberCompositions.get(host)?.primary ?? connectMemberComposition(host);
}

export function renderMemberPrimary(host, html, {hidden = false} = {}) {
  const primary = memberPrimary(host);
  primary.innerHTML = String(html ?? '');
  primary.hidden = hidden;
  const actions = refreshMemberComposition(host);
  host.hidden = hidden && actions.length === 0;
  return primary;
}

export function refreshMemberComposition(host) {
  const composition = memberCompositions.get(host);
  if (!composition) {
    return Object.freeze([]);
  }
  const actions = [...(host.children ?? host.childNodes ?? [])]
    .filter(child => child !== composition.primary && child?.localName === 'causeway-action');
  const actionSet = new Set(actions);
  for (const previous of composition.actions) {
    if (!actionSet.has(previous)) {
      previous.removeAttribute?.('data-causeway-associated-action');
    }
  }
  for (const action of actions) {
    action.setAttribute?.('data-causeway-associated-action', '');
  }
  composition.actions = Object.freeze(actions);
  if (actions.length > 0) {
    host.setAttribute('data-causeway-associated-member', host.getAttribute?.('member') ?? '');
    host.setAttribute('data-causeway-action-group', '');
  } else {
    host.removeAttribute('data-causeway-associated-member');
    host.removeAttribute('data-causeway-action-group');
  }
  host.hidden = composition.primary.hidden && actions.length === 0;
  return composition.actions;
}

export function associatedActions(host) {
  return refreshMemberComposition(host);
}

export function eventOriginatesFromAssociatedAction(host, target) {
  let candidate = target;
  while (candidate && candidate !== host) {
    if (candidate.parentNode === host) {
      return candidate.localName === 'causeway-action';
    }
    candidate = candidate.parentNode;
  }
  return false;
}
