import{A as Pt,B as X,C as H,D as B,E as Lt,F as Ot,G as S,H as Vt,I as $,J as _e,b as he,c as yt,d as At,e as V,f as Dt,g as kt,h as j,i as R,j as Et,k as ce,l as Mt,m as Pe,o as Tt,p as F,q as Q,r as U,s as It,t as Ft,u as Bt,v as le,w as $t,y as G,z as me}from"./chunks/chunk-T6TECDK2.js";import{a as W,c as fe,d as zt}from"./chunks/chunk-V7PLVXUQ.js";import{G as pe,H as q,K as Y,M as D,N as A,O as ne,P as k,Q as E,R as K,S as St,a as b,b as m,c as d,d as oe,e as gt,f as c,g as h,h as M,k as p,l as v,m as bt,n as f,r as xt,t as ue,v as z,w as se,x as wt,y as Ct}from"./chunks/chunk-JAZKWOIA.js";var Rt=m`
  :host {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    gap: var(--vaadin-button-gap, 0 var(--vaadin-gap-s));
    white-space: var(--vaadin-button-label-wrap, normal);
    -webkit-tap-highlight-color: transparent;
    -webkit-user-select: none;
    user-select: none;
    cursor: var(--vaadin-clickable-cursor);
    box-sizing: border-box;
    flex-shrink: 0;
    height: var(--vaadin-button-height, fit-content);
    margin: var(--vaadin-button-margin, 0);
    padding: var(--vaadin-button-padding, var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container));
    font-family: var(--vaadin-button-font-family, inherit);
    font-size: var(--vaadin-button-font-size, inherit);
    line-height: var(--vaadin-button-line-height, inherit);
    font-weight: var(--vaadin-button-font-weight, 500);
    color: var(--vaadin-button-text-color, var(--vaadin-text-color));
    background: var(--vaadin-button-background, var(--vaadin-background-container));
    background-origin: border-box;
    border: var(--vaadin-button-border-width, 1px) solid
      var(--vaadin-button-border-color, var(--vaadin-border-color-secondary));
    border-radius: var(--vaadin-button-border-radius, var(--vaadin-radius-m));
    touch-action: manipulation;
  }

  :host([hidden]) {
    display: none !important;
  }

  .vaadin-button-container,
  [part='prefix'],
  [part='suffix'] {
    display: contents;
  }

  [part='label'] {
    overflow: hidden;
    text-overflow: ellipsis;
  }

  :host(:is([focus-ring], :focus-visible)) {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: 1px;
  }

  :host([theme~='primary']) {
    --vaadin-button-background: var(--vaadin-text-color);
    --vaadin-button-text-color: var(--vaadin-background-color);
    --vaadin-button-border-color: transparent;
  }

  :host([theme~='tertiary']) {
    background: transparent;
    border-color: transparent;
  }

  :host([disabled]) {
    pointer-events: var(--_vaadin-button-disabled-pointer-events, none);
    cursor: var(--vaadin-disabled-cursor);
    opacity: 0.5;
  }

  :host([disabled][theme~='primary']) {
    --vaadin-button-text-color: var(--vaadin-background-container-strong);
    --vaadin-button-background: var(--vaadin-text-color-disabled);
  }

  @media (forced-colors: active) {
    :host {
      --vaadin-button-border-width: 1px;
      --vaadin-button-background: ButtonFace;
      --vaadin-button-text-color: ButtonText;
    }

    :host([theme~='primary']) {
      forced-color-adjust: none;
      --vaadin-button-background: CanvasText;
      --vaadin-button-text-color: Canvas;
      --vaadin-icon-color: Canvas;
    }

    ::slotted(*) {
      forced-color-adjust: auto;
    }

    :host([disabled]) {
      --vaadin-button-background: transparent !important;
      --vaadin-button-border-color: GrayText !important;
      --vaadin-button-text-color: GrayText !important;
      opacity: 1;
    }
  }
`;var J=o=>class extends K(U(o)){get _activeKeys(){return[" "]}ready(){super.ready(),W(this,"down",e=>{this._shouldSetActive(e)&&this._setActive(!0)}),W(this,"up",()=>{this._setActive(!1)})}disconnectedCallback(){super.disconnectedCallback(),this._setActive(!1)}_shouldSetActive(e){return!this.disabled}_onKeyDown(e){super._onKeyDown(e),this._shouldSetActive(e)&&this._activeKeys.includes(e.key)&&(this._setActive(!0),document.addEventListener("keyup",t=>{this._activeKeys.includes(t.key)&&this._setActive(!1)},{once:!0}))}_setActive(e){this.toggleAttribute("active",e)}};var Xi=["mousedown","mouseup","click","dblclick","keypress","keydown","keyup"],ve=o=>class extends J(St(F(o))){constructor(){super(),this.__onInteractionEvent=this.__onInteractionEvent.bind(this),Xi.forEach(e=>{this.addEventListener(e,this.__onInteractionEvent,!0)}),this.tabindex=0}get _activeKeys(){return["Enter"," "]}ready(){super.ready(),this.hasAttribute("role")||this.setAttribute("role","button"),this.__shouldAllowFocusWhenDisabled()&&this.style.setProperty("--_vaadin-button-disabled-pointer-events","auto")}_onKeyDown(e){super._onKeyDown(e),!(e.altKey||e.shiftKey||e.ctrlKey||e.metaKey)&&this._activeKeys.includes(e.key)&&(e.preventDefault(),this.click())}__onInteractionEvent(e){this.__shouldSuppressInteractionEvent(e)&&e.stopImmediatePropagation()}__shouldSuppressInteractionEvent(e){return this.disabled}};var Le=class extends ve(A(f(p(v(c))))){static get is(){return"vaadin-button"}static get styles(){return Rt}static get properties(){return{disabled:{type:Boolean,value:!1,observer:"_disabledChanged",reflectToAttribute:!0,sync:!0}}}render(){return d`
      <div class="vaadin-button-container">
        <span part="prefix" aria-hidden="true">
          <slot name="prefix"></slot>
        </span>
        <span part="label">
          <slot></slot>
        </span>
        <span part="suffix" aria-hidden="true">
          <slot name="suffix"></slot>
        </span>

        <slot name="tooltip"></slot>
      </div>
    `}ready(){super.ready(),this._tooltipController=new E(this),this.addController(this._tooltipController)}__shouldAllowFocusWhenDisabled(){return window.Vaadin.featureFlags.accessibleDisabledButtons}};h(Le);var Ut=(o,r=o)=>m`
  :host {
    align-items: baseline;
    column-gap: var(--vaadin-${b(r)}-gap, var(--vaadin-gap-s));
    grid-template: none;
    grid-template-columns: auto 1fr;
    grid-template-rows: repeat(auto-fill, minmax(0, max-content));
    -webkit-tap-highlight-color: transparent;
    --_cursor: var(--vaadin-clickable-cursor);
  }

  :host([disabled]) {
    --_cursor: var(--vaadin-disabled-cursor);
  }

  :host(:not([has-label])) {
    column-gap: 0;
  }

  [part='${b(o)}'],
  ::slotted(input),
  [part='label'],
  ::slotted(label) {
    grid-row: 1;
  }

  [part='label'],
  ::slotted(label) {
    font-size: var(--vaadin-${b(r)}-label-font-size, var(--vaadin-input-field-label-font-size, inherit));
    line-height: var(--vaadin-${b(r)}-label-line-height, var(--vaadin-input-field-label-line-height, inherit));
    font-weight: var(--vaadin-${b(r)}-label-font-weight, var(--vaadin-input-field-label-font-weight, 500));
    color: var(--vaadin-${b(r)}-label-color, var(--vaadin-input-field-label-color, var(--vaadin-text-color)));
    word-break: break-word;
    cursor: var(--_cursor);
  }

  [part='${b(o)}'],
  ::slotted(input) {
    grid-column: 1;
  }

  [part='label'],
  [part='helper-text'],
  [part='error-message'] {
    margin-bottom: 0;
    grid-column: 2;
    width: auto;
    min-width: auto;
  }

  [part='helper-text'],
  [part='error-message'] {
    margin-top: var(--_gap-s);
    grid-row: auto;
  }

  /* Baseline vertical alignment */
  :host::before {
    grid-row: 1;
    margin: 0;
    padding: 0;
    border: 0;
  }

  /* visually hidden */
  ::slotted(input) {
    cursor: inherit;
    align-self: stretch;
    appearance: none;
    cursor: var(--_cursor);
    /* Ensure minimum click target (WCAG) */
    margin: min(0px, (24px - 100%) / -2) !important;
    /* Extend the input to cover the gap between the checkbox/radio and label */
    margin-inline-end: calc(min(0px, (24px - 100%) / -2) - var(--vaadin-${b(r)}-gap, var(--vaadin-gap-s))) !important;
  }

  /* Control container (checkbox, radio button) */
  [part='${b(o)}'] {
    background: var(--vaadin-${b(r)}-background, var(--vaadin-background-color));
    border-color: var(--vaadin-${b(r)}-border-color, var(--vaadin-input-field-border-color, var(--vaadin-border-color)));
    border-radius: var(--vaadin-${b(r)}-border-radius, var(--vaadin-radius-s));
    border-style: var(--_border-style, solid);
    --_border-width: var(--vaadin-${b(r)}-border-width, var(--vaadin-input-field-border-width, 1px));
    border-width: var(--_border-width);
    box-sizing: border-box;
    --_color: var(--vaadin-${b(r)}-marker-color, var(--vaadin-${b(r)}-background, var(--vaadin-background-color)));
    color: var(--_color);
    height: var(--vaadin-${b(r)}-size, 1lh);
    width: var(--vaadin-${b(r)}-size, 1lh);
    position: relative;
    cursor: var(--_cursor);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  :host(:is([checked], [indeterminate])) {
    --vaadin-${b(r)}-background: var(--vaadin-text-color);
    --vaadin-${b(r)}-border-color: transparent;
  }

  :host([disabled]) {
    --vaadin-${b(r)}-background: var(--vaadin-input-field-disabled-background, var(--vaadin-background-container-strong));
    --vaadin-${b(r)}-border-color: transparent;
    --vaadin-${b(r)}-marker-color: var(--vaadin-text-color-disabled);
  }

  /* Focus ring */
  :host([focus-ring]) [part='${b(o)}'] {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--_border-width) * -1);
  }

  :host([focus-ring]:is([checked], [indeterminate])) [part='${b(o)}'] {
    outline-offset: 1px;
  }

  :host([readonly][focus-ring]) [part='${b(o)}'] {
    --vaadin-${b(r)}-border-color: transparent;
    outline-offset: calc(var(--_border-width) * -1);
    outline-style: dashed;
  }

  /* Checked indicator (checkmark, dot) */
  [part='${b(o)}']::after {
    content: '\\2003' / '';
    background: currentColor;
    border-radius: inherit;
    display: flex;
    align-items: center;
    --_filter: var(--vaadin-${b(r)}-marker-color, saturate(0) invert(1) hue-rotate(180deg) contrast(100) brightness(100));
    filter: var(--_filter);
  }

  :host(:not([checked], [indeterminate])) [part='${b(o)}']::after {
    opacity: 0;
  }

  @media (forced-colors: active) {
    :host(:is([checked], [indeterminate])) {
      --vaadin-${b(r)}-border-color: CanvasText !important;
    }

    :host(:is([checked], [indeterminate])) [part='${b(o)}'] {
      background: SelectedItem !important;
    }

    :host(:is([checked], [indeterminate])) [part='${b(o)}']::after {
      background: SelectedItemText !important;
    }

    :host([readonly]) [part='${b(o)}']::after {
      background: CanvasText !important;
    }

    :host([disabled]) {
      --vaadin-${b(r)}-border-color: GrayText !important;
    }

    :host([disabled]) [part='${b(o)}']::after {
      background: GrayText !important;
    }
  }
`;var Ji=m`
  [part='checkbox'] {
    color: var(--vaadin-checkbox-checkmark-color, var(--_color));
  }

  [part='checkbox']::after {
    inset: 0;
    mask: var(--_vaadin-icon-checkmark) 50% /
      var(--vaadin-checkbox-checkmark-size, var(--vaadin-checkbox-marker-size, 100%)) no-repeat;
    filter: var(--vaadin-checkbox-checkmark-color, var(--_filter));
  }

  :host([readonly]) {
    --vaadin-checkbox-background: transparent;
    --vaadin-checkbox-border-color: var(--vaadin-border-color);
    --vaadin-checkbox-marker-color: var(--vaadin-text-color);
    --_border-style: dashed;
  }

  :host([indeterminate]) [part='checkbox']::after {
    mask-image: var(--_vaadin-icon-minus);
  }
`,Ht=[Ot,Ut("checkbox"),Ji];var Nt=o=>class extends me(K(Ft(o))){static get properties(){return{checked:{type:Boolean,value:!1,notify:!0,reflectToAttribute:!0,sync:!0}}}static get delegateProps(){return[...super.delegateProps,"checked"]}_onChange(e){let t=e.target;this._toggleChecked(t.checked)}_toggleChecked(e){this.checked=e}};var jt=o=>class extends It(G(Nt(Q(J(o))))){static get properties(){return{indeterminate:{type:Boolean,notify:!0,value:!1,reflectToAttribute:!0},name:{type:String,value:""},readonly:{type:Boolean,value:!1,reflectToAttribute:!0}}}static get observers(){return["__readonlyChanged(readonly, inputElement)"]}static get delegateProps(){return[...super.delegateProps,"indeterminate"]}static get delegateAttrs(){return[...super.delegateAttrs,"name","invalid","required"]}constructor(){super(),this._setType("checkbox"),this._boundOnInputClick=this._onInputClick.bind(this),this.value="on",this.tabindex=0}get slotStyles(){return[`
          ${this.localName} > input[slot='input'] {
            opacity: 0;
          }
        `]}ready(){super.ready(),this.addController(new H(this,e=>{this._setInputElement(e),this._setFocusElement(e),this.stateTarget=e,this.ariaTarget=e})),this.addController(new B(this.inputElement,this._labelController)),this._createPropertyObserver("checked","_checkedChanged")}_shouldSetActive(e){let[t]=e.composedPath(),i=t===this.inputElement||t.part.contains("required-indicator")||this._labelNode.contains(t)&&!t.closest("a");return this.readonly||!i?!1:super._shouldSetActive(e)}_addInputListeners(e){super._addInputListeners(e),e.addEventListener("click",this._boundOnInputClick)}_removeInputListeners(e){super._removeInputListeners(e),e.removeEventListener("click",this._boundOnInputClick)}_onInputClick(e){this.readonly&&e.preventDefault()}__readonlyChanged(e,t){t&&(e?t.setAttribute("aria-readonly","true"):t.removeAttribute("aria-readonly"))}_toggleChecked(e){this.indeterminate&&(this.indeterminate=!1),super._toggleChecked(e)}checkValidity(){return!this.required||!!this.checked}_setFocused(e){super._setFocused(e),!e&&document.hasFocus()&&this._requestValidation()}_checkedChanged(e,t){(e||t)&&this._requestValidation()}_requiredChanged(e){super._requiredChanged(e),e===!1&&this._requestValidation()}_onRequiredIndicatorClick(){this._labelNode.click()}};var Oe=class extends jt(A(f(p(v(c))))){static get is(){return"vaadin-checkbox"}static get styles(){return Ht}render(){return d`
      <div class="vaadin-checkbox-container">
        <div part="checkbox" aria-hidden="true"></div>
        <slot name="input"></slot>
        <div part="label">
          <slot name="label"></slot>
          <div part="required-indicator" @click="${this._onRequiredIndicatorClick}"></div>
        </div>
        <div part="helper-text">
          <slot name="helper"></slot>
        </div>
        <div part="error-message">
          <slot name="error-message"></slot>
        </div>
      </div>
      <slot name="tooltip"></slot>
    `}ready(){super.ready(),this._tooltipController=new E(this),this._tooltipController.setAriaTarget(this.inputElement),this.addController(this._tooltipController)}};h(Oe);var qt=m`
  [part='overlay'] {
    display: flex;
    flex: auto;
    max-height: var(--vaadin-date-picker-overlay-max-height, 30rem);
    box-sizing: content-box;
    width: var(
      --vaadin-date-picker-overlay-width,
      round(
        var(--vaadin-date-picker-date-width, 2rem) * 7 +
          var(--vaadin-date-picker-month-padding, var(--vaadin-padding-s)) * 2 +
          var(--vaadin-date-picker-year-scroller-width, 3rem),
        1px
      )
    );
    cursor: default;
  }

  :host([fullscreen]) [part='backdrop'] {
    display: block;
  }

  :host([fullscreen]) [part='overlay'] {
    border: none;
    border-radius: 0;
    max-height: 75vh;
    width: 100%;
  }

  [part~='content'] {
    flex: auto;
  }

  @media (max-width: 450px), (max-height: 450px) {
    :host {
      inset: auto 0 0 !important;
    }
  }
`;var Yt=o=>class extends ce(R(o)){_shouldCloseOnOutsideClick(e){return!e.composedPath().includes(this.positionTarget)}_mouseDownListener(e){super._mouseDownListener(e),this._shouldCloseOnOutsideClick(e)&&!wt(e.composedPath()[0])&&e.preventDefault()}};var Ve=class extends Yt(M(f(p(v(c))))){static get is(){return"vaadin-date-picker-overlay"}static get styles(){return[V,qt]}render(){return d`
      <div id="backdrop" part="backdrop" ?hidden="${!this.withBackdrop}"></div>
      <div part="overlay" id="overlay">
        <div part="content" id="content">
          <slot></slot>
        </div>
      </div>
    `}get _contentRoot(){return this.owner._overlayContent}};h(Ve);function Wt(o){let r=o.getDay();r===0&&(r=7);let e=4-r,t=new Date(o.getTime()+e*24*3600*1e3),i=new Date(0,0);i.setFullYear(t.getFullYear());let a=t.getTime()-i.getTime(),s=Math.round(a/(24*3600*1e3));return Math.floor(s/7+1)}function ge(o){let r=new Date(o);return r.setHours(0,0,0,0),r}function de(o){return new Date(Date.UTC(o.getUTCFullYear(),o.getUTCMonth(),o.getUTCDate(),0,0,0,0))}function C(o,r,e=ge){return o instanceof Date&&r instanceof Date&&e(o).getTime()===e(r).getTime()}function ze(o){return{day:o.getDate(),month:o.getMonth(),year:o.getFullYear()}}function O(o,r,e,t){let i=!1;if(typeof t=="function"&&o){let a=ze(o);i=t(a)}return(!r||o>=r)&&(!e||o<=e)&&!i}function be(o,r){return r.filter(e=>e!==void 0).reduce((e,t)=>{if(!t)return e;if(!e)return t;let i=Math.abs(o.getTime()-t.getTime()),a=Math.abs(e.getTime()-o.getTime());return i<a?t:e})}function ye(o){let r=new Date,e=new Date(r);return e.setDate(1),e.setMonth(parseInt(o)+r.getMonth()),e}function Kt(o,r,e=0,t=1){if(r>99)throw new Error("The provided year cannot have more than 2 digits.");if(r<0)throw new Error("The provided year cannot be negative.");let i=r+Math.floor(o.getFullYear()/100)*100;return o<new Date(i-50,e,t)?i-=100:o>new Date(i+50,e,t)&&(i+=100),i}function Z(o){let r=/^([-+]\d{1}|\d{2,4}|[-+]\d{6})-(\d{1,2})-(\d{1,2})$/u.exec(o);if(!r)return;let e=new Date(0,0);return e.setFullYear(parseInt(r[1],10)),e.setMonth(parseInt(r[2],10)-1),e.setDate(parseInt(r[3],10)),e}function Qt(o){let r=/^([-+]\d{1}|\d{2,4}|[-+]\d{6})-(\d{1,2})-(\d{1,2})$/u.exec(o);if(!r)return;let e=new Date(Date.UTC(0,0));return e.setUTCFullYear(parseInt(r[1],10)),e.setUTCMonth(parseInt(r[2],10)-1),e.setUTCDate(parseInt(r[3],10)),e}function Gt(o){let r=(l,u="00")=>(u+l).substr((u+l).length-u.length),e="",t="0000",i=o.year;i<0?(i=-i,e="-",t="000000"):o.year>=1e4&&(e="+",t="000000");let a=e+r(i,t),s=r(o.month+1),n=r(o.day);return[a,s,n].join("-")}function Xt(o){return o instanceof Date?Gt({year:o.getFullYear(),month:o.getMonth(),day:o.getDate()}):""}function Jt(o){return o instanceof Date?Gt({year:o.getUTCFullYear(),month:o.getUTCMonth(),day:o.getUTCDate()}):""}var Zt=document.createElement("template");Zt.innerHTML=`
  <style>
    :host {
      display: block;
      overflow: hidden;
      height: 500px;
    }

    #scroller {
      position: relative;
      height: 100%;
      overflow: auto;
      /* Prevent browser scroll anchoring from overriding the virtual scroll position. */
      overflow-anchor: none;
      outline: none;
      overflow-x: hidden;
      scrollbar-width: none;
    }

    #scroller::-webkit-scrollbar {
      display: none;
    }

    .buffer {
      position: absolute;
      width: var(--vaadin-infinite-scroller-buffer-width, 100%);
      box-sizing: border-box;
      top: var(--vaadin-infinite-scroller-buffer-offset, 0);
    }
  </style>

  <div id="scroller" tabindex="-1">
    <div class="buffer"></div>
    <div class="buffer"></div>
    <div id="fullHeight"></div>
  </div>
`;var ee=class extends HTMLElement{constructor(){super(),this.attachShadow({mode:"open"}).appendChild(Zt.content.cloneNode(!0)),this.bufferSize=20,this._initialScroll=5e5,this._initialIndex=0,this._activated=!1}get active(){return this._activated}set active(r){r&&!this._activated&&(this._createPool(),this._activated=!0)}get bufferOffset(){return this._buffers[0].offsetTop}get itemHeight(){if(!this._itemHeightVal){let r=getComputedStyle(this).getPropertyValue("--vaadin-infinite-scroller-item-height"),e="background-position";this.$.fullHeight.style.setProperty(e,r);let t=getComputedStyle(this.$.fullHeight).getPropertyValue(e);this.$.fullHeight.style.removeProperty(e),this._itemHeightVal=parseFloat(t)}return this._itemHeightVal}get _bufferHeight(){return this.itemHeight*this.bufferSize}get position(){return(this.$.scroller.scrollTop-this._buffers[0].translateY)/this.itemHeight+this._firstIndex}set position(r){this._preventScrollEvent=!0,r>this._firstIndex&&r<this._firstIndex+this.bufferSize*2?this.$.scroller.scrollTop=this.itemHeight*(r-this._firstIndex)+this._buffers[0].translateY:(this._initialIndex=~~r,this._reset(),this._scrollDisabled=!0,this.$.scroller.scrollTop+=r%1*this.itemHeight,this._scrollDisabled=!1)}connectedCallback(){this._ready||(this._ready=!0,this.$={},this.shadowRoot.querySelectorAll("[id]").forEach(r=>{this.$[r.id]=r}),this.$.scroller.addEventListener("scroll",()=>this._scroll()),this._buffers=[...this.shadowRoot.querySelectorAll(".buffer")],this.$.fullHeight.style.height=`${this._initialScroll*2}px`)}disconnectedCallback(){this._debouncerScrollFinish&&this._debouncerScrollFinish.cancel(),this._debouncerUpdateClones&&this._debouncerUpdateClones.cancel(),this.__pendingFinishInit&&cancelAnimationFrame(this.__pendingFinishInit)}forceUpdate(){this._debouncerScrollFinish&&this._debouncerScrollFinish.flush(),this._debouncerUpdateClones&&(this._buffers[0].updated=this._buffers[1].updated=!1,this._updateClones(),this._debouncerUpdateClones.cancel())}_createElement(){}_updateElement(r,e){}_finishInit(){this._initDone||(this._buffers.forEach(r=>{[...r.children].forEach(e=>{this._ensureStampedInstance(e._itemWrapper)})}),this._buffers[0].translateY||this._reset(),this._initDone=!0,this.dispatchEvent(new CustomEvent("init-done")))}_translateBuffer(r){let e=r?1:0;this._buffers[e].translateY=this._buffers[e?0:1].translateY+this._bufferHeight*(e?-1:1),this._buffers[e].style.transform=`translate3d(0, ${this._buffers[e].translateY}px, 0)`,this._buffers[e].updated=!1,this._buffers.reverse()}_scroll(){if(this._scrollDisabled)return;let r=this.$.scroller.scrollTop;(r<this._bufferHeight||r>this._initialScroll*2-this._bufferHeight)&&(this._initialIndex=~~this.position,this._reset());let e=this.itemHeight+this.bufferOffset,t=r>this._buffers[1].translateY+e,i=r<this._buffers[0].translateY+e;(t||i)&&(this._translateBuffer(i),this._updateClones()),this._preventScrollEvent||this.dispatchEvent(new CustomEvent("custom-scroll",{bubbles:!1,composed:!0})),this._preventScrollEvent=!1,this._debouncerScrollFinish=Y.debounce(this._debouncerScrollFinish,q.after(200),()=>{let a=this.$.scroller.getBoundingClientRect();!this._isVisible(this._buffers[0],a)&&!this._isVisible(this._buffers[1],a)&&(this.position=this.position)})}_reset(){this._scrollDisabled=!0,this.$.scroller.scrollTop=this._initialScroll,this._buffers[0].translateY=this._initialScroll-this._bufferHeight,this._buffers[1].translateY=this._initialScroll,this._buffers.forEach(r=>{r.style.transform=`translate3d(0, ${r.translateY}px, 0)`}),this._buffers[0].updated=this._buffers[1].updated=!1,this._updateClones(!0),this._debouncerUpdateClones=Y.debounce(this._debouncerUpdateClones,q.after(200),()=>{this._buffers[0].updated=this._buffers[1].updated=!1,this._updateClones()}),this._scrollDisabled=!1}_createPool(){let r=this.innerHeight;this._buffers.forEach(e=>{for(let t=0;t<this.bufferSize;t++){let i=document.createElement("div");i.style.height=`${this.itemHeight}px`,i.instance={};let a=`vaadin-infinite-scroller-item-content-${pe()}`,s=document.createElement("slot");s.setAttribute("name",a),s._itemWrapper=i,e.appendChild(s),i.setAttribute("slot",a),this.appendChild(i),this.itemHeight*t<=r&&this._ensureStampedInstance(i)}}),this.__pendingFinishInit=requestAnimationFrame(()=>{this._finishInit(),this.__pendingFinishInit=null})}_ensureStampedInstance(r){if(r.firstElementChild)return;let e=r.instance;r.instance=this._createElement(),r.appendChild(r.instance),Object.keys(e).forEach(t=>{r.instance[t]=e[t]})}_updateClones(r){this._firstIndex=Math.round((this._buffers[0].translateY-this._initialScroll)/this.itemHeight)+this._initialIndex;let e=r?this.$.scroller.getBoundingClientRect():void 0;this._buffers.forEach((t,i)=>{if(!t.updated){let a=this._firstIndex+this.bufferSize*i;[...t.children].forEach((s,n)=>{let l=s._itemWrapper;(!r||this._isVisible(l,e))&&this._updateElement(l.instance,a+n)}),t.updated=!0}})}_isVisible(r,e){let t=r.getBoundingClientRect();return t.bottom>e.top&&t.top<e.bottom}};var ei=document.createElement("template");ei.innerHTML=`
  <style>
    :host {
      --vaadin-infinite-scroller-item-height: 270px;
      grid-area: months;
      height: auto;
    }
  </style>
`;var Re=class extends ee{static get is(){return"vaadin-date-picker-month-scroller"}constructor(){super(),this.bufferSize=3,this.shadowRoot.appendChild(ei.content.cloneNode(!0))}_createElement(){return document.createElement("vaadin-month-calendar")}_updateElement(r,e){r.month=ye(e)}};h(Re);var ti=document.createElement("template");ti.innerHTML=`
  <style>
    :host {
      --vaadin-infinite-scroller-item-height: 80px;
      width: 50px;
      display: block;
      position: relative;
      grid-area: years;
      height: auto;
      -webkit-tap-highlight-color: transparent;
      -webkit-user-select: none;
      user-select: none;
      /* Center the year scroller position. */
      --vaadin-infinite-scroller-buffer-offset: 50%;
    }

    :host::before {
      content: '';
      display: block;
      background: transparent;
      width: 0;
      height: 0;
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      border-width: 6px;
      border-style: solid;
      border-color: transparent;
      border-left-color: #000;
    }
  </style>
`;var Ue=class extends ee{static get is(){return"vaadin-date-picker-year-scroller"}constructor(){super(),this.bufferSize=12,this.shadowRoot.appendChild(ti.content.cloneNode(!0))}_createElement(){return document.createElement("vaadin-date-picker-year")}_updateElement(r,e){r.year=this._yearAfterXYears(e)}_yearAfterXYears(r){let e=new Date,t=new Date(e);return t.setFullYear(parseInt(r)+e.getFullYear()),t.getFullYear()}};h(Ue);var ii=m`
  :host {
    display: block;
    height: 100%;
  }

  [part='year-number'] {
    align-items: center;
    display: flex;
    height: 50%;
    justify-content: center;
    transform: translateY(-50%);
    color: var(--vaadin-text-color-secondary);
  }

  :host([current]) [part='year-number'] {
    color: var(--vaadin-date-picker-year-scroller-current-year-color, var(--vaadin-text-color));
  }
`;var He=class extends f(p(v(c))){static get is(){return"vaadin-date-picker-year"}static get styles(){return ii}static get properties(){return{year:{type:String,sync:!0},selectedDate:{type:Object,sync:!0}}}render(){return d`
      <div part="year-number">${this.year}</div>
      <div part="year-separator" aria-hidden="true"></div>
    `}updated(r){super.updated(r),r.has("year")&&this.toggleAttribute("current",this.year===new Date().getFullYear()),(r.has("year")||r.has("selectedDate"))&&this.toggleAttribute("selected",this.selectedDate&&this.selectedDate.getFullYear()===this.year)}};h(He);var ri=m`
  :host {
    display: block;
    padding: var(--vaadin-date-picker-month-padding, var(--vaadin-padding-s));
  }

  [part='month-header'] {
    color: var(--vaadin-date-picker-month-header-color, var(--vaadin-text-color));
    font-size: var(--vaadin-date-picker-month-header-font-size, 0.9375rem);
    font-weight: var(--vaadin-date-picker-month-header-font-weight, 500);
    line-height: 1;
    margin-bottom: 0.75rem;
    text-align: center;
  }

  table {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
  }

  thead,
  tbody,
  tr {
    display: contents;
  }

  [part~='weekday'] {
    color: var(--vaadin-date-picker-weekday-color, var(--vaadin-text-color-secondary));
    font-size: var(--vaadin-date-picker-weekday-font-size, 0.75rem);
    font-weight: var(--vaadin-date-picker-weekday-font-weight, 500);
    margin-bottom: 0.375rem;
  }

  /* Week numbers are on a separate row, don't reserve space on weekday row. */
  [part~='weekday']:empty {
    display: none;
  }

  [part~='week-number'] {
    grid-column: -1 / 1;
    color: var(--vaadin-date-picker-week-number-color, var(--vaadin-text-color-secondary));
    font-size: var(--vaadin-date-picker-week-number-font-size, 0.7rem);
    line-height: 1;
    margin-top: 0.125em;
    margin-bottom: 0.125em;
    gap: 0.25em;
  }

  [part~='week-number']::after {
    content: '';
    height: 1px;
    flex: 1;
    background: var(
      --vaadin-date-picker-week-divider-color,
      var(--vaadin-divider-color, var(--vaadin-border-color-secondary))
    );
  }

  [part~='weekday'],
  [part~='week-number'],
  [part~='date'] {
    align-items: center;
    display: flex;
    justify-content: center;
    padding: 0;
  }

  [part~='date'] {
    border-radius: var(--vaadin-date-picker-date-border-radius, var(--vaadin-radius-m));
    position: relative;
    height: var(--vaadin-date-picker-date-height, 2rem);
    cursor: var(--vaadin-clickable-cursor);
    outline: none;
  }

  [part~='date']:empty {
    pointer-events: none !important;
  }

  [part~='date']::after {
    border-radius: inherit;
    content: '';
    position: absolute;
    z-index: -1;
    height: min(2em, 100%);
    aspect-ratio: 1;
  }

  :where([part~='date']:focus-visible)::after {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-focus-ring-width) * -1);
  }

  [part~='today'] {
    color: var(--vaadin-date-picker-date-today-color, var(--vaadin-text-color));
  }

  [part~='selected'] {
    color: var(--vaadin-date-picker-date-selected-color, var(--vaadin-background-color));
  }

  [part~='selected']::after {
    background: var(--vaadin-date-picker-date-selected-background, var(--vaadin-text-color));
    outline-offset: 1px;
  }

  [disabled] {
    cursor: var(--vaadin-disabled-cursor);
    color: var(--vaadin-date-picker-date-disabled-color, var(--vaadin-text-color-disabled));
    opacity: 0.7;
  }

  [hidden] {
    display: none;
  }

  @media (forced-colors: active) {
    [part~='week-number']::after {
      background: CanvasText;
    }

    [part~='today'] {
      font-weight: 600;
    }

    [part~='selected'] {
      forced-color-adjust: none;
      --vaadin-date-picker-date-selected-color: SelectedItemText;
      color: SelectedItemText !important;
      --vaadin-date-picker-date-selected-background: SelectedItem;
    }

    [disabled] {
      color: GrayText !important;
    }
  }
`;var ai=o=>class extends F(o){static get properties(){return{month:{type:Object,value:new Date,sync:!0},selectedDate:{type:Object,notify:!0,sync:!0},focusedDate:{type:Object},showWeekNumbers:{type:Boolean,value:!1},i18n:{type:Object},ignoreTaps:{type:Boolean},minDate:{type:Date,value:null,sync:!0},maxDate:{type:Date,value:null,sync:!0},isDateDisabled:{type:Function,value:()=>!1},enteredDate:{type:Date},disabled:{type:Boolean,reflectToAttribute:!0,computed:"__computeDisabled(month, minDate, maxDate)"},_days:{type:Array,computed:"__computeDays(month, i18n, minDate, maxDate, isDateDisabled)"},_weeks:{type:Array,computed:"__computeWeeks(_days)"},_notTapping:{type:Boolean},__hasFocus:{type:Boolean}}}static get observers(){return["__focusedDateChanged(focusedDate, _days)","_showWeekNumbersChanged(showWeekNumbers, i18n)"]}get focusableDateElement(){return[...this.shadowRoot.querySelectorAll("[part~=date]")].find(e=>C(e.date,this.focusedDate))}ready(){super.ready(),W(this.$.monthGrid,"tap",this._handleTap.bind(this))}_setFocused(e){super._setFocused(e),this.__hasFocus=e}__computeDisabled(e,t,i){let a=new Date(0,0);a.setFullYear(e.getFullYear()),a.setMonth(e.getMonth()),a.setDate(1);let s=new Date(0,0);return s.setFullYear(e.getFullYear()),s.setMonth(e.getMonth()+1),s.setDate(0),t&&i&&t.getMonth()===i.getMonth()&&t.getMonth()===e.getMonth()&&i.getDate()-t.getDate()>=0?!1:!O(a,t,i)&&!O(s,t,i)}_getTitle(e,t){if(!(e===void 0||t===void 0))return t.formatTitle(t.monthNames[e.getMonth()],e.getFullYear())}_onMonthGridTouchStart(){this._notTapping=!1,setTimeout(()=>{this._notTapping=!0},300)}_dateAdd(e,t){e.setDate(e.getDate()+t)}_applyFirstDayOfWeek(e,t){if(!(e===void 0||t===void 0))return e.slice(t).concat(e.slice(0,t))}__computeWeekDayNames(e,t){if(e===void 0||t===void 0)return[];let{weekdays:i,weekdaysShort:a,firstDayOfWeek:s}=e,n=this._applyFirstDayOfWeek(a,s);return this._applyFirstDayOfWeek(i,s).map((u,_)=>({weekDay:u,weekDayShort:n[_]})).slice(0,7)}__focusedDateChanged(e,t){Array.isArray(t)&&t.some(i=>C(i,e))?this.removeAttribute("aria-hidden"):this.setAttribute("aria-hidden","true")}_getDate(e){return e?e.getDate():""}__computeShowWeekSeparator(e,t){return e&&t?.firstDayOfWeek===1}_isToday(e){return C(new Date,e)}__computeDays(e,t){if(e===void 0||t===void 0)return[];let i=new Date(0,0);for(i.setFullYear(e.getFullYear()),i.setMonth(e.getMonth()),i.setDate(1);i.getDay()!==t.firstDayOfWeek;)this._dateAdd(i,-1);let a=[],s=i.getMonth(),n=e.getMonth();for(;i.getMonth()===n||i.getMonth()===s;)a.push(i.getMonth()===n?new Date(i.getTime()):null),this._dateAdd(i,1);return a}__computeWeeks(e){return e.reduce((t,i,a)=>(a%7===0&&t.push([]),t[t.length-1].push(i),t),[])}_handleTap(e){!this.ignoreTaps&&!this._notTapping&&e.target.date&&!e.target.hasAttribute("disabled")&&(this.selectedDate=e.target.date,this.dispatchEvent(new CustomEvent("date-tap",{detail:{date:e.target.date},bubbles:!0,composed:!0})))}_preventDefault(e){e.preventDefault()}__computeWeekNumber(e){let t=e.reduce((i,a)=>!i&&a?a:i);return Wt(t)}__computeDayAriaLabel(e){if(!e)return"";let t=`${this._getDate(e)} ${this.i18n.monthNames[e.getMonth()]} ${e.getFullYear()}, ${this.i18n.weekdays[e.getDay()]}`;return this._isToday(e)&&(t+=`, ${this.i18n.today}`),t}_showWeekNumbersChanged(e,t){this.__computeShowWeekSeparator(e,t)?this.setAttribute("week-numbers",""):this.removeAttribute("week-numbers")}__computeDatePart(e,t,i,a,s,n,l,u){let _=["date"];return this.__isDayDisabled(e,a,s,n)&&_.push("disabled"),C(e,t)&&(u||C(e,l))&&_.push("focused"),this.__isDaySelected(e,i)&&_.push("selected"),this._isToday(e)&&_.push("today"),e<ge(new Date)&&_.push("past"),e>ge(new Date)&&_.push("future"),_.join(" ")}__isDaySelected(e,t){return C(e,t)}__computeDayAriaSelected(e,t){return String(this.__isDaySelected(e,t))}__isDayDisabled(e,t,i,a){return!O(e,t,i,a)}__computeDayAriaDisabled(e,t,i,a){return e===void 0||t===void 0&&i===void 0&&a===void 0?"false":String(this.__isDayDisabled(e,t,i,a))}__computeDayTabIndex(e,t){return C(e,t)?"0":"-1"}};var Ne=class extends ai(f(p(v(c)))){static get is(){return"vaadin-month-calendar"}static get styles(){return ri}render(){let r=this.__computeWeekDayNames(this.i18n,this.showWeekNumbers),e=this._weeks,t=!this.__computeShowWeekSeparator(this.showWeekNumbers,this.i18n);return d`
      <div part="month-header" id="month-header" aria-hidden="true">${this._getTitle(this.month,this.i18n)}</div>
      <table
        id="monthGrid"
        role="grid"
        aria-labelledby="month-header"
        @touchend="${this._preventDefault}"
        @touchstart="${this._onMonthGridTouchStart}"
      >
        <thead id="weekdays-container">
          <tr role="row" part="weekdays">
            <th part="weekday" aria-hidden="true" ?hidden="${t}"></th>
            ${r.map(i=>d`
                <th role="columnheader" part="weekday" scope="col" abbr="${i.weekDay}" aria-hidden="true">
                  ${i.weekDayShort}
                </th>
              `)}
          </tr>
        </thead>
        <tbody id="days-container">
          ${e.map(i=>d`
              <tr role="row">
                <td part="week-number" aria-hidden="true" ?hidden="${t}">
                  ${this.__computeWeekNumber(i)}
                </td>
                ${i.map(a=>d`
                    <td
                      role="gridcell"
                      part="${this.__computeDatePart(a,this.focusedDate,this.selectedDate,this.minDate,this.maxDate,this.isDateDisabled,this.enteredDate,this.__hasFocus)}"
                      .date="${a}"
                      ?disabled="${this.__isDayDisabled(a,this.minDate,this.maxDate,this.isDateDisabled)}"
                      tabindex="${this.__computeDayTabIndex(a,this.focusedDate)}"
                      aria-selected="${this.__computeDayAriaSelected(a,this.selectedDate)}"
                      aria-disabled="${this.__computeDayAriaDisabled(a,this.minDate,this.maxDate,this.isDateDisabled)}"
                      aria-label="${this.__computeDayAriaLabel(a)}"
                      >${this._getDate(a)}</td
                    >
                  `)}
              </tr>
            `)}
        </tbody>
      </table>
    `}};h(Ne);var oi=m`
  :host {
    display: grid;
    grid-template-areas:
      'header header'
      'months years'
      'toolbar years';
    grid-template-columns: minmax(0, 1fr) 0;
    height: 100%;
    outline: none;
    overflow: hidden;
  }

  :host([desktop]) {
    grid-template-columns: minmax(0, 1fr) auto;
  }

  :host([fullscreen][years-visible]) {
    grid-template-columns: minmax(0, 1fr) auto;
  }

  [part='years-toggle-button'] {
    display: inline-flex;
    align-items: center;
    border-radius: var(--vaadin-button-border-radius, var(--vaadin-radius-m));
    color: var(--vaadin-text-color);
    font-size: var(--vaadin-button-font-size, inherit);
    font-weight: var(--vaadin-button-font-weight, 500);
    height: var(--vaadin-button-height, auto);
    line-height: var(--vaadin-button-line-height, inherit);
    padding: var(--vaadin-button-padding, var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container));
    cursor: var(--vaadin-clickable-cursor);
  }

  :host([years-visible]) [part='years-toggle-button'] {
    background: var(--vaadin-text-color);
    color: var(--vaadin-background-color);
  }

  [hidden] {
    display: none !important;
  }

  ::slotted([slot='months']) {
    --vaadin-infinite-scroller-item-height: round(
      var(--vaadin-date-picker-month-header-font-size, 0.9375rem) + 0.75rem +
        var(--vaadin-date-picker-date-height, 2rem) * 7 + var(--_vaadin-date-picker-week-numbers-visible, 0) *
        (
          var(--vaadin-date-picker-week-number-font-size, 0.7rem) * 6.25 +
            var(--vaadin-date-picker-month-padding, var(--vaadin-padding-s)) * 3
        ),
      1px
    );
  }

  :host(:not([fullscreen])) ::slotted([slot='months']) {
    border-bottom: 1px solid var(--vaadin-border-color-secondary);
  }

  ::slotted([slot='years']) {
    visibility: hidden;
    background: var(--vaadin-date-picker-year-scroller-background, var(--vaadin-background-container));
    width: var(--vaadin-date-picker-year-scroller-width, 3rem);
    box-sizing: border-box;
    border-inline-start: 1px solid
      var(--vaadin-date-picker-year-scroller-border-color, var(--vaadin-border-color-secondary));
    overflow: visible;
    min-height: 0;
    clip-path: inset(0);
  }

  ::slotted([slot='years'])::before {
    background: var(--vaadin-overlay-background, var(--vaadin-background-color));
    border: 1px solid var(--vaadin-date-picker-year-scroller-border-color, var(--vaadin-border-color-secondary));
    width: 16px;
    height: 16px;
    position: absolute;
    left: auto;
    z-index: 1;
    rotate: 45deg;
    translate: calc(-50% - 1px) -50%;
    transform: none;
  }

  :host([dir='rtl']) ::slotted([slot='years'])::before {
    translate: calc(50% + 1px) -50%;
  }

  :host([desktop]) ::slotted([slot='years']),
  :host([years-visible]) ::slotted([slot='years']) {
    visibility: visible;
  }

  [part='toolbar'] {
    display: flex;
    grid-area: toolbar;
    justify-content: space-between;
    padding: var(--vaadin-date-picker-toolbar-padding, var(--vaadin-padding-s));
  }

  :host([fullscreen]) [part='toolbar'] {
    grid-area: header;
    border-bottom: 1px solid var(--vaadin-border-color-secondary);
  }
`;var N=class{constructor(r,e){this.query=r,this.callback=e,this._boundQueryHandler=this._queryHandler.bind(this)}hostConnected(){this._removeListener(),this._mediaQuery=window.matchMedia(this.query),this._addListener(),this._queryHandler(this._mediaQuery)}hostDisconnected(){this._removeListener()}_addListener(){this._mediaQuery&&this._mediaQuery.addListener(this._boundQueryHandler)}_removeListener(){this._mediaQuery&&this._mediaQuery.removeListener(this._boundQueryHandler),this._mediaQuery=null}_queryHandler(r){typeof this.callback=="function"&&this.callback(r.matches)}};var si=o=>class extends o{static get properties(){return{scrollDuration:{type:Number,value:300},selectedDate:{type:Object,value:null,sync:!0},focusedDate:{type:Object,notify:!0,observer:"_focusedDateChanged",sync:!0},_focusedMonthDate:Number,initialPosition:{type:Object,observer:"_initialPositionChanged",sync:!0},_originDate:{type:Object,value:new Date},_visibleMonthIndex:Number,_desktopMode:{type:Boolean,observer:"_desktopModeChanged"},_desktopMediaQuery:{type:String,value:"(min-width: 375px)"},i18n:{type:Object},showWeekNumbers:{type:Boolean,value:!1},_ignoreTaps:Boolean,_notTapping:Boolean,minDate:{type:Object,sync:!0},maxDate:{type:Object,sync:!0},isDateDisabled:{type:Function},enteredDate:{type:Date,sync:!0},label:String,_cancelButton:{type:Object},_todayButton:{type:Object},calendars:{type:Array,value:()=>[]},years:{type:Array,value:()=>[]}}}static get observers(){return["__updateCalendars(calendars, i18n, minDate, maxDate, selectedDate, focusedDate, showWeekNumbers, _ignoreTaps, _theme, isDateDisabled, enteredDate)","__updateCancelButton(_cancelButton, i18n)","__updateTodayButton(_todayButton, i18n, minDate, maxDate, isDateDisabled)","__updateYears(years, selectedDate, _theme)"]}get __useSubMonthScrolling(){return this._monthScroller.clientHeight<this._monthScroller.itemHeight+this._monthScroller.bufferOffset}get focusableDateElement(){return this.calendars.map(e=>e.focusableDateElement).find(Boolean)}_initControllers(){this.addController(new N(this._desktopMediaQuery,e=>{this._desktopMode=e})),this.addController(new k(this,"today-button","vaadin-button",{observe:!1,initializer:e=>{e.setAttribute("theme","tertiary"),e.addEventListener("keydown",t=>this.__onTodayButtonKeyDown(t)),e.addEventListener("click",this._onTodayTap.bind(this)),this._todayButton=e}})),this.addController(new k(this,"cancel-button","vaadin-button",{observe:!1,initializer:e=>{e.setAttribute("theme","tertiary"),e.addEventListener("keydown",t=>this.__onCancelButtonKeyDown(t)),e.addEventListener("click",this._cancel.bind(this)),this._cancelButton=e}})),this.__initMonthScroller(),this.__initYearScroller()}reset(){this._closeYearScroller()}focusCancel(){this._cancelButton.focus()}scrollToDate(e,t){let i=this.__useSubMonthScrolling?this._calculateWeekScrollOffset(e):0;this._scrollToPosition(this._differenceInMonths(e,this._originDate)+i,t),this._monthScroller.forceUpdate()}__initMonthScroller(){this.addController(new k(this,"months","vaadin-date-picker-month-scroller",{observe:!1,initializer:e=>{e.addEventListener("custom-scroll",()=>{this._onMonthScroll()}),e.addEventListener("touchstart",()=>{this._onMonthScrollTouchStart()}),e.addEventListener("keydown",t=>{this.__onMonthCalendarKeyDown(t)}),e.addEventListener("init-done",()=>{let t=[...this.querySelectorAll("vaadin-month-calendar")];t.forEach(i=>{i.addEventListener("selected-date-changed",a=>{this.selectedDate=a.detail.value})}),this.calendars=t}),this._monthScroller=e}}))}__initYearScroller(){this.addController(new k(this,"years","vaadin-date-picker-year-scroller",{observe:!1,initializer:e=>{e.setAttribute("aria-hidden","true"),W(e,"tap",t=>{this._onYearTap(t)}),e.addEventListener("custom-scroll",()=>{this._onYearScroll()}),e.addEventListener("touchstart",()=>{this._onYearScrollTouchStart()}),e.addEventListener("init-done",()=>{this.years=[...this.querySelectorAll("vaadin-date-picker-year")]}),this._yearScroller=e}}))}__updateCancelButton(e,t){e&&(e.textContent=t?.cancel)}__updateTodayButton(e,t,i,a,s){e&&(e.textContent=t?.today,e.disabled=!this._isTodayAllowed(i,a,s))}__updateCalendars(e,t,i,a,s,n,l,u,_,g,y){e?.length&&e.forEach(x=>{x.i18n=t,x.minDate=i,x.maxDate=a,x.isDateDisabled=g,x.focusedDate=n,x.selectedDate=s,x.showWeekNumbers=l,x.ignoreTaps=u,x.enteredDate=y,_?x.setAttribute("theme",_):x.removeAttribute("theme")})}__updateYears(e,t,i){e?.length&&e.forEach(a=>{a.selectedDate=t,i?a.setAttribute("theme",i):a.removeAttribute("theme")})}_selectDate(e){return this._dateAllowed(e)?(this.selectedDate=e,this.dispatchEvent(new CustomEvent("date-selected",{detail:{date:e},bubbles:!0,composed:!0})),!0):!1}_desktopModeChanged(e){this.toggleAttribute("desktop",e)}_focusedDateChanged(e){this.revealDate(e)}revealDate(e,t=!0){if(!e)return;let i=this._differenceInMonths(e,this._originDate);if(this.__useSubMonthScrolling){let u=this._calculateWeekScrollOffset(e);this._scrollToPosition(i+u,t);return}let a=this._monthScroller.position>i,n=Math.max(this._monthScroller.itemHeight,this._monthScroller.clientHeight-this._monthScroller.bufferOffset*2)/this._monthScroller.itemHeight,l=this._monthScroller.position+n-1<i;a?this._scrollToPosition(i,t):l&&this._scrollToPosition(i-n+1,t)}_calculateWeekScrollOffset(e){let t=new Date(0,0);t.setFullYear(e.getFullYear()),t.setMonth(e.getMonth()),t.setDate(1);let i=0;for(;t.getDate()<e.getDate();)t.setDate(t.getDate()+1),t.getDay()===this.i18n.firstDayOfWeek&&(i+=1);return i/6}_initialPositionChanged(e){this._monthScroller&&this._yearScroller&&(this._monthScroller.active=!0,this._yearScroller.active=!0),this.scrollToDate(e)}_repositionYearScroller(){let e=this._monthScroller.position;this._visibleMonthIndex=Math.floor(e),this._yearScroller.position=(e+this._originDate.getMonth())/12}_repositionMonthScroller(){this._monthScroller.position=this._yearScroller.position*12-this._originDate.getMonth(),this._visibleMonthIndex=Math.floor(this._monthScroller.position)}_onMonthScroll(){this._repositionYearScroller(),this._doIgnoreTaps()}_onYearScroll(){this._repositionMonthScroller(),this._doIgnoreTaps()}_onYearScrollTouchStart(){this._notTapping=!1,setTimeout(()=>{this._notTapping=!0},300),this._repositionMonthScroller()}_onMonthScrollTouchStart(){this._repositionYearScroller()}_doIgnoreTaps(){this._ignoreTaps=!0,this._debouncer=Y.debounce(this._debouncer,q.after(300),()=>{this._ignoreTaps=!1})}_onTodayTap(){let e=this._getTodayMidnight();Math.abs(this._monthScroller.position-this._differenceInMonths(e,this._originDate))<.001?(this._selectDate(e),this._close()):this._scrollToCurrentMonth()}_scrollToCurrentMonth(){this.focusedDate&&(this.focusedDate=new Date),this.scrollToDate(new Date,!0)}_onYearTap(e){if(!this._ignoreTaps&&!this._notTapping){let i=(e.detail.y-(this._yearScroller.getBoundingClientRect().top+this._yearScroller.clientHeight/2))/this._yearScroller.itemHeight;this._scrollToPosition(this._monthScroller.position+i*12,!0)}}_scrollToPosition(e,t){if(this._targetPosition!==void 0){this._targetPosition=e;return}if(!t){this._monthScroller.position=e,this._monthScroller.forceUpdate(),this._targetPosition=void 0,this._repositionYearScroller(),this.__tryFocusDate();return}this._targetPosition=e;let i;this._revealPromise=new Promise(u=>{i=u});let a=(u,_,g,y)=>(u/=y/2,u<1?g/2*u*u+_:(u-=1,-g/2*(u*(u-2)-1)+_)),s=0,n=this._monthScroller.position,l=u=>{s||(s=u);let _=u-s;if(_<this.scrollDuration){let g=a(_,n,this._targetPosition-n,this.scrollDuration);this._monthScroller.position=g,window.requestAnimationFrame(l)}else this.dispatchEvent(new CustomEvent("scroll-animation-finished",{bubbles:!0,composed:!0,detail:{position:this._targetPosition,oldPosition:n}})),this._monthScroller.position=this._targetPosition,this._monthScroller.forceUpdate(),this._targetPosition=void 0,i(),this._revealPromise=void 0;setTimeout(this._repositionYearScroller.bind(this),1)};window.requestAnimationFrame(l)}_toggleYearScroller(){this.toggleAttribute("years-visible")}_closeYearScroller(){this.removeAttribute("years-visible")}_yearAfterXMonths(e){return ye(e).getFullYear()}_differenceInMonths(e,t){return(e.getFullYear()-t.getFullYear())*12-t.getMonth()+e.getMonth()}_clear(){this._selectDate("")}_close(){this.dispatchEvent(new CustomEvent("close",{bubbles:!0,composed:!0}))}_cancel(){this.focusedDate=this.selectedDate,this._close()}__toggleDate(e){C(e,this.selectedDate)?(this._clear(),this.focusedDate=e):this._selectDate(e)}__onMonthCalendarKeyDown(e){let t=!1;switch(e.key){case"ArrowDown":this._moveFocusByDays(7),t=!0;break;case"ArrowUp":this._moveFocusByDays(-7),t=!0;break;case"ArrowRight":this._moveFocusByDays(this.__isRTL?-1:1),t=!0;break;case"ArrowLeft":this._moveFocusByDays(this.__isRTL?1:-1),t=!0;break;case"Enter":this._selectDate(this.focusedDate)&&(this._close(),t=!0);break;case" ":this.__toggleDate(this.focusedDate),t=!0;break;case"Home":this._moveFocusInsideMonth(this.focusedDate,"minDate"),t=!0;break;case"End":this._moveFocusInsideMonth(this.focusedDate,"maxDate"),t=!0;break;case"PageDown":this._moveFocusByMonths(e.shiftKey?12:1),t=!0;break;case"PageUp":this._moveFocusByMonths(e.shiftKey?-12:-1),t=!0;break;case"Tab":this._onTabKeyDown(e,"calendar");break;default:break}t&&(e.preventDefault(),e.stopPropagation())}_onTabKeyDown(e,t){switch(e.stopPropagation(),t){case"calendar":e.shiftKey&&(e.preventDefault(),this.hasAttribute("fullscreen")?this.focusCancel():this.__focusInput());break;case"today":e.shiftKey&&(e.preventDefault(),this.focusDateElement());break;case"cancel":e.shiftKey||(e.preventDefault(),this.hasAttribute("fullscreen")?this.focusDateElement():this.__focusInput());break;default:break}}__onTodayButtonKeyDown(e){e.key==="Tab"&&this._onTabKeyDown(e,"today")}__onCancelButtonKeyDown(e){e.key==="Tab"&&this._onTabKeyDown(e,"cancel")}__focusInput(){this.dispatchEvent(new CustomEvent("focus-input",{bubbles:!0,composed:!0}))}__tryFocusDate(){if(this.__pendingDateFocus){let t=this.focusableDateElement;t&&C(t.date,this.__pendingDateFocus)&&(delete this.__pendingDateFocus,t.focus())}}async focusDate(e,t){let i=e||this.selectedDate||this.initialPosition||new Date;this.focusedDate=i,t||(this._focusedMonthDate=i.getDate()),await this.focusDateElement(!1)}async focusDateElement(e=!0){this.__pendingDateFocus=this.focusedDate,this.calendars.length||await new Promise(t=>{requestAnimationFrame(()=>{setTimeout(()=>{t()})})}),e&&this.revealDate(this.focusedDate),this._revealPromise&&await this._revealPromise,this.__tryFocusDate()}_focusClosestDate(e){this.focusDate(be(e,[this.minDate,this.maxDate]))}_focusAllowedDate(e,t,i){this._dateAllowed(e,void 0,void 0,()=>!1)?this.focusDate(e,i):this._dateAllowed(this.focusedDate)?t>0?this.focusDate(this.maxDate):this.focusDate(this.minDate):this._focusClosestDate(this.focusedDate)}_getDateDiff(e,t){let i=new Date(0,0);return i.setFullYear(this.focusedDate.getFullYear()),i.setMonth(this.focusedDate.getMonth()+e),t&&i.setDate(this.focusedDate.getDate()+t),i}_moveFocusByDays(e){let t=this._getDateDiff(0,e);this._focusAllowedDate(t,e,!1)}_moveFocusByMonths(e){let t=this._getDateDiff(e),i=t.getMonth();this._focusedMonthDate||(this._focusedMonthDate=this.focusedDate.getDate()),t.setDate(this._focusedMonthDate),t.getMonth()!==i&&t.setDate(0),this._focusAllowedDate(t,e,!0)}_moveFocusInsideMonth(e,t){let i=new Date(0,0);i.setFullYear(e.getFullYear()),t==="minDate"?(i.setMonth(e.getMonth()),i.setDate(1)):(i.setMonth(e.getMonth()+1),i.setDate(0)),this._dateAllowed(i)?this.focusDate(i):this._dateAllowed(e)?this.focusDate(this[t]):this._focusClosestDate(e)}_dateAllowed(e,t=this.minDate,i=this.maxDate,a=this.isDateDisabled){return O(e,t,i,a)}_isTodayAllowed(e,t,i){return this._dateAllowed(this._getTodayMidnight(),e,t,i)}_getTodayMidnight(){let e=new Date,t=new Date(0,0);return t.setFullYear(e.getFullYear()),t.setMonth(e.getMonth()),t.setDate(e.getDate()),t}};var je=class extends si(f(M(p(v(c))))){static get is(){return"vaadin-date-picker-overlay-content"}static get styles(){return oi}static get lumoInjector(){return{...super.lumoInjector,includeBaseStyles:!0}}render(){return d`
      <slot name="months"></slot>
      <slot name="years"></slot>

      <div role="toolbar" part="toolbar">
        <slot name="today-button"></slot>
        <div
          part="years-toggle-button"
          ?hidden="${this._desktopMode}"
          aria-hidden="true"
          @click="${this._toggleYearScroller}"
        >
          ${this._yearAfterXMonths(this._visibleMonthIndex)}
        </div>
        <slot name="cancel-button"></slot>
      </div>
    `}firstUpdated(){super.firstUpdated(),this.setAttribute("role","dialog"),this._initControllers()}};h(je);var ni=m`
  :host([opened]) {
    pointer-events: auto;
  }

  :host([week-numbers]) {
    --_vaadin-date-picker-week-numbers-visible: 1;
  }

  :host([dir='rtl']) [part='input-field'] {
    direction: ltr;
  }

  :host([dir='rtl']) [part='input-field'] ::slotted(input)::placeholder {
    direction: rtl;
    text-align: left;
  }

  [part~='toggle-button']::before {
    mask-image: var(--_vaadin-icon-calendar);
  }

  :host([readonly]) [part~='toggle-button'] {
    display: none;
  }
`;var te=new WeakMap,Ae=new WeakMap,xe={},qe=0,li=o=>o?.nodeType===Node.ELEMENT_NODE,Ye=(...o)=>{console.error(`Error: ${o.join(" ")}. Skip setting aria-hidden.`)},Zi=(o,r)=>li(o)?r.map(e=>{if(!li(e))return Ye(e,"is not a valid element"),null;let t=e;for(;t&&t!==o;){if(o.contains(t))return e;t=t.getRootNode().host}return Ye(e,"is not contained inside",o),null}).filter(e=>!!e):(Ye(o,"is not a valid element"),[]),er=(o,r,e,t)=>{let i=Zi(r,Array.isArray(o)?o:[o]);xe[e]||(xe[e]=new WeakMap);let a=xe[e],s=[],n=new Set,l=new Set(i),u=g=>{if(!g||n.has(g))return;n.add(g);let y=g.assignedSlot;y&&u(y),u(g.parentNode||g.host)};i.forEach(u);let _=g=>{if(!g||l.has(g))return;let y=g.shadowRoot;(y?[...g.children,...y.children]:[...g.children]).forEach(w=>{if(!["template","script","style"].includes(w.localName))if(n.has(w))_(w);else{let L=w.getAttribute(t),ae=L!==null&&L!=="false",ft=(te.get(w)||0)+1,vt=(a.get(w)||0)+1;te.set(w,ft),a.set(w,vt),s.push(w),ft===1&&ae&&Ae.set(w,!0),vt===1&&w.setAttribute(e,"true"),ae||w.setAttribute(t,"true")}})};return _(r),n.clear(),qe+=1,()=>{s.forEach(g=>{let y=te.get(g)-1,x=a.get(g)-1;te.set(g,y),a.set(g,x),y||(Ae.has(g)?Ae.delete(g):g.removeAttribute(t)),x||g.removeAttribute(e)}),qe-=1,qe||(te=new WeakMap,te=new WeakMap,Ae=new WeakMap,xe={})}},di=(o,r=document.body,e="data-aria-hidden")=>{let t=Array.from(Array.isArray(o)?o:[o]);return r&&t.push(...Array.from(r.querySelectorAll("[aria-live]"))),er(t,r,e,"aria-hidden")};var Yo="inert"in HTMLElement.prototype;var we=class{constructor(r){this.host=r,r.addEventListener("opened-changed",()=>{r.opened||this.__setVirtualKeyboardEnabled(!1)}),r.addEventListener("blur",()=>this.__setVirtualKeyboardEnabled(!0)),r.addEventListener("touchstart",()=>this.__setVirtualKeyboardEnabled(!0))}__setVirtualKeyboardEnabled(r){this.host.inputElement&&(this.host.inputElement.inputMode=r?"":"none")}};var Ce=Object.freeze({monthNames:["January","February","March","April","May","June","July","August","September","October","November","December"],weekdays:["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"],weekdaysShort:["Sun","Mon","Tue","Wed","Thu","Fri","Sat"],firstDayOfWeek:0,today:"Today",cancel:"Cancel",referenceDate:"",formatDate(o){let r=String(o.year).replace(/\d+/u,e=>"0000".substr(e.length)+e);return[o.month+1,o.day,r].join("/")},parseDate(o){let r=o.split("/"),e=new Date,t,i=e.getMonth(),a=e.getFullYear();if(r.length===3){if(i=parseInt(r[0])-1,t=parseInt(r[1]),a=parseInt(r[2]),r[2].length<3&&a>=0){let s=this.referenceDate?Z(this.referenceDate):new Date;a=Kt(s,a,i,t)}}else r.length===2?(i=parseInt(r[0])-1,t=parseInt(r[1])):r.length===1&&(t=parseInt(r[0]));if(t!==void 0)return{day:t,month:i,year:a}},formatTitle:(o,r)=>`${o} ${r}`}),hi=o=>class extends $(Q(Pt(U(o)))){static get properties(){return{_selectedDate:{type:Object,sync:!0},_focusedDate:{type:Object,sync:!0},value:{type:String,notify:!0,value:"",sync:!0},initialPosition:{type:String},opened:{type:Boolean,reflectToAttribute:!0,notify:!0,observer:"_openedChanged",sync:!0},autoOpenDisabled:{type:Boolean,sync:!0},showWeekNumbers:{type:Boolean,value:!1,sync:!0},_fullscreen:{type:Boolean,value:!1,sync:!0},_fullscreenMediaQuery:{value:"(max-width: 450px), (max-height: 450px)"},min:{type:String,sync:!0},max:{type:String,sync:!0},isDateDisabled:{type:Function},_minDate:{type:Date,computed:"__computeMinOrMaxDate(min)"},_maxDate:{type:Date,computed:"__computeMinOrMaxDate(max)"},_noInput:{type:Boolean,computed:"_isNoInput(inputElement, _fullscreen, _ios, __effectiveI18n, opened, autoOpenDisabled)"},_ios:{type:Boolean,value:xt},_focusOverlayOnOpen:Boolean,_overlayContent:{type:Object,sync:!0},__enteredDate:{type:Date,sync:!0}}}static get observers(){return["_selectedDateChanged(_selectedDate, __effectiveI18n)","_focusedDateChanged(_focusedDate, __effectiveI18n)","__updateOverlayContent(_overlayContent, __effectiveI18n, label, _minDate, _maxDate, _focusedDate, _selectedDate, showWeekNumbers, isDateDisabled, __enteredDate)","__updateOverlayContentTheme(_overlayContent, _theme)","__updateOverlayContentFullScreen(_overlayContent, _fullscreen)"]}static get defaultI18n(){return Ce}static get constraints(){return[...super.constraints,"min","max"]}constructor(){super(),this._boundOnClick=this._onClick.bind(this),this._boundOnScroll=this._onScroll.bind(this)}get i18n(){return super.i18n}set i18n(e){super.i18n=e}get _inputElementValue(){return super._inputElementValue}set _inputElementValue(e){super._inputElementValue=e;let t=this.__parseDate(e);this.__setEnteredDate(t)}get __unparsableValue(){return!this._inputElementValue||this.__parseDate(this._inputElementValue)?"":this._inputElementValue}_onFocus(e){super._onFocus(e),this._noInput&&!z()&&e.target.blur()}_onBlur(e){super._onBlur(e),this.opened||(this.__commitParsedOrFocusedDate(),document.hasFocus()&&this._requestValidation())}ready(){super.ready(),this.addEventListener("click",this._boundOnClick),this.addController(new N(this._fullscreenMediaQuery,e=>{this._fullscreen=e})),this.addController(new we(this)),this._overlayElement=this.$.overlay}updated(e){super.updated(e),(e.has("showWeekNumbers")||e.has("__effectiveI18n"))&&this.toggleAttribute("week-numbers",this.showWeekNumbers&&this.__effectiveI18n.firstDayOfWeek===1)}disconnectedCallback(){super.disconnectedCallback(),this.opened=!1}focus(e){this._noInput&&!z()?this.open():super.focus(e)}open(){!this.disabled&&!this.readonly&&(this.opened=!0)}close(){this.$.overlay.close()}__ensureContent(){if(this._overlayContent)return;let e=document.createElement("vaadin-date-picker-overlay-content");e.setAttribute("slot","overlay"),this.appendChild(e),this._overlayContent=e,e.addEventListener("close",()=>{this._close()}),e.addEventListener("focus-input",this._focusAndSelect.bind(this)),e.addEventListener("date-tap",t=>{this.__commitDate(t.detail.date),this._close()}),e.addEventListener("date-selected",t=>{this.__commitDate(t.detail.date)}),e.addEventListener("focusin",()=>{this._keyboardActive&&this._setFocused(!0)}),e.addEventListener("focusout",t=>{this._shouldRemoveFocus(t)&&this._setFocused(!1)}),e.addEventListener("focused-date-changed",t=>{this._focusedDate=t.detail.value}),e.addEventListener("click",t=>t.stopPropagation())}__parseDate(e){if(!this.__effectiveI18n.parseDate)return;let t=this.__effectiveI18n.parseDate(e);if(t&&(t=Z(`${t.year}-${t.month+1}-${t.day}`)),t&&!isNaN(t.getTime()))return t}__formatDate(e){if(this.__effectiveI18n.formatDate)return this.__effectiveI18n.formatDate(ze(e))}checkValidity(){let e=this._inputElementValue,t=!e||!!this._selectedDate&&e===this.__formatDate(this._selectedDate),i=!this._selectedDate||O(this._selectedDate,this._minDate,this._maxDate,this.isDateDisabled),a=!0;return this.inputElement&&this.inputElement.checkValidity&&(a=this.inputElement.checkValidity()),t&&i&&a}_shouldSetFocus(e){return!this._shouldKeepFocusRing}_shouldKeepFocusOnClearMousedown(){return this.opened?!0:super._shouldKeepFocusOnClearMousedown()}_shouldRemoveFocus(e){let{relatedTarget:t}=e;return this.opened&&t!==null&&t!==document.body&&!this.contains(t)&&!this._overlayContent.contains(t)?!0:!this.opened}_setFocused(e){super._setFocused(e),this._shouldKeepFocusRing=e&&this._keyboardActive}__commitValueChange(){let e=this.__unparsableValue;this.__committedValue!==this.value?(this._requestValidation(),this.dispatchEvent(new CustomEvent("change",{bubbles:!0}))):this.__committedUnparsableValue!==e&&(this._requestValidation(),this.dispatchEvent(new CustomEvent("unparsable-change"))),this.__committedValue=this.value,this.__committedUnparsableValue=e}__commitDate(e){this.__keepCommittedValue=!0,this._selectedDate=e,this.__keepCommittedValue=!1,this.__commitValueChange()}_close(){this._focus(),this.close()}_isNoInput(e,t,i,a,s,n){return!e||t&&(!n||s)||i&&s||!a.parseDate}_formatISO(e){return Xt(e)}_inputElementChanged(e){super._inputElementChanged(e),e&&(e.autocomplete="off",e.setAttribute("role","combobox"),e.setAttribute("aria-haspopup","dialog"),e.setAttribute("aria-expanded",!!this.opened),this._applyInputValue(this._selectedDate))}_openedChanged(e){e&&this.__ensureContent(),this.inputElement&&this.inputElement.setAttribute("aria-expanded",e)}_selectedDateChanged(e,t){e===void 0||t===void 0||(this.__keepInputValue||this._applyInputValue(e),this.value=this._formatISO(e),this._ignoreFocusedDateChange=!0,this._focusedDate=e,this._ignoreFocusedDateChange=!1)}_focusedDateChanged(e,t){e===void 0||t===void 0||!this._ignoreFocusedDateChange&&!this._noInput&&this._applyInputValue(e)}_valueChanged(e,t){let i=Z(e);if(e&&!i){this.value=t;return}e?C(this._selectedDate,i)||(this._selectedDate=i,t!==void 0&&this._requestValidation()):this._selectedDate=null,this.__keepCommittedValue||(this.__committedValue=this.value,this.__committedUnparsableValue=""),this._toggleHasValue(this._hasValue)}__updateOverlayContent(e,t,i,a,s,n,l,u,_,g){e&&(e.i18n=t,e.label=i,e.minDate=a,e.maxDate=s,e.focusedDate=n,e.selectedDate=l,e.showWeekNumbers=u,e.isDateDisabled=_,e.enteredDate=g)}__updateOverlayContentTheme(e,t){e&&(t?e.setAttribute("theme",t):e.removeAttribute("theme"))}__updateOverlayContentFullScreen(e,t){e&&e.toggleAttribute("fullscreen",t)}_onOverlayEscapePress(e){e.stopPropagation(),this._focusedDate=this._selectedDate,this._applyInputValue(this._selectedDate),this._close()}_onOverlayOpened(){let e=this._overlayContent;e.reset();let t=this._getInitialPosition();e.initialPosition=t;let i=e.focusedDate||t;e.scrollToDate(i),this._ignoreFocusedDateChange=!0,e.focusedDate=i,this._ignoreFocusedDateChange=!1,window.addEventListener("scroll",this._boundOnScroll,!0),this._focusOverlayOnOpen?(e.focusDateElement(),this._focusOverlayOnOpen=!1):this._focus();let a=this.inputElement;this._noInput&&a&&(a.blur(),this._overlayContent.focusDateElement());let s=this._noInput?e:this;this.__showOthers=di(s)}_getInitialPosition(){let e=Z(this.initialPosition),t=this._selectedDate||this._overlayContent.initialPosition||e||new Date;return e||O(t,this._minDate,this._maxDate,this.isDateDisabled)?t:this._minDate||this._maxDate?be(t,[this._minDate,this._maxDate]):new Date}__commitParsedOrFocusedDate(){if(this._ignoreFocusedDateChange=!0,this.__effectiveI18n.parseDate){let e=this._inputElementValue||"",t=this.__parseDate(e);t?this.__commitDate(t):(this.__keepInputValue=!0,this.__commitDate(null),this.__keepInputValue=!1)}else this._focusedDate&&this.__commitDate(this._focusedDate);this._ignoreFocusedDateChange=!1}_onOverlayClosed(){this.__showOthers&&(this.__showOthers(),this.__showOthers=null),window.removeEventListener("scroll",this._boundOnScroll,!0),this.__commitParsedOrFocusedDate(),this.inputElement&&this.inputElement.selectionStart&&(this.inputElement.selectionStart=this.inputElement.selectionEnd),!this.value&&!this._keyboardActive&&this._requestValidation()}_onScroll(e){(e.target===window||!this._overlayContent.contains(e.target))&&this._overlayContent._repositionYearScroller()}_focus(){this._noInput||this.inputElement.focus()}_focusAndSelect(){this._focus(),this._setSelectionRange(0,this._inputElementValue.length)}_applyInputValue(e){this._inputElementValue=e?this.__formatDate(e):""}_setSelectionRange(e,t){this.inputElement&&this.inputElement.setSelectionRange(e,t)}_onChange(e){e.stopPropagation()}_onClick(e){e.composedPath().includes(this._overlayElement)||this._isClearButton(e)||this._onHostClick(e)}_onHostClick(e){(!this.autoOpenDisabled||this._noInput)&&(e.preventDefault(),this.open())}_onClearButtonClick(e){e.preventDefault(),this.__commitDate(null)}_onKeyDown(e){switch(super._onKeyDown(e),this._noInput&&["Tab","Escape"].indexOf(e.key)===-1&&e.preventDefault(),e.key){case"ArrowDown":case"ArrowUp":e.preventDefault(),this.opened?this._overlayContent.focusDateElement():(this._focusOverlayOnOpen=!0,this.open());break;case"Tab":this.opened&&(e.preventDefault(),e.stopPropagation(),this._setSelectionRange(0,0),e.shiftKey?this._overlayContent.focusCancel():this._overlayContent.focusDateElement());break;default:break}}_onEnter(e){e.composedPath().includes(this._overlayContent)||(this.opened?this.close():this.__commitParsedOrFocusedDate())}_onEscape(e){if(this.opened){this._onOverlayEscapePress(e);return}if(this.clearButtonVisible&&this.value&&!this.readonly){e.stopPropagation(),this._onClearButtonClick(e);return}this.inputElement.value===""?this.__commitDate(null):this._applyInputValue(this._selectedDate)}_isClearButton(e){return e.composedPath()[0]===this.clearElement}_onInput(){!this.opened&&this._inputElementValue&&!this.autoOpenDisabled&&this.open();let e=this.__parseDate(this._inputElementValue||"");e&&(this._ignoreFocusedDateChange=!0,C(e,this._focusedDate)||(this._focusedDate=e),this._ignoreFocusedDateChange=!1),this.__setEnteredDate(e)}__setEnteredDate(e){e?C(this.__enteredDate,e)||(this.__enteredDate=e):this.__enteredDate=null}__computeMinOrMaxDate(e){return Z(e)}};var We=class extends hi(X(f(A(p(v(c)))))){static get is(){return"vaadin-date-picker"}static get styles(){return[S,ni]}static get properties(){return{_positionTarget:{type:Object,sync:!0}}}get clearElement(){return this.$.clearButton}render(){return d`
      <div class="vaadin-date-picker-container">
        <div part="label">
          <slot name="label"></slot>
          <span part="required-indicator" aria-hidden="true" @click="${this.focus}"></span>
        </div>

        <vaadin-input-container
          part="input-field"
          .readonly="${this.readonly}"
          .disabled="${this.disabled}"
          .invalid="${this.invalid}"
          theme="${D(this._theme)}"
        >
          <slot name="prefix" slot="prefix"></slot>
          <slot name="input"></slot>
          <div id="clearButton" part="field-button clear-button" slot="suffix" aria-hidden="true"></div>
          <div part="field-button toggle-button" slot="suffix" aria-hidden="true" @click="${this._toggle}"></div>
        </vaadin-input-container>

        <div part="helper-text">
          <slot name="helper"></slot>
        </div>

        <div part="error-message">
          <slot name="error-message"></slot>
        </div>

        <slot name="tooltip"></slot>
      </div>

      <vaadin-date-picker-overlay
        id="overlay"
        .owner="${this}"
        ?fullscreen="${this._fullscreen}"
        theme="${D(this._theme)}"
        .opened="${this.opened}"
        @opened-changed="${this._onOpenedChanged}"
        @vaadin-overlay-open="${this._onOverlayOpened}"
        @vaadin-overlay-close="${this._onVaadinOverlayClose}"
        @vaadin-overlay-closing="${this._onOverlayClosed}"
        restore-focus-on-close
        no-vertical-overlap
        exportparts="backdrop, overlay, content"
        .restoreFocusNode="${this.inputElement}"
        .positionTarget="${this._positionTarget}"
      >
        <slot name="overlay"></slot>
      </vaadin-date-picker-overlay>
    `}ready(){super.ready(),this.addController(new H(this,e=>{this._setInputElement(e),this._setFocusElement(e),this.stateTarget=e,this.ariaTarget=e},{uniqueIdPrefix:"search-input"})),this.addController(new B(this.inputElement,this._labelController)),this._tooltipController=new E(this),this.addController(this._tooltipController),this._tooltipController.setPosition("top"),this._tooltipController.setAriaTarget(this.inputElement),this._tooltipController.setShouldShow(e=>!e.opened),this._positionTarget=this.shadowRoot.querySelector('[part="input-field"]'),this.shadowRoot.querySelector('[part="field-button toggle-button"]').addEventListener("mousedown",e=>e.preventDefault())}_onOpenedChanged(r){this.opened=r.detail.value}_onVaadinOverlayClose(r){let e=r.detail.sourceEvent;e?.composedPath().includes(this)&&!e.composedPath().includes(this._overlayElement)&&r.preventDefault()}_toggle(r){r.stopPropagation(),this.$.overlay.opened?this.close():this.open()}};h(We);var Ke=class extends At(f(M(p(v(c))))){static get is(){return"vaadin-time-picker-item"}static get styles(){return[he,yt]}render(){return d`
      <span part="checkmark" aria-hidden="true"></span>
      <div part="content">
        <slot></slot>
      </div>
    `}};h(Ke);var ui=m`
  :host {
    --vaadin-item-checkmark-display: block;
  }

  #overlay {
    width: var(--vaadin-time-picker-overlay-width, var(--_vaadin-time-picker-overlay-default-width, auto));
  }

  [part='content'] {
    display: flex;
    flex-direction: column;
    height: 100%;
  }
`;var Qe=class extends Mt(R(M(f(p(v(c)))))){static get is(){return"vaadin-time-picker-overlay"}static get styles(){return[V,ui]}render(){return d`
      <div part="overlay" id="overlay">
        <div part="content" id="content">
          <slot></slot>
        </div>
      </div>
    `}};h(Qe);var Ge=class extends Tt(p(c)){static get is(){return"vaadin-time-picker-scroller"}static get styles(){return Pe}render(){return d`
      <div id="selector">
        <slot></slot>
      </div>
    `}};h(Ge);var ci=m`
  :host([opened]) {
    pointer-events: auto;
  }

  [part~='toggle-button']::before {
    mask-image: var(--_vaadin-icon-clock);
  }

  :host([readonly]) [part~='toggle-button'] {
    display: none;
  }

  /* See https://github.com/vaadin/vaadin-time-picker/issues/145 */
  :host([dir='rtl']) [part='input-field'] {
    direction: ltr;
  }

  :host([dir='rtl']) [part='input-field'] ::slotted(input)::placeholder {
    direction: rtl;
    text-align: left;
  }
`;function ie(o){if(!o)return"";let r=(t=0,i="00")=>(i+t).substr((i+t).length-i.length),e=`${r(o.hours)}:${r(o.minutes)}`;return o.seconds!==void 0&&(e+=`:${r(o.seconds)}`),o.milliseconds!==void 0&&(e+=`.${r(o.milliseconds,"000")}`),e}var tr="(\\d|[0-1]\\d|2[0-3])",pi="(\\d|[0-5]\\d)",ir=pi,rr="(\\d{1,3})",ar=new RegExp(`^${tr}(?::${pi}(?::${ir}(?:\\.${rr})?)?)?$`,"u");function I(o){let r=ar.exec(o);if(r){if(r[4])for(;r[4].length<3;)r[4]+="0";return{hours:r[1],minutes:r[2],seconds:r[3],milliseconds:r[4]}}}function or(o){let r=o==null?60:parseFloat(o);if(r%3600===0)return 1;if(r%60===0||!r)return 2;if(r%1===0)return 3;if(r<1)return 4}function P(o,r){if(!o)return o;let e=or(r);return{...o,hours:parseInt(o.hours),minutes:parseInt(o.minutes||0),seconds:e<3?void 0:parseInt(o.seconds||0),milliseconds:e<4?void 0:parseInt(o.milliseconds||0)}}var De=Object.freeze({formatTime:ie,parseTime:I}),mi="00:00:00.000",_i="23:59:59.999",fi=o=>class extends $(Lt(Vt(X(o)))){static get properties(){return{value:{type:String,notify:!0,value:"",sync:!0},min:{type:String,value:"",sync:!0},max:{type:String,value:"",sync:!0},step:{type:Number,sync:!0},_comboBoxValue:{type:String,sync:!0,observer:"__comboBoxValueChanged"},_inputContainer:{type:Object}}}static get observers(){return["_openedOrItemsChanged(opened, _dropdownItems)","_updateScroller(opened, _dropdownItems, _focusedIndex, _theme, _comboBoxValue)","__updateAriaAttributes(_dropdownItems, opened, inputElement)","__updateDropdownItems(__effectiveI18n, min, max, step)"]}static get defaultI18n(){return De}static get constraints(){return[...super.constraints,"min","max"]}get _tagNamePrefix(){return"vaadin-time-picker"}get clearElement(){return this.$.clearButton}get i18n(){return super.i18n}set i18n(e){super.i18n=e}get __unparsableValue(){return this._inputElementValue&&!this.__effectiveI18n.parseTime(this._inputElementValue)?this._inputElementValue:""}ready(){super.ready(),this.addController(new H(this,e=>{this._setInputElement(e),this._setFocusElement(e),this.stateTarget=e,this.ariaTarget=e},{uniqueIdPrefix:"search-input"})),this.addController(new B(this.inputElement,this._labelController)),this._inputContainer=this.shadowRoot.querySelector('[part~="input-field"]'),this._toggleElement=this.$.toggleButton,this._tooltipController=new E(this),this._tooltipController.setShouldShow(e=>!e.opened),this._tooltipController.setPosition("top"),this._tooltipController.setAriaTarget(this.inputElement),this.addController(this._tooltipController)}checkValidity(){return!!(this.inputElement.checkValidity()&&(!this.value||this._timeAllowed(I(this.value)))&&(!this._comboBoxValue||this.__effectiveI18n.parseTime(this._comboBoxValue)))}_getItemLabel(e){return e?e.label:""}_updateScroller(e,t,i,a,s){e&&(this._scroller.style.maxHeight=getComputedStyle(this).getPropertyValue(`--${this._tagNamePrefix}-overlay-max-height`)||"65vh"),this._scroller.setProperties({items:e?t:[],opened:e,focusedIndex:i,theme:a,selectedItem:t?.find(n=>n.value===s)})}_openedOrItemsChanged(e,t){this._overlayOpened=e&&!!t?.length}_onClosed(){this._commitValue()}_onEscapeCancel(){this._inputElementValue=this._comboBoxValue,this._closeOrCommit()}_onClearAction(){this._comboBoxValue="",this._inputElementValue="",this.__commitValueChange()}_commitValue(){if(this._focusedIndex>-1){let e=this._dropdownItems[this._focusedIndex],t=this._getItemLabel(e);this._inputElementValue=t,this._comboBoxValue=t,this._focusedIndex=-1}else this._inputElementValue===""||this._inputElementValue===void 0?this._comboBoxValue="":this._comboBoxValue=this._inputElementValue;this.__commitValueChange(),this._clearSelectionRange()}_closeOrCommit(){this.opened?this.close():this._commitValue()}_revertInputValue(){this._inputElementValue=this._comboBoxValue,this._clearSelectionRange()}_setFocused(e){super._setFocused(e),!e&&!this._closeOnBlurIsPrevented&&document.hasFocus()&&this._requestValidation()}__validDayDivisor(e){return!e||24*3600%e===0||e<1&&e%1*1e3%1===0}_onKeyDown(e){if(super._onKeyDown(e),this.readonly||this.disabled||this._dropdownItems.length)return;let t=this.__validDayDivisor(this.step)&&this.step||60;e.keyCode===40?this.__onArrowPressWithStep(-t):e.keyCode===38&&this.__onArrowPressWithStep(t)}__onArrowPressWithStep(e){let t=this.__addStep(this.__getMsec(this.__memoValue),e,!0);this.__memoValue=t,this.__useMemo=!0,this._comboBoxValue=this.__effectiveI18n.formatTime(t),this.__useMemo=!1,this.__commitValueChange()}__commitValueChange(){let e=this.__unparsableValue;this.__committedValue!==this.value?(this._requestValidation(),this.dispatchEvent(new CustomEvent("change",{bubbles:!0}))):this.__committedUnparsableValue!==e&&(this._requestValidation(),this.dispatchEvent(new CustomEvent("unparsable-change"))),this.__committedValue=this.value,this.__committedUnparsableValue=e}__getMsec(e){let t=(e?.hours||0)*60*60*1e3;return t+=(e?.minutes||0)*60*1e3,t+=(e?.seconds||0)*1e3,t+=parseInt(e?.milliseconds)||0,t}__getSec(e){let t=(e?.hours||0)*60*60;return t+=(e?.minutes||0)*60,t+=e?.seconds||0,t+=(e?.milliseconds||0)/1e3,t}__addStep(e,t,i){e===0&&t<0&&(e=1440*60*1e3);let a=t*1e3,s=e%a;a<0&&s&&i?e-=s:a>0&&s&&i?e-=s-a:e+=a;let n=Math.floor(e/1e3/60/60);e-=n*1e3*60*60;let l=Math.floor(e/1e3/60);e-=l*1e3*60;let u=Math.floor(e/1e3);return e-=u*1e3,{hours:n<24?n:0,minutes:l,seconds:u,milliseconds:e}}__updateDropdownItems(e,t,i,a){let s=P(I(t||mi),a),n=this.__getSec(s),l=P(I(i||_i),a),u=this.__getSec(l);this._dropdownItems=this.__generateDropdownList(n,u,a);let _=P(I(this.value),a);a!==this.__oldStep&&(this.__oldStep=a,this.__updateValue(_)),this.value&&(this._comboBoxValue=e.formatTime(_))}__updateAriaAttributes(e,t,i){e===void 0||i===void 0||(e.length===0?(i.removeAttribute("role"),i.removeAttribute("aria-expanded")):(i.setAttribute("role","combobox"),i.setAttribute("aria-expanded",!!t)))}__generateDropdownList(e,t,i){if(i<900||!this.__validDayDivisor(i))return[];let a=[];i||(i=3600);let s=-i+e;for(;s+i>=e&&s+i<=t;){let n=P(this.__addStep(s*1e3,i),i);s+=i;let l=this.__effectiveI18n.formatTime(n);a.push({label:l,value:l})}return a}_valueChanged(e,t){let i=this.__memoValue=I(e),a=ie(i)||"";this.__keepCommittedValue||(this.__committedValue=e,this.__committedUnparsableValue=""),e!==""&&e!==null&&!i?this.value=t??"":e!==a?this.value=a:this.__keepInvalidInput?delete this.__keepInvalidInput:this.__updateInputValue(i),this._toggleHasValue(this._hasValue)}__comboBoxValueChanged(e,t){if(e===""&&t===void 0)return;let i=this.__useMemo?this.__memoValue:this.__effectiveI18n.parseTime(e),a=P(i,this.step),s=this.__effectiveI18n.formatTime(a)||"";a?e!==s?this._comboBoxValue=s:(this.__keepCommittedValue=!0,this.__updateValue(a),this.__keepCommittedValue=!1):(this.value!==""&&e!==""&&(this.__keepInvalidInput=!0),this.__keepCommittedValue=!0,this.value="",this.__keepCommittedValue=!1)}__updateValue(e){let t=ie(P(e,this.step))||"";this.value=t,this.__updateInputValue(e)}__updateInputValue(e){let t=this.__effectiveI18n.formatTime(P(e,this.step))||"";this._inputElementValue=t,this._comboBoxValue=t}_timeAllowed(e){let t=I(this.min||mi),i=I(this.max||_i);return(!t||this.__getMsec(e)>=this.__getMsec(t))&&(!i||this.__getMsec(e)<=this.__getMsec(i))}_onClearButtonClick(e){e.stopPropagation(),super._onClearButtonClick(e),this.opened&&this._scroller.requestContentUpdate()}_onHostClick(e){let t=e.composedPath();(t.includes(this._labelNode)||t.includes(this._inputContainer))&&super._onHostClick(e)}_onChange(e){e.stopPropagation()}};var Xe=class extends fi(f(A(p(v(c))))){static get is(){return"vaadin-time-picker"}static get styles(){return[S,ci]}render(){return d`
      <div class="vaadin-time-picker-container">
        <div part="label">
          <slot name="label"></slot>
          <span part="required-indicator" aria-hidden="true" @click="${this.focus}"></span>
        </div>

        <vaadin-input-container
          part="input-field"
          .readonly="${this.readonly}"
          .disabled="${this.disabled}"
          .invalid="${this.invalid}"
          theme="${D(this._theme)}"
        >
          <slot name="prefix" slot="prefix"></slot>
          <slot name="input"></slot>
          <div id="clearButton" part="field-button clear-button" slot="suffix" aria-hidden="true"></div>
          <div id="toggleButton" part="field-button toggle-button" slot="suffix" aria-hidden="true"></div>
        </vaadin-input-container>

        <div part="helper-text">
          <slot name="helper"></slot>
        </div>

        <div part="error-message">
          <slot name="error-message"></slot>
        </div>

        <slot name="tooltip"></slot>
      </div>

      <vaadin-time-picker-overlay
        id="overlay"
        dir="ltr"
        .owner="${this}"
        .opened="${this._overlayOpened}"
        theme="${D(this._theme)}"
        .positionTarget="${this._inputContainer}"
        no-vertical-overlap
        exportparts="overlay, content"
      >
        <slot name="overlay"></slot>
      </vaadin-time-picker-overlay>
    `}};h(Xe);var vi=m`
  .vaadin-date-time-picker-container {
    width: calc(var(--vaadin-field-default-width, 12em) * 2 + var(--vaadin-date-time-picker-gap, var(--vaadin-gap-s)));
  }

  [part='input-fields'] {
    display: flex;
    gap: var(--vaadin-date-time-picker-gap, var(--vaadin-gap-s));
  }

  [part='input-fields'] ::slotted([slot='date-picker']) {
    min-width: 0;
    flex: 1 1 auto;
  }

  [part='input-fields'] ::slotted([slot='time-picker']) {
    min-width: 0;
    flex: 1 1.65 auto;
  }
`;var sr=Object.keys(Ce),nr=Object.keys(De),lr={...Ce,...De},ke=class extends k{constructor(r,e){super(r,`${e}-picker`,`vaadin-${e}-picker`,{initializer:(t,i)=>{let a=`__${e}Picker`;i[a]=t}})}},gi=o=>class extends $(G(F(K(o)))){static get properties(){return{name:{type:String},value:{type:String,notify:!0,value:"",observer:"__valueChanged",sync:!0},min:{type:String,observer:"__minChanged",sync:!0},max:{type:String,observer:"__maxChanged",sync:!0},__minDateTime:{type:Date,value:"",sync:!0},__maxDateTime:{type:Date,value:"",sync:!0},datePlaceholder:{type:String,sync:!0},timePlaceholder:{type:String,sync:!0},step:{type:Number,sync:!0},initialPosition:{type:String,sync:!0},showWeekNumbers:{type:Boolean,value:!1,sync:!0},autoOpenDisabled:{type:Boolean,sync:!0},readonly:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},autofocus:{type:Boolean},__selectedDateTime:{type:Date,sync:!0},__datePicker:{type:Object,sync:!0,observer:"__datePickerChanged"},__timePicker:{type:Object,sync:!0,observer:"__timePickerChanged"}}}static get observers(){return["__selectedDateTimeChanged(__selectedDateTime)","__datePlaceholderChanged(datePlaceholder, __datePicker)","__timePlaceholderChanged(timePlaceholder, __timePicker)","__stepChanged(step, __timePicker)","__initialPositionChanged(initialPosition, __datePicker)","__showWeekNumbersChanged(showWeekNumbers, __datePicker)","__requiredChanged(required, __datePicker, __timePicker)","__invalidChanged(invalid, __datePicker, __timePicker)","__disabledChanged(disabled, __datePicker, __timePicker)","__readonlyChanged(readonly, __datePicker, __timePicker)","__i18nChanged(__effectiveI18n, __datePicker, __timePicker)","__autoOpenDisabledChanged(autoOpenDisabled, __datePicker, __timePicker)","__themeChanged(_theme, __datePicker, __timePicker)","__pickersChanged(__datePicker, __timePicker)","__labelOrAccessibleNameChanged(label, accessibleName, __effectiveI18n, __datePicker, __timePicker)"]}static get defaultI18n(){return lr}constructor(){super(),this.__defaultDateMinMaxValue=void 0,this.__defaultTimeMinValue="00:00:00.000",this.__defaultTimeMaxValue="23:59:59.999",this.__onGlobalClick=this.__onGlobalClick.bind(this),this.__changeEventHandler=this.__changeEventHandler.bind(this),this.__valueChangedEventHandler=this.__valueChangedEventHandler.bind(this),this.__openedChangedEventHandler=this.__openedChangedEventHandler.bind(this)}get i18n(){return super.i18n}set i18n(e){super.i18n=e}get __pickers(){return[this.__datePicker,this.__timePicker]}get __filledPickers(){return this.__pickers.filter(e=>e.value||e.__unparsableValue)}get __formattedValue(){let e=this.__pickers.map(t=>t.value);return e.every(Boolean)?e.join("T"):""}get __unparsableValue(){return this.__filledPickers.length>0&&!this.__pickers.every(e=>e.value)?this.__pickers.map(e=>e.value||e.__unparsableValue).join("T"):""}ready(){super.ready(),this._datePickerController=new ke(this,"date"),this.addController(this._datePickerController),this._timePickerController=new ke(this,"time"),this.addController(this._timePickerController),this.autofocus&&!this.disabled&&window.requestAnimationFrame(()=>this.focus()),this.setAttribute("role","group"),this._tooltipController=new E(this),this.addController(this._tooltipController),this._tooltipController.setPosition("top"),this._tooltipController.setShouldShow(e=>e.__datePicker&&!e.__datePicker.opened&&e.__timePicker&&!e.__timePicker.opened),this.ariaTarget=this}connectedCallback(){super.connectedCallback(),document.addEventListener("click",this.__onGlobalClick,!0)}disconnectedCallback(){super.disconnectedCallback(),document.removeEventListener("click",this.__onGlobalClick,!0)}focus(e){this.__datePicker&&this.__datePicker.focus(e)}__onGlobalClick(e){if(!(this.__datePicker.opened||this.__timePicker.opened))return;e.composedPath().every(a=>![this.__datePicker,this.__datePicker.$.overlay,this.__timePicker,this.__timePicker.$.overlay].includes(a))&&(this.__outsideClickInProgress=!0,setTimeout(()=>{this.__outsideClickInProgress=!1}))}_setFocused(e){super._setFocused(e),!e&&document.hasFocus()&&this.__commitPendingValueChange()}_shouldRemoveFocus(e){let t=e.relatedTarget;return!(this.__datePicker.opened||this.__timePicker.opened||this.__datePicker.contains(t)||this.__timePicker.contains(t))}__syncI18n(e,t,i){let a={};i.forEach(s=>{t?.hasOwnProperty(s)&&(a[s]=t[s])}),e.i18n=a}__changeEventHandler(e){e.stopPropagation();let t=this.invalid,i=this.__filledPickers;i.length===1&&i[0].checkValidity()&&!t||this.__hasPendingValueChange&&this.__commitPendingValueChange()}__openedChangedEventHandler(){let e=this.__datePicker.opened||this.__timePicker.opened;this.style.pointerEvents=e?"auto":"",!e&&this.__outsideClickInProgress&&this.__commitPendingValueChange()}__addInputListeners(e){e.addEventListener("change",this.__changeEventHandler),e.addEventListener("unparsable-change",this.__changeEventHandler),e.addEventListener("value-changed",this.__valueChangedEventHandler),e.addEventListener("opened-changed",this.__openedChangedEventHandler)}__removeInputListeners(e){e.removeEventListener("change",this.__changeEventHandler),e.removeEventListener("unparsable-change",this.__changeEventHandler),e.removeEventListener("value-changed",this.__valueChangedEventHandler),e.removeEventListener("opened-changed",this.__openedChangedEventHandler)}__isDefaultPicker(e,t){let i=this[`_${t}PickerController`];return i&&e===i.defaultNode}__datePickerChanged(e,t){e&&(t&&(this.__removeInputListeners(t),t.remove()),this.__addInputListeners(e),this.__isDefaultPicker(e,"date")||(this.datePlaceholder=e.placeholder,this.initialPosition=e.initialPosition,this.showWeekNumbers=e.showWeekNumbers),e.min=this.__formatDateISO(this.__minDateTime,this.__defaultDateMinMaxValue),e.max=this.__formatDateISO(this.__maxDateTime,this.__defaultDateMinMaxValue),e.manualValidation=!0)}__timePickerChanged(e,t){e&&(t&&(this.__removeInputListeners(t),t.remove()),this.__addInputListeners(e),this.__isDefaultPicker(e,"time")||(this.timePlaceholder=e.placeholder,this.step=e.step),this.__updateTimePickerMinMax(),e.manualValidation=!0)}__updateTimePickerMinMax(){if(this.__timePicker&&this.__datePicker){let e=this.__parseDate(this.__datePicker.value),t=C(this.__minDateTime,this.__maxDateTime,de);this.__minDateTime&&C(e,this.__minDateTime,de)||t?this.__timePicker.min=this.__dateToIsoTimeString(this.__minDateTime):this.__timePicker.min=this.__defaultTimeMinValue,this.__maxDateTime&&C(e,this.__maxDateTime,de)||t?this.__timePicker.max=this.__dateToIsoTimeString(this.__maxDateTime):this.__timePicker.max=this.__defaultTimeMaxValue}}__i18nChanged(e,t,i){t&&this.__isDefaultPicker(t,"date")&&this.__syncI18n(t,e,sr),i&&this.__isDefaultPicker(i,"time")&&this.__syncI18n(i,e,nr)}__labelOrAccessibleNameChanged(e,t,i,a,s){let n=t||e||"";a&&(a.accessibleName=`${n} ${i.dateLabel||""}`.trim()),s&&(s.accessibleName=`${n} ${i.timeLabel||""}`.trim())}__datePlaceholderChanged(e,t){t&&(t.placeholder=e)}__timePlaceholderChanged(e,t){t&&(t.placeholder=e)}__stepChanged(e,t){t&&t.step!==e&&(t.step=e)}__initialPositionChanged(e,t){t&&(t.initialPosition=e)}__showWeekNumbersChanged(e,t){t&&(t.showWeekNumbers=e)}__invalidChanged(e,t,i){t&&(t.invalid=e),i&&(i.invalid=e)}__requiredChanged(e,t,i){t&&(t.required=e),i&&(i.required=e),this.__oldRequired&&!e&&this._requestValidation(),this.__oldRequired=e}__disabledChanged(e,t,i){t&&(t.disabled=e),i&&(i.disabled=e)}__readonlyChanged(e,t,i){t&&(t.readonly=e),i&&(i.readonly=e)}__parseDate(e){return Qt(e)}__formatDateISO(e,t){return e?Jt(e):t}__parseDateTime(e){let[t,i]=e.split("T");if(!(t&&i))return;let a=this.__parseDate(t);if(!a)return;let s=I(i);if(s)return a.setUTCHours(parseInt(s.hours)),a.setUTCMinutes(parseInt(s.minutes||0)),a.setUTCSeconds(parseInt(s.seconds||0)),a.setUTCMilliseconds(parseInt(s.milliseconds||0)),a}__formatDateTime(e){if(!e)return"";let t=this.__formatDateISO(e,""),i=this.__dateToIsoTimeString(e);return`${t}T${i}`}__dateToIsoTimeString(e){return ie(P({hours:e.getUTCHours(),minutes:e.getUTCMinutes(),seconds:e.getUTCSeconds(),milliseconds:e.getUTCMilliseconds()},this.step))}checkValidity(){let e=this.__pickers.some(a=>!a.checkValidity()),t=this.__filledPickers.length===1,i=this.required&&this.__pickers.some(a=>!a.value);return!e&&!i&&!t}__commitPendingValueChange(){this._requestValidation(),this.__committedValue!==this.value?this.dispatchEvent(new CustomEvent("change",{bubbles:!0})):this.__committedUnparsableValue!==this.__unparsableValue&&this.dispatchEvent(new CustomEvent("unparsable-change")),this.__committedValue=this.value,this.__committedUnparsableValue=this.__unparsableValue}get __hasPendingValueChange(){return this.__committedValue!==this.value||this.__committedUnparsableValue!==this.__unparsableValue}__dateTimeEquals(e,t){return C(e,t,de)?e.getUTCHours()===t.getUTCHours()&&e.getUTCMinutes()===t.getUTCMinutes()&&e.getUTCSeconds()===t.getUTCSeconds()&&e.getUTCMilliseconds()===t.getUTCMilliseconds():!1}__handleDateTimeChange(e,t,i,a){if(!i){this[e]="",this[t]="";return}let s=this.__parseDateTime(i);if(!s){this[e]=a;return}this.__dateTimeEquals(this[t],s)||(this[t]=s)}__valueChanged(e,t){this.__handleDateTimeChange("value","__selectedDateTime",e,t),this.__keepCommittedValue||(this.__committedValue=e,this.__committedUnparsableValue=""),this.toggleAttribute("has-value",!!e),this.__updateTimePickerMinMax()}__dispatchChange(){this.dispatchEvent(new CustomEvent("change",{bubbles:!0}))}__minChanged(e,t){this.__handleDateTimeChange("min","__minDateTime",e,t),this.__datePicker&&(this.__datePicker.min=this.__formatDateISO(this.__minDateTime,this.__defaultDateMinMaxValue)),this.__updateTimePickerMinMax(),this.__datePicker&&this.__timePicker&&this.value&&this._requestValidation()}__maxChanged(e,t){this.__handleDateTimeChange("max","__maxDateTime",e,t),this.__datePicker&&(this.__datePicker.max=this.__formatDateISO(this.__maxDateTime,this.__defaultDateMinMaxValue)),this.__updateTimePickerMinMax(),this.__datePicker&&this.__timePicker&&this.value&&this._requestValidation()}__selectedDateTimeChanged(e){let t=this.__formatDateTime(e);if(this.value!==t&&(this.value=t),!!(this.__datePicker&&this.__datePicker.$)&&!this.__ignoreInputValueChange){this.__ignoreInputValueChange=!0;let[a,s]=this.value.split("T");this.__datePicker.value=a||"",this.__timePicker.value=s||"",this.__ignoreInputValueChange=!1}}__valueChangedEventHandler(){this.__ignoreInputValueChange||(this.__ignoreInputValueChange=!0,this.__keepCommittedValue=!0,this.__updateTimePickerMinMax(),this.value=this.__formattedValue,this.__keepCommittedValue=!1,this.__ignoreInputValueChange=!1)}__autoOpenDisabledChanged(e,t,i){t&&(t.autoOpenDisabled=e),i&&(i.autoOpenDisabled=e)}__themeChanged(e,t,i){!t||!i||[t,i].forEach(a=>{e?a.setAttribute("theme",e):a.removeAttribute("theme")})}__pickersChanged(e,t){!e||!t||this.__isDefaultPicker(e,"date")===this.__isDefaultPicker(t,"time")&&(e.value?this.__valueChangedEventHandler():this.value&&(this.__selectedDateTimeChanged(this.__selectedDateTime),(this.min&&this.__minDateTime||this.max&&this.__maxDateTime)&&this._requestValidation()))}};var Je=class extends gi(f(A(p(v(c))))){static get is(){return"vaadin-date-time-picker"}static get styles(){return[S,vi]}render(){return d`
      <div class="vaadin-date-time-picker-container">
        <div part="label" @click="${this.focus}">
          <slot name="label"></slot>
          <span part="required-indicator" aria-hidden="true"></span>
        </div>

        <div part="input-fields">
          <slot name="date-picker" id="dateSlot"></slot>
          <slot name="time-picker" id="timeSlot"></slot>
        </div>

        <div part="helper-text">
          <slot name="helper"></slot>
        </div>

        <div part="error-message">
          <slot name="error-message"></slot>
        </div>
      </div>

      <slot name="tooltip"></slot>
    `}};h(Je);var dr=m`
  /* Optical centering */
  :host::before,
  :host::after {
    content: '';
    flex-basis: 0;
    flex-grow: 1;
  }

  :host::after {
    flex-grow: 1.1;
  }

  :host {
    cursor: default;
    --_overflow-indicator-height: var(--vaadin-dialog-overflow-indicator-height, 1px);
    --_overflow-indicator-color: var(--vaadin-dialog-overflow-indicator-color, var(--vaadin-border-color-secondary));
  }

  [part='overlay']:focus-visible {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
  }

  [part='overlay'] {
    color: var(--vaadin-dialog-text-color, var(--vaadin-overlay-text-color, var(--vaadin-text-color)));
    background: var(--vaadin-dialog-background, var(--vaadin-overlay-background, var(--vaadin-background-color)));
    background-origin: border-box;
    border: var(--vaadin-dialog-border-width, var(--vaadin-overlay-border-width, 1px)) solid
      var(--vaadin-dialog-border-color, var(--vaadin-overlay-border-color, var(--vaadin-border-color-secondary)));
    box-shadow: var(--vaadin-dialog-shadow, var(--vaadin-overlay-shadow, 0 8px 24px -4px rgba(0, 0, 0, 0.3)));
    border-radius: var(--vaadin-dialog-border-radius, var(--vaadin-radius-l));
    width: max-content;
    min-width: min(var(--vaadin-dialog-min-width, 4em), 100%);
    max-width: min(var(--vaadin-dialog-max-width, 100%), 100%);
    max-height: 100%;
  }

  [part='header'],
  [part='header-content'],
  [part='footer'] {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    flex: none;
    pointer-events: none;
    z-index: 1;
    gap: var(--vaadin-dialog-toolbar-gap, var(--vaadin-gap-s));
  }

  ::slotted(*) {
    pointer-events: auto;
  }

  [part='header'],
  [part='content'],
  [part='footer'] {
    padding: var(--vaadin-dialog-padding, var(--vaadin-padding-l));
  }

  :host([theme~='no-padding']) [part='content'] {
    padding: 0 !important;
  }

  :host(:is([has-header], [has-title])) [part='content'] {
    padding-top: 0;
  }

  :host([has-footer]) [part='content'] {
    padding-bottom: 0;
  }

  [part='header'] {
    flex-wrap: nowrap;
  }

  ::slotted([slot='header-content']),
  ::slotted([slot='title']),
  ::slotted([slot='footer']) {
    display: contents;
  }

  ::slotted([slot='title']) {
    font: inherit !important;
    color: inherit !important;
    overflow-wrap: anywhere;
  }

  [part='title'] {
    color: var(--vaadin-dialog-title-color, var(--vaadin-text-color));
    font-weight: var(--vaadin-dialog-title-font-weight, 600);
    font-size: var(--vaadin-dialog-title-font-size, 1em);
    line-height: var(--vaadin-dialog-title-line-height, inherit);
  }

  [part='header-content'] {
    flex: 1;
  }

  :host([has-title]) [part='header-content'],
  [part='footer'] {
    justify-content: flex-end;
  }

  :host(:not([has-title]):not([has-header])) [part='header'],
  :host(:not([has-header])) [part='header-content'],
  :host(:not([has-title])) [part='title'],
  :host(:not([has-footer])) [part='footer'] {
    display: none !important;
  }

  [part='header'],
  [part='footer'] {
    position: relative;

    &::after {
      content: '';
      opacity: 0;
      position: absolute;
      pointer-events: none;
      height: var(--_overflow-indicator-height);
      top: 100%;
      inset-inline: 0;
      background: linear-gradient(
        var(--_overflow-indicator-dir, to bottom),
        var(--_overflow-indicator-color),
        var(--_overflow-indicator-color) 1px,
        transparent
      );
    }
  }

  [part='footer']::after {
    top: auto;
    bottom: 100%;
    --_overflow-indicator-dir: to top;
  }

  :host([overflow~='top']) [part='header']::after,
  :host([overflow~='bottom']) [part='footer']::after {
    opacity: 1;
  }
`,hr=m`
  [part='overlay'] {
    position: relative;
    overflow: visible;
    display: flex;
  }

  :host([has-bounds-set]) [part='overlay'] {
    min-width: 0;
  }

  :host([has-bounds-set]:not([keep-in-viewport])) [part='overlay'] {
    max-width: none;
    max-height: none;
  }

  /* Content part scrolls by default */
  [part='content'] {
    flex: 1;
    min-height: 0;
    overflow: auto;
    overscroll-behavior: contain;
    clip-path: border-box;
  }

  [part='header'],
  :host(:not([has-title], [has-header])) [part='content'] {
    border-top-left-radius: inherit;
    border-top-right-radius: inherit;
  }

  [part='footer'],
  :host(:not([has-footer])) [part='content'] {
    border-bottom-left-radius: inherit;
    border-bottom-right-radius: inherit;
  }

  .resizer-container {
    display: flex;
    flex-direction: column;
    flex-grow: 1;
    max-width: 100%;
    border-radius: calc(
      var(--vaadin-dialog-border-radius, var(--vaadin-radius-l)) - var(
          --vaadin-dialog-border-width,
          var(--vaadin-overlay-border-width, 1px)
        )
    );
  }

  :host(:not([resizable])) .resizer {
    display: none;
  }

  .resizer {
    position: absolute;
    height: 16px;
    width: 16px;
  }

  .resizer.edge {
    height: 8px;
    width: 8px;
    inset: -4px;
  }

  .resizer.edge.n {
    width: auto;
    bottom: auto;
    cursor: ns-resize;
  }

  .resizer.ne {
    top: -4px;
    right: -4px;
    cursor: nesw-resize;
  }

  .resizer.edge.e {
    height: auto;
    left: auto;
    cursor: ew-resize;
  }

  .resizer.se {
    bottom: -4px;
    right: -4px;
    cursor: nwse-resize;
  }

  .resizer.edge.s {
    width: auto;
    top: auto;
    cursor: ns-resize;
  }

  .resizer.sw {
    bottom: -4px;
    left: -4px;
    cursor: nesw-resize;
  }

  .resizer.edge.w {
    height: auto;
    right: auto;
    cursor: ew-resize;
  }

  .resizer.nw {
    top: -4px;
    left: -4px;
    cursor: nwse-resize;
  }
`,bi=[V,dr,hr];var Ee=class{constructor(r){this.host=r}hostConnected(){if(!this.__initialized){this.__initialized=!0;let{host:r}=this;this.__resizeObserver=new ResizeObserver(()=>this.__updateState()),this.__resizeObserver.observe(r.$.resizerContainer),r.$.content.addEventListener("scroll",()=>this.__updateState(!0)),r.shadowRoot.addEventListener("slotchange",()=>this.__updateState(!0))}}update(){this.__updateState(!0)}__updateState(r=!1){cancelAnimationFrame(this.__updateRaf),r?this.__writeState(this.__readState()):this.__updateRaf=requestAnimationFrame(()=>this.__writeState(this.__readState()))}__readState(){let r=this.host.$.content,e="";return r.scrollTop>0&&(e+=" top"),r.scrollTop<r.scrollHeight-r.clientHeight&&(e+=" bottom"),e.trim()}__writeState(r){let{host:e}=this;r.length>0?j(e,"overflow",r):j(e,"overflow",null)}};var yi=o=>class extends R(o){static get properties(){return{headerTitle:{type:String},headerRenderer:{type:Object},footerRenderer:{type:Object},keepInViewport:{type:Boolean,reflectToAttribute:!0}}}static get observers(){return["_headerFooterRendererChange(headerRenderer, footerRenderer, opened)","_headerTitleChanged(headerTitle, opened)"]}get _contentRoot(){return this.owner}get _rendererRoot(){if(!this.__savedRoot){let e=document.createElement("vaadin-dialog-content");e.style.display="contents",this.owner.appendChild(e),this.__savedRoot=e}return this.__savedRoot}ready(){super.ready(),this.__overflowController=new Ee(this),this.addController(this.__overflowController),this.__resizeObserver=new ResizeObserver(()=>{requestAnimationFrame(()=>{this.__adjustPosition()})}),this.__resizeObserver.observe(this.$.resizerContainer);let e=this.shadowRoot.querySelector('slot[name="header-content"]');this.__headerSlotObserver=new ne(e,({currentNodes:i})=>{j(this,"has-header",i.length>0),this.__overflowController.update()});let t=this.shadowRoot.querySelector('slot[name="footer"]');this.__footerSlotObserver=new ne(t,({currentNodes:i})=>{j(this,"has-footer",i.length>0),this.__overflowController.update()}),this.__handleWindowResize=this.__handleWindowResize.bind(this)}updated(e){super.updated(e),(e.has("opened")||e.has("keepInViewport"))&&(this.opened&&this.keepInViewport?window.addEventListener("resize",this.__handleWindowResize):window.removeEventListener("resize",this.__handleWindowResize))}__createContainer(e){let t=document.createElement("vaadin-dialog-content");return t.setAttribute("slot",e),t}__clearContainer(e){e.innerHTML="",delete e._$litPart$}__initContainer(e,t){return e?this.__clearContainer(e):(e=this.__createContainer(t),this.owner.appendChild(e)),e}_headerFooterRendererChange(e,t,i){let a=this.__oldHeaderRenderer!==e;this.__oldHeaderRenderer=e;let s=this.__oldFooterRenderer!==t;this.__oldFooterRenderer=t;let n=this._oldOpenedFooterHeader!==i;this._oldOpenedFooterHeader=i,a&&(e?this.headerContainer=this.__initContainer(this.headerContainer,"header-content"):this.headerContainer&&(this.headerContainer.remove(),this.headerContainer=null)),s&&(t?this.footerContainer=this.__initContainer(this.footerContainer,"footer"):this.footerContainer&&(this.footerContainer.remove(),this.footerContainer=null)),(e&&(a||n)||t&&(s||n))&&i&&this.requestContentUpdate()}_headerTitleChanged(e,t){j(this,"has-title",!!e),t&&(e||this._oldHeaderTitle)&&this.requestContentUpdate(),this._oldHeaderTitle=e}_headerTitleRenderer(){this.headerTitle?(this.headerTitleElement||(this.headerTitleElement=document.createElement("h2"),this.headerTitleElement.setAttribute("slot","title"),this.headerTitleElement.classList.add("draggable")),this.owner.appendChild(this.headerTitleElement),this.headerTitleElement.textContent=this.headerTitle):this.headerTitleElement&&(this.headerTitleElement.remove(),this.headerTitleElement=null)}requestContentUpdate(){super.requestContentUpdate(),this.headerContainer&&this.headerRenderer&&this.headerRenderer.call(this.owner,this.headerContainer,this.owner),this.footerContainer&&this.footerRenderer&&this.footerRenderer.call(this.owner,this.footerContainer,this.owner),this._headerTitleRenderer(),this.__overflowController?.update()}getBounds(){let e=this.$.overlay.getBoundingClientRect(),t=this.getBoundingClientRect(),i=e.top-t.top,a=e.left-t.left,s=e.width,n=e.height;return{top:i,left:a,width:s,height:n}}setBounds(e,t=!0){super.setBounds(e,t),this.__adjustPosition()}__handleWindowResize(){this.__adjustPosition()}__adjustPosition(){if(!this.opened||!this.keepInViewport)return;let e=getComputedStyle(this.$.overlay);if(e.position!=="absolute")return;let t=this.getBoundingClientRect(),i=this.getBounds(),a=parseFloat(e.width)||i.width,s=parseFloat(e.height)||i.height,n=t.right-t.left-a,l=t.bottom-t.top-s;if(i.left>n||i.top>l){let u=Math.max(0,Math.min(i.left,n)),_=Math.max(0,Math.min(i.top,l));Object.assign(this.$.overlay.style,{left:`${u}px`,top:`${_}px`})}}};var Me=class o extends yi(M(f(p(v(c))))){static get is(){return"vaadin-dialog-overlay"}static get styles(){return bi}get _focusTrapRoot(){return this.owner}render(){return d`
      <div id="backdrop" part="backdrop" ?hidden="${!this.withBackdrop}"></div>
      <div part="overlay" id="overlay">
        <section id="resizerContainer" class="resizer-container">
          <header part="header">
            <div part="title"><slot name="title"></slot></div>
            <div part="header-content"><slot name="header-content"></slot></div>
          </header>
          <div part="content" id="content"><slot></slot></div>
          <footer part="footer"><slot name="footer"></slot></footer>
        </section>
      </div>
    `}bringToFront(r){if(r instanceof Event){let e=r.composedPath();if(Dt(this).some(i=>e.includes(i)&&kt(this,i)&&i instanceof o))return}super.bringToFront()}};h(Me);var Ai=o=>class extends o{static get properties(){return{opened:{type:Boolean,reflectToAttribute:!0,value:!1,notify:!0,sync:!0},noCloseOnOutsideClick:{type:Boolean,value:!1},noCloseOnEsc:{type:Boolean,value:!1},modeless:{type:Boolean,value:!1},noFocusTrap:{type:Boolean,value:!1},top:{type:String},left:{type:String},overlayRole:{type:String},keepInViewport:{type:Boolean,value:!1,reflectToAttribute:!0}}}static get observers(){return["__positionChanged(top, left)"]}ready(){super.ready();let e=this.$.overlay;e.addEventListener("vaadin-overlay-outside-click",this._handleOutsideClick.bind(this)),e.addEventListener("vaadin-overlay-escape-press",this._handleEscPress.bind(this)),e.addEventListener("vaadin-overlay-closed",this.__handleOverlayClosed.bind(this)),this._overlayElement=e,this.hasAttribute("role")||(this.role="dialog"),this.setAttribute("tabindex","0")}updated(e){super.updated(e),e.has("overlayRole")&&(this.role=this.overlayRole||"dialog"),e.has("modeless")&&(this.modeless?this.removeAttribute("aria-modal"):this.setAttribute("aria-modal","true"))}__handleOverlayClosed(){this.dispatchEvent(new CustomEvent("closed"))}connectedCallback(){super.connectedCallback(),this.__restoreOpened&&(this.opened=!0)}disconnectedCallback(){super.disconnectedCallback(),setTimeout(()=>{this.isConnected||(this.__restoreOpened=this.opened,this.opened=!1)})}_onOverlayOpened(e){e.detail.value===!1&&(this.opened=!1)}_handleOutsideClick(e){this.noCloseOnOutsideClick&&e.preventDefault()}_handleEscPress(e){this.noCloseOnEsc&&e.preventDefault()}_bringOverlayToFront(e){this.modeless&&this._overlayElement.bringToFront(e)}__positionChanged(e,t){requestAnimationFrame(()=>this.$.overlay.setBounds({top:e,left:t}))}__sizeChanged(e,t){requestAnimationFrame(()=>this.$.overlay.setBounds({width:e,height:t},!1))}};function re(o){return o.touches?o.touches[0]:o}function Te(o){return o.clientX>=0&&o.clientX<=window.innerWidth&&o.clientY>=0&&o.clientY<=window.innerHeight}var xi=o=>class extends o{static get properties(){return{draggable:{type:Boolean,value:!1,reflectToAttribute:!0},_touchDevice:{type:Boolean,value:ue},__dragHandleClassName:{type:String}}}ready(){super.ready(),this._originalBounds={},this._originalMouseCoords={},this._startDrag=this._startDrag.bind(this),this._drag=this._drag.bind(this),this._stopDrag=this._stopDrag.bind(this),this.$.overlay.$.overlay.addEventListener("mousedown",this._startDrag),this.$.overlay.$.overlay.addEventListener("touchstart",this._startDrag)}_startDrag(e){if(!(e.type==="touchstart"&&e.touches.length>1)&&!e.defaultPrevented&&this.draggable&&(e.button===0||e.touches)){let t=this.$.overlay.$.resizerContainer,i=e.target===t,a=e.offsetX>t.clientWidth||e.offsetY>t.clientHeight,s=e.target===this.$.overlay.$.content,n=e.composedPath().some((l,u)=>{if(!l.classList)return!1;let _=l.classList.contains(this.__dragHandleClassName||"draggable"),g=l.classList.contains("draggable-leaf-only"),y=u===0;return g&&y||_&&(!g||y)});if(i&&!a||s||n){e.preventDefault(),this._originalBounds=this.$.overlay.getBounds();let l=re(e);this._originalMouseCoords={top:l.pageY,left:l.pageX},window.addEventListener("mouseup",this._stopDrag),window.addEventListener("touchend",this._stopDrag),window.addEventListener("mousemove",this._drag),window.addEventListener("touchmove",this._drag);let{top:u,left:_,width:g,height:y}=this._originalBounds;this.$.overlay.$.overlay.style.position!=="absolute"&&(this.top=u,this.left=_),this.dispatchEvent(new CustomEvent("drag-start",{detail:{width:g,height:y,top:u,left:_}}))}}}_drag(e){let t=re(e);if(Te(t)){let i=this._originalBounds.top+(t.pageY-this._originalMouseCoords.top),a=this._originalBounds.left+(t.pageX-this._originalMouseCoords.left);if(this.keepInViewport){let{width:s,height:n}=this._originalBounds,l=this.$.overlay.getBoundingClientRect(),u=l.right-l.left-s,_=l.bottom-l.top-n;a=Math.max(0,Math.min(a,u)),i=Math.max(0,Math.min(i,_))}this.top=i,this.left=a}}_stopDrag(){this.dispatchEvent(new CustomEvent("dragged",{detail:{top:this.top,left:this.left}})),window.removeEventListener("mouseup",this._stopDrag),window.removeEventListener("touchend",this._stopDrag),window.removeEventListener("mousemove",this._drag),window.removeEventListener("touchmove",this._drag)}};var wi=o=>class extends o{static get properties(){return{renderer:{type:Object},headerTitle:{type:String},headerRenderer:{type:Object},footerRenderer:{type:Object}}}requestContentUpdate(){this._overlayElement&&this._overlayElement.requestContentUpdate()}};var Ci=o=>class extends o{static get properties(){return{resizable:{type:Boolean,value:!1,reflectToAttribute:!0}}}ready(){super.ready(),this._originalBounds={},this._originalMouseCoords={},this._resizeListeners={start:{},resize:{},stop:{}},this._addResizeListeners()}_addResizeListeners(){["n","e","s","w","nw","ne","se","sw"].forEach(e=>{let t=document.createElement("div");this._resizeListeners.start[e]=i=>this._startResize(i,e),this._resizeListeners.resize[e]=i=>this._resize(i,e),this._resizeListeners.stop[e]=()=>this._stopResize(e),e.length===1&&t.classList.add("edge"),t.classList.add("resizer"),t.classList.add(e),t.addEventListener("mousedown",this._resizeListeners.start[e]),t.addEventListener("touchstart",this._resizeListeners.start[e]),this.$.overlay.$.resizerContainer.appendChild(t)})}_startResize(e,t){if(!(e.type==="touchstart"&&e.touches.length>1)&&(e.button===0||e.touches)){e.preventDefault(),this._originalBounds=this.$.overlay.getBounds();let i=re(e);this._originalMouseCoords={top:i.pageY,left:i.pageX},window.addEventListener("mousemove",this._resizeListeners.resize[t]),window.addEventListener("touchmove",this._resizeListeners.resize[t]),window.addEventListener("mouseup",this._resizeListeners.stop[t]),window.addEventListener("touchend",this._resizeListeners.stop[t]),this.$.overlay.setBounds(this._originalBounds),this.$.overlay.setAttribute("has-bounds-set","");let{width:a,height:s,top:n,left:l}=this._originalBounds;this.dispatchEvent(new CustomEvent("resize-start",{detail:{width:a,height:s,top:n,left:l}}))}}_resize(e,t){let i=re(e);Te(i)&&t.split("").forEach(s=>{switch(s){case"n":{let n=this._originalBounds.height-(i.pageY-this._originalMouseCoords.top),l=this._originalBounds.top+(i.pageY-this._originalMouseCoords.top);n>40&&(this.top=l,this.height=n);break}case"e":{let n=this._originalBounds.width+(i.pageX-this._originalMouseCoords.left);n>40&&(this.width=n);break}case"s":{let n=this._originalBounds.height+(i.pageY-this._originalMouseCoords.top);n>40&&(this.height=n);break}case"w":{let n=this._originalBounds.width-(i.pageX-this._originalMouseCoords.left),l=this._originalBounds.left+(i.pageX-this._originalMouseCoords.left);n>40&&(this.left=l,this.width=n);break}default:break}})}_stopResize(e){window.removeEventListener("mousemove",this._resizeListeners.resize[e]),window.removeEventListener("touchmove",this._resizeListeners.resize[e]),window.removeEventListener("mouseup",this._resizeListeners.stop[e]),window.removeEventListener("touchend",this._resizeListeners.stop[e]),this.dispatchEvent(new CustomEvent("resize",{detail:this._getResizeDimensions()}))}_getResizeDimensions(){let{width:e,height:t,top:i,left:a}=getComputedStyle(this.$.overlay.$.overlay);return{width:e,height:t,top:i,left:a}}};var Di=o=>class extends o{static get properties(){return{width:{type:String},height:{type:String}}}static get observers(){return["__sizeChanged(width, height)"]}__sizeChanged(e,t){requestAnimationFrame(()=>this.$.overlay.setBounds({width:e,height:t},!1))}};var Ze=class extends Di(xi(Ci(wi(Ai(bt(A(p(c)))))))){static get is(){return"vaadin-dialog"}static get styles(){return m`
      :host([opened]),
      :host([opening]),
      :host([closing]) {
        display: block !important;
        position: fixed;
        outline: none;
      }

      :host,
      :host([hidden]) {
        display: none !important;
      }

      :host(:focus-visible) ::part(overlay) {
        outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
      }
    `}render(){return d`
      <vaadin-dialog-overlay
        id="overlay"
        .owner="${this}"
        .opened="${this.opened}"
        .headerTitle="${this.headerTitle}"
        .renderer="${this.renderer}"
        .headerRenderer="${this.headerRenderer}"
        .footerRenderer="${this.footerRenderer}"
        .keepInViewport="${this.keepInViewport}"
        @opened-changed="${this._onOverlayOpened}"
        @mousedown="${this._bringOverlayToFront}"
        @touchstart="${this._bringOverlayToFront}"
        theme="${D(this._theme)}"
        .modeless="${this.modeless}"
        .withBackdrop="${!this.modeless}"
        ?resizable="${this.resizable}"
        restore-focus-on-close
        ?focus-trap="${!this.noFocusTrap}"
        exportparts="backdrop, overlay, header, title, header-content, content, footer"
      >
        <slot name="title" slot="title"></slot>
        <slot name="header-content" slot="header-content"></slot>
        <slot name="footer" slot="footer"></slot>
        <slot></slot>
      </vaadin-dialog-overlay>
    `}updated(r){super.updated(r),r.has("headerTitle")&&(this.ariaLabel=this.headerTitle)}};h(Ze);var Se=o=>class extends J(F(o)){static get properties(){return{_hasVaadinItemMixin:{value:!0},selected:{type:Boolean,value:!1,reflectToAttribute:!0,observer:"_selectedChanged",sync:!0},_value:String}}get _activeKeys(){return["Enter"," "]}get value(){return this._value??this.textContent.trim()}set value(e){this._value=e}ready(){super.ready();let e=this.getAttribute("value");e!==null&&(this.value=e),this.__shouldAllowFocusWhenDisabled()&&this.style.setProperty("--_vaadin-item-disabled-pointer-events","auto")}focus(e){this.disabled&&!this.__shouldAllowFocusWhenDisabled()||super.focus(e)}_shouldSetActive(e){return!this.disabled&&!(e.type==="keydown"&&e.defaultPrevented)}_selectedChanged(e){this.setAttribute("aria-selected",e)}_disabledChanged(e){super._disabledChanged(e),e&&(this.selected=!1,this.__shouldAllowFocusWhenDisabled()||this.blur())}_onKeyDown(e){super._onKeyDown(e),this._activeKeys.includes(e.key)&&!e.defaultPrevented&&(e.preventDefault(),this.click())}__shouldAllowFocusWhenDisabled(){return!1}};var et=class extends Se(f(M(p(v(c))))){static get is(){return"vaadin-select-item"}static get styles(){return he}static get properties(){return{role:{type:String,value:"option",reflectToAttribute:!0}}}render(){return d`
      <span part="checkmark" aria-hidden="true"></span>
      <div part="content">
        <slot></slot>
      </div>
    `}};h(et);var ki=o=>class extends U(o){get focused(){return(this._getItems()||[]).find(Ct)}get _vertical(){return!0}get _tabNavigation(){return!1}focus(e){let t=this._getFocusableIndex();t>=0&&this._focus(t,e)}_getFocusableIndex(){let e=this._getItems();return Array.isArray(e)?this._getAvailableIndex(e,0,null,t=>!se(t)):-1}_getItems(){return Array.from(this.children)}_onKeyDown(e){if(super._onKeyDown(e),e.metaKey||e.ctrlKey)return;let{key:t,shiftKey:i}=e,a=this._getItems()||[],s=a.indexOf(this.focused),n,l,_=!this._vertical&&this.getAttribute("dir")==="rtl"?-1:1;this.__isPrevKeyPressed(t,i)?(l=-_,n=s-_):this.__isNextKeyPressed(t,i)?(l=_,n=s+_):t==="Home"?(l=1,n=0):t==="End"&&(l=-1,n=a.length-1),n=this._getAvailableIndex(a,n,l,g=>!se(g)),!(this._tabNavigation&&t==="Tab"&&(n>s&&e.shiftKey||n<s&&!e.shiftKey||n===s))&&n>=0&&(e.preventDefault(),this._focus(n,{focusVisible:!0,preventScroll:!0},!0))}__isPrevKeyPressed(e,t){return this._vertical?e==="ArrowUp":e==="ArrowLeft"||this._tabNavigation&&e==="Tab"&&t}__isNextKeyPressed(e,t){return this._vertical?e==="ArrowDown":e==="ArrowRight"||this._tabNavigation&&e==="Tab"&&!t}_focus(e,t,i=!1){let a=this._getItems();this._focusItem(a[e],t,i)}_focusItem(e,t){e&&e.focus(t)}_getAvailableIndex(e,t,i,a){let s=e.length,n=t;for(let l=0;typeof n=="number"&&l<s;l+=1,n+=i||1){n<0?n=s-1:n>=s&&(n=0);let u=e[n];if(this._isItemFocusable(u)&&this.__isMatchingItem(u,a))return n}return-1}__isMatchingItem(e,t){return typeof t=="function"?t(e):!0}_isItemFocusable(e){return!e.hasAttribute("disabled")}};var Ie=o=>class extends ki(o){static get properties(){return{disabled:{type:Boolean,value:!1,reflectToAttribute:!0},selected:{type:Number,reflectToAttribute:!0,notify:!0,sync:!0},orientation:{type:String,reflectToAttribute:!0,value:""},items:{type:Array,readOnly:!0,notify:!0},_searchBuf:{type:String,value:""}}}static get observers(){return["_enhanceItems(items, orientation, selected, disabled)"]}get _isRTL(){return!this._vertical&&this.getAttribute("dir")==="rtl"}get _scrollerElement(){return console.warn(`Please implement the '_scrollerElement' property in <${this.localName}>`),this}get _vertical(){return this.orientation!=="horizontal"}focus(e){this._observer&&this._observer.flush();let t=Array.isArray(this.items)?this.items:[],i=this._getAvailableIndex(t,0,null,a=>a.tabIndex===0&&!se(a));i>=0?this._focus(i,e):super.focus(e)}ready(){super.ready(),this.addEventListener("click",t=>this._onClick(t));let e=this.shadowRoot.querySelector("slot:not([name])");this._observer=new ne(e,()=>{this._setItems(this._filterItems([...this.children]))})}_getItems(){return this.items}_enhanceItems(e,t,i,a){if(!a&&e){this.setAttribute("aria-orientation",t||"vertical"),e.forEach(n=>{t?n.setAttribute("orientation",t):n.removeAttribute("orientation")}),this._setFocusable(i<0||!i?0:i);let s=e[i];e.forEach(n=>{n.selected=n===s}),s&&!s.disabled&&this._scrollToItem(i)}}_filterItems(e){return e.filter(t=>t._hasVaadinItemMixin)}_onClick(e){if(e.metaKey||e.shiftKey||e.ctrlKey||e.defaultPrevented)return;let t=this._filterItems(e.composedPath())[0],i;t&&!t.disabled&&(i=this.items.indexOf(t))>=0&&(this.selected=i)}_searchKey(e,t){this._searchReset=Y.debounce(this._searchReset,q.after(500),()=>{this._searchBuf=""}),this._searchBuf+=t.toLowerCase(),this.items.some(a=>this.__isMatchingKey(a))||(this._searchBuf=t.toLowerCase());let i=this._searchBuf.length===1?e+1:e;return this._getAvailableIndex(this.items,i,1,a=>this.__isMatchingKey(a)&&getComputedStyle(a).display!=="none")}__isMatchingKey(e){return e.textContent.replace(/[^\p{L}\p{Nd}]/gu,"").toLowerCase().startsWith(this._searchBuf)}_onKeyDown(e){if(e.metaKey||e.ctrlKey)return;let t=e.key,i=this.items.indexOf(this.focused);if(/[\p{L}\p{Nd}]/u.test(t)&&t.length===1){let a=this._searchKey(i,t);a>=0&&this._focus(a);return}super._onKeyDown(e)}_setFocusable(e){e=this._getAvailableIndex(this.items,e,1);let t=this.items[e];this.items.forEach(i=>{i.tabIndex=i===t?0:-1})}_focus(e,t){this.items.forEach((i,a)=>{i.focused=a===e}),this._setFocusable(e),this._scrollToItem(e),super._focus(e,t??{preventScroll:!0})}_scrollToItem(e){let t=this._getItems()[e];t&&t.scrollIntoView({block:"nearest",inline:"nearest"})}_scroll(e){if(this._vertical)this._scrollerElement.scrollTop+=e;else{let t=this.getAttribute("dir")||"ltr",i=fe(this._scrollerElement,t)+e;zt(this._scrollerElement,t,i)}}_isItemFocusable(e){return e.disabled&&e.__shouldAllowFocusWhenDisabled?e.__shouldAllowFocusWhenDisabled():super._isItemFocusable(e)}};var Ei=m`
  :host {
    --vaadin-item-checkmark-display: block;
    display: flex;
  }

  :host([hidden]) {
    display: none !important;
  }

  [part='items'] {
    height: 100%;
    overflow-y: auto;
    width: 100%;
  }

  [part='items'] ::slotted(hr) {
    border-color: var(--vaadin-divider-color, var(--vaadin-border-color-secondary));
    border-width: 0 0 1px;
    margin: 4px 8px;
    margin-inline-start: calc(var(--vaadin-icon-size, 1lh) + var(--vaadin-item-gap, var(--vaadin-gap-s)) + 8px);
  }
`;var tt=class extends Ie(f(M(p(v(c))))){static get is(){return"vaadin-select-list-box"}static get styles(){return Ei}static get properties(){return{orientation:{readOnly:!0}}}get _scrollerElement(){return this.shadowRoot.querySelector('[part="items"]')}render(){return d`
      <div part="items">
        <slot></slot>
      </div>
    `}ready(){super.ready(),this.setAttribute("role","listbox")}};h(tt);var Mi=m`
  :host {
    align-items: flex-start;
    justify-content: flex-start;
  }

  [part='overlay'] {
    min-width: var(--vaadin-select-overlay-width, var(--_vaadin-select-overlay-default-width));
  }

  [part='content'] {
    padding: var(--vaadin-item-overlay-padding, 4px);
  }

  [part='backdrop'] {
    background: transparent;
  }
`;var Ti=o=>class extends ce(R(M(o))){static get observers(){return["_updateOverlayWidth(opened, positionTarget)"]}ready(){super.ready(),this.restoreFocusOnClose=!0}get _contentRoot(){return this._rendererRoot}get _rendererRoot(){if(!this.__savedRoot){let e=document.createElement("div");e.setAttribute("slot","overlay"),this.owner.appendChild(e),this.__savedRoot=e}return this.__savedRoot}_shouldCloseOnOutsideClick(e){return!0}_mouseDownListener(e){super._mouseDownListener(e),e.preventDefault()}_getMenuElement(){return Array.from(this._rendererRoot.children).find(e=>e.localName!=="style")}_updateOverlayWidth(e,t){e&&t&&this.style.setProperty("--_vaadin-select-overlay-default-width",`${t.offsetWidth}px`)}requestContentUpdate(){if(super.requestContentUpdate(),this.owner){let e=this._getMenuElement();this.owner._assignMenuElement(e)}}};var it=class extends Ti(f(p(v(c)))){static get is(){return"vaadin-select-overlay"}static get styles(){return[V,Mi]}render(){return d`
      <div id="backdrop" part="backdrop" ?hidden="${!this.withBackdrop}"></div>
      <div part="overlay" id="overlay">
        <div part="content" id="content">
          <slot></slot>
        </div>
      </div>
    `}updated(r){super.updated(r),r.has("renderer")&&this.requestContentUpdate()}};h(it);var Si=m`
  :host {
    min-height: 1lh;
    outline: none;
    overflow: hidden;
    white-space: nowrap;
    width: 100%;
    display: flex;
    align-items: center;
  }

  ::slotted(*) {
    padding: 0;
    cursor: inherit;
  }

  .vaadin-button-container,
  [part='label'] {
    display: contents;
  }

  :host([placeholder]) {
    color: var(--vaadin-input-field-placeholder-color, var(--vaadin-text-color-secondary));
  }

  :host([disabled]) {
    pointer-events: none;
  }
`;var rt=class extends ve(f(p(v(c)))){static get is(){return"vaadin-select-value-button"}static get styles(){return Si}render(){return d`
      <div class="vaadin-button-container">
        <span part="label">
          <slot></slot>
        </span>
      </div>
    `}};h(rt);var Ii=m`
  .sr-only {
    border: 0 !important;
    clip: rect(1px, 1px, 1px, 1px) !important;
    clip-path: inset(50%) !important;
    height: 1px !important;
    margin: -1px !important;
    overflow: hidden !important;
    padding: 0 !important;
    position: absolute !important;
    width: 1px !important;
    white-space: nowrap !important;
  }
`;var Fi=m`
  :host {
    position: relative;
  }

  ::slotted([slot='value']) {
    flex: 1;
  }

  ::slotted(div[slot='overlay']) {
    display: contents;
  }

  :host(:not([focus-ring])) [part='input-field'] {
    outline: none;
  }

  :host([readonly]:not([focus-ring])) [part='input-field'] {
    --vaadin-input-field-border-color: inherit;
  }

  [part='input-field'],
  :host(:not([readonly])) ::slotted([slot='value']) {
    cursor: var(--vaadin-clickable-cursor);
  }

  [part~='toggle-button']::before {
    mask-image: var(--_vaadin-icon-chevron-down);
  }

  :host([readonly]) [part~='toggle-button'] {
    display: none;
  }

  :host([theme~='align-start']) {
    --vaadin-item-text-align: start;
  }

  :host([theme~='align-center']) {
    --vaadin-item-text-align: center;
  }

  :host([theme~='align-end']) {
    --vaadin-item-text-align: end;
  }

  :host([theme~='align-left']) {
    --vaadin-item-text-align: left;
  }

  :host([theme~='align-right']) {
    --vaadin-item-text-align: right;
  }

  :host([theme~='align-start']) ::slotted([slot='value']) {
    justify-content: start;
  }

  :host([theme~='align-center']) ::slotted([slot='value']) {
    justify-content: center;
  }

  :host([theme~='align-end']) ::slotted([slot='value']) {
    justify-content: end;
  }

  :host([theme~='align-left']) ::slotted([slot='value']) {
    justify-content: left;
  }

  :host([theme~='align-right']) ::slotted([slot='value']) {
    justify-content: right;
  }
`;var Fe=class extends k{constructor(r){super(r,"value","vaadin-select-value-button",{initializer:(e,t)=>{t._setFocusElement(e),t.ariaTarget=e,t.stateTarget=e,e.setAttribute("aria-haspopup","listbox")}})}};var Bi=o=>class extends Q(me(U(G(o)))){static get properties(){return{items:{type:Array,observer:"__itemsChanged"},opened:{type:Boolean,value:!1,notify:!0,observer:"_openedChanged",reflectToAttribute:!0,sync:!0},renderer:{type:Object},value:{type:String,value:"",notify:!0,observer:"_valueChanged",sync:!0},name:{type:String},placeholder:{type:String},readonly:{type:Boolean,value:!1,reflectToAttribute:!0},noVerticalOverlap:{type:Boolean,value:!1},_phone:Boolean,_phoneMediaQuery:{value:"(max-width: 450px), (max-height: 450px)"},_inputContainer:Object,_items:Object}}static get delegateAttrs(){return[...super.delegateAttrs,"invalid"]}static get observers(){return["_updateAriaExpanded(opened, focusElement)","_updateSelectedItem(value, _items, placeholder, focusElement)"]}constructor(){super(),this._itemId=`value-${this.localName}-${pe()}`,this._srLabelController=new $t(this),this._srLabelController.slotName="sr-label"}disconnectedCallback(){super.disconnectedCallback(),this.opened=!1}ready(){super.ready(),this._inputContainer=this.shadowRoot.querySelector('[part~="input-field"]'),this._overlayElement=this.$.overlay,this._valueButtonController=new Fe(this),this.addController(this._valueButtonController),this.addController(this._srLabelController),this.addController(new N(this._phoneMediaQuery,e=>{this._phone=e})),this._tooltipController=new E(this),this._tooltipController.setPosition("top"),this._tooltipController.setAriaTarget(this.focusElement),this.addController(this._tooltipController)}updated(e){super.updated(e),e.has("_phone")&&this.toggleAttribute("phone",this._phone)}requestContentUpdate(){this._overlayElement&&this._overlayElement.requestContentUpdate()}_requiredChanged(e){super._requiredChanged(e),e===!1&&this._requestValidation()}__itemsChanged(e,t){(e||t)&&this.requestContentUpdate()}_assignMenuElement(e){e&&e!==this.__lastMenuElement&&(this._menuElement=e,this.__initMenuItems(e),e.addEventListener("items-changed",()=>{this.__initMenuItems(e)}),e.addEventListener("selected-changed",()=>this.__updateValueButton()),e.addEventListener("keydown",t=>this._onKeyDownInside(t),!0),e.addEventListener("click",t=>{let i=t.composedPath().find(a=>a._hasVaadinItemMixin);this.__dispatchChangePending=i?.value!==void 0&&i.value!==this.value,this.opened=!1},!0),this.__lastMenuElement=e),this._menuElement&&this._menuElement.items&&this._updateSelectedItem(this.value,this._menuElement.items)}__initMenuItems(e){e.items&&(this._items=e.items)}_valueChanged(e,t){this.toggleAttribute("has-value",!!e),t!==void 0&&!this.__dispatchChangePending&&this._requestValidation()}_onClick(e){this.disabled||(e.preventDefault(),this.opened=!this.readonly)}_onEscape(e){this.opened&&(e.stopPropagation(),this.opened=!1)}_onToggleMouseDown(e){e.preventDefault(),this.opened||this.focusElement.focus()}_onKeyDown(e){if(super._onKeyDown(e),!(e.altKey||e.shiftKey||e.ctrlKey||e.metaKey)&&e.target===this.focusElement&&!this.readonly&&!this.disabled&&!this.opened){if(/^(Enter|SpaceBar|\s|ArrowDown|Down|ArrowUp|Up)$/u.test(e.key))e.preventDefault(),this.opened=!0;else if(/[\p{L}\p{Nd}]/u.test(e.key)&&e.key.length===1){let i=this._menuElement.selected??-1,a=this._menuElement._searchKey(i,e.key);a>=0&&(this.__dispatchChangePending=!0,this._updateAriaLive(!0),this._menuElement.selected=a)}}}_onKeyDownInside(e){e.key==="Tab"&&(this.focusElement.setAttribute("tabindex","-1"),this._overlayElement.restoreFocusOnClose=!1,this.opened=!1,setTimeout(()=>{this.focusElement.setAttribute("tabindex","0"),this._overlayElement.restoreFocusOnClose=!0}))}_openedChanged(e,t){if(e){if(this.disabled||this.readonly){this.opened=!1;return}this._updateAriaLive(!1);let i=this.hasAttribute("focus-ring");this._openedWithFocusRing=i,i&&this.removeAttribute("focus-ring")}else t&&(this._openedWithFocusRing&&this.setAttribute("focus-ring",""),!this.__dispatchChangePending&&!this._keyboardActive&&this._requestValidation())}_updateAriaExpanded(e,t){t&&t.setAttribute("aria-expanded",e?"true":"false")}_updateAriaLive(e){this.focusElement&&(e?this.focusElement.setAttribute("aria-live","polite"):this.focusElement.removeAttribute("aria-live"))}__attachSelectedItem(e){let t,i=e.getAttribute("label");i?t=this.__createItemElement({label:i}):t=e.cloneNode(!0),t._sourceItem=e,this.__appendValueItemElement(t,this.focusElement),t.selected=!0}__createItemElement(e){let t=document.createElement(e.component||"vaadin-select-item");return e.label&&(t.textContent=e.label),e.value&&(t.value=e.value),e.disabled&&(t.disabled=e.disabled),e.className&&(t.className=e.className),t}__appendValueItemElement(e,t){t.appendChild(e),e.removeAttribute("tabindex"),e.removeAttribute("aria-selected"),e.removeAttribute("role"),e.removeAttribute("focused"),e.removeAttribute("focus-ring"),e.removeAttribute("active"),e.setAttribute("id",this._itemId)}_accessibleNameChanged(e){this._srLabelController.setLabel(e),this._setCustomAriaLabelledBy(e?this._srLabelController.defaultId:null)}_accessibleNameRefChanged(e){this._setCustomAriaLabelledBy(e)}_setCustomAriaLabelledBy(e){let t=this._getLabelIdWithItemId(e);this._fieldAriaController.setLabelId(t,!0)}_getLabelIdWithItemId(e){let i=(this._items?this._items[this._menuElement.selected]:!1)||this.placeholder?this._itemId:"";return e?`${e} ${i}`.trim():null}__updateValueButton(){let e=this.focusElement;if(!e)return;e.innerHTML="";let t=this._items?this._items[this._menuElement.selected]:void 0;if(e.removeAttribute("placeholder"),this._hasContent(t))this.__attachSelectedItem(t);else if(this.placeholder){let a=this.__createItemElement({label:this.placeholder});this.__appendValueItemElement(a,e),e.setAttribute("placeholder","")}!this._valueChanging&&t&&(this._selectedChanging=!0,this.value=t.value||"",this.__dispatchChangePending&&this.__dispatchChange(),delete this._selectedChanging);let i=t||this.placeholder?{newId:this._itemId}:{oldId:this._itemId};Bt(e,"aria-labelledby",i),(this.accessibleName||this.accessibleNameRef)&&this._setCustomAriaLabelledBy(this.accessibleNameRef||this._srLabelController.defaultId)}_hasContent(e){if(!e)return!1;let t=!!(e.hasAttribute("label")?e.getAttribute("label"):e.textContent.trim()),i=e.childElementCount>0;return t||i}_updateSelectedItem(e,t,i){if(t){let a=e==null?e:e.toString();this._menuElement.selected=t.reduce((s,n,l)=>s===void 0&&n.value===a?l:s,void 0),this._selectedChanging||(this._valueChanging=!0,this.__updateValueButton(),delete this._valueChanging)}else i&&this.__updateValueButton()}_shouldRemoveFocus(e){return!this.contains(e.relatedTarget)}_setFocused(e){super._setFocused(e),!e&&document.hasFocus()&&this._requestValidation()}checkValidity(){return!this.required||this.readonly||!!this.value}__defaultRenderer(e,t){if(!this.items||this.items.length===0){e.textContent="";return}let i=e.firstElementChild;i||(i=document.createElement("vaadin-select-list-box"),e.appendChild(i)),i.textContent="",this.items.forEach(a=>{i.appendChild(this.__createItemElement(a))})}__dispatchChange(){this._requestValidation(),this.dispatchEvent(new CustomEvent("change",{bubbles:!0})),this.__dispatchChangePending=!1}};var at=class extends Bi(A(f(p(v(c))))){static get is(){return"vaadin-select"}static get styles(){return[S,Ii,Fi]}render(){return d`
      <div class="vaadin-select-container">
        <div part="label" @click="${this._onClick}">
          <slot name="label"></slot>
          <span part="required-indicator" aria-hidden="true" @click="${this.focus}"></span>
        </div>

        <vaadin-input-container
          part="input-field"
          .readonly="${this.readonly}"
          .disabled="${this.disabled}"
          .invalid="${this.invalid}"
          theme="${D(this._theme)}"
          @click="${this._onClick}"
        >
          <slot name="prefix" slot="prefix"></slot>
          <slot name="value"></slot>
          <div
            part="field-button toggle-button"
            slot="suffix"
            aria-hidden="true"
            @mousedown="${this._onToggleMouseDown}"
          ></div>
        </vaadin-input-container>

        <div part="helper-text">
          <slot name="helper"></slot>
        </div>

        <div part="error-message">
          <slot name="error-message"></slot>
        </div>
      </div>

      <vaadin-select-overlay
        id="overlay"
        .owner="${this}"
        .positionTarget="${this._inputContainer}"
        .opened="${this.opened}"
        .withBackdrop="${this._phone}"
        .renderer="${this.renderer||this.__defaultRenderer}"
        ?phone="${this._phone}"
        theme="${D(this._theme)}"
        ?no-vertical-overlap="${this.noVerticalOverlap}"
        exportparts="backdrop, overlay, content"
        @opened-changed="${this._onOpenedChanged}"
        @vaadin-overlay-open="${this._onOverlayOpen}"
      >
        <slot name="overlay"></slot>
      </vaadin-select-overlay>

      <slot name="tooltip"></slot>
      <div class="sr-only">
        <slot name="sr-label"></slot>
      </div>
    `}_onOpenedChanged(r){this.opened=r.detail.value}_onOverlayOpen(){this._menuElement&&this._menuElement.focus({focusVisible:z()})}};h(at);var $i=m`
  :host {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: var(--vaadin-tab-gap, var(--vaadin-gap-s));
    padding: var(--vaadin-tab-padding, var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container));
    cursor: var(--vaadin-clickable-cursor);
    font-size: var(--vaadin-tab-font-size, 1em);
    font-weight: var(--vaadin-tab-font-weight, 500);
    line-height: var(--vaadin-tab-line-height, inherit);
    color: var(--vaadin-tab-text-color, var(--vaadin-text-color-secondary));
    background: var(--vaadin-tab-background, transparent);
    border-radius: var(--vaadin-tab-border-radius, var(--vaadin-radius-m));
    border: var(--vaadin-tab-border-width, 0) solid var(--vaadin-tab-border-color, var(--vaadin-border-color-secondary));
    -webkit-tap-highlight-color: transparent;
    -webkit-user-select: none;
    user-select: none;
    touch-action: manipulation;
    position: relative;
  }

  :host([hidden]) {
    display: none !important;
  }

  :host([orientation='vertical']) {
    justify-content: start;
  }

  :host([selected]) {
    --vaadin-tab-background: var(--vaadin-background-container);
    --vaadin-tab-text-color: var(--vaadin-text-color);
  }

  :host([disabled]) {
    cursor: var(--vaadin-disabled-cursor);
    opacity: 0.5;
  }

  :host(:is([focus-ring], :focus-visible)) {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-focus-ring-width) * -1);
  }

  slot {
    gap: inherit;
    align-items: inherit;
    justify-content: inherit;
  }

  ::slotted(a) {
    color: inherit !important;
    cursor: inherit !important;
    text-decoration: inherit !important;
    display: flex;
    align-items: inherit;
    justify-content: inherit;
    gap: inherit;
  }

  ::slotted(a)::before {
    content: '';
    position: absolute;
    inset: 0;
  }

  @media (forced-colors: active) {
    :host {
      border: 1px solid Canvas;
    }

    :host([selected]) {
      color: Highlight;
      border-color: Highlight;
    }

    :host([disabled]) {
      color: GrayText;
      opacity: 1;
    }
  }
`;var ot=class extends Se(f(A(p(v(c))))){static get is(){return"vaadin-tab"}static get styles(){return $i}render(){return d`
      <slot></slot>
      <slot name="tooltip"></slot>
    `}ready(){super.ready(),this.setAttribute("role","tab"),this._tooltipController=new E(this),this.addController(this._tooltipController)}_onKeyUp(r){let e=this.hasAttribute("active");if(super._onKeyUp(r),e){let t=this.querySelector("a");t&&t.click()}}};h(ot);var Pi=m`
  :host {
    display: flex;
    max-width: 100%;
    max-height: 100%;
    position: relative;
    box-sizing: border-box;
    padding: var(--vaadin-tabs-padding);
    background: var(--vaadin-tabs-background);
    border-radius: var(--vaadin-tabs-border-radius);
    border: var(--vaadin-tabs-border-width, 0) solid
      var(--vaadin-tabs-border-color, var(--vaadin-border-color-secondary));
  }

  :host([hidden]) {
    display: none !important;
  }

  :host([orientation='vertical']) {
    flex-direction: column;
  }

  [part='tabs'] {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: var(--vaadin-tabs-gap, var(--vaadin-gap-s));
    animation: enable-smooth-scroll-after-first-render 1s both;
    width: 100%;
    height: 100%;
  }

  :host([overflow]) [part='tabs'] {
    overflow: auto;
  }

  :host([overflow][orientation='horizontal']) [part='tabs'] {
    overscroll-behavior: contain auto;
  }

  :host([overflow][orientation='vertical']) [part='tabs'] {
    overscroll-behavior: auto contain;
  }

  @keyframes enable-smooth-scroll-after-first-render {
    100% {
      scroll-behavior: smooth;
    }
  }

  :host([orientation='horizontal']) [part='tabs'] {
    flex-direction: row;
    scrollbar-width: none;
  }

  /* scrollbar-width is supported in Safari 18.2, use the following for earlier */
  :host([orientation='horizontal']) [part='tabs']::-webkit-scrollbar {
    display: none;
  }

  [part$='button'] {
    position: absolute;
    z-index: 1;
    pointer-events: none;
    opacity: 0;
    cursor: var(--vaadin-clickable-cursor);
    box-sizing: border-box;
    height: 100%;
    padding: var(--vaadin-tab-padding, var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container));
    background: var(--vaadin-background-color);
    display: flex;
    align-items: center;
    justify-content: center;
    -webkit-tap-highlight-color: transparent;
    -webkit-user-select: none;
    user-select: none;
    touch-action: manipulation;
  }

  [part='forward-button'] {
    inset-inline-end: 0;
  }

  :host([overflow~='start']) [part='back-button'],
  :host([overflow~='end']) [part='forward-button'] {
    pointer-events: auto;
    opacity: 1;
  }

  [part$='button']::before {
    content: '';
    display: block;
    width: var(--vaadin-icon-size, 1lh);
    height: var(--vaadin-icon-size, 1lh);
    background: currentColor;
    mask: var(--_vaadin-icon-chevron-down) 50% / var(--vaadin-icon-visual-size, 100%) no-repeat;
    rotate: 90deg;
  }

  [part='forward-button']::before {
    rotate: -90deg;
  }

  :host(:is([orientation='vertical'], [theme~='hide-scroll-buttons'])) [part$='button'] {
    display: none;
  }

  @media (pointer: coarse) {
    :host(:not([theme~='show-scroll-buttons'])) [part$='button'] {
      display: none;
    }
  }

  :host([dir='rtl']) [part$='button']::before {
    scale: 1 -1;
  }

  @media (forced-colors: active) {
    [part$='button']::before {
      background: CanvasText;
    }
  }
`;var Li=o=>class extends _e(Ie(o)){static get properties(){return{orientation:{value:"horizontal",type:String,reflectToAttribute:!0,sync:!0},selected:{value:0,type:Number,reflectToAttribute:!0}}}static get observers(){return["__tabsItemsChanged(items)"]}constructor(){super(),this.__itemsResizeObserver=new ResizeObserver(()=>{setTimeout(()=>this._updateOverflow())})}get _scrollOffset(){return this._vertical?this._scrollerElement.offsetHeight:this._scrollerElement.offsetWidth}get _scrollerElement(){return this.$.scroll}get __direction(){return!this._vertical&&this.__isRTL?1:-1}ready(){super.ready(),this._updateOverflow(),this._scrollerElement.addEventListener("scroll",()=>this._updateOverflow()),this.setAttribute("role","tablist")}_onResize(){this._updateOverflow()}__tabsItemsChanged(e){this.__itemsResizeObserver.disconnect(),(e||[]).forEach(t=>{this.__itemsResizeObserver.observe(t)}),this._updateOverflow()}_scrollToItem(e){let t=this.items[e],i=t.getBoundingClientRect(),a=this._scrollerElement.getBoundingClientRect(),s=this._vertical?10:20;if(this._vertical)i.bottom>a.bottom-s&&(this._scrollerElement.scrollTop=t.offsetTop-(a.height-i.height)+s),i.top<a.top+s&&(this._scrollerElement.scrollTop=t.offsetTop-s);else{let n=this.shadowRoot.querySelector('[part="back-button"]').offsetWidth,l=this.shadowRoot.querySelector('[part="forward-button"]').offsetWidth;i.right>a.right-l-s&&(this._scrollerElement.scrollLeft=t.offsetLeft-(a.width-i.width)+l+s),i.left<a.left+n+s&&(this._scrollerElement.scrollLeft=t.offsetLeft-n-s)}}_scrollForward(e){(e===void 0||this.__scrollTimer===void 0)&&this._scroll(this.__direction*(this._scrollOffset/2)*-1)}_scrollBack(e){(e===void 0||this.__scrollTimer===void 0)&&this._scroll(this.__direction*(this._scrollOffset/2))}_startScrollForward(e){e.button===0&&(this._scrollForward(),this.__scrollTimer=setInterval(this._scrollForward.bind(this),300))}_startScrollBack(e){e.button===0&&(this._scrollBack(),this.__scrollTimer=setInterval(this._scrollBack.bind(this),300))}_stopScroll(){clearTimeout(this.__scrollTimer)}_updateOverflow(){let e=this._vertical?this._scrollerElement.scrollTop:fe(this._scrollerElement,this.getAttribute("dir")),t=this._vertical?this._scrollerElement.scrollHeight:this._scrollerElement.scrollWidth,i=Math.floor(e)>1?"start":"";Math.ceil(e)<Math.ceil(t-this._scrollOffset)&&(i+=" end"),this.__direction===1&&(i=i.replace(/start|end/giu,a=>a==="start"?"end":"start")),i?this.setAttribute("overflow",i.trim()):this.removeAttribute("overflow")}};var st=class extends Li(A(f(p(v(c))))){static get is(){return"vaadin-tabs"}static get styles(){return Pi}render(){return d`
      <div
        @pointerdown="${this._startScrollBack}"
        @pointerup="${this._stopScroll}"
        @pointerleave="${this._stopScroll}"
        @pointercancel="${this._stopScroll}"
        @click="${this._scrollBack}"
        part="back-button"
        aria-hidden="true"
      ></div>

      <div id="scroll" part="tabs" tabindex="-1">
        <slot></slot>
      </div>

      <div
        @pointerdown="${this._startScrollForward}"
        @pointerup="${this._stopScroll}"
        @pointerleave="${this._stopScroll}"
        @pointercancel="${this._stopScroll}"
        @click="${this._scrollForward}"
        part="forward-button"
        aria-hidden="true"
      ></div>
    `}};h(st);var Oi=m`
  :host {
    height: auto;
  }

  [part='input-field'] {
    overflow: auto;
    scroll-padding: var(
      --vaadin-input-field-padding,
      var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container)
    );
  }

  ::slotted(textarea) {
    resize: none;
    white-space: pre-wrap;
  }

  [part='input-field'] ::slotted(:not(textarea)),
  [part~='clear-button'] {
    align-self: flex-start;
    position: sticky;
    top: 0;
  }

  [part~='clear-button'] {
    top: min(0px, (24px - 1lh) / -2);
  }

  /* Workaround https://bugzilla.mozilla.org/show_bug.cgi?id=1739079 */
  :host([disabled]) ::slotted(textarea) {
    user-select: none;
  }
`;var Be=o=>class extends X(o){static get properties(){return{autocomplete:{type:String},autocorrect:{type:String,reflectToAttribute:!0},autocapitalize:{type:String,reflectToAttribute:!0}}}static get delegateAttrs(){return[...super.delegateAttrs,"autocapitalize","autocomplete","autocorrect"]}_inputElementChanged(e){super._inputElementChanged(e),e&&(e.value&&e.value!==this.value&&(console.warn(`Please define value on the <${this.localName}> component!`),e.value=""),this.value&&(e.value=this.value))}_setFocused(e){super._setFocused(e),!e&&document.hasFocus()&&this._requestValidation()}_onInput(e){super._onInput(e),this.invalid&&this._requestValidation()}_valueChanged(e,t){super._valueChanged(e,t),t!==void 0&&this.invalid&&this._requestValidation()}};var $e=class extends k{constructor(r,e){super(r,"textarea","textarea",{initializer:(t,i)=>{let a=i.getAttribute("value");a&&(t.value=a);let s=i.getAttribute("name");s&&t.setAttribute("name",s),t.id=this.defaultId,typeof e=="function"&&e(t)},useUniqueId:!0})}};var Vi=o=>class extends _e(Be(o)){static get properties(){return{maxlength:{type:Number},minlength:{type:Number},pattern:{type:String},minRows:{type:Number,value:2,observer:"__minRowsChanged"},maxRows:{type:Number}}}static get delegateAttrs(){return[...super.delegateAttrs,"maxlength","minlength","pattern"]}static get constraints(){return[...super.constraints,"maxlength","minlength","pattern"]}static get observers(){return["__updateMinHeight(minRows, inputElement)","__updateMaxHeight(maxRows, inputElement, _inputField)"]}get clearElement(){return this.$.clearButton}_onResize(){this._updateHeight(),this.__scrollPositionUpdated()}_onScroll(){this.__scrollPositionUpdated()}ready(){super.ready(),this.__textAreaController=new $e(this,e=>{this._setInputElement(e),this._setFocusElement(e),this.stateTarget=e,this.ariaTarget=e}),this.addController(this.__textAreaController),this.addController(new B(this.inputElement,this._labelController)),this._inputField=this.shadowRoot.querySelector("[part=input-field]"),this._inputField.addEventListener("wheel",e=>{let t=this._inputField.scrollTop;this._inputField.scrollTop+=e.deltaY,t!==this._inputField.scrollTop&&(e.preventDefault(),this.__scrollPositionUpdated())}),this._updateHeight(),this.__scrollPositionUpdated()}__scrollPositionUpdated(){this._inputField.style.setProperty("--_text-area-vertical-scroll-position","0px"),this._inputField.style.setProperty("--_text-area-vertical-scroll-position",`${this._inputField.scrollTop}px`)}_valueChanged(e,t){super._valueChanged(e,t),this._updateHeight()}_updateHeight(){let e=this.inputElement,t=this._inputField;if(!e||!t)return;let i=t.scrollTop,a=parseFloat(e.style.height),s=this.value?this.value.length:0;if(this._oldValueLength>=s){let l=getComputedStyle(t).height,u=getComputedStyle(e).width;t.style.height=l,e.style.maxWidth=u,e.style.alignSelf="flex-start",e.style.height="auto"}this._oldValueLength=s;let n=e.scrollHeight;Math.abs(n-a)<=1?e.style.height=`${a}px`:n>e.clientHeight&&(e.style.height=`${n}px`),e.style.removeProperty("max-width"),e.style.removeProperty("align-self"),t.style.removeProperty("height"),t.scrollTop=i,this.__updateMaxHeight(this.maxRows)}__updateMinHeight(e){this.inputElement&&this.inputElement===this.__textAreaController.defaultNode&&(this.inputElement.rows=Math.max(e,1))}__updateMaxHeight(e){if(!(!this._inputField||!this.inputElement))if(e){let t=getComputedStyle(this.inputElement),i=getComputedStyle(this._inputField),s=parseFloat(t.lineHeight)*e,n=parseFloat(t.paddingTop)+parseFloat(t.paddingBottom)+parseFloat(t.marginTop)+parseFloat(t.marginBottom)+parseFloat(i.borderTopWidth)+parseFloat(i.borderBottomWidth)+parseFloat(i.paddingTop)+parseFloat(i.paddingBottom),l=Math.ceil(s+n);this._inputField.style.setProperty("max-height",`${l}px`)}else this._inputField.style.removeProperty("max-height")}__minRowsChanged(e){e<1&&console.warn("<vaadin-text-area> minRows must be at least 1.")}scrollToStart(){this._inputField.scrollTop=0}scrollToEnd(){this._inputField.scrollTop=this._inputField.scrollHeight}checkValidity(){if(!super.checkValidity())return!1;if(!this.pattern||!this.inputElement.value)return!0;try{let e=this.inputElement.value.match(this.pattern);return e?e[0]===e.input:!1}catch{return!0}}};var nt=class extends Vi(f(A(p(v(c))))){static get is(){return"vaadin-text-area"}static get styles(){return[S,Oi]}render(){return d`
      <div class="vaadin-text-area-container">
        <div part="label">
          <slot name="label"></slot>
          <span part="required-indicator" aria-hidden="true" @click="${this.focus}"></span>
        </div>

        <vaadin-input-container
          part="input-field"
          .readonly="${this.readonly}"
          .disabled="${this.disabled}"
          .invalid="${this.invalid}"
          theme="${D(this._theme)}"
          @scroll="${this._onScroll}"
        >
          <slot name="prefix" slot="prefix"></slot>
          <slot name="textarea"></slot>
          <slot name="suffix" slot="suffix"></slot>
          <div id="clearButton" part="field-button clear-button" slot="suffix" aria-hidden="true"></div>
        </vaadin-input-container>

        <div part="helper-text">
          <slot name="helper"></slot>
        </div>

        <div part="error-message">
          <slot name="error-message"></slot>
        </div>

        <slot name="tooltip"></slot>
      </div>
    `}ready(){super.ready(),this._tooltipController=new E(this),this._tooltipController.setPosition("top"),this._tooltipController.setAriaTarget(this.inputElement),this.addController(this._tooltipController)}};h(nt);var zi=o=>class extends Be(o){static get properties(){return{maxlength:{type:Number},minlength:{type:Number},pattern:{type:String}}}static get delegateAttrs(){return[...super.delegateAttrs,"maxlength","minlength","pattern"]}static get constraints(){return[...super.constraints,"maxlength","minlength","pattern"]}constructor(){super(),this._setType("text")}get clearElement(){return this.$.clearButton}ready(){super.ready(),this.addController(new H(this,e=>{this._setInputElement(e),this._setFocusElement(e),this.stateTarget=e,this.ariaTarget=e})),this.addController(new B(this.inputElement,this._labelController))}};var lt=class extends zi(f(A(p(v(c))))){static get is(){return"vaadin-text-field"}static get styles(){return[S]}render(){return d`
      <div class="vaadin-field-container">
        <div part="label">
          <slot name="label"></slot>
          <span part="required-indicator" aria-hidden="true" @click="${this.focus}"></span>
        </div>

        <vaadin-input-container
          part="input-field"
          .readonly="${this.readonly}"
          .disabled="${this.disabled}"
          .invalid="${this.invalid}"
          theme="${D(this._theme)}"
        >
          <slot name="prefix" slot="prefix"></slot>
          <slot name="input"></slot>
          ${this._renderSuffix()}
        </vaadin-input-container>

        <div part="helper-text">
          <slot name="helper"></slot>
        </div>

        <div part="error-message">
          <slot name="error-message"></slot>
        </div>
        <slot name="tooltip"></slot>
      </div>
    `}ready(){super.ready(),this._tooltipController=new E(this),this._tooltipController.setPosition("top"),this._tooltipController.setAriaTarget(this.inputElement),this.addController(this._tooltipController)}_renderSuffix(){return d`
      <slot name="suffix" slot="suffix"></slot>
      <div id="clearButton" part="field-button clear-button" slot="suffix" aria-hidden="true"></div>
    `}};h(lt);var Ri=m`
  :host {
    display: inline-flex;
  }

  :host::before {
    background: var(--vaadin-upload-icon-color, currentColor);
    content: '';
    display: inline-block;
    flex: none;
    height: var(--vaadin-icon-size, 1lh);
    mask: var(--_vaadin-icon-upload) 50% / var(--vaadin-icon-visual-size, 100%) no-repeat;
    width: var(--vaadin-icon-size, 1lh);
  }

  :host([hidden]) {
    display: none !important;
  }

  @media (forced-colors: active) {
    :host::before {
      background: CanvasText;
    }
  }
`;var dt=class extends f(v(c)){static get is(){return"vaadin-upload-icon"}static get styles(){return Ri}static get lumoInjector(){return{...super.lumoInjector,includeBaseStyles:!0}}render(){return d``}};h(dt);var Ui=document.createElement("template");Ui.innerHTML=`
  <style>
    @font-face {
      font-family: 'vaadin-upload-icons';
      src: url(data:application/font-woff;charset=utf-8;base64,d09GRgABAAAAAAasAAsAAAAABmAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAABPUy8yAAABCAAAAGAAAABgDxIF5mNtYXAAAAFoAAAAVAAAAFQXVtKMZ2FzcAAAAbwAAAAIAAAACAAAABBnbHlmAAABxAAAAfQAAAH0bBJxYWhlYWQAAAO4AAAANgAAADYPD267aGhlYQAAA/AAAAAkAAAAJAfCA8tobXR4AAAEFAAAACgAAAAoHgAAx2xvY2EAAAQ8AAAAFgAAABYCSgHsbWF4cAAABFQAAAAgAAAAIAAOADVuYW1lAAAEdAAAAhYAAAIWmmcHf3Bvc3QAAAaMAAAAIAAAACAAAwAAAAMDtwGQAAUAAAKZAswAAACPApkCzAAAAesAMwEJAAAAAAAAAAAAAAAAAAAAARAAAAAAAAAAAAAAAAAAAAAAQAAA6QUDwP/AAEADwABAAAAAAQAAAAAAAAAAAAAAIAAAAAAAAwAAAAMAAAAcAAEAAwAAABwAAwABAAAAHAAEADgAAAAKAAgAAgACAAEAIOkF//3//wAAAAAAIOkA//3//wAB/+MXBAADAAEAAAAAAAAAAAAAAAEAAf//AA8AAQAAAAAAAAAAAAIAADc5AQAAAAABAAAAAAAAAAAAAgAANzkBAAAAAAEAAAAAAAAAAAACAAA3OQEAAAAAAgAA/8AEAAPAABkAMgAAEz4DMzIeAhczLgMjIg4CBycRIScFIRcOAyMiLgInIx4DMzI+AjcXphZGWmo6SH9kQwyADFiGrmJIhXJbIEYBAFoDWv76YBZGXGw8Rn5lRQyADFmIrWBIhHReIkYCWjJVPSIyVnVDXqN5RiVEYTxG/wBa2loyVT0iMlZ1Q16jeUYnRWE5RgAAAAABAIAAAAOAA4AAAgAAExEBgAMAA4D8gAHAAAAAAwAAAAAEAAOAAAIADgASAAAJASElIiY1NDYzMhYVFAYnETMRAgD+AAQA/gAdIyMdHSMjXYADgPyAgCMdHSMjHR0jwAEA/wAAAQANADMD5gNaAAUAACUBNwUBFwHT/jptATMBppMzAU2a4AIgdAAAAAEAOv/6A8YDhgALAAABJwkBBwkBFwkBNwEDxoz+xv7GjAFA/sCMAToBOoz+wAL6jP7AAUCM/sb+xowBQP7AjAE6AAAAAwAA/8AEAAPAAAcACwASAAABFSE1IREhEQEjNTMJAjMRIRECwP6A/sAEAP0AgIACQP7A/sDAAQABQICA/oABgP8AgAHAAUD+wP6AAYAAAAABAAAAAQAAdhiEdV8PPPUACwQAAAAAANX4FR8AAAAA1fgVHwAA/8AEAAPAAAAACAACAAAAAAAAAAEAAAPA/8AAAAQAAAAAAAQAAAEAAAAAAAAAAAAAAAAAAAAKBAAAAAAAAAAAAAAAAgAAAAQAAAAEAACABAAAAAQAAA0EAAA6BAAAAAAAAAAACgAUAB4AagB4AJwAsADSAPoAAAABAAAACgAzAAMAAAAAAAIAAAAAAAAAAAAAAAAAAAAAAAAADgCuAAEAAAAAAAEAEwAAAAEAAAAAAAIABwDMAAEAAAAAAAMAEwBaAAEAAAAAAAQAEwDhAAEAAAAAAAUACwA5AAEAAAAAAAYAEwCTAAEAAAAAAAoAGgEaAAMAAQQJAAEAJgATAAMAAQQJAAIADgDTAAMAAQQJAAMAJgBtAAMAAQQJAAQAJgD0AAMAAQQJAAUAFgBEAAMAAQQJAAYAJgCmAAMAAQQJAAoANAE0dmFhZGluLXVwbG9hZC1pY29ucwB2AGEAYQBkAGkAbgAtAHUAcABsAG8AYQBkAC0AaQBjAG8AbgBzVmVyc2lvbiAxLjAAVgBlAHIAcwBpAG8AbgAgADEALgAwdmFhZGluLXVwbG9hZC1pY29ucwB2AGEAYQBkAGkAbgAtAHUAcABsAG8AYQBkAC0AaQBjAG8AbgBzdmFhZGluLXVwbG9hZC1pY29ucwB2AGEAYQBkAGkAbgAtAHUAcABsAG8AYQBkAC0AaQBjAG8AbgBzUmVndWxhcgBSAGUAZwB1AGwAYQBydmFhZGluLXVwbG9hZC1pY29ucwB2AGEAYQBkAGkAbgAtAHUAcABsAG8AYQBkAC0AaQBjAG8AbgBzRm9udCBnZW5lcmF0ZWQgYnkgSWNvTW9vbi4ARgBvAG4AdAAgAGcAZQBuAGUAcgBhAHQAZQBkACAAYgB5ACAASQBjAG8ATQBvAG8AbgAuAAAAAwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==) format('woff');
      font-weight: normal;
      font-style: normal;
    }
  </style>
`;document.head.appendChild(Ui.content);var Hi=m`
  :host {
    display: block;
    width: 100%; /* prevent collapsing inside non-stretching column flex */
    height: var(--vaadin-progress-bar-height, 0.5lh);
    contain: layout size;
  }

  :host([hidden]) {
    display: none !important;
  }

  [part='bar'] {
    box-sizing: border-box;
    height: 100%;
    --_padding: var(--vaadin-progress-bar-padding, 0px);
    padding: var(--_padding);
    background: var(--vaadin-progress-bar-background, var(--vaadin-background-container));
    border-radius: var(--vaadin-progress-bar-border-radius, var(--vaadin-radius-m));
    border: var(--vaadin-progress-bar-border-width, 1px) solid
      var(--vaadin-progress-bar-border-color, var(--vaadin-border-color-secondary));
  }

  [part='value'] {
    box-sizing: border-box;
    height: 100%;
    width: calc(var(--vaadin-progress-value) * 100%);
    background: var(--vaadin-progress-bar-value-background, var(--vaadin-border-color));
    border-radius: calc(
      var(--vaadin-progress-bar-border-radius, var(--vaadin-radius-m)) - var(
          --vaadin-progress-bar-border-width,
          1px
        ) - var(--_padding)
    );
    transition: width 150ms;
  }

  /* Indeterminate progress */
  :host([indeterminate]) [part='value'] {
    --_w-min: clamp(8px, 5%, 16px);
    --_w-max: clamp(16px, 20%, 128px);
    animation: indeterminate var(--vaadin-progress-bar-animation-duration, 1s) linear infinite alternate;
    width: var(--_w-min);
  }

  :host([indeterminate][aria-valuenow]) [part='value'] {
    animation-delay: 150ms;
  }

  @keyframes indeterminate {
    0% {
      animation-timing-function: ease-in;
    }

    20% {
      margin-inline-start: 0%;
      width: var(--_w-max);
    }

    50% {
      margin-inline-start: calc(50% - var(--_w-max) / 2);
    }

    80% {
      width: var(--_w-max);
      margin-inline-start: calc(100% - var(--_w-max));
      animation-timing-function: ease-out;
    }

    100% {
      width: var(--_w-min);
      margin-inline-start: calc(100% - var(--_w-min));
    }
  }

  @keyframes indeterminate-reduced {
    100% {
      opacity: 0.2;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    [part='value'] {
      transition: none;
    }

    :host([indeterminate]) [part='value'] {
      width: 25%;
      animation: indeterminate-reduced 2s linear infinite alternate;
    }
  }

  @media (forced-colors: active) {
    [part='bar'] {
      border-width: max(1px, var(--vaadin-progress-bar-border-width));
    }

    [part='value'] {
      background: CanvasText !important;
    }
  }
`;var Ni=o=>class extends o{static get properties(){return{value:{type:Number,observer:"_valueChanged"},min:{type:Number,value:0,observer:"_minChanged"},max:{type:Number,value:1,observer:"_maxChanged"},indeterminate:{type:Boolean,value:!1,reflectToAttribute:!0}}}static get observers(){return["_normalizedValueChanged(value, min, max)"]}ready(){super.ready(),this.setAttribute("role","progressbar")}_normalizedValueChanged(e,t,i){let a=this._normalizeValue(e,t,i);this.style.setProperty("--vaadin-progress-value",a)}_valueChanged(e){this.setAttribute("aria-valuenow",e)}_minChanged(e){this.setAttribute("aria-valuemin",e)}_maxChanged(e){this.setAttribute("aria-valuemax",e)}_normalizeValue(e,t,i){let a;return!e&&e!==0?a=0:t>=i?a=1:(a=(e-t)/(i-t),a=Math.min(Math.max(a,0),1)),a}};var ht=class extends Ni(A(f(p(v(c))))){static get is(){return"vaadin-progress-bar"}static get styles(){return Hi}render(){return d`
      <div part="bar">
        <div part="value"></div>
      </div>
    `}};h(ht);var ji=[Et,m`
    :host {
      align-items: baseline;
      display: grid;
      gap: var(--vaadin-upload-file-gap, var(--vaadin-gap-s));
      grid-template-columns: var(--vaadin-icon-size, 1lh) minmax(0, 1fr) auto;
      padding: var(--vaadin-upload-file-padding, var(--vaadin-padding-s));
      border-radius: var(--vaadin-upload-file-border-radius, var(--vaadin-radius-m));
    }

    :host(:focus-visible) {
      outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
      outline-offset: calc(var(--vaadin-focus-ring-width) * -1);
    }

    [hidden] {
      display: none;
    }

    [part='thumbnail'],
    [part='loader'] {
      display: none;
      grid-column: 1;
    }

    [part='done-icon']:not([hidden]),
    [part='warning-icon']:not([hidden]) {
      display: flex;
      grid-column: 1;
    }

    [part='loader']::before,
    [part='done-icon']::before,
    [part='warning-icon']::before {
      content: '\\2003' / '';
      display: inline-flex;
      align-items: center;
      flex: none;
      height: var(--vaadin-icon-size, 1lh);
      width: var(--vaadin-icon-size, 1lh);
    }

    [part='loader']::before {
      margin: calc(var(--vaadin-spinner-width, 2px) * -1);
    }

    :is([part$='icon'], [part$='button'])::before {
      mask-size: var(--vaadin-icon-visual-size, 100%);
      mask-position: 50%;
      mask-repeat: no-repeat;
    }

    [part='done-icon']::before {
      background: var(--vaadin-upload-file-done-color, currentColor);
      mask-image: var(--_vaadin-icon-checkmark);
    }

    [part='warning-icon']::before {
      background: var(--vaadin-upload-file-warning-color, currentColor);
      mask-image: var(--_vaadin-icon-warn);
    }

    [part='meta'] {
      grid-column: 2;

      & > div {
        cursor: inherit;
      }
    }

    [part='name'] {
      color: var(--vaadin-upload-file-name-color, var(--vaadin-text-color));
      font-size: var(--vaadin-upload-file-name-font-size, inherit);
      font-weight: var(--vaadin-upload-file-name-font-weight, inherit);
      line-height: var(--vaadin-upload-file-name-line-height, inherit);
      overflow: hidden;
      text-overflow: ellipsis;
    }

    [part='status'] {
      color: var(--vaadin-upload-file-status-color, var(--vaadin-text-color-secondary));
      font-size: var(--vaadin-upload-file-status-font-size, inherit);
      font-weight: var(--vaadin-upload-file-status-font-weight, inherit);
      line-height: var(--vaadin-upload-file-status-line-height, inherit);
    }

    [part='error'] {
      color: var(--vaadin-upload-file-error-color, var(--vaadin-text-color));
      font-size: var(--vaadin-upload-file-error-font-size, inherit);
      font-weight: var(--vaadin-upload-file-error-font-weight, inherit);
      line-height: var(--vaadin-upload-file-error-line-height, inherit);
    }

    [part='commands'] {
      display: flex;
      align-items: center;
      gap: var(--vaadin-gap-xs);
      height: var(--vaadin-icon-size, 1lh);
      align-self: center;
    }

    button {
      background: var(--vaadin-upload-file-button-background, transparent);
      border: var(--vaadin-upload-file-button-border-width, 0) solid
        var(--vaadin-upload-file-button-border-color, var(--vaadin-border-color-secondary));
      border-radius: var(--vaadin-upload-file-button-border-radius, var(--vaadin-radius-m));
      color: var(--vaadin-upload-file-button-text-color, var(--vaadin-text-color));
      cursor: var(--vaadin-clickable-cursor);
      flex-shrink: 0;
      font: inherit;
      /* Ensure minimum click target (WCAG) */
      padding: var(--vaadin-upload-file-button-padding, max(0px, (24px - var(--vaadin-icon-size, 1lh)) / 2));
    }

    button:focus-visible {
      outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    }

    [part='start-button']::before,
    [part='retry-button']::before,
    [part='remove-button']::before {
      background: currentColor;
      content: '\\2003' / '';
      display: flex;
      align-items: center;
      height: var(--vaadin-icon-size, 1lh);
      width: var(--vaadin-icon-size, 1lh);
    }

    [part='start-button']::before {
      mask-image: var(--_vaadin-icon-play);
    }

    [part='retry-button']::before {
      mask-image: var(--_vaadin-icon-refresh);
    }

    [part='remove-button']::before {
      mask-image: var(--_vaadin-icon-cross);
    }

    ::slotted([slot='progress']) {
      grid-column: 2 / -1;
      width: auto;
    }

    :host([complete]) ::slotted([slot='progress']),
    :host([error]) ::slotted([slot='progress']) {
      display: none;
    }

    /* THUMBNAILS VARIANT */

    :host([theme~='thumbnails']) {
      grid-template-columns: max-content 1fr max-content;
      align-items: center;
      background: var(--vaadin-background-container);
      padding: 0;
      contain: content;

      & [part] {
        grid-row: 1;
      }

      & [part='thumbnail'],
      & [part$='icon'] {
        grid-column: 1;
        align-self: stretch;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: 0.2s opacity linear;
        background: var(--vaadin-background-container-strong);
        padding: var(--vaadin-upload-file-padding, var(--vaadin-padding-s));
        contain: content;

        &[hidden] {
          opacity: 0;
        }
      }

      & [part='thumbnail'] > img {
        object-fit: cover;
        position: absolute;
        width: 100%;
        height: 100%;
      }

      & [part='loader']:not([hidden]) {
        place-self: center;
        display: flex;
      }

      & [part='done-icon']::before {
        background: var(--vaadin-upload-file-done-color, currentColor);
        mask-image: var(--_vaadin-icon-file);
      }

      & [part='meta'] {
        padding: var(--vaadin-upload-file-padding, var(--vaadin-padding-s));
        padding-inline: 0;
      }

      & [part='name'] {
        word-break: break-all;
      }

      & [part='commands'] {
        padding: var(--vaadin-upload-padding, var(--vaadin-padding-s));
        padding-inline-start: 0;
      }

      & [part='status'] {
        clip-path: inset(50%);
        width: 1px;
        height: 1px;
        margin: 0;
        overflow: hidden;
        position: absolute;
        white-space: nowrap;
      }

      & [part='error'] {
        font-size: 0.875em;
        line-height: 1.25;
      }

      & ::slotted([slot='progress']) {
        grid-row: auto;
        grid-column: auto;
        position: absolute;
        inset: 0;
        opacity: 0.3;
        pointer-events: none;
        --vaadin-progress-bar-height: auto;
        --vaadin-progress-bar-border-width: 0px;
        --vaadin-progress-bar-border-radius: 0px;
        --vaadin-progress-bar-background: transparent;
      }

      & ::slotted([slot='progress'][indeterminate]) {
        opacity: 0;
      }
    }

    :host([theme~='thumbnails']:not([complete])) [part='thumbnail'],
    :host([theme~='thumbnails'][complete]) [part='thumbnail']:not([hidden]) + [part='done-icon'] {
      display: none;
    }

    /* TODO: queued state styles (no attribute makes this difficult to target) */

    @media (forced-colors: active) {
      :is([part$='icon'], [part$='button'])::before {
        background: CanvasText;
      }
    }
  `];var qi=o=>class extends F(o){static get properties(){return{disabled:{type:Boolean,value:!1,reflectToAttribute:!0},complete:{type:Boolean,value:!1,reflectToAttribute:!0},errorMessage:{type:String,value:"",observer:"_errorMessageChanged"},file:{type:Object},fileName:{type:String},held:{type:Boolean,value:!1},indeterminate:{type:Boolean,value:!1,reflectToAttribute:!0},i18n:{type:Object},progress:{type:Number},status:{type:String},tabindex:{type:Number,value:0},uploading:{type:Boolean,value:!1,reflectToAttribute:!0},_progress:{type:Object},__thumbnail:{type:String}}}static get observers(){return["__updateTabindex(tabindex, disabled)","__updateProgress(_progress, progress, indeterminate)","__updateThumbnail(file)"]}ready(){super.ready(),this.addController(new k(this,"progress","vaadin-progress-bar",{initializer:e=>{this._progress=e}})),this.shadowRoot.addEventListener("focusin",e=>{e.composedPath()[0].getAttribute("part").endsWith("button")&&this._setFocused(!1)}),this.shadowRoot.addEventListener("focusout",e=>{e.relatedTarget===this&&this._setFocused(!0)})}_shouldSetFocus(e){return e.composedPath()[0]===this}__disabledChanged(e){e?this.removeAttribute("tabindex"):this.setAttribute("tabindex",this.tabindex)}_errorMessageChanged(e){this.toggleAttribute("error",!!e)}__updateTabindex(e,t){t?this.removeAttribute("tabindex"):this.setAttribute("tabindex",e)}__updateProgress(e,t,i){e&&(e.value=isNaN(t)?0:t/100,e.indeterminate=i)}_fireFileEvent(e){return e.preventDefault(),this.dispatchEvent(new CustomEvent(e.target.getAttribute("file-event"),{detail:{file:this.file},bubbles:!0,composed:!0}))}__updateThumbnail(e){if(this.__thumbnailReader&&(this.__thumbnailReader.abort(),this.__thumbnailReader=null),!e){this.__thumbnail="";return}if(e.type&&e.type.startsWith("image/")&&e instanceof Blob){let t=new FileReader;this.__thumbnailReader=t,t.onload=i=>{this.__thumbnail=i.target.result,this.__thumbnailReader=null},t.readAsDataURL(e)}else this.__thumbnail=""}};var ut=class extends qi(f(p(v(c)))){static get is(){return"vaadin-upload-file"}static get styles(){return ji}static get lumoInjector(){return{...super.lumoInjector,includeBaseStyles:!0}}render(){let r=this.held&&!this.uploading&&!this.complete,e=this.errorMessage;return d`
      <div part="loader" ?hidden="${!this.uploading}" aria-hidden="true"></div>

      ${this.__thumbnail?d`<div part="thumbnail">
            <img src="${this.__thumbnail}" alt="${this.fileName}" />
          </div>`:oe}

      <div part="done-icon" ?hidden="${!this.complete}" aria-hidden="true"></div>
      <div part="warning-icon" ?hidden="${!this.errorMessage}" aria-hidden="true"></div>

      <div part="meta">
        <div part="name" id="name">${this.fileName}</div>
        <div part="status" ?hidden="${!this.status}" id="status">${this.status}</div>
        <div part="error" id="error" ?hidden="${!this.errorMessage}">${this.errorMessage}</div>
      </div>

      <div part="commands">
        <button
          type="button"
          part="start-button"
          file-event="file-start"
          @click="${this._fireFileEvent}"
          ?hidden="${!r}"
          ?disabled="${this.disabled}"
          aria-label="${this.i18n?this.i18n.file.start:oe}"
          aria-describedby="name"
        ></button>
        <button
          type="button"
          part="retry-button"
          file-event="file-retry"
          @click="${this._fireFileEvent}"
          ?hidden="${!e}"
          ?disabled="${this.disabled}"
          aria-label="${this.i18n?this.i18n.file.retry:oe}"
          aria-describedby="name"
        ></button>
        <button
          type="button"
          part="remove-button"
          file-event="file-abort"
          @click="${this._fireFileEvent}"
          ?disabled="${this.disabled}"
          aria-label="${this.i18n?this.i18n.file.remove:oe}"
          aria-describedby="name"
        ></button>
      </div>

      <slot name="progress"></slot>
    `}};h(ut);var Yi=m`
  :host {
    display: block;
    overflow: auto;
  }

  :host([hidden]) {
    display: none !important;
  }

  [part='list'] {
    list-style-type: none;
    margin: 0;
    padding: 0;
  }

  ::slotted(:first-child) {
    margin-top: var(--vaadin-upload-gap, var(--vaadin-gap-s));
  }

  ::slotted(li:not(:last-of-type)) {
    border-bottom: var(--vaadin-upload-file-list-divider-width, 1px) solid
      var(--vaadin-upload-file-list-divider-color, var(--vaadin-border-color-secondary));
  }

  /* Thumbnails theme variant */
  :host([theme~='thumbnails']) [part='list'] {
    display: flex;
    flex-wrap: wrap;
    gap: var(--vaadin-gap-s);
  }

  :host([theme~='thumbnails']) ::slotted(:first-child) {
    margin-top: 0;
  }

  :host([theme~='thumbnails']) ::slotted(li) {
    border-bottom: none;
  }
`;var T=class extends EventTarget{#e=[];#s=!1;#n=!1;#r=[];#t=0;#h="POST";#u=1/0;#c=3;#p={};constructor(r={}){super(),this.target=r.target||"",this.method=r.method||"POST",this.headers=r.headers||{},this.timeout=r.timeout||0,this.maxFiles=r.maxFiles??1/0,this.maxFileSize=r.maxFileSize??1/0,this.accept=r.accept||"",this.noAuto=r.noAuto??!1,this.withCredentials=r.withCredentials??!1,this.uploadFormat=r.uploadFormat||"raw",this.maxConcurrentUploads=r.maxConcurrentUploads??3,this.formDataName=r.formDataName||"file",this.disabled=r.disabled??!1}get method(){return this.#h}set method(r){if(r!=="POST"&&r!=="PUT")throw new Error(`Invalid method "${r}". Only POST and PUT are allowed.`);this.#h=r}get maxFiles(){return this.#u}set maxFiles(r){if(r<0)throw new Error(`Invalid maxFiles "${r}". Value must be non-negative.`);this.#u=r,this.#m()}get maxConcurrentUploads(){return this.#c}set maxConcurrentUploads(r){if(r<=0)throw new Error(`Invalid maxConcurrentUploads "${r}". Value must be positive.`);this.#c=r}get headers(){return this.#p}set headers(r){this.#p={...r}}get files(){return[...this.#e]}set files(r){let e=[];for(let t of r){if(this.#e.includes(t)){e.push(t);continue}let i=this.#_(t,e.length);if(i){this.dispatchEvent(new CustomEvent("file-reject",{detail:{file:t,error:i}}));continue}e.push(t)}this.#l(e)}#l(r){this.#e=r,this.#m(),this.#i()}get maxFilesReached(){return this.#s}get disabled(){return this.#n}set disabled(r){let e=!!r;e!==this.#n&&(this.#n=e,this.dispatchEvent(new CustomEvent("disabled-changed",{detail:{value:e}})))}addFiles(r){Array.from(r).forEach(e=>this.#b(e))}uploadFiles(r=this.#e){r&&!Array.isArray(r)&&(r=[r]),r.filter(e=>this.#e.includes(e)&&!e.complete).forEach(e=>this.#d(e))}retryUpload(r){this.#x(r)}abortUpload(r){this.#w(r)}removeFile(r){this.#f(r)}get#g(){if(!this.accept)return null;let r=this.accept.split(",").map(e=>{let t=e.trim();return t=t.replaceAll(/[+.]/gu,String.raw`\$&`),t.startsWith(String.raw`\.`)&&(t=`.*${t}$`),t.replaceAll("/*","/.*")});return new RegExp(`^(${r.join("|")})$`,"iu")}#m(){let r=this.maxFiles>=0&&this.#e.length>=this.maxFiles;r!==this.#s&&(this.#s=r,this.dispatchEvent(new CustomEvent("max-files-reached-changed",{detail:{value:r}})))}#_(r,e){if(e>=this.maxFiles)return"tooManyFiles";if(this.maxFileSize>=0&&r.size>this.maxFileSize)return"fileIsTooBig";let t=this.#g;return t&&!(t.test(r.type)||t.test(r.name))?"incorrectFileType":null}#b(r){let e=this.#_(r,this.#e.length);if(e){this.dispatchEvent(new CustomEvent("file-reject",{detail:{file:r,error:e}}));return}r.loaded=0,r.held=!0,r.formDataName=this.formDataName,this.#l([r,...this.#e]),this.noAuto||this.#d(r)}#f(r){this.#r=this.#r.filter(t=>t!==r),r.uploading&&!r.held&&!r.abort&&r.xhr&&(r.abort=!0,r.xhr.abort());let e=this.#e.indexOf(r);e>=0&&(this.#l(this.#e.filter(t=>t!==r)),this.dispatchEvent(new CustomEvent("file-remove",{detail:{file:r,fileIndex:e}})))}#d(r){r.uploading||this.#r.includes(r)||(r.loaded=0,r.progress=0,r.held=!0,r.uploading=r.indeterminate=!0,r.complete=r.abort=r.errorKey=!1,r.stalled=!1,this.#i(),this.#r.push(r),this.#a())}#a(){for(;this.#r.length>0&&this.#t<this.maxConcurrentUploads;){let r=this.#r.shift();r&&this.#y(r)}}#y(r){this.#t+=1;let e=Date.now(),t=r.xhr=this._createXhr(),i;t.upload.onprogress=_=>{clearTimeout(i);let g=(Date.now()-e)/1e3,y=_.loaded,x=_.total,w=x>0?Math.trunc(y/x*100):100,L=Math.max(0,Math.min(100,w));r.loaded=y,r.progress=L,r.indeterminate=x>0?y<=0||y>=x:!1,r.stalled&&(r.stalled=!1),r.errorKey?r.indeterminate=r.status=void 0:r.abort||L<100&&(this.#C(r,x,y,g),i=setTimeout(()=>{r.uploading&&!r.abort&&(r.stalled=!0,this.#i())},2e3)),this.#i(),this.dispatchEvent(new CustomEvent("upload-progress",{detail:{file:r,xhr:t}}))},t.onabort=()=>{clearTimeout(i),this.#t-=1,this.#o(t),this.#a()},t.ontimeout=()=>{clearTimeout(i),r.indeterminate=r.uploading=!1,r.errorKey="timeout",r.status="",this.#t-=1,this.#a(),this.#o(t),this.dispatchEvent(new CustomEvent("upload-error",{detail:{file:r,xhr:t}})),this.#i()},t.onreadystatechange=()=>{if(t.readyState===4){if(clearTimeout(i),r.indeterminate=r.uploading=!1,this.#t-=1,this.#a(),this.#o(t),r.abort||r.errorKey||(r.status="",!this.dispatchEvent(new CustomEvent("upload-response",{detail:{file:r,xhr:t},cancelable:!0}))))return;t.status===0?r.errorKey="serverUnavailable":t.status>=500?r.errorKey="unexpectedServerError":t.status===413?r.errorKey="fileTooLarge":t.status>=400&&(r.errorKey="forbidden"),r.complete=!r.errorKey;let g=r.errorKey?"upload-error":"upload-success";this.dispatchEvent(new CustomEvent(g,{detail:{file:r,xhr:t}})),r.xhr=null,this.#i()}};let a=this.uploadFormat==="raw";if(r.uploadTarget||(r.uploadTarget=this.target),!this.dispatchEvent(new CustomEvent("upload-before",{detail:{file:r,xhr:t},cancelable:!0}))){this.#v(r);return}if(!this.#e.includes(r)){r.abort||(this.#t-=1),this.#o(t),this.#a();return}let n;if(a)n=r;else{let _=new FormData;_.append(r.formDataName||this.formDataName,r,r.name),n=_}t.open(this.method,r.uploadTarget,!0),this.#A(t,r,a),r.held=!1,t.upload.onloadstart=()=>{this.dispatchEvent(new CustomEvent("upload-start",{detail:{file:r,xhr:t}})),this.#i()};let l={file:r,xhr:t,uploadFormat:this.uploadFormat,requestBody:n};if(a||(l.formData=n),!this.dispatchEvent(new CustomEvent("upload-request",{detail:l,cancelable:!0}))){this.#v(r);return}if(!this.#e.includes(r)){r.abort||(this.#t-=1),this.#o(t),this.#a();return}try{t.send(n)}catch(_){this.#t-=1,r.uploading=!1,r.indeterminate=!1,r.errorKey=_.message||"sendFailed",this.#o(t),this.#i(),this.#a()}}_createXhr(){return new XMLHttpRequest}#v(r){this.#t-=1,r.uploading=!1,r.indeterminate=!1,r.held=!0,this.#i(),this.#a()}#o(r){r&&(r.upload.onprogress=null,r.upload.onloadstart=null,r.onreadystatechange=null,r.onabort=null,r.ontimeout=null)}#A(r,e,t){Object.entries(this.headers).forEach(([i,a])=>{r.setRequestHeader(i,a)}),t&&(r.setRequestHeader("Content-Type",e.type||"application/octet-stream"),r.setRequestHeader("X-Filename",encodeURIComponent(e.name))),this.timeout&&(r.timeout=this.timeout),r.withCredentials=this.withCredentials}#x(r){this.dispatchEvent(new CustomEvent("upload-retry",{detail:{file:r,xhr:r.xhr},cancelable:!0}))&&(r.uploading=!1,this.#r=this.#r.filter(t=>t!==r),this.#d(r))}#w(r){this.dispatchEvent(new CustomEvent("upload-abort",{detail:{file:r,xhr:r.xhr},cancelable:!0}))&&(r.abort=!0,r.xhr&&r.xhr.abort(),this.#f(r))}#C(r,e,t,i){r.elapsed=i,r.remaining=t>0?Math.ceil(i*(e/t-1)):0,r.speed=i>0?Math.trunc(t/i/1024):0,r.total=e}#i(){this.dispatchEvent(new CustomEvent("files-changed",{detail:{value:this.#e}}))}};var ur={file:{retry:"Retry",start:"Start",remove:"Remove"},error:{tooManyFiles:"Too Many Files.",fileIsTooBig:"File is Too Big.",incorrectFileType:"Incorrect File Type."},uploading:{status:{connecting:"Connecting...",stalled:"Stalled",processing:"Processing File...",held:"Queued"},remainingTime:{prefix:"remaining time: ",unknown:"unknown remaining time"},error:{serverUnavailable:"Upload failed, please try again later",unexpectedServerError:"Upload failed due to server error",forbidden:"Upload forbidden",fileTooLarge:"File is too large"}},units:{size:["B","kB","MB","GB","TB","PB","EB","ZB","YB"]}},Wi=o=>class extends $(o){static get properties(){return{items:{type:Array},disabled:{type:Boolean,value:!1,reflectToAttribute:!0},manager:{type:Object,value:null,observer:"__managerChanged"}}}static get observers(){return["__updateItems(items, __effectiveI18n, disabled, _theme)"]}static get defaultI18n(){return ur}get i18n(){return super.i18n}set i18n(e){super.i18n=e}constructor(){super(),this.__onManagerFilesChanged=this.__onManagerFilesChanged.bind(this),this.__onManagerDisabledChanged=this.__onManagerDisabledChanged.bind(this),this.__onFileRetry=this.__onFileRetry.bind(this),this.__onFileAbort=this.__onFileAbort.bind(this),this.__onFileStart=this.__onFileStart.bind(this),this.__onFileRemove=this.__onFileRemove.bind(this)}ready(){super.ready(),this.addEventListener("file-retry",this.__onFileRetry),this.addEventListener("file-abort",this.__onFileAbort),this.addEventListener("file-start",this.__onFileStart),this.addEventListener("file-remove",this.__onFileRemove)}disconnectedCallback(){super.disconnectedCallback(),this.manager instanceof T&&(this.manager.removeEventListener("files-changed",this.__onManagerFilesChanged),this.manager.removeEventListener("disabled-changed",this.__onManagerDisabledChanged))}connectedCallback(){super.connectedCallback(),this.manager instanceof T&&(this.manager.addEventListener("files-changed",this.__onManagerFilesChanged),this.manager.addEventListener("disabled-changed",this.__onManagerDisabledChanged),this.__syncFromManager())}__managerChanged(e,t){t instanceof T&&(t.removeEventListener("files-changed",this.__onManagerFilesChanged),t.removeEventListener("disabled-changed",this.__onManagerDisabledChanged)),this.isConnected&&e instanceof T?(e.addEventListener("files-changed",this.__onManagerFilesChanged),e.addEventListener("disabled-changed",this.__onManagerDisabledChanged),this.__syncFromManager()):this.items=[]}__onManagerFilesChanged(){this.__syncFromManager()}__onManagerDisabledChanged(){this.requestContentUpdate()}__syncFromManager(){this.manager instanceof T&&(this.items=[...this.manager.files])}__onFileRetry(e){this.manager instanceof T&&(e.stopPropagation(),this.manager.retryUpload(e.detail.file))}__onFileAbort(e){this.manager instanceof T&&(e.stopPropagation(),this.manager.abortUpload(e.detail.file))}__onFileStart(e){this.manager instanceof T&&(e.stopPropagation(),this.manager.uploadFiles(e.detail.file))}__onFileRemove(e){this.manager instanceof T&&(e.stopPropagation(),this.manager.removeFile(e.detail.file))}__updateItems(e,t,i,a){e&&t&&(e.forEach(s=>this.__applyI18nToFile(s)),this.requestContentUpdate())}__applyI18nToFile(e){let t=this.__effectiveI18n;e.total&&this.__applyFileSizeStrings(e),e.status=this.__getFileStatus(e,t),this.__applyFileError(e,t)}__applyFileSizeStrings(e){e.totalStr=this.__formatSize(e.total),e.loadedStr=this.__formatSize(e.loaded||0),e.elapsed!=null&&(e.elapsedStr=this.__formatTime(e.elapsed,this.__splitTimeByUnits(e.elapsed))),e.remaining!=null&&(e.remainingStr=this.__formatTime(e.remaining,this.__splitTimeByUnits(e.remaining)))}__getFileStatus(e,t){return e.held&&!e.error?t.uploading.status.held:e.stalled?t.uploading.status.stalled:e.uploading&&e.indeterminate&&!e.held?e.progress===100?t.uploading.status.processing:t.uploading.status.connecting:e.uploading&&e.progress<100&&e.total?this.__formatFileProgress(e):e.status}__applyFileError(e,t){e.errorKey&&t.uploading.error[e.errorKey]?e.error=t.uploading.error[e.errorKey]:!e.errorKey&&this.manager instanceof T&&(e.error="")}__formatSize(e){let t=this.__effectiveI18n;if(typeof t.formatSize=="function")return t.formatSize(e);let i=t.units.sizeBase||1e3,a=Math.trunc(Math.log(e)/Math.log(i)),s=Math.max(0,Math.min(3,a-1));return`${Number.parseFloat((e/i**a).toFixed(s))} ${t.units.size[a]}`}__splitTimeByUnits(e){let t=[60,60,24,1/0],i=[0];for(let a=0;a<t.length&&e>0;a++)i[a]=e%t[a],e=Math.floor(e/t[a]);return i}__formatTime(e,t){let i=this.__effectiveI18n;if(typeof i.formatTime=="function")return i.formatTime(e,t);for(;t.length<3;)t.push(0);return t.reverse().map(a=>(a<10?"0":"")+a).join(":")}__formatFileProgress(e){let t=this.__effectiveI18n,i=e.loaded>0?t.uploading.remainingTime.prefix+e.remainingStr:t.uploading.remainingTime.unknown;return`${e.totalStr}: ${e.progress}% (${i})`}requestContentUpdate(){let{items:e,__effectiveI18n:t,disabled:i}=this,a=this.manager instanceof T&&this.manager.disabled,s=i||a;gt(d`
          ${e.map(n=>d`
              <li>
                <vaadin-upload-file
                  .disabled="${s}"
                  .file="${n}"
                  .complete="${n.complete}"
                  .errorMessage="${n.error}"
                  .fileName="${n.name}"
                  .held="${n.held}"
                  .indeterminate="${n.indeterminate}"
                  .progress="${n.progress}"
                  .status="${n.status}"
                  .uploading="${n.uploading}"
                  .i18n="${t}"
                  theme="${D(this._theme)}"
                ></vaadin-upload-file>
              </li>
            `)}
        `,this)}};var ct=class extends Wi(f(p(c))){static get is(){return"vaadin-upload-file-list"}static get styles(){return Yi}render(){return d`
      <ul part="list">
        <slot></slot>
      </ul>
    `}};h(ct);var Ki=m`
  :host {
    background: var(--vaadin-upload-background, transparent);
    border: var(--vaadin-upload-border-width, 1px) solid
      var(--vaadin-upload-border-color, var(--vaadin-border-color-secondary));
    border-radius: var(--vaadin-upload-border-radius, var(--vaadin-radius-m));
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    padding: var(--vaadin-upload-padding, var(--vaadin-padding-s));
    position: relative;
  }

  :host([dragover-valid]) {
    --vaadin-upload-background: var(--vaadin-background-container);
    --vaadin-upload-border-color: var(--vaadin-text-color);
    border-style: dashed;
  }

  :host([hidden]) {
    display: none !important;
  }

  [hidden] {
    display: none !important;
  }

  [part='primary-buttons'] {
    align-items: center;
    display: flex;
    gap: var(--vaadin-gap-s);
  }

  [part='drop-label'] {
    align-items: center;
    color: var(--vaadin-upload-drop-label-color, var(--vaadin-text-color));
    display: flex;
    font-size: var(--vaadin-upload-drop-label-font-size, inherit);
    font-weight: var(--vaadin-upload-drop-label-font-weight, inherit);
    gap: var(--vaadin-upload-drop-label-gap, var(--vaadin-gap-s));
    line-height: var(--vaadin-upload-drop-label-line-height, inherit);
  }
`;function Qi(o){async function r(i){if(i.isFile)return new Promise(a=>{i.file(a,()=>a([]))});if(i.isDirectory){let a=i.createReader(),s=await new Promise(l=>{a.readEntries(l,()=>l([]))});return(await Promise.all(s.map(r))).flat()}}if(!Array.from(o.dataTransfer.items).filter(i=>!!i).filter(i=>typeof i.webkitGetAsEntry=="function").map(i=>i.webkitGetAsEntry()).some(i=>!!i?.isDirectory))return Promise.resolve(o.dataTransfer.files?Array.from(o.dataTransfer.files):[]);let t=Array.from(o.dataTransfer.items).map(i=>i.webkitGetAsEntry()).filter(i=>!!i).map(r);return Promise.all(t).then(i=>i.flat())}var cr={dropFiles:{one:"Drop file here",many:"Drop files here"},addFiles:{one:"Upload File...",many:"Upload Files..."},error:{tooManyFiles:"Too Many Files.",fileIsTooBig:"File is Too Big.",incorrectFileType:"Incorrect File Type."},uploading:{status:{connecting:"Connecting...",stalled:"Stalled",processing:"Processing File...",held:"Queued"},remainingTime:{prefix:"remaining time: ",unknown:"unknown remaining time"},error:{serverUnavailable:"Upload failed, please try again later",unexpectedServerError:"Upload failed due to server error",forbidden:"Upload forbidden",fileTooLarge:"File is too large"}},file:{retry:"Retry",start:"Start",remove:"Remove"},units:{size:["B","kB","MB","GB","TB","PB","EB","ZB","YB"]}},pt=class extends k{constructor(r){super(r,"add-button","vaadin-button")}initNode(r){r._isDefault&&(this.defaultNode=r),r.addEventListener("touchend",e=>{this.host._onAddFilesTouchEnd(e)}),r.addEventListener("click",e=>{this.host._onAddFilesClick(e)}),this.host._addButton=r}},mt=class extends k{constructor(r){super(r,"drop-label","span")}initNode(r){r._isDefault&&(this.defaultNode=r),this.host._dropLabel=r}},Gi=o=>class extends $(o){static get properties(){return{disabled:{type:Boolean,value:!1,reflectToAttribute:!0},nodrop:{type:Boolean,reflectToAttribute:!0,value:ue},target:{type:String,value:""},method:{type:String,value:"POST"},headers:{type:Object,value:{}},timeout:{type:Number,value:0},_dragover:{type:Boolean,value:!1,observer:"_dragoverChanged"},files:{type:Array,notify:!0,value:()=>[],sync:!0},maxFiles:{type:Number,value:1/0,sync:!0},maxFilesReached:{type:Boolean,value:!1,notify:!0,readOnly:!0,reflectToAttribute:!0},accept:{type:String,value:""},maxFileSize:{type:Number,value:1/0},_dragoverValid:{type:Boolean,value:!1,observer:"_dragoverValidChanged"},formDataName:{type:String,value:"file"},noAuto:{type:Boolean,value:!1},withCredentials:{type:Boolean,value:!1},uploadFormat:{type:String,value:"raw"},maxConcurrentUploads:{type:Number,value:3,sync:!0},capture:{type:String},_addButton:{type:Object},_dropLabel:{type:Object},_fileList:{type:Object},_files:{type:Array},_uploadQueue:{type:Array,value:()=>[]},_activeUploads:{type:Number,value:0}}}static get observers(){return["__updateAddButton(_addButton, maxFiles, __effectiveI18n, maxFilesReached, disabled)","__updateDropLabel(_dropLabel, maxFiles, __effectiveI18n)","__updateFileList(_fileList, files, __effectiveI18n, disabled, _theme)","__updateMaxFilesReached(maxFiles, files)"]}static get defaultI18n(){return cr}get i18n(){return super.i18n}set i18n(e){super.i18n=e}get __acceptRegexp(){if(!this.accept)return null;let e=this.accept.split(",").map(t=>{let i=t.trim();return i=i.replace(/[+.]/gu,"\\$&"),i.startsWith("\\.")&&(i=`.*${i}$`),i.replace(/\/\*/gu,"/.*")});return new RegExp(`^(${e.join("|")})$`,"iu")}ready(){super.ready(),this.addEventListener("dragover",this._onDragover.bind(this)),this.addEventListener("dragleave",this._onDragleave.bind(this)),this.addEventListener("drop",this._onDrop.bind(this)),this.addEventListener("file-retry",this._onFileRetry.bind(this)),this.addEventListener("file-abort",this._onFileAbort.bind(this)),this.addEventListener("file-start",this._onFileStart.bind(this)),this.addEventListener("file-reject",this._onFileReject.bind(this)),this.addEventListener("upload-start",this._onUploadStart.bind(this)),this.addEventListener("upload-success",this._onUploadSuccess.bind(this)),this.addEventListener("upload-error",this._onUploadError.bind(this)),this._addButtonController=new pt(this),this.addController(this._addButtonController),this._dropLabelController=new mt(this),this.addController(this._dropLabelController),this.addController(new k(this,"file-list","vaadin-upload-file-list",{initializer:e=>{this._fileList=e}})),this.addController(new k(this,"drop-label-icon","vaadin-upload-icon"))}_formatSize(e){if(typeof this.__effectiveI18n.formatSize=="function")return this.__effectiveI18n.formatSize(e);let t=this.__effectiveI18n.units.sizeBase||1e3,i=~~(Math.log(e)/Math.log(t)),a=Math.max(0,Math.min(3,i-1));return`${parseFloat((e/t**i).toFixed(a))} ${this.__effectiveI18n.units.size[i]}`}_splitTimeByUnits(e){let t=[60,60,24,1/0],i=[0];for(let a=0;a<t.length&&e>0;a++)i[a]=e%t[a],e=Math.floor(e/t[a]);return i}_formatTime(e,t){if(typeof this.__effectiveI18n.formatTime=="function")return this.__effectiveI18n.formatTime(e,t);for(;t.length<3;)t.push(0);return t.reverse().map(i=>(i<10?"0":"")+i).join(":")}_formatFileProgress(e){let t=e.loaded>0?this.__effectiveI18n.uploading.remainingTime.prefix+e.remainingStr:this.__effectiveI18n.uploading.remainingTime.unknown;return`${e.totalStr}: ${e.progress}% (${t})`}__updateMaxFilesReached(e,t){this._setMaxFilesReached(e>=0&&t.length>=e)}__updateAddButton(e,t,i,a,s){e&&(e.disabled=s||a,e===this._addButtonController.defaultNode&&(e.textContent=this._i18nPlural(t,i.addFiles)))}__updateDropLabel(e,t,i){e&&e===this._dropLabelController.defaultNode&&(e.textContent=this._i18nPlural(t,i.dropFiles))}__updateFileList(e,t,i,a){e&&(e.items=[...t],e.i18n=i,e.disabled=a,this._theme?e.setAttribute("theme",this._theme):e.removeAttribute("theme"))}_onDragover(e){e.preventDefault(),!this.nodrop&&!this._dragover&&(this._dragoverValid=!this.maxFilesReached&&!this.disabled,this._dragover=!0),e.dataTransfer.dropEffect=!this._dragoverValid||this.nodrop?"none":"copy"}_onDragleave(e){e.preventDefault(),this._dragover&&!this.nodrop&&(this._dragover=this._dragoverValid=!1)}async _onDrop(e){if(!this.nodrop&&!this.disabled){e.preventDefault(),this._dragover=this._dragoverValid=!1;let t=await Qi(e);this._addFiles(t)}}_createXhr(){return new XMLHttpRequest}_configureXhr(e,t=null,i=!1){if(typeof this.headers=="string")try{this.headers=JSON.parse(this.headers)}catch{this.headers=void 0}Object.entries(this.headers).forEach(([a,s])=>{e.setRequestHeader(a,s)}),i&&t&&(e.setRequestHeader("Content-Type",t.type||"application/octet-stream"),e.setRequestHeader("X-Filename",encodeURIComponent(t.name))),this.timeout&&(e.timeout=this.timeout),e.withCredentials=this.withCredentials}_setStatus(e,t,i,a){e.elapsed=a,e.elapsedStr=this._formatTime(e.elapsed,this._splitTimeByUnits(e.elapsed)),e.remaining=Math.ceil(a*(t/i-1)),e.remainingStr=this._formatTime(e.remaining,this._splitTimeByUnits(e.remaining)),e.speed=~~(t/a/1024),e.totalStr=this._formatSize(t),e.loadedStr=this._formatSize(i),e.status=this._formatFileProgress(e)}uploadFiles(e=this.files){e&&!Array.isArray(e)&&(e=[e]),e.filter(t=>!t.complete).forEach(t=>this._queueFileUpload(t))}_queueFileUpload(e){e.uploading||(e.held=!0,e.uploading=e.indeterminate=!0,e.complete=e.abort=e.error=!1,e.status=this.__effectiveI18n.uploading.status.held,this._renderFileList(),this._uploadQueue.push(e),this._processUploadQueue())}_processUploadQueue(){for(;this._uploadQueue.length>0&&this._activeUploads<this.maxConcurrentUploads;){let e=this._uploadQueue.shift();e&&this._uploadFile(e)}}_uploadFile(e){this._activeUploads+=1;let t=Date.now(),i=e.xhr=this._createXhr(),a,s;i.upload.onprogress=y=>{clearTimeout(a),s=Date.now();let x=(s-t)/1e3,w=y.loaded,L=y.total,ae=~~(w/L*100);e.loaded=w,e.progress=ae,e.indeterminate=w<=0||w>=L,e.error?e.indeterminate=e.status=void 0:e.abort||(ae<100?(this._setStatus(e,L,w,x),a=setTimeout(()=>{e.status=this.__effectiveI18n.uploading.status.stalled,this._renderFileList()},2e3)):(e.loadedStr=e.totalStr,e.status=this.__effectiveI18n.uploading.status.processing)),this._renderFileList(),this.dispatchEvent(new CustomEvent("upload-progress",{detail:{file:e,xhr:i}}))},i.onabort=()=>{this._activeUploads-=1,this._processUploadQueue()},i.onreadystatechange=()=>{if(i.readyState===4){if(clearTimeout(a),e.indeterminate=e.uploading=!1,this._activeUploads-=1,this._processUploadQueue(),e.abort||(e.status="",!this.dispatchEvent(new CustomEvent("upload-response",{detail:{file:e,xhr:i},cancelable:!0}))))return;i.status===0?e.error=this.__effectiveI18n.uploading.error.serverUnavailable:i.status>=500?e.error=this.__effectiveI18n.uploading.error.unexpectedServerError:i.status===413?e.error=this.__effectiveI18n.uploading.error.fileTooLarge:i.status>=400&&(e.error=this.__effectiveI18n.uploading.error.forbidden),e.complete=!e.error,this.dispatchEvent(new CustomEvent(`upload-${e.error?"error":"success"}`,{detail:{file:e,xhr:i}})),this._renderFileList()}};let n=this.uploadFormat==="raw";if(e.uploadTarget||(e.uploadTarget=this.target||""),n||(e.formDataName=this.formDataName),!this.dispatchEvent(new CustomEvent("upload-before",{detail:{file:e,xhr:i},cancelable:!0})))return;let u;if(n)u=e;else{let y=new FormData;y.append(e.formDataName,e,e.name),u=y}i.open(this.method,e.uploadTarget,!0),this._configureXhr(i,e,n),e.held=!1,e.status=this.__effectiveI18n.uploading.status.connecting,i.upload.onloadstart=()=>{this.dispatchEvent(new CustomEvent("upload-start",{detail:{file:e,xhr:i}})),this._renderFileList()};let _={file:e,xhr:i,uploadFormat:this.uploadFormat,requestBody:u};n||(_.formData=u),this.dispatchEvent(new CustomEvent("upload-request",{detail:_,cancelable:!0}))&&i.send(u)}_retryFileUpload(e){this.dispatchEvent(new CustomEvent("upload-retry",{detail:{file:e,xhr:e.xhr},cancelable:!0}))&&(this._queueFileUpload(e),this._updateFocus(this.files.indexOf(e)))}_abortFileUpload(e){this.dispatchEvent(new CustomEvent("upload-abort",{detail:{file:e,xhr:e.xhr},cancelable:!0}))&&(e.abort=!0,e.xhr&&e.xhr.abort(),this._removeFile(e))}_renderFileList(){this._fileList&&typeof this._fileList.requestContentUpdate=="function"&&this._fileList.requestContentUpdate()}_addFiles(e){Array.prototype.forEach.call(e,this._addFile.bind(this))}_addFile(e){if(this.maxFilesReached){this.dispatchEvent(new CustomEvent("file-reject",{detail:{file:e,error:this.__effectiveI18n.error.tooManyFiles}}));return}if(this.maxFileSize>=0&&e.size>this.maxFileSize){this.dispatchEvent(new CustomEvent("file-reject",{detail:{file:e,error:this.__effectiveI18n.error.fileIsTooBig}}));return}let t=this.__acceptRegexp;if(t&&!(t.test(e.type)||t.test(e.name))){this.dispatchEvent(new CustomEvent("file-reject",{detail:{file:e,error:this.__effectiveI18n.error.incorrectFileType}}));return}e.loaded=0,e.held=!0,e.status=this.__effectiveI18n.uploading.status.held,this.files=[e,...this.files],this.noAuto||this._queueFileUpload(e)}_updateFocus(e){if(this.files.length===0){this._addButton.focus({focusVisible:z()});return}e===this.files.length&&(e-=1),this._fileList.children[e].firstElementChild.focus({focusVisible:z()})}_removeFile(e){this._uploadQueue=this._uploadQueue.filter(i=>i!==e),this._processUploadQueue();let t=this.files.indexOf(e);t>=0&&(this.files=this.files.filter(i=>i!==e),this.dispatchEvent(new CustomEvent("file-remove",{detail:{file:e},bubbles:!0,composed:!0})),this._updateFocus(t))}_onAddFilesTouchEnd(e){e.preventDefault(),this._onAddFilesClick(e)}_onAddFilesClick(e){this.maxFilesReached||(e.stopPropagation(),this.$.fileInput.value="",this.$.fileInput.click())}_onFileInputChange(e){this._addFiles(e.target.files)}_onFileStart(e){this._queueFileUpload(e.detail.file)}_onFileRetry(e){this._retryFileUpload(e.detail.file)}_onFileAbort(e){this._abortFileUpload(e.detail.file)}_onFileReject(e){le(`${e.detail.file.name}: ${e.detail.error}`,{mode:"alert"})}_onUploadStart(e){le(`${e.detail.file.name}: 0%`,{mode:"alert"})}_onUploadSuccess(e){le(`${e.detail.file.name}: 100%`,{mode:"alert"})}_onUploadError(e){le(`${e.detail.file.name}: ${e.detail.file.error}`,{mode:"alert"})}_dragoverChanged(e){e?this.setAttribute("dragover",e):this.removeAttribute("dragover")}_dragoverValidChanged(e){e?this.setAttribute("dragover-valid",e):this.removeAttribute("dragover-valid")}_i18nPlural(e,t){return e===1?t.one:t.many}_isMultiple(e){return e!==1}};var _t=class extends Gi(A(f(p(v(c))))){static get is(){return"vaadin-upload"}static get styles(){return Ki}static get lumoInjector(){return{...super.lumoInjector,includeBaseStyles:!0}}render(){return d`
      <div part="primary-buttons">
        <slot name="add-button"></slot>
        <div part="drop-label" ?hidden="${this.nodrop}" id="dropLabelContainer" aria-hidden="true">
          <slot name="drop-label-icon"></slot>
          <slot name="drop-label"></slot>
        </div>
      </div>
      <slot name="file-list"></slot>
      <slot></slot>
      <input
        type="file"
        id="fileInput"
        hidden
        @change="${this._onFileInputChange}"
        accept="${this.accept}"
        ?multiple="${this._isMultiple(this.maxFiles)}"
        capture="${D(this.capture)}"
      />
    `}};h(_t);
/*! Bundled license information:

@vaadin/button/src/styles/vaadin-button-base-styles.js:
@vaadin/button/src/vaadin-button-mixin.js:
@vaadin/button/src/vaadin-button.js:
@vaadin/checkbox/src/styles/vaadin-checkbox-base-styles.js:
@vaadin/checkbox/src/vaadin-checkbox-mixin.js:
@vaadin/checkbox/src/vaadin-checkbox.js:
@vaadin/dialog/src/styles/vaadin-dialog-overlay-base-styles.js:
@vaadin/dialog/src/vaadin-dialog-overflow-controller.js:
@vaadin/dialog/src/vaadin-dialog-overlay-mixin.js:
@vaadin/dialog/src/vaadin-dialog-overlay.js:
@vaadin/dialog/src/vaadin-dialog-base-mixin.js:
@vaadin/dialog/src/vaadin-dialog-utils.js:
@vaadin/dialog/src/vaadin-dialog-draggable-mixin.js:
@vaadin/dialog/src/vaadin-dialog-renderer-mixin.js:
@vaadin/dialog/src/vaadin-dialog-resizable-mixin.js:
@vaadin/dialog/src/vaadin-dialog-size-mixin.js:
@vaadin/dialog/src/vaadin-dialog.js:
@vaadin/item/src/vaadin-item-mixin.js:
@vaadin/select/src/vaadin-select-item.js:
@vaadin/a11y-base/src/list-mixin.js:
@vaadin/list-box/src/styles/vaadin-list-box-base-styles.js:
@vaadin/select/src/vaadin-select-list-box.js:
@vaadin/select/src/styles/vaadin-select-overlay-base-styles.js:
@vaadin/select/src/vaadin-select-overlay-mixin.js:
@vaadin/select/src/vaadin-select-overlay.js:
@vaadin/select/src/styles/vaadin-select-value-button-base-styles.js:
@vaadin/select/src/vaadin-select-value-button.js:
@vaadin/select/src/styles/vaadin-select-base-styles.js:
@vaadin/select/src/vaadin-select-base-mixin.js:
@vaadin/select/src/vaadin-select.js:
@vaadin/tabs/src/styles/vaadin-tab-base-styles.js:
@vaadin/tabs/src/vaadin-tab.js:
@vaadin/tabs/src/styles/vaadin-tabs-base-styles.js:
@vaadin/tabs/src/vaadin-tabs-mixin.js:
@vaadin/tabs/src/vaadin-tabs.js:
@vaadin/text-field/src/vaadin-text-field.js:
@vaadin/progress-bar/src/styles/vaadin-progress-bar-base-styles.js:
@vaadin/progress-bar/src/vaadin-progress-mixin.js:
@vaadin/progress-bar/src/vaadin-progress-bar.js:
  (**
   * @license
   * Copyright (c) 2017 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/a11y-base/src/active-mixin.js:
@vaadin/field-base/src/styles/checkable-base-styles.js:
@vaadin/field-base/src/checked-mixin.js:
@vaadin/component-base/src/media-query-controller.js:
@vaadin/field-base/src/virtual-keyboard-controller.js:
@vaadin/a11y-base/src/styles/sr-only-styles.js:
@vaadin/text-area/src/styles/vaadin-text-area-base-styles.js:
@vaadin/field-base/src/input-field-mixin.js:
@vaadin/field-base/src/text-area-controller.js:
@vaadin/text-area/src/vaadin-text-area-mixin.js:
@vaadin/text-area/src/vaadin-text-area.js:
@vaadin/text-field/src/vaadin-text-field-mixin.js:
  (**
   * @license
   * Copyright (c) 2021 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/date-picker/src/styles/vaadin-date-picker-overlay-base-styles.js:
@vaadin/date-picker/src/vaadin-date-picker-overlay.js:
@vaadin/date-picker/src/vaadin-date-picker-helper.js:
@vaadin/date-picker/src/vaadin-infinite-scroller.js:
@vaadin/date-picker/src/vaadin-date-picker-month-scroller.js:
@vaadin/date-picker/src/vaadin-date-picker-year-scroller.js:
@vaadin/date-picker/src/styles/vaadin-date-picker-year-base-styles.js:
@vaadin/date-picker/src/vaadin-date-picker-year.js:
@vaadin/date-picker/src/styles/vaadin-month-calendar-base-styles.js:
@vaadin/date-picker/src/vaadin-month-calendar-mixin.js:
@vaadin/date-picker/src/vaadin-month-calendar.js:
@vaadin/date-picker/src/styles/vaadin-date-picker-overlay-content-base-styles.js:
@vaadin/date-picker/src/vaadin-date-picker-overlay-content-mixin.js:
@vaadin/date-picker/src/vaadin-date-picker-overlay-content.js:
@vaadin/date-picker/src/styles/vaadin-date-picker-base-styles.js:
@vaadin/date-picker/src/vaadin-date-picker-mixin.js:
@vaadin/date-picker/src/vaadin-date-picker.js:
@vaadin/upload/src/styles/vaadin-upload-icon-base-styles.js:
@vaadin/upload/src/vaadin-upload-icon.js:
@vaadin/upload/src/vaadin-upload-icons.js:
@vaadin/upload/src/styles/vaadin-upload-file-base-styles.js:
@vaadin/upload/src/vaadin-upload-file-mixin.js:
@vaadin/upload/src/vaadin-upload-file.js:
@vaadin/upload/src/styles/vaadin-upload-file-list-base-styles.js:
@vaadin/upload/src/vaadin-upload-file-list-mixin.js:
@vaadin/upload/src/vaadin-upload-file-list.js:
@vaadin/upload/src/styles/vaadin-upload-base-styles.js:
@vaadin/upload/src/vaadin-upload-mixin.js:
@vaadin/upload/src/vaadin-upload.js:
  (**
   * @license
   * Copyright (c) 2016 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/date-picker/src/vaadin-date-picker-overlay-mixin.js:
  (**
   * @license
   * Copyright (c) 2015 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/a11y-base/src/aria-hidden.js:
  (**
   * @license
   * Copyright (c) 2017 Anton Korzunov
   * SPDX-License-Identifier: MIT
   *)

@vaadin/time-picker/src/vaadin-time-picker-item.js:
@vaadin/time-picker/src/styles/vaadin-time-picker-overlay-base-styles.js:
@vaadin/time-picker/src/vaadin-time-picker-overlay.js:
@vaadin/time-picker/src/styles/vaadin-time-picker-scroller-base-styles.js:
@vaadin/time-picker/src/vaadin-time-picker-scroller.js:
@vaadin/time-picker/src/styles/vaadin-time-picker-base-styles.js:
@vaadin/time-picker/src/vaadin-time-picker-helper.js:
@vaadin/time-picker/src/vaadin-time-picker-mixin.js:
@vaadin/time-picker/src/vaadin-time-picker.js:
  (**
   * @license
   * Copyright (c) 2018 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/date-time-picker/src/styles/vaadin-date-time-picker-base-styles.js:
@vaadin/date-time-picker/src/vaadin-date-time-picker-mixin.js:
@vaadin/date-time-picker/src/vaadin-date-time-picker.js:
  (**
   * @license
   * Copyright (c) 2019 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/a11y-base/src/keyboard-direction-mixin.js:
  (**
   * @license
   * Copyright (c) 2022 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/select/src/button-controller.js:
  (**
   * @license
   * Copyright (c) 2023 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/upload/src/vaadin-upload-manager.js:
@vaadin/upload/src/vaadin-upload-helpers.js:
  (**
   * @license
   * Copyright (c) 2000 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)
*/
