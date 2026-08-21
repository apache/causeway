import{a as te}from"./chunks/chunk-6DJFYO7X.js";import{B as L,C as F,D as O,E as ee,G as M,H as ie,I as oe,J as re,a as J,b,c as x,d as I,e as y,i as C,j as Q,l as w,m as P,n as c,o as V,v as B,x as Z}from"./chunks/chunk-T6TECDK2.js";import{M as g,N as E,P as X,Q as S,b as _,c as l,f as d,g as a,h as f,j as z,k as h,l as u,n as m}from"./chunks/chunk-JAZKWOIA.js";var q=class extends I(m(f(h(u(d))))){static get is(){return"vaadin-combo-box-item"}static get styles(){return[b,x]}render(){return l`
      <span part="checkmark" aria-hidden="true"></span>
      <div part="content">
        <slot></slot>
      </div>
    `}};a(q);var A=[Q,_`
    :host {
      --vaadin-item-checkmark-display: block;
    }

    [part='overlay'] {
      position: relative;
      width: var(--vaadin-combo-box-overlay-width, var(--_vaadin-combo-box-overlay-default-width, auto));
    }

    [part='content'] {
      display: flex;
      flex-direction: column;
      height: 100%;
    }

    :host([loading]) [part='content'] {
      --_items-min-height: calc(var(--vaadin-icon-size, 1lh) + 4px);
    }

    [part='loader'] {
      position: absolute;
      inset: var(--vaadin-item-overlay-padding, 4px);
      inset-block-end: auto;
      inset-inline-start: auto;
      margin: 2px;
    }
  `];var G=class extends w(C(f(m(h(u(d)))))){static get is(){return"vaadin-combo-box-overlay"}static get styles(){return[y,A]}render(){return l`
      <div part="overlay" id="overlay">
        <div part="loader"></div>
        <div part="content" id="content"><slot></slot></div>
      </div>
    `}};a(G);var N=class extends V(h(d)){static get is(){return"vaadin-combo-box-scroller"}static get styles(){return P}render(){return l`
      <div id="selector">
        <slot></slot>
      </div>
    `}};a(N);var D=_`
  :host([opened]) {
    pointer-events: auto;
  }

  [part~='toggle-button']::before {
    mask-image: var(--_vaadin-icon-chevron-down);
  }

  :host([readonly]) [part~='toggle-button'] {
    display: none;
  }
`;var k=n=>class extends n{static get properties(){return{pageSize:{type:Number,value:50,observer:"_pageSizeChanged",sync:!0},size:{type:Number,observer:"_sizeChanged",sync:!0},dataProvider:{type:Object,observer:"_dataProviderChanged",sync:!0}}}static get observers(){return["_dataProviderFilterChanged(filter)","_ensureFirstPage(opened)"]}constructor(){super(),this.__dataProviderInitialized=!1,this.__previousDataProviderFilter,this.__dataProviderController=new te(this,{placeholder:new c,isPlaceholder:e=>e instanceof c,dataProviderParams:()=>({filter:this.filter})}),this.__dataProviderController.addEventListener("page-requested",this.__onDataProviderPageRequested.bind(this)),this.__dataProviderController.addEventListener("page-loaded",this.__onDataProviderPageLoaded.bind(this))}ready(){super.ready(),this._scroller.addEventListener("index-requested",e=>{if(!this._shouldFetchData())return;let t=e.detail.index;t!==void 0&&this.__dataProviderController.ensureFlatIndexLoaded(t)}),this.__dataProviderInitialized=!0,this.dataProvider&&this.__synchronizeControllerState()}_dataProviderFilterChanged(e){if(this.__previousDataProviderFilter===void 0&&e===""){this.__previousDataProviderFilter=e;return}this.__previousDataProviderFilter!==e&&(this.__previousDataProviderFilter=e,this.__keepOverlayOpened=!0,this.size=void 0,this.clearCache(),this.__keepOverlayOpened=!1)}_shouldFetchData(){return this.dataProvider?this.opened||this.filter&&this.filter.length:!1}_ensureFirstPage(e){!this._shouldFetchData()||!e||(this._forceNextRequest||this.size===void 0?(this._forceNextRequest=!1,this.__dataProviderController.loadFirstPage()):this.size>0&&this.__dataProviderController.ensureFlatIndexLoaded(0))}__onDataProviderPageRequested(){this.loading=!0}__onDataProviderPageLoaded(){let{rootCache:e}=this.__dataProviderController;e.items=[...e.items],this.__synchronizeControllerState(),!this.opened&&!this._isInputFocused()&&this._commitValue()}clearCache(){this.dataProvider&&(this.__dataProviderController.clearCache(),this.__synchronizeControllerState(),this._shouldFetchData()?(this._forceNextRequest=!1,this.__dataProviderController.loadFirstPage()):this._forceNextRequest=!0)}_sizeChanged(e){let{rootCache:t}=this.__dataProviderController;t.size!==e&&(t.size=e,t.items=[...t.items],this.__synchronizeControllerState())}_filteredItemsChanged(e){if(super._filteredItemsChanged(e),this.dataProvider&&e){let{rootCache:t}=this.__dataProviderController;t.items!==e&&(t.items=e,this.__synchronizeControllerState())}}__synchronizeControllerState(){if(this.__dataProviderInitialized&&this.dataProvider){let{rootCache:e}=this.__dataProviderController;this.size=e.size,this.filteredItems=e.items,this.loading=this.__dataProviderController.isLoading()}}_pageSizeChanged(e,t){if(Math.floor(e)!==e||e<1)throw this.pageSize=t,new Error("`pageSize` value must be an integer > 0");this.__dataProviderController.setPageSize(e),this.clearCache()}_dataProviderChanged(e,t){this._ensureItemsOrDataProvider(()=>{this.dataProvider=t}),this.__dataProviderController.setDataProvider(e),this.clearCache()}_ensureItemsOrDataProvider(e){if(this.items!==void 0&&this.dataProvider!==void 0)throw e(),new Error("Using `items` and `dataProvider` together is not supported")}};var T=n=>class extends n{static get observers(){return["__clearPendingFocusOnFilter(filter)"]}__focusIndex(e){if(!(typeof e!="number"||Number.isNaN(e)||e<0)){if(!this._overlayOpened||!this._dropdownItems||this._dropdownItems.length===0){this.__pendingFocusIndex=e;return}if(!(e>=this._dropdownItems.length)){if(this._focusedIndex=e,this._scrollIntoView(e,!0),this.loading){this.__pendingFocusIndex=e;return}delete this.__pendingFocusIndex,requestAnimationFrame(()=>{this.isConnected&&this._updateActiveDescendant(e)})}}}__focusPendingIndexIfNeeded(){this.__pendingFocusIndex!==void 0&&!this.loading&&this.__focusIndex(this.__pendingFocusIndex)}__clearPendingFocusOnFilter(){delete this.__pendingFocusIndex}_onOpened(){super._onOpened(),this.__focusPendingIndexIfNeeded()}__onDataProviderPageLoaded(){super.__onDataProviderPageLoaded(),this.__focusPendingIndexIfNeeded()}};function ce(n){return n!=null}function se(n,r){return n.findIndex(e=>e instanceof c?!1:r(e))}var $=n=>class extends ie(n){static get properties(){return{items:{type:Array,sync:!0,observer:"_itemsChanged"},filteredItems:{type:Array,observer:"_filteredItemsChanged",sync:!0},filter:{type:String,value:"",notify:!0,sync:!0},itemLabelGenerator:{type:Object},itemLabelPath:{type:String,value:"label",observer:"_itemLabelPathChanged",sync:!0},itemValuePath:{type:String,value:"value",sync:!0}}}updated(e){super.updated(e),e.has("filter")&&this._filterChanged(this.filter),e.has("itemLabelGenerator")&&this.requestContentUpdate()}_onInput(e){let t=this._inputElementValue,i={};this.filter===t?this._filterChanged(this.filter):i.filter=t,!this.opened&&!this._isClearButton(e)&&!this.autoOpenDisabled&&(i.opened=!0),this.setProperties(i)}_getItemLabel(e){if(typeof this.itemLabelGenerator=="function"&&e)return this.itemLabelGenerator(e)||"";let t=e&&this.itemLabelPath?z(this.itemLabelPath,e):void 0;return t==null&&(t=e?e.toString():""),t}_getItemValue(e){let t=e&&this.itemValuePath?z(this.itemValuePath,e):void 0;return t===void 0&&(t=e?e.toString():""),t}_itemLabelPathChanged(e){typeof e!="string"&&console.error("You should set itemLabelPath to a valid string")}_filterChanged(e){this._scrollIntoView(0),this._focusedIndex=-1,this.items?this.filteredItems=this._filterItems(this.items,e):this._filteredItemsChanged(this.filteredItems)}_itemsChanged(e,t){this._ensureItemsOrDataProvider(()=>{this.items=t}),e?this.filteredItems=e.slice(0):t&&(this.filteredItems=null)}_filteredItemsChanged(e){this._setDropdownItems(e)}_setDropdownItems(){}_filterItems(e,t){return e&&e.filter(o=>(t=t?t.toString().toLowerCase():"",this._getItemLabel(o).toString().toLowerCase().indexOf(t)>-1))}__getItemIndexByValue(e,t){return!e||!ce(t)?-1:se(e,i=>this._getItemValue(i)===t)}__getItemIndexByLabel(e,t){return!e||!t?-1:se(e,i=>this._getItemLabel(i).toString().toLowerCase()===t.toString().toLowerCase())}};function pe(n){return n!=null}var ne=n=>class extends Z($(n)){static get properties(){return{renderer:{type:Object,sync:!0},allowCustomValue:{type:Boolean,value:!1},loading:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},selectedItem:{type:Object,notify:!0,sync:!0},itemClassNameGenerator:{type:Object},itemIdPath:{type:String,sync:!0},__keepOverlayOpened:{type:Boolean,sync:!0}}}static get observers(){return["_openedOrItemsChanged(opened, _dropdownItems, loading, __keepOverlayOpened)","_selectedItemChanged(selectedItem, itemValuePath, itemLabelPath)","_updateScroller(opened, _dropdownItems, _focusedIndex, _theme)"]}ready(){super.ready(),this._lastCommittedValue=this.value}requestContentUpdate(){this._scroller&&(this._scroller.requestContentUpdate(),this._getItemElements().forEach(e=>{e.requestContentUpdate()}))}updated(e){super.updated(e),["loading","itemIdPath","itemClassNameGenerator","renderer","selectedItem"].forEach(t=>{e.has(t)&&(this._scroller[t]=this[t])})}_updateScroller(e,t,i,o){e&&(this._scroller.style.maxHeight=getComputedStyle(this).getPropertyValue(`--${this._tagNamePrefix}-overlay-max-height`)||"65vh");let s=this.hasAttribute("closing");this._scroller.setProperties({items:e||s?t:[],opened:e,focusedIndex:i,theme:o})}_openedOrItemsChanged(e,t,i,o){this._overlayOpened=e&&(o||i||!!t?.length)}_onClearButtonClick(e){super._onClearButtonClick(e),this.opened&&this.requestContentUpdate()}_inputElementChanged(e){super._inputElementChanged(e),e&&this._revertInputValueToValue()}_closeOrCommit(){!this.opened&&!this.loading?this._commitValue():this.close()}_hasValidInputValue(){let e=this._focusedIndex<0&&this._inputElementValue!==""&&this._getItemLabel(this.selectedItem)!==this._inputElementValue;return this.allowCustomValue||!e}_onEscapeCancel(){this.cancel()}_onClearAction(){this.selectedItem=null,this.allowCustomValue&&(this.value=""),this._detectAndDispatchChange()}_clearFilter(){this.filter=""}cancel(){this._revertInputValueToValue(),this._lastCommittedValue=this.value,this._closeOrCommit()}_onOpened(){this.dispatchEvent(new CustomEvent("vaadin-combo-box-dropdown-opened",{bubbles:!0,composed:!0})),this._lastCommittedValue=this.value}_onOverlayClosed(){this.dispatchEvent(new CustomEvent("vaadin-combo-box-dropdown-closed",{bubbles:!0,composed:!0}))}_onClosed(){(!this.loading||this.allowCustomValue)&&this._commitValue()}_commitValue(){if(this._focusedIndex>-1){let e=this._dropdownItems[this._focusedIndex];this.selectedItem!==e&&(this.selectedItem=e),this._inputElementValue=this._getItemLabel(this.selectedItem),this._focusedIndex=-1}else if(this._inputElementValue===""||this._inputElementValue===void 0)this.selectedItem=null,this.allowCustomValue&&(this.value="");else{let e=[this.selectedItem,...this._dropdownItems||[]],t=e[this.__getItemIndexByLabel(e,this._inputElementValue)];if(this.allowCustomValue&&!t){let i=this._inputElementValue;this._lastCustomValue=i;let o=new CustomEvent("custom-value-set",{detail:i,composed:!0,cancelable:!0,bubbles:!0});this.dispatchEvent(o),o.defaultPrevented||(this.value=i)}else!this.allowCustomValue&&!this.opened&&t?this.value=this._getItemValue(t):this._revertInputValueToValue()}this._detectAndDispatchChange(),this._clearSelectionRange(),this._clearFilter()}_onChange(e){e.stopPropagation()}_revertInputValue(){this.filter!==""?this._inputElementValue=this.filter:this._revertInputValueToValue(),this._clearSelectionRange()}_revertInputValueToValue(){this.allowCustomValue&&!this.selectedItem?this._inputElementValue=this.value:this._inputElementValue=this._getItemLabel(this.selectedItem)}_selectedItemChanged(e){if(e==null)this.filteredItems&&(this.allowCustomValue||(this.value=""),this._toggleHasValue(this._hasValue),this._inputElementValue=this.value);else{let t=this._getItemValue(e);if(this.value!==t&&(this.value=t,this.value!==t))return;this._toggleHasValue(!0),this._inputElementValue=this._getItemLabel(e)}}_valueChanged(e,t){e===""&&t===void 0||(pe(e)?(this._getItemValue(this.selectedItem)!==e&&this._selectItemForValue(e),!this.selectedItem&&this.allowCustomValue&&(this._inputElementValue=e),this._toggleHasValue(this._hasValue)):this.selectedItem=null,this._clearFilter(),this._lastCommittedValue=void 0)}_detectAndDispatchChange(){document.hasFocus()&&this._requestValidation(),this.value!==this._lastCommittedValue&&(this.dispatchEvent(new CustomEvent("change",{bubbles:!0})),this._lastCommittedValue=this.value)}_selectItemForValue(e){let t=this.__getItemIndexByValue(this.filteredItems,e),i=this.selectedItem;t>=0?this.selectedItem=this.filteredItems[t]:this.dataProvider&&this.selectedItem===void 0?this.selectedItem=void 0:this.selectedItem=null,this.selectedItem===null&&i===null&&this._selectedItemChanged(this.selectedItem)}_setDropdownItems(e){let t=this._dropdownItems;this._dropdownItems=e;let i=t?t[this._focusedIndex]:null,o=this.__getItemIndexByValue(e,this.value);if((this.selectedItem===null||this.selectedItem===void 0)&&o>=0&&(this.selectedItem=e[o]),t&&t[this._focusedIndex]instanceof c&&e[this._focusedIndex]instanceof c)return;let s=this.__getItemIndexByValue(e,this._getItemValue(i));s>-1?this._focusedIndex=s:this._focusedIndex=this.__getItemIndexByLabel(e,this.filter)}_handleFocusOut(){if(!this.opened&&this.allowCustomValue&&this._inputElementValue===this._lastCustomValue){delete this._lastCustomValue;return}super._handleFocusOut()}};var W=class extends T(k(ne(ee(L(m(E(h(u(d))))))))){static get is(){return"vaadin-combo-box"}static get styles(){return[M,D]}static get properties(){return{_positionTarget:{type:Object}}}get clearElement(){return this.$.clearButton}render(){return l`
      <div class="vaadin-combo-box-container">
        <div part="label">
          <slot name="label"></slot>
          <span part="required-indicator" aria-hidden="true" @click="${this.focus}"></span>
        </div>

        <vaadin-input-container
          part="input-field"
          .readonly="${this.readonly}"
          .disabled="${this.disabled}"
          .invalid="${this.invalid}"
          theme="${g(this._theme)}"
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

      <vaadin-combo-box-overlay
        id="overlay"
        exportparts="overlay, content, loader"
        .owner="${this}"
        .dir="${this.dir}"
        .opened="${this._overlayOpened}"
        ?loading="${this.loading}"
        theme="${g(this._theme)}"
        .positionTarget="${this._positionTarget}"
        no-vertical-overlap
      >
        <slot name="overlay"></slot>
      </vaadin-combo-box-overlay>
    `}ready(){super.ready(),this.addController(new F(this,r=>{this._setInputElement(r),this._setFocusElement(r),this.stateTarget=r,this.ariaTarget=r})),this.addController(new O(this.inputElement,this._labelController)),this._tooltipController=new S(this),this.addController(this._tooltipController),this._tooltipController.setPosition("top"),this._tooltipController.setAriaTarget(this.inputElement),this._tooltipController.setShouldShow(r=>!r.opened),this._positionTarget=this.shadowRoot.querySelector('[part="input-field"]'),this._toggleElement=this.$.toggleButton}updated(r){super.updated(r),(r.has("dataProvider")||r.has("value"))&&this._warnDataProviderValue(this.dataProvider,this.value)}_onClearButtonClick(r){r.stopPropagation(),super._onClearButtonClick(r)}_onHostClick(r){let e=r.composedPath();(e.includes(this._labelNode)||e.includes(this._positionTarget))&&super._onHostClick(r)}_warnDataProviderValue(r,e){if(r&&e!==""&&(this.selectedItem===void 0||this.selectedItem===null)){let t=this.__getItemIndexByValue(this.filteredItems,e);(t<0||!this._getItemLabel(this.filteredItems[t]))&&console.warn("Warning: unable to determine the label for the provided `value`. Nothing to display in the text field. This usually happens when setting an initial `value` before any items are returned from the `dataProvider` callback. Consider setting `selectedItem` instead of `value`")}}};a(W);var le=_`
  :host {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    white-space: nowrap;
    box-sizing: border-box;
    gap: var(--vaadin-chip-gap, 0);
    background: var(--vaadin-chip-background, var(--vaadin-background-container));
    color: var(--vaadin-chip-text-color, var(--vaadin-text-color));
    font-size: max(11px, var(--vaadin-chip-font-size, 0.875em));
    font-weight: var(--vaadin-chip-font-weight, 500);
    line-height: var(--vaadin-input-field-value-line-height, inherit);
    padding: 0 var(--vaadin-chip-padding, 0.3em);
    height: var(--vaadin-chip-height, calc(1lh / 0.875));
    border-radius: var(--vaadin-chip-border-radius, var(--vaadin-radius-m));
    border: var(--vaadin-chip-border-width, 1px) solid
      var(--vaadin-chip-border-color, var(--vaadin-border-color-secondary));
    cursor: default;
  }

  :host(:not([slot='overflow'])) {
    min-width: min(max-content, var(--vaadin-multi-select-combo-box-chip-min-width, 48px));
  }

  :host([focused]) {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-chip-border-width, 1px) * -1);
  }

  [part='label'] {
    overflow: hidden;
    text-overflow: ellipsis;
    margin-block: calc(var(--vaadin-chip-border-width, 1px) * -1);
  }

  [part='remove-button'] {
    flex: none;
    display: block;
    margin-inline-start: auto;
    margin-block: calc(var(--vaadin-chip-border-width, 1px) * -1);
    color: var(--vaadin-chip-remove-button-text-color, var(--vaadin-text-color-secondary));
    cursor: var(--vaadin-clickable-cursor);
    translate: 25%;
  }

  [part='remove-button']::before {
    content: '';
    display: block;
    width: var(--vaadin-icon-size, 1lh);
    height: var(--vaadin-icon-size, 1lh);
    background: currentColor;
    mask-image: var(--_vaadin-icon-cross);
  }

  :host([disabled]) {
    cursor: var(--vaadin-disabled-cursor);
  }

  :host([disabled]) [part='label'] {
    --vaadin-chip-text-color: var(--vaadin-text-color-disabled);
  }

  :host([hidden]),
  :host(:is([readonly], [disabled], [slot='overflow'])) [part='remove-button'] {
    display: none !important;
  }

  :host([slot='overflow']) {
    position: relative;
    margin-inline-start: 8px;
    min-width: 1.5em;
  }

  :host([slot='overflow'])::before,
  :host([slot='overflow'])::after {
    content: '';
    position: absolute;
    inset: calc(var(--vaadin-chip-border-width, 1px) * -1);
    border-inline-start: 2px solid var(--vaadin-chip-border-color, var(--vaadin-border-color-secondary));
    border-radius: inherit;
  }

  :host([slot='overflow'])::before {
    left: calc(-4px - var(--vaadin-chip-border-width, 1px));
  }

  :host([slot='overflow'])::after {
    left: calc(-8px - var(--vaadin-chip-border-width, 1px));
  }

  :host([count='2']) {
    margin-inline-start: 4px;
  }

  :host([count='1']) {
    margin-inline-start: 0;
  }

  :host([count='2'])::after,
  :host([count='1'])::before,
  :host([count='1'])::after {
    display: none;
  }

  @media (forced-colors: active) {
    :host {
      border: 1px solid !important;
    }

    [part='remove-button']::before {
      background: CanvasText;
    }
  }
`;var R=class extends m(h(u(d))){static get is(){return"vaadin-multi-select-combo-box-chip"}static get styles(){return le}static get properties(){return{disabled:{type:Boolean,reflectToAttribute:!0,sync:!0},readonly:{type:Boolean,reflectToAttribute:!0,sync:!0},label:{type:String,sync:!0},item:{type:Object}}}render(){return l`
      <div part="label">${this.label}</div>
      <div part="remove-button" @click="${this._onRemoveClick}"></div>
    `}_onRemoveClick(r){r.stopPropagation(),this.dispatchEvent(new CustomEvent("item-removed",{detail:{item:this.item},bubbles:!0,composed:!0}))}};a(R);var j=class extends J{static get is(){return"vaadin-multi-select-combo-box-container"}static get styles(){return[super.styles,_`
        #wrapper {
          display: flex;
          width: 100%;
          min-width: 0;
          gap: var(--_wrapper-gap);
          align-self: start;
        }

        :host([auto-expand-vertically]) #wrapper {
          flex-wrap: wrap;
        }
      `]}static get properties(){return{autoExpandVertically:{type:Boolean,reflectToAttribute:!0}}}render(){return l`
      <div id="wrapper">
        <slot name="prefix"></slot>
        <slot></slot>
      </div>
      <slot name="suffix"></slot>
    `}};a(j);var U=class extends I(m(f(h(u(d))))){static get is(){return"vaadin-multi-select-combo-box-item"}static get styles(){return[b,x]}render(){return l`
      <span part="checkmark" aria-hidden="true"></span>
      <div part="content">
        <slot></slot>
      </div>
    `}};a(U);var ae=[A,_`
    #overlay {
      width: var(
        --vaadin-multi-select-combo-box-overlay-width,
        var(--_vaadin-multi-select-combo-box-overlay-default-width, auto)
      );
    }
  `];var H=class extends w(C(f(m(h(u(d)))))){static get is(){return"vaadin-multi-select-combo-box-overlay"}static get styles(){return[y,ae]}render(){return l`
      <div part="overlay" id="overlay">
        <div part="loader"></div>
        <div part="content" id="content"><slot></slot></div>
      </div>
    `}};a(H);var de=P;var K=class extends V(h(d)){static get is(){return"vaadin-multi-select-combo-box-scroller"}static get styles(){return de}render(){return l`
      <div id="selector">
        <slot></slot>
      </div>
    `}ready(){super.ready(),this.setAttribute("aria-multiselectable","true")}_isItemSelected(r,e,t){return r instanceof c||this.owner.readonly?!1:this.owner._findIndex(r,this.owner.selectedItems,t)>-1}_updateElement(r,e){super._updateElement(r,e),r.toggleAttribute("readonly",this.owner.readonly)}};a(K);var he=[D,_`
    :host {
      max-width: 100%;
      --_input-min-width: var(--vaadin-multi-select-combo-box-input-min-width, 4rem);
      --_chip-min-width: var(--vaadin-multi-select-combo-box-chip-min-width, 48px);
      --_wrapper-gap: var(--vaadin-multi-select-combo-box-chips-gap, 2px);
    }

    #chips {
      display: flex;
      align-items: center;
      gap: var(--vaadin-multi-select-combo-box-chips-gap, 2px);
    }

    ::slotted(input) {
      box-sizing: border-box;
      flex: 1 0 var(--_input-min-width);
    }

    ::slotted([slot='chip']),
    ::slotted([slot='overflow']) {
      flex: 0 1 auto;
    }

    ::slotted([slot='chip']) {
      overflow: hidden;
    }

    :host(:is([readonly], [disabled])) ::slotted(input) {
      flex-grow: 0;
      flex-basis: 0;
      padding: 0;
    }

    :host([readonly]:not([disabled])) [part~='toggle-button'] {
      display: block;
      color: var(--vaadin-input-field-button-text-color, var(--vaadin-text-color-secondary));
    }

    :host([readonly]:not([disabled])) [part$='button'] {
      cursor: var(--vaadin-clickable-cursor);
    }

    :host([auto-expand-vertically]) #chips {
      display: contents;
    }

    :host([auto-expand-horizontally]) {
      --vaadin-field-default-width: auto;
    }
  `];var _e={cleared:"Selection cleared",focused:"focused. Press Backspace to remove",selected:"added to selection",deselected:"removed from selection",total:"{count} items selected"},ue=n=>class extends oe(T(k($(L(re(n)))))){static get properties(){return{autoExpandHorizontally:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},autoExpandVertically:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},collapseChips:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},itemClassNameGenerator:{type:Object,sync:!0},itemIdPath:{type:String,sync:!0},keepFilter:{type:Boolean,value:!1},loading:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},readonly:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},selectedItems:{type:Array,value:()=>[],notify:!0,sync:!0},allowCustomValue:{type:Boolean,value:!1},placeholder:{type:String,observer:"_placeholderChanged",reflectToAttribute:!0,sync:!0},renderer:{type:Function,sync:!0},selectedItemsOnTop:{type:Boolean,value:!1,sync:!0},value:{type:String},_overflowItems:{type:Array,value:()=>[],sync:!0},_focusedChipIndex:{type:Number,value:-1,observer:"_focusedChipIndexChanged"},_lastFilter:{type:String,sync:!0},_topGroup:{type:Array,observer:"_topGroupChanged",sync:!0},_inputField:{type:Object}}}static get observers(){return["_selectedItemsChanged(selectedItems)","__openedOrItemsChanged(opened, _dropdownItems, loading, __keepOverlayOpened)","__updateOverflowChip(_overflow, _overflowItems, disabled, readonly)","__updateScroller(opened, _dropdownItems, _focusedIndex, _theme)","__updateTopGroup(selectedItemsOnTop, selectedItems, opened)"]}static get defaultI18n(){return _e}get i18n(){return super.i18n}set i18n(e){super.i18n=e}get slotStyles(){let e=this.localName;return[...super.slotStyles,`
        ${e}[has-value] input::placeholder {
          color: transparent !important;
          forced-color-adjust: none;
        }
      `]}get clearElement(){return this.$.clearButton}get _chips(){return[...this.querySelectorAll('[slot="chip"]')]}get _hasValue(){return this.selectedItems&&this.selectedItems.length>0}get _tagNamePrefix(){return"vaadin-multi-select-combo-box"}ready(){super.ready(),this.addController(new F(this,e=>{this._setInputElement(e),this._setFocusElement(e),this.stateTarget=e,this.ariaTarget=e})),this.addController(new O(this.inputElement,this._labelController)),this._tooltipController=new S(this),this.addController(this._tooltipController),this._tooltipController.setPosition("top"),this._tooltipController.setAriaTarget(this.inputElement),this._tooltipController.setShouldShow(e=>!e.opened),this._toggleElement=this.$.toggleButton,this._inputField=this.shadowRoot.querySelector('[part="input-field"]'),this._overflowController=new X(this,"overflow","vaadin-multi-select-combo-box-chip",{initializer:e=>{e.addEventListener("mousedown",t=>this._preventBlur(t)),this._overflow=e}}),this.addController(this._overflowController)}updated(e){super.updated(e),["loading","itemIdPath","itemClassNameGenerator","renderer"].forEach(i=>{e.has(i)&&(this._scroller[i]=this[i])}),e.has("selectedItems")&&this.opened&&this.$.overlay._updateOverlayWidth(),["autoExpandHorizontally","autoExpandVertically","collapseChips","disabled","readonly","clearButtonVisible","itemClassNameGenerator"].some(i=>e.has(i))&&this.__updateChips(),e.has("readonly")&&(this._setDropdownItems(this.filteredItems),this.dataProvider&&this.clearCache())}checkValidity(){return this.required&&!this.readonly?this._hasValue:!0}open(){!this.disabled&&!(this.readonly&&this.selectedItems.length===0)&&(this.opened=!0)}clear(){this.__updateSelection([]),B(this.__effectiveI18n.cleared)}__syncTopGroup(){this._topGroup=this.selectedItemsOnTop?[...this.selectedItems]:[]}clearCache(){this.readonly||(super.clearCache(),this.__syncTopGroup())}_itemsChanged(e,t){super._itemsChanged(e,t),this.__syncTopGroup()}requestContentUpdate(){this._scroller&&(this._scroller.requestContentUpdate(),this._getItemElements().forEach(e=>{e.requestContentUpdate()}))}_onClearAction(){this.clear()}_onClosed(){this._ignoreCommitValue=!0,(!this.loading||this.allowCustomValue)&&this._commitValue()}__updateScroller(e,t,i,o){e&&(this._scroller.style.maxHeight=getComputedStyle(this).getPropertyValue(`--${this._tagNamePrefix}-overlay-max-height`)||"65vh");let s=this.hasAttribute("closing");this._scroller.setProperties({items:e||s?t:[],opened:e,focusedIndex:i,theme:o})}__openedOrItemsChanged(e,t,i,o){this._overlayOpened=e&&(o||i||!!t?.length)}_closeOrCommit(){this.opened?this.close():this._commitValue()}_commitValue(){this._lastFilter=this.filter,this._ignoreCommitValue?(this._inputElementValue="",this._focusedIndex=-1,this._ignoreCommitValue=!1):this.__commitUserInput(),(!this.keepFilter||!this.opened)&&(this.filter="")}__commitUserInput(){if(this._focusedIndex>-1){let e=this._dropdownItems[this._focusedIndex];this.__selectItem(e)}else if(this._inputElementValue){let e=[...this._dropdownItems],t=e[this.__getItemIndexByLabel(e,this._inputElementValue)];if(this.allowCustomValue&&!t){let i=this._inputElementValue;this._lastCustomValue=i,this.__clearInternalValue(!0),this.dispatchEvent(new CustomEvent("custom-value-set",{detail:i,composed:!0,bubbles:!0}))}else!this.allowCustomValue&&!this.opened&&t?this.__selectItem(t):this._inputElementValue=""}}_setFocused(e){let t=!e&&!this._closeOnBlurIsPrevented;t&&(this._ignoreCommitValue=!0),super._setFocused(e),t&&document.hasFocus()&&(this._focusedChipIndex=-1,this._requestValidation()),t&&this.readonly&&this.close()}_onResize(){this.__updateChips()}_delegateAttribute(e,t){if(this.stateTarget){if(e==="required"){this._delegateAttribute("aria-required",t?"true":!1);return}super._delegateAttribute(e,t)}}_placeholderChanged(e){let t=this.__tmpA11yPlaceholder;t!==e&&(this.__savedPlaceholder=e,t&&(this.placeholder=t))}_selectedItemsChanged(e){if(this._toggleHasValue(this._hasValue),this._hasValue){let t=this._mergeItemLabels(e);this.__tmpA11yPlaceholder===void 0&&(this.__savedPlaceholder=this.placeholder),this.__tmpA11yPlaceholder=t,this.placeholder=t}else this.__tmpA11yPlaceholder!==void 0&&(delete this.__tmpA11yPlaceholder,this.placeholder=this.__savedPlaceholder);this.__updateChips(),this.requestContentUpdate()}_topGroupChanged(e){e&&this._setDropdownItems(this.filteredItems)}_hasValidInputValue(){let e=this._focusedIndex<0&&this._inputElementValue!=="";return this.allowCustomValue||!e}_shouldFetchData(){return this.readonly?!1:super._shouldFetchData()}_setDropdownItems(e){if(this.readonly){this.__setDropdownItems(this.selectedItems);return}if(this.filter||!this.selectedItemsOnTop){this.__setDropdownItems(e);return}if(e?.length&&this._topGroup?.length){let t=e.filter(i=>this._findIndex(i,this._topGroup,this.itemIdPath)===-1);this.__setDropdownItems(this._topGroup.concat(t));return}this.__setDropdownItems(e)}__setDropdownItems(e){let t=this._dropdownItems;this._dropdownItems=e;let i=t?t[this._focusedIndex]:null;if(t&&t[this._focusedIndex]instanceof c&&e[this._focusedIndex]instanceof c)return;let o=this.__getItemIndexByValue(e,this._getItemValue(i));o>-1?this._focusedIndex=o:this._focusedIndex=this.__getItemIndexByLabel(e,this.filter)}_mergeItemLabels(e){return e.map(t=>this._getItemLabel(t)).join(", ")}_findIndex(e,t,i){if(i&&e){for(let o=0;o<t.length;o++)if(t[o]&&t[o][i]===e[i])return o;return-1}return t.indexOf(e)}__clearInternalValue(e=!1){!this.keepFilter||e?(this.filter="",this._inputElementValue=""):this._inputElementValue=this.filter}__announceItem(e,t,i){let o=t?"selected":"deselected",s=this.__effectiveI18n.total.replace("{count}",i||0);B(`${e} ${this.__effectiveI18n[o]} ${s}`)}__removeItem(e){let t=[...this.selectedItems];t.splice(t.indexOf(e),1),this.__updateSelection(t);let i=this._getItemLabel(e);this.__announceItem(i,!1,t.length)}__selectItem(e){let t=[...this.selectedItems],i=this._findIndex(e,t,this.itemIdPath),o=this._getItemLabel(e),s=!1;if(i!==-1){if(this._lastFilter?.toLowerCase()===o.toLowerCase()){this.__clearInternalValue();return}t.splice(i,1)}else t.push(e),s=!0;this.__updateSelection(t),this.__clearInternalValue(),this.__announceItem(o,s,t.length)}__updateSelection(e){this.selectedItems=e,this._requestValidation(),this.dispatchEvent(new CustomEvent("change",{bubbles:!0}))}__updateTopGroup(e,t,i){e?(!i||this.__needToSyncTopGroup())&&(this._topGroup=[...t]):this._topGroup=[]}__needToSyncTopGroup(){return this.itemIdPath?this._topGroup&&this._topGroup.some(e=>{let t=this.selectedItems[this._findIndex(e,this.selectedItems,this.itemIdPath)];return t&&e!==t}):!1}__createChip(e){let t=document.createElement("vaadin-multi-select-combo-box-chip");t.setAttribute("slot","chip"),t.item=e,t.disabled=this.disabled,t.readonly=this.readonly;let i=this._getItemLabel(e);return t.label=i,t.setAttribute("title",i),typeof this.itemClassNameGenerator=="function"&&(t.className=this.itemClassNameGenerator(e)),t.addEventListener("item-removed",o=>this._onItemRemoved(o)),t.addEventListener("mousedown",o=>this._preventBlur(o)),t}__getWrapperWidth(){return this._inputField.$.wrapper.clientWidth}__getOverflowWidth(){let e=this._overflow;e.style.visibility="hidden",e.removeAttribute("hidden");let t=e.getAttribute("count");e.setAttribute("count","99");let i=getComputedStyle(e),o=e.clientWidth+parseInt(i.marginInlineStart);return e.setAttribute("count",t),e.setAttribute("hidden",""),e.style.visibility="",o}__updateChips(){if(!this._inputField||!this.inputElement)return;if(this._chips.forEach(t=>{t.remove()}),this.selectedItems.length===0){this._overflowItems=[];return}if(this.autoExpandVertically){this.selectedItems.forEach(t=>{this.appendChild(this.__createChip(t))}),this._overflowItems=[];return}let e=parseInt(getComputedStyle(this.inputElement).flexBasis);this.collapseChips?this._overflowItems=this.__updateChipsCollapsed(this.selectedItems,e):this.autoExpandHorizontally?this._overflowItems=this.__updateChipsHorizontalExpand(this.selectedItems,e):this._overflowItems=this.__updateChipsDefault(this.selectedItems,e)}__renderAllChips(e,t){let i=e.map(s=>{let p=this.__createChip(s);return this.appendChild(p),p}),o=this.__getWrapperWidth()-this.$.chips.clientWidth>=t;return{chips:i,allChipsFit:o}}__updateChipsCollapsed(e,t){let{chips:i,allChipsFit:o}=this.__renderAllChips(e,t);return o?[]:(i.forEach(s=>s.remove()),e.slice())}__updateChipsHorizontalExpand(e,t){let{chips:i,allChipsFit:o}=this.__renderAllChips(e,t);if(o)return[];let s=this.__getOverflowWidth(),p=i.length;for(;p>1&&(p-=1,i[p].remove(),!(this.__getWrapperWidth()-this.$.chips.clientWidth>=t+s)););if(p===1){let v=parseInt(getComputedStyle(this).getPropertyValue("--_chip-min-width")),me=this.__getWrapperWidth()-t-s;i[0].style.maxWidth=`${Math.max(v,me)}px`}return e.slice(p)}__updateChipsDefault(e,t){let i=this.__getWrapperWidth()-t;e.length>1&&(i-=this.__getOverflowWidth());let o=parseInt(getComputedStyle(this).getPropertyValue("--_chip-min-width"));for(let s=e.length-1,p=null;s>=0;s--){let v=this.__createChip(e[s]);if(this.insertBefore(v,p),this.$.chips.clientWidth>i&&(i<o||p!==null))return v.remove(),e.slice(0,s+1);v.style.maxWidth=`${i}px`,p=v}return[]}__updateOverflowChip(e,t,i,o){if(e){let s=t.length;e.label=`${s}`,e.setAttribute("count",`${s}`),e.setAttribute("title",this._mergeItemLabels(t)),e.toggleAttribute("hidden",s===0),e.disabled=i,e.readonly=o}}_onClearButtonClick(e){e.stopPropagation(),super._onClearButtonClick(e),this.opened&&this.requestContentUpdate()}_onChange(e){e.stopPropagation()}_onEscape(e){if(this.readonly){e.stopPropagation(),this.opened&&this.close();return}this.clearButtonVisible&&!this.opened&&this.selectedItems&&this.selectedItems.length&&(e.stopPropagation(),this._onClearAction()),super._onEscape(e)}_onEscapeCancel(){this._closeOrCommit()}_onEnter(e){if(this.opened){if(e.preventDefault(),e.stopPropagation(),this.readonly)this.close();else if(this._hasValidInputValue()){let t=this._dropdownItems[this._focusedIndex];this._commitValue(),this._focusedIndex=this._dropdownItems.indexOf(t)}return}super._onEnter(e)}_onArrowDown(){this.readonly?this.opened||this.open():super._onArrowDown()}_onArrowUp(){this.readonly?this.opened||this.open():super._onArrowUp()}_onKeyDown(e){super._onKeyDown(e);let t=this._chips;if(!this.readonly&&t.length>0)switch(e.key){case"Backspace":this._onBackSpace(t);break;case"ArrowLeft":this._onArrowLeft(t,e);break;case"ArrowRight":this._onArrowRight(t,e);break;default:this._focusedChipIndex=-1;break}}_onArrowLeft(e,t){if(this.inputElement.selectionStart!==0)return;let i=this._focusedChipIndex;i!==-1&&t.preventDefault();let o;this.__isRTL?i===e.length-1?o=-1:i>-1&&(o=i+1):i===-1?o=e.length-1:i>0&&(o=i-1),o!==void 0&&(this._focusedChipIndex=o)}_onArrowRight(e,t){if(this.inputElement.selectionStart!==0)return;let i=this._focusedChipIndex;i!==-1&&t.preventDefault();let o;this.__isRTL?i===-1?o=e.length-1:i>0&&(o=i-1):i===e.length-1?o=-1:i>-1&&(o=i+1),o!==void 0&&(this._focusedChipIndex=o)}_onBackSpace(e){if(this.inputElement.selectionStart!==0)return;let t=this._focusedChipIndex;t===-1?this._focusedChipIndex=e.length-1:(this.__removeItem(e[t].item),this._focusedChipIndex=-1)}_focusedChipIndexChanged(e,t){if(e>-1||t>-1){let i=this._chips;if(i.forEach((o,s)=>{o.toggleAttribute("focused",s===e)}),e>-1){let o=i[e].item,s=this._getItemLabel(o);B(`${s} ${this.__effectiveI18n.focused}`)}}}_overlaySelectedItemChanged(e){e.stopPropagation(),!this.hasAttribute("closing")&&(this.readonly||e.detail.item instanceof c||this.opened&&(this._lastFilter=this._inputElementValue,this.__selectItem(e.detail.item)))}_onItemRemoved(e){this.__removeItem(e.detail.item)}_preventBlur(e){e.preventDefault()}};var Y=class extends ue(m(E(h(u(d))))){static get is(){return"vaadin-multi-select-combo-box"}static get styles(){return[M,he]}render(){return l`
      <div class="vaadin-multi-select-combo-box-container">
        <div part="label">
          <slot name="label"></slot>
          <span part="required-indicator" aria-hidden="true" @click="${this.focus}"></span>
        </div>

        <vaadin-multi-select-combo-box-container
          part="input-field"
          .autoExpandVertically="${this.autoExpandVertically}"
          .readonly="${this.readonly}"
          .disabled="${this.disabled}"
          .invalid="${this.invalid}"
          theme="${g(this._theme)}"
        >
          <slot name="overflow" slot="prefix"></slot>
          <div id="chips" part="chips" slot="prefix">
            <slot name="chip"></slot>
          </div>
          <slot name="input"></slot>
          <div id="clearButton" part="field-button clear-button" slot="suffix" aria-hidden="true"></div>
          <div id="toggleButton" part="field-button toggle-button" slot="suffix" aria-hidden="true"></div>
        </vaadin-multi-select-combo-box-container>

        <div part="helper-text">
          <slot name="helper"></slot>
        </div>

        <div part="error-message">
          <slot name="error-message"></slot>
        </div>

        <slot name="tooltip"></slot>
      </div>

      <vaadin-multi-select-combo-box-overlay
        id="overlay"
        exportparts="overlay, content, loader"
        .owner="${this}"
        .dir="${this.dir}"
        .opened="${this._overlayOpened}"
        ?loading="${this.loading}"
        theme="${g(this._theme)}"
        .positionTarget="${this._inputField}"
        no-vertical-overlap
      >
        <slot name="overlay"></slot>
      </vaadin-multi-select-combo-box-overlay>
    `}};a(Y);
