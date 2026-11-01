var me=globalThis,ve=me.ShadowRoot&&(me.ShadyCSS===void 0||me.ShadyCSS.nativeShadow)&&"adoptedStyleSheets"in Document.prototype&&"replace"in CSSStyleSheet.prototype,ut=Symbol(),hi=new WeakMap,j=class{constructor(t,e,i){if(this._$cssResult$=!0,i!==ut)throw Error("CSSResult is not constructable. Use `unsafeCSS` or `css` instead.");this.cssText=t,this.t=e}get styleSheet(){let t=this.o,e=this.t;if(ve&&t===void 0){let i=e!==void 0&&e.length===1;i&&(t=hi.get(e)),t===void 0&&((this.o=t=new CSSStyleSheet).replaceSync(this.cssText),i&&hi.set(e,t))}return t}toString(){return this.cssText}},pt=s=>new j(typeof s=="string"?s:s+"",void 0,ut),u=(s,...t)=>{let e=s.length===1?s[0]:t.reduce((i,r,o)=>i+(n=>{if(n._$cssResult$===!0)return n.cssText;if(typeof n=="number")return n;throw Error("Value passed to 'css' function must be a 'css' function result: "+n+". Use 'unsafeCSS' to pass non-literal values, but take care to ensure page security.")})(r)+s[o+1],s[0]);return new j(e,s,ut)},ge=(s,t)=>{if(ve)s.adoptedStyleSheets=t.map(e=>e instanceof CSSStyleSheet?e:e.styleSheet);else for(let e of t){let i=document.createElement("style"),r=me.litNonce;r!==void 0&&i.setAttribute("nonce",r),i.textContent=e.cssText,s.appendChild(i)}},_t=ve?s=>s:s=>s instanceof CSSStyleSheet?(t=>{let e="";for(let i of t.cssRules)e+=i.cssText;return pt(e)})(s):s;var{is:Bs,defineProperty:Fs,getOwnPropertyDescriptor:Rs,getOwnPropertyNames:Hs,getOwnPropertySymbols:js,getPrototypeOf:Us}=Object,be=globalThis,ci=be.trustedTypes,qs=ci?ci.emptyScript:"",Ws=be.reactiveElementPolyfillSupport,se=(s,t)=>s,ft={toAttribute(s,t){switch(t){case Boolean:s=s?qs:null;break;case Object:case Array:s=s==null?s:JSON.stringify(s)}return s},fromAttribute(s,t){let e=s;switch(t){case Boolean:e=s!==null;break;case Number:e=s===null?null:Number(s);break;case Object:case Array:try{e=JSON.parse(s)}catch{e=null}}return e}},ye=(s,t)=>!Bs(s,t),ui={attribute:!0,type:String,converter:ft,reflect:!1,useDefault:!1,hasChanged:ye};Symbol.metadata??=Symbol("metadata"),be.litPropertyMetadata??=new WeakMap;var T=class extends HTMLElement{static addInitializer(t){this._$Ei(),(this.l??=[]).push(t)}static get observedAttributes(){return this.finalize(),this._$Eh&&[...this._$Eh.keys()]}static createProperty(t,e=ui){if(e.state&&(e.attribute=!1),this._$Ei(),this.prototype.hasOwnProperty(t)&&((e=Object.create(e)).wrapped=!0),this.elementProperties.set(t,e),!e.noAccessor){let i=Symbol(),r=this.getPropertyDescriptor(t,i,e);r!==void 0&&Fs(this.prototype,t,r)}}static getPropertyDescriptor(t,e,i){let{get:r,set:o}=Rs(this.prototype,t)??{get(){return this[e]},set(n){this[e]=n}};return{get:r,set(n){let l=r?.call(this);o?.call(this,n),this.requestUpdate(t,l,i)},configurable:!0,enumerable:!0}}static getPropertyOptions(t){return this.elementProperties.get(t)??ui}static _$Ei(){if(this.hasOwnProperty(se("elementProperties")))return;let t=Us(this);t.finalize(),t.l!==void 0&&(this.l=[...t.l]),this.elementProperties=new Map(t.elementProperties)}static finalize(){if(this.hasOwnProperty(se("finalized")))return;if(this.finalized=!0,this._$Ei(),this.hasOwnProperty(se("properties"))){let e=this.properties,i=[...Hs(e),...js(e)];for(let r of i)this.createProperty(r,e[r])}let t=this[Symbol.metadata];if(t!==null){let e=litPropertyMetadata.get(t);if(e!==void 0)for(let[i,r]of e)this.elementProperties.set(i,r)}this._$Eh=new Map;for(let[e,i]of this.elementProperties){let r=this._$Eu(e,i);r!==void 0&&this._$Eh.set(r,e)}this.elementStyles=this.finalizeStyles(this.styles)}static finalizeStyles(t){let e=[];if(Array.isArray(t)){let i=new Set(t.flat(1/0).reverse());for(let r of i)e.unshift(_t(r))}else t!==void 0&&e.push(_t(t));return e}static _$Eu(t,e){let i=e.attribute;return i===!1?void 0:typeof i=="string"?i:typeof t=="string"?t.toLowerCase():void 0}constructor(){super(),this._$Ep=void 0,this.isUpdatePending=!1,this.hasUpdated=!1,this._$Em=null,this._$Ev()}_$Ev(){this._$ES=new Promise(t=>this.enableUpdating=t),this._$AL=new Map,this._$E_(),this.requestUpdate(),this.constructor.l?.forEach(t=>t(this))}addController(t){(this._$EO??=new Set).add(t),this.renderRoot!==void 0&&this.isConnected&&t.hostConnected?.()}removeController(t){this._$EO?.delete(t)}_$E_(){let t=new Map,e=this.constructor.elementProperties;for(let i of e.keys())this.hasOwnProperty(i)&&(t.set(i,this[i]),delete this[i]);t.size>0&&(this._$Ep=t)}createRenderRoot(){let t=this.shadowRoot??this.attachShadow(this.constructor.shadowRootOptions);return ge(t,this.constructor.elementStyles),t}connectedCallback(){this.renderRoot??=this.createRenderRoot(),this.enableUpdating(!0),this._$EO?.forEach(t=>t.hostConnected?.())}enableUpdating(t){}disconnectedCallback(){this._$EO?.forEach(t=>t.hostDisconnected?.())}attributeChangedCallback(t,e,i){this._$AK(t,i)}_$ET(t,e){let i=this.constructor.elementProperties.get(t),r=this.constructor._$Eu(t,i);if(r!==void 0&&i.reflect===!0){let o=(i.converter?.toAttribute!==void 0?i.converter:ft).toAttribute(e,i.type);this._$Em=t,o==null?this.removeAttribute(r):this.setAttribute(r,o),this._$Em=null}}_$AK(t,e){let i=this.constructor,r=i._$Eh.get(t);if(r!==void 0&&this._$Em!==r){let o=i.getPropertyOptions(r),n=typeof o.converter=="function"?{fromAttribute:o.converter}:o.converter?.fromAttribute!==void 0?o.converter:ft;this._$Em=r;let l=n.fromAttribute(e,o.type);this[r]=l??this._$Ej?.get(r)??l,this._$Em=null}}requestUpdate(t,e,i,r=!1,o){if(t!==void 0){let n=this.constructor;if(r===!1&&(o=this[t]),i??=n.getPropertyOptions(t),!((i.hasChanged??ye)(o,e)||i.useDefault&&i.reflect&&o===this._$Ej?.get(t)&&!this.hasAttribute(n._$Eu(t,i))))return;this.C(t,e,i)}this.isUpdatePending===!1&&(this._$ES=this._$EP())}C(t,e,{useDefault:i,reflect:r,wrapped:o},n){i&&!(this._$Ej??=new Map).has(t)&&(this._$Ej.set(t,n??e??this[t]),o!==!0||n!==void 0)||(this._$AL.has(t)||(this.hasUpdated||i||(e=void 0),this._$AL.set(t,e)),r===!0&&this._$Em!==t&&(this._$Eq??=new Set).add(t))}async _$EP(){this.isUpdatePending=!0;try{await this._$ES}catch(e){Promise.reject(e)}let t=this.scheduleUpdate();return t!=null&&await t,!this.isUpdatePending}scheduleUpdate(){return this.performUpdate()}performUpdate(){if(!this.isUpdatePending)return;if(!this.hasUpdated){if(this.renderRoot??=this.createRenderRoot(),this._$Ep){for(let[r,o]of this._$Ep)this[r]=o;this._$Ep=void 0}let i=this.constructor.elementProperties;if(i.size>0)for(let[r,o]of i){let{wrapped:n}=o,l=this[r];n!==!0||this._$AL.has(r)||l===void 0||this.C(r,void 0,o,l)}}let t=!1,e=this._$AL;try{t=this.shouldUpdate(e),t?(this.willUpdate(e),this._$EO?.forEach(i=>i.hostUpdate?.()),this.update(e)):this._$EM()}catch(i){throw t=!1,this._$EM(),i}t&&this._$AE(e)}willUpdate(t){}_$AE(t){this._$EO?.forEach(e=>e.hostUpdated?.()),this.hasUpdated||(this.hasUpdated=!0,this.firstUpdated(t)),this.updated(t)}_$EM(){this._$AL=new Map,this.isUpdatePending=!1}get updateComplete(){return this.getUpdateComplete()}getUpdateComplete(){return this._$ES}shouldUpdate(t){return!0}update(t){this._$Eq&&=this._$Eq.forEach(e=>this._$ET(e,this[e])),this._$EM()}updated(t){}firstUpdated(t){}};T.elementStyles=[],T.shadowRootOptions={mode:"open"},T[se("elementProperties")]=new Map,T[se("finalized")]=new Map,Ws?.({ReactiveElement:T}),(be.reactiveElementVersions??=[]).push("2.1.2");var Ct=globalThis,pi=s=>s,xe=Ct.trustedTypes,_i=xe?xe.createPolicy("lit-html",{createHTML:s=>s}):void 0,yi="$lit$",k=`lit$${Math.random().toFixed(9).slice(2)}$`,xi="?"+k,Gs=`<${xi}>`,z=document,oe=()=>z.createComment(""),ne=s=>s===null||typeof s!="object"&&typeof s!="function",wt=Array.isArray,Ks=s=>wt(s)||typeof s?.[Symbol.iterator]=="function",mt=`[ 	
\f\r]`,re=/<(?:(!--|\/[^a-zA-Z])|(\/?[a-zA-Z][^>\s]*)|(\/?$))/g,fi=/-->/g,mi=/>/g,D=RegExp(`>|${mt}(?:([^\\s"'>=/]+)(${mt}*=${mt}*(?:[^ 	
\f\r"'\`<>=]|("|')|))|$)`,"g"),vi=/'/g,gi=/"/g,Ci=/^(?:script|style|textarea|title)$/i,It=s=>(t,...e)=>({_$litType$:s,strings:t,values:e}),f=It(1),Kr=It(2),Zr=It(3),B=Symbol.for("lit-noChange"),v=Symbol.for("lit-nothing"),bi=new WeakMap,N=z.createTreeWalker(z,129);function wi(s,t){if(!wt(s)||!s.hasOwnProperty("raw"))throw Error("invalid template strings array");return _i!==void 0?_i.createHTML(t):t}var Zs=(s,t)=>{let e=s.length-1,i=[],r,o=t===2?"<svg>":t===3?"<math>":"",n=re;for(let l=0;l<e;l++){let a=s[l],d,h,c=-1,y=0;for(;y<a.length&&(n.lastIndex=y,h=n.exec(a),h!==null);)y=n.lastIndex,n===re?h[1]==="!--"?n=fi:h[1]!==void 0?n=mi:h[2]!==void 0?(Ci.test(h[2])&&(r=RegExp("</"+h[2],"g")),n=D):h[3]!==void 0&&(n=D):n===D?h[0]===">"?(n=r??re,c=-1):h[1]===void 0?c=-2:(c=n.lastIndex-h[2].length,d=h[1],n=h[3]===void 0?D:h[3]==='"'?gi:vi):n===gi||n===vi?n=D:n===fi||n===mi?n=re:(n=D,r=void 0);let S=n===D&&s[l+1].startsWith("/>")?" ":"";o+=n===re?a+Gs:c>=0?(i.push(d),a.slice(0,c)+yi+a.slice(c)+k+S):a+k+(c===-2?l:S)}return[wi(s,o+(s[e]||"<?>")+(t===2?"</svg>":t===3?"</math>":"")),i]},ae=class s{constructor({strings:t,_$litType$:e},i){let r;this.parts=[];let o=0,n=0,l=t.length-1,a=this.parts,[d,h]=Zs(t,e);if(this.el=s.createElement(d,i),N.currentNode=this.el.content,e===2||e===3){let c=this.el.content.firstChild;c.replaceWith(...c.childNodes)}for(;(r=N.nextNode())!==null&&a.length<l;){if(r.nodeType===1){if(r.hasAttributes())for(let c of r.getAttributeNames())if(c.endsWith(yi)){let y=h[n++],S=r.getAttribute(c).split(k),H=/([.?@])?(.*)/.exec(y);a.push({type:1,index:o,name:H[2],strings:S,ctor:H[1]==="."?gt:H[1]==="?"?bt:H[1]==="@"?yt:q}),r.removeAttribute(c)}else c.startsWith(k)&&(a.push({type:6,index:o}),r.removeAttribute(c));if(Ci.test(r.tagName)){let c=r.textContent.split(k),y=c.length-1;if(y>0){r.textContent=xe?xe.emptyScript:"";for(let S=0;S<y;S++)r.append(c[S],oe()),N.nextNode(),a.push({type:2,index:++o});r.append(c[y],oe())}}}else if(r.nodeType===8)if(r.data===xi)a.push({type:2,index:o});else{let c=-1;for(;(c=r.data.indexOf(k,c+1))!==-1;)a.push({type:7,index:o}),c+=k.length-1}o++}}static createElement(t,e){let i=z.createElement("template");return i.innerHTML=t,i}};function U(s,t,e=s,i){if(t===B)return t;let r=i!==void 0?e._$Co?.[i]:e._$Cl,o=ne(t)?void 0:t._$litDirective$;return r?.constructor!==o&&(r?._$AO?.(!1),o===void 0?r=void 0:(r=new o(s),r._$AT(s,e,i)),i!==void 0?(e._$Co??=[])[i]=r:e._$Cl=r),r!==void 0&&(t=U(s,r._$AS(s,t.values),r,i)),t}var vt=class{constructor(t,e){this._$AV=[],this._$AN=void 0,this._$AD=t,this._$AM=e}get parentNode(){return this._$AM.parentNode}get _$AU(){return this._$AM._$AU}u(t){let{el:{content:e},parts:i}=this._$AD,r=(t?.creationScope??z).importNode(e,!0);N.currentNode=r;let o=N.nextNode(),n=0,l=0,a=i[0];for(;a!==void 0;){if(n===a.index){let d;a.type===2?d=new le(o,o.nextSibling,this,t):a.type===1?d=new a.ctor(o,a.name,a.strings,this,t):a.type===6&&(d=new xt(o,this,t)),this._$AV.push(d),a=i[++l]}n!==a?.index&&(o=N.nextNode(),n++)}return N.currentNode=z,r}p(t){let e=0;for(let i of this._$AV)i!==void 0&&(i.strings!==void 0?(i._$AI(t,i,e),e+=i.strings.length-2):i._$AI(t[e])),e++}},le=class s{get _$AU(){return this._$AM?._$AU??this._$Cv}constructor(t,e,i,r){this.type=2,this._$AH=v,this._$AN=void 0,this._$AA=t,this._$AB=e,this._$AM=i,this.options=r,this._$Cv=r?.isConnected??!0}get parentNode(){let t=this._$AA.parentNode,e=this._$AM;return e!==void 0&&t?.nodeType===11&&(t=e.parentNode),t}get startNode(){return this._$AA}get endNode(){return this._$AB}_$AI(t,e=this){t=U(this,t,e),ne(t)?t===v||t==null||t===""?(this._$AH!==v&&this._$AR(),this._$AH=v):t!==this._$AH&&t!==B&&this._(t):t._$litType$!==void 0?this.$(t):t.nodeType!==void 0?this.T(t):Ks(t)?this.k(t):this._(t)}O(t){return this._$AA.parentNode.insertBefore(t,this._$AB)}T(t){this._$AH!==t&&(this._$AR(),this._$AH=this.O(t))}_(t){this._$AH!==v&&ne(this._$AH)?this._$AA.nextSibling.data=t:this.T(z.createTextNode(t)),this._$AH=t}$(t){let{values:e,_$litType$:i}=t,r=typeof i=="number"?this._$AC(t):(i.el===void 0&&(i.el=ae.createElement(wi(i.h,i.h[0]),this.options)),i);if(this._$AH?._$AD===r)this._$AH.p(e);else{let o=new vt(r,this),n=o.u(this.options);o.p(e),this.T(n),this._$AH=o}}_$AC(t){let e=bi.get(t.strings);return e===void 0&&bi.set(t.strings,e=new ae(t)),e}k(t){wt(this._$AH)||(this._$AH=[],this._$AR());let e=this._$AH,i,r=0;for(let o of t)r===e.length?e.push(i=new s(this.O(oe()),this.O(oe()),this,this.options)):i=e[r],i._$AI(o),r++;r<e.length&&(this._$AR(i&&i._$AB.nextSibling,r),e.length=r)}_$AR(t=this._$AA.nextSibling,e){for(this._$AP?.(!1,!0,e);t!==this._$AB;){let i=pi(t).nextSibling;pi(t).remove(),t=i}}setConnected(t){this._$AM===void 0&&(this._$Cv=t,this._$AP?.(t))}},q=class{get tagName(){return this.element.tagName}get _$AU(){return this._$AM._$AU}constructor(t,e,i,r,o){this.type=1,this._$AH=v,this._$AN=void 0,this.element=t,this.name=e,this._$AM=r,this.options=o,i.length>2||i[0]!==""||i[1]!==""?(this._$AH=Array(i.length-1).fill(new String),this.strings=i):this._$AH=v}_$AI(t,e=this,i,r){let o=this.strings,n=!1;if(o===void 0)t=U(this,t,e,0),n=!ne(t)||t!==this._$AH&&t!==B,n&&(this._$AH=t);else{let l=t,a,d;for(t=o[0],a=0;a<o.length-1;a++)d=U(this,l[i+a],e,a),d===B&&(d=this._$AH[a]),n||=!ne(d)||d!==this._$AH[a],d===v?t=v:t!==v&&(t+=(d??"")+o[a+1]),this._$AH[a]=d}n&&!r&&this.j(t)}j(t){t===v?this.element.removeAttribute(this.name):this.element.setAttribute(this.name,t??"")}},gt=class extends q{constructor(){super(...arguments),this.type=3}j(t){this.element[this.name]=t===v?void 0:t}},bt=class extends q{constructor(){super(...arguments),this.type=4}j(t){this.element.toggleAttribute(this.name,!!t&&t!==v)}},yt=class extends q{constructor(t,e,i,r,o){super(t,e,i,r,o),this.type=5}_$AI(t,e=this){if((t=U(this,t,e,0)??v)===B)return;let i=this._$AH,r=t===v&&i!==v||t.capture!==i.capture||t.once!==i.once||t.passive!==i.passive,o=t!==v&&(i===v||r);r&&this.element.removeEventListener(this.name,this,i),o&&this.element.addEventListener(this.name,this,t),this._$AH=t}handleEvent(t){typeof this._$AH=="function"?this._$AH.call(this.options?.host??this.element,t):this._$AH.handleEvent(t)}},xt=class{constructor(t,e,i){this.element=t,this.type=6,this._$AN=void 0,this._$AM=e,this.options=i}get _$AU(){return this._$AM._$AU}_$AI(t){U(this,t)}};var Ys=Ct.litHtmlPolyfillSupport;Ys?.(ae,le),(Ct.litHtmlVersions??=[]).push("3.3.3");var Ii=(s,t,e)=>{let i=e?.renderBefore??t,r=i._$litPart$;if(r===void 0){let o=e?.renderBefore??null;i._$litPart$=r=new le(t.insertBefore(oe(),o),o,void 0,e??{})}return r._$AI(s),r};var St=globalThis,p=class extends T{constructor(){super(...arguments),this.renderOptions={host:this},this._$Do=void 0}createRenderRoot(){let t=super.createRenderRoot();return this.renderOptions.renderBefore??=t.firstChild,t}update(t){let e=this.render();this.hasUpdated||(this.renderOptions.isConnected=this.isConnected),super.update(t),this._$Do=Ii(e,this.renderRoot,this.renderOptions)}connectedCallback(){super.connectedCallback(),this._$Do?.setConnected(!0)}disconnectedCallback(){super.disconnectedCallback(),this._$Do?.setConnected(!1)}render(){return B}};p._$litElement$=!0,p.finalized=!0,St.litElementHydrateSupport?.({LitElement:p});var Qs=St.litElementPolyfillSupport;Qs?.({LitElement:p});(St.litElementVersions??=[]).push("4.2.2");window.Vaadin||={};window.Vaadin.featureFlags||={};function Xs(s){return s.replace(/-[a-z]/gu,t=>t[1].toUpperCase())}var O={};function m(s,t="25.2.8"){if(Object.defineProperty(s,"version",{get(){return t}}),s.experimental){let i=typeof s.experimental=="string"?s.experimental:`${Xs(s.is.split("-").slice(1).join("-"))}Component`;if(!window.Vaadin.featureFlags[i]&&!O[i]){O[i]=new Set,O[i].add(s),Object.defineProperty(window.Vaadin.featureFlags,i,{get(){return O[i].size===0},set(r){r&&O[i].size>0&&(O[i].forEach(o=>{customElements.define(o.is,o)}),O[i].clear())}});return}else if(O[i]){O[i].add(s);return}}let e=customElements.get(s.is);if(!e)customElements.define(s.is,s);else{let i=e.version;i&&s.version&&i===s.version?console.warn(`The component ${s.is} has been loaded twice`):console.error(`Tried to define ${s.is} version ${s.version} when version ${e.version} is already in use. Something will probably break.`)}}var M=[];function Et(s,t,e=s.getAttribute("dir")){t?s.setAttribute("dir",t):e!=null&&s.removeAttribute("dir")}function At(){return document.documentElement.getAttribute("dir")}function Js(){let s=At();M.forEach(t=>{Et(t,s)})}var er=new MutationObserver(Js);er.observe(document.documentElement,{attributes:!0,attributeFilter:["dir"]});var E=s=>class extends s{static get properties(){return{dir:{type:String,value:"",reflectToAttribute:!0,converter:{fromAttribute:e=>e||"",toAttribute:e=>e===""?null:e}}}}get __isRTL(){return this.getAttribute("dir")==="rtl"}connectedCallback(){super.connectedCallback(),(!this.hasAttribute("dir")||this.__restoreSubscription)&&(this.__subscribe(),Et(this,At(),null))}attributeChangedCallback(e,i,r){if(super.attributeChangedCallback(e,i,r),e!=="dir")return;let o=At(),n=r===o&&M.indexOf(this)===-1,l=!r&&i&&M.indexOf(this)===-1;n||l?(this.__subscribe(),Et(this,o,r)):r!==o&&i===o&&this.__unsubscribe()}disconnectedCallback(){super.disconnectedCallback(),this.__restoreSubscription=M.includes(this),this.__unsubscribe()}_valueToNodeAttribute(e,i,r){r==="dir"&&i===""&&!e.hasAttribute("dir")||super._valueToNodeAttribute(e,i,r)}_attributeToProperty(e,i,r){e==="dir"&&!i?this.dir="":super._attributeToProperty(e,i,r)}__subscribe(){M.includes(this)||M.push(this)}__unsubscribe(){M.includes(this)&&M.splice(M.indexOf(this),1)}};var Si=new WeakMap;function tr(s,t){let e=t;for(;e;){if(Si.get(e)===s)return!0;e=Object.getPrototypeOf(e)}return!1}function _(s){return t=>{if(tr(s,t))return t;let e=s(t);return Si.set(e,s),e}}function V(s,t){return s.split(".").reduce((e,i)=>e?e[i]:void 0,t)}function Ei(s,t,e){let i=s.split("."),r=i.pop(),o=i.reduce((n,l)=>n[l],e);o[r]=t}var Pt={},ir=/([A-Z])/gu;function Ai(s){return Pt[s]||(Pt[s]=s.replace(ir,"-$1").toLowerCase()),Pt[s]}function Pi(s){return s[0].toUpperCase()+s.substring(1)}function Tt(s){let[t,e]=s.split("("),i=e.replace(")","").split(",").map(r=>r.trim());return{method:t,observerProps:i}}function Ot(s,t){return Object.prototype.hasOwnProperty.call(s,t)||(s[t]=new Map(s[t])),s[t]}var sr=s=>{class t extends s{static enabledWarnings=[];static createProperty(i,r){[String,Boolean,Number,Array].includes(r)&&(r={type:r}),r?.reflectToAttribute&&(r.reflect=!0),super.createProperty(i,r)}static getOrCreateMap(i){return Ot(this,i)}static finalize(){if(window.litIssuedWarnings&&(window.litIssuedWarnings.add("no-override-create-property"),window.litIssuedWarnings.add("no-override-get-property-descriptor")),super.finalize(),Array.isArray(this.observers)){let i=this.getOrCreateMap("__complexObservers");this.observers.forEach(r=>{let{method:o,observerProps:n}=Tt(r);i.set(o,n)})}}static addCheckedInitializer(i){super.addInitializer(r=>{r instanceof this&&i(r)})}static getPropertyDescriptor(i,r,o){let n=super.getPropertyDescriptor(i,r,o),l=n;if(this.getOrCreateMap("__propKeys").set(i,r),o.sync&&(l={get:n.get,set(a){let d=this[i];ye(a,d)&&(this[r]=a,this.requestUpdate(i,d,o),this.hasUpdated&&this.performUpdate())},configurable:!0,enumerable:!0}),o.readOnly){let a=l.set;this.addCheckedInitializer(d=>{d[`_set${Pi(i)}`]=function(h){a.call(d,h)}}),l={get:l.get,set(){},configurable:!0,enumerable:!0}}if("value"in o&&this.addCheckedInitializer(a=>{let d=typeof o.value=="function"?o.value.call(a):o.value;o.readOnly?a[`_set${Pi(i)}`](d):a[i]=d}),o.observer){let a=o.observer;this.getOrCreateMap("__observers").set(i,a),this.addCheckedInitializer(d=>{d[a]||console.warn(`observer method ${a} not defined`)})}if(o.notify){if(!this.__notifyProps)this.__notifyProps=new Set;else if(!this.hasOwnProperty("__notifyProps")){let a=this.__notifyProps;this.__notifyProps=new Set(a)}this.__notifyProps.add(i)}if(o.computed){let a=`__assignComputed${i}`,d=Tt(o.computed);this.prototype[a]=function(...h){this[i]=this[d.method](...h)},this.getOrCreateMap("__computedObservers").set(a,d.observerProps)}return o.attribute||(o.attribute=Ai(i)),l}static get polylitConfig(){return{asyncFirstRender:!1}}connectedCallback(){super.connectedCallback();let{polylitConfig:i}=this.constructor;!this.hasUpdated&&!i.asyncFirstRender&&this.performUpdate()}firstUpdated(){super.firstUpdated(),this.$||(this.$={}),this.renderRoot.querySelectorAll("[id]").forEach(i=>{this.$[i.id]=i})}ready(){}willUpdate(i){this.constructor.__computedObservers&&this.__runComplexObservers(i,this.constructor.__computedObservers)}updated(i){let r=this.__isReadyInvoked;this.__isReadyInvoked=!0,this.constructor.__observers&&this.__runObservers(i,this.constructor.__observers),this.constructor.__complexObservers&&this.__runComplexObservers(i,this.constructor.__complexObservers),this.__dynamicPropertyObservers&&this.__runDynamicObservers(i,this.__dynamicPropertyObservers),this.__dynamicMethodObservers&&this.__runComplexObservers(i,this.__dynamicMethodObservers),this.constructor.__notifyProps&&this.__runNotifyProps(i,this.constructor.__notifyProps),r||this.ready()}setProperties(i){Object.entries(i).forEach(([r,o])=>{let n=this.constructor.__propKeys.get(r),l=this[n];this[n]=o,this.requestUpdate(r,l)}),this.hasUpdated&&this.performUpdate()}_createMethodObserver(i){let r=Ot(this,"__dynamicMethodObservers"),{method:o,observerProps:n}=Tt(i);r.set(o,n)}_createPropertyObserver(i,r){Ot(this,"__dynamicPropertyObservers").set(r,i)}__runComplexObservers(i,r){r.forEach((o,n)=>{o.some(l=>i.has(l))&&(this[n]?this[n](...o.map(l=>this[l])):console.warn(`observer method ${n} not defined`))})}__runDynamicObservers(i,r){r.forEach((o,n)=>{i.has(o)&&this[n]&&this[n](this[o],i.get(o))})}__runObservers(i,r){i.forEach((o,n)=>{let l=r.get(n);l!==void 0&&this[l]&&this[l](this[n],o)})}__runNotifyProps(i,r){i.forEach((o,n)=>{r.has(n)&&this.dispatchEvent(new CustomEvent(`${Ai(n)}-changed`,{detail:{value:this[n]}}))})}_get(i,r){return V(i,r)}_set(i,r,o){Ei(i,r,o)}}return t},g=_(sr);function Ce(s){try{CSS.registerProperty(s)}catch(t){if(t instanceof DOMException&&t.name==="InvalidModificationError")console.warn(`The CSS property ${s.name} has already been registered.`);else throw t}}var Ti=(s,...t)=>{let e=document.createElement("style");e.id=s,e.textContent=t.map(i=>i.toString()).join(`
`),document.head.insertAdjacentElement("afterbegin",e)};var we=class s extends EventTarget{#t;#s=new Set;#e;#i=!1;constructor(t){super(),this.#t=t,this.#e=new CSSStyleSheet}#o(t){let{propertyName:e}=t;this.#s.has(e)&&this.dispatchEvent(new CustomEvent("property-changed",{detail:{propertyName:e}}))}observe(t){this.connect(),!this.#s.has(t)&&(this.#s.add(t),this.#e.replaceSync(`
      :root::before, :host::before {
        content: '' !important;
        position: absolute !important;
        top: -9999px !important;
        left: -9999px !important;
        visibility: hidden !important;
        transition: 1ms allow-discrete step-end !important;
        transition-property: ${[...this.#s].join(", ")} !important;
      }
    `))}connect(){this.#i||(this.#t.adoptedStyleSheets.unshift(this.#e),this.#r.addEventListener("transitionstart",t=>this.#o(t)),this.#r.addEventListener("transitionend",t=>this.#o(t)),this.#i=!0)}disconnect(){this.#s.clear(),this.#t.adoptedStyleSheets=this.#t.adoptedStyleSheets.filter(t=>t!==this.#e),this.#r.removeEventListener("transitionstart",this.#o),this.#r.removeEventListener("transitionend",this.#o),this.#i=!1}get#r(){return this.#t.documentElement??this.#t.host}static for(t){return t.__cssPropertyObserver||=new s(t),t.__cssPropertyObserver}};function rr(s){let{baseStyles:t,themeStyles:e,elementStyles:i,lumoInjector:r}=s.constructor,o=s.__lumoStyleSheet;return o?[...r.includeBaseStyles?t??i:[],o,...e??[]]:i}function Mt(s){ge(s.shadowRoot,rr(s))}function kt(s,t){s.__lumoStyleSheet=t,Mt(s)}function Ie(s){s.__lumoStyleSheet=void 0,Mt(s)}var Oi=new Set;function Vt(s){Oi.has(s)||(Oi.add(s),console.warn(s))}var Mi=new WeakMap;function ki(s){try{return s.media.mediaText}catch{return Vt('[LumoInjector] Browser denied to access property "mediaText" for some CSS rules, so they were skipped.'),""}}function or(s){try{return s.cssRules}catch{return Vt('[LumoInjector] Browser denied to access property "cssRules" for some CSS stylesheets, so they were skipped.'),[]}}function Vi(s,t={tags:new Map,modules:new Map}){for(let e of or(s)){if(e instanceof CSSImportRule){let i=ki(e);i.startsWith("lumo_")?t.modules.set(i,[...e.styleSheet.cssRules]):Vi(e.styleSheet,t);continue}if(e instanceof CSSMediaRule){let i=ki(e);i.startsWith("lumo_")&&t.modules.set(i,[...e.cssRules]);continue}if(e instanceof CSSStyleRule&&e.cssText.includes("-inject")){for(let i of e.style){let r=i.match(/^--_lumo-(.*)-inject-modules$/u)?.[1];if(!r)continue;let o=e.style.getPropertyValue(i);t.tags.set(r,o.split(",").map(n=>n.trim().replace(/'|"/gu,"")))}continue}}return t}function Li(s){let t=new Map,e=new Map;for(let i of s){let r=Mi.get(i);r||(r=Vi(i),Mi.set(i,r)),t=new Map([...t,...r.tags]),e=new Map([...e,...r.modules])}return{tags:t,modules:e}}function Lt(s){return`--_lumo-${s.is}-inject`}var Se=class{#t;#s;#e=new Map;#i=new Map;constructor(t=document){this.#t=t,this.handlePropertyChange=this.handlePropertyChange.bind(this),this.#s=we.for(t),this.#s.addEventListener("property-changed",this.handlePropertyChange)}disconnect(){this.#s.removeEventListener("property-changed",this.handlePropertyChange),this.#e.clear(),this.#i.values().forEach(t=>t.forEach(Ie))}forceUpdate(){for(let t of this.#e.keys())this.#r(t)}componentConnected(t){let{lumoInjector:e}=t.constructor,{is:i}=e;this.#i.set(i,this.#i.get(i)??new Set),this.#i.get(i).add(t);let r=this.#e.get(i);if(r){r.cssRules.length>0&&kt(t,r);return}this.#o(i);let o=Lt(e);this.#s.observe(o)}componentDisconnected(t){let{is:e}=t.constructor.lumoInjector;this.#i.get(e)?.delete(t),Ie(t)}handlePropertyChange(t){let{propertyName:e}=t.detail,i=e.match(/^--_lumo-(.*)-inject$/u)?.[1];i&&this.#r(i)}#o(t){this.#e.set(t,new CSSStyleSheet),this.#r(t)}#r(t){let{tags:e,modules:i}=Li(this.#n),r=(e.get(t)??[]).flatMap(n=>i.get(n)??[]).map(n=>n.cssText).join(`
`),o=this.#e.get(t);o.replaceSync(r),this.#i.get(t)?.forEach(n=>{r?kt(n,o):Ie(n)})}get#n(){let t=new Set;for(let e of[this.#t,document])t=t.union(new Set(e.styleSheets)),t=t.union(new Set(e.adoptedStyleSheets));return[...t]}};var $i=new Set;function Di(s){let t=s.getRootNode();return t.host&&t.host.constructor.version?Di(t.host):t}var x=s=>class extends s{static finalize(){super.finalize();let e=Lt(this.lumoInjector);this.is&&!$i.has(e)&&($i.add(e),Ce({name:e,syntax:"<number>",inherits:!0,initialValue:"0"}))}static get lumoInjector(){return{is:this.is,includeBaseStyles:!1}}connectedCallback(){super.connectedCallback();let e=Di(this);e.__lumoInjectorDisabled||this.isConnected&&(e.__lumoInjector||=new Se(e),this.__lumoInjector=e.__lumoInjector,this.__lumoInjector.componentConnected(this))}disconnectedCallback(){super.disconnectedCallback(),this.__lumoInjector&&(this.__lumoInjector.componentDisconnected(this),this.__lumoInjector=void 0)}};var Ni=s=>class extends s{static get properties(){return{_theme:{type:String,readOnly:!0}}}static get observedAttributes(){return[...super.observedAttributes,"theme"]}attributeChangedCallback(e,i,r){super.attributeChangedCallback(e,i,r),e==="theme"&&this._set_theme(r)}};var $t=[],nr=new Set,ar=new Set;function lr(s){return s&&Object.prototype.hasOwnProperty.call(s,"__themes")}function dr(s,t){return(s||"").split(" ").some(e=>new RegExp(`^${e.split("*").join(".*")}$`,"u").test(t))}function hr(s){return s.map(t=>t.cssText).join(`
`)}var cr="vaadin-themable-mixin-style";function ur(s,t){let e=document.createElement("style");e.id=cr,e.textContent=hr(s),t.content.appendChild(e)}function pr(s=""){let t=0;return s.startsWith("lumo-")||s.startsWith("material-")?t=1:s.startsWith("vaadin-")&&(t=2),t}function zi(s){let t=[];return s.include&&[].concat(s.include).forEach(e=>{let i=$t.find(r=>r.moduleId===e);i?t.push(...zi(i),...i.styles):console.warn(`Included moduleId ${e} not found in style registry`)},s.styles),t}function _r(s){let t=`${s}-default-theme`,e=$t.filter(i=>i.moduleId!==t&&dr(i.themeFor,s)).map(i=>({...i,styles:[...zi(i),...i.styles],includePriority:pr(i.moduleId)})).sort((i,r)=>r.includePriority-i.includePriority);return e.length>0?e:$t.filter(i=>i.moduleId===t)}var C=s=>class extends Ni(s){constructor(){super(),nr.add(new WeakRef(this))}static finalize(){if(super.finalize(),this.is&&ar.add(this.is),this.elementStyles)return;let e=this.prototype._template;!e||lr(this)||ur(this.getStylesForThis(),e)}static finalizeStyles(e){return this.baseStyles=e?[e].flat(1/0):[],this.themeStyles=this.getStylesForThis(),[...this.baseStyles,...this.themeStyles]}static getStylesForThis(){let e=s.__themes||[],i=Object.getPrototypeOf(this.prototype),r=(i?i.constructor.__themes:[])||[];this.__themes=[...e,...r,..._r(this.is)];let o=this.__themes.flatMap(n=>n.styles);return o.filter((n,l)=>l===o.lastIndexOf(n))}};["--vaadin-text-color","--vaadin-text-color-disabled","--vaadin-text-color-secondary","--vaadin-border-color","--vaadin-border-color-secondary","--vaadin-background-color"].forEach(s=>{Ce({name:s,syntax:"<color>",inherits:!0,initialValue:"transparent"})});Ti("vaadin-base",u`
    @layer vaadin.base {
      html {
        /* Background color */
        --vaadin-background-color: light-dark(#fff, #222);

        /* Container colors */
        --vaadin-background-container: color-mix(in oklab, var(--vaadin-text-color) 5%, var(--vaadin-background-color));
        --vaadin-background-container-strong: color-mix(
          in oklab,
          var(--vaadin-text-color) 10%,
          var(--vaadin-background-color)
        );

        /* Border colors */
        --vaadin-border-color-secondary: color-mix(in oklab, var(--vaadin-text-color) 24%, transparent);
        --vaadin-border-color: color-mix(in oklab, var(--vaadin-text-color) 48%, transparent); /* Above 3:1 contrast */

        /* Text colors */
        /* Above 3:1 contrast */
        --vaadin-text-color-disabled: color-mix(in oklab, var(--vaadin-text-color) 48%, transparent);
        /* Above 4.5:1 contrast */
        --vaadin-text-color-secondary: color-mix(in oklab, var(--vaadin-text-color) 68%, transparent);
        /* Above 7:1 contrast */
        --vaadin-text-color: light-dark(#1f1f1f, white);

        /* Padding */
        --vaadin-padding-xs: 6px;
        --vaadin-padding-s: 8px;
        --vaadin-padding-m: 12px;
        --vaadin-padding-l: 16px;
        --vaadin-padding-xl: 24px;
        --vaadin-padding-block-container: var(--vaadin-padding-xs);
        --vaadin-padding-inline-container: var(--vaadin-padding-s);

        /* Gap/spacing */
        --vaadin-gap-xs: 6px;
        --vaadin-gap-s: 8px;
        --vaadin-gap-m: 12px;
        --vaadin-gap-l: 16px;
        --vaadin-gap-xl: 24px;

        /* Border radius */
        --vaadin-radius-s: 3px;
        --vaadin-radius-m: 6px;
        --vaadin-radius-l: 12px;

        /* Focus outline */
        --vaadin-focus-ring-width: 2px;
        --vaadin-focus-ring-color: var(--vaadin-text-color);

        /* Icons, used as mask-image */
        --_vaadin-icon-arrow-up: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m5 12 7-7 7 7"/><path d="M12 19V5"/></svg>');
        --_vaadin-icon-calendar: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M8 2v4"/><path d="M16 2v4"/><rect width="18" height="18" x="3" y="4" rx="2"/><path d="M3 10h18"/></svg>');
        --_vaadin-icon-checkmark: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m4.5 12.75 6 6 9-13.5" /></svg>');
        --_vaadin-icon-chevron-down: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m6 9 6 6 6-6"/></svg>');
        --_vaadin-icon-chevron-right: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6"/></svg>');
        --_vaadin-icon-clock: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 6v6l4 2"/><circle cx="12" cy="12" r="10"/></svg>');
        --_vaadin-icon-cross: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" /></svg>');
        --_vaadin-icon-drag: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"><path d="M11 7c0 .82843-.6716 1.5-1.5 1.5C8.67157 8.5 8 7.82843 8 7s.67157-1.5 1.5-1.5c.8284 0 1.5.67157 1.5 1.5Zm0 5c0 .8284-.6716 1.5-1.5 1.5-.82843 0-1.5-.6716-1.5-1.5s.67157-1.5 1.5-1.5c.8284 0 1.5.6716 1.5 1.5Zm0 5c0 .8284-.6716 1.5-1.5 1.5-.82843 0-1.5-.6716-1.5-1.5s.67157-1.5 1.5-1.5c.8284 0 1.5.6716 1.5 1.5Zm5-10c0 .82843-.6716 1.5-1.5 1.5S13 7.82843 13 7s.6716-1.5 1.5-1.5S16 6.17157 16 7Zm0 5c0 .8284-.6716 1.5-1.5 1.5S13 12.8284 13 12s.6716-1.5 1.5-1.5 1.5.6716 1.5 1.5Zm0 5c0 .8284-.6716 1.5-1.5 1.5S13 17.8284 13 17s.6716-1.5 1.5-1.5 1.5.6716 1.5 1.5Z" fill="currentColor"/></svg>');
        --_vaadin-icon-ellipsis: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/></svg>');
        --_vaadin-icon-eye: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 0 1 0-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178Z" /><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" /></svg>');
        --_vaadin-icon-eye-slash: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 0 0 1.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.451 10.451 0 0 1 12 4.5c4.756 0 8.773 3.162 10.065 7.498a10.522 10.522 0 0 1-4.293 5.774M6.228 6.228 3 3m3.228 3.228 3.65 3.65m7.894 7.894L21 21m-3.228-3.228-3.65-3.65m0 0a3 3 0 1 0-4.243-4.243m4.242 4.242L9.88 9.88" /></svg>');
        --_vaadin-icon-file: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z"/><path d="M14 2v4a2 2 0 0 0 2 2h4"/></svg>');
        --_vaadin-icon-fullscreen: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.75 3.75v4.5m0-4.5h4.5m-4.5 0L9 9M3.75 20.25v-4.5m0 4.5h4.5m-4.5 0L9 15M20.25 3.75h-4.5m4.5 0v4.5m0-4.5L15 9m5.25 11.25h-4.5m4.5 0v-4.5m0 4.5L15 15" /></svg>');
        --_vaadin-icon-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="3" rx="2" ry="2"/><circle cx="9" cy="9" r="2"/><path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21"/></svg>');
        --_vaadin-icon-link: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>');
        --_vaadin-icon-menu: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" /></svg>');
        --_vaadin-icon-minus: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/></svg>');
        --_vaadin-icon-paper-airplane: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M6 12 3.269 3.125A59.769 59.769 0 0 1 21.485 12 59.768 59.768 0 0 1 3.27 20.875L5.999 12Zm0 0h7.5" /></svg>');
        --_vaadin-icon-pen: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z"/><path d="m15 5 4 4"/></svg>');
        --_vaadin-icon-play: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M5.25 5.653c0-.856.917-1.398 1.667-.986l11.54 6.347a1.125 1.125 0 0 1 0 1.972l-11.54 6.347a1.125 1.125 0 0 1-1.667-.986V5.653Z" /></svg>');
        --_vaadin-icon-plus: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="M12 5v14"/></svg>');
        --_vaadin-icon-redo: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 7v6h-6"/><path d="M3 17a9 9 0 0 1 9-9 9 9 0 0 1 6 2.3l3 2.7"/></svg>');
        --_vaadin-icon-refresh: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"><path d="M22 10C22 10 19.995 7.26822 18.3662 5.63824C16.7373 4.00827 14.4864 3 12 3C7.02944 3 3 7.02944 3 12C3 16.9706 7.02944 21 12 21C16.1031 21 19.5649 18.2543 20.6482 14.5M22 10V4M22 10H16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>');
        --_vaadin-icon-resize: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><path fill-rule="evenodd" clip-rule="evenodd" d="M18.5303 7.46967c.2929.29289.2929.76777 0 1.06066L8.53033 18.5304c-.29289.2929-.76777.2929-1.06066 0s-.29289-.7678 0-1.0607L17.4697 7.46967c.2929-.29289.7677-.29289 1.0606 0Zm0 4.50003c.2929.2929.2929.7678 0 1.0607l-5.5 5.5c-.2929.2928-.7677.2928-1.0606 0-.2929-.2929-.2929-.7678 0-1.0607l5.4999-5.5c.2929-.2929.7678-.2929 1.0607 0Zm0 4.5c.2929.2928.2929.7677 0 1.0606l-1 1.0001c-.2929.2928-.7677.2929-1.0606 0-.2929-.2929-.2929-.7678 0-1.0607l1-1c.2929-.2929.7677-.2929 1.0606 0Z" fill="currentColor"/></svg>');
        --_vaadin-icon-slash: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"><rect x="13.7812" y="4.22583" width="1.5" height="16" rx="0.75" transform="rotate(20 13.7812 4.22583)" fill="currentColor"/></svg>');
        --_vaadin-icon-sort: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="8" height="12" viewBox="0 0 8 12" fill="none"><path d="M7.49854 6.99951C7.92795 6.99951 8.15791 7.50528 7.87549 7.82861L4.37646 11.8296C4.17728 12.0571 3.82272 12.0571 3.62354 11.8296L0.125488 7.82861C-0.157248 7.50531 0.0719873 6.99956 0.501465 6.99951H7.49854ZM3.62354 0.17041C3.82275 -0.0573875 4.17725 -0.0573848 4.37646 0.17041L7.87549 4.17041C8.15825 4.49373 7.92806 5.00049 7.49854 5.00049L0.501465 4.99951C0.0719873 4.99946 -0.157248 4.49371 0.125488 4.17041L3.62354 0.17041Z" fill="black"/></svg>');
        --_vaadin-icon-undo: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 7v6h6"/><path d="M21 17a9 9 0 0 0-9-9 9 9 0 0 0-6 2.3L3 13"/></svg>');
        --_vaadin-icon-upload: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3v12"/><path d="m17 8-5-5-5 5"/><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/></svg>');
        --_vaadin-icon-user: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>');
        --_vaadin-icon-warn: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3"/><path d="M12 9v4"/><path d="M12 17h.01"/></svg>');

        /* Cursors for interactive elements */
        --vaadin-clickable-cursor: pointer;
        --vaadin-disabled-cursor: not-allowed;

        /* Use units so that the values can be used in calc() */
        --safe-area-inset-top: env(safe-area-inset-top, 0px);
        --safe-area-inset-right: env(safe-area-inset-right, 0px);
        --safe-area-inset-bottom: env(safe-area-inset-bottom, 0px);
        --safe-area-inset-left: env(safe-area-inset-left, 0px);
        --safe-area-inset-inline-start: var(--safe-area-inset-left);
        --safe-area-inset-inline-end: var(--safe-area-inset-right);

        &:dir(rtl) {
          --safe-area-inset-inline-start: var(--safe-area-inset-right);
          --safe-area-inset-inline-end: var(--safe-area-inset-left);
        }
      }

      @supports not (color: hsl(0 0 0)) {
        html {
          --_vaadin-safari-17-deg: 1deg;
        }
      }

      @media (forced-colors: active) {
        html {
          --vaadin-background-color: Canvas;
          --vaadin-border-color: CanvasText;
          --vaadin-border-color-secondary: CanvasText;
          --vaadin-text-color-disabled: CanvasText;
          --vaadin-text-color-secondary: CanvasText;
          --vaadin-text-color: CanvasText;
          --vaadin-icon-color: CanvasText;
          --vaadin-focus-ring-color: Highlight;
        }
      }
    }
  `);var Bi=u`
  :host {
    display: flex;
    align-items: center;
    --_radius: var(--vaadin-input-field-border-radius, var(--vaadin-radius-m));
    border-radius:
      /* See https://developer.mozilla.org/en-US/docs/Web/CSS/border-radius */
      var(--vaadin-input-field-top-start-radius, var(--_radius))
      var(--vaadin-input-field-top-end-radius, var(--_radius))
      var(--vaadin-input-field-bottom-end-radius, var(--_radius))
      var(--vaadin-input-field-bottom-start-radius, var(--_radius));
    border: var(--vaadin-input-field-border-width, 1px) solid
      var(--vaadin-input-field-border-color, var(--vaadin-border-color));
    box-sizing: border-box;
    cursor: text;
    padding: var(
      --vaadin-input-field-padding,
      var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container)
    );
    gap: var(--vaadin-input-field-gap, var(--vaadin-gap-s));
    background: var(--vaadin-input-field-background, var(--vaadin-background-color));
    color: var(--vaadin-input-field-value-color, var(--vaadin-text-color));
    font-size: var(--vaadin-input-field-value-font-size, inherit);
    line-height: var(--vaadin-input-field-value-line-height, inherit);
    font-weight: var(--vaadin-input-field-value-font-weight, 400);
  }

  :host([dir='rtl']) {
    --_radius: var(--vaadin-input-field-border-radius, var(--vaadin-radius-m));
    border-radius:
      /* Don't use logical props, see https://github.com/vaadin/vaadin-time-picker/issues/145 */
      var(--vaadin-input-field-top-end-radius, var(--_radius))
      var(--vaadin-input-field-top-start-radius, var(--_radius))
      var(--vaadin-input-field-bottom-start-radius, var(--_radius))
      var(--vaadin-input-field-bottom-end-radius, var(--_radius));
  }

  :host([hidden]) {
    display: none !important;
  }

  /* Reset the native input styles */
  ::slotted(:is(input, textarea)) {
    appearance: none;
    align-self: stretch;
    box-sizing: border-box;
    flex: auto;
    white-space: nowrap;
    overflow: hidden;
    width: 100%;
    height: auto;
    outline: none;
    margin: 0;
    padding: 0;
    border: 0;
    border-radius: 0;
    min-width: 0;
    font: inherit;
    font-size: 1em;
    color: inherit;
    background: transparent;
    cursor: inherit;
    text-align: inherit;
    caret-color: var(--vaadin-input-field-value-color);
  }

  ::slotted(*) {
    flex: none;
  }

  slot[name$='fix'] {
    cursor: auto;
  }

  ::slotted(:is(input, textarea))::placeholder {
    /* Use ::slotted(:is(input, textarea):placeholder-shown) to style the placeholder */
    /* because ::slotted(...)::placeholder does not work in Safari. */
    font: inherit;
    color: inherit;
  }

  ::slotted(:is(input, textarea):placeholder-shown) {
    color: var(--vaadin-input-field-placeholder-color, var(--vaadin-text-color-secondary));
  }

  :host(:focus-within) {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-input-field-border-width, 1px) * -1);
  }

  :host([invalid]) {
    --vaadin-input-field-border-color: var(--vaadin-input-field-error-color, var(--vaadin-text-color));
  }

  :host([readonly]) {
    border-style: dashed;
  }

  :host([readonly]:focus-within) {
    outline-style: dashed;
    --vaadin-input-field-border-color: transparent;
  }

  :host([disabled]) {
    --vaadin-input-field-value-color: var(--vaadin-input-field-disabled-text-color, var(--vaadin-text-color-disabled));
    --vaadin-input-field-background: var(
      --vaadin-input-field-disabled-background,
      var(--vaadin-background-container-strong)
    );
    --vaadin-input-field-border-color: transparent;
  }

  :host([theme~='align-start']) slot:not([name])::slotted(*) {
    text-align: start;
  }

  :host([theme~='align-center']) slot:not([name])::slotted(*) {
    text-align: center;
  }

  :host([theme~='align-end']) slot:not([name])::slotted(*) {
    text-align: end;
  }

  :host([theme~='align-left']) slot:not([name])::slotted(*) {
    text-align: left;
  }

  :host([theme~='align-right']) slot:not([name])::slotted(*) {
    text-align: right;
  }

  @media (forced-colors: active) {
    :host {
      --vaadin-input-field-background: Field;
      --vaadin-input-field-value-color: FieldText;
      --vaadin-input-field-placeholder-color: GrayText;
    }

    :host([disabled]) {
      --vaadin-input-field-value-color: GrayText;
      --vaadin-icon-color: GrayText;
    }
  }
`;var de=class extends C(E(g(x(p)))){static get is(){return"vaadin-input-container"}static get styles(){return Bi}static get properties(){return{disabled:{type:Boolean,reflectToAttribute:!0},readonly:{type:Boolean,reflectToAttribute:!0},invalid:{type:Boolean,reflectToAttribute:!0}}}render(){return f`
      <slot name="prefix"></slot>
      <slot></slot>
      <slot name="suffix"></slot>
    `}ready(){super.ready(),this.addEventListener("pointerdown",t=>{t.target===this&&t.preventDefault()}),this.addEventListener("click",t=>{t.target===this&&this.shadowRoot.querySelector("slot:not([name])").assignedNodes({flatten:!0}).forEach(e=>e.focus&&e.focus())})}};m(de);var Ee=u`
  :host {
    align-items: center;
    border-radius: var(--vaadin-item-border-radius, var(--vaadin-radius-m));
    box-sizing: border-box;
    cursor: var(--vaadin-clickable-cursor);
    display: flex;
    column-gap: var(--vaadin-item-gap, var(--vaadin-gap-s));
    height: var(--vaadin-item-height, auto);
    padding: var(--vaadin-item-padding, var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container));
    -webkit-tap-highlight-color: transparent;
  }

  :host([focus-ring]) {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-focus-ring-width) / -1);
  }

  :host([disabled]) {
    cursor: var(--vaadin-disabled-cursor);
    opacity: 0.5;
    pointer-events: var(--_vaadin-item-disabled-pointer-events, none);
  }

  :host([hidden]) {
    display: none !important;
  }

  [part='checkmark'] {
    color: var(--vaadin-item-checkmark-color, inherit);
    display: var(--vaadin-item-checkmark-display, none);
    visibility: hidden;
  }

  [part='checkmark']::before {
    content: '';
    display: block;
    background: currentColor;
    height: var(--vaadin-icon-size, 1lh);
    mask: var(--_vaadin-icon-checkmark) 50% / var(--vaadin-icon-visual-size, 100%) no-repeat;
    width: var(--vaadin-icon-size, 1lh);
  }

  :host([selected]) [part='checkmark'] {
    visibility: visible;
  }

  [part='content'] {
    flex: 1;
    display: flex;
    align-items: center;
    column-gap: inherit;
    justify-content: var(--vaadin-item-text-align, start);
  }

  @media (forced-colors: active) {
    [part='checkmark']::before {
      background: CanvasText;
    }
  }
