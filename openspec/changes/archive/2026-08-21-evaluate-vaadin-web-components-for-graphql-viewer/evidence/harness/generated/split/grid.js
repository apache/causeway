import{a as be}from"./chunks/chunk-6DJFYO7X.js";import{a as U,b as Z,c as Ce}from"./chunks/chunk-V7PLVXUQ.js";import{B as ce,H as E,I as G,J as C,K as f,L as _e,M as ue,N as fe,O as ge,Q as pe,S as me,b as ie,c as re,f as H,g as B,h as oe,j as A,k as N,l as ne,n as se,o as Y,p as $,q as ae,r as J,s as W,t as V,v as le,w as de,x as he}from"./chunks/chunk-JAZKWOIA.js";function b(a){return a.__cells||Array.from(a.querySelectorAll('[part~="cell"]:not([part~="details-cell"])'))}function m(a,r){[...a.children].forEach(r)}function x(a,r){b(a).forEach(r),a.__detailsCell&&r(a.__detailsCell)}function ve(a,r,e){let t=1;a.forEach(i=>{t%10===0&&(t+=1),i._order=e+t*r,t+=1})}function L(a,r,e){switch(typeof e){case"boolean":a.toggleAttribute(r,e);break;case"string":a.setAttribute(r,e);break;default:a.removeAttribute(r);break}}function u(a,r,e){a.classList.toggle(r,e||e===""),a.part.toggle(r,e||e===""),a.part.length===0&&a.removeAttribute("part")}function D(a,r,e){a.forEach(t=>{u(t,r,e)})}function S(a,r){let e=b(a);Object.entries(r).forEach(([t,i])=>{L(a,t,i);let o=`${t}-row`;u(a,o,i),D(e,`${o}-cell`,i)})}function Q(a,r){let e=b(a);Object.entries(r).forEach(([t,i])=>{let o=a.getAttribute(t);if(L(a,t,i),o){let n=`${t}-${o}-row`;u(a,n,!1),D(e,`${n}-cell`,!1)}if(i){let n=`${t}-${i}-row`;u(a,n,i),D(e,`${n}-cell`,i)}})}function R(a,r,e,t,i){L(a,r,e),i&&u(a,i,!1),u(a,t||`${r}-cell`,e)}function ye(a){return b(a).find(r=>r._content.querySelector("vaadin-grid-tree-toggle"))}var F=class a{constructor(r,e){this.__host=r,this.__callback=e,this.__currentSlots=[],this.__onMutation=this.__onMutation.bind(this),this.__observer=new MutationObserver(this.__onMutation),this.__observer.observe(r,{childList:!0}),this.__initialCallDebouncer=f.debounce(this.__initialCallDebouncer,C,()=>this.__onMutation())}disconnect(){this.__observer.disconnect(),this.__initialCallDebouncer.cancel(),this.__toggleSlotChangeListeners(!1)}flush(){this.__onMutation()}__toggleSlotChangeListeners(r){this.__currentSlots.forEach(e=>{r?e.addEventListener("slotchange",this.__onMutation):e.removeEventListener("slotchange",this.__onMutation)})}__onMutation(){let r=!this.__currentColumns;this.__currentColumns||=[];let e=a.getColumns(this.__host),t=e.filter(s=>!this.__currentColumns.includes(s)),i=this.__currentColumns.filter(s=>!e.includes(s)),o=this.__currentColumns.some((s,l)=>s!==e[l]);this.__currentColumns=e,this.__toggleSlotChangeListeners(!1),this.__currentSlots=[...this.__host.children].filter(s=>s instanceof HTMLSlotElement),this.__toggleSlotChangeListeners(!0),(r||t.length||i.length||o)&&this.__callback(t,i)}static __isColumnElement(r){return r.nodeType===Node.ELEMENT_NODE&&/\bcolumn\b/u.test(r.localName)}static getColumns(r){let e=[],t=r._isColumnElement||a.__isColumnElement;return[...r.children].forEach(i=>{t(i)?e.push(i):i instanceof HTMLSlotElement&&[...i.assignedElements({flatten:!0})].filter(o=>t(o)).forEach(o=>e.push(o))}),e}};var we=a=>class extends a{static get properties(){return{resizable:{type:Boolean,sync:!0,value(){if(this.localName==="vaadin-grid-column-group")return;let e=this.parentNode;return e?.localName==="vaadin-grid-column-group"&&e.resizable||!1}},frozen:{type:Boolean,value:!1,sync:!0},frozenToEnd:{type:Boolean,value:!1,sync:!0},rowHeader:{type:Boolean,value:!1,sync:!0},hidden:{type:Boolean,value:!1,sync:!0},header:{type:String,sync:!0},textAlign:{type:String,sync:!0},headerPartName:{type:String,sync:!0},footerPartName:{type:String,sync:!0},_lastFrozen:{type:Boolean,value:!1,sync:!0},_bodyContentHidden:{type:Boolean,value:!1,sync:!0},_firstFrozenToEnd:{type:Boolean,value:!1,sync:!0},_order:{type:Number,sync:!0},_reorderStatus:{type:Boolean,sync:!0},_emptyCells:Array,_headerCell:{type:Object,sync:!0},_footerCell:{type:Object,sync:!0},_grid:Object,__initialized:{type:Boolean,value:!0},headerRenderer:{type:Function,sync:!0},_headerRenderer:{type:Function,computed:"_computeHeaderRenderer(headerRenderer, header, __initialized)"},footerRenderer:{type:Function,sync:!0},_footerRenderer:{type:Function,computed:"_computeFooterRenderer(footerRenderer, __initialized)"},__gridColumnElement:{type:Boolean,value:!0}}}static get observers(){return["_widthChanged(width, _headerCell, _footerCell, _cells)","_frozenChanged(frozen, _headerCell, _footerCell, _cells)","_frozenToEndChanged(frozenToEnd, _headerCell, _footerCell, _cells)","_flexGrowChanged(flexGrow, _headerCell, _footerCell, _cells)","_textAlignChanged(textAlign, _cells, _headerCell, _footerCell)","_lastFrozenChanged(_lastFrozen)","_firstFrozenToEndChanged(_firstFrozenToEnd)","_onRendererOrBindingChanged(_renderer, _cells, _bodyContentHidden, path)","_onHeaderRendererOrBindingChanged(_headerRenderer, _headerCell, path, header)","_onFooterRendererOrBindingChanged(_footerRenderer, _footerCell)","_resizableChanged(resizable, _headerCell)","_reorderStatusChanged(_reorderStatus, _headerCell, _footerCell, _cells)","_hiddenChanged(hidden, _headerCell, _footerCell, _cells)","_rowHeaderChanged(rowHeader, _cells)","__headerFooterPartNameChanged(_headerCell, _footerCell, headerPartName, footerPartName)"]}get _grid(){return this._gridValue||(this._gridValue=this._findHostGrid()),this._gridValue}get _allCells(){return[].concat(this._cells||[]).concat(this._emptyCells||[]).concat(this._headerCell).concat(this._footerCell).filter(e=>e)}connectedCallback(){super.connectedCallback(),requestAnimationFrame(()=>{this._grid&&this._allCells.forEach(e=>{e._content.parentNode||this._grid.appendChild(e._content)})})}disconnectedCallback(){super.disconnectedCallback(),requestAnimationFrame(()=>{this._grid||this._allCells.forEach(e=>{e._content.parentNode&&e._content.parentNode.removeChild(e._content)})}),this._gridValue=void 0}_findHostGrid(){let e=this;for(;e&&!/^vaadin.*grid(-pro)?$/u.test(e.localName);)e=e.assignedSlot?e.assignedSlot.parentNode:e.parentNode;return e||void 0}_renderHeaderAndFooter(){this._renderHeaderCellContent(this._headerRenderer,this._headerCell),this._renderFooterCellContent(this._footerRenderer,this._footerCell)}_flexGrowChanged(e){this.parentElement&&this.parentElement._columnPropChanged&&this.parentElement._columnPropChanged("flexGrow"),this._allCells.forEach(t=>{t.style.flexGrow=e})}_widthChanged(e){this.parentElement&&this.parentElement._columnPropChanged&&this.parentElement._columnPropChanged("width"),this._allCells.forEach(t=>{t.style.width=e})}_frozenChanged(e){this.parentElement&&this.parentElement._columnPropChanged&&this.parentElement._columnPropChanged("frozen",e),this._allCells.forEach(t=>{R(t,"frozen",e)}),this._grid&&this._grid._frozenCellsChanged&&this._grid._frozenCellsChanged()}_frozenToEndChanged(e){this.parentElement&&this.parentElement._columnPropChanged&&this.parentElement._columnPropChanged("frozenToEnd",e),this._allCells.forEach(t=>{this._grid&&t.parentElement===this._grid.$.sizer||R(t,"frozen-to-end",e)}),this._grid&&this._grid._frozenCellsChanged&&this._grid._frozenCellsChanged()}_lastFrozenChanged(e){this._allCells.forEach(t=>{R(t,"last-frozen",e)}),this.parentElement&&this.parentElement._columnPropChanged&&(this.parentElement._lastFrozen=e)}_firstFrozenToEndChanged(e){this._allCells.forEach(t=>{this._grid&&t.parentElement===this._grid.$.sizer||R(t,"first-frozen-to-end",e)}),this.parentElement&&this.parentElement._columnPropChanged&&(this.parentElement._firstFrozenToEnd=e)}_rowHeaderChanged(e,t){t&&t.forEach(i=>{i.setAttribute("role",e?"rowheader":"gridcell")})}_generateHeader(e){return e.substr(e.lastIndexOf(".")+1).replace(/([A-Z])/gu,"-$1").toLowerCase().replace(/-/gu," ").replace(/^./u,t=>t.toUpperCase())}_reorderStatusChanged(e){let t=this.__previousReorderStatus,i=t?`reorder-${t}-cell`:"",o=`reorder-${e}-cell`;this._allCells.forEach(n=>{R(n,"reorder-status",e,o,i)}),this.__previousReorderStatus=e}_resizableChanged(e,t){e===void 0||t===void 0||t&&[t].concat(this._emptyCells).forEach(i=>{if(i){let o=i.querySelector('[part~="resize-handle"]');if(o&&i.removeChild(o),e){let n=document.createElement("div");u(n,"resize-handle",!0),i.appendChild(n)}}})}_textAlignChanged(e){if(!(e===void 0||this._grid===void 0)){if(["start","end","center"].indexOf(e)===-1){console.warn('textAlign can only be set as "start", "end" or "center"');return}this._allCells.forEach(t=>{t._content.style.textAlign=e})}}_hiddenChanged(e){this.parentElement&&this.parentElement._columnPropChanged&&this.parentElement._columnPropChanged("hidden",e),!!e!=!!this._previousHidden&&this._grid&&(e===!0&&this._allCells.forEach(t=>{t._content.parentNode&&t._content.parentNode.removeChild(t._content)}),this._grid._debouncerHiddenChanged=f.debounce(this._grid._debouncerHiddenChanged,G,()=>{this._grid&&this._grid._renderColumnTree&&this._grid._renderColumnTree(this._grid._columnTree)}),this._grid._debounceUpdateFrozenColumn&&this._grid._debounceUpdateFrozenColumn(),this._grid._resetKeyboardNavigation&&this._grid._resetKeyboardNavigation()),this._previousHidden=e}_runRenderer(e,t,i){let o=i?.item&&!t.parentElement.hidden;if(!(o||e===this._headerRenderer||e===this._footerRenderer))return;let s=[t._content,this];o&&s.push(i),e.apply(this,s)}__renderCellsContent(e,t){this.hidden||!this._grid||t.forEach(i=>{if(!i.parentElement)return;let o=this._grid.__getRowModel(i.parentElement);e&&(i._renderer!==e&&this._clearCellContent(i),i._renderer=e,this._runRenderer(e,i,o))})}_clearCellContent(e){e._content.innerHTML="",delete e._content._$litPart$}_renderHeaderCellContent(e,t){!t||!e||(this.__renderCellsContent(e,[t]),this._grid&&t.parentElement&&this._grid.__debounceUpdateHeaderFooterRowVisibility(t.parentElement))}_onHeaderRendererOrBindingChanged(e,t,...i){this._renderHeaderCellContent(e,t)}__headerFooterPartNameChanged(e,t,i,o){[{cell:e,partName:i},{cell:t,partName:o}].forEach(({cell:n,partName:s})=>{if(n){let l=n.__customParts||[];n.part.remove(...l),n.__customParts=s?s.trim().split(" "):[],n.part.add(...n.__customParts)}})}_renderBodyCellsContent(e,t){!t||!e||this.__renderCellsContent(e,t)}_onRendererOrBindingChanged(e,t,...i){this._renderBodyCellsContent(e,t)}_renderFooterCellContent(e,t){!t||!e||(this.__renderCellsContent(e,[t]),this._grid&&t.parentElement&&this._grid.__debounceUpdateHeaderFooterRowVisibility(t.parentElement))}_onFooterRendererOrBindingChanged(e,t){this._renderFooterCellContent(e,t)}__setTextContent(e,t){e.textContent!==t&&(e.textContent=t)}__textHeaderRenderer(){this.__setTextContent(this._headerCell._content,this.header)}_defaultHeaderRenderer(){this.path&&this.__setTextContent(this._headerCell._content,this._generateHeader(this.path))}_defaultRenderer(e,t,{item:i}){this.path&&this.__setTextContent(e,A(this.path,i))}_defaultFooterRenderer(){}_computeHeaderRenderer(e,t){return e||(t!=null?this.__textHeaderRenderer:this._defaultHeaderRenderer)}_computeRenderer(e){return e||this._defaultRenderer}_computeFooterRenderer(e){return e||this._defaultFooterRenderer}},xe=a=>class extends we(oe(a)){static get properties(){return{width:{type:String,value:"100px",sync:!0},flexGrow:{type:Number,value:1,sync:!0},renderer:{type:Function,sync:!0},_renderer:{type:Function,computed:"_computeRenderer(renderer, __initialized)"},path:{type:String,sync:!0},autoWidth:{type:Boolean,value:!1},_focusButtonMode:{type:Boolean,value:!1},_cells:{type:Array,sync:!0}}}};var ee=class extends xe(N(H)){static get is(){return"vaadin-grid-column"}};B(ee);var Re=ie`
  /* stylelint-disable no-duplicate-selectors */
  :host {
    display: flex;
    max-width: 100%;
    height: 400px;
    min-height: var(--_grid-min-height, 0);
    flex: 1 1 auto;
    align-self: stretch;
    position: relative;
    box-sizing: border-box;
    overflow: hidden;
    -webkit-tap-highlight-color: transparent;
    background: var(--vaadin-grid-background, var(--vaadin-background-color));
    border: var(--vaadin-grid-border-width, 1px) solid var(--_border-color);
    cursor: default;
    --_border-color: var(--vaadin-grid-border-color, var(--vaadin-border-color-secondary));
    --_row-border-width: var(--vaadin-grid-row-border-width, 1px);
    --_column-border-width: var(--vaadin-grid-column-border-width, 0px);
    --_default-cell-padding: var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container);
    border-radius: var(--vaadin-grid-border-radius, var(--vaadin-radius-m));
  }

  :host([hidden]),
  [hidden] {
    display: none !important;
  }

  :host([disabled]) {
    pointer-events: none;
    opacity: 0.7;
  }

  /* Variant: No outer border */
  :host([theme~='no-border']) {
    border-width: 0;
    border-radius: 0;
  }

  :host([all-rows-visible]) {
    height: auto;
    align-self: flex-start;
    min-height: auto;
    flex-grow: 0;
    flex-shrink: 0;
    width: 100%;
  }

  #scroller {
    contain: layout;
    position: relative;
    display: flex;
    width: 100%;
    min-width: 0;
    min-height: 0;
    align-self: stretch;
    overflow: hidden;
  }

  #items {
    flex-grow: 1;
    flex-shrink: 0;
    display: block;
    position: sticky;
    width: 100%;
    left: 0;
    min-height: 1px;
    z-index: 1;
  }

  #table {
    display: flex;
    flex-direction: column;
    width: 100%;
    overflow: auto;
    position: relative;
    border-radius: inherit;
    /* Workaround for a Chrome bug: new stacking context here prevents the scrollbar from getting hidden */
    z-index: 0;
  }

  [no-scrollbars]:is([safari], [firefox]) #table {
    overflow: hidden;
  }

  #header,
  #footer {
    display: block;
    position: sticky;
    left: 0;
    width: 100%;
    z-index: 2;
  }

  :host([dir='rtl']) #items,
  :host([dir='rtl']) #header,
  :host([dir='rtl']) #footer {
    left: auto;
  }

  #header {
    top: 0;
  }

  #footer {
    bottom: 0;
  }

  .header-cell {
    text-align: inherit;
  }

  .header-cell,
  .reorder-ghost {
    font-size: var(--vaadin-grid-header-font-size, 1em);
    font-weight: var(--vaadin-grid-header-font-weight, 500);
    color: var(--vaadin-grid-header-text-color, var(--vaadin-text-color));
  }

  .row {
    display: flex;
    width: 100%;
    box-sizing: border-box;
    margin: 0;
    position: relative;
  }

  .row:not(:focus-within) {
    --_non-focused-row-none: none;
  }

  .body-row[loading] ::slotted(vaadin-grid-cell-content) {
    visibility: hidden;
  }

  #scroller[column-rendering='lazy'] .body-cell:not(.frozen-cell, .frozen-to-end-cell) {
    transform: translateX(var(--_grid-lazy-columns-start));
  }

  .body-row:empty {
    height: 100%;
  }

  .cell {
    padding: 0;
    box-sizing: border-box;
  }

  .cell:where(:not(.details-cell)) {
    flex-shrink: 0;
    flex-grow: 1;
    display: flex;
    width: 100%;
    position: relative;
    align-items: center;
    white-space: nowrap;
  }

  /*
    Block borders

    ::after - row and cell focus outline
    ::before - header bottom and footer top borders that only appear when scrolling
  */

  .row::after {
    top: 0;
    bottom: calc(var(--_row-border-width) * -1);
  }

  .body-row {
    scroll-margin-bottom: var(--_row-border-width);
  }

  .cell {
    border-block: var(--_row-border-width) var(--_border-color);
    border-top-style: solid;
  }

  .cell::after {
    top: calc(var(--_row-border-width) * -1);
    bottom: calc(var(--_row-border-width) * -1);
  }

  /* Block borders / Last header row and first footer row */

  .last-header-row::before,
  .first-footer-row::before {
    position: absolute;
    inset-inline: 0;
    border-block: var(--_row-border-width) var(--_border-color);
    transform: translateX(var(--_grid-horizontal-scroll-position));
  }

  /* Block borders / First header row */

  .first-header-row-cell {
    border-top-style: none;
  }

  .first-header-row-cell::after {
    top: 0;
  }

  /* Block borders / Last header row */

  :host([overflow~='top']) .last-header-row::before {
    content: '';
    bottom: calc(var(--_row-border-width) * -1);
    border-bottom-style: solid;
  }

  /* Block borders / First body row */

  #table:not([has-header]) .first-row-cell {
    border-top-style: none;
  }

  #table:not([has-header]) .first-row-cell::after {
    top: 0;
  }

  /* Block borders / Last body row */

  .last-row::after {
    bottom: 0;
  }

  .last-row .details-cell,
  .last-row-cell:not(.details-opened-row-cell) {
    border-bottom-style: solid;
  }

  /* Block borders / Last body row without footer */

  :host([all-rows-visible]),
  :host([overflow~='top']),
  :host([overflow~='bottom']) {
    #table:not([has-footer]) .last-row .details-cell,
    #table:not([has-footer]) .last-row-cell:not(.details-opened-row-cell) {
      border-bottom-style: none;

      &::after {
        bottom: 0;
      }
    }
  }

  /* Block borders / First footer row */

  .first-footer-row::after {
    top: calc(var(--_row-border-width) * -1);
  }

  .first-footer-row-cell {
    border-top-style: none;
  }

  :host([overflow~='bottom']),
  :host(:not([overflow~='top'], [all-rows-visible])) #scroller:not([empty-state]) {
    .first-footer-row::before {
      content: '';
      top: calc(var(--_row-border-width) * -1);
      border-top-style: solid;
    }
  }

  /* Block borders / Last footer row */

  .last-footer-row::after,
  .last-footer-row-cell::after {
    bottom: 0;
  }

  /* Inline borders */

  .cell {
    border-inline: var(--_column-border-width) var(--_border-color);
  }

  .header-cell:not(.first-column-cell),
  .footer-cell:not(.first-column-cell),
  .body-cell:not(.first-column-cell) {
    border-inline-start-style: solid;
  }

  .last-frozen-cell:not(.last-column-cell) {
    border-inline-end-style: solid;

    & + .cell {
      border-inline-start-style: none;
    }
  }

  /* Row and cell background */

  .row {
    background-color: var(--vaadin-grid-row-background-color, var(--vaadin-background-color));
  }

  .cell {
    --_cell-background-image: linear-gradient(
      var(--vaadin-grid-cell-background-color, transparent),
      var(--vaadin-grid-cell-background-color, transparent)
    );

    background-color: inherit;
    background-repeat: no-repeat;
    background-origin: padding-box;
    background-image: var(--_cell-background-image);
  }

  .body-cell {
    --_cell-highlight-background-image: linear-gradient(
      var(--vaadin-grid-row-highlight-background-color, transparent),
      var(--vaadin-grid-row-highlight-background-color, transparent)
    );

    background-image:
      var(--_row-hover-background-image, none), var(--_row-selected-background-image, none),
      var(--_cell-highlight-background-image, none), var(--_row-odd-background-image, none),
      var(--_cell-background-image, none);
  }

  .selected-row {
    --_row-selected-background-color: var(
      --vaadin-grid-row-selected-background-color,
      color-mix(in srgb, currentColor 8%, transparent)
    );
    --_row-selected-background-image: linear-gradient(
      var(--_row-selected-background-color),
      var(--_row-selected-background-color)
    );
  }

  @media (any-hover: hover) {
    .body-row:hover {
      --_row-hover-background-color: var(--vaadin-grid-row-hover-background-color, transparent);
      --_row-hover-background-image: linear-gradient(
        var(--_row-hover-background-color),
        var(--_row-hover-background-color)
      );
    }
  }

  :host([theme~='row-stripes']) .odd-row {
    --_row-odd-background-color: var(
      --vaadin-grid-row-odd-background-color,
      color-mix(in srgb, var(--vaadin-text-color) 4%, transparent)
    );
    --_row-odd-background-image: linear-gradient(var(--_row-odd-background-color), var(--_row-odd-background-color));
  }

  /* Variant: wrap cell contents */

  :host([theme~='wrap-cell-content']) .cell:not(.details-cell) {
    white-space: normal;
  }

  /* Raise highlighted rows above others */
  .row,
  .frozen-cell,
  .frozen-to-end-cell {
    &:focus,
    &:focus-within {
      z-index: 3;
    }
  }

  .details-cell {
    position: absolute;
    bottom: 0;
    width: 100%;
  }

  ::slotted(vaadin-grid-cell-content) {
    display: block;
    overflow: hidden;
    text-overflow: var(--vaadin-grid-cell-text-overflow, ellipsis);
    padding: var(--vaadin-grid-cell-padding, var(--_default-cell-padding));
    flex: 1;
    min-height: 1lh;
    min-width: 0;
  }

  .details-cell,
  .frozen-cell,
  .frozen-to-end-cell {
    z-index: 2;
  }

  /* Empty state */
  #scroller:not([empty-state]) #emptystatebody,
  #scroller[empty-state] #items {
    display: none;
  }

  #emptystatebody {
    display: flex;
    position: sticky;
    inset: 0;
    flex: 1;
    overflow: hidden;
  }

  #emptystaterow {
    display: flex;
    flex: 1;
  }

  #emptystatecell {
    display: block;
    flex: 1;
    overflow: auto;
    padding: var(--vaadin-grid-cell-padding, var(--_default-cell-padding));
    outline: none;
    border-block: var(--_row-border-width) var(--_border-color);
  }

  #table[has-header] #emptystatecell {
    border-top-style: solid;
  }

  #table[has-footer] #emptystatecell {
    border-bottom-style: solid;
  }

  #emptystatecell:focus-visible {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-focus-ring-width) * -1);
  }

  /* Reordering styles */
  :host([reordering]) ::slotted(vaadin-grid-cell-content),
  :host([reordering]) .resize-handle,
  #scroller[no-content-pointer-events] ::slotted(vaadin-grid-cell-content) {
    pointer-events: none;
  }

  .reorder-ghost {
    visibility: hidden;
    position: fixed;
    pointer-events: none;
    box-shadow:
      0 0 0 1px hsla(0deg, 0%, 0%, 0.2),
      0 8px 24px -2px hsla(0deg, 0%, 0%, 0.2);
    padding: var(--vaadin-grid-cell-padding, var(--_default-cell-padding)) !important;
    border-radius: 3px;

    /* Prevent overflowing the grid in Firefox */
    top: 0;
    inset-inline-start: 0;
  }

  :host([reordering]) {
    -webkit-user-select: none;
    user-select: none;
  }

  :host([reordering]) .cell {
    /* TODO expose a custom property to control this */
    --_reorder-curtain-filter: brightness(0.9) contrast(1.1);
  }

  :host([reordering]) .cell::after {
    content: '';
    position: absolute;
    inset: 0;
    z-index: 1;
    -webkit-backdrop-filter: var(--_reorder-curtain-filter);
    backdrop-filter: var(--_reorder-curtain-filter);
    outline: 0;
  }

  :host([reordering]) .reorder-allowed-cell {
    /* TODO expose a custom property to control this */
    --_reorder-curtain-filter: brightness(0.94) contrast(1.07);
  }

  :host([reordering]) .reorder-dragging-cell {
    --_reorder-curtain-filter: none;
  }

  /* Resizing styles */
  .resize-handle {
    position: absolute;
    top: 0;
    inset-inline-end: 0;
    height: 100%;
    cursor: col-resize;
    z-index: 1;
    opacity: 0;
    width: var(--vaadin-focus-ring-width);
    background: var(--vaadin-grid-column-resize-handle-color, var(--vaadin-focus-ring-color));
    transition: opacity 0.2s;
    translate: var(--_column-border-width);
  }

  .last-column-cell .resize-handle {
    translate: 0;
  }

  :host(:not([reordering])) #scroller:not([column-resizing]) .resize-handle:hover,
  .resize-handle:active {
    opacity: 1;
    transition-delay: 0.15s;
  }

  .resize-handle::before {
    position: absolute;
    content: '';
    height: 100%;
    width: 16px;
    translate: calc(-50% + var(--vaadin-focus-ring-width) / 2);
  }

  :host([dir='rtl']) .resize-handle::before {
    translate: calc(50% - var(--vaadin-focus-ring-width) / 2);
  }

  :is(.last-column-cell, .last-frozen-cell, .first-frozen-to-end-cell) .resize-handle::before {
    width: 8px;
    translate: 0;
  }

  :is(.last-column-cell, .last-frozen-cell) .resize-handle::before {
    inset-inline-end: 0;
  }

  .frozen-to-end-cell :is(.resize-handle, .resize-handle::before) {
    inset-inline: 0 auto;
  }

  .frozen-to-end-cell .resize-handle {
    translate: calc(var(--_column-border-width) * -1);
  }

  .first-frozen-to-end-cell {
    margin-inline-start: auto;
  }

  #scroller:is([column-resizing], [range-selecting]) {
    -webkit-user-select: none;
    user-select: none;
  }

  /* Focus outline element, also used for d'n'd indication */
  :is(.row, .cell)::after {
    position: absolute;
    z-index: 3;
    inset-inline: 0;
    pointer-events: none;
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-focus-ring-width) * -1);
  }

  .row::after {
    transform: translateX(var(--_grid-horizontal-scroll-position));
  }

  .cell:where(:not(.details-cell))::after {
    inset-inline: calc(var(--_column-border-width) * -1);
  }

  .first-column-cell::after {
    inset-inline-start: 0;
  }

  .last-column-cell::after {
    inset-inline-end: 0;
  }

  :host([navigating]) .row:focus,
  :host([navigating]) .cell:focus {
    outline: 0;
  }

  .row:focus-visible,
  .cell:focus-visible {
    outline: 0;
  }

  .row:focus-visible::after,
  .cell:focus-visible::after,
  :host([navigating]) .row:focus::after,
  :host([navigating]) .cell:focus::after {
    content: '';
  }

  /* Drag'n'drop styles */
  :host([dragover]) {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-grid-border-width, 1px) * -1);
  }

  /* Styles applied to draggable cell content; see _filterDragAndDrop in vaadin-grid-drag-and-drop-mixin.js. */
  ::slotted(vaadin-grid-cell-content[draggable-source]) {
    -webkit-user-drag: element;
    -webkit-user-select: none;
    user-select: none;
  }

  .row[dragover] {
    z-index: 100 !important;
  }

  .row[dragover]::after {
    content: '';
  }

  .dragover-above-row::after {
    outline: 0;
    border-top: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
  }

  .dragover-above-row:not(.first-row)::after {
    top: calc(var(--vaadin-focus-ring-width) / -2);
  }

  .dragover-below-row::after {
    outline: 0;
    border-bottom: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
  }

  .dragover-below-row:not(.last-row)::after {
    bottom: calc(var(--vaadin-focus-ring-width) / -2);
  }

  .dragstart-row-cell {
    border-block: none !important;
    padding-block: var(--_row-border-width) !important;
  }

  .dragstart-row-cell.last-column-cell {
    border-radius: 0 3px 3px 0;
  }

  .dragstart-row-cell.first-column-cell {
    border-radius: 3px 0 0 3px;
  }

  /* Indicates the number of dragged rows */
  /* TODO export custom properties to control styles */
  #scroller .dragstart-row:not([dragstart=''])::before {
    position: absolute;
    left: var(--_grid-drag-start-x);
    top: var(--_grid-drag-start-y);
    z-index: 100;
    content: attr(dragstart);
    box-sizing: border-box;
    padding: 0.3em;
    color: white;
    background-color: red;
    border-radius: 1em;
    font-size: 0.75rem;
    line-height: 1;
    font-weight: 500;
    min-width: 1.6em;
    text-align: center;
  }

  /* Sizer styles */
  #sizer {
    display: flex;
    visibility: hidden;
  }

  #sizer .details-cell,
  #sizer ::slotted(vaadin-grid-cell-content) {
    display: none !important;
  }

  #sizer .cell {
    display: block;
    flex-shrink: 0;
    line-height: 0;
    height: 0 !important;
    min-height: 0 !important;
    max-height: 0 !important;
    padding: 0 !important;
    border: none !important;
  }

  #sizer .cell::before {
    content: '-';
  }
`;var Ee=a=>class extends a{static get properties(){return{accessibleName:{type:String}}}static get observers(){return["__a11yUpdateGridSize(size, _columnTree, __emptyState)"]}__a11yGetHeaderRowCount(e){return e.filter(t=>t.some(i=>i.headerRenderer||i.path&&i.header!==null||i.header)).length}__a11yGetFooterRowCount(e){return e.filter(t=>t.some(i=>i.footerRenderer)).length}__a11yUpdateGridSize(e,t,i){if(e===void 0||t===void 0)return;let o=this.__a11yGetHeaderRowCount(t),n=this.__a11yGetFooterRowCount(t),l=(i?1:e)+o+n;this.$.table.setAttribute("aria-rowcount",l);let c=t[t.length-1],d=i?1:l&&c?.length||0;this.$.table.setAttribute("aria-colcount",d),this.__a11yUpdateHeaderRows(),this.__a11yUpdateFooterRows()}__a11yUpdateHeaderRows(){m(this.$.header,(e,t)=>{e.setAttribute("aria-rowindex",t+1)})}__a11yUpdateFooterRows(){m(this.$.footer,(e,t)=>{e.setAttribute("aria-rowindex",this.__a11yGetHeaderRowCount(this._columnTree)+this.size+t+1)})}__a11yUpdateRowRowindex(e){e.setAttribute("aria-rowindex",e.index+this.__a11yGetHeaderRowCount(this._columnTree)+1)}__a11yUpdateRowSelected(e,t){e.setAttribute("aria-selected",!!t),x(e,i=>{i.setAttribute("aria-selected",!!t)})}__a11yUpdateRowExpanded(e){let t=ye(e);this.__isRowExpandable(e)?(e.setAttribute("aria-expanded","false"),t&&t.setAttribute("aria-expanded","false")):this.__isRowCollapsible(e)?(e.setAttribute("aria-expanded","true"),t&&t.setAttribute("aria-expanded","true")):(e.removeAttribute("aria-expanded"),t&&t.removeAttribute("aria-expanded"))}__a11yUpdateRowLevel(e,t){t>0||this.__isRowCollapsible(e)||this.__isRowExpandable(e)?e.setAttribute("aria-level",t+1):e.removeAttribute("aria-level")}__a11ySetRowDetailsCell(e,t){x(e,i=>{i!==t&&i.setAttribute("aria-controls",t.id)})}__a11yUpdateCellColspan(e,t){e.setAttribute("aria-colspan",Number(t))}__a11yUpdateSorters(){Array.from(this.querySelectorAll("vaadin-grid-sorter")).forEach(e=>{let t=e.parentNode;for(;t&&t.localName!=="vaadin-grid-cell-content";)t=t.parentNode;t?.assignedSlot&&t.assignedSlot.parentNode.setAttribute("aria-sort",{asc:"ascending",desc:"descending"}[String(e.direction)]||"none")})}};var Ye=a=>a.offsetParent&&!a.part.contains("body-cell")&&he(a)&&getComputedStyle(a).visibility!=="hidden",Se=a=>class extends a{static get properties(){return{activeItem:{type:Object,notify:!0,value:null,sync:!0}}}ready(){super.ready(),this.$.scroller.addEventListener("click",this._onClick.bind(this)),this.addEventListener("cell-activate",this._activateItem.bind(this)),this.addEventListener("row-activate",this._activateItem.bind(this))}_activateItem(e){let t=e.detail.model,i=t?t.item:null;i&&(this.activeItem=this._itemsEqual(this.activeItem,i)?null:i)}_shouldPreventCellActivationOnClick(e){let{cell:t}=this._getGridEventLocation(e);return e.defaultPrevented||e.skipCellActivate||!t||t.part.contains("details-cell")||t===this.$.emptystatecell||t._content.contains(this.getRootNode().activeElement)||this._isFocusable(e.target)||e.target instanceof HTMLLabelElement}_onClick(e){if(this._shouldPreventCellActivationOnClick(e))return;let{cell:t}=this._getGridEventLocation(e);t&&this.dispatchEvent(new CustomEvent("cell-activate",{detail:{model:this.__getRowModel(t.parentElement)}}))}_isFocusable(e){return Ye(e)}};function P(a,r){return a.split(".").reduce((e,t)=>e[t],r)}function ze(a,r,e){if(e.length===0)return!1;let t=!0;return a.forEach(({path:i})=>{if(!i||i.indexOf(".")===-1)return;let o=i.replace(/\.[^.]*$/u,"");P(o,e[0])===void 0&&(console.warn(`Path "${i}" used for ${r} does not exist in all of the items, ${r} is disabled.`),t=!1)}),t}function q(a){return[void 0,null].indexOf(a)>=0?"":isNaN(a)?a.toString():a}function Ie(a,r){return a=q(a),r=q(r),a<r?-1:a>r?1:0}function Je(a,r){return a.sort((e,t)=>r.map(i=>i.direction==="asc"?Ie(P(i.path,e),P(i.path,t)):i.direction==="desc"?Ie(P(i.path,t),P(i.path,e)):0).reduce((i,o)=>i!==0?i:o,0))}function Ze(a,r){return a.filter(e=>r.every(t=>{let i=q(P(t.path,e)),o=q(t.value).toString().toLowerCase();return i.toString().toLowerCase().includes(o)}))}var Te=a=>(r,e)=>{let t=a?[...a]:[];r.filters&&ze(r.filters,"filtering",t)&&(t=Ze(t,r.filters)),Array.isArray(r.sortOrders)&&r.sortOrders.length&&ze(r.sortOrders,"sorting",t)&&(t=Je(t,r.sortOrders));let i=Math.min(t.length,r.pageSize),o=r.page*i,n=o+i,s=t.slice(o,n);e(s,t.length)};var Fe=a=>class extends a{static get properties(){return{items:{type:Array,sync:!0}}}static get observers(){return["__dataProviderOrItemsChanged(dataProvider, items, isAttached, items.*)"]}__setArrayDataProvider(e){let t=Te(this.items,{});t.__items=e,this._arrayDataProvider=t,this.size=e.length,this.dataProvider=t}_onDataProviderPageReceived(){super._onDataProviderPageReceived(),this._arrayDataProvider&&(this.size=this._flatSize)}__dataProviderOrItemsChanged(e,t,i){i&&(this._arrayDataProvider?e!==this._arrayDataProvider?(this._arrayDataProvider=void 0,this.items=void 0):t?this._arrayDataProvider.__items===t?this.clearCache():this.__setArrayDataProvider(t):(this._arrayDataProvider=void 0,this.dataProvider=void 0,this.size=0,this.clearCache()):t&&this.__setArrayDataProvider(t))}};var Pe=a=>class extends a{static get properties(){return{__pendingRecalculateColumnWidths:{type:Boolean,value:!0}}}static get observers(){return["__dataProviderChangedAutoWidth(dataProvider)","__columnTreeChangedAutoWidth(_columnTree)","__flatSizeChangedAutoWidth(_flatSize)"]}updated(r){super.updated(r),r.has("__hostVisible")&&!r.get("__hostVisible")&&this.__tryToRecalculateColumnWidthsIfPending()}__dataProviderChangedAutoWidth(r){this.__hasHadRenderedRowsForColumnWidthCalculation||this.recalculateColumnWidths()}__columnTreeChangedAutoWidth(r){queueMicrotask(()=>this.recalculateColumnWidths())}__flatSizeChangedAutoWidth(r){requestAnimationFrame(()=>{r&&!this.__hasHadRenderedRowsForColumnWidthCalculation?this.recalculateColumnWidths():this.__tryToRecalculateColumnWidthsIfPending()})}_onDataProviderPageLoaded(){super._onDataProviderPageLoaded(),this.__tryToRecalculateColumnWidthsIfPending()}_updateFrozenColumn(){super._updateFrozenColumn(),this.__tryToRecalculateColumnWidthsIfPending()}__getIntrinsicWidth(r){return this.__intrinsicWidthCache.has(r)||this.__calculateAndCacheIntrinsicWidths([r]),this.__intrinsicWidthCache.get(r)}__getDistributedWidth(r,e){if(r==null||r===this)return 0;let t=Math.max(this.__getIntrinsicWidth(r),this.__getDistributedWidth(this.__getParentColumnGroup(r),r));if(!e)return t;let i=r,o=t,n=i._visibleChildColumns.map(d=>this.__getIntrinsicWidth(d)).reduce((d,h)=>d+h,0),s=Math.max(0,o-n),c=this.__getIntrinsicWidth(e)/n*s;return this.__getIntrinsicWidth(e)+c}_recalculateColumnWidths(){this.__virtualizer.flush(),[...this.$.header.children,...this.$.footer.children].forEach(o=>{o.__debounceUpdateHeaderFooterRowVisibility&&o.__debounceUpdateHeaderFooterRowVisibility.flush()}),this.__hasHadRenderedRowsForColumnWidthCalculation||=this._getRenderedRows().length>0,this.__intrinsicWidthCache=new Map;let r=this._firstVisibleIndex,e=this._lastVisibleIndex;this.__viewportRowsCache=this._getRenderedRows().filter(o=>o.index>=r&&o.index<=e);let t=this.__getAutoWidthColumns(),i=new Set;for(let o of t){let n=this.__getParentColumnGroup(o);for(;n&&!i.has(n);)i.add(n),n=this.__getParentColumnGroup(n)}this.__calculateAndCacheIntrinsicWidths([...t,...i]),t.forEach(o=>{o.width=`${this.__getDistributedWidth(o)}px`}),this.__intrinsicWidthCache.clear()}__getParentColumnGroup(r){let e=(r.assignedSlot||r).parentElement;return e&&e!==this?e:null}__setVisibleCellContentAutoWidth(r,e){r._allCells.filter(t=>this.$.items.contains(t)?this.__viewportRowsCache.includes(t.parentElement):!0).forEach(t=>{t.__measuringAutoWidth=e,t.__measuringAutoWidth?(t.__originalWidth=t.style.width,t.style.width="auto",t.style.position="absolute"):(t.style.width=t.__originalWidth,delete t.__originalWidth,t.style.position="")}),e?this.$.scroller.setAttribute("measuring-auto-width",""):this.$.scroller.removeAttribute("measuring-auto-width")}__getAutoWidthCellsMaxWidth(r){return r._allCells.reduce((e,t)=>t.__measuringAutoWidth?Math.max(e,t.offsetWidth+1):e,0)}__calculateAndCacheIntrinsicWidths(r){r.forEach(e=>this.__setVisibleCellContentAutoWidth(e,!0)),r.forEach(e=>{let t=this.__getAutoWidthCellsMaxWidth(e);this.__intrinsicWidthCache.set(e,t)}),r.forEach(e=>this.__setVisibleCellContentAutoWidth(e,!1))}recalculateColumnWidths(){if(!this.__isReadyForColumnWidthCalculation()){this.__pendingRecalculateColumnWidths=!0;return}this._recalculateColumnWidths()}__tryToRecalculateColumnWidthsIfPending(){this.__pendingRecalculateColumnWidths&&(this.__pendingRecalculateColumnWidths=!1,this.recalculateColumnWidths())}__getAutoWidthColumns(){return this._getColumns().filter(r=>!r.hidden&&r.autoWidth)}__isReadyForColumnWidthCalculation(){if(!this._columnTree)return!1;let r=this.__getAutoWidthColumns().filter(n=>!customElements.get(n.localName));if(r.length)return Promise.all(r.map(n=>customElements.whenDefined(n.localName))).then(()=>{this.__tryToRecalculateColumnWidthsIfPending()}),!1;let e=[...this.$.items.children].some(n=>n.index===void 0),t=this._debouncerHiddenChanged&&this._debouncerHiddenChanged.isActive(),i=this.__debounceUpdateFrozenColumn&&this.__debounceUpdateFrozenColumn.isActive(),o=this.clientHeight>0;return!this._dataProviderController.isLoading()&&!e&&!de(this)&&!t&&!i&&o}};var Ae=a=>class extends a{static get properties(){return{columnReorderingAllowed:{type:Boolean,value:!1},_orderBaseScope:{type:Number,value:1e7}}}static get observers(){return["_updateOrders(_columnTree)"]}ready(){super.ready(),U(this,"track",this._onTrackEvent),this._reorderGhost=this.shadowRoot.querySelector('[part="reorder-ghost"]'),this.addEventListener("touchstart",this._onTouchStart.bind(this)),this.addEventListener("touchmove",this._onTouchMove.bind(this)),this.addEventListener("touchend",this._onTouchEnd.bind(this)),this.addEventListener("contextmenu",this._onContextMenu.bind(this))}_onContextMenu(e){this.hasAttribute("reordering")&&(e.preventDefault(),V||this._onTrackEnd())}_cancelReorderForMultiTouch(e){return e.touches.length>1?(clearTimeout(this._startTouchReorderTimeout),this._draggedColumn&&this._onTrackEnd(),!0):!1}_onTouchStart(e){this._cancelReorderForMultiTouch(e)||(this._startTouchReorderTimeout=setTimeout(()=>{this._onTrackStart({detail:{x:e.touches[0].clientX,y:e.touches[0].clientY}})},100))}_onTouchMove(e){this._cancelReorderForMultiTouch(e)||(this._draggedColumn&&e.preventDefault(),clearTimeout(this._startTouchReorderTimeout))}_onTouchEnd(){clearTimeout(this._startTouchReorderTimeout),this._onTrackEnd()}_onTrackEvent(e){if(e.detail.state==="start"){let t=e.composedPath(),i=t[t.indexOf(this.$.header)-2];if(!i||!i._content||i._content.contains(this.getRootNode().activeElement)||this.$.scroller.hasAttribute("column-resizing"))return;this._touchDevice||this._onTrackStart(e)}else e.detail.state==="track"?this._onTrack(e):e.detail.state==="end"&&this._onTrackEnd(e)}_onTrackStart(e){if(!this.columnReorderingAllowed)return;let t=e.composedPath?.();if(t?.slice(0,Math.max(0,t.indexOf(this))).some(o=>o.draggable))return;let i=this._cellFromPoint(e.detail.x,e.detail.y);if(!(!i||!i.part.contains("header-cell"))){for(this.toggleAttribute("reordering",!0),this._draggedColumn=i._column;this._draggedColumn.parentElement.childElementCount===1;)this._draggedColumn=this._draggedColumn.parentElement;this._setSiblingsReorderStatus(this._draggedColumn,"allowed"),this._draggedColumn._reorderStatus="dragging",this._updateGhost(i),this._reorderGhost.style.visibility="visible",this._updateGhostPosition(e.detail.x,this._touchDevice?e.detail.y-50:e.detail.y),this._autoScroller()}}_onTrack(e){if(!this._draggedColumn)return;let t=this._cellFromPoint(e.detail.x,e.detail.y);if(!t)return;let i=this._getTargetColumn(t,this._draggedColumn);if(this._isSwapAllowed(this._draggedColumn,i)&&this._isSwappableByPosition(i,e.detail.x)){let o=this._columnTree.findIndex(d=>d.includes(i)),n=this._getColumnsInOrder(o),s=n.indexOf(this._draggedColumn),l=n.indexOf(i),c=s<l?1:-1;for(let d=s;d!==l;d+=c)this._swapColumnOrders(this._draggedColumn,n[d+c])}this._updateGhostPosition(e.detail.x,this._touchDevice?e.detail.y-50:e.detail.y),this._lastDragClientX=e.detail.x}_onTrackEnd(){this._draggedColumn&&(this.toggleAttribute("reordering",!1),this._draggedColumn._reorderStatus="",this._setSiblingsReorderStatus(this._draggedColumn,""),this._draggedColumn=null,this._lastDragClientX=null,this._reorderGhost.style.visibility="hidden",this.dispatchEvent(new CustomEvent("column-reorder",{detail:{columns:this._getColumnsInOrder()}})))}_getColumnsInOrder(e=this._columnTree.length-1){return this._columnTree[e].filter(t=>!t.hidden).sort((t,i)=>t._order-i._order)}_cellFromPoint(e=0,t=0){this._draggedColumn||this.$.scroller.toggleAttribute("no-content-pointer-events",!0);let i=this.shadowRoot.elementFromPoint(e,t);return this.$.scroller.toggleAttribute("no-content-pointer-events",!1),this._getCellFromElement(i)}_getCellFromElement(e){if(e){if(e._column)return e;let{parentElement:t}=e;if(t?._focusButton===e)return t}return null}_updateGhostPosition(e,t){let i=this._reorderGhost.getBoundingClientRect(),o=e-i.width/2,n=t-i.height/2,s=parseInt(this._reorderGhost._left||0),l=parseInt(this._reorderGhost._top||0);this._reorderGhost._left=s-(i.left-o),this._reorderGhost._top=l-(i.top-n),this._reorderGhost.style.transform=`translate(${this._reorderGhost._left}px, ${this._reorderGhost._top}px)`}_updateGhost(e){let t=this._reorderGhost;t.textContent=e._content.innerText;let i=window.getComputedStyle(e);return["boxSizing","display","width","height","background","alignItems","padding","border","flex-direction","overflow"].forEach(o=>{t.style[o]=i[o]}),t}_updateOrders(e){e!==void 0&&(e[0].forEach(t=>{t._order=0}),ve(e[0],this._orderBaseScope,0))}_resetColumnOrder(){this._columnTree===void 0||this._columnTree.every(t=>t.every((i,o)=>o===0||i._order>=t[o-1]._order))||(this._columnTree=this._getColumnTree())}_setSiblingsReorderStatus(e,t){m(e.parentNode,i=>{/column/u.test(i.localName)&&this._isSwapAllowed(i,e)&&(i._reorderStatus=t)})}_autoScroller(){if(this._lastDragClientX){let e=this._lastDragClientX-this.getBoundingClientRect().right+50,t=this.getBoundingClientRect().left-this._lastDragClientX+50;e>0?this.$.table.scrollLeft+=e/10:t>0&&(this.$.table.scrollLeft-=t/10)}this._draggedColumn&&setTimeout(()=>this._autoScroller(),10)}_isSwapAllowed(e,t){if(e&&t){let i=e!==t,o=e.parentElement===t.parentElement,n=e.frozen&&t.frozen||e.frozenToEnd&&t.frozenToEnd||!e.frozen&&!e.frozenToEnd&&!t.frozen&&!t.frozenToEnd;return i&&o&&n}}_isSwappableByPosition(e,t){let i=Array.from(this.$.header.querySelectorAll('tr:not([hidden]) [part~="cell"]')).find(s=>e.contains(s._column)),o=this.$.header.querySelector("tr:not([hidden]) [reorder-status=dragging]").getBoundingClientRect(),n=i.getBoundingClientRect();return n.left>o.left?t>n.right-o.width:t<n.left+o.width}_swapColumnOrders(e,t){[e._order,t._order]=[t._order,e._order];let[i,o]=e._order<t._order?[e,t]:[t,e];[...this.$.header.children,...this.$.footer.children,...this.$.items.children,this.$.sizer].forEach(n=>{let s=b(n),l=s.filter(d=>i.contains(d._column)),c=s.find(d=>o.contains(d._column));l.forEach(d=>c.before(d)),n.__cells&&(n.__cells=n.__cells.toSorted((d,h)=>d._column._order-h._column._order))}),this._debounceUpdateFrozenColumn(),this._updateFirstAndLastColumn()}_getTargetColumn(e,t){if(e&&t){let i=e._column;for(;i.parentElement!==t.parentElement&&i!==this;)i=i.parentElement;return i.parentElement===t.parentElement?i:e._column}}};var $e=a=>class extends a{ready(){super.ready();let e=this.$.scroller;U(e,"track",this._onHeaderTrack.bind(this)),e.addEventListener("touchstart",t=>{t.touches.length>1&&e.removeAttribute("column-resizing")}),e.addEventListener("touchmove",t=>{if(t.touches.length>1){e.removeAttribute("column-resizing");return}e.hasAttribute("column-resizing")&&t.preventDefault()}),e.addEventListener("contextmenu",t=>t.target.part.contains("resize-handle")&&t.preventDefault()),e.addEventListener("mousedown",t=>t.target.part.contains("resize-handle")&&t.preventDefault())}_onHeaderTrack(e){let t=e.target;if(t.part.contains("resize-handle")){if(e.detail.state!=="start"&&!this.$.scroller.hasAttribute("column-resizing"))return;let o=t.parentElement._column;for(this.$.scroller.toggleAttribute("column-resizing",!0);o.localName==="vaadin-grid-column-group";)o=o._childColumns.slice(0).sort((h,_)=>h._order-_._order).filter(h=>!h.hidden).pop();let n=this.__isRTL,s=e.detail.x,l=Array.from(this.$.header.querySelectorAll('[part~="row"]:last-child [part~="cell"]')),c=l.find(h=>h._column===o);if(c.offsetWidth){let h=getComputedStyle(c._content),_=10+parseInt(h.paddingLeft)+parseInt(h.paddingRight)+parseInt(h.borderLeftWidth)+parseInt(h.borderRightWidth)+parseInt(h.marginLeft)+parseInt(h.marginRight),v,y=c.offsetWidth,p=c.getBoundingClientRect();c.hasAttribute("frozen-to-end")?v=y+(n?s-p.right:p.left-s):v=y+(n?p.left-s:s-p.right),o.width=`${Math.max(_,v)}px`,o.flexGrow=0}l.slice(0,l.indexOf(c)).forEach(h=>{h._column.width=`${h.offsetWidth}px`,h._column.flexGrow=0});let d=this._frozenToEndCells[0];if(d&&this.$.table.scrollWidth>this.$.table.offsetWidth){let h=d.getBoundingClientRect(),_=s-(n?h.right:h.left);(n&&_<=0||!n&&_>=0)&&(this.$.table.scrollLeft+=_)}e.detail.state==="end"&&(this.$.scroller.toggleAttribute("column-resizing",!1),this.dispatchEvent(new CustomEvent("column-resize",{detail:{resizedColumn:o}}))),this._resizeHandler()}}};var De=a=>class extends a{static get properties(){return{size:{type:Number,notify:!0,sync:!0},_flatSize:{type:Number,sync:!0},pageSize:{type:Number,value:50,observer:"_pageSizeChanged",sync:!0},dataProvider:{type:Object,notify:!0,observer:"_dataProviderChanged",sync:!0},loading:{type:Boolean,notify:!0,readOnly:!0,reflectToAttribute:!0},_hasData:{type:Boolean,value:!1,sync:!0},itemHasChildrenPath:{type:String,value:"children",observer:"__itemHasChildrenPathChanged",sync:!0},itemIdPath:{type:String,value:null,sync:!0},expandedItems:{type:Object,notify:!0,value:()=>[],sync:!0},__expandedKeys:{type:Object,computed:"__computeExpandedKeys(itemIdPath, expandedItems)"}}}static get observers(){return["_sizeChanged(size)","_expandedItemsChanged(expandedItems)"]}constructor(){super(),this._dataProviderController=new be(this,{size:this.size||0,pageSize:this.pageSize,getItemId:this.getItemId.bind(this),isExpanded:this._isExpanded.bind(this),dataProvider:this.dataProvider?this.dataProvider.bind(this):null,dataProviderParams:()=>({sortOrders:this._mapSorters(),filters:this._mapFilters()})}),this._dataProviderController.addEventListener("page-requested",this._onDataProviderPageRequested.bind(this)),this._dataProviderController.addEventListener("page-received",this._onDataProviderPageReceived.bind(this)),this._dataProviderController.addEventListener("page-loaded",this._onDataProviderPageLoaded.bind(this))}_sizeChanged(e){this._dataProviderController.rootCache.size=e,this._dataProviderController.recalculateFlatSize(),this._flatSize=this._dataProviderController.flatSize}__itemHasChildrenPathChanged(e,t){!t&&e==="children"||this.requestContentUpdate()}__getRowLevel(e){let{level:t}=this._dataProviderController.getFlatIndexContext(e.index);return t}__getRowItem(e){let{item:t}=this._dataProviderController.getFlatIndexContext(e.index);return t}__ensureRowItem(e){this._dataProviderController.ensureFlatIndexLoaded(e.index)}__ensureRowHierarchy(e){this._dataProviderController.ensureFlatIndexHierarchy(e.index)}getItemId(e){return this.itemIdPath?A(this.itemIdPath,e):e}_isExpanded(e){return this.__expandedKeys&&this.__expandedKeys.has(this.getItemId(e))}_hasChildren(e){return this.itemHasChildrenPath&&e&&!!A(this.itemHasChildrenPath,e)}_expandedItemsChanged(){this._dataProviderController.recalculateFlatSize(),this._flatSize=this._dataProviderController.flatSize,this.__updateVisibleRows()}__computeExpandedKeys(e,t){let i=t||[],o=new Set;return i.forEach(n=>{o.add(this.getItemId(n))}),o}expandItem(e){this._isExpanded(e)||(this.expandedItems=[...this.expandedItems,e])}collapseItem(e){this._isExpanded(e)&&(this.expandedItems=this.expandedItems.filter(t=>!this._itemsEqual(t,e)))}_onDataProviderPageRequested(){this._setLoading(!0)}_onDataProviderPageReceived(){this._flatSize!==this._dataProviderController.flatSize&&(this._shouldLoadAllRenderedRowsAfterPageLoad=!0,this._flatSize=this._dataProviderController.flatSize),this._getRenderedRows().forEach(e=>this.__ensureRowHierarchy(e)),this._hasData=!0}_onDataProviderPageLoaded(){this._debouncerApplyCachedData=f.debounce(this._debouncerApplyCachedData,E.after(0),()=>{this._setLoading(!1);let e=this._shouldLoadAllRenderedRowsAfterPageLoad;this._shouldLoadAllRenderedRowsAfterPageLoad=!1,this._getRenderedRows().forEach(t=>{this.__updateRow(t),e&&this.__ensureRowItem(t)}),this.__scrollToPendingIndexes(),this.__dispatchPendingBodyCellFocus()}),this._dataProviderController.isLoading()||this._debouncerApplyCachedData.flush()}__debounceClearCache(){this.__clearCacheDebouncer=f.debounce(this.__clearCacheDebouncer,C,()=>this.clearCache())}clearCache(){this._dataProviderController.clearCache(),this._dataProviderController.rootCache.size=this.size||0,this._dataProviderController.recalculateFlatSize(),this._hasData=!1,this.__updateVisibleRows(),(!this.__virtualizer||!this.__virtualizer.size)&&this._dataProviderController.loadFirstPage()}_pageSizeChanged(e,t){this._dataProviderController.setPageSize(e),t!==void 0&&e!==t&&this.clearCache()}_checkSize(){this.size===void 0&&this._flatSize===0&&console.warn("The <vaadin-grid> needs the total number of items in order to display rows, which you can specify either by setting the `size` property, or by providing it to the second argument of the `dataProvider` function `callback` call.")}_dataProviderChanged(e,t){this._dataProviderController.setDataProvider(e?e.bind(this):null),t!==void 0&&this.clearCache(),this._ensureFirstPageLoaded(),this._debouncerCheckSize=f.debounce(this._debouncerCheckSize,E.after(2e3),this._checkSize.bind(this))}_ensureFirstPageLoaded(){this._hasData||this._dataProviderController.loadFirstPage()}_itemsEqual(e,t){return this.getItemId(e)===this.getItemId(t)}scrollToIndex(...e){if(!this.__virtualizer||!this.clientHeight||!this._columnTree){this.__pendingScrollToIndexes=e;return}let t;for(;t!==(t=this._dataProviderController.getFlatIndexByPath(e));)this._scrollToFlatIndex(t);this._dataProviderController.isLoading()&&(this.__pendingScrollToIndexes=e)}__scrollToPendingIndexes(){if(this.__pendingScrollToIndexes&&this.$.items.children.length){let e=this.__pendingScrollToIndexes;delete this.__pendingScrollToIndexes,this.scrollToIndex(...e)}}};var O={BETWEEN:"between",ON_TOP:"on-top",ON_TOP_OR_BETWEEN:"on-top-or-between",ON_GRID:"on-grid"},z={ON_TOP:"on-top",ABOVE:"above",BELOW:"below",EMPTY:"empty"},Le=a=>class extends a{static get properties(){return{dropMode:{type:String,sync:!0},rowsDraggable:{type:Boolean,sync:!0},dragFilter:{type:Function,sync:!0},dropFilter:{type:Function,sync:!0},__dndAutoScrollThreshold:{value:50},__draggedItems:{value:()=>[]}}}static get observers(){return["_dragDropAccessChanged(rowsDraggable, dropMode, dragFilter, dropFilter, loading)"]}constructor(){super(),this.__onDocumentDragStart=this.__onDocumentDragStart.bind(this)}ready(){super.ready(),this.$.table.addEventListener("dragstart",this._onDragStart.bind(this)),this.$.table.addEventListener("dragend",this._onDragEnd.bind(this)),this.$.table.addEventListener("dragover",this._onDragOver.bind(this)),this.$.table.addEventListener("dragleave",this._onDragLeave.bind(this)),this.$.table.addEventListener("drop",this._onDrop.bind(this)),this.$.table.addEventListener("dragenter",e=>{this.dropMode&&(e.preventDefault(),e.stopPropagation())})}connectedCallback(){super.connectedCallback(),document.addEventListener("dragstart",this.__onDocumentDragStart,{capture:!0})}disconnectedCallback(){super.disconnectedCallback(),document.removeEventListener("dragstart",this.__onDocumentDragStart,{capture:!0})}_onDragStart(e){if(this.rowsDraggable){let t=e.target;if(t.localName==="vaadin-grid-cell-content"&&(t=t.assignedSlot.parentNode.parentNode),t.parentNode!==this.$.items)return;if(e.stopPropagation(),this.toggleAttribute("dragging-rows",!0),this._safari){let s=t.style.transform;t.style.top=/translateY\((.*)\)/u.exec(s)[1],t.style.transform="none",requestAnimationFrame(()=>{t.style.top="",t.style.transform=s})}let i=t.getBoundingClientRect();e.dataTransfer.setDragImage(t,e.clientX-i.left,e.clientY-i.top);let o=[t];this._isSelected(t._item)&&(o=this.__getViewportRows().filter(s=>this._isSelected(s._item)).filter(s=>!this.dragFilter||this.dragFilter(this.__getRowModel(s)))),this.__draggedItems=o.map(s=>s._item),e.dataTransfer.setData("text",this.__formatDefaultTransferData(o)),S(t,{dragstart:o.length>1?`${o.length}`:""}),this.style.setProperty("--_grid-drag-start-x",`${e.clientX-i.left+20}px`),this.style.setProperty("--_grid-drag-start-y",`${e.clientY-i.top+10}px`),requestAnimationFrame(()=>{S(t,{dragstart:!1}),this.style.setProperty("--_grid-drag-start-x",""),this.style.setProperty("--_grid-drag-start-y",""),this.requestContentUpdate()});let n=new CustomEvent("grid-dragstart",{detail:{draggedItems:[...this.__draggedItems],setDragData:(s,l)=>e.dataTransfer.setData(s,l),setDraggedItemsCount:s=>t.setAttribute("dragstart",s)}});n.originalEvent=e,this.dispatchEvent(n)}}_onDragEnd(e){this.toggleAttribute("dragging-rows",!1),e.stopPropagation();let t=new CustomEvent("grid-dragend");t.originalEvent=e,this.dispatchEvent(t),this.__draggedItems=[],this.requestContentUpdate()}_onDragLeave(e){this.dropMode&&(e.stopPropagation(),this._clearDragStyles())}_onDragOver(e){if(this.dropMode){if(this._dropLocation=void 0,this._dragOverItem=void 0,this.__dndAutoScroll(e.clientY)){this._clearDragStyles();return}let t=e.composedPath().find(i=>i.localName==="tr");if(this.__updateRowScrollPositionProperty(t),!this._flatSize||this.dropMode===O.ON_GRID)this._dropLocation=z.EMPTY;else if(!t||t.parentNode!==this.$.items){if(t)return;if(this.dropMode===O.BETWEEN||this.dropMode===O.ON_TOP_OR_BETWEEN)t=Array.from(this.$.items.children).filter(i=>!i.hidden).pop(),this._dropLocation=z.BELOW;else return}else{let i=t.getBoundingClientRect();if(this._dropLocation=z.ON_TOP,this.dropMode===O.BETWEEN){let o=e.clientY-i.top<i.bottom-e.clientY;this._dropLocation=o?z.ABOVE:z.BELOW}else this.dropMode===O.ON_TOP_OR_BETWEEN&&(e.clientY-i.top<i.height/3?this._dropLocation=z.ABOVE:e.clientY-i.top>i.height/3*2&&(this._dropLocation=z.BELOW))}if(t?.hasAttribute("drop-disabled")){this._dropLocation=void 0;return}e.stopPropagation(),e.preventDefault(),this._dropLocation===z.EMPTY?this.toggleAttribute("dragover",!0):t?(this._dragOverItem=t._item,t.getAttribute("dragover")!==this._dropLocation&&Q(t,{dragover:this._dropLocation})):this._clearDragStyles()}}__onDocumentDragStart(e){if(e.target.contains(this)){let t=[e.target,this.$.items,this.$.scroller],i=t.map(o=>o.style.cssText);this.$.table.scrollHeight>2e4&&(this.$.scroller.style.display="none"),$&&(e.target.style.willChange="transform"),W&&(this.$.items.style.flexShrink=1),requestAnimationFrame(()=>{t.forEach((o,n)=>{o.style.cssText=i[n]})})}}__dndAutoScroll(e){if(this.__dndAutoScrolling)return!0;let t=this.$.header.getBoundingClientRect().bottom,i=this.$.footer.getBoundingClientRect().top,o=t-e+this.__dndAutoScrollThreshold,n=e-i+this.__dndAutoScrollThreshold,s=0;if(n>0?s=n*2:o>0&&(s=-o*2),s){let l=this.$.table.scrollTop;if(this.$.table.scrollTop+=s,l!==this.$.table.scrollTop)return this.__dndAutoScrolling=!0,setTimeout(()=>{this.__dndAutoScrolling=!1},20),!0}}__getViewportRows(){let e=this.$.header.getBoundingClientRect().bottom,t=this.$.footer.getBoundingClientRect().top;return Array.from(this.$.items.children).filter(i=>{let o=i.getBoundingClientRect();return o.bottom>e&&o.top<t})}_clearDragStyles(){this.removeAttribute("dragover"),m(this.$.items,e=>{Q(e,{dragover:null})})}__updateDragSourceParts(e,t){S(e,{"drag-source":this.__draggedItems.includes(t.item)})}_onDrop(e){if(this.dropMode&&this._dropLocation){e.stopPropagation(),e.preventDefault();let t=e.dataTransfer.types&&Array.from(e.dataTransfer.types).map(o=>({type:o,data:e.dataTransfer.getData(o)}));this._clearDragStyles();let i=new CustomEvent("grid-drop",{bubbles:e.bubbles,cancelable:e.cancelable,detail:{dropTargetItem:this._dragOverItem,dropLocation:this._dropLocation,dragData:t}});i.originalEvent=e,this.dispatchEvent(i)}}__formatDefaultTransferData(e){return e.map(t=>b(t).filter(i=>!i.hidden).map(i=>i._content.textContent.trim()).filter(i=>i).join("	")).join(`
`)}_dragDropAccessChanged(){this.filterDragAndDrop()}filterDragAndDrop(){m(this.$.items,e=>{e.hidden||this._filterDragAndDrop(e,this.__getRowModel(e))})}_filterDragAndDrop(e,t){let i=this.loading||e.hasAttribute("loading"),o=!this.rowsDraggable||i||this.dragFilter&&!this.dragFilter(t),n=!this.dropMode||i||this.dropFilter&&!this.dropFilter(t),s=$?"draggable-source":"draggable";x(e,l=>{o?l._content.removeAttribute(s):l._content.setAttribute(s,!0)}),S(e,{"drag-disabled":!!o,"drop-disabled":!!n})}};function Oe(a,r){if(!a||!r||a.length!==r.length)return!1;for(let e=0,t=a.length;e<t;e++)if(a[e]instanceof Array&&r[e]instanceof Array){if(!Oe(a[e],r[e]))return!1}else if(a[e]!==r[e])return!1;return!0}var ke=a=>class extends a{static get properties(){return{_columnTree:{type:Object,sync:!0}}}ready(){super.ready(),this._addNodeObserver()}_hasColumnGroups(e){return e.some(t=>t.localName==="vaadin-grid-column-group")}_getChildColumns(e){return F.getColumns(e)}_flattenColumnGroups(e){return e.map(t=>t.localName==="vaadin-grid-column-group"?this._getChildColumns(t):[t]).reduce((t,i)=>t.concat(i),[])}_getColumnTree(){let e=F.getColumns(this),t=[e],i=e;for(;this._hasColumnGroups(i);)i=this._flattenColumnGroups(i),t.push(i);return t}_debounceUpdateColumnTree(){this.__updateColumnTreeDebouncer=f.debounce(this.__updateColumnTreeDebouncer,C,()=>this._updateColumnTree())}_updateColumnTree(){let e=this._getColumnTree();Oe(e,this._columnTree)||(this._columnTree=e)}_addNodeObserver(){this._observer=new F(this,(e,t)=>{let i=t.flatMap(n=>n._allCells),o=n=>i.filter(s=>s?._content.contains(n)).length;this.__removeSorters(this._sorters.filter(o)),this.__removeFilters(this._filters.filter(o)),this._debounceUpdateColumnTree(),this._debouncerCheckImports=f.debounce(this._debouncerCheckImports,E.after(2e3),this._checkImports.bind(this)),this._ensureFirstPageLoaded()})}_checkImports(){["vaadin-grid-column-group","vaadin-grid-filter","vaadin-grid-filter-column","vaadin-grid-tree-toggle","vaadin-grid-selection-column","vaadin-grid-sort-column","vaadin-grid-sorter"].forEach(e=>{this.querySelector(e)&&!customElements.get(e)&&console.warn(`Make sure you have imported the required module for <${e}> element.`)})}_updateFirstAndLastColumn(){Array.from(this.shadowRoot.querySelectorAll("tr")).forEach(e=>this._updateFirstAndLastColumnForRow(e))}_updateFirstAndLastColumnForRow(e){b(e).forEach((t,i,o)=>{R(t,"first-column",i===0),R(t,"last-column",i===o.length-1)})}_isColumnElement(e){return e.nodeType===Node.ELEMENT_NODE&&/\bcolumn\b/u.test(e.localName)}};var Me=a=>class extends a{getEventContext(e){let t={},{cell:i}=this._getGridEventLocation(e);return i&&(t.section=["body","header","footer","details"].find(o=>i.part.contains(`${o}-cell`)),i._column&&(t.column=i._column),(t.section==="body"||t.section==="details")&&Object.assign(t,this.__getRowModel(i.__parentRow))),t}};var He=a=>class extends a{static get properties(){return{_filters:{type:Array,value:()=>[]}}}constructor(){super(),this._filterChanged=this._filterChanged.bind(this),this.addEventListener("filter-changed",this._filterChanged)}_filterChanged(e){e.stopPropagation(),this.__addFilter(e.target),this.__applyFilters()}__removeFilters(e){e.length!==0&&(this._filters=this._filters.filter(t=>e.indexOf(t)<0),this.__applyFilters())}__addFilter(e){this._filters.indexOf(e)===-1&&this._filters.push(e)}__applyFilters(){this.dataProvider&&this.isAttached&&this.clearCache()}_mapFilters(){return this._filters.map(e=>({path:e.path,value:e.value}))}};function K(a){return a instanceof HTMLTableRowElement}function j(a){return a instanceof HTMLTableCellElement}function T(a){return a.matches('[part~="details-cell"]')}var Be=a=>class extends a{static get properties(){return{_headerFocusable:{type:Object,observer:"_focusableChanged",sync:!0},_itemsFocusable:{type:Object,observer:"_focusableChanged",sync:!0},_footerFocusable:{type:Object,observer:"_focusableChanged",sync:!0},_navigatingIsHidden:Boolean,_focusedItemIndex:{type:Number,value:0},_focusedColumnOrder:Number,_focusedCell:{type:Object,observer:"_focusedCellChanged",sync:!0},interacting:{type:Boolean,value:!1,reflectToAttribute:!0,readOnly:!0,observer:"_interactingChanged"}}}get __rowFocusMode(){return[this._headerFocusable,this._itemsFocusable,this._footerFocusable].some(K)}set __rowFocusMode(e){["_itemsFocusable","_footerFocusable","_headerFocusable"].forEach(t=>{let i=this[t];if(e){let o=i?.parentElement;j(i)?this[t]=o:j(o)&&(this[t]=o.parentElement)}else if(!e&&K(i)){let o=i.firstElementChild;this[t]=o._focusButton||o}})}get _visibleItemsCount(){return this._lastVisibleIndex-this._firstVisibleIndex-1}ready(){super.ready(),!(this._ios||this._android)&&(this.addEventListener("keydown",this._onKeyDown),this.addEventListener("keyup",this._onKeyUp),this.addEventListener("focusin",this._onFocusIn),this.addEventListener("focusout",this._onFocusOut),this.$.table.addEventListener("focusin",this._onContentFocusIn.bind(this)),this.addEventListener("mousedown",()=>{this.toggleAttribute("navigating",!1),this._isMousedown=!0,this._focusedColumnOrder=void 0}),this.addEventListener("mouseup",()=>{this._isMousedown=!1}))}_focusableChanged(e,t){t&&t.setAttribute("tabindex","-1"),e&&this._updateGridSectionFocusTarget(e)}_focusedCellChanged(e,t){t&&u(t,"focused-cell",!1),e&&u(e,"focused-cell",!0)}_interactingChanged(){this._updateGridSectionFocusTarget(this._headerFocusable),this._updateGridSectionFocusTarget(this._itemsFocusable),this._updateGridSectionFocusTarget(this._footerFocusable)}__updateItemsFocusable(){if(!this._itemsFocusable)return;let e=this.shadowRoot.activeElement===this._itemsFocusable;this._getRenderedRows().forEach(t=>{if(t.index===this._focusedItemIndex)if(this.__rowFocusMode)this._itemsFocusable=t;else{let i=this._itemsFocusable.parentElement,o=this._itemsFocusable;if(i){j(i)&&(o=i,i=i.parentElement);let n=[...i.children].indexOf(o);this._itemsFocusable=this.__getFocusable(t,t.children[n])}}}),e&&this._itemsFocusable.focus()}_onKeyDown(e){let t=e.key,i;switch(t){case"ArrowUp":case"ArrowDown":case"ArrowLeft":case"ArrowRight":case"PageUp":case"PageDown":case"Home":case"End":i="Navigation";break;case"Enter":case"Escape":case"F2":i="Interaction";break;case"Tab":i="Tab";break;case" ":i="Space";break;default:break}this._detectInteracting(e),this.interacting&&i!=="Interaction"&&(i=void 0),i&&this[`_on${i}KeyDown`](e,t)}__ensureFlatIndexInViewport(e){let t=[...this.$.items.children].find(i=>i.index===e);t?this.__scrollIntoViewport(t):this._scrollToFlatIndex(e)}__isRowExpandable(e){return this._hasChildren(e._item)&&!this._isExpanded(e._item)}__isRowCollapsible(e){return this._isExpanded(e._item)}_onNavigationKeyDown(e,t){e.preventDefault();let i=this.__isRTL,o=e.composedPath().find(K),n=e.composedPath().find(j),s=0,l=0;switch(t){case"ArrowRight":s=i?-1:1;break;case"ArrowLeft":s=i?1:-1;break;case"Home":this.__rowFocusMode||e.ctrlKey?l=-1/0:s=-1/0;break;case"End":this.__rowFocusMode||e.ctrlKey?l=1/0:s=1/0;break;case"ArrowDown":l=1;break;case"ArrowUp":l=-1;break;case"PageDown":if(this.$.items.contains(o)){let h=this.__getIndexInGroup(o,this._focusedItemIndex);this._scrollToFlatIndex(h)}l=this._visibleItemsCount;break;case"PageUp":l=-this._visibleItemsCount;break;default:break}if(this.__rowFocusMode&&!o||!this.__rowFocusMode&&!n)return;let c=i?"ArrowLeft":"ArrowRight",d=i?"ArrowRight":"ArrowLeft";if(t===c){if(this.__rowFocusMode){if(this.__isRowExpandable(o)){this.expandItem(o._item);return}this.__rowFocusMode=!1,this._onCellNavigation(o.firstElementChild,0,0);return}}else if(t===d){if(this.__rowFocusMode){if(this.__isRowCollapsible(o)){this.collapseItem(o._item);return}}else if(n===o.firstElementChild||T(n)){this.__rowFocusMode=!0,this._onRowNavigation(o,0);return}}this.__rowFocusMode?this._onRowNavigation(o,l):this._onCellNavigation(n,s,l)}_onRowNavigation(e,t){let{dstRow:i}=this.__navigateRows(t,e);i&&i.focus()}__getIndexInGroup(e,t){let i=e.parentNode;return i===this.$.items?t??e.index:[...i.children].indexOf(e)}__navigateRows(e,t,i){let o=this.__getIndexInGroup(t,this._focusedItemIndex),n=t.parentNode,s=(n===this.$.items?this._flatSize:n.children.length)-1,l=Math.max(0,Math.min(o+e,s));if(n!==this.$.items){if(l>o)for(;l<s&&n.children[l].hidden;)l+=1;else if(l<o)for(;l>0&&n.children[l].hidden;)l-=1;return this.toggleAttribute("navigating",!0),{dstRow:n.children[l]}}let c=!1;if(i){let d=T(i);if(n===this.$.items){let h=t._item,{item:_}=this._dataProviderController.getFlatIndexContext(l);d?c=e===0:c=e===1&&this._isDetailsOpened(h)||e===-1&&l!==o&&this._isDetailsOpened(_),c!==d&&(e===1&&c||e===-1&&!c)&&(l=o)}}return this.__ensureFlatIndexInViewport(l),this._focusedItemIndex=l,this.toggleAttribute("navigating",!0),{dstRow:[...n.children].find(d=>!d.hidden&&d.index===l),dstIsRowDetails:c}}_onCellNavigation(e,t,i){let o=e.parentNode,{dstRow:n,dstIsRowDetails:s}=this.__navigateRows(i,o,e);if(!n)return;let l=T(e),c=o.parentNode;if(this._focusedColumnOrder===void 0&&(l?this._focusedColumnOrder=0:this._focusedColumnOrder=e._column._order),s)[...n.children].find(T).focus();else{let d=this.__getIndexInGroup(n,this._focusedItemIndex),h=this._getColumns(c,d).filter(g=>!g.hidden),_=h.map(g=>g._order).sort((g,w)=>g-w),v=_.length-1,y=_.indexOf(_.slice(0).sort((g,w)=>Math.abs(g-this._focusedColumnOrder)-Math.abs(w-this._focusedColumnOrder))[0]),p=i===0&&l?y:Math.max(0,Math.min(y+t,v));p!==y&&(this._focusedColumnOrder=void 0);let k=h.find(g=>g._order===_[p]),I;if(this.$.items.contains(e)){let g=[...this.$.sizer.children].find(w=>w._column===k);this._lazyColumns&&(this.__isColumnInViewport(g._column)||g.scrollIntoView(),this.__updateColumnsBodyContentHidden(),this.__updateHorizontalScrollPosition()),I=[...n.children].find(w=>w._column===g._column),this._scrollHorizontallyToCell(I)}else I=[...n.children].find(g=>g._column.contains(k)),this._scrollHorizontallyToCell(I);I.focus({preventScroll:!0})}}_onInteractionKeyDown(e,t){let i=e.composedPath()[0],o=i.localName==="input"&&!/^(button|checkbox|color|file|image|radio|range|reset|submit)$/iu.test(i.type),n;switch(t){case"Enter":n=this.interacting?!o:!0;break;case"Escape":n=!1;break;case"F2":n=!this.interacting;break;default:break}let{cell:s}=this._getGridEventLocation(e);if(this.interacting!==n&&s!==null)if(n){let l=s._content.querySelector("[focus-target]")||[...s._content.querySelectorAll("*")].find(c=>this._isFocusable(c));l&&(e.preventDefault(),l.focus(),this._setInteracting(!0),this.toggleAttribute("navigating",!1))}else e.preventDefault(),this._focusedColumnOrder=void 0,s.focus(),this._setInteracting(!1),this.toggleAttribute("navigating",!0);t==="Escape"&&this._hideTooltip(!0)}_predictFocusStepTarget(e,t){let i=[this.$.table,this._headerFocusable,this.__emptyState?this.$.emptystatecell:this._itemsFocusable,this._footerFocusable,this.$.focusexit],o=i.indexOf(e);for(o+=t;o>=0&&o<=i.length-1;){let s=i[o];if(s&&!this.__rowFocusMode&&(s=i[o].parentNode),!s||s.hidden)o+=t;else break}let n=i[o];if(n&&!this.__isHorizontallyInViewport(n)){let s=this._getColumnsInOrder().find(l=>this.__isColumnInViewport(l));if(s)if(n===this._headerFocusable)n=s._headerCell;else if(n===this._itemsFocusable){let l=n._column._cells.indexOf(n);n=s._cells[l]}else n===this._footerFocusable&&(n=s._footerCell)}return n}_onTabKeyDown(e){let t=this._predictFocusStepTarget(e.composedPath()[0],e.shiftKey?-1:1);t&&(e.stopPropagation(),t===this._itemsFocusable&&(this.__ensureFlatIndexInViewport(this._focusedItemIndex),this.__updateItemsFocusable(),t=this._itemsFocusable),t.focus(),t!==this.$.table&&t!==this.$.focusexit&&e.preventDefault(),this.toggleAttribute("navigating",!0))}_onSpaceKeyDown(e){e.preventDefault();let t=e.composedPath()[0],i=K(t);(i||!t._content||!t._content.firstElementChild)&&this.dispatchEvent(new CustomEvent(i?"row-activate":"cell-activate",{detail:{model:this.__getRowModel(i?t:t.parentElement)}}))}_onKeyUp(e){if(!/^( |SpaceBar)$/u.test(e.key)||this.interacting)return;e.preventDefault();let t=e.composedPath()[0],i=t._content&&t._content.firstElementChild||t,o=this.hasAttribute("navigating"),n=new MouseEvent("click",{shiftKey:e.shiftKey,bubbles:!0,composed:!0,cancelable:!0});n.skipCellActivate=i===t,i.dispatchEvent(n),this.toggleAttribute("navigating",o)}_onFocusIn(e){this._isMousedown||this.toggleAttribute("navigating",!0);let t=e.composedPath()[0];t===this.$.table||t===this.$.focusexit?(this._isMousedown||this._predictFocusStepTarget(t,t===this.$.table?1:-1).focus(),this._setInteracting(!1)):this._detectInteracting(e)}_onFocusOut(e){this.toggleAttribute("navigating",!1),this._detectInteracting(e),this._hideTooltip(),this._focusedCell=null}_onContentFocusIn(e){let{section:t,cell:i,row:o}=this._getGridEventLocation(e);if(!(!i&&!this.__rowFocusMode)&&(this._detectInteracting(e),t&&(i||o)))if(this._activeRowGroup=t,t===this.$.header?this._headerFocusable=this.__getFocusable(o,i):t===this.$.items?(this._itemsFocusable=this.__getFocusable(o,i),this._focusedItemIndex=o.index):t===this.$.footer&&(this._footerFocusable=this.__getFocusable(o,i)),i){let n=this.getEventContext(e);this.__pendingBodyCellFocus=this.loading&&n.section==="body",!this.__pendingBodyCellFocus&&i!==this.$.emptystatecell&&i.dispatchEvent(new CustomEvent("cell-focus",{bubbles:!0,composed:!0,detail:{context:n}})),this._focusedCell=i._focusButton||i,le()&&e.target===i&&this._showTooltip(e)}else this._focusedCell=null}__dispatchPendingBodyCellFocus(){this.__pendingBodyCellFocus&&this.shadowRoot.activeElement===this._itemsFocusable&&this._itemsFocusable.dispatchEvent(new Event("focusin",{bubbles:!0,composed:!0}))}__getFocusable(e,t){return this.__rowFocusMode?e:t._focusButton||t}_detectInteracting(e){let t=e.composedPath().some(i=>i.localName==="slot"&&this.shadowRoot.contains(i));this._setInteracting(t),this.__updateHorizontalScrollPosition()}_updateGridSectionFocusTarget(e){if(!e)return;let t=this._getGridSectionFromFocusTarget(e),i=this.interacting&&t===this._activeRowGroup;e.tabIndex=i?-1:0}_preventScrollerRotatingCellFocus(){this._activeRowGroup===this.$.items&&(this.__preventScrollerRotatingCellFocusDebouncer=f.debounce(this.__preventScrollerRotatingCellFocusDebouncer,G,()=>{let e=this._activeRowGroup===this.$.items;this._getRenderedRows().some(i=>i.index===this._focusedItemIndex)?(this.__updateItemsFocusable(),e&&!this.__rowFocusMode&&(this._focusedCell=this._itemsFocusable),this._navigatingIsHidden&&(this.toggleAttribute("navigating",!0),this._navigatingIsHidden=!1)):e&&(this._focusedCell=null,this.hasAttribute("navigating")&&(this._navigatingIsHidden=!0,this.toggleAttribute("navigating",!1)))}))}_getColumns(e,t){let i=this._columnTree.length-1;return e===this.$.header?i=t:e===this.$.footer&&(i=this._columnTree.length-1-t),this._columnTree[i]}__isValidFocusable(e){return this.$.table.contains(e)&&e.offsetHeight}_resetKeyboardNavigation(){if(!this.$&&this.performUpdate&&this.performUpdate(),["header","footer"].forEach(e=>{if(!this.__isValidFocusable(this[`_${e}Focusable`])){let t=[...this.$[e].children].find(o=>o.offsetHeight),i=t?[...t.children].find(o=>!o.hidden):null;t&&i&&(this[`_${e}Focusable`]=this.__getFocusable(t,i))}}),!this.__isValidFocusable(this._itemsFocusable)&&this.$.items.firstElementChild){let e=this.__getFirstVisibleItem(),t=e?[...e.children].find(i=>!i.hidden):null;t&&e&&(this._focusedColumnOrder=void 0,this._itemsFocusable=this.__getFocusable(e,t))}else this.__updateItemsFocusable()}_scrollHorizontallyToCell(e){if(e.hasAttribute("frozen")||e.hasAttribute("frozen-to-end")||T(e))return;let t=e.getBoundingClientRect(),i=e.parentNode,o=Array.from(i.children).indexOf(e),n=this.$.table.getBoundingClientRect(),s=this.$.table.clientWidth-this.$.table.offsetWidth,l=n.left-(this.__isRTL?s:0),c=n.right+(this.__isRTL?0:s);for(let d=o-1;d>=0;d--){let h=i.children[d];if(!(h.hasAttribute("hidden")||T(h))&&(h.hasAttribute("frozen")||h.hasAttribute("frozen-to-end"))){l=h.getBoundingClientRect().right;break}}for(let d=o+1;d<i.children.length;d++){let h=i.children[d];if(!(h.hasAttribute("hidden")||T(h))&&(h.hasAttribute("frozen")||h.hasAttribute("frozen-to-end"))){c=h.getBoundingClientRect().left;break}}t.left<l&&(this.$.table.scrollLeft+=t.left-l),t.right>c&&(this.$.table.scrollLeft+=t.right-c)}_getGridEventLocation(e){let t=e.__composedPath||e.composedPath(),i=t.indexOf(this.$.table),o=i>=1?t[i-1]:null,n=i>=2?t[i-2]:null,s=i>=3?t[i-3]:null;return{section:o,row:n,cell:s}}_getGridSectionFromFocusTarget(e){return e===this._headerFocusable?this.$.header:e===this._itemsFocusable?this.$.items:e===this._footerFocusable?this.$.footer:null}};var Ne=a=>class extends a{static get properties(){return{__hostVisible:{type:Boolean,value:!1},__tableRect:Object,__headerRect:Object,__itemsRect:Object,__footerRect:Object}}ready(){super.ready();let r=new ResizeObserver(e=>{e.findLast(({target:l})=>l===this)&&(this.__hostVisible=this.checkVisibility());let i=e.findLast(({target:l})=>l===this.$.table);i&&(this.__tableRect=i.contentRect);let o=e.findLast(({target:l})=>l===this.$.header);o&&(this.__headerRect=o.contentRect);let n=e.findLast(({target:l})=>l===this.$.items);n&&(this.__itemsRect=n.contentRect);let s=e.findLast(({target:l})=>l===this.$.footer);s&&(this.__footerRect=s.contentRect)});r.observe(this),r.observe(this.$.table),r.observe(this.$.header),r.observe(this.$.items),r.observe(this.$.footer)}};var We=a=>class extends a{static get properties(){return{detailsOpenedItems:{type:Array,value:()=>[],sync:!0},rowDetailsRenderer:{type:Function,sync:!0},_detailsCells:{type:Array},__detailsOpenedKeys:{type:Object,computed:"__computeDetailsOpenedKeys(itemIdPath, detailsOpenedItems)"}}}static get observers(){return["_detailsOpenedItemsChanged(detailsOpenedItems, rowDetailsRenderer)","_rowDetailsRendererChanged(rowDetailsRenderer)"]}ready(){super.ready(),this._detailsCellResizeObserver=new ResizeObserver(e=>{e.forEach(({target:t})=>{this._updateDetailsCellHeight(t.parentElement)})})}_rowDetailsRendererChanged(e){e&&this._columnTree&&this._getRenderedRows().forEach(t=>{if(!t.querySelector("[part~=details-cell]")){this.__initRow(t,this._columnTree[this._columnTree.length-1]),this.__updateRow(t);return}t.hasAttribute("details-opened")&&this.__updateRow(t)})}_detailsOpenedItemsChanged(e,t){this._getRenderedRows().forEach(i=>{i.hasAttribute("details-opened")!==!!(t&&this._isDetailsOpened(i._item))&&this.__updateRow(i)})}_configureDetailsCell(e){u(e,"cell",!0),u(e,"details-cell",!0),e.toggleAttribute("frozen",!0),this._detailsCellResizeObserver.observe(e)}_toggleDetailsCell(e,t){let i=e.querySelector('[part~="details-cell"]');i&&(i.hidden=!t,!i.hidden&&this.rowDetailsRenderer&&(i._renderer=this.rowDetailsRenderer))}_updateDetailsCellHeight(e){let t=e.querySelector('[part~="details-cell"]');t&&(this.__updateDetailsRowPadding(e,t),requestAnimationFrame(()=>this.__updateDetailsRowPadding(e,t)))}__updateDetailsRowPadding(e,t){t.hidden?e.style.removeProperty("padding-bottom"):e.style.setProperty("padding-bottom",`${t.offsetHeight}px`)}_updateDetailsCellHeights(){this._getRenderedRows().forEach(e=>{this._updateDetailsCellHeight(e)})}_isDetailsOpened(e){return this.__detailsOpenedKeys&&this.__detailsOpenedKeys.has(this.getItemId(e))}__computeDetailsOpenedKeys(e,t){let i=t||[],o=new Set;return i.forEach(n=>{o.add(this.getItemId(n))}),o}openItemDetails(e){this._isDetailsOpened(e)||(this.detailsOpenedItems=[...this.detailsOpenedItems,e])}closeItemDetails(e){this._isDetailsOpened(e)&&(this.detailsOpenedItems=this.detailsOpenedItems.filter(t=>!this._itemsEqual(t,e)))}};var X=class{constructor(r,e){this.host=r,this.scrollTarget=e||r,this.__boundOnScroll=this.__onScroll.bind(this)}hostConnected(){this.initialized||(this.initialized=!0,this.observe())}observe(){let{host:r}=this;this.__resizeObserver=new ResizeObserver(()=>this.__onResize()),this.__resizeObserver.observe(r),[...r.children].forEach(e=>{this.__resizeObserver.observe(e)}),this.__childObserver=new MutationObserver(e=>{e.forEach(({addedNodes:t,removedNodes:i})=>{t.forEach(o=>{o.nodeType===Node.ELEMENT_NODE&&this.__resizeObserver.observe(o)}),i.forEach(o=>{o.nodeType===Node.ELEMENT_NODE&&this.__resizeObserver.unobserve(o)}),t.length===0&&i.length>0&&this.__updateState({sync:!0})})}),this.__childObserver.observe(r,{childList:!0}),this.scrollTarget.addEventListener("scroll",this.__boundOnScroll)}__onResize(){this.__updateState({sync:!1})}__onScroll(){this.__updateState({sync:!0})}__updateState({sync:r}){cancelAnimationFrame(this.__resizeRaf);let e=this.__readState();r?this.__writeState(e):this.__resizeRaf=requestAnimationFrame(()=>this.__writeState(e))}__readState(){let r=this.scrollTarget,e="";r.scrollTop>0&&(e+=" top"),Math.ceil(r.scrollTop)<Math.ceil(r.scrollHeight-r.clientHeight)&&(e+=" bottom");let t=Math.abs(r.scrollLeft);return t>0&&(e+=" start"),Math.ceil(t)<Math.ceil(r.scrollWidth-r.clientWidth)&&(e+=" end"),{overflow:e.trim()}}__writeState({overflow:r}){r?this.host.setAttribute("overflow",r):this.host.removeAttribute("overflow")}};var Ve={SCROLLING:500,UPDATE_CONTENT_VISIBILITY:100},Ge=a=>class extends a{static get properties(){return{columnRendering:{type:String,value:"eager",sync:!0},_frozenCells:{type:Array,value:()=>[]},_frozenToEndCells:{type:Array,value:()=>[]}}}static get observers(){return["__columnRenderingChanged(_columnTree, columnRendering)"]}get _scrollLeft(){return this.$.table.scrollLeft}get _scrollTop(){return this.$.table.scrollTop}set _scrollTop(e){this.$.table.scrollTop=e}get _lazyColumns(){return this.columnRendering==="lazy"}ready(){super.ready(),this.scrollTarget=this.$.table,this.$.items.addEventListener("focusin",e=>{let t=e.composedPath(),i=t[t.indexOf(this.$.items)-1];if(i){if(!this._isMousedown){let o=this.$.table.clientHeight,n=this.$.header.clientHeight,s=this.$.footer.clientHeight,l=o-n-s,d=i.clientHeight>l?e.target:i;this.__scrollIntoViewport(d)}this.$.table.contains(e.relatedTarget)||this.$.table.dispatchEvent(new CustomEvent("virtualizer-element-focused",{detail:{element:i}}))}}),this.$.table.addEventListener("scroll",()=>this._afterScroll()),this.__overflowController=new X(this,this.$.table),this.addController(this.__overflowController)}_scrollToFlatIndex(e){e=Math.min(this._flatSize-1,Math.max(0,e)),this.__virtualizer.scrollToIndex(e);let t=[...this.$.items.children].find(i=>i.index===e);this.__scrollIntoViewport(t)}scrollToColumn(e){if(!this._columnTree){this.__pendingScrollToColumn=e;return}let t=this._getColumnsInOrder(),i;if(typeof e=="number"){if(i=t[e],!i){console.warn(`Column index ${e} is out of bounds`);return}}else if(i=e,!t.includes(i)){console.warn("Column is not a visible column of this grid");return}i.frozen||i.frozenToEnd||(this._scrollHorizontallyToCell(i._headerCell),this.__updateHorizontalScrollPosition(),this.__updateColumnsBodyContentHidden())}__scrollToPendingColumn(){if(this.__pendingScrollToColumn!==void 0){let e=this.__pendingScrollToColumn;delete this.__pendingScrollToColumn,this.scrollToColumn(e)}}__scrollIntoViewport(e){if(!e)return;let t=e.getBoundingClientRect(),i=getComputedStyle(e),o=t.top+parseInt(i.scrollMarginTop||0),n=t.bottom+parseInt(i.scrollMarginBottom||0),s=this.$.footer.getBoundingClientRect().top,l=this.$.header.getBoundingClientRect().bottom;n>s?this.$.table.scrollTop+=n-s:o<l&&(this.$.table.scrollTop-=l-o)}_scheduleScrolling(){this._scrollingFrame||(this._scrollingFrame=requestAnimationFrame(()=>this.$.scroller.toggleAttribute("scrolling",!0))),this._debounceScrolling=f.debounce(this._debounceScrolling,E.after(Ve.SCROLLING),()=>{cancelAnimationFrame(this._scrollingFrame),delete this._scrollingFrame,this.$.scroller.toggleAttribute("scrolling",!1)})}_afterScroll(){this.__updateHorizontalScrollPosition(),this.hasAttribute("reordering")||this._scheduleScrolling(),this.hasAttribute("navigating")||this._hideTooltip(!0),this._debounceColumnContentVisibility=f.debounce(this._debounceColumnContentVisibility,E.after(Ve.UPDATE_CONTENT_VISIBILITY),()=>{this._lazyColumns&&this.__cachedScrollLeft!==this._scrollLeft&&(this.__cachedScrollLeft=this._scrollLeft,this.__updateColumnsBodyContentHidden())})}__updateColumnsBodyContentHidden(){if(!this._columnTree||!this._areSizerCellsAssigned())return;this.__scrollToPendingColumn();let e=this._getColumnsInOrder(),t=!1;if(e.forEach(i=>{let o=this._lazyColumns&&!this.__isColumnInViewport(i);i._bodyContentHidden!==o&&(t=!0,i._cells.forEach(n=>{if(n!==i._sizerCell){if(o)n.remove();else if(n.__parentRow){let s=[...n.__parentRow.children].find(l=>e.indexOf(l._column)>e.indexOf(i));n.__parentRow.insertBefore(n,s)}}})),i._bodyContentHidden=o}),t&&this._frozenCellsChanged(),this._lazyColumns){let i=[...e].reverse().find(s=>s.frozen),o=this.__getColumnEnd(i),n=e.find(s=>!s.frozen&&!s._bodyContentHidden);this.__lazyColumnsStart=this.__getColumnStart(n)-o,this.$.items.style.setProperty("--_grid-lazy-columns-start",`${this.__lazyColumnsStart}px`),this._resetKeyboardNavigation()}}__getColumnEnd(e){return e?e._sizerCell.offsetLeft+(this.__isRTL?0:e._sizerCell.offsetWidth):this.__isRTL?this.$.table.clientWidth:0}__getColumnStart(e){return e?e._sizerCell.offsetLeft+(this.__isRTL?e._sizerCell.offsetWidth:0):this.__isRTL?this.$.table.clientWidth:0}__isColumnInViewport(e){return e.frozen||e.frozenToEnd?!0:this.__isHorizontallyInViewport(e._sizerCell)}__isHorizontallyInViewport(e){return e.offsetLeft+e.offsetWidth>=this._scrollLeft&&e.offsetLeft<=this._scrollLeft+this.clientWidth}__columnRenderingChanged(e,t){t==="eager"?this.$.scroller.removeAttribute("column-rendering"):this.$.scroller.setAttribute("column-rendering",t),this.__updateColumnsBodyContentHidden()}_frozenCellsChanged(){this._debouncerCacheElements=f.debounce(this._debouncerCacheElements,C,()=>{Array.from(this.shadowRoot.querySelectorAll('[part~="cell"]')).forEach(e=>{e.style.transform=""}),this._frozenCells=Array.prototype.slice.call(this.$.table.querySelectorAll("[frozen]")),this._frozenToEndCells=Array.prototype.slice.call(this.$.table.querySelectorAll("[frozen-to-end]")),this.__updateHorizontalScrollPosition()}),this._debounceUpdateFrozenColumn()}_debounceUpdateFrozenColumn(){this.__debounceUpdateFrozenColumn=f.debounce(this.__debounceUpdateFrozenColumn,C,()=>this._updateFrozenColumn())}_updateFrozenColumn(){if(!this._columnTree)return;let e=this._columnTree[this._columnTree.length-1].slice(0);e.sort((o,n)=>o._order-n._order);let t,i;for(let o=0;o<e.length;o++){let n=e[o];n._lastFrozen=!1,n._firstFrozenToEnd=!1,i===void 0&&n.frozenToEnd&&!n.hidden&&(i=o),n.frozen&&!n.hidden&&(t=o)}t!==void 0&&(e[t]._lastFrozen=!0),i!==void 0&&(e[i]._firstFrozenToEnd=!0),this.__updateColumnsBodyContentHidden()}__updateHorizontalScrollPosition(){if(!this._columnTree)return;let e=this.$.table.scrollWidth,t=this.$.table.clientWidth,i=Math.max(0,this.$.table.scrollLeft),o=Ce(this.$.table,this.getAttribute("dir")),n=`translate(${-i}px, 0)`;this.$.header.style.transform=n,this.$.footer.style.transform=n,this.$.items.style.transform=n;let s=this.__isRTL?o+t-e:i;this.__horizontalScrollPosition=s;let l=`translate(${s}px, 0)`;this._frozenCells.forEach(p=>{p.style.transform=l});let c=this.__isRTL?o:i+t-e,d=`translate(${c}px, 0)`,h=d;if(this._lazyColumns&&this._areSizerCellsAssigned()){let p=this._getColumnsInOrder(),k=[...p].reverse().find(M=>!M.frozenToEnd&&!M._bodyContentHidden),I=this.__getColumnEnd(k),g=p.find(M=>M.frozenToEnd),w=this.__getColumnStart(g);h=`translate(${c+(w-I)+this.__lazyColumnsStart}px, 0)`}this._frozenToEndCells.forEach(p=>{this.$.items.contains(p)?p.style.transform=h:p.style.transform=d});let _=this.shadowRoot.querySelector("[part~='row']:focus");_&&this.__updateRowScrollPositionProperty(_);let v=this.$.header.querySelector("[part~='last-header-row']");v&&this.__updateRowScrollPositionProperty(v);let y=this.$.footer.querySelector("[part~='first-footer-row']");y&&this.__updateRowScrollPositionProperty(y)}__updateRowScrollPositionProperty(e){if(!(e instanceof HTMLTableRowElement))return;let t=`${this.__horizontalScrollPosition}px`;e.style.getPropertyValue("--_grid-horizontal-scroll-position")!==t&&e.style.setProperty("--_grid-horizontal-scroll-position",t)}_areSizerCellsAssigned(){return this._getColumnsInOrder().every(e=>e._sizerCell)}};var Ue=a=>class extends a{static get properties(){return{selectedItems:{type:Object,notify:!0,value:()=>[],sync:!0},isItemSelectable:{type:Function},__selectedKeys:{type:Object,computed:"__computeSelectedKeys(itemIdPath, selectedItems)"}}}static get observers(){return["__selectedItemsChanged(itemIdPath, selectedItems, isItemSelectable)"]}_isSelected(e){return this.__selectedKeys.has(this.getItemId(e))}__isItemSelectable(e){return!this.isItemSelectable||!e?!0:this.isItemSelectable(e)}selectItem(e){this._isSelected(e)||(this.selectedItems=[...this.selectedItems,e])}deselectItem(e){this._isSelected(e)&&(this.selectedItems=this.selectedItems.filter(t=>!this._itemsEqual(t,e)))}updated(e){super.updated(e),e.has("isItemSelectable")&&this.dispatchEvent(new CustomEvent("is-item-selectable-changed"))}__selectedItemsChanged(){this._getRenderedRows().forEach(e=>{(e.hasAttribute("selected")!==this._isSelected(e._item)||e.hasAttribute("nonselectable")!==!this.__isItemSelectable(e._item))&&this.__updateRow(e)})}__computeSelectedKeys(e,t){let i=t||[],o=new Set;return i.forEach(n=>{o.add(this.getItemId(n))}),o}};var qe="prepend",Ke=a=>class extends a{static get properties(){return{multiSort:{type:Boolean,value:!1},multiSortPriority:{type:String,value:()=>qe},multiSortOnShiftClick:{type:Boolean,value:!1},_sorters:{type:Array,value:()=>[]},_previousSorters:{type:Array,value:()=>[]}}}static setDefaultMultiSortPriority(e){qe=["append","prepend"].includes(e)?e:"prepend"}ready(){super.ready(),this.addEventListener("sorter-changed",this._onSorterChanged)}_onSorterChanged(e){let t=e.target;e.stopPropagation(),t._grid=this,this.__updateSorter(t,e.detail.shiftClick,e.detail.fromSorterClick),this.__applySorters()}__removeSorters(e){e.length!==0&&(this._sorters=this._sorters.filter(t=>!e.includes(t)),this.__applySorters())}__updateSortOrders(){this._sorters.forEach(t=>{t._order=null});let e=this._getActiveSorters();e.length>1&&e.forEach((t,i)=>{t._order=i})}__updateSorter(e,t,i){if(!e.direction&&!this._sorters.includes(e))return;e._order=null;let o=this._sorters.filter(n=>n!==e);this.multiSort&&(!this.multiSortOnShiftClick||!i)||this.multiSortOnShiftClick&&t?this.multiSortPriority==="append"?this._sorters=[...o,e]:this._sorters=[e,...o]:(e.direction||this.multiSortOnShiftClick)&&(this._sorters=e.direction?[e]:[],o.forEach(n=>{n._order=null,n.direction=null}))}__applySorters(){this.__updateSortOrders(),this.dataProvider&&this.isAttached&&JSON.stringify(this._previousSorters)!==JSON.stringify(this._mapSorters())&&this.__debounceClearCache(),this.__a11yUpdateSorters(),this._previousSorters=this._mapSorters()}_getActiveSorters(){return this._sorters.filter(e=>e.direction&&e.isConnected)}_mapSorters(){return this._getActiveSorters().map(e=>({path:e.path,direction:e.direction}))}};var je=a=>class extends a{static get properties(){return{cellPartNameGenerator:{type:Function,sync:!0}}}static get observers(){return["__cellPartNameGeneratorChanged(cellPartNameGenerator)"]}__cellPartNameGeneratorChanged(){this.generateCellPartNames()}generateCellPartNames(){m(this.$.items,e=>{e.hidden||this._generateCellPartNames(e,this.__getRowModel(e))})}_generateCellPartNames(e,t){x(e,i=>{if(i.__generatedParts&&i.__generatedParts.forEach(o=>{u(i,o,null)}),this.cellPartNameGenerator&&!e.hasAttribute("loading")){let o=this.cellPartNameGenerator(i._column,t);i.__generatedParts=o&&o.split(" ").filter(n=>n.length>0),i.__generatedParts&&i.__generatedParts.forEach(n=>{u(i,n,!0)})}})}};var Xe=a=>class extends Pe(Fe(De(ke(Se(Ge(Ue(Ke(We(Be(Ee(He(Ae($e(Me(Le(je(me(Ne(a))))))))))))))))))){static get observers(){return["_columnTreeChanged(_columnTree)","_flatSizeChanged(_flatSize, __virtualizer, _hasData, _columnTree)"]}static get properties(){return{_safari:{type:Boolean,value:W},_ios:{type:Boolean,value:J},_firefox:{type:Boolean,value:ae},_android:{type:Boolean,value:Y},_touchDevice:{type:Boolean,value:V},allRowsVisible:{type:Boolean,value:!1,reflectToAttribute:!0},isAttached:{value:!1},__gridElement:{type:Boolean,value:!0},__hasEmptyStateContent:{type:Boolean,value:!1},__emptyState:{type:Boolean,computed:"__computeEmptyState(_flatSize, __hasEmptyStateContent)"}}}get _firstVisibleIndex(){let r=this.__getFirstVisibleItem();return r?r.index:void 0}get _lastVisibleIndex(){let r=this.__getLastVisibleItem();return r?r.index:void 0}connectedCallback(){super.connectedCallback(),this.isAttached=!0,this.__virtualizer.hostConnected()}disconnectedCallback(){super.disconnectedCallback(),this.isAttached=!1,this._hideTooltip(!0)}__getFirstVisibleItem(){return this._getRenderedRows().find(r=>this._isInViewport(r))}__getLastVisibleItem(){return this._getRenderedRows().reverse().find(r=>this._isInViewport(r))}_isInViewport(r){let e=this.$.table.getBoundingClientRect(),t=r.getBoundingClientRect(),i=this.$.header.getBoundingClientRect().height,o=this.$.footer.getBoundingClientRect().height;return t.bottom>e.top+i&&t.top<e.bottom-o}_getRenderedRows(){return Array.from(this.$.items.children).filter(r=>!r.hidden).sort((r,e)=>r.index-e.index)}_getRowContainingNode(r){let e=ce("vaadin-grid-cell-content",r);return e?e.assignedSlot.parentElement.parentElement:void 0}_isItemAssignedToRow(r,e){let t=this.__getRowModel(e);return this.getItemId(r)===this.getItemId(t.item)}ready(){super.ready(),Z(this,""),Z(this.$.scroller,""),this.__virtualizer=new _e({createElements:r=>this.__createVirtualizerElements(r),updateElement:(r,e)=>{this.__updateVirtualizerElement(r,e)},scrollContainer:this.$.items,scrollTarget:this.$.table,reorderElements:!0,__disableHeightPlaceholder:!0}),this._tooltipController=new pe(this),this.addController(this._tooltipController),this._tooltipController.setManual(!0),this.__emptyStateContentObserver=new ge(this.$.emptystateslot,({currentNodes:r})=>{this.$.emptystatecell._content=r[0],this.__hasEmptyStateContent=!!this.$.emptystatecell._content})}updated(r){super.updated(r),r.has("__hostVisible")&&!r.get("__hostVisible")&&(this._resetKeyboardNavigation(),requestAnimationFrame(()=>this.__scrollToPendingIndexes())),(r.has("__headerRect")||r.has("__footerRect")||r.has("__itemsRect"))&&setTimeout(()=>this.__updateMinHeight()),r.has("__tableRect")&&(setTimeout(()=>this.__updateColumnsBodyContentHidden()),this.__updateHorizontalScrollPosition())}__getBodyCellCoordinates(r){if(this.$.items.contains(r)&&r.localName==="td")return{item:r.parentElement._item,column:r._column}}__focusBodyCell({item:r,column:e}){let t=this._getRenderedRows().find(o=>o._item===r),i=t&&[...t.children].find(o=>o._column===e);i&&i.focus()}_focusFirstVisibleRow(){let r=this.__getFirstVisibleItem();this.__rowFocusMode=!0,r.focus()}_flatSizeChanged(r,e,t,i){if(e&&t&&i){let o=this.shadowRoot.activeElement,n=this.__getBodyCellCoordinates(o),s=e.size||0;e.size=r,e.update(s-1,s-1),r<s&&e.update(r-1,r-1),n&&o.parentElement.hidden&&this.__focusBodyCell(n),this._resetKeyboardNavigation()}}__createVirtualizerElements(r){let e=[];for(let t=0;t<r;t++){let i=document.createElement("tr");i.setAttribute("role","row"),i.setAttribute("tabindex","-1"),u(i,"row",!0),u(i,"body-row",!0),this._columnTree&&this.__initRow(i,this._columnTree[this._columnTree.length-1],"body",!1,!0),e.push(i)}return this._columnTree&&this._columnTree[this._columnTree.length-1].forEach(t=>{t.isConnected&&t._cells&&(t._cells=[...t._cells])}),e}_createCell(r,e){let i=`vaadin-grid-cell-content-${this._contentIndex=this._contentIndex+1||0}`,o=document.createElement("vaadin-grid-cell-content");o.setAttribute("slot",i);let n=document.createElement(r);n.id=i.replace("-content-","-"),n.setAttribute("role",r==="td"?"gridcell":"columnheader"),!Y&&!J&&(n.addEventListener("mouseenter",l=>{this.$.scroller.hasAttribute("scrolling")||this._showTooltip(l)}),n.addEventListener("mouseleave",()=>{this._hideTooltip()}),n.addEventListener("mousedown",()=>{this._hideTooltip(!0)}));let s=document.createElement("slot");if(s.setAttribute("name",i),e?._focusButtonMode){let l=document.createElement("div");l.setAttribute("role","button"),l.setAttribute("tabindex","-1"),n.appendChild(l),n._focusButton=l,n.focus=function(c){n._focusButton.focus(c)},l.appendChild(s)}else n.setAttribute("tabindex","-1"),n.appendChild(s);return n._content=o,o.addEventListener("mousedown",()=>{if($){let l=c=>{let d=o.contains(this.getRootNode().activeElement),h=c.composedPath().includes(o);!d&&h&&n.focus({preventScroll:!0}),document.removeEventListener("mouseup",l,!0)};document.addEventListener("mouseup",l,!0)}else setTimeout(()=>{o.contains(this.getRootNode().activeElement)||n.focus({preventScroll:!0})})}),n}__initRow(r,e,t="body",i=!1,o=!1){let n=document.createDocumentFragment();x(r,s=>{s._vacant=!0}),r.innerHTML="",t==="body"&&(r.__cells=[],r.__detailsCell=null),e.filter(s=>!s.hidden).toSorted((s,l)=>s._order-l._order).forEach((s,l,c)=>{let d;if(t==="body"){s._cells||(s._cells=[]),d=s._cells.find(_=>_._vacant),d||(d=this._createCell("td",s),s._onCellKeyDown&&d.addEventListener("keydown",s._onCellKeyDown.bind(s)),s._cells.push(d)),u(d,"cell",!0),u(d,"body-cell",!0),d.__parentRow=r,r.__cells.push(d);let h=r===this.$.sizer;if((!s._bodyContentHidden||h)&&r.appendChild(d),h&&(s._sizerCell=d),l===c.length-1&&this.rowDetailsRenderer){this._detailsCells||(this._detailsCells=[]);let _=this._detailsCells.find(v=>v._vacant)||this._createCell("td");this._detailsCells.indexOf(_)===-1&&this._detailsCells.push(_),_._content.parentElement||n.appendChild(_._content),this._configureDetailsCell(_),_.__parentRow=r,r.appendChild(_),r.__detailsCell=_,this.__a11ySetRowDetailsCell(r,_),_._vacant=!1}o||(s._cells=[...s._cells])}else{let h=t==="header"?"th":"td";i||s.localName==="vaadin-grid-column-group"?(d=s[`_${t}Cell`],d||(d=this._createCell(h),s._onCellKeyDown&&d.addEventListener("keydown",s._onCellKeyDown.bind(s))),d._column=s,r.appendChild(d),s[`_${t}Cell`]=d):(s._emptyCells||(s._emptyCells=[]),d=s._emptyCells.find(_=>_._vacant)||this._createCell(h),d._column=s,r.appendChild(d),s._emptyCells.indexOf(d)===-1&&s._emptyCells.push(d)),u(d,"cell",!0),u(d,`${t}-cell`,!0)}d._content.parentElement||n.appendChild(d._content),d._vacant=!1,d._column=s}),t!=="body"&&this.__debounceUpdateHeaderFooterRowVisibility(r),this.appendChild(n),this._frozenCellsChanged(),this._updateFirstAndLastColumnForRow(r)}__debounceUpdateHeaderFooterRowVisibility(r){r.__debounceUpdateHeaderFooterRowVisibility=f.debounce(r.__debounceUpdateHeaderFooterRowVisibility,C,()=>this.__updateHeaderFooterRowVisibility(r))}__updateHeaderFooterRowVisibility(r){if(!r)return;let e=Array.from(r.children).filter(t=>{let i=t._column;if(i._emptyCells&&i._emptyCells.indexOf(t)>-1)return!1;if(r.parentElement===this.$.header){if(i.headerRenderer)return!0;if(i.header===null)return!1;if(i.path||i.header!==void 0)return!0}else if(i.footerRenderer)return!0;return!1});r.hidden!==!e.length&&(r.hidden=!e.length),r.parentElement===this.$.header&&(this.$.table.toggleAttribute("has-header",this.$.header.querySelector("tr:not([hidden])")),this.__updateHeaderFooterRowParts("header")),r.parentElement===this.$.footer&&(this.$.table.toggleAttribute("has-footer",this.$.footer.querySelector("tr:not([hidden])")),this.__updateHeaderFooterRowParts("footer")),this._resetKeyboardNavigation(),this.__a11yUpdateGridSize(this.size,this._columnTree,this.__emptyState)}__updateVirtualizerElement(r,e){this._preventScrollerRotatingCellFocus(r,e),this._columnTree&&(r.index=e,this.__ensureRowItem(r),this.__ensureRowHierarchy(r),this.__updateRow(r))}_columnTreeChanged(r){this._renderColumnTree(r),this.__updateColumnsBodyContentHidden()}__updateRowOrderParts(r){S(r,{first:r.index===0,last:r.index===this._flatSize-1,odd:r.index%2!==0,even:r.index%2===0})}__updateRowStateParts(r,{item:e,expanded:t,selected:i,detailsOpened:o}){S(r,{expanded:t,collapsed:this.__isRowExpandable(r),selected:i,nonselectable:this.__isItemSelectable(e)===!1,"details-opened":o})}__computeEmptyState(r,e){return r===0&&e}_renderColumnTree(r){for(m(this.$.items,e=>{this.__initRow(e,r[r.length-1],"body",!1,!0),this.__updateRow(e)});this.$.header.children.length<r.length;){let e=document.createElement("tr");e.setAttribute("role","row"),e.setAttribute("tabindex","-1"),u(e,"row",!0),u(e,"header-row",!0),this.$.header.appendChild(e);let t=document.createElement("tr");t.setAttribute("role","row"),t.setAttribute("tabindex","-1"),u(t,"row",!0),u(t,"footer-row",!0),this.$.footer.appendChild(t)}for(;this.$.header.children.length>r.length;)this.$.header.removeChild(this.$.header.firstElementChild),this.$.footer.removeChild(this.$.footer.firstElementChild);m(this.$.header,(e,t)=>{this.__initRow(e,r[t],"header",t===r.length-1)}),m(this.$.footer,(e,t)=>{this.__initRow(e,r[r.length-1-t],"footer",t===0)}),this.__initRow(this.$.sizer,r[r.length-1]),this.__updateHeaderFooterRowParts("header"),this.__updateHeaderFooterRowParts("footer"),this._resizeHandler(),this._frozenCellsChanged(),this._updateFirstAndLastColumn(),this._resetKeyboardNavigation(),this.__a11yUpdateHeaderRows(),this.__a11yUpdateFooterRows(),this.generateCellPartNames(),this.__updateHeaderAndFooter()}__updateHeaderFooterRowParts(r){let e=[...this.$[r].querySelectorAll("tr:not([hidden])")];[...this.$[r].children].forEach(t=>{u(t,`first-${r}-row`,t===e.at(0)),u(t,`last-${r}-row`,t===e.at(-1)),b(t).forEach(i=>{u(i,`first-${r}-row-cell`,t===e.at(0)),u(i,`last-${r}-row-cell`,t===e.at(-1))})})}__updateRowLoading(r,e){let t=b(r);L(r,"loading",e),D(t,"loading-row-cell",e),e&&this._generateCellPartNames(r)}__updateRow(r){this.__a11yUpdateRowRowindex(r),this.__updateRowOrderParts(r);let e=this.__getRowItem(r);if(e)this.__updateRowLoading(r,!1);else{this.__updateRowLoading(r,!0);return}r._item=e;let t=this.__getRowModel(r);this._toggleDetailsCell(r,t.detailsOpened),this.__a11yUpdateRowLevel(r,t.level),this.__a11yUpdateRowSelected(r,t.selected),this.__updateRowStateParts(r,t),this._generateCellPartNames(r,t),this._filterDragAndDrop(r,t),this.__updateDragSourceParts(r,t),m(r,i=>{if(!(i._column&&!i._column.isConnected)&&i._renderer){let o=i._column||this;i._renderer.call(o,i._content,o,t)}}),this._updateDetailsCellHeight(r),this.__a11yUpdateRowExpanded(r,t.expanded)}_resizeHandler(){this._updateDetailsCellHeights(),this.__updateHorizontalScrollPosition()}__getRowModel(r){return{index:r.index,item:r._item,level:this.__getRowLevel(r),expanded:this._isExpanded(r._item),selected:this._isSelected(r._item),hasChildren:this._hasChildren(r._item),detailsOpened:!!this.rowDetailsRenderer&&this._isDetailsOpened(r._item)}}_showTooltip(r){if(this._tooltipController.node?.isConnected){let t=r.target;if(!this.__isCellFullyVisible(t))return;this._tooltipController.setTarget(t),this._tooltipController.setContext(this.getEventContext(r)),this._tooltipController.open({focus:r.type==="focusin",hover:r.type==="mouseenter"})}}__isCellFullyVisible(r){if(r.hasAttribute("frozen")||r.hasAttribute("frozen-to-end"))return!0;let{left:e,right:t}=this.getBoundingClientRect(),i=[...r.parentNode.children].find(s=>s.hasAttribute("last-frozen"));if(i){let s=i.getBoundingClientRect();e=this.__isRTL?e:s.right,t=this.__isRTL?s.left:t}let o=[...r.parentNode.children].find(s=>s.hasAttribute("first-frozen-to-end"));if(o){let s=o.getBoundingClientRect();e=this.__isRTL?s.right:e,t=this.__isRTL?t:s.left}let n=r.getBoundingClientRect();return n.left>=e&&n.right<=t}_hideTooltip(r){this._tooltipController.close(r)}requestContentUpdate(){this.__updateHeaderAndFooter(),this.__updateVisibleRows()}__updateHeaderAndFooter(){(this._columnTree||[]).forEach(r=>{r.forEach(e=>{e._renderHeaderAndFooter&&e._renderHeaderAndFooter()})})}__updateVisibleRows(r,e){this.__virtualizer?.update(r,e)}__updateMinHeight(){let e=this.$.header.clientHeight,t=this.$.footer.clientHeight,i=this.$.table.offsetHeight-this.$.table.clientHeight,o=e+36+t+i;this.__minHeightStyleSheet||(this.__minHeightStyleSheet=new CSSStyleSheet,this.shadowRoot.adoptedStyleSheets.push(this.__minHeightStyleSheet)),this.__minHeightStyleSheet.replaceSync(`:host { --_grid-min-height: ${o}px; }`)}};var te=class extends Xe(fe(se(N(ne(H))))){static get is(){return"vaadin-grid"}static get styles(){return Re}render(){return re`
      <div
        id="scroller"
        ?safari="${this._safari}"
        ?ios="${this._ios}"
        ?loading="${this.loading}"
        ?column-reordering-allowed="${this.columnReorderingAllowed}"
        ?empty-state="${this.__emptyState}"
      >
        <table
          id="table"
          role="treegrid"
          aria-multiselectable="true"
          tabindex="0"
          aria-label="${ue(this.accessibleName)}"
        >
          <caption id="sizer" part="row"></caption>
          <thead id="header" role="rowgroup"></thead>
          <tbody id="items" role="rowgroup"></tbody>
          <tbody id="emptystatebody">
            <tr id="emptystaterow">
              <td part="empty-state" class="empty-state" id="emptystatecell" tabindex="0">
                <slot name="empty-state" id="emptystateslot"></slot>
              </td>
            </tr>
          </tbody>
          <tfoot id="footer" role="rowgroup"></tfoot>
        </table>

        <div part="reorder-ghost" class="reorder-ghost"></div>
      </div>

      <slot name="tooltip"></slot>

      <div id="focusexit" tabindex="0"></div>
    `}};B(te);