/*! Bundled license information:

@vaadin/combo-box/src/vaadin-combo-box-item.js:
@vaadin/combo-box/src/styles/vaadin-combo-box-overlay-base-styles.js:
@vaadin/combo-box/src/vaadin-combo-box-overlay.js:
@vaadin/combo-box/src/vaadin-combo-box-scroller.js:
@vaadin/combo-box/src/styles/vaadin-combo-box-base-styles.js:
@vaadin/combo-box/src/vaadin-combo-box-data-provider-mixin.js:
@vaadin/combo-box/src/vaadin-combo-box-focus-index-mixin.js:
@vaadin/combo-box/src/vaadin-combo-box-items-mixin.js:
@vaadin/combo-box/src/vaadin-combo-box-mixin.js:
@vaadin/combo-box/src/vaadin-combo-box.js:
  (**
   * @license
   * Copyright (c) 2015 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/multi-select-combo-box/src/styles/vaadin-multi-select-combo-box-chip-base-styles.js:
@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-chip.js:
@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-container.js:
@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-item.js:
@vaadin/multi-select-combo-box/src/styles/vaadin-multi-select-combo-box-overlay-base-styles.js:
@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-overlay.js:
@vaadin/multi-select-combo-box/src/styles/vaadin-multi-select-combo-box-scroller-base-styles.js:
@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-scroller.js:
@vaadin/multi-select-combo-box/src/styles/vaadin-multi-select-combo-box-base-styles.js:
@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-mixin.js:
@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box.js:
  (**
   * @license
   * Copyright (c) 2021 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)
*/