`;var Ae=u`
  :host([focused]) {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-focus-ring-width) / -1);
  }
`;var Pe=s=>class extends s{static get properties(){return{index:{type:Number},item:{type:Object},label:{type:String},selected:{type:Boolean,value:!1,reflectToAttribute:!0},focused:{type:Boolean,value:!1,reflectToAttribute:!0},renderer:{type:Function}}}static get observers(){return["__rendererOrItemChanged(renderer, index, item, selected, focused)","__updateLabel(label, renderer)"]}static get observedAttributes(){return[...super.observedAttributes,"hidden"]}attributeChangedCallback(e,i,r){e==="hidden"&&r!==null?this.index=void 0:super.attributeChangedCallback(e,i,r)}connectedCallback(){super.connectedCallback(),this._owner=this.parentNode.owner;let e=this._getHostDir();e&&this.setAttribute("dir",e)}_getHostDir(){return this._owner&&this._owner.$.overlay.getAttribute("dir")}requestContentUpdate(){if(!this.renderer||this.hidden)return;let e={index:this.index,item:this.item,focused:this.focused,selected:this.selected};this.renderer(this,this._owner,e)}__rendererOrItemChanged(e,i,r){r===void 0||i===void 0||(this._oldRenderer!==e&&(this.innerHTML="",delete this._$litPart$),e&&(this._oldRenderer=e,this.requestContentUpdate()))}__updateLabel(e,i){i||(this.textContent=e)}};var Dt=class extends Pe(C(E(g(x(p))))){static get is(){return"vaadin-combo-box-item"}static get styles(){return[Ee,Ae]}render(){return f`
      <span part="checkmark" aria-hidden="true"></span>
      <div part="content">
        <slot></slot>
      </div>
    `}};m(Dt);var Te=u`
  :host {
    z-index: 200;
    position: fixed;

    /* Despite of what the names say, <vaadin-overlay> is just a container
          for position/sizing/alignment. The actual overlay is the overlay part. */

    /* Default position constraints. Themes can
          override this to adjust the gap between the overlay and the viewport. */
    inset: max(env(safe-area-inset-top, 0px), var(--vaadin-overlay-viewport-inset, 8px))
      max(env(safe-area-inset-right, 0px), var(--vaadin-overlay-viewport-inset, 8px))
      max(env(safe-area-inset-bottom, 0px), var(--vaadin-overlay-viewport-bottom))
      max(env(safe-area-inset-left, 0px), var(--vaadin-overlay-viewport-inset, 8px));

    /* Override native [popover] user agent styles */
    width: auto;
    height: auto;
    border: none;
    padding: 0;
    background-color: transparent;
    overflow: visible;

    /* Use flexbox alignment for the overlay part. */
    display: flex;
    flex-direction: column; /* makes dropdowns sizing easier */
    /* Align to center by default. */
    align-items: center;
    justify-content: center;

    /* Allow centering when max-width/max-height applies. */
    margin: auto;

    /* The host is not clickable, only the overlay part is. */
    pointer-events: none;

    /* Remove tap highlight on touch devices. */
    -webkit-tap-highlight-color: transparent;

    /* CSS API for host */
    --vaadin-overlay-viewport-bottom: 8px;
  }

  :host([hidden]),
  :host(:not([opened]):not([closing])),
  :host(:not([opened]):not([closing])) [part='overlay'] {
    display: none !important;
  }

  [part='overlay'] {
    color: var(--vaadin-overlay-text-color, var(--vaadin-text-color));
    background: var(--vaadin-overlay-background, var(--vaadin-background-color));
    border: var(--vaadin-overlay-border-width, 1px) solid
      var(--vaadin-overlay-border-color, var(--vaadin-border-color-secondary));
    border-radius: var(--vaadin-overlay-border-radius, var(--vaadin-radius-m));
    box-shadow: var(--vaadin-overlay-shadow, 0 8px 24px -4px rgba(0, 0, 0, 0.3));
    box-sizing: border-box;
    max-width: 100%;
    overflow: auto;
    overscroll-behavior: contain;
    pointer-events: auto;
    -webkit-tap-highlight-color: initial;

    /* CSS reset for font styles */
    font: initial;
    letter-spacing: initial;
    text-align: initial;
    text-decoration: initial;
    text-indent: initial;
    text-transform: initial;
    user-select: text;
    white-space: initial;
    word-spacing: initial;

    /* Inherit font-family */
    font-family: inherit;
  }

  [part='backdrop'] {
    background: var(--vaadin-overlay-backdrop-background, rgba(0, 0, 0, 0.2));
    content: '';
    inset: 0;
    pointer-events: auto;
    position: fixed;
    z-index: -1;
  }

  [part='overlay']:focus-visible {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
  }

  @media (forced-colors: active) {
    [part='overlay'] {
      border: 3px solid !important;
    }
  }
