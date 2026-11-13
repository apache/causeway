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

import {defineComponent, h, type Component} from 'vue';
import type {RouteRecordRaw} from 'vue-router';
import HomePage from './components/HomePage.vue';
import ObjectRoutePage from './components/ObjectRoutePage.vue';
import {parseCanonicalObjectPath} from './route-codec';

export const CAUSEWAY_ROUTE_NAMES = Object.freeze({
  home: 'causeway-home',
  object: 'causeway-object',
  invalid: 'causeway-invalid-route',
  notFound: 'causeway-not-found'
});

function statusPage(title: string, message: string, state: string, role?: string): Component {
  return defineComponent({
    name: `Causeway${title.replace(/\W/gu, '')}Page`,
    setup() {
      return () => h('section', {
        class: ['causeway-vue-route-page', 'causeway-vue-status', state === 'invalid-route' && 'causeway-vue-status-danger'],
        'data-route-state': state,
        tabindex: -1,
        role
      }, [h('h1', title), h('p', message)]);
    }
  });
}

export const CausewayInvalidRoutePage = statusPage(
  'Invalid route',
  'The requested application route is invalid.',
  'invalid-route',
  'alert'
);

export const CausewayNotFoundPage = statusPage(
  'Page unavailable',
  'The requested page is unavailable.',
  'not-found',
  'alert'
);

export interface CausewayRouteRecordOptions {
  readonly homeComponent?: Component;
  readonly notFoundComponent?: Component;
}

export function createCausewayRouteRecords(options: CausewayRouteRecordOptions = {}): RouteRecordRaw[] {
  return [
    {path: '/', name: CAUSEWAY_ROUTE_NAMES.home, component: options.homeComponent ?? HomePage},
    {
      path: '/object/:logicalTypeName/:objectId',
      name: CAUSEWAY_ROUTE_NAMES.object,
      component: ObjectRoutePage,
      beforeEnter(to) {
        try {
          parseCanonicalObjectPath(to.path, '/');
          return true;
        } catch {
          return {name: CAUSEWAY_ROUTE_NAMES.invalid, replace: true};
        }
      }
    },
    {
      path: '/invalid-route',
      name: CAUSEWAY_ROUTE_NAMES.invalid,
      component: CausewayInvalidRoutePage
    },
    {
      path: '/:pathMatch(.*)*',
      name: CAUSEWAY_ROUTE_NAMES.notFound,
      component: options.notFoundComponent ?? CausewayNotFoundPage
    }
  ];
}
