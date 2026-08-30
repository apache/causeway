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

export const MENU_BARS_XML = `<?xml version="1.0" encoding="UTF-8"?>
<mb:menuBars
    xmlns:mb="https://causeway.apache.org/applib/layout/menubars/bootstrap3"
    xmlns:cpt="https://causeway.apache.org/applib/layout/component"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="https://causeway.apache.org/applib/layout/menubars/bootstrap3 menubars.xsd">
  <mb:primary>
    <mb:menu cssClassFa="fa-building">
      <mb:named>Samples &amp; Objects</mb:named>
      <mb:section>
        <mb:named>Explore</mb:named>
        <mb:serviceAction objectType="causeway.webcomponents.sample.SampleMenu" id="welcomeMessage" cssClassFa="fa-message" cssClassFaPosition="RIGHT">
          <cpt:named>Welcome Message</cpt:named>
          <cpt:describedAs>Return a friendly greeting.</cpt:describedAs>
        </mb:serviceAction>
        <mb:serviceAction objectType="causeway.webcomponents.sample.SampleMenu" id="disabledAction">
          <cpt:named>Disabled Action</cpt:named>
          <cpt:describedAs>Performs an administrative operation.</cpt:describedAs>
        </mb:serviceAction>
        <mb:serviceAction objectType="causeway.webcomponents.sample.SampleMenu" id="hiddenAction"/>
      </mb:section>
    </mb:menu>
  </mb:primary>
  <mb:secondary>
    <mb:menu>
      <mb:named>Tools</mb:named>
      <mb:section>
        <mb:serviceAction objectType="causeway.webcomponents.sample.SampleMenu" id="greet">
          <cpt:named>Personal Greeting</cpt:named>
        </mb:serviceAction>
      </mb:section>
    </mb:menu>
  </mb:secondary>
  <mb:tertiary>
    <mb:menu>
      <mb:named>Administration</mb:named>
      <mb:section>
        <mb:serviceAction objectType="causeway.webcomponents.sample.AdminMenu" id="clearNotes"/>
      </mb:section>
    </mb:menu>
  </mb:tertiary>
</mb:menuBars>`;

export const MENU_BARS_WITH_UNSUPPORTED_CONTENT_XML = MENU_BARS_XML.replace(
  '<mb:named>Explore</mb:named>',
  '<mb:named>Explore</mb:named><mb:future-setting value="ignored"/>'
);

export const MENU_BARS_WITH_INVALID_REFERENCE_XML = MENU_BARS_XML.replace(
  '<mb:serviceAction objectType="causeway.webcomponents.sample.SampleMenu" id="hiddenAction"/>',
  '<mb:serviceAction objectType="unsafe service" id="not-valid!"/>'
);

export const MENU_ACTION_STATES = new Map([
  ['causeway.webcomponents.sample.SampleMenu#welcomeMessage', Object.freeze({hidden: false, disabled: null, error: null})],
  ['causeway.webcomponents.sample.SampleMenu#disabledAction', Object.freeze({hidden: false, disabled: 'Available to administrators only.', error: null})],
  ['causeway.webcomponents.sample.SampleMenu#hiddenAction', Object.freeze({hidden: true, disabled: null, error: null})],
  ['causeway.webcomponents.sample.SampleMenu#greet', Object.freeze({hidden: false, disabled: null, error: null})],
  ['causeway.webcomponents.sample.AdminMenu#clearNotes', Object.freeze({hidden: false, disabled: null, error: null})]
]);