`;var Oe=s=>s.test(navigator.userAgent),Nt=s=>s.test(navigator.platform),fr=s=>s.test(navigator.vendor),Cn=Oe(/Android/u),wn=Oe(/Chrome/u)&&fr(/Google Inc/u),In=Oe(/Firefox/u),mr=Nt(/^iPad/u)||Nt(/^Mac/u)&&navigator.maxTouchPoints>1,vr=Nt(/^iPhone/u),Fi=vr||mr,Ri=Oe(/^((?!chrome|android).)*safari/iu),he=(()=>{try{return document.createEvent("TouchEvent"),!0}catch{return!1}})();var Bt=!1;window.addEventListener("keydown",()=>{Bt=!0},{capture:!0});window.addEventListener("mousedown",()=>{Bt=!1},{capture:!0});function ce(){let s=document.activeElement||document.body;for(;s.shadowRoot&&s.shadowRoot.activeElement;)s=s.shadowRoot.activeElement;return s}function L(){return Bt}function Hi(s){let t=s.style;if(t.visibility==="hidden"||t.display==="none")return!0;let e=window.getComputedStyle(s);return e.visibility==="hidden"||e.display==="none"}function gr(s,t){let e=Math.max(s.tabIndex,0),i=Math.max(t.tabIndex,0);return e===0||i===0?i>e:e>i}function br(s,t){let e=[];for(;s.length>0&&t.length>0;)gr(s[0],t[0])?e.push(t.shift()):e.push(s.shift());return e.concat(s,t)}function zt(s){let t=s.length;if(t<2)return s;let e=Math.ceil(t/2),i=zt(s.slice(0,e)),r=zt(s.slice(e));return br(i,r)}function ji(s){return s.checkVisibility?!s.checkVisibility({visibilityProperty:!0}):s.offsetParent===null&&s.clientWidth===0&&s.clientHeight===0?!0:Hi(s)}function Ft(s){return s.matches('[tabindex="-1"]')?!1:s.matches("input, select, textarea, button, object")?s.matches(":not([disabled])"):s.matches("a[href], area[href], iframe, [tabindex], [contentEditable]")}function W(s){return s.getRootNode().activeElement===s}function yr(s){if(!Ft(s))return-1;let t=s.getAttribute("tabindex")||0;return Number(t)}function Ui(s,t){if(s.nodeType!==Node.ELEMENT_NODE||Hi(s))return!1;let e=s,i=yr(e),r=i>0;i>=0&&t.push(e);let o=[];return e.localName==="slot"?o=e.assignedNodes({flatten:!0}):o=(e.shadowRoot||e).children,[...o].forEach(n=>{r=Ui(n,t)||r}),r}function qi(s){let t=[];return Ui(s,t)?zt(t):t}var Me=class{saveFocus(t){this.focusNode=t||ce()}restoreFocus(t){let e=this.focusNode;if(!e)return;let i={preventScroll:t?t.preventScroll:!1,focusVisible:t?t.focusVisible:!1};ce()===document.body?setTimeout(()=>e.focus(i)):e.focus(i),this.focusNode=null}};var Rt=[];var ke=class{constructor(t){this.host=t,this.__trapNode=null,this.__onKeyDown=this.__onKeyDown.bind(this)}get __focusableElements(){return qi(this.__trapNode)}get __focusedElementIndex(){let t=this.__focusableElements;return t.indexOf(t.filter(W).pop())}hostConnected(){document.addEventListener("keydown",this.__onKeyDown)}hostDisconnected(){document.removeEventListener("keydown",this.__onKeyDown)}trapFocus(t){if(this.__trapNode=t,this.__focusableElements.length===0)throw this.__trapNode=null,new Error("The trap node should have at least one focusable descendant or be focusable itself.");Rt.push(this),this.__focusedElementIndex===-1&&this.__focusableElements[0].focus({focusVisible:L()})}releaseFocus(){this.__trapNode=null,Rt.pop()}__onKeyDown(t){if(this.__trapNode&&this===Array.from(Rt).pop()&&t.key==="Tab"){if(t.defaultPrevented)return;t.preventDefault();let e=t.shiftKey;this.__focusNextElement(e)}}__focusNextElement(t=!1){let e=this.__focusableElements,i=t?-1:1,r=this.__focusedElementIndex,o=(e.length+r+i)%e.length,n=e[o];n.focus({focusVisible:!0}),n.localName==="input"&&n.select()}};var Wi=s=>class extends s{static get properties(){return{focusTrap:{type:Boolean,value:!1},restoreFocusOnClose:{type:Boolean,value:!1},restoreFocusNode:{type:HTMLElement}}}constructor(){super(),this.__focusTrapController=new ke(this),this.__focusRestorationController=new Me}get _contentRoot(){return this}ready(){super.ready(),this.addController(this.__focusTrapController),this.addController(this.__focusRestorationController)}get _focusTrapRoot(){return this.$.overlay}_resetFocus(){if(this.focusTrap&&this.__focusTrapController.releaseFocus(),this.restoreFocusOnClose&&this._shouldRestoreFocus()){let e=L(),i=!e;this.__focusRestorationController.restoreFocus({preventScroll:i,focusVisible:e})}}_saveFocus(){this.restoreFocusOnClose&&this.__focusRestorationController.saveFocus(this.restoreFocusNode)}_trapFocus(){this.focusTrap&&!ji(this._focusTrapRoot)&&this.__focusTrapController.trapFocus(this._focusTrapRoot)}_shouldRestoreFocus(){let e=ce();return e===document.body||this._deepContains(e)}_deepContains(e){if(this._contentRoot.contains(e))return!0;let i=e,r=e.ownerDocument;for(;i&&i!==r&&i!==this._contentRoot;)i=i.parentNode||i.host;return i===this._contentRoot}};var Ve=new Set,Le=()=>[...Ve].filter(s=>!s.hasAttribute("closing")),xr=s=>{let t=Le(),e=t.indexOf(s);return e===-1?[]:t.slice(e+1)},Cr=(s,t)=>s._deepContains(t),Gi=(s,t=e=>!0)=>{let e=Le().filter(t);return s===e.pop()},Ki=s=>class extends s{get _last(){return Gi(this)}get _isAttached(){return Ve.has(this)}bringToFront(){if(Gi(this))return;let e=xr(this),i=e.filter(r=>r._hasOverlayPositionMixin&&Cr(this,r));i.length!==e.length&&[this,...i].forEach(r=>{r.matches(":popover-open")&&(r.hidePopover(),r.showPopover()),r._removeAttachedInstance(),r._appendAttachedInstance()})}_enterModalState(){document.body.style.pointerEvents!=="none"&&(this._previousDocumentPointerEvents=document.body.style.pointerEvents,document.body.style.pointerEvents="none"),Le().forEach(e=>{e!==this&&(e.$.overlay.style.pointerEvents="none")})}_exitModalState(){this._previousDocumentPointerEvents!==void 0&&(document.body.style.pointerEvents=this._previousDocumentPointerEvents,delete this._previousDocumentPointerEvents);let e=Le(),i;for(;(i=e.pop())&&!(i!==this&&(i.$.overlay.style.removeProperty("pointer-events"),!i.modeless)););}_appendAttachedInstance(){Ve.add(this)}_removeAttachedInstance(){this._isAttached&&Ve.delete(this)}};function Zi(s,t){let e=null,i,r=document.documentElement;function o(){i&&clearTimeout(i),e?.disconnect(),e=null}function n(l=!1,a=1){o();let{left:d,top:h,width:c,height:y}=s.getBoundingClientRect();if(l||t(),!c||!y)return;let S=Math.floor(h),H=Math.floor(r.clientWidth-(d+c)),Ls=Math.floor(r.clientHeight-(h+y)),$s=Math.floor(d),Ds={rootMargin:`${-S}px ${-H}px ${-Ls}px ${-$s}px`,threshold:Math.max(0,Math.min(1,a))||1},di=!0;function Ns(zs){let ct=zs[0].intersectionRatio;if(ct!==a){if(!di)return n();ct?n(!1,ct):i=setTimeout(()=>{n(!1,1e-7)},1e3)}di=!1}e=new IntersectionObserver(Ns,Ds),e.observe(s)}return n(!0),o}function w(s,t,e){let i=[s];s.owner&&i.push(s.owner),typeof e=="string"?i.forEach(r=>{r.setAttribute(t,e)}):e?i.forEach(r=>{r.setAttribute(t,"")}):i.forEach(r=>{r.removeAttribute(t)})}var $e=s=>class extends Wi(Ki(s)){static get properties(){return{opened:{type:Boolean,notify:!0,observer:"_openedChanged",reflectToAttribute:!0,sync:!0},owner:{type:Object,sync:!0},model:{type:Object,sync:!0},renderer:{type:Object,sync:!0},modeless:{type:Boolean,value:!1,reflectToAttribute:!0,observer:"_modelessChanged",sync:!0},hidden:{type:Boolean,reflectToAttribute:!0,observer:"_hiddenChanged",sync:!0},withBackdrop:{type:Boolean,value:!1,reflectToAttribute:!0,observer:"_withBackdropChanged",sync:!0}}}static get observers(){return["_rendererOrDataChanged(renderer, owner, model, opened)"]}get _rendererRoot(){return this}constructor(){super(),this._boundMouseDownListener=this._mouseDownListener.bind(this),this._boundMouseUpListener=this._mouseUpListener.bind(this),this._boundOutsideClickListener=this._outsideClickListener.bind(this),this._boundKeydownListener=this._keydownListener.bind(this),Fi&&(this._boundIosResizeListener=()=>this._detectIosNavbar())}firstUpdated(){super.firstUpdated(),this.popover="manual",this.addEventListener("click",()=>{}),this.$.backdrop&&this.$.backdrop.addEventListener("click",()=>{}),this.addEventListener("mouseup",()=>{document.activeElement===document.body&&this.$.overlay.getAttribute("tabindex")==="0"&&this.$.overlay.focus()}),this.addEventListener("animationcancel",()=>{this._flushAnimation("opening"),this._flushAnimation("closing")})}connectedCallback(){super.connectedCallback(),this._boundIosResizeListener&&(this._detectIosNavbar(),window.addEventListener("resize",this._boundIosResizeListener)),this.opened&&this._attachOverlay()}disconnectedCallback(){super.disconnectedCallback(),this.__scheduledOpen&&(cancelAnimationFrame(this.__scheduledOpen),this.__scheduledOpen=null),this._boundIosResizeListener&&window.removeEventListener("resize",this._boundIosResizeListener)}requestContentUpdate(){this.renderer&&this.renderer.call(this.owner,this._rendererRoot,this.owner,this.model)}close(e){let i=new CustomEvent("vaadin-overlay-close",{bubbles:!0,cancelable:!0,detail:{overlay:this,sourceEvent:e}});this.dispatchEvent(i),document.body.dispatchEvent(i),i.defaultPrevented||(this.opened=!1)}setBounds(e,i=!0){let r=this.$.overlay,o={...e};i&&r.style.position!=="absolute"&&(r.style.position="absolute"),Object.keys(o).forEach(n=>{o[n]!==null&&!isNaN(o[n])&&(o[n]=`${o[n]}px`)}),Object.assign(r.style,o)}_detectIosNavbar(){if(!this.opened)return;let e=window.innerHeight,r=window.innerWidth>e,o=document.documentElement.clientHeight;r&&o>e?this.style.setProperty("--vaadin-overlay-viewport-bottom",`${o-e}px`):this.style.setProperty("--vaadin-overlay-viewport-bottom","0px")}_shouldAddGlobalListeners(){return!this.modeless}_addGlobalListeners(){this.__hasGlobalListeners||(this.__hasGlobalListeners=!0,document.addEventListener("mousedown",this._boundMouseDownListener),document.addEventListener("mouseup",this._boundMouseUpListener),document.documentElement.addEventListener("click",this._boundOutsideClickListener,!0))}_removeGlobalListeners(){this.__hasGlobalListeners&&(this.__hasGlobalListeners=!1,document.removeEventListener("mousedown",this._boundMouseDownListener),document.removeEventListener("mouseup",this._boundMouseUpListener),document.documentElement.removeEventListener("click",this._boundOutsideClickListener,!0))}_rendererOrDataChanged(e,i,r,o){let n=this._oldOwner!==i||this._oldModel!==r;this._oldModel=r,this._oldOwner=i;let l=this._oldRenderer!==e,a=this._oldRenderer!==void 0;this._oldRenderer=e;let d=this._oldOpened!==o;this._oldOpened=o,l&&a&&(this._rendererRoot.innerHTML="",delete this._rendererRoot._$litPart$),o&&e&&(l||d||n)&&this.requestContentUpdate()}_modelessChanged(e){this.opened&&(this._shouldAddGlobalListeners()?this._addGlobalListeners():this._removeGlobalListeners()),e?this._exitModalState():this.opened&&this._enterModalState(),w(this,"modeless",e)}_withBackdropChanged(e){w(this,"with-backdrop",e)}_openedChanged(e,i){if(e){if(!this.isConnected){this.opened=!1;return}this._saveFocus(),this._animatedOpening(),this.__scheduledOpen=requestAnimationFrame(()=>{setTimeout(()=>{this._trapFocus();let r=new CustomEvent("vaadin-overlay-open",{detail:{overlay:this},bubbles:!0});this.dispatchEvent(r),document.body.dispatchEvent(r)})}),document.addEventListener("keydown",this._boundKeydownListener),this._shouldAddGlobalListeners()&&this._addGlobalListeners()}else i&&(this.__scheduledOpen&&(cancelAnimationFrame(this.__scheduledOpen),this.__scheduledOpen=null),this._resetFocus(),this._animatedClosing(),document.removeEventListener("keydown",this._boundKeydownListener),this._shouldAddGlobalListeners()&&this._removeGlobalListeners())}_hiddenChanged(e){e&&this.hasAttribute("closing")&&this._flushAnimation("closing")}_shouldAnimate(){let e=getComputedStyle(this),i=e.getPropertyValue("animation-name");return!(e.getPropertyValue("display")==="none")&&i&&i!=="none"}_enqueueAnimation(e,i){let r=`__${e}Handler`,o=n=>{n&&n.target!==this||(i(),this.removeEventListener("animationend",o),delete this[r])};this[r]=o,this.addEventListener("animationend",o)}_flushAnimation(e){let i=`__${e}Handler`;typeof this[i]=="function"&&this[i]()}_animatedOpening(){this._isAttached&&this.hasAttribute("closing")&&this._flushAnimation("closing"),this._attachOverlay(),this._appendAttachedInstance(),this.bringToFront(),this.modeless||this._enterModalState(),w(this,"opening",!0),this._shouldAnimate()?this._enqueueAnimation("opening",()=>{this._finishOpening()}):this._finishOpening()}_attachOverlay(){this.matches(":popover-open")||this.showPopover()}_finishOpening(){w(this,"opening",!1)}_finishClosing(){this._detachOverlay(),this._removeAttachedInstance(),this.$.overlay.style.removeProperty("pointer-events"),w(this,"closing",!1),this.dispatchEvent(new CustomEvent("vaadin-overlay-closed"))}_animatedClosing(){this.hasAttribute("opening")&&this._flushAnimation("opening"),this._isAttached&&(this._exitModalState(),w(this,"closing",!0),this.dispatchEvent(new CustomEvent("vaadin-overlay-closing")),this._shouldAnimate()?this._enqueueAnimation("closing",()=>{this._finishClosing()}):this._finishClosing())}_detachOverlay(){this.hidePopover()}_mouseDownListener(e){this._mouseDownInside=e.composedPath().indexOf(this.$.overlay)>=0}_mouseUpListener(e){this._mouseUpInside=e.composedPath().indexOf(this.$.overlay)>=0}_shouldCloseOnOutsideClick(e){return this._last}_outsideClickListener(e){if(e.composedPath().includes(this.$.overlay)||this._mouseDownInside||this._mouseUpInside){this._mouseDownInside=!1,this._mouseUpInside=!1;return}if(!this._shouldCloseOnOutsideClick(e))return;let i=new CustomEvent("vaadin-overlay-outside-click",{cancelable:!0,detail:{sourceEvent:e}});this.dispatchEvent(i),this.opened&&!i.defaultPrevented&&(this.close(e),!this.opened&&!this.modeless&&e.preventDefault())}_keydownListener(e){if(!(!this._last||e.defaultPrevented)&&!(!this._shouldAddGlobalListeners()&&!e.composedPath().includes(this._focusTrapRoot))&&e.key==="Escape"){let i=new CustomEvent("vaadin-overlay-escape-press",{cancelable:!0,detail:{sourceEvent:e}});this.dispatchEvent(i),this.opened&&!i.defaultPrevented&&this.close(e)}}};var Yi=u`
  @keyframes fade-in {
    0% {
      opacity: 0;
    }
  }

  @keyframes spin {
    to {
      rotate: 1turn;
    }
  }

  [part='loader'] {
    animation:
      spin var(--vaadin-spinner-animation-duration, 0.7s) linear infinite,
      fade-in 0.15s 0.3s both;
    border: var(--vaadin-spinner-width, 2px) solid var(--vaadin-spinner-color, var(--vaadin-text-color));
    border-radius: 50%;
    box-sizing: border-box;
    height: var(--vaadin-spinner-size, 1lh);
    mask-image: radial-gradient(circle at 50% var(--vaadin-spinner-width, 2px), transparent 40%, #000 70%);
    pointer-events: none;
    width: var(--vaadin-spinner-size, 1lh);
  }

  :host(:not([loading])) [part~='loader'] {
    display: none;
  }

  @media (forced-colors: active) {
    [part='loader'] {
      forced-color-adjust: none;
      --vaadin-spinner-color: CanvasText;
    }
  }
`;var De=[Yi,u`
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
  `];function Qi(s){let t=[];for(;s;){if(s.nodeType===Node.DOCUMENT_NODE){t.push(s);break}if(s.nodeType===Node.DOCUMENT_FRAGMENT_NODE){t.push(s),s=s.host;continue}if(s.assignedSlot){s=s.assignedSlot;continue}s=s.parentNode}return t}function Ne(s){return s?new Set(s.split(" ")):new Set}function ue(s){return s?[...s].join(" "):""}function Ht(s,t,e){let i=Ne(s.getAttribute(t));i.add(e),s.setAttribute(t,ue(i))}function Xi(s,t,e){let i=Ne(s.getAttribute(t));if(i.delete(e),i.size===0){s.removeAttribute(t);return}s.setAttribute(t,ue(i))}function Ji(s){return s.nodeType===Node.TEXT_NODE&&s.textContent.trim()===""}var jt={start:"top",end:"bottom"},Ut={start:"left",end:"right"},es=new ResizeObserver(s=>{setTimeout(()=>{s.forEach(t=>{t.target.__overlay&&t.target.__overlay._updatePosition()})})}),ts=s=>class extends s{static get properties(){return{positionTarget:{type:Object,value:null,sync:!0},horizontalAlign:{type:String,value:"start",sync:!0},verticalAlign:{type:String,value:"top",sync:!0},noHorizontalOverlap:{type:Boolean,value:!1,sync:!0},noVerticalOverlap:{type:Boolean,value:!1,sync:!0},requiredVerticalSpace:{type:Number,value:0,sync:!0}}}constructor(){super(),this._hasOverlayPositionMixin=!0,this.__onScroll=this.__onScroll.bind(this),this._updatePosition=this._updatePosition.bind(this)}connectedCallback(){super.connectedCallback(),this.opened&&this.__addUpdatePositionEventListeners()}disconnectedCallback(){super.disconnectedCallback(),this.__removeUpdatePositionEventListeners()}updated(e){if(super.updated(e),e.has("positionTarget")){let r=e.get("positionTarget");this.__oldContentWidth=void 0,this.__oldContentHeight=void 0,(!this.positionTarget&&r||this.positionTarget&&!r&&this.__margins)&&this.__resetPosition()}(e.has("opened")||e.has("positionTarget"))&&this.__updatePositionSettings(this.opened,this.positionTarget),["horizontalAlign","verticalAlign","noHorizontalOverlap","noVerticalOverlap","requiredVerticalSpace"].some(r=>e.has(r))&&this._updatePosition()}__addUpdatePositionEventListeners(){window.visualViewport.addEventListener("resize",this._updatePosition),window.visualViewport.addEventListener("scroll",this.__onScroll,!0),this.__positionTargetAncestorRootNodes=Qi(this.positionTarget),this.__positionTargetAncestorRootNodes.forEach(e=>{e.addEventListener("scroll",this.__onScroll,!0)}),this.positionTarget&&(this.__observePositionTargetMove=Zi(this.positionTarget,()=>{this._updatePosition()}))}__removeUpdatePositionEventListeners(){window.visualViewport.removeEventListener("resize",this._updatePosition),window.visualViewport.removeEventListener("scroll",this.__onScroll,!0),this.__positionTargetAncestorRootNodes&&(this.__positionTargetAncestorRootNodes.forEach(e=>{e.removeEventListener("scroll",this.__onScroll,!0)}),this.__positionTargetAncestorRootNodes=null),this.__observePositionTargetMove&&(this.__observePositionTargetMove(),this.__observePositionTargetMove=null)}__updatePositionSettings(e,i){if(this.__removeUpdatePositionEventListeners(),i&&(i.__overlay=null,es.unobserve(i),e&&(this.__addUpdatePositionEventListeners(),i.__overlay=this,es.observe(i))),e){let r=getComputedStyle(this);this.__margins||(this.__margins={},["top","bottom","left","right"].forEach(o=>{this.__margins[o]=parseInt(r[o],10)})),this._updatePosition(),requestAnimationFrame(()=>this._updatePosition())}}__onScroll(e){e.target instanceof Node&&this._deepContains(e.target)||this._updatePosition()}__resetPosition(){this.__margins=null,Object.assign(this.style,{justifyContent:"",alignItems:"",top:"",bottom:"",left:"",right:""}),w(this,"bottom-aligned",!1),w(this,"top-aligned",!1),w(this,"end-aligned",!1),w(this,"start-aligned",!1)}_updatePosition(){if(!this.positionTarget||!this.opened||!this.__margins)return;let e=this.positionTarget.getBoundingClientRect();if(e.width===0&&e.height===0&&this.opened){this.opened=!1;return}let i=this.__shouldAlignStartVertically(e);this.style.justifyContent=i?"flex-start":"flex-end";let r=this.__isRTL,o=this.__shouldAlignStartHorizontally(e,r),n=!r&&o||r&&!o;this.style.alignItems=n?"flex-start":"flex-end";let l=this.getBoundingClientRect(),a=this.__calculatePositionInOneDimension(e,l,this.noVerticalOverlap,jt,this,i),d=this.__calculatePositionInOneDimension(e,l,this.noHorizontalOverlap,Ut,this,o);Object.assign(this.style,a,d),w(this,"bottom-aligned",!i),w(this,"top-aligned",i),w(this,"end-aligned",!n),w(this,"start-aligned",n)}__shouldAlignStartHorizontally(e,i){let r=Math.max(this.__oldContentWidth||0,this.$.overlay.offsetWidth);this.__oldContentWidth=this.$.overlay.offsetWidth;let o=Math.min(window.innerWidth,document.documentElement.clientWidth),n=!i&&this.horizontalAlign==="start"||i&&this.horizontalAlign==="end";return this.__shouldAlignStart(e,r,o,this.__margins,n,this.noHorizontalOverlap,Ut)}__shouldAlignStartVertically(e){let i=this.requiredVerticalSpace||Math.max(this.__oldContentHeight||0,this.$.overlay.offsetHeight);this.__oldContentHeight=this.$.overlay.offsetHeight;let r=Math.min(window.innerHeight,document.documentElement.clientHeight),o=this.verticalAlign==="top";return this.__shouldAlignStart(e,i,r,this.__margins,o,this.noVerticalOverlap,jt)}__shouldAlignStart(e,i,r,o,n,l,a){let d=r-e[l?a.end:a.start]-o[a.end],h=e[l?a.start:a.end]-o[a.start],c=n?d:h,S=c>(n?h:d)||c>i;return n===S}__adjustBottomProperty(e,i,r){let o;if(e===i.end){if(i.end===jt.end){let n=Math.min(window.innerHeight,document.documentElement.clientHeight);if(r>n&&this.__oldViewportHeight){let l=this.__oldViewportHeight-n;o=r-l}this.__oldViewportHeight=n}if(i.end===Ut.end){let n=Math.min(window.innerWidth,document.documentElement.clientWidth);if(r>n&&this.__oldViewportWidth){let l=this.__oldViewportWidth-n;o=r-l}this.__oldViewportWidth=n}}return o}__calculatePositionInOneDimension(e,i,r,o,n,l){let a=l?o.start:o.end,d=l?o.end:o.start,h=parseFloat(n.style[a]||getComputedStyle(n)[a]),c=this.__adjustBottomProperty(a,o,h),y=i[l?o.start:o.end]-e[r===l?o.end:o.start],S=c?`${c}px`:`${h+y*(l?-1:1)}px`;return{[a]:S,[d]:""}}};var ze=s=>class extends ts(s){static get observers(){return["_setOverlayWidth(positionTarget, opened)"]}constructor(){super(),this.requiredVerticalSpace=200}_shouldCloseOnOutsideClick(e){let i=e.composedPath();return!i.includes(this.positionTarget)&&!i.includes(this)}_mouseDownListener(e){super._mouseDownListener(e),this._shouldCloseOnOutsideClick(e)&&!Ft(e.composedPath()[0])&&e.preventDefault()}_updateOverlayWidth(){this.style.setProperty(`--_${this.localName}-default-width`,`${this.positionTarget.offsetWidth}px`)}_setOverlayWidth(e,i){e&&i&&(this._updateOverlayWidth(),this._updatePosition())}};var qt=class extends ze($e(E(C(g(x(p)))))){static get is(){return"vaadin-combo-box-overlay"}static get styles(){return[Te,De]}render(){return f`
      <div part="overlay" id="overlay">
        <div part="loader"></div>
        <div part="content" id="content"><slot></slot></div>
      </div>
    `}};m(qt);var Be=u`
  :host {
    /* Fixes scrollbar disappearing when 'Show scroll bars: Always' enabled in Safari */
    box-shadow: 0 0 0 white;
    display: block;
    min-height: 1px;
    overflow: auto;
    /* Fixes item background from getting on top of scrollbars on Safari */
    transform: translate3d(0, 0, 0);
  }

  #selector {
    border: 0 solid transparent;
    border-width: var(--vaadin-item-overlay-padding, 4px);
    position: relative;
    forced-color-adjust: none;
    min-height: var(--_items-min-height, auto);
  }

  #selector > * {
    forced-color-adjust: auto;
  }
