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

import {createApp} from 'vue';
import {createRouter, createWebHistory} from 'vue-router';
import {
  createCausewayRouteRecords,
  createCausewayVueViewer,
  isFrameworkLogoutAction
} from '@apache-causeway/vue-viewer';
import '@apache-causeway/vue-viewer/theme.css';
import App from './App.vue';
import HomePage from './pages/HomePage.vue';
import PetOwnerPage from './pages/PetOwnerPage.vue';
import PetPage from './pages/PetPage.vue';
import VisitPage from './pages/VisitPage.vue';
import './petclinic.css';
import {authenticationContext, bootstrapAuthentication} from './authentication';

const toolkit = new URLSearchParams(location.search).get('toolkit') === 'native' ? 'native' : 'vaadin';
const policy = document.documentElement.dataset;
policy.causewayComponentToolkit = toolkit;
policy.causewayPresentation = toolkit;
policy.causewayActionButtons = toolkit;
policy.causewayReferenceWidgets = toolkit;
policy.causewayCollectionGrid = toolkit;
policy.causewayApplicationMenubar = toolkit;
policy.causewayFieldFamilies = toolkit === 'vaadin' ? 'basic,numeric,local-temporal' : '';
policy.causewayReferenceMinimumSearchLength = '2';
policy.causewayReferenceMaximumResults = '50';

await bootstrapAuthentication('/graphql');

const componentsModuleUrl = '/causeway-webcomponents/index.mjs';
await import(/* @vite-ignore */ componentsModuleUrl);

const router = createRouter({
  history: createWebHistory('/vue/'),
  routes: createCausewayRouteRecords()
});
const viewer = createCausewayVueViewer({
  router,
  endpoint: '/graphql',
  basePath: '/vue',
  applicationResourceBase: '/',
  pages: {
    'petclinic.HomePage': HomePage,
    'petclinic.PetOwner': PetOwnerPage,
    'petclinic.Pet': PetPage,
    'petclinic.Visit': VisitPage
  },
  policies: {
    menuActionLabel(detail) {
      return authenticationContext.value && isFrameworkLogoutAction(detail) ? 'Sign out' : undefined;
    },
    action(detail, claim) {
      if (!authenticationContext.value || !isFrameworkLogoutAction(detail)) return;
      if (!claim.claim()) return true;
      const form = document.querySelector<HTMLFormElement>('[data-causeway-authentication-logout]');
      if (!form) throw new Error('The secured Vue logout form is unavailable.');
      form.requestSubmit();
      return true;
    },
    error(error) {
      if (error instanceof Error && error.message === 'The authenticated session has expired.') return;
      console.error('Causeway Vue viewer policy error', error);
    }
  }
});

const app = createApp(App).use(router).use(viewer.plugin);
await router.isReady();
app.mount('#app');