/*! Bundled license information:

@vaadin/grid/src/vaadin-grid-helpers.js:
@vaadin/grid/src/vaadin-grid-column-mixin.js:
@vaadin/grid/src/vaadin-grid-column.js:
@vaadin/grid/src/styles/vaadin-grid-base-styles.js:
@vaadin/grid/src/vaadin-grid-a11y-mixin.js:
@vaadin/grid/src/vaadin-grid-active-item-mixin.js:
@vaadin/grid/src/vaadin-grid-array-data-provider-mixin.js:
@vaadin/grid/src/vaadin-grid-column-auto-width-mixin.js:
@vaadin/grid/src/vaadin-grid-column-reordering-mixin.js:
@vaadin/grid/src/vaadin-grid-column-resizing-mixin.js:
@vaadin/grid/src/vaadin-grid-data-provider-mixin.js:
@vaadin/grid/src/vaadin-grid-drag-and-drop-mixin.js:
@vaadin/grid/src/vaadin-grid-dynamic-columns-mixin.js:
@vaadin/grid/src/vaadin-grid-event-context-mixin.js:
@vaadin/grid/src/vaadin-grid-filter-mixin.js:
@vaadin/grid/src/vaadin-grid-keyboard-navigation-mixin.js:
@vaadin/grid/src/vaadin-grid-resize-mixin.js:
@vaadin/grid/src/vaadin-grid-row-details-mixin.js:
@vaadin/grid/src/vaadin-grid-scroll-mixin.js:
@vaadin/grid/src/vaadin-grid-selection-mixin.js:
@vaadin/grid/src/vaadin-grid-sort-mixin.js:
@vaadin/grid/src/vaadin-grid-styling-mixin.js:
@vaadin/grid/src/vaadin-grid-mixin.js:
@vaadin/grid/src/vaadin-grid.js:
  (**
   * @license
   * Copyright (c) 2016 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/grid/src/array-data-provider.js:
  (**
   * @license
   * Copyright (c) 2000 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/component-base/src/overflow-controller.js:
  (**
   * @license
   * Copyright (c) 2021 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)
*/