`;var wr=0;function Fe(){return wr++}var is=0,ss=0,G=[],Wt=!1;function Ir(){Wt=!1;let s=G.length;for(let t=0;t<s;t++){let e=G[t];if(e)try{e()}catch(i){setTimeout(()=>{throw i})}}G.splice(0,s),ss+=s}var K={after(s){return{run(t){return window.setTimeout(t,s)},cancel(t){window.clearTimeout(t)}}},run(s,t){return window.setTimeout(s,t)},cancel(s){window.clearTimeout(s)}};var F={run(s){return window.requestAnimationFrame(s)},cancel(s){window.cancelAnimationFrame(s)}};var Re={run(s){return window.requestIdleCallback?window.requestIdleCallback(s):window.setTimeout(s,16)},cancel(s){window.cancelIdleCallback?window.cancelIdleCallback(s):window.clearTimeout(s)}};var pe={run(s){Wt||(Wt=!0,queueMicrotask(()=>Ir())),G.push(s);let t=is;return is+=1,t},cancel(s){let t=s-ss;if(t>=0){if(!G[t])throw new Error(`invalid async handle: ${s}`);G[t]=null}}};var _e=new Set,I=class s{static debounce(t,e,i){return t instanceof s?t._cancelAsync():t=new s,t.setConfig(e,i),t}constructor(){this._asyncModule=null,this._callback=null,this._timer=null}setConfig(t,e){this._asyncModule=t,this._callback=e,this._timer=this._asyncModule.run(()=>{this._timer=null,_e.delete(this),this._callback()})}cancel(){this.isActive()&&(this._cancelAsync(),_e.delete(this))}_cancelAsync(){this.isActive()&&(this._asyncModule.cancel(this._timer),this._timer=null)}flush(){this.isActive()&&(this.cancel(),this._callback())}isActive(){return this._timer!=null}};function He(s){_e.add(s)}function Sr(){let s=!!_e.size;return _e.forEach(t=>{try{t.flush()}catch(e){setTimeout(()=>{throw e})}}),s}var R=()=>{let s;do s=Sr();while(s)};var rs=navigator.userAgent.match(/iP(?:hone|ad;(?: U;)? CPU) OS (\d+)/u),Er=rs&&rs[1]>=8,os=3,ns={_ratio:.5,_scrollerPaddingTop:0,_scrollPosition:0,_physicalSize:0,_physicalAverage:0,_physicalAverageCount:0,_physicalTop:0,_virtualCount:0,_estScrollHeight:0,_scrollHeight:0,_viewportHeight:0,_viewportWidth:0,_physicalItems:null,_physicalSizes:null,_firstVisibleIndexVal:null,_lastVisibleIndexVal:null,_maxPages:2,_templateCost:0,get _physicalBottom(){return this._physicalTop+this._physicalSize},get _scrollBottom(){return this._scrollPosition+this._viewportHeight},get _virtualEnd(){return this._virtualStart+this._physicalCount-1},get _hiddenContentSize(){return this._physicalSize-this._viewportHeight},get _maxScrollTop(){return this._estScrollHeight-this._viewportHeight+this._scrollOffset},get _maxVirtualStart(){let s=this._virtualCount;return Math.max(0,s-this._physicalCount)},get _virtualStart(){return this._virtualStartVal||0},set _virtualStart(s){s=this._clamp(s,0,this._maxVirtualStart),this._virtualStartVal=s},get _physicalStart(){return this._physicalStartVal||0},set _physicalStart(s){s%=this._physicalCount,s<0&&(s=this._physicalCount+s),this._physicalStartVal=s},get _physicalEnd(){return(this._physicalStart+this._physicalCount-1)%this._physicalCount},get _physicalCount(){return this._physicalCountVal||0},set _physicalCount(s){this._physicalCountVal=s},get _optPhysicalSize(){return this._viewportHeight===0?1/0:this._viewportHeight*this._maxPages},get _isVisible(){return!!(this.offsetWidth||this.offsetHeight)},get firstVisibleIndex(){let s=this._firstVisibleIndexVal;if(s==null){let t=this._physicalTop+this._scrollOffset;s=this._iterateItems((e,i)=>{if(t+=this._getPhysicalSizeIncrement(e),t>this._scrollPosition)return i})||0,this._firstVisibleIndexVal=s}return s},get lastVisibleIndex(){let s=this._lastVisibleIndexVal;if(s==null){let t=this._physicalTop+this._scrollOffset;this._iterateItems((e,i)=>{t<this._scrollBottom&&(s=i),t+=this._getPhysicalSizeIncrement(e)}),this._lastVisibleIndexVal=s}return s},get _scrollOffset(){return this._scrollerPaddingTop+this.scrollOffset},_scrollHandler(){let s=Math.max(0,Math.min(this._maxScrollTop,this._scrollTop)),t=s-this._scrollPosition,e=t>=0;if(this._scrollPosition=s,this._firstVisibleIndexVal=null,this._lastVisibleIndexVal=null,Math.abs(t)>this._physicalSize&&this._physicalSize>0){t-=this._scrollOffset;let i=Math.round(t/this._physicalAverage);this._virtualStart+=i,this._physicalStart+=i,this._physicalTop=Math.min(Math.floor(this._virtualStart)*this._physicalAverage,this._scrollPosition),this._update()}else if(this._physicalCount>0){let i=this._getReusables(e);e?(this._physicalTop=i.physicalTop,this._virtualStart+=i.indexes.length,this._physicalStart+=i.indexes.length):(this._virtualStart-=i.indexes.length,this._physicalStart-=i.indexes.length),this._update(i.indexes,e?null:i.indexes),this._debounce("_increasePoolIfNeeded",this._increasePoolIfNeeded.bind(this,0),pe)}},_getReusables(s){let t,e,i,r=[],o=this._hiddenContentSize*this._ratio,n=this._virtualStart,l=this._virtualEnd,a=this._physicalCount,d=this._physicalTop+this._scrollOffset,h=this._physicalBottom+this._scrollOffset,c=this._scrollPosition,y=this._scrollBottom;for(s?(t=this._physicalStart,e=c-d):(t=this._physicalEnd,e=h-y);i=this._getPhysicalSizeIncrement(t),e-=i,!(r.length>=a||e<=o);)if(s){if(l+r.length+1>=this._virtualCount||d+i>=c-this._scrollOffset)break;r.push(t),d+=i,t=(t+1)%a}else{if(n-r.length<=0||d+this._physicalSize-i<=y)break;r.push(t),d-=i,t=t===0?a-1:t-1}return{indexes:r,physicalTop:d-this._scrollOffset}},_update(s,t){if(!(s&&s.length===0||this._physicalCount===0)){if(this._assignModels(s),this._updateMetrics(s),t)for(;t.length;){let e=t.pop();this._physicalTop-=this._getPhysicalSizeIncrement(e)}this._positionItems(),this._updateScrollerSize()}},_isClientFull(){return this._scrollBottom!==0&&this._physicalBottom-1>=this._scrollBottom&&this._physicalTop<=this._scrollPosition},_increasePoolIfNeeded(s){let e=this._clamp(this._physicalCount+s,os,this._virtualCount-this._virtualStart)-this._physicalCount,i=Math.round(this._physicalCount*.5);if(!(e<0)){if(e>0){let r=window.performance.now();[].push.apply(this._physicalItems,this._createPool(e));for(let o=0;o<e;o++)this._physicalSizes.push(0);this._physicalCount+=e,this._physicalStart>this._physicalEnd&&this._isIndexRendered(this._focusedVirtualIndex)&&this._getPhysicalIndex(this._focusedVirtualIndex)<this._physicalEnd&&(this._physicalStart+=e),this._update(),this._templateCost=(window.performance.now()-r)/e,i=Math.round(this._physicalCount*.5)}this._virtualEnd>=this._virtualCount-1||i===0||(this._isClientFull()?this._physicalSize<this._optPhysicalSize&&this._debounce("_increasePoolIfNeeded",this._increasePoolIfNeeded.bind(this,this._clamp(Math.round(50/this._templateCost),1,i)),Re):this._debounce("_increasePoolIfNeeded",this._increasePoolIfNeeded.bind(this,i),pe))}},_render(){if(!(!this.isAttached||!this._isVisible))if(this._physicalCount!==0){let s=this._getReusables(!0);this._physicalTop=s.physicalTop,this._virtualStart+=s.indexes.length,this._physicalStart+=s.indexes.length,this._update(s.indexes),this._update(),this._increasePoolIfNeeded(0)}else this._virtualCount>0&&(this.updateViewportBoundaries(),this._increasePoolIfNeeded(os))},_itemsChanged(s){s.path==="items"&&(this._virtualStart=0,this._physicalTop=0,this._virtualCount=this.items?this.items.length:0,this._physicalIndexForKey={},this._firstVisibleIndexVal=null,this._lastVisibleIndexVal=null,this._physicalItems||(this._physicalItems=[]),this._physicalSizes||(this._physicalSizes=[]),this._physicalStart=0,this._scrollTop>this._scrollOffset&&this._resetScrollPosition(0),this._debounce("_render",this._render,F))},_iterateItems(s,t){let e,i,r,o;if(arguments.length===2&&t){for(o=0;o<t.length;o++)if(e=t[o],i=this._computeVidx(e),(r=s.call(this,e,i))!=null)return r}else{for(e=this._physicalStart,i=this._virtualStart;e<this._physicalCount;e++,i++)if((r=s.call(this,e,i))!=null)return r;for(e=0;e<this._physicalStart;e++,i++)if((r=s.call(this,e,i))!=null)return r}},_computeVidx(s){return s>=this._physicalStart?this._virtualStart+(s-this._physicalStart):this._virtualStart+(this._physicalCount-this._physicalStart)+s},_positionItems(){this._adjustScrollPosition();let s=this._physicalTop;this._iterateItems(t=>{this.translate3d(0,`${s}px`,0,this._physicalItems[t]),s+=this._physicalSizes[t]})},_getPhysicalSizeIncrement(s){return this._physicalSizes[s]},_adjustScrollPosition(){let s=this._virtualStart===0?this._physicalTop:Math.min(this._scrollPosition+this._physicalTop,0);if(s!==0){this._physicalTop-=s;let t=this._scrollPosition;!Er&&t>0&&this._resetScrollPosition(t-s)}},_resetScrollPosition(s){this.scrollTarget&&s>=0&&(this._scrollTop=s,this._scrollPosition=this._scrollTop)},_updateScrollerSize(s){let t=this._physicalBottom+Math.max(this._virtualCount-this._physicalCount-this._virtualStart,0)*this._physicalAverage;this._estScrollHeight=t,(s||this._scrollHeight===0||this._scrollPosition>=t-this._physicalSize||Math.abs(t-this._scrollHeight)>=this._viewportHeight)&&(this.$.items.style.height=`${t}px`,this._scrollHeight=t)},scrollToIndex(s){if(typeof s!="number"||s<0||s>this.items.length-1||(R(),this._physicalCount===0))return;s=this._clamp(s,0,this._virtualCount-1),(!this._isIndexRendered(s)||s>=this._maxVirtualStart)&&(this._virtualStart=s-1),this._assignModels(),this._updateMetrics(),this._physicalTop=this._virtualStart*this._physicalAverage;let t=this._physicalStart,e=this._virtualStart,i=0,r=this._hiddenContentSize;for(;e<s&&i<=r;)i+=this._getPhysicalSizeIncrement(t),t=(t+1)%this._physicalCount,e+=1;this._updateScrollerSize(!0),this._positionItems(),this._resetScrollPosition(this._physicalTop+this._scrollOffset+i),this._increasePoolIfNeeded(0),this._firstVisibleIndexVal=null,this._lastVisibleIndexVal=null},_resetAverage(){this._physicalAverage=0,this._physicalAverageCount=0},_resizeHandler(){this._debounce("_render",()=>{this._firstVisibleIndexVal=null,this._lastVisibleIndexVal=null,this._isVisible?(this.updateViewportBoundaries(),this.toggleScrollListener(!0),this._resetAverage(),this._render()):this.toggleScrollListener(!1)},F)},_isIndexRendered(s){return s>=this._virtualStart&&s<=this._virtualEnd},_getPhysicalIndex(s){return(this._physicalStart+(s-this._virtualStart))%this._physicalCount},_clamp(s,t,e){return Math.min(e,Math.max(t,s))},_debounce(s,t,e){this._debouncers||(this._debouncers={}),this._debouncers[s]=I.debounce(this._debouncers[s],e,t.bind(this)),He(this._debouncers[s])}};var Ar=1e5,Gt=1e3,fe=class{constructor({createElements:t,updateElement:e,scrollTarget:i,scrollContainer:r,reorderElements:o,elementsContainer:n,__disableHeightPlaceholder:l}){this.isAttached=!0,this._vidxOffset=0,this.createElements=t,this.updateElement=e,this.scrollTarget=i,this.scrollContainer=r,this.reorderElements=o,this.elementsContainer=n||r,this.__disableHeightPlaceholder=l??!1,this._maxPages=1.3,this.__placeholderHeight=200,this.__elementHeightQueue=Array(10),this.timeouts={SCROLL_REORDER:500,PREVENT_OVERSCROLL:500,FIX_INVALID_ITEM_POSITIONING:100},this.__resizeObserver=new ResizeObserver(()=>this._resizeHandler()),getComputedStyle(this.scrollTarget).overflow==="visible"&&(this.scrollTarget.style.overflow="auto"),getComputedStyle(this.scrollContainer).position==="static"&&(this.scrollContainer.style.position="relative"),this.__resizeObserver.observe(this.scrollTarget),this.scrollTarget.addEventListener("scroll",()=>this._scrollHandler()),new ResizeObserver(([{contentRect:d}])=>{let h=d.width===0&&d.height===0;!h&&this.__scrollTargetHidden&&this.scrollTarget.scrollTop!==this._scrollPosition&&(this.scrollTarget.scrollTop=this._scrollPosition),this.__scrollTargetHidden=h}).observe(this.scrollTarget),this.scrollTarget.addEventListener("virtualizer-element-focused",d=>this.__onElementFocused(d)),this.elementsContainer.addEventListener("focusin",()=>{this.scrollTarget.dispatchEvent(new CustomEvent("virtualizer-element-focused",{detail:{element:this.__getFocusedElement()}}))}),this.reorderElements&&(this.scrollTarget.addEventListener("mousedown",d=>{d.target===this.scrollTarget&&(this.__mouseDown=!0)}),this.scrollTarget.addEventListener("mouseup",()=>{this.__mouseDown=!1,this.__pendingReorder&&this.__reorderElements()}))}get scrollOffset(){return 0}get adjustedFirstVisibleIndex(){return this.firstVisibleIndex+this._vidxOffset}get adjustedLastVisibleIndex(){return this.lastVisibleIndex+this._vidxOffset}get _maxVirtualIndexOffset(){return this.size-this._virtualCount}__hasPlaceholders(){return this.__getVisibleElements().some(t=>t.__virtualizerPlaceholder)}scrollToIndex(t){if(typeof t!="number"||isNaN(t)||this.size===0||!this.scrollTarget.offsetHeight)return;delete this.__pendingScrollToIndex,this._physicalCount<=3&&this.flush(),t=this._clamp(t,0,this.size-1);let e=this.__getVisibleElements().length,i=Math.floor(t/this.size*this._virtualCount);this._virtualCount-i<e?(i=this._virtualCount-(this.size-t),this._vidxOffset=this._maxVirtualIndexOffset):i<e?t<Gt?(i=t,this._vidxOffset=0):(i=Gt,this._vidxOffset=t-i):this._vidxOffset=t-i,this.__skipNextVirtualIndexAdjust=!0,super.scrollToIndex(i),this.adjustedFirstVisibleIndex!==t&&this._scrollTop<this._maxScrollTop&&!this.grid&&(this._scrollTop-=this.__getIndexScrollOffset(t)||0),this._scrollHandler(),this.__hasPlaceholders()&&(this.__pendingScrollToIndex=t)}flush(){this.scrollTarget.offsetHeight!==0&&(this._resizeHandler(),R(),this._scrollHandler(),this.__fixInvalidItemPositioningDebouncer&&this.__fixInvalidItemPositioningDebouncer.flush(),this.__scrollReorderDebouncer&&this.__scrollReorderDebouncer.flush(),this.__debouncerWheelAnimationFrame&&this.__debouncerWheelAnimationFrame.flush())}hostConnected(){this.scrollTarget.offsetParent&&this.scrollTarget.scrollTop!==this._scrollPosition&&(this.scrollTarget.scrollTop=this._scrollPosition)}update(t=0,e=this.size-1){let i=[];this.__getVisibleElements().forEach(r=>{r.__virtualIndex>=t&&r.__virtualIndex<=e&&(this.__updateElement(r,r.__virtualIndex,!0),i.push(r))}),this.__afterElementsUpdated(i)}_updateMetrics(t){R();let e=0,i=0,r=this._physicalAverageCount,o=this._physicalAverage;this._iterateItems((n,l)=>{i+=this._physicalSizes[n];let a=this._physicalSizes[n];this._physicalSizes[n]=Math.ceil(this.__getBorderBoxHeight(this._physicalItems[n])),this._physicalSizes[n]!==a&&(this.__resizeObserver.unobserve(this._physicalItems[n]),this.__resizeObserver.observe(this._physicalItems[n],{box:"border-box"})),e+=this._physicalSizes[n],this._physicalAverageCount+=this._physicalSizes[n]?1:0},t),this._physicalSize=this._physicalSize+e-i,this._physicalAverageCount!==r&&(this._physicalAverage=Math.round((o*r+e)/this._physicalAverageCount))}__getBorderBoxHeight(t){let e=getComputedStyle(t),i=parseFloat(e.height)||0;if(e.boxSizing==="border-box")return i;let r=parseFloat(e.paddingBottom)||0,o=parseFloat(e.paddingTop)||0,n=parseFloat(e.borderBottomWidth)||0,l=parseFloat(e.borderTopWidth)||0;return i+r+o+n+l}__updateElement(t,e,i){t.__virtualizerPlaceholder&&(t.style.paddingTop="",t.style.opacity="",t.__virtualizerPlaceholder=!1),!this.__preventElementUpdates&&(t.__lastUpdatedIndex!==e||i)&&(this.updateElement(t,e),t.__lastUpdatedIndex=e)}__afterElementsUpdated(t){this.__disableHeightPlaceholder||t.forEach(e=>{let i=e.offsetHeight;if(i===0)e.style.paddingTop=`${this.__placeholderHeight}px`,e.style.opacity="0",e.__virtualizerPlaceholder=!0,this.__placeholderClearDebouncer=I.debounce(this.__placeholderClearDebouncer,F,()=>this._resizeHandler());else{this.__elementHeightQueue.push(i),this.__elementHeightQueue.shift();let r=this.__elementHeightQueue.filter(o=>o!==void 0);this.__placeholderHeight=Math.round(r.reduce((o,n)=>o+n,0)/r.length)}}),this.__pendingScrollToIndex!==void 0&&!this.__hasPlaceholders()&&this.scrollToIndex(this.__pendingScrollToIndex)}__getIndexScrollOffset(t){let e=this.__getVisibleElements().find(i=>i.__virtualIndex===t);return e?this.scrollTarget.getBoundingClientRect().top-e.getBoundingClientRect().top:void 0}__restoreScrollOffset(t,e){let i=this.__getIndexScrollOffset(t);e!==void 0&&i!==void 0&&(this._scrollTop+=e-i)}get size(){return this.__size}set size(t){if(t===this.size)return;this.__fixInvalidItemPositioningDebouncer&&this.__fixInvalidItemPositioningDebouncer.cancel(),this._debouncers&&this._debouncers._increasePoolIfNeeded&&this._debouncers._increasePoolIfNeeded.cancel();let e=t>0&&this._scrollTop>0,i,r;e&&(i=this.adjustedFirstVisibleIndex,r=this.__getIndexScrollOffset(i)),this.__size=t,this.__preventElementUpdates=e,this._itemsChanged({path:"items"}),R(),e&&(i=Math.min(i,t-1),this.scrollToIndex(i),this.__restoreScrollOffset(i,r)),this.__preventElementUpdates=!1,this._isVisible||this._assignModels(),this.elementsContainer.children.length||requestAnimationFrame(()=>this._resizeHandler()),this._updateScrollerSize(!0),this._resizeHandler(),R(),this._debounce("_update",this._update,pe)}get _scrollTop(){return this.scrollTarget.scrollTop}set _scrollTop(t){this.scrollTarget.scrollTop=t}get items(){return{length:Math.min(this.size,Ar)}}get offsetHeight(){return this.scrollTarget.offsetHeight}get $(){return{items:this.scrollContainer}}updateViewportBoundaries(){let t=window.getComputedStyle(this.scrollTarget);this._scrollerPaddingTop=this.scrollTarget===this?0:parseInt(t["padding-top"],10),this._isRTL=t.direction==="rtl",this._viewportWidth=this.elementsContainer.offsetWidth,this._viewportHeight=this.scrollTarget.offsetHeight}setAttribute(){}_createPool(t){let e=this.createElements(t),i=document.createDocumentFragment();return e.forEach(r=>{r.style.position="absolute",i.appendChild(r),this.__resizeObserver.observe(r,{box:"border-box"})}),this.elementsContainer.appendChild(i),e}_assignModels(t){let e=[];this._iterateItems((i,r)=>{let o=this._physicalItems[i];o.hidden=r>=this.size,o.hidden?delete o.__lastUpdatedIndex:(o.__virtualIndex=r+(this._vidxOffset||0),this.__updateElement(o,o.__virtualIndex),e.push(o))},t),this.__afterElementsUpdated(e)}_isClientFull(){return setTimeout(()=>{this.__clientFull=!0}),this.__clientFull||super._isClientFull()}translate3d(t,e,i,r){r.style.transform=`translateY(${e})`}toggleScrollListener(){}__getFocusedElement(t=this.__getVisibleElements()){let e=document.activeElement;for(;e?.shadowRoot?.activeElement;)e=e.shadowRoot.activeElement;for(;e&&!t.includes(e);)e=e.assignedSlot||e.parentNode||e.host;return e}__nextFocusableSiblingMissing(t,e){return e.indexOf(t)===e.length-1&&this.size>t.__virtualIndex+1}__previousFocusableSiblingMissing(t,e){return e.indexOf(t)===0&&t.__virtualIndex>0}__onElementFocused(t){if(!this.reorderElements)return;let e=t.detail.element;if(!e)return;let i=this.__getVisibleElements();(this.__previousFocusableSiblingMissing(e,i)||this.__nextFocusableSiblingMissing(e,i))&&this.flush();let r=this.__getVisibleElements();this.__nextFocusableSiblingMissing(e,r)?(this._scrollTop+=Math.ceil(e.getBoundingClientRect().bottom)-Math.floor(this.scrollTarget.getBoundingClientRect().bottom-1),this.flush()):this.__previousFocusableSiblingMissing(e,r)&&(this._scrollTop-=Math.ceil(this.scrollTarget.getBoundingClientRect().top+1)-Math.floor(e.getBoundingClientRect().top),this.flush())}_scrollHandler(){if(this.scrollTarget.offsetHeight===0)return;this._adjustVirtualIndexOffset(this._scrollTop-this._scrollPosition);let t=this._scrollTop-this._scrollPosition;if(super._scrollHandler(),this._physicalCount!==0){let e=t>=0,i=this._getReusables(!e);i.indexes.length&&(this._physicalTop=i.physicalTop,e?(this._virtualStart-=i.indexes.length,this._physicalStart-=i.indexes.length):(this._virtualStart+=i.indexes.length,this._physicalStart+=i.indexes.length),this._resizeHandler())}t&&(this.__fixInvalidItemPositioningDebouncer=I.debounce(this.__fixInvalidItemPositioningDebouncer,K.after(this.timeouts.FIX_INVALID_ITEM_POSITIONING),()=>this.__fixInvalidItemPositioning()),this.__overscrollDebouncer?.isActive()||(this.scrollTarget.style.overscrollBehavior="none"),this.__overscrollDebouncer=I.debounce(this.__overscrollDebouncer,K.after(this.timeouts.PREVENT_OVERSCROLL),()=>{this.scrollTarget.style.overscrollBehavior=null})),this.reorderElements&&(this.__scrollReorderDebouncer=I.debounce(this.__scrollReorderDebouncer,K.after(this.timeouts.SCROLL_REORDER),()=>this.__reorderElements())),this._scrollPosition===0&&this.firstVisibleIndex!==0&&Math.abs(t)>0&&this.scrollToIndex(0)}_resizeHandler(){super._resizeHandler();let t=this.adjustedLastVisibleIndex===this.size-1,e=this._physicalTop-this._scrollPosition;if(t&&e>0){let i=Math.ceil(e/this._physicalAverage);this._virtualStart=Math.max(0,this._virtualStart-i),this._physicalStart=Math.max(0,this._physicalStart-i),super.scrollToIndex(this._virtualCount-1),this.scrollTarget.scrollTop=this.scrollTarget.scrollHeight-this.scrollTarget.clientHeight}}__fixInvalidItemPositioning(){if(!this.scrollTarget.isConnected)return;let t=this._physicalTop>this._scrollTop,e=this._physicalBottom<this._scrollBottom,i=this.adjustedFirstVisibleIndex===0,r=this.adjustedLastVisibleIndex===this.size-1;if(t&&!i||e&&!r){let o=e,n=this._ratio;this._ratio=0,this._scrollPosition=this._scrollTop+(o?-1:1),this._scrollHandler(),this._ratio=n}}_increasePoolIfNeeded(t){if(this._physicalCount>2&&this._physicalAverage>0&&t>0){let i=Math.ceil(this._optPhysicalSize/this._physicalAverage)-this._physicalCount;super._increasePoolIfNeeded(Math.max(t,Math.min(100,i)))}else super._increasePoolIfNeeded(t)}get _optPhysicalSize(){let t=super._optPhysicalSize;return t<=0||this.__hasPlaceholders()?t:t+this.__getItemHeightBuffer()}__getItemHeightBuffer(){if(this._physicalCount===0)return 0;let t=Math.ceil(this._viewportHeight*(this._maxPages-1)/2),e=Math.max(...this._physicalSizes);return e>Math.min(...this._physicalSizes)?Math.max(0,e-t):0}__getVisibleElements(){return Array.from(this.elementsContainer.children).filter(t=>!t.hidden)}__reorderElements(){if(this.__mouseDown){this.__pendingReorder=!0;return}this.__pendingReorder=!1;let t=this._virtualStart+(this._vidxOffset||0),e=this.__getVisibleElements(),i=this.__getFocusedElement(e)||e[0];if(!i)return;let r=i.__virtualIndex-t,o=e.indexOf(i)-r;if(o>0)for(let n=0;n<o;n++)this.elementsContainer.appendChild(e[n]);else if(o<0)for(let n=e.length+o;n<e.length;n++)this.elementsContainer.insertBefore(e[n],e[0]);if(Ri){let{transform:n}=this.scrollTarget.style;this.scrollTarget.style.transform="translateZ(0)",setTimeout(()=>{this.scrollTarget.style.transform=n})}}_adjustVirtualIndexOffset(t){let e=this._maxVirtualIndexOffset;if(this._virtualCount>=this.size)this._vidxOffset=0;else if(this.__skipNextVirtualIndexAdjust)this.__skipNextVirtualIndexAdjust=!1;else if(Math.abs(t)>1e4){let i=this._scrollTop/(this.scrollTarget.scrollHeight-this.scrollTarget.clientHeight);this._vidxOffset=Math.round(i*e)}else{let i=this._vidxOffset,r=Gt,o=100,n,l,a=()=>{n=this.adjustedFirstVisibleIndex,l=this.__getIndexScrollOffset(n)};this._scrollTop===0?i!==0&&(a(),this._vidxOffset=0,super.scrollToIndex(0)):this.firstVisibleIndex<r&&this._vidxOffset>0&&(a(),this._vidxOffset-=Math.min(this._vidxOffset,o),super.scrollToIndex(this.firstVisibleIndex+(i-this._vidxOffset))),this._scrollTop>=this._maxScrollTop&&this._maxScrollTop>0?i!==e&&(a(),this._vidxOffset=e,super.scrollToIndex(this._virtualCount-1)):this.firstVisibleIndex>this._virtualCount-r&&this._vidxOffset<e&&(a(),this._vidxOffset+=Math.min(e-this._vidxOffset,o),super.scrollToIndex(this.firstVisibleIndex-(this._vidxOffset-i))),n!==void 0&&this.__restoreScrollOffset(n,l)}}};Object.setPrototypeOf(fe.prototype,ns);var je=class{constructor(t){this.__adapter=new fe(t)}get firstVisibleIndex(){return this.__adapter.adjustedFirstVisibleIndex}get lastVisibleIndex(){return this.__adapter.adjustedLastVisibleIndex}get size(){return this.__adapter.size}set size(t){this.__adapter.size=t}scrollToIndex(t){this.__adapter.scrollToIndex(t)}update(t=0,e=this.size-1){this.__adapter.update(t,e)}flush(){this.__adapter.flush()}hostConnected(){this.__adapter.hostConnected()}};var b=class{toString(){return""}};var Ue=s=>class extends s{static get properties(){return{items:{type:Array,sync:!0,observer:"__itemsChanged"},focusedIndex:{type:Number,sync:!0,observer:"__focusedIndexChanged"},loading:{type:Boolean,sync:!0,observer:"__loadingChanged"},opened:{type:Boolean,sync:!0,observer:"__openedChanged"},selectedItem:{type:Object,sync:!0,observer:"__selectedItemChanged"},itemClassNameGenerator:{type:Object,observer:"__itemClassNameGeneratorChanged"},itemIdPath:{type:String},owner:{type:Object},getItemLabel:{type:Object},renderer:{type:Object,sync:!0,observer:"__rendererChanged"},theme:{type:String}}}constructor(){super(),this.__boundOnItemClick=this.__onItemClick.bind(this)}get _viewportTotalPaddingBottom(){if(this._cachedViewportTotalPaddingBottom===void 0){let e=window.getComputedStyle(this.$.selector);this._cachedViewportTotalPaddingBottom=[e.paddingBottom,e.borderBottomWidth].map(i=>parseInt(i,10)).reduce((i,r)=>i+r)}return this._cachedViewportTotalPaddingBottom}ready(){super.ready(),this.setAttribute("role","listbox"),this.id=`${this.localName}-${Fe()}`,this.__hostTagName=this.constructor.is.replace("-scroller",""),this.addEventListener("click",e=>e.stopPropagation()),this.__patchWheelOverScrolling()}requestContentUpdate(){this.__virtualizer&&(this.items&&(this.__virtualizer.size=this.items.length),this.opened&&this.__virtualizer.update())}scrollIntoView(e,i=!1){if(!this.__virtualizer||!(this.opened&&e>=0))return;let r=[...this.children].find(h=>!h.hidden&&h.index===e);if(!i&&r){let h=r.getBoundingClientRect(),c=this.getBoundingClientRect();if(h.top>=c.top&&h.bottom+this._viewportTotalPaddingBottom<=c.bottom)return}let o=e;if(!i){let h=this._visibleItemsCount();e>this.__virtualizer.lastVisibleIndex-1?(this.__virtualizer.scrollToIndex(e),o=e-h+1):e>this.__virtualizer.firstVisibleIndex&&(o=this.__virtualizer.firstVisibleIndex)}this.__virtualizer.scrollToIndex(Math.max(0,o)),this.__virtualizer.flush();let n=[...this.children].find(h=>!h.hidden&&h.index===e);if(!n)return;if(i){n.scrollIntoView({block:"center"});return}let l=n.getBoundingClientRect(),a=this.getBoundingClientRect(),d=l.bottom+this._viewportTotalPaddingBottom;d>a.bottom?this.scrollTop+=d-a.bottom:l.top<a.top&&(this.scrollTop-=a.top-l.top)}_isItemSelected(e,i,r){return e instanceof b?!1:r&&e!==void 0&&i!==void 0?V(r,e)===V(r,i):e===i}__initVirtualizer(){this.__virtualizer=new je({createElements:this.__createElements.bind(this),updateElement:this._updateElement.bind(this),elementsContainer:this,scrollTarget:this,scrollContainer:this.$.selector,reorderElements:!0,__disableHeightPlaceholder:!0})}__itemsChanged(e){e&&this.__virtualizer&&this.requestContentUpdate()}__loadingChanged(){this.requestContentUpdate()}__openedChanged(e){if(e){this.__virtualizer||this.__initVirtualizer(),this.requestContentUpdate();return}let i=this.__virtualizer&&this.__virtualizer.__adapter;i&&i._scrollPosition>0&&(this.scrollTop=0,i._scrollPosition=0)}__selectedItemChanged(){this.requestContentUpdate()}__itemClassNameGeneratorChanged(e,i){(e||i)&&this.requestContentUpdate()}__focusedIndexChanged(e,i){e!==i&&this.requestContentUpdate(),e>=0&&!this.loading&&this.scrollIntoView(e)}__rendererChanged(e,i){(e||i)&&this.requestContentUpdate()}__createElements(e){return[...Array(e)].map(()=>{let i=document.createElement(`${this.__hostTagName}-item`);return i.addEventListener("click",this.__boundOnItemClick),i.tabIndex="-1",i.style.width="100%",i})}_updateElement(e,i){let r=this.items[i],o=this.focusedIndex,n=this._isItemSelected(r,this.selectedItem,this.itemIdPath);e.setProperties({item:r,index:i,label:this.getItemLabel(r),selected:n,renderer:this.renderer,focused:!this.loading&&o===i}),typeof this.itemClassNameGenerator=="function"?e.className=this.itemClassNameGenerator(r):e.className!==""&&(e.className=""),e.id=`${this.__hostTagName}-item-${i}`,e.setAttribute("role",i!==void 0?"option":!1),e.setAttribute("aria-selected",n.toString()),e.setAttribute("aria-posinset",i+1),e.setAttribute("aria-setsize",this.items.length),this.theme?e.setAttribute("theme",this.theme):e.removeAttribute("theme"),r instanceof b&&this.__requestItemByIndex(i)}__onItemClick(e){this.dispatchEvent(new CustomEvent("selection-changed",{detail:{item:e.currentTarget.item}}))}__patchWheelOverScrolling(){this.$.selector.addEventListener("wheel",e=>{let i=this.scrollTop===0,r=this.scrollHeight-this.scrollTop-this.clientHeight<=1;(i&&e.deltaY<0||r&&e.deltaY>0)&&e.preventDefault()})}__requestItemByIndex(e){requestAnimationFrame(()=>{this.dispatchEvent(new CustomEvent("index-requested",{detail:{index:e}}))})}_visibleItemsCount(){return this.__virtualizer.scrollToIndex(this.__virtualizer.firstVisibleIndex),this.__virtualizer.size>0?this.__virtualizer.lastVisibleIndex-this.__virtualizer.firstVisibleIndex+1:0}};var Kt=class extends Ue(g(p)){static get is(){return"vaadin-combo-box-scroller"}static get styles(){return Be}render(){return f`
      <div id="selector">
        <slot></slot>
      </div>
    `}};m(Kt);var Z=s=>s??v;var as=function(){};window.Vaadin||(window.Vaadin={});window.Vaadin.registrations||(window.Vaadin.registrations=[]);window.Vaadin.developmentModeCallback||(window.Vaadin.developmentModeCallback={});window.Vaadin.developmentModeCallback["vaadin-usage-statistics"]=function(){as()};var Zt,ls=new Set,qe=s=>class extends E(s){static _ensureRegistrations(){let{is:e}=this;if(e&&!ls.has(e)){window.Vaadin.registrations.push(this),ls.add(e);let i=window.Vaadin.developmentModeCallback;i&&(Zt=I.debounce(Zt,Re,()=>{i["vaadin-usage-statistics"]()}),He(Zt))}}constructor(){super(),document.doctype===null&&console.warn('Vaadin components require the "standards mode" declaration. Please add <!DOCTYPE html> to the HTML document.'),this.constructor._ensureRegistrations()}};var We=class{constructor(t,e,i={}){this.target=t,this.callback=e,this.forceInitial=i.forceInitial,this._storedNodes=[],this._isSlot=t instanceof HTMLSlotElement,this._connected=!1,this._scheduled=!1,this._boundSchedule=()=>{this._schedule()},this.connect(),i.syncInitial?this.flush():this._schedule()}connect(){this.target.addEventListener("slotchange",this._boundSchedule),this._connected=!0}disconnect(){this.target.removeEventListener("slotchange",this._boundSchedule),this._connected=!1}_schedule(){this._scheduled||(this._scheduled=!0,queueMicrotask(()=>{this._scheduled&&this.flush()}))}flush(){this._connected&&(this._scheduled=!1,this._processNodes())}_collectNodes(){let t=this._isSlot?[this.target]:[...this.target.querySelectorAll("slot")];return[...new Set(t.flatMap(e=>e.assignedNodes({flatten:!0})))]}_groupNodesBySlot(t){let e=new Map;return t.forEach(i=>{let r=i.assignedSlot;e.set(r,e.get(r)??[]),e.get(r).push(i)}),e}_collectMovedNodes(t){let e=this._groupNodesBySlot(t),i=this._groupNodesBySlot(this._storedNodes),r=[];return e.forEach((o,n)=>{let l=i.get(n)||[];new Set(l).difference(new Set(o)).size>0||l.forEach((a,d)=>{o.indexOf(a)!==d&&r.push(a)})}),r}_processNodes(){let t=this._collectNodes(),e=t.filter(o=>!this._storedNodes.includes(o)),i=this._storedNodes.filter(o=>!t.includes(o)),r=this._collectMovedNodes(t);(e.length||i.length||r.length||this.forceInitial)&&this.callback({addedNodes:e,currentNodes:t,movedNodes:r,removedNodes:i}),this.forceInitial&&(this.forceInitial=!1),this._storedNodes=t}};var P=class extends EventTarget{static generateId(t,e="default"){return`${e}-${t.localName}-${Fe()}`}constructor(t,e,i,r={}){super();let{initializer:o,multiple:n,observe:l,useUniqueId:a,uniqueIdPrefix:d}=r;this.host=t,this.slotName=e,this.tagName=i,this.observe=typeof l=="boolean"?l:!0,this.multiple=typeof n=="boolean"?n:!1,this.slotInitializer=o,n&&(this.nodes=[]),a&&(this.defaultId=this.constructor.generateId(t,d||e))}hostConnected(){this.initialized||(this.multiple?this.initMultiple():this.initSingle(),this.observe&&this.observeSlot(),this.initialized=!0)}initSingle(){let t=this.getSlotChild();t?(this.node=t,this.initAddedNode(t)):(t=this.attachDefaultNode(),this.initNode(t))}initMultiple(){let t=this.getSlotChildren();if(t.length===0){let e=this.attachDefaultNode();e&&(this.nodes=[e],this.initNode(e))}else this.nodes=t,t.forEach(e=>{this.initAddedNode(e)})}attachDefaultNode(){let{host:t,slotName:e,tagName:i}=this,r=this.defaultNode;return!r&&i&&(r=document.createElement(i),r instanceof Element&&(e!==""&&r.setAttribute("slot",e),this.defaultNode=r)),r&&(this.node=r,t.appendChild(r)),r}getSlotChildren(){let{slotName:t}=this;return Array.from(this.host.childNodes).filter(e=>e.nodeType===Node.ELEMENT_NODE&&e.hasAttribute("data-slot-ignore")?!1:e.nodeType===Node.ELEMENT_NODE&&e.slot===t||e.nodeType===Node.TEXT_NODE&&e.textContent.trim()&&t==="")}getSlotChild(){return this.getSlotChildren()[0]}initNode(t){let{slotInitializer:e}=this;e&&e(t,this.host)}initCustomNode(t){}teardownNode(t){}initAddedNode(t){t!==this.defaultNode&&(this.initCustomNode(t),this.initNode(t))}observeSlot(){let{slotName:t}=this,e=t===""?"slot:not([name])":`slot[name=${t}]`,i=this.host.shadowRoot.querySelector(e);this.__slotObserver=new We(i,({addedNodes:r,removedNodes:o})=>{let n=this.multiple?this.nodes:[this.node],l=r.filter(a=>!Ji(a)&&!n.includes(a)&&!(a.nodeType===Node.ELEMENT_NODE&&a.hasAttribute("data-slot-ignore")));o.length&&(this.nodes=n.filter(a=>!o.includes(a)),o.forEach(a=>{this.teardownNode(a)})),l?.length>0&&(this.multiple?(this.defaultNode&&this.defaultNode.remove(),this.nodes=[...n,...l].filter(a=>a!==this.defaultNode),l.forEach(a=>{this.initAddedNode(a)})):(this.node&&this.node.remove(),this.node=l[0],this.initAddedNode(this.node)))})}};var Y=class extends P{constructor(t){super(t,"tooltip"),this.setTarget(t),this.__onContentChange=this.__onContentChange.bind(this)}initCustomNode(t){t.target=this.target,this.ariaTarget!==void 0&&(t.ariaTarget=this.ariaTarget),this.context!==void 0&&(t.context=this.context),this.manual!==void 0&&(t.manual=this.manual),this.position!==void 0&&(t._position=this.position),this.shouldShow!==void 0&&(t.shouldShow=this.shouldShow),this.manual||this.host.setAttribute("has-tooltip",""),this.__notifyChange(t),t.addEventListener("content-changed",this.__onContentChange)}teardownNode(t){this.manual||this.host.removeAttribute("has-tooltip"),t.removeEventListener("content-changed",this.__onContentChange),this.__notifyChange(null)}setAriaTarget(t){this.ariaTarget=t;let e=this.node;e&&(e.ariaTarget=t)}setContext(t){this.context=t;let e=this.node;e&&(e.context=t)}setManual(t){this.manual=t;let e=this.node;e&&(e.manual=t)}setPosition(t){this.position=t;let e=this.node;e&&(e._position=t)}setShouldShow(t){this.shouldShow=t;let e=this.node;e&&(e.shouldShow=t)}setTarget(t){this.target=t;let e=this.node;e&&(e.target=t)}open(t){let e=this.node;e?.isConnected&&e._stateController.open(t)}close(t){let e=this.node;e&&e._stateController.close(t)}__onContentChange(t){this.__notifyChange(t.target)}__notifyChange(t){this.dispatchEvent(new CustomEvent("tooltip-changed",{detail:{node:t}}))}};var Pr=s=>class extends s{get _keyboardActive(){return L()}ready(){this.addEventListener("focusin",e=>{this._shouldSetFocus(e)&&this._setFocused(!0)}),this.addEventListener("focusout",e=>{this._shouldRemoveFocus(e)&&this._setFocused(!1)}),super.ready()}disconnectedCallback(){super.disconnectedCallback(),this.hasAttribute("focused")&&this._setFocused(!1)}focus(e){super.focus(e),e?.focusVisible!==!1&&this.setAttribute("focus-ring","")}_setFocused(e){this.toggleAttribute("focused",e),this.toggleAttribute("focus-ring",e&&this._keyboardActive)}_shouldSetFocus(e){return!0}_shouldRemoveFocus(e){return!0}},Ge=_(Pr);var Tr=s=>class extends s{static get properties(){return{disabled:{type:Boolean,value:!1,observer:"_disabledChanged",reflectToAttribute:!0,sync:!0}}}_disabledChanged(e){this._setAriaDisabled(e)}_setAriaDisabled(e){e?this.setAttribute("aria-disabled","true"):this.removeAttribute("aria-disabled")}click(){this.disabled||super.click()}},Ke=_(Tr);var ds=s=>class extends Ke(s){static get properties(){return{tabindex:{type:Number,reflectToAttribute:!0,observer:"_tabindexChanged",sync:!0},_lastTabIndex:{type:Number}}}_disabledChanged(e,i){super._disabledChanged(e,i),!this.__shouldAllowFocusWhenDisabled()&&(e?(this.tabindex!==void 0&&(this._lastTabIndex=this.tabindex),this.setAttribute("tabindex","-1")):i&&(this._lastTabIndex!==void 0?this.setAttribute("tabindex",this._lastTabIndex):this.tabindex=void 0))}_tabindexChanged(e){this.__shouldAllowFocusWhenDisabled()||this.disabled&&e!==-1&&(this._lastTabIndex=e,this.setAttribute("tabindex","-1"))}focus(e){(!this.disabled||this.__shouldAllowFocusWhenDisabled())&&super.focus(e)}__shouldAllowFocusWhenDisabled(){return!1}};var Or=s=>class extends Ge(ds(s)){static get properties(){return{autofocus:{type:Boolean},focusElement:{type:Object,readOnly:!0,observer:"_focusElementChanged",sync:!0},_lastTabIndex:{value:0}}}constructor(){super(),this._boundOnBlur=this._onBlur.bind(this),this._boundOnFocus=this._onFocus.bind(this)}ready(){super.ready(),this.autofocus&&!this.disabled&&requestAnimationFrame(()=>{this.focus()})}focus(e){this.focusElement&&!this.disabled&&(this.focusElement.focus(),e?.focusVisible!==!1&&this.setAttribute("focus-ring",""))}blur(){this.focusElement&&this.focusElement.blur()}click(){this.focusElement&&!this.disabled&&this.focusElement.click()}_focusElementChanged(e,i){e?(e.disabled=this.disabled,this._addFocusListeners(e),this.__forwardTabIndex(this.tabindex)):i&&this._removeFocusListeners(i)}_addFocusListeners(e){e.addEventListener("blur",this._boundOnBlur),e.addEventListener("focus",this._boundOnFocus)}_removeFocusListeners(e){e.removeEventListener("blur",this._boundOnBlur),e.removeEventListener("focus",this._boundOnFocus)}_onFocus(e){e.stopPropagation(),this.dispatchEvent(new Event("focus"))}_onBlur(e){e.stopPropagation(),this.dispatchEvent(new Event("blur"))}_shouldSetFocus(e){return e.target===this.focusElement}_shouldRemoveFocus(e){return e.target===this.focusElement}_disabledChanged(e,i){super._disabledChanged(e,i),this.focusElement&&(this.focusElement.disabled=e),e&&this.blur()}_tabindexChanged(e){this.__forwardTabIndex(e)}__forwardTabIndex(e){e!==void 0&&this.focusElement&&(this.focusElement.tabIndex=e,e!==-1&&(this.tabindex=void 0)),this.disabled&&e&&(e!==-1&&(this._lastTabIndex=e),this.tabindex=void 0),e===void 0&&this.hasAttribute("tabindex")&&this.removeAttribute("tabindex")}},hs=_(Or);var Mr=s=>class extends s{ready(){super.ready(),this.addEventListener("keydown",e=>{this._onKeyDown(e)}),this.addEventListener("keyup",e=>{this._onKeyUp(e)})}_onKeyDown(e){switch(e.key){case"Enter":this._onEnter(e);break;case"Escape":this._onEscape(e);break;default:break}}_onKeyUp(e){}_onEnter(e){}_onEscape(e){}},Q=_(Mr);var Yt=new WeakMap;function kr(s){return Yt.has(s)||Yt.set(s,new Set),Yt.get(s)}function Vr(s,t){let e=document.createElement("style");e.textContent=s,t===document?document.head.appendChild(e):t.insertBefore(e,t.firstChild)}var Lr=s=>class extends s{get slotStyles(){return[]}connectedCallback(){super.connectedCallback(),this.__applySlotStyles()}__applySlotStyles(){let e=this.getRootNode(),i=kr(e);this.slotStyles.forEach(r=>{i.has(r)||(Vr(r,e),i.add(r))})}},cs=_(Lr);var $r=s=>class extends s{static get properties(){return{inputElement:{type:Object,readOnly:!0,observer:"_inputElementChanged",sync:!0},type:{type:String,readOnly:!0},value:{type:String,value:"",observer:"_valueChanged",notify:!0,sync:!0}}}constructor(){super(),this._boundOnInput=this._onInput.bind(this),this._boundOnChange=this._onChange.bind(this)}get _hasValue(){return this.value!=null&&this.value!==""}get _inputElementValueProperty(){return"value"}get _inputElementValue(){return this.inputElement?this.inputElement[this._inputElementValueProperty]:void 0}set _inputElementValue(e){this.inputElement&&(this.inputElement[this._inputElementValueProperty]=e)}clear(){this.value="",this._inputElementValue=""}_addInputListeners(e){e.addEventListener("input",this._boundOnInput),e.addEventListener("change",this._boundOnChange)}_removeInputListeners(e){e.removeEventListener("input",this._boundOnInput),e.removeEventListener("change",this._boundOnChange)}_forwardInputValue(e){this.inputElement&&(this._inputElementValue=e??"")}_inputElementChanged(e,i){e?this._addInputListeners(e):i&&this._removeInputListeners(i)}_onInput(e){let i=e.composedPath()[0];this.__userInput=e.isTrusted,this.value=i.value,this.__userInput=!1}_onChange(e){}_toggleHasValue(e){this.toggleAttribute("has-value",e)}_valueChanged(e,i){this._toggleHasValue(this._hasValue),!(e===""&&i===void 0)&&(this.__userInput||this._forwardInputValue(e))}},X=_($r);var us=s=>class extends X(Q(s)){static get properties(){return{clearButtonVisible:{type:Boolean,reflectToAttribute:!0,value:!1}}}get clearElement(){return console.warn(`Please implement the 'clearElement' property in <${this.localName}>`),null}ready(){super.ready(),this.clearElement&&(this.clearElement.addEventListener("mousedown",e=>this._onClearButtonMouseDown(e)),this.clearElement.addEventListener("click",e=>this._onClearButtonClick(e)))}_onClearButtonClick(e){e.preventDefault(),this._onClearAction()}_onClearButtonMouseDown(e){this._shouldKeepFocusOnClearMousedown()&&e.preventDefault(),he||this.inputElement.focus()}_onEscape(e){super._onEscape(e),this.clearButtonVisible&&this.value&&!this.readonly&&(e.stopPropagation(),this._onClearAction())}_onClearAction(){this._inputElementValue="",this.inputElement.dispatchEvent(new Event("input",{bubbles:!0,composed:!0})),this.inputElement.dispatchEvent(new Event("change",{bubbles:!0}))}_shouldKeepFocusOnClearMousedown(){return W(this.inputElement)}};var Qt=new Map;function Xt(s){return Qt.has(s)||Qt.set(s,new WeakMap),Qt.get(s)}function ps(s,t){s&&s.removeAttribute(t)}function _s(s,t){if(!s||!t)return;let e=Xt(t);if(e.has(s))return;let i=Ne(s.getAttribute(t));e.set(s,new Set(i))}function fs(s,t){if(!s||!t)return;let e=Xt(t),i=e.get(s);!i||i.size===0?s.removeAttribute(t):Ht(s,t,ue(i)),e.delete(s)}function Ze(s,t,e={newId:null,oldId:null,fromUser:!1}){if(!s||!t)return;let{newId:i,oldId:r,fromUser:o}=e,n=Xt(t),l=n.get(s);if(!o&&l){r&&l.delete(r),i&&l.add(i);return}o&&(l?i||n.delete(s):_s(s,t),ps(s,t)),Xi(s,t,r);let a=i||ue(l);a&&Ht(s,t,a)}function ms(s,t){_s(s,t),ps(s,t)}var Ye=class{constructor(t){this.host=t,this.__required=!1}setTarget(t){this.__target=t,this.__setAriaRequiredAttribute(this.__required),this.__setLabelIdToAriaAttribute(this.__labelId,this.__labelId),this.__labelIdFromUser!=null&&this.__setLabelIdToAriaAttribute(this.__labelIdFromUser,this.__labelIdFromUser,!0),this.__setErrorIdToAriaAttribute(this.__errorId),this.__setHelperIdToAriaAttribute(this.__helperId),this.setAriaLabel(this.__label)}setRequired(t){this.__setAriaRequiredAttribute(t),this.__required=t}setAriaLabel(t){this.__setAriaLabelToAttribute(t),this.__label=t}setLabelId(t,e=!1){let i=e?this.__labelIdFromUser:this.__labelId;this.__setLabelIdToAriaAttribute(t,i,e),e?this.__labelIdFromUser=t:this.__labelId=t}setErrorId(t){this.__setErrorIdToAriaAttribute(t,this.__errorId),this.__errorId=t}setHelperId(t){this.__setHelperIdToAriaAttribute(t,this.__helperId),this.__helperId=t}__setAriaLabelToAttribute(t){this.__target&&(t?(ms(this.__target,"aria-labelledby"),this.__target.setAttribute("aria-label",t)):this.__label&&(fs(this.__target,"aria-labelledby"),this.__target.removeAttribute("aria-label")))}__setLabelIdToAriaAttribute(t,e,i){Ze(this.__target,"aria-labelledby",{newId:t,oldId:e,fromUser:i})}__setErrorIdToAriaAttribute(t,e){Ze(this.__target,"aria-describedby",{newId:t,oldId:e,fromUser:!1})}__setHelperIdToAriaAttribute(t,e){Ze(this.__target,"aria-describedby",{newId:t,oldId:e,fromUser:!1})}__setAriaRequiredAttribute(t){this.__target&&(["input","textarea"].includes(this.__target.localName)||(t?this.__target.setAttribute("aria-required","true"):this.__target.removeAttribute("aria-required")))}};var A=document.createElement("div");A.style.position="fixed";A.style.clip="rect(0px, 0px, 0px, 0px)";A.setAttribute("aria-live","polite");document.body.appendChild(A);var Qe;function J(s,t={}){let e=t.mode||"polite",i=t.timeout??150;e==="alert"?(A.removeAttribute("aria-live"),A.removeAttribute("role"),Qe=I.debounce(Qe,F,()=>{A.setAttribute("role","alert")})):(Qe&&Qe.cancel(),A.removeAttribute("role"),A.setAttribute("aria-live",e)),A.textContent="",setTimeout(()=>{A.textContent=s},i)}var $=class extends P{constructor(t,e,i,r={}){super(t,e,i,{...r,useUniqueId:!0})}initCustomNode(t){this.__updateNodeId(t),this.__notifyChange(t)}teardownNode(t){let e=this.getSlotChild();e&&e!==this.defaultNode?this.__notifyChange(e):(this.restoreDefaultNode(),this.updateDefaultNode(this.node))}attachDefaultNode(){let t=super.attachDefaultNode();return t&&this.__updateNodeId(t),t}restoreDefaultNode(){}updateDefaultNode(t){this.__notifyChange(t)}observeNode(t){this.__nodeObserver&&this.__nodeObserver.disconnect(),this.__nodeObserver=new MutationObserver(e=>{e.forEach(i=>{let r=i.target,o=r===this.node;i.type==="attributes"?o&&this.__updateNodeId(r):(o||r.parentElement===this.node)&&this.__notifyChange(this.node)})}),this.__nodeObserver.observe(t,{attributes:!0,attributeFilter:["id"],childList:!0,subtree:!0,characterData:!0})}__hasContent(t){return t?t.nodeType===Node.ELEMENT_NODE&&(customElements.get(t.localName)||t.children.length>0)||t.textContent&&t.textContent.trim()!=="":!1}__notifyChange(t){this.dispatchEvent(new CustomEvent("slot-content-changed",{detail:{hasContent:this.__hasContent(t),node:t}}))}__updateNodeId(t){let e=!this.nodes||t===this.nodes[0];t.nodeType===Node.ELEMENT_NODE&&(!this.multiple||e)&&!t.id&&(t.id=this.defaultId)}};var Xe=class extends ${constructor(t){super(t,"error-message","div")}setErrorMessage(t){this.errorMessage=t,this.updateDefaultNode(this.node)}setInvalid(t){this.invalid=t,this.updateDefaultNode(this.node)}initAddedNode(t){t!==this.defaultNode&&this.initCustomNode(t)}initNode(t){this.updateDefaultNode(t)}initCustomNode(t){t.textContent&&!this.errorMessage&&(this.errorMessage=t.textContent.trim()),super.initCustomNode(t)}restoreDefaultNode(){this.attachDefaultNode()}updateDefaultNode(t){let{errorMessage:e,invalid:i}=this,r=!!(i&&e&&e.trim()!=="");t&&(t.textContent=r?e:"",t.hidden=!r,r&&J(e,{mode:"assertive"})),super.updateDefaultNode(t)}};var Je=class extends ${constructor(t){super(t,"helper",null)}setHelperText(t){this.helperText=t,this.getSlotChild()||this.restoreDefaultNode(),this.node===this.defaultNode&&this.updateDefaultNode(this.node)}restoreDefaultNode(){let{helperText:t}=this;if(t&&t.trim()!==""){this.tagName="div";let e=this.attachDefaultNode();this.observeNode(e)}}updateDefaultNode(t){t&&(t.textContent=this.helperText),super.updateDefaultNode(t)}initCustomNode(t){super.initCustomNode(t),this.observeNode(t)}};var et=class extends ${constructor(t){super(t,"label","label")}setLabel(t){this.label=t,this.getSlotChild()||this.restoreDefaultNode(),this.node===this.defaultNode&&this.updateDefaultNode(this.node)}restoreDefaultNode(){let{label:t}=this;if(t&&t.trim()!==""){let e=this.attachDefaultNode();this.observeNode(e)}}updateDefaultNode(t){t&&(t.textContent=this.label),super.updateDefaultNode(t)}initCustomNode(t){super.initCustomNode(t),this.observeNode(t)}};var vs=s=>class extends s{static get properties(){return{label:{type:String,observer:"_labelChanged"}}}constructor(){super(),this._labelController=new et(this),this._labelController.addEventListener("slot-content-changed",e=>{this.toggleAttribute("has-label",e.detail.hasContent)})}get _labelId(){return this._labelNode?.id}get _labelNode(){return this._labelController.node}ready(){super.ready(),this.addController(this._labelController)}_labelChanged(e){this._labelController.setLabel(e)}};var Dr=s=>class extends s{static get properties(){return{invalid:{type:Boolean,reflectToAttribute:!0,notify:!0,value:!1,sync:!0},manualValidation:{type:Boolean,value:!1},required:{type:Boolean,reflectToAttribute:!0,sync:!0}}}validate(){let t=this.checkValidity();return this._setInvalid(!t),this.dispatchEvent(new CustomEvent("validated",{detail:{valid:t}})),t}checkValidity(){return!this.required||!!this.value}_setInvalid(t){this._shouldSetInvalid(t)&&(this.invalid=t)}_shouldSetInvalid(t){return!0}_requestValidation(){this.manualValidation||this.validate()}},ee=_(Dr);var gs=s=>class extends ee(vs(s)){static get properties(){return{ariaTarget:{type:Object,observer:"_ariaTargetChanged"},errorMessage:{type:String,observer:"_errorMessageChanged"},helperText:{type:String,observer:"_helperTextChanged"},accessibleName:{type:String,observer:"_accessibleNameChanged"},accessibleNameRef:{type:String,observer:"_accessibleNameRefChanged"}}}static get observers(){return["_invalidChanged(invalid)","_requiredChanged(required)"]}constructor(){super(),this._fieldAriaController=new Ye(this),this._helperController=new Je(this),this._errorController=new Xe(this),this._errorController.addEventListener("slot-content-changed",e=>{this.toggleAttribute("has-error-message",e.detail.hasContent)}),this._labelController.addEventListener("slot-content-changed",e=>{let{hasContent:i,node:r}=e.detail;this.__labelChanged(i,r)}),this._helperController.addEventListener("slot-content-changed",e=>{let{hasContent:i,node:r}=e.detail;this.toggleAttribute("has-helper",i),this.__helperChanged(i,r)})}get _errorNode(){return this._errorController.node}get _helperNode(){return this._helperController.node}ready(){super.ready(),this.addController(this._fieldAriaController),this.addController(this._helperController),this.addController(this._errorController)}__helperChanged(e,i){e?this._fieldAriaController.setHelperId(i.id):this._fieldAriaController.setHelperId(null)}_accessibleNameChanged(e){this._fieldAriaController.setAriaLabel(e)}_accessibleNameRefChanged(e){this._fieldAriaController.setLabelId(e,!0)}__labelChanged(e,i){e?this._fieldAriaController.setLabelId(i.id):this._fieldAriaController.setLabelId(null)}_errorMessageChanged(e){this._errorController.setErrorMessage(e)}_helperTextChanged(e){this._helperController.setHelperText(e)}_ariaTargetChanged(e){e&&this._fieldAriaController.setTarget(e)}_requiredChanged(e){this._fieldAriaController.setRequired(e)}_invalidChanged(e){this._errorController.setInvalid(e),setTimeout(()=>{if(e){let i=this._errorNode;this._fieldAriaController.setErrorId(i?.id)}else this._fieldAriaController.setErrorId(null)})}};var Nr=s=>class extends s{static get properties(){return{stateTarget:{type:Object,observer:"_stateTargetChanged"}}}static get delegateAttrs(){return[]}static get delegateProps(){return[]}ready(){super.ready(),this._createDelegateAttrsObserver(),this._createDelegatePropsObserver()}_stateTargetChanged(e){e&&(this._ensureAttrsDelegated(),this._ensurePropsDelegated())}_createDelegateAttrsObserver(){this._createMethodObserver(`_delegateAttrsChanged(${this.constructor.delegateAttrs.join(", ")})`)}_createDelegatePropsObserver(){this._createMethodObserver(`_delegatePropsChanged(${this.constructor.delegateProps.join(", ")})`)}_ensureAttrsDelegated(){this.constructor.delegateAttrs.forEach(e=>{this._delegateAttribute(e,this[e])})}_ensurePropsDelegated(){this.constructor.delegateProps.forEach(e=>{this._delegateProperty(e,this[e])})}_delegateAttrsChanged(...e){this.constructor.delegateAttrs.forEach((i,r)=>{this._delegateAttribute(i,e[r])})}_delegatePropsChanged(...e){this.constructor.delegateProps.forEach((i,r)=>{this._delegateProperty(i,e[r])})}_delegateAttribute(e,i){this.stateTarget&&(e==="invalid"&&this._delegateAttribute("aria-invalid",i?"true":!1),typeof i=="boolean"?this.stateTarget.toggleAttribute(e,i):i?this.stateTarget.setAttribute(e,i):this.stateTarget.removeAttribute(e))}_delegateProperty(e,i){this.stateTarget&&(this.stateTarget[e]=i)}},bs=_(Nr);var zr=s=>class extends bs(ee(X(s))){static get constraints(){return["required"]}static get delegateAttrs(){return[...super.delegateAttrs,"required"]}ready(){super.ready(),this._createConstraintsObserver()}checkValidity(){return this.inputElement&&this._hasValidConstraints(this.constructor.constraints.map(e=>this[e]))?this.inputElement.checkValidity():!this.invalid}_hasValidConstraints(e){return e.some(i=>this.__isValidConstraint(i))}_createConstraintsObserver(){this._createMethodObserver(`_constraintsChanged(stateTarget, ${this.constructor.constraints.join(", ")})`)}_constraintsChanged(e,...i){if(!e)return;let r=this._hasValidConstraints(i),o=this.__previousHasConstraints&&!r;(this._hasValue||this.invalid)&&r?this._requestValidation():o&&!this.manualValidation&&this._setInvalid(!1),this.__previousHasConstraints=r}_onChange(e){e.stopPropagation(),this._requestValidation(),this.dispatchEvent(new CustomEvent("change",{detail:{sourceEvent:e},bubbles:e.bubbles,cancelable:e.cancelable}))}__isValidConstraint(e){return!!e||e===0}},tt=_(zr);var it=s=>class extends cs(hs(tt(gs(us(Q(s)))))){static get properties(){return{allowedCharPattern:{type:String,observer:"_allowedCharPatternChanged"},autoselect:{type:Boolean,value:!1},name:{type:String,reflectToAttribute:!0},placeholder:{type:String,reflectToAttribute:!0},readonly:{type:Boolean,value:!1,reflectToAttribute:!0},title:{type:String,reflectToAttribute:!0}}}static get delegateAttrs(){return[...super.delegateAttrs,"name","type","placeholder","readonly","invalid","title"]}constructor(){super(),this._boundOnPaste=this._onPaste.bind(this),this._boundOnDrop=this._onDrop.bind(this),this._boundOnBeforeInput=this._onBeforeInput.bind(this)}get slotStyles(){let e=this.localName;return[`
          /* Needed for Safari, where ::slotted(...)::placeholder does not work */
          ${e} > :is(input[slot='input'], textarea[slot='textarea'])::placeholder {
            font: inherit;
            color: inherit;
          }

          /* Override built-in autofill styles */
          ${e} > input[slot='input']:autofill {
            -webkit-text-fill-color: var(--vaadin-input-field-autofill-color, black) !important;
            background-clip: text !important;
          }

          ${e}:has(> input[slot='input']:autofill)::part(input-field) {
            --vaadin-input-field-background: var(--vaadin-input-field-autofill-background, lightyellow) !important;
            --vaadin-input-field-value-color: var(--vaadin-input-field-autofill-color, black) !important;
            --vaadin-input-field-button-text-color: var(--vaadin-input-field-autofill-color, black) !important;
          }
        `]}_onFocus(e){super._onFocus(e),this.autoselect&&this.inputElement&&this.inputElement.select()}_addInputListeners(e){super._addInputListeners(e),e.addEventListener("paste",this._boundOnPaste),e.addEventListener("drop",this._boundOnDrop),e.addEventListener("beforeinput",this._boundOnBeforeInput)}_removeInputListeners(e){super._removeInputListeners(e),e.removeEventListener("paste",this._boundOnPaste),e.removeEventListener("drop",this._boundOnDrop),e.removeEventListener("beforeinput",this._boundOnBeforeInput)}_onKeyDown(e){super._onKeyDown(e),this.allowedCharPattern&&!this.__shouldAcceptKey(e)&&e.target===this.inputElement&&(e.preventDefault(),this._markInputPrevented())}_markInputPrevented(){this.setAttribute("input-prevented",""),this._preventInputDebouncer=I.debounce(this._preventInputDebouncer,K.after(200),()=>{this.removeAttribute("input-prevented")})}__shouldAcceptKey(e){return e.metaKey||e.ctrlKey||!e.key||e.key.length!==1||this.__allowedCharRegExp.test(e.key)}_onPaste(e){if(this.allowedCharPattern){let i=e.clipboardData.getData("text");this.__allowedTextRegExp.test(i)||(e.preventDefault(),this._markInputPrevented())}}_onDrop(e){if(this.allowedCharPattern){let i=e.dataTransfer.getData("text");this.__allowedTextRegExp.test(i)||(e.preventDefault(),this._markInputPrevented())}}_onBeforeInput(e){this.allowedCharPattern&&e.data&&!this.__allowedTextRegExp.test(e.data)&&(e.preventDefault(),this._markInputPrevented())}_allowedCharPatternChanged(e){if(e)try{this.__allowedCharRegExp=new RegExp(`^${e}$`,"u"),this.__allowedTextRegExp=new RegExp(`^${e}*$`,"u")}catch(i){console.error(i)}}};var te=class extends P{constructor(t,e,i={}){let{uniqueIdPrefix:r}=i;super(t,"input","input",{initializer:(o,n)=>{n.value&&(o.value=n.value),n.type&&o.setAttribute("type",n.type),o.id=this.defaultId,typeof e=="function"&&e(o)},useUniqueId:!0,uniqueIdPrefix:r})}};var ie=class{constructor(t,e){this.input=t,this.__preventDuplicateLabelClick=this.__preventDuplicateLabelClick.bind(this),e.addEventListener("slot-content-changed",i=>{this.__initLabel(i.detail.node)}),this.__initLabel(e.node)}__initLabel(t){t&&(t.addEventListener("click",this.__preventDuplicateLabelClick),this.input&&t.setAttribute("for",this.input.id))}__preventDuplicateLabelClick(){let t=e=>{e.stopImmediatePropagation(),this.input.removeEventListener("click",t)};this.input.addEventListener("click",t)}};var ys=s=>class extends tt(s){static get properties(){return{pattern:{type:String}}}static get delegateAttrs(){return[...super.delegateAttrs,"pattern"]}static get constraints(){return[...super.constraints,"pattern"]}};var xs=u`
  [part$='button'] {
    color: var(--vaadin-input-field-button-text-color, var(--vaadin-text-color-secondary));
    cursor: var(--vaadin-clickable-cursor);
    touch-action: manipulation;
    -webkit-tap-highlight-color: transparent;
    -webkit-user-select: none;
    user-select: none;
    /* Ensure minimum click target (WCAG) */
    padding: max(0px, (24px - var(--vaadin-icon-size, 1lh)) / 2);
    margin: min(0px, (24px - var(--vaadin-icon-size, 1lh)) / -2);
  }

  /* Icon */
  [part$='button']::before {
    background: currentColor;
    content: '';
    display: block;
    height: var(--vaadin-icon-size, 1lh);
    width: var(--vaadin-icon-size, 1lh);
    mask-size: var(--vaadin-icon-visual-size, 100%);
    mask-position: 50%;
    mask-repeat: no-repeat;
  }

  :host(:is(:not([clear-button-visible][has-value]), [disabled], [readonly])) [part~='clear-button'] {
    display: none;
  }

  [part~='clear-button']::before {
    mask-image: var(--_vaadin-icon-cross);
  }

  :host(:is([readonly], [disabled])) [part$='button'] {
    color: var(--vaadin-text-color-disabled);
    cursor: var(--vaadin-disabled-cursor);
  }

  @media (forced-colors: active) {
    [part$='button']::before {
      background: CanvasText;
    }

    :host([disabled]) [part$='button'] {
      color: GrayText;
    }

    :host([disabled]) [part$='button']::before {
      background: GrayText;
    }
  }
`;var Cs=u`
  :host {
    --_helper-below-field: initial;
    --_helper-above-field: ;
    --_no-label: initial;
    --_has-label: ;
    --_no-helper: initial;
    --_has-helper: ;
    --_no-error: initial;
    --_has-error: ;
    --_gap: var(--vaadin-input-field-container-gap, var(--vaadin-gap-xs));
    --_gap-s: round(var(--_gap) / 3, 2px);
    display: inline-grid;
    grid-template:
      'label' auto var(--_helper-above-field, 'helper' auto) 'baseline' 0 'input' 1fr var(
        --_helper-below-field,
        'helper' auto
      )
      'error' auto / 100%;
    height: fit-content;
    outline: none;
    cursor: default;
    -webkit-tap-highlight-color: transparent;
  }

  :host([has-label]) {
    --_has-label: initial;
    --_no-label: ;
  }

  :host([has-helper]) {
    --_has-helper: initial;
    --_no-helper: ;
  }

  :host([has-error-message]) {
    --_has-error: initial;
    --_no-error: ;
  }

  :host([hidden]) {
    display: none !important;
  }

  :host(:not([has-label])) [part='label'],
  :host(:not([has-helper])) [part='helper-text'],
  :host(:not([has-error-message])) [part='error-message'] {
    display: none;
  }

  /* Baseline alignment guide */
  :host::before {
    content: '\\2003' / '';
    grid-column: 1;
    grid-row: var(--_has-label, label / baseline) var(--_no-label, label / input);
    align-self: var(--_has-label, end) var(--_no-label, start);
    font-size: var(--vaadin-input-field-value-font-size, inherit);
    line-height: var(--vaadin-input-field-value-line-height, inherit);
    padding: var(
      --vaadin-input-field-padding,
      var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container)
    );
    border: var(--vaadin-input-field-border-width, 1px) solid transparent;
    pointer-events: none;
    margin-bottom: var(--_no-label, 0)
      var(
        --_has-label,
        calc(
          var(
              --vaadin-field-baseline-input-height,
              (1lh + var(--vaadin-padding-block-container) * 2 + var(--vaadin-input-field-border-width, 1px) * 2)
            ) *
            -1
        )
      );
  }

  [class$='container'] {
    display: contents;
  }

  [part] {
    grid-column: 1;
  }

  [part='label'] {
    font-size: var(--vaadin-input-field-label-font-size, inherit);
    line-height: var(--vaadin-input-field-label-line-height, inherit);
    font-weight: var(--vaadin-input-field-label-font-weight, 500);
    color: var(--vaadin-input-field-label-color, var(--vaadin-text-color));
    word-break: break-word;
    position: relative;
    grid-area: label;
    margin-bottom: var(--_helper-below-field, var(--_gap)) var(--_helper-above-field, var(--_no-helper, var(--_gap)));
  }

  ::slotted(label) {
    cursor: inherit;
  }

  :host([disabled]) [part='label'],
  :host([disabled]) ::slotted(label) {
    opacity: 0.5;
  }

  :host([disabled]) [part='label'] ::slotted(label) {
    opacity: 1;
  }

  :host([required]) [part='label'] {
    padding-inline-end: 1em;
  }

  [part='required-indicator'] {
    display: inline-block;
    position: absolute;
    width: 1em;
    text-align: center;
    color: var(--vaadin-input-field-required-indicator-color, var(--vaadin-text-color-secondary));
  }

  [part='required-indicator']::after {
    content: var(--vaadin-input-field-required-indicator, '*');
  }

  :host(:not([required])) [part='required-indicator'] {
    display: none;
  }

  [part='label'],
  [part='helper-text'],
  [part='error-message'] {
    width: min-content;
    min-width: 100%;
    box-sizing: border-box;
  }

  [part='input-field'],
  [part='group-field'],
  [part='input-fields'] {
    grid-area: input;
  }

  [part='input-field'] {
    width: var(--vaadin-field-default-width, 12em);
    max-width: 100%;
    min-width: 100%;
  }

  :host([readonly]) [part='input-field'] {
    cursor: default;
  }

  :host([disabled]) [part='input-field'] {
    cursor: var(--vaadin-disabled-cursor);
  }

  [part='helper-text'] {
    font-size: var(--vaadin-input-field-helper-font-size, inherit);
    line-height: var(--vaadin-input-field-helper-line-height, inherit);
    font-weight: var(--vaadin-input-field-helper-font-weight, 400);
    color: var(--vaadin-input-field-helper-color, var(--vaadin-text-color-secondary));
    grid-area: helper;
    margin-top: var(--_helper-above-field, var(--_gap-s)) var(--_helper-below-field, var(--_gap));
    margin-bottom: var(--_helper-above-field, var(--_gap));
  }

  [part='error-message'] {
    font-size: var(--vaadin-input-field-error-font-size, inherit);
    line-height: var(--vaadin-input-field-error-line-height, inherit);
    font-weight: var(--vaadin-input-field-error-font-weight, 400);
    color: var(--vaadin-input-field-error-color, var(--vaadin-text-color));
    display: flex;
    gap: var(--vaadin-gap-xs);
    grid-area: error;
    margin-top: var(--_has-helper, var(--_helper-below-field, var(--_gap-s)) var(--_helper-above-field, var(--_gap)))
      var(--_no-helper, var(--_gap));
  }

  [part='error-message']::before {
    content: '';
    display: inline-block;
    flex: none;
    width: var(--vaadin-icon-size, 1lh);
    height: var(--vaadin-icon-size, 1lh);
    mask: var(--_vaadin-icon-warn) 50% / var(--vaadin-icon-visual-size, 100%) no-repeat;
    background: currentColor;
  }

  :host([theme~='helper-above-field']) {
    --_helper-above-field: initial;
    --_helper-below-field: ;
  }

  @media (forced-colors: active) {
    [part='error-message']::before {
      background: CanvasText;
    }
  }
`;var st=[Cs,xs];var rt=u`
  :host([opened]) {
    pointer-events: auto;
  }

  [part~='toggle-button']::before {
    mask-image: var(--_vaadin-icon-chevron-down);
  }

  :host([readonly]) [part~='toggle-button'] {
    display: none;
  }
`;var ot=class s{context;items=[];pendingRequests={};#t={};#s;#e=0;#i=0;constructor(t,e,i,r,o){this.context=t,this.pageSize=e,this.size=i,this.parentCache=r,this.parentCacheIndex=o,this.#i=i||0}get parentItem(){return this.parentCache&&this.parentCache.items[this.parentCacheIndex]}get subCaches(){return Object.values(this.#t)}get isLoading(){return Object.keys(this.pendingRequests).length>0?!0:this.subCaches.some(t=>t.isLoading)}get flatSize(){return this.#i}get pageSize(){return this.#s}set pageSize(t){this.#s=t,this.pendingRequests={},this.subCaches.forEach(e=>{e.pageSize=t})}get size(){return this.#e}set size(t){if(this.#e!==t){if(this.#e=t,this.context.placeholder!==void 0){this.items.length=t||0;for(let i=0;i<t;i++)this.items[i]||=this.context.placeholder}this.items.length>t&&(this.items.length=t||0),Object.keys(this.pendingRequests).forEach(i=>{parseInt(i)*this.pageSize>=this.size&&delete this.pendingRequests[i]})}}recalculateFlatSize(){this.#i=!this.parentItem||this.context.isExpanded(this.parentItem)?this.size+this.subCaches.reduce((t,e)=>(e.recalculateFlatSize(),t+e.flatSize),0):0}setPage(t,e){let i=t*this.pageSize;e.forEach((r,o)=>{let n=i+o;(this.size===void 0||n<this.size)&&(this.items[n]=r)})}getSubCache(t){return this.#t[t]}removeSubCache(t){delete this.#t[t]}removeSubCaches(){this.#t={}}createSubCache(t){let e=new s(this.context,this.pageSize,0,this,t);return this.#t[t]=e,e}getFlatIndex(t){let e=Math.max(0,Math.min(this.size-1,t));return this.subCaches.reduce((i,r)=>{let o=r.parentCacheIndex;return e>o?i+r.flatSize:i},e)}};function Jt(s,t,e=0){let i=t;for(let r of s.subCaches){let o=r.parentCacheIndex;if(i<=o)break;if(i<=o+r.flatSize)return Jt(r,i-o-1,e+1);i-=r.flatSize}return{cache:s,item:s.items[i],index:i,page:Math.floor(i/s.pageSize),level:e}}function ei({getItemId:s},t,e,i=0,r=0){for(let o=0;o<t.items.length;o++){let n=t.items[o];if(n&&s(n)===s(e))return{cache:t,level:i,item:n,index:o,page:Math.floor(o/t.pageSize),subCache:t.getSubCache(o),flatIndex:r+t.getFlatIndex(o)}}for(let o of t.subCaches){let n=r+t.getFlatIndex(o.parentCacheIndex),l=ei({getItemId:s},o,e,i+1,n+1);if(l)return l}}function ti(s,[t,...e],i=0){t===1/0&&(t=s.size-1);let r=s.getFlatIndex(t),o=s.getSubCache(t);return o?.flatSize>0&&e.length?ti(o,e,i+r+1):i+r}var nt=class extends EventTarget{host;dataProvider;dataProviderParams;isExpanded;getItemId;rootCache;placeholder;isPlaceholder;constructor(t,{size:e,pageSize:i,isExpanded:r,getItemId:o,isPlaceholder:n,placeholder:l,dataProvider:a,dataProviderParams:d}){super(),this.host=t,this.getItemId=o,this.isExpanded=r,this.placeholder=l,this.isPlaceholder=n,this.dataProvider=a,this.dataProviderParams=d,this.rootCache=this.#s(i,e)}get flatSize(){return this.rootCache.flatSize}get pageSize(){return this.rootCache.pageSize}get#t(){return{isExpanded:this.isExpanded,placeholder:this.placeholder}}isLoading(){return this.rootCache.isLoading}setPageSize(t){this.rootCache.pageSize=t}setDataProvider(t){this.dataProvider=t}recalculateFlatSize(){this.rootCache.recalculateFlatSize()}clearCache(){this.rootCache=this.#s(this.rootCache.pageSize,this.rootCache.size)}getFlatIndexContext(t){return Jt(this.rootCache,t)}getItemContext(t){return ei({getItemId:this.getItemId},this.rootCache,t)}getFlatIndexByPath(t){return ti(this.rootCache,t)}ensureFlatIndexLoaded(t){let{cache:e,page:i,item:r}=this.getFlatIndexContext(t);this.#i(r)||this.#e(e,i)}ensureFlatIndexHierarchy(t){let{cache:e,item:i,index:r}=this.getFlatIndexContext(t);if(this.#i(i)&&this.isExpanded(i)&&!e.getSubCache(r)){let o=e.createSubCache(r);this.#e(o,0)}}loadFirstPage(){this.#e(this.rootCache,0)}_shouldLoadCachePage(t,e){return!0}#s(t,e){return new ot(this.#t,t,e)}#e(t,e){if(!this.dataProvider||t.pendingRequests[e]||!this._shouldLoadCachePage(t,e))return;let i={page:e,pageSize:t.pageSize,parentItem:t.parentItem};this.dataProviderParams&&(i={...i,...this.dataProviderParams()});let r=(o,n)=>{t.pendingRequests[e]===r&&(n!==void 0?t.size=n:i.parentItem&&(t.size=o.length),t.setPage(e,o),this.recalculateFlatSize(),this.dispatchEvent(new CustomEvent("page-received")),delete t.pendingRequests[e],this.dispatchEvent(new CustomEvent("page-loaded")))};t.pendingRequests[e]=r,this.dispatchEvent(new CustomEvent("page-requested")),this.dataProvider(i,r)}#i(t){return this.isPlaceholder?!this.isPlaceholder(t):this.placeholder?t!==this.placeholder:!!t}};var at=s=>class extends s{static get properties(){return{pageSize:{type:Number,value:50,observer:"_pageSizeChanged",sync:!0},size:{type:Number,observer:"_sizeChanged",sync:!0},dataProvider:{type:Object,observer:"_dataProviderChanged",sync:!0}}}static get observers(){return["_dataProviderFilterChanged(filter)","_ensureFirstPage(opened)"]}constructor(){super(),this.__dataProviderInitialized=!1,this.__previousDataProviderFilter,this.__dataProviderController=new nt(this,{placeholder:new b,isPlaceholder:e=>e instanceof b,dataProviderParams:()=>({filter:this.filter})}),this.__dataProviderController.addEventListener("page-requested",this.__onDataProviderPageRequested.bind(this)),this.__dataProviderController.addEventListener("page-loaded",this.__onDataProviderPageLoaded.bind(this))}ready(){super.ready(),this._scroller.addEventListener("index-requested",e=>{if(!this._shouldFetchData())return;let i=e.detail.index;i!==void 0&&this.__dataProviderController.ensureFlatIndexLoaded(i)}),this.__dataProviderInitialized=!0,this.dataProvider&&this.__synchronizeControllerState()}_dataProviderFilterChanged(e){if(this.__previousDataProviderFilter===void 0&&e===""){this.__previousDataProviderFilter=e;return}this.__previousDataProviderFilter!==e&&(this.__previousDataProviderFilter=e,this.__keepOverlayOpened=!0,this.size=void 0,this.clearCache(),this.__keepOverlayOpened=!1)}_shouldFetchData(){return this.dataProvider?this.opened||this.filter&&this.filter.length:!1}_ensureFirstPage(e){!this._shouldFetchData()||!e||(this._forceNextRequest||this.size===void 0?(this._forceNextRequest=!1,this.__dataProviderController.loadFirstPage()):this.size>0&&this.__dataProviderController.ensureFlatIndexLoaded(0))}__onDataProviderPageRequested(){this.loading=!0}__onDataProviderPageLoaded(){let{rootCache:e}=this.__dataProviderController;e.items=[...e.items],this.__synchronizeControllerState(),!this.opened&&!this._isInputFocused()&&this._commitValue()}clearCache(){this.dataProvider&&(this.__dataProviderController.clearCache(),this.__synchronizeControllerState(),this._shouldFetchData()?(this._forceNextRequest=!1,this.__dataProviderController.loadFirstPage()):this._forceNextRequest=!0)}_sizeChanged(e){let{rootCache:i}=this.__dataProviderController;i.size!==e&&(i.size=e,i.items=[...i.items],this.__synchronizeControllerState())}_filteredItemsChanged(e){if(super._filteredItemsChanged(e),this.dataProvider&&e){let{rootCache:i}=this.__dataProviderController;i.items!==e&&(i.items=e,this.__synchronizeControllerState())}}__synchronizeControllerState(){if(this.__dataProviderInitialized&&this.dataProvider){let{rootCache:e}=this.__dataProviderController;this.size=e.size,this.filteredItems=e.items,this.loading=this.__dataProviderController.isLoading()}}_pageSizeChanged(e,i){if(Math.floor(e)!==e||e<1)throw this.pageSize=i,new Error("`pageSize` value must be an integer > 0");this.__dataProviderController.setPageSize(e),this.clearCache()}_dataProviderChanged(e,i){this._ensureItemsOrDataProvider(()=>{this.dataProvider=i}),this.__dataProviderController.setDataProvider(e),this.clearCache()}_ensureItemsOrDataProvider(e){if(this.items!==void 0&&this.dataProvider!==void 0)throw e(),new Error("Using `items` and `dataProvider` together is not supported")}};var lt=s=>class extends s{static get observers(){return["__clearPendingFocusOnFilter(filter)"]}__focusIndex(e){if(!(typeof e!="number"||Number.isNaN(e)||e<0)){if(!this._overlayOpened||!this._dropdownItems||this._dropdownItems.length===0){this.__pendingFocusIndex=e;return}if(!(e>=this._dropdownItems.length)){if(this._focusedIndex=e,this._scrollIntoView(e,!0),this.loading){this.__pendingFocusIndex=e;return}delete this.__pendingFocusIndex,requestAnimationFrame(()=>{this.isConnected&&this._updateActiveDescendant(e)})}}}__focusPendingIndexIfNeeded(){this.__pendingFocusIndex!==void 0&&!this.loading&&this.__focusIndex(this.__pendingFocusIndex)}__clearPendingFocusOnFilter(){delete this.__pendingFocusIndex}_onOpened(){super._onOpened(),this.__focusPendingIndexIfNeeded()}__onDataProviderPageLoaded(){super.__onDataProviderPageLoaded(),this.__focusPendingIndexIfNeeded()}};var ws=s=>class extends Q(X(Ke(Ge(s)))){static get properties(){return{opened:{type:Boolean,notify:!0,value:!1,reflectToAttribute:!0,sync:!0,observer:"_openedChanged"},autoOpenDisabled:{type:Boolean,sync:!0},readonly:{type:Boolean,value:!1,reflectToAttribute:!0},_focusedIndex:{type:Number,observer:"_focusedIndexChanged",value:-1,sync:!0},_toggleElement:{type:Object,observer:"_toggleElementChanged"},_dropdownItems:{type:Array,sync:!0},_overlayOpened:{type:Boolean,sync:!0,observer:"_overlayOpenedChanged"}}}constructor(){super(),this._scroller,this._closeOnBlurIsPrevented,this._boundOverlaySelectedItemChanged=this._overlaySelectedItemChanged.bind(this),this._boundOnClearButtonMouseDown=this.__onClearButtonMouseDown.bind(this),this._boundOnClick=this._onClick.bind(this),this._boundOnOverlayTouchAction=this._onOverlayTouchAction.bind(this),this._boundOnTouchend=this._onTouchend.bind(this)}get _tagNamePrefix(){return"vaadin-combo-box"}_inputElementChanged(e){super._inputElementChanged(e),e&&(e.autocomplete="off",e.autocapitalize="off",e.setAttribute("role","combobox"),e.setAttribute("aria-autocomplete","list"),e.setAttribute("aria-expanded",!!this.opened),e.setAttribute("spellcheck","false"),e.setAttribute("autocorrect","off"))}firstUpdated(){super.firstUpdated(),this._initScroller()}ready(){super.ready(),this._initOverlay(),this.addEventListener("click",this._boundOnClick),this.addEventListener("touchend",this._boundOnTouchend),this.clearElement&&this.clearElement.addEventListener("mousedown",this._boundOnClearButtonMouseDown)}disconnectedCallback(){super.disconnectedCallback(),this.close()}open(){!this.disabled&&!this.readonly&&(this.opened=!0)}close(){this.opened=!1}_initOverlay(){let e=this.$.overlay;e.addEventListener("touchend",this._boundOnOverlayTouchAction),e.addEventListener("touchmove",this._boundOnOverlayTouchAction),e.addEventListener("mousedown",i=>i.preventDefault()),e.addEventListener("opened-changed",i=>{this._overlayOpened=i.detail.value}),e.addEventListener("vaadin-overlay-closed",()=>{this._scroller.items=[],this._onOverlayClosed()}),this._overlayElement=e}_initScroller(){let e=document.createElement(`${this._tagNamePrefix}-scroller`);e.owner=this,e.getItemLabel=this._getItemLabel.bind(this),e.addEventListener("selection-changed",this._boundOverlaySelectedItemChanged),this._renderScroller(e),this._scroller=e}_renderScroller(e){e.setAttribute("slot","overlay"),e.setAttribute("tabindex","-1"),this.appendChild(e)}get _hasDropdownItems(){return!!(this._dropdownItems&&this._dropdownItems.length)}_overlayOpenedChanged(e,i){e?this._onOpened():i&&this._hasDropdownItems&&this.close()}_focusedIndexChanged(e,i){i!==void 0&&this._updateActiveDescendant(e)}_isInputFocused(){return this.inputElement&&W(this.inputElement)}_updateActiveDescendant(e){let i=this.inputElement;if(!i)return;let r=this._getItemElements().find(o=>o.index===e);r?i.setAttribute("aria-activedescendant",r.id):i.removeAttribute("aria-activedescendant")}_openedChanged(e,i){if(i===void 0)return;e?!this._isInputFocused()&&!he&&this.inputElement&&this.inputElement.focus():(this.autoselect&&(this.__autoselectPending=!0),this._onClosed());let r=this.inputElement;r&&(r.setAttribute("aria-expanded",!!e),e?r.setAttribute("aria-controls",this._scroller.id):r.removeAttribute("aria-controls"))}_onOverlayTouchAction(){this._closeOnBlurIsPrevented=!0,this.inputElement.blur(),this._closeOnBlurIsPrevented=!1}_isClearButton(e){return e.composedPath()[0]===this.clearElement}__onClearButtonMouseDown(e){e.preventDefault(),this.inputElement.focus()}_onClearButtonClick(e){e.preventDefault(),this._onClearAction()}_onToggleButtonClick(e){e.preventDefault(),this.opened?this.close():this.open()}_onHostClick(e){this.autoOpenDisabled||(e.preventDefault(),this.open())}_onClick(e){this.autoselect&&this.inputElement&&this.__autoselectPending&&(this.inputElement.selectionStart!==this.inputElement.selectionEnd||this.inputElement.select()),this.__autoselectPending=!1,this._isClearButton(e)?this._onClearButtonClick(e):e.composedPath().includes(this._toggleElement)?this._onToggleButtonClick(e):this._onHostClick(e)}_onTouchend(e){!this.clearElement||e.composedPath()[0]!==this.clearElement||(e.preventDefault(),this._onClearAction())}_onKeyDown(e){super._onKeyDown(e),e.key==="ArrowDown"?(this._onArrowDown(),e.preventDefault()):e.key==="ArrowUp"&&(this._onArrowUp(),e.preventDefault())}_getItemLabel(e){return e?e.toString():""}_onArrowDown(){if(this.opened){let e=this._dropdownItems;e&&(this._focusedIndex=Math.min(e.length-1,this._focusedIndex+1),this._prefillFocusedItemLabel())}else this.open()}_onArrowUp(){if(this.opened){if(this._focusedIndex>-1)this._focusedIndex=Math.max(0,this._focusedIndex-1);else{let e=this._dropdownItems;e&&(this._focusedIndex=e.length-1)}this._prefillFocusedItemLabel()}else this.open()}_prefillFocusedItemLabel(){if(this._focusedIndex>-1){let e=this._dropdownItems[this._focusedIndex];this._inputElementValue=this._getItemLabel(e),this._markAllSelectionRange()}}_setSelectionRange(e,i){this._isInputFocused()&&this.inputElement.setSelectionRange&&this.inputElement.setSelectionRange(e,i)}_markAllSelectionRange(){this._inputElementValue!==void 0&&this._setSelectionRange(0,this._inputElementValue.length)}_clearSelectionRange(){if(this._inputElementValue!==void 0){let e=this._inputElementValue?this._inputElementValue.length:0;this._setSelectionRange(e,e)}}_closeOrCommit(){this.opened?this.close():this._commitValue()}_onEnter(e){if(!this._hasValidInputValue()){e.preventDefault(),e.stopPropagation();return}this.opened&&(e.preventDefault(),e.stopPropagation()),this._closeOrCommit()}_hasValidInputValue(){return!0}_onEscape(e){this.autoOpenDisabled&&(this.opened||this.value!==this._inputElementValue&&this._inputElementValue.length>0)?(e.stopPropagation(),this._focusedIndex=-1,this._onEscapeCancel()):this.opened?(e.stopPropagation(),this._focusedIndex>-1?(this._focusedIndex=-1,this._revertInputValue()):this._onEscapeCancel()):this.clearButtonVisible&&this.value&&!this.readonly&&(e.stopPropagation(),this._onClearAction())}_onEscapeCancel(){}_toggleElementChanged(e){e&&(e.addEventListener("mousedown",i=>i.preventDefault()),e.addEventListener("click",()=>{he&&!this._isInputFocused()&&document.activeElement.blur()}))}_onClearAction(){}_onOpened(){}_onClosed(){}_onOverlayClosed(){}_commitValue(){}_revertInputValue(){this._inputElementValue=this.value,this._clearSelectionRange()}_onInput(e){!this.opened&&!this._isClearButton(e)&&!this.autoOpenDisabled&&(this.opened=!0)}_getItemElements(){return Array.from(this._scroller.querySelectorAll(`${this._tagNamePrefix}-item`))}_scrollIntoView(e,i=!1){this._scroller&&this._scroller.scrollIntoView(e,i)}_overlaySelectedItemChanged(e){e.stopPropagation(),!this.hasAttribute("closing")&&(e.detail.item instanceof b||this.opened&&(this._focusedIndex=this._dropdownItems.indexOf(e.detail.item),this.close()))}_setFocused(e){super._setFocused(e),e||(this.__autoselectPending=!1),!e&&!this.readonly&&!this._closeOnBlurIsPrevented&&this._handleFocusOut()}_handleFocusOut(){if(L()){this._closeOrCommit();return}this.opened?this._overlayOpened||this.close():this._commitValue()}_shouldRemoveFocus(e){return e.relatedTarget&&e.relatedTarget.localName===`${this._tagNamePrefix}-item`?!1:e.relatedTarget===this._overlayElement?(e.composedPath()[0].focus(),!1):!0}};function Br(s){return s!=null}function Is(s,t){return s.findIndex(e=>e instanceof b?!1:t(e))}var dt=s=>class extends ws(s){static get properties(){return{items:{type:Array,sync:!0,observer:"_itemsChanged"},filteredItems:{type:Array,observer:"_filteredItemsChanged",sync:!0},filter:{type:String,value:"",notify:!0,sync:!0},itemLabelGenerator:{type:Object},itemLabelPath:{type:String,value:"label",observer:"_itemLabelPathChanged",sync:!0},itemValuePath:{type:String,value:"value",sync:!0}}}updated(e){super.updated(e),e.has("filter")&&this._filterChanged(this.filter),e.has("itemLabelGenerator")&&this.requestContentUpdate()}_onInput(e){let i=this._inputElementValue,r={};this.filter===i?this._filterChanged(this.filter):r.filter=i,!this.opened&&!this._isClearButton(e)&&!this.autoOpenDisabled&&(r.opened=!0),this.setProperties(r)}_getItemLabel(e){if(typeof this.itemLabelGenerator=="function"&&e)return this.itemLabelGenerator(e)||"";let i=e&&this.itemLabelPath?V(this.itemLabelPath,e):void 0;return i==null&&(i=e?e.toString():""),i}_getItemValue(e){let i=e&&this.itemValuePath?V(this.itemValuePath,e):void 0;return i===void 0&&(i=e?e.toString():""),i}_itemLabelPathChanged(e){typeof e!="string"&&console.error("You should set itemLabelPath to a valid string")}_filterChanged(e){this._scrollIntoView(0),this._focusedIndex=-1,this.items?this.filteredItems=this._filterItems(this.items,e):this._filteredItemsChanged(this.filteredItems)}_itemsChanged(e,i){this._ensureItemsOrDataProvider(()=>{this.items=i}),e?this.filteredItems=e.slice(0):i&&(this.filteredItems=null)}_filteredItemsChanged(e){this._setDropdownItems(e)}_setDropdownItems(){}_filterItems(e,i){return e&&e.filter(o=>(i=i?i.toString().toLowerCase():"",this._getItemLabel(o).toString().toLowerCase().indexOf(i)>-1))}__getItemIndexByValue(e,i){return!e||!Br(i)?-1:Is(e,r=>this._getItemValue(r)===i)}__getItemIndexByLabel(e,i){return!e||!i?-1:Is(e,r=>this._getItemLabel(r).toString().toLowerCase()===i.toString().toLowerCase())}};function Fr(s){return s!=null}var Ss=s=>class extends ee(dt(s)){static get properties(){return{renderer:{type:Object,sync:!0},allowCustomValue:{type:Boolean,value:!1},loading:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},selectedItem:{type:Object,notify:!0,sync:!0},itemClassNameGenerator:{type:Object},itemIdPath:{type:String,sync:!0},__keepOverlayOpened:{type:Boolean,sync:!0}}}static get observers(){return["_openedOrItemsChanged(opened, _dropdownItems, loading, __keepOverlayOpened)","_selectedItemChanged(selectedItem, itemValuePath, itemLabelPath)","_updateScroller(opened, _dropdownItems, _focusedIndex, _theme)"]}ready(){super.ready(),this._lastCommittedValue=this.value}requestContentUpdate(){this._scroller&&(this._scroller.requestContentUpdate(),this._getItemElements().forEach(e=>{e.requestContentUpdate()}))}updated(e){super.updated(e),["loading","itemIdPath","itemClassNameGenerator","renderer","selectedItem"].forEach(i=>{e.has(i)&&(this._scroller[i]=this[i])})}_updateScroller(e,i,r,o){e&&(this._scroller.style.maxHeight=getComputedStyle(this).getPropertyValue(`--${this._tagNamePrefix}-overlay-max-height`)||"65vh");let n=this.hasAttribute("closing");this._scroller.setProperties({items:e||n?i:[],opened:e,focusedIndex:r,theme:o})}_openedOrItemsChanged(e,i,r,o){this._overlayOpened=e&&(o||r||!!i?.length)}_onClearButtonClick(e){super._onClearButtonClick(e),this.opened&&this.requestContentUpdate()}_inputElementChanged(e){super._inputElementChanged(e),e&&this._revertInputValueToValue()}_closeOrCommit(){!this.opened&&!this.loading?this._commitValue():this.close()}_hasValidInputValue(){let e=this._focusedIndex<0&&this._inputElementValue!==""&&this._getItemLabel(this.selectedItem)!==this._inputElementValue;return this.allowCustomValue||!e}_onEscapeCancel(){this.cancel()}_onClearAction(){this.selectedItem=null,this.allowCustomValue&&(this.value=""),this._detectAndDispatchChange()}_clearFilter(){this.filter=""}cancel(){this._revertInputValueToValue(),this._lastCommittedValue=this.value,this._closeOrCommit()}_onOpened(){this.dispatchEvent(new CustomEvent("vaadin-combo-box-dropdown-opened",{bubbles:!0,composed:!0})),this._lastCommittedValue=this.value}_onOverlayClosed(){this.dispatchEvent(new CustomEvent("vaadin-combo-box-dropdown-closed",{bubbles:!0,composed:!0}))}_onClosed(){(!this.loading||this.allowCustomValue)&&this._commitValue()}_commitValue(){if(this._focusedIndex>-1){let e=this._dropdownItems[this._focusedIndex];this.selectedItem!==e&&(this.selectedItem=e),this._inputElementValue=this._getItemLabel(this.selectedItem),this._focusedIndex=-1}else if(this._inputElementValue===""||this._inputElementValue===void 0)this.selectedItem=null,this.allowCustomValue&&(this.value="");else{let e=[this.selectedItem,...this._dropdownItems||[]],i=e[this.__getItemIndexByLabel(e,this._inputElementValue)];if(this.allowCustomValue&&!i){let r=this._inputElementValue;this._lastCustomValue=r;let o=new CustomEvent("custom-value-set",{detail:r,composed:!0,cancelable:!0,bubbles:!0});this.dispatchEvent(o),o.defaultPrevented||(this.value=r)}else!this.allowCustomValue&&!this.opened&&i?this.value=this._getItemValue(i):this._revertInputValueToValue()}this._detectAndDispatchChange(),this._clearSelectionRange(),this._clearFilter()}_onChange(e){e.stopPropagation()}_revertInputValue(){this.filter!==""?this._inputElementValue=this.filter:this._revertInputValueToValue(),this._clearSelectionRange()}_revertInputValueToValue(){this.allowCustomValue&&!this.selectedItem?this._inputElementValue=this.value:this._inputElementValue=this._getItemLabel(this.selectedItem)}_selectedItemChanged(e){if(e==null)this.filteredItems&&(this.allowCustomValue||(this.value=""),this._toggleHasValue(this._hasValue),this._inputElementValue=this.value);else{let i=this._getItemValue(e);if(this.value!==i&&(this.value=i,this.value!==i))return;this._toggleHasValue(!0),this._inputElementValue=this._getItemLabel(e)}}_valueChanged(e,i){e===""&&i===void 0||(Fr(e)?(this._getItemValue(this.selectedItem)!==e&&this._selectItemForValue(e),!this.selectedItem&&this.allowCustomValue&&(this._inputElementValue=e),this._toggleHasValue(this._hasValue)):this.selectedItem=null,this._clearFilter(),this._lastCommittedValue=void 0)}_detectAndDispatchChange(){document.hasFocus()&&this._requestValidation(),this.value!==this._lastCommittedValue&&(this.dispatchEvent(new CustomEvent("change",{bubbles:!0})),this._lastCommittedValue=this.value)}_selectItemForValue(e){let i=this.__getItemIndexByValue(this.filteredItems,e),r=this.selectedItem;i>=0?this.selectedItem=this.filteredItems[i]:this.dataProvider&&this.selectedItem===void 0?this.selectedItem=void 0:this.selectedItem=null,this.selectedItem===null&&r===null&&this._selectedItemChanged(this.selectedItem)}_setDropdownItems(e){let i=this._dropdownItems;this._dropdownItems=e;let r=i?i[this._focusedIndex]:null,o=this.__getItemIndexByValue(e,this.value);if((this.selectedItem===null||this.selectedItem===void 0)&&o>=0&&(this.selectedItem=e[o]),i&&i[this._focusedIndex]instanceof b&&e[this._focusedIndex]instanceof b)return;let n=this.__getItemIndexByValue(e,this._getItemValue(r));n>-1?this._focusedIndex=n:this._focusedIndex=this.__getItemIndexByLabel(e,this.filter)}_handleFocusOut(){if(!this.opened&&this.allowCustomValue&&this._inputElementValue===this._lastCustomValue){delete this._lastCustomValue;return}super._handleFocusOut()}};var ii=class extends lt(at(Ss(ys(it(C(qe(g(x(p))))))))){static get is(){return"vaadin-combo-box"}static get styles(){return[st,rt]}static get properties(){return{_positionTarget:{type:Object}}}get clearElement(){return this.$.clearButton}render(){return f`
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
          theme="${Z(this._theme)}"
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
        theme="${Z(this._theme)}"
        .positionTarget="${this._positionTarget}"
        no-vertical-overlap
      >
        <slot name="overlay"></slot>
      </vaadin-combo-box-overlay>
    `}ready(){super.ready(),this.addController(new te(this,t=>{this._setInputElement(t),this._setFocusElement(t),this.stateTarget=t,this.ariaTarget=t})),this.addController(new ie(this.inputElement,this._labelController)),this._tooltipController=new Y(this),this.addController(this._tooltipController),this._tooltipController.setPosition("top"),this._tooltipController.setAriaTarget(this.inputElement),this._tooltipController.setShouldShow(t=>!t.opened),this._positionTarget=this.shadowRoot.querySelector('[part="input-field"]'),this._toggleElement=this.$.toggleButton}updated(t){super.updated(t),(t.has("dataProvider")||t.has("value"))&&this._warnDataProviderValue(this.dataProvider,this.value)}_onClearButtonClick(t){t.stopPropagation(),super._onClearButtonClick(t)}_onHostClick(t){let e=t.composedPath();(e.includes(this._labelNode)||e.includes(this._positionTarget))&&super._onHostClick(t)}_warnDataProviderValue(t,e){if(t&&e!==""&&(this.selectedItem===void 0||this.selectedItem===null)){let i=this.__getItemIndexByValue(this.filteredItems,e);(i<0||!this._getItemLabel(this.filteredItems[i]))&&console.warn("Warning: unable to determine the label for the provided `value`. Nothing to display in the text field. This usually happens when setting an initial `value` before any items are returned from the `dataProvider` callback. Consider setting `selectedItem` instead of `value`")}}};m(ii);var Es=u`
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
`;var si=class extends C(g(x(p))){static get is(){return"vaadin-multi-select-combo-box-chip"}static get styles(){return Es}static get properties(){return{disabled:{type:Boolean,reflectToAttribute:!0,sync:!0},readonly:{type:Boolean,reflectToAttribute:!0,sync:!0},label:{type:String,sync:!0},item:{type:Object}}}render(){return f`
      <div part="label">${this.label}</div>
      <div part="remove-button" @click="${this._onRemoveClick}"></div>
    `}_onRemoveClick(t){t.stopPropagation(),this.dispatchEvent(new CustomEvent("item-removed",{detail:{item:this.item},bubbles:!0,composed:!0}))}};m(si);var ri=class extends de{static get is(){return"vaadin-multi-select-combo-box-container"}static get styles(){return[super.styles,u`
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
      `]}static get properties(){return{autoExpandVertically:{type:Boolean,reflectToAttribute:!0}}}render(){return f`
      <div id="wrapper">
        <slot name="prefix"></slot>
        <slot></slot>
      </div>
      <slot name="suffix"></slot>
    `}};m(ri);var oi=class extends Pe(C(E(g(x(p))))){static get is(){return"vaadin-multi-select-combo-box-item"}static get styles(){return[Ee,Ae]}render(){return f`
      <span part="checkmark" aria-hidden="true"></span>
      <div part="content">
        <slot></slot>
      </div>
    `}};m(oi);var As=[De,u`
    #overlay {
      width: var(
        --vaadin-multi-select-combo-box-overlay-width,
        var(--_vaadin-multi-select-combo-box-overlay-default-width, auto)
      );
    }
  `];var ni=class extends ze($e(E(C(g(x(p)))))){static get is(){return"vaadin-multi-select-combo-box-overlay"}static get styles(){return[Te,As]}render(){return f`
      <div part="overlay" id="overlay">
        <div part="loader"></div>
        <div part="content" id="content"><slot></slot></div>
      </div>
    `}};m(ni);var Ps=Be;var ai=class extends Ue(g(p)){static get is(){return"vaadin-multi-select-combo-box-scroller"}static get styles(){return Ps}render(){return f`
      <div id="selector">
        <slot></slot>
      </div>
    `}ready(){super.ready(),this.setAttribute("aria-multiselectable","true")}_isItemSelected(t,e,i){return t instanceof b||this.owner.readonly?!1:this.owner._findIndex(t,this.owner.selectedItems,i)>-1}_updateElement(t,e){super._updateElement(t,e),t.toggleAttribute("readonly",this.owner.readonly)}};m(ai);var Ts=[rt,u`
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
  `];function Os(s,...t){let e=o=>Array.isArray(o),i=o=>o&&typeof o=="object"&&!e(o),r=(o,n)=>{i(n)&&i(o)&&Object.keys(n).forEach(l=>{let a=n[l];i(a)?(o[l]||(o[l]={}),r(o[l],a)):e(a)?o[l]=[...a]:a!=null&&(o[l]=a)})};return t.forEach(o=>{r(s,o)}),s}var Ms=s=>class extends s{static get properties(){return{i18n:{type:Object},__effectiveI18n:{type:Object,sync:!0}}}static get defaultI18n(){return{}}constructor(){super(),this.i18n=Os({},this.constructor.defaultI18n)}get i18n(){return this.__customI18n}set i18n(e){e!==this.__customI18n&&(this.__customI18n=e,this.__effectiveI18n=Os({},this.constructor.defaultI18n,this.__customI18n))}};var ht=new ResizeObserver(s=>{setTimeout(()=>{s.forEach(t=>{t.target.isConnected&&(t.target.resizables?t.target.resizables.forEach(e=>{e._onResize(t.contentRect)}):t.target._onResize(t.contentRect))})})}),Rr=s=>class extends s{get _observeParent(){return!1}connectedCallback(){if(super.connectedCallback(),ht.observe(this),this._observeParent){let e=this.parentNode instanceof ShadowRoot?this.parentNode.host:this.parentNode;e.resizables||(e.resizables=new Set,ht.observe(e)),e.resizables.add(this),this.__parent=e}}disconnectedCallback(){super.disconnectedCallback(),ht.unobserve(this);let e=this.__parent;if(this._observeParent&&e){let i=e.resizables;i&&(i.delete(this),i.size===0&&ht.unobserve(e)),this.__parent=null}}_onResize(e){}},ks=_(Rr);var Hr={cleared:"Selection cleared",focused:"focused. Press Backspace to remove",selected:"added to selection",deselected:"removed from selection",total:"{count} items selected"},Vs=s=>class extends Ms(lt(at(dt(it(ks(s)))))){static get properties(){return{autoExpandHorizontally:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},autoExpandVertically:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},collapseChips:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},itemClassNameGenerator:{type:Object,sync:!0},itemIdPath:{type:String,sync:!0},keepFilter:{type:Boolean,value:!1},loading:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},readonly:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},selectedItems:{type:Array,value:()=>[],notify:!0,sync:!0},allowCustomValue:{type:Boolean,value:!1},placeholder:{type:String,observer:"_placeholderChanged",reflectToAttribute:!0,sync:!0},renderer:{type:Function,sync:!0},selectedItemsOnTop:{type:Boolean,value:!1,sync:!0},value:{type:String},_overflowItems:{type:Array,value:()=>[],sync:!0},_focusedChipIndex:{type:Number,value:-1,observer:"_focusedChipIndexChanged"},_lastFilter:{type:String,sync:!0},_topGroup:{type:Array,observer:"_topGroupChanged",sync:!0},_inputField:{type:Object}}}static get observers(){return["_selectedItemsChanged(selectedItems)","__openedOrItemsChanged(opened, _dropdownItems, loading, __keepOverlayOpened)","__updateOverflowChip(_overflow, _overflowItems, disabled, readonly)","__updateScroller(opened, _dropdownItems, _focusedIndex, _theme)","__updateTopGroup(selectedItemsOnTop, selectedItems, opened)"]}static get defaultI18n(){return Hr}get i18n(){return super.i18n}set i18n(e){super.i18n=e}get slotStyles(){let e=this.localName;return[...super.slotStyles,`
        ${e}[has-value] input::placeholder {
          color: transparent !important;
          forced-color-adjust: none;
        }
      `]}get clearElement(){return this.$.clearButton}get _chips(){return[...this.querySelectorAll('[slot="chip"]')]}get _hasValue(){return this.selectedItems&&this.selectedItems.length>0}get _tagNamePrefix(){return"vaadin-multi-select-combo-box"}ready(){super.ready(),this.addController(new te(this,e=>{this._setInputElement(e),this._setFocusElement(e),this.stateTarget=e,this.ariaTarget=e})),this.addController(new ie(this.inputElement,this._labelController)),this._tooltipController=new Y(this),this.addController(this._tooltipController),this._tooltipController.setPosition("top"),this._tooltipController.setAriaTarget(this.inputElement),this._tooltipController.setShouldShow(e=>!e.opened),this._toggleElement=this.$.toggleButton,this._inputField=this.shadowRoot.querySelector('[part="input-field"]'),this._overflowController=new P(this,"overflow","vaadin-multi-select-combo-box-chip",{initializer:e=>{e.addEventListener("mousedown",i=>this._preventBlur(i)),this._overflow=e}}),this.addController(this._overflowController)}updated(e){super.updated(e),["loading","itemIdPath","itemClassNameGenerator","renderer"].forEach(r=>{e.has(r)&&(this._scroller[r]=this[r])}),e.has("selectedItems")&&this.opened&&this.$.overlay._updateOverlayWidth(),["autoExpandHorizontally","autoExpandVertically","collapseChips","disabled","readonly","clearButtonVisible","itemClassNameGenerator"].some(r=>e.has(r))&&this.__updateChips(),e.has("readonly")&&(this._setDropdownItems(this.filteredItems),this.dataProvider&&this.clearCache())}checkValidity(){return this.required&&!this.readonly?this._hasValue:!0}open(){!this.disabled&&!(this.readonly&&this.selectedItems.length===0)&&(this.opened=!0)}clear(){this.__updateSelection([]),J(this.__effectiveI18n.cleared)}__syncTopGroup(){this._topGroup=this.selectedItemsOnTop?[...this.selectedItems]:[]}clearCache(){this.readonly||(super.clearCache(),this.__syncTopGroup())}_itemsChanged(e,i){super._itemsChanged(e,i),this.__syncTopGroup()}requestContentUpdate(){this._scroller&&(this._scroller.requestContentUpdate(),this._getItemElements().forEach(e=>{e.requestContentUpdate()}))}_onClearAction(){this.clear()}_onClosed(){this._ignoreCommitValue=!0,(!this.loading||this.allowCustomValue)&&this._commitValue()}__updateScroller(e,i,r,o){e&&(this._scroller.style.maxHeight=getComputedStyle(this).getPropertyValue(`--${this._tagNamePrefix}-overlay-max-height`)||"65vh");let n=this.hasAttribute("closing");this._scroller.setProperties({items:e||n?i:[],opened:e,focusedIndex:r,theme:o})}__openedOrItemsChanged(e,i,r,o){this._overlayOpened=e&&(o||r||!!i?.length)}_closeOrCommit(){this.opened?this.close():this._commitValue()}_commitValue(){this._lastFilter=this.filter,this._ignoreCommitValue?(this._inputElementValue="",this._focusedIndex=-1,this._ignoreCommitValue=!1):this.__commitUserInput(),(!this.keepFilter||!this.opened)&&(this.filter="")}__commitUserInput(){if(this._focusedIndex>-1){let e=this._dropdownItems[this._focusedIndex];this.__selectItem(e)}else if(this._inputElementValue){let e=[...this._dropdownItems],i=e[this.__getItemIndexByLabel(e,this._inputElementValue)];if(this.allowCustomValue&&!i){let r=this._inputElementValue;this._lastCustomValue=r,this.__clearInternalValue(!0),this.dispatchEvent(new CustomEvent("custom-value-set",{detail:r,composed:!0,bubbles:!0}))}else!this.allowCustomValue&&!this.opened&&i?this.__selectItem(i):this._inputElementValue=""}}_setFocused(e){let i=!e&&!this._closeOnBlurIsPrevented;i&&(this._ignoreCommitValue=!0),super._setFocused(e),i&&document.hasFocus()&&(this._focusedChipIndex=-1,this._requestValidation()),i&&this.readonly&&this.close()}_onResize(){this.__updateChips()}_delegateAttribute(e,i){if(this.stateTarget){if(e==="required"){this._delegateAttribute("aria-required",i?"true":!1);return}super._delegateAttribute(e,i)}}_placeholderChanged(e){let i=this.__tmpA11yPlaceholder;i!==e&&(this.__savedPlaceholder=e,i&&(this.placeholder=i))}_selectedItemsChanged(e){if(this._toggleHasValue(this._hasValue),this._hasValue){let i=this._mergeItemLabels(e);this.__tmpA11yPlaceholder===void 0&&(this.__savedPlaceholder=this.placeholder),this.__tmpA11yPlaceholder=i,this.placeholder=i}else this.__tmpA11yPlaceholder!==void 0&&(delete this.__tmpA11yPlaceholder,this.placeholder=this.__savedPlaceholder);this.__updateChips(),this.requestContentUpdate()}_topGroupChanged(e){e&&this._setDropdownItems(this.filteredItems)}_hasValidInputValue(){let e=this._focusedIndex<0&&this._inputElementValue!=="";return this.allowCustomValue||!e}_shouldFetchData(){return this.readonly?!1:super._shouldFetchData()}_setDropdownItems(e){if(this.readonly){this.__setDropdownItems(this.selectedItems);return}if(this.filter||!this.selectedItemsOnTop){this.__setDropdownItems(e);return}if(e?.length&&this._topGroup?.length){let i=e.filter(r=>this._findIndex(r,this._topGroup,this.itemIdPath)===-1);this.__setDropdownItems(this._topGroup.concat(i));return}this.__setDropdownItems(e)}__setDropdownItems(e){let i=this._dropdownItems;this._dropdownItems=e;let r=i?i[this._focusedIndex]:null;if(i&&i[this._focusedIndex]instanceof b&&e[this._focusedIndex]instanceof b)return;let o=this.__getItemIndexByValue(e,this._getItemValue(r));o>-1?this._focusedIndex=o:this._focusedIndex=this.__getItemIndexByLabel(e,this.filter)}_mergeItemLabels(e){return e.map(i=>this._getItemLabel(i)).join(", ")}_findIndex(e,i,r){if(r&&e){for(let o=0;o<i.length;o++)if(i[o]&&i[o][r]===e[r])return o;return-1}return i.indexOf(e)}__clearInternalValue(e=!1){!this.keepFilter||e?(this.filter="",this._inputElementValue=""):this._inputElementValue=this.filter}__announceItem(e,i,r){let o=i?"selected":"deselected",n=this.__effectiveI18n.total.replace("{count}",r||0);J(`${e} ${this.__effectiveI18n[o]} ${n}`)}__removeItem(e){let i=[...this.selectedItems];i.splice(i.indexOf(e),1),this.__updateSelection(i);let r=this._getItemLabel(e);this.__announceItem(r,!1,i.length)}__selectItem(e){let i=[...this.selectedItems],r=this._findIndex(e,i,this.itemIdPath),o=this._getItemLabel(e),n=!1;if(r!==-1){if(this._lastFilter?.toLowerCase()===o.toLowerCase()){this.__clearInternalValue();return}i.splice(r,1)}else i.push(e),n=!0;this.__updateSelection(i),this.__clearInternalValue(),this.__announceItem(o,n,i.length)}__updateSelection(e){this.selectedItems=e,this._requestValidation(),this.dispatchEvent(new CustomEvent("change",{bubbles:!0}))}__updateTopGroup(e,i,r){e?(!r||this.__needToSyncTopGroup())&&(this._topGroup=[...i]):this._topGroup=[]}__needToSyncTopGroup(){return this.itemIdPath?this._topGroup&&this._topGroup.some(e=>{let i=this.selectedItems[this._findIndex(e,this.selectedItems,this.itemIdPath)];return i&&e!==i}):!1}__createChip(e){let i=document.createElement("vaadin-multi-select-combo-box-chip");i.setAttribute("slot","chip"),i.item=e,i.disabled=this.disabled,i.readonly=this.readonly;let r=this._getItemLabel(e);return i.label=r,i.setAttribute("title",r),typeof this.itemClassNameGenerator=="function"&&(i.className=this.itemClassNameGenerator(e)),i.addEventListener("item-removed",o=>this._onItemRemoved(o)),i.addEventListener("mousedown",o=>this._preventBlur(o)),i}__getWrapperWidth(){return this._inputField.$.wrapper.clientWidth}__getOverflowWidth(){let e=this._overflow;e.style.visibility="hidden",e.removeAttribute("hidden");let i=e.getAttribute("count");e.setAttribute("count","99");let r=getComputedStyle(e),o=e.clientWidth+parseInt(r.marginInlineStart);return e.setAttribute("count",i),e.setAttribute("hidden",""),e.style.visibility="",o}__updateChips(){if(!this._inputField||!this.inputElement)return;if(this._chips.forEach(i=>{i.remove()}),this.selectedItems.length===0){this._overflowItems=[];return}if(this.autoExpandVertically){this.selectedItems.forEach(i=>{this.appendChild(this.__createChip(i))}),this._overflowItems=[];return}let e=parseInt(getComputedStyle(this.inputElement).flexBasis);this.collapseChips?this._overflowItems=this.__updateChipsCollapsed(this.selectedItems,e):this.autoExpandHorizontally?this._overflowItems=this.__updateChipsHorizontalExpand(this.selectedItems,e):this._overflowItems=this.__updateChipsDefault(this.selectedItems,e)}__renderAllChips(e,i){let r=e.map(n=>{let l=this.__createChip(n);return this.appendChild(l),l}),o=this.__getWrapperWidth()-this.$.chips.clientWidth>=i;return{chips:r,allChipsFit:o}}__updateChipsCollapsed(e,i){let{chips:r,allChipsFit:o}=this.__renderAllChips(e,i);return o?[]:(r.forEach(n=>n.remove()),e.slice())}__updateChipsHorizontalExpand(e,i){let{chips:r,allChipsFit:o}=this.__renderAllChips(e,i);if(o)return[];let n=this.__getOverflowWidth(),l=r.length;for(;l>1&&(l-=1,r[l].remove(),!(this.__getWrapperWidth()-this.$.chips.clientWidth>=i+n)););if(l===1){let a=parseInt(getComputedStyle(this).getPropertyValue("--_chip-min-width")),d=this.__getWrapperWidth()-i-n;r[0].style.maxWidth=`${Math.max(a,d)}px`}return e.slice(l)}__updateChipsDefault(e,i){let r=this.__getWrapperWidth()-i;e.length>1&&(r-=this.__getOverflowWidth());let o=parseInt(getComputedStyle(this).getPropertyValue("--_chip-min-width"));for(let n=e.length-1,l=null;n>=0;n--){let a=this.__createChip(e[n]);if(this.insertBefore(a,l),this.$.chips.clientWidth>r&&(r<o||l!==null))return a.remove(),e.slice(0,n+1);a.style.maxWidth=`${r}px`,l=a}return[]}__updateOverflowChip(e,i,r,o){if(e){let n=i.length;e.label=`${n}`,e.setAttribute("count",`${n}`),e.setAttribute("title",this._mergeItemLabels(i)),e.toggleAttribute("hidden",n===0),e.disabled=r,e.readonly=o}}_onClearButtonClick(e){e.stopPropagation(),super._onClearButtonClick(e),this.opened&&this.requestContentUpdate()}_onChange(e){e.stopPropagation()}_onEscape(e){if(this.readonly){e.stopPropagation(),this.opened&&this.close();return}this.clearButtonVisible&&!this.opened&&this.selectedItems&&this.selectedItems.length&&(e.stopPropagation(),this._onClearAction()),super._onEscape(e)}_onEscapeCancel(){this._closeOrCommit()}_onEnter(e){if(this.opened){if(e.preventDefault(),e.stopPropagation(),this.readonly)this.close();else if(this._hasValidInputValue()){let i=this._dropdownItems[this._focusedIndex];this._commitValue(),this._focusedIndex=this._dropdownItems.indexOf(i)}return}super._onEnter(e)}_onArrowDown(){this.readonly?this.opened||this.open():super._onArrowDown()}_onArrowUp(){this.readonly?this.opened||this.open():super._onArrowUp()}_onKeyDown(e){super._onKeyDown(e);let i=this._chips;if(!this.readonly&&i.length>0)switch(e.key){case"Backspace":this._onBackSpace(i);break;case"ArrowLeft":this._onArrowLeft(i,e);break;case"ArrowRight":this._onArrowRight(i,e);break;default:this._focusedChipIndex=-1;break}}_onArrowLeft(e,i){if(this.inputElement.selectionStart!==0)return;let r=this._focusedChipIndex;r!==-1&&i.preventDefault();let o;this.__isRTL?r===e.length-1?o=-1:r>-1&&(o=r+1):r===-1?o=e.length-1:r>0&&(o=r-1),o!==void 0&&(this._focusedChipIndex=o)}_onArrowRight(e,i){if(this.inputElement.selectionStart!==0)return;let r=this._focusedChipIndex;r!==-1&&i.preventDefault();let o;this.__isRTL?r===-1?o=e.length-1:r>0&&(o=r-1):r===e.length-1?o=-1:r>-1&&(o=r+1),o!==void 0&&(this._focusedChipIndex=o)}_onBackSpace(e){if(this.inputElement.selectionStart!==0)return;let i=this._focusedChipIndex;i===-1?this._focusedChipIndex=e.length-1:(this.__removeItem(e[i].item),this._focusedChipIndex=-1)}_focusedChipIndexChanged(e,i){if(e>-1||i>-1){let r=this._chips;if(r.forEach((o,n)=>{o.toggleAttribute("focused",n===e)}),e>-1){let o=r[e].item,n=this._getItemLabel(o);J(`${n} ${this.__effectiveI18n.focused}`)}}}_overlaySelectedItemChanged(e){e.stopPropagation(),!this.hasAttribute("closing")&&(this.readonly||e.detail.item instanceof b||this.opened&&(this._lastFilter=this._inputElementValue,this.__selectItem(e.detail.item)))}_onItemRemoved(e){this.__removeItem(e.detail.item)}_preventBlur(e){e.preventDefault()}};var li=class extends Vs(C(qe(g(x(p))))){static get is(){return"vaadin-multi-select-combo-box"}static get styles(){return[st,Ts]}render(){return f`
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
          theme="${Z(this._theme)}"
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
        theme="${Z(this._theme)}"
        .positionTarget="${this._inputField}"
        no-vertical-overlap
      >
        <slot name="overlay"></slot>
      </vaadin-multi-select-combo-box-overlay>
    `}};m(li);
