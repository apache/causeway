var Re=globalThis,ze=Re.ShadowRoot&&(Re.ShadyCSS===void 0||Re.ShadyCSS.nativeShadow)&&"adoptedStyleSheets"in Document.prototype&&"replace"in CSSStyleSheet.prototype,Vt=Symbol(),qi=new WeakMap,se=class{constructor(t,e,i){if(this._$cssResult$=!0,i!==Vt)throw Error("CSSResult is not constructable. Use `unsafeCSS` or `css` instead.");this.cssText=t,this.t=e}get styleSheet(){let t=this.o,e=this.t;if(ze&&t===void 0){let i=e!==void 0&&e.length===1;i&&(t=qi.get(e)),t===void 0&&((this.o=t=new CSSStyleSheet).replaceSync(this.cssText),i&&qi.set(e,t))}return t}toString(){return this.cssText}},$t=s=>new se(typeof s=="string"?s:s+"",void 0,Vt),u=(s,...t)=>{let e=s.length===1?s[0]:t.reduce((i,r,n)=>i+(o=>{if(o._$cssResult$===!0)return o.cssText;if(typeof o=="number")return o;throw Error("Value passed to 'css' function must be a 'css' function result: "+o+". Use 'unsafeCSS' to pass non-literal values, but take care to ensure page security.")})(r)+s[n+1],s[0]);return new se(e,s,Vt)},Ue=(s,t)=>{if(ze)s.adoptedStyleSheets=t.map(e=>e instanceof CSSStyleSheet?e:e.styleSheet);else for(let e of t){let i=document.createElement("style"),r=Re.litNonce;r!==void 0&&i.setAttribute("nonce",r),i.textContent=e.cssText,s.appendChild(i)}},Lt=ze?s=>s:s=>s instanceof CSSStyleSheet?(t=>{let e="";for(let i of t.cssRules)e+=i.cssText;return $t(e)})(s):s;var{is:Wr,defineProperty:Yr,getOwnPropertyDescriptor:Kr,getOwnPropertyNames:Gr,getOwnPropertySymbols:Xr,getPrototypeOf:Zr}=Object,je=globalThis,Wi=je.trustedTypes,Qr=Wi?Wi.emptyScript:"",Jr=je.reactiveElementPolyfillSupport,Ce=(s,t)=>s,Nt={toAttribute(s,t){switch(t){case Boolean:s=s?Qr:null;break;case Object:case Array:s=s==null?s:JSON.stringify(s)}return s},fromAttribute(s,t){let e=s;switch(t){case Boolean:e=s!==null;break;case Number:e=s===null?null:Number(s);break;case Object:case Array:try{e=JSON.parse(s)}catch{e=null}}return e}},qe=(s,t)=>!Wr(s,t),Yi={attribute:!0,type:String,converter:Nt,reflect:!1,useDefault:!1,hasChanged:qe};Symbol.metadata??=Symbol("metadata"),je.litPropertyMetadata??=new WeakMap;var N=class extends HTMLElement{static addInitializer(t){this._$Ei(),(this.l??=[]).push(t)}static get observedAttributes(){return this.finalize(),this._$Eh&&[...this._$Eh.keys()]}static createProperty(t,e=Yi){if(e.state&&(e.attribute=!1),this._$Ei(),this.prototype.hasOwnProperty(t)&&((e=Object.create(e)).wrapped=!0),this.elementProperties.set(t,e),!e.noAccessor){let i=Symbol(),r=this.getPropertyDescriptor(t,i,e);r!==void 0&&Yr(this.prototype,t,r)}}static getPropertyDescriptor(t,e,i){let{get:r,set:n}=Kr(this.prototype,t)??{get(){return this[e]},set(o){this[e]=o}};return{get:r,set(o){let a=r?.call(this);n?.call(this,o),this.requestUpdate(t,a,i)},configurable:!0,enumerable:!0}}static getPropertyOptions(t){return this.elementProperties.get(t)??Yi}static _$Ei(){if(this.hasOwnProperty(Ce("elementProperties")))return;let t=Zr(this);t.finalize(),t.l!==void 0&&(this.l=[...t.l]),this.elementProperties=new Map(t.elementProperties)}static finalize(){if(this.hasOwnProperty(Ce("finalized")))return;if(this.finalized=!0,this._$Ei(),this.hasOwnProperty(Ce("properties"))){let e=this.properties,i=[...Gr(e),...Xr(e)];for(let r of i)this.createProperty(r,e[r])}let t=this[Symbol.metadata];if(t!==null){let e=litPropertyMetadata.get(t);if(e!==void 0)for(let[i,r]of e)this.elementProperties.set(i,r)}this._$Eh=new Map;for(let[e,i]of this.elementProperties){let r=this._$Eu(e,i);r!==void 0&&this._$Eh.set(r,e)}this.elementStyles=this.finalizeStyles(this.styles)}static finalizeStyles(t){let e=[];if(Array.isArray(t)){let i=new Set(t.flat(1/0).reverse());for(let r of i)e.unshift(Lt(r))}else t!==void 0&&e.push(Lt(t));return e}static _$Eu(t,e){let i=e.attribute;return i===!1?void 0:typeof i=="string"?i:typeof t=="string"?t.toLowerCase():void 0}constructor(){super(),this._$Ep=void 0,this.isUpdatePending=!1,this.hasUpdated=!1,this._$Em=null,this._$Ev()}_$Ev(){this._$ES=new Promise(t=>this.enableUpdating=t),this._$AL=new Map,this._$E_(),this.requestUpdate(),this.constructor.l?.forEach(t=>t(this))}addController(t){(this._$EO??=new Set).add(t),this.renderRoot!==void 0&&this.isConnected&&t.hostConnected?.()}removeController(t){this._$EO?.delete(t)}_$E_(){let t=new Map,e=this.constructor.elementProperties;for(let i of e.keys())this.hasOwnProperty(i)&&(t.set(i,this[i]),delete this[i]);t.size>0&&(this._$Ep=t)}createRenderRoot(){let t=this.shadowRoot??this.attachShadow(this.constructor.shadowRootOptions);return Ue(t,this.constructor.elementStyles),t}connectedCallback(){this.renderRoot??=this.createRenderRoot(),this.enableUpdating(!0),this._$EO?.forEach(t=>t.hostConnected?.())}enableUpdating(t){}disconnectedCallback(){this._$EO?.forEach(t=>t.hostDisconnected?.())}attributeChangedCallback(t,e,i){this._$AK(t,i)}_$ET(t,e){let i=this.constructor.elementProperties.get(t),r=this.constructor._$Eu(t,i);if(r!==void 0&&i.reflect===!0){let n=(i.converter?.toAttribute!==void 0?i.converter:Nt).toAttribute(e,i.type);this._$Em=t,n==null?this.removeAttribute(r):this.setAttribute(r,n),this._$Em=null}}_$AK(t,e){let i=this.constructor,r=i._$Eh.get(t);if(r!==void 0&&this._$Em!==r){let n=i.getPropertyOptions(r),o=typeof n.converter=="function"?{fromAttribute:n.converter}:n.converter?.fromAttribute!==void 0?n.converter:Nt;this._$Em=r;let a=o.fromAttribute(e,n.type);this[r]=a??this._$Ej?.get(r)??a,this._$Em=null}}requestUpdate(t,e,i,r=!1,n){if(t!==void 0){let o=this.constructor;if(r===!1&&(n=this[t]),i??=o.getPropertyOptions(t),!((i.hasChanged??qe)(n,e)||i.useDefault&&i.reflect&&n===this._$Ej?.get(t)&&!this.hasAttribute(o._$Eu(t,i))))return;this.C(t,e,i)}this.isUpdatePending===!1&&(this._$ES=this._$EP())}C(t,e,{useDefault:i,reflect:r,wrapped:n},o){i&&!(this._$Ej??=new Map).has(t)&&(this._$Ej.set(t,o??e??this[t]),n!==!0||o!==void 0)||(this._$AL.has(t)||(this.hasUpdated||i||(e=void 0),this._$AL.set(t,e)),r===!0&&this._$Em!==t&&(this._$Eq??=new Set).add(t))}async _$EP(){this.isUpdatePending=!0;try{await this._$ES}catch(e){Promise.reject(e)}let t=this.scheduleUpdate();return t!=null&&await t,!this.isUpdatePending}scheduleUpdate(){return this.performUpdate()}performUpdate(){if(!this.isUpdatePending)return;if(!this.hasUpdated){if(this.renderRoot??=this.createRenderRoot(),this._$Ep){for(let[r,n]of this._$Ep)this[r]=n;this._$Ep=void 0}let i=this.constructor.elementProperties;if(i.size>0)for(let[r,n]of i){let{wrapped:o}=n,a=this[r];o!==!0||this._$AL.has(r)||a===void 0||this.C(r,void 0,n,a)}}let t=!1,e=this._$AL;try{t=this.shouldUpdate(e),t?(this.willUpdate(e),this._$EO?.forEach(i=>i.hostUpdate?.()),this.update(e)):this._$EM()}catch(i){throw t=!1,this._$EM(),i}t&&this._$AE(e)}willUpdate(t){}_$AE(t){this._$EO?.forEach(e=>e.hostUpdated?.()),this.hasUpdated||(this.hasUpdated=!0,this.firstUpdated(t)),this.updated(t)}_$EM(){this._$AL=new Map,this.isUpdatePending=!1}get updateComplete(){return this.getUpdateComplete()}getUpdateComplete(){return this._$ES}shouldUpdate(t){return!0}update(t){this._$Eq&&=this._$Eq.forEach(e=>this._$ET(e,this[e])),this._$EM()}updated(t){}firstUpdated(t){}};N.elementStyles=[],N.shadowRootOptions={mode:"open"},N[Ce("elementProperties")]=new Map,N[Ce("finalized")]=new Map,Jr?.({ReactiveElement:N}),(je.reactiveElementVersions??=[]).push("2.1.2");var jt=globalThis,Ki=s=>s,We=jt.trustedTypes,Gi=We?We.createPolicy("lit-html",{createHTML:s=>s}):void 0,ts="$lit$",z=`lit$${Math.random().toFixed(9).slice(2)}$`,is="?"+z,en=`<${is}>`,G=document,Se=()=>G.createComment(""),Ee=s=>s===null||typeof s!="object"&&typeof s!="function",qt=Array.isArray,tn=s=>qt(s)||typeof s?.[Symbol.iterator]=="function",Ft=`[ 	
\f\r]`,we=/<(?:(!--|\/[^a-zA-Z])|(\/?[a-zA-Z][^>\s]*)|(\/?$))/g,Xi=/-->/g,Zi=/>/g,Y=RegExp(`>|${Ft}(?:([^\\s"'>=/]+)(${Ft}*=${Ft}*(?:[^ 	
\f\r"'\`<>=]|("|')|))|$)`,"g"),Qi=/'/g,Ji=/"/g,ss=/^(?:script|style|textarea|title)$/i,Wt=s=>(t,...e)=>({_$litType$:s,strings:t,values:e}),v=Wt(1),ko=Wt(2),Do=Wt(3),X=Symbol.for("lit-noChange"),C=Symbol.for("lit-nothing"),es=new WeakMap,K=G.createTreeWalker(G,129);function rs(s,t){if(!qt(s)||!s.hasOwnProperty("raw"))throw Error("invalid template strings array");return Gi!==void 0?Gi.createHTML(t):t}var sn=(s,t)=>{let e=s.length-1,i=[],r,n=t===2?"<svg>":t===3?"<math>":"",o=we;for(let a=0;a<e;a++){let l=s[a],d,h,c=-1,p=0;for(;p<l.length&&(o.lastIndex=p,h=o.exec(l),h!==null);)p=o.lastIndex,o===we?h[1]==="!--"?o=Xi:h[1]!==void 0?o=Zi:h[2]!==void 0?(ss.test(h[2])&&(r=RegExp("</"+h[2],"g")),o=Y):h[3]!==void 0&&(o=Y):o===Y?h[0]===">"?(o=r??we,c=-1):h[1]===void 0?c=-2:(c=o.lastIndex-h[2].length,d=h[1],o=h[3]===void 0?Y:h[3]==='"'?Ji:Qi):o===Ji||o===Qi?o=Y:o===Xi||o===Zi?o=we:(o=Y,r=void 0);let f=o===Y&&s[a+1].startsWith("/>")?" ":"";n+=o===we?l+en:c>=0?(i.push(d),l.slice(0,c)+ts+l.slice(c)+z+f):l+z+(c===-2?a:f)}return[rs(s,n+(s[e]||"<?>")+(t===2?"</svg>":t===3?"</math>":"")),i]},ke=class s{constructor({strings:t,_$litType$:e},i){let r;this.parts=[];let n=0,o=0,a=t.length-1,l=this.parts,[d,h]=sn(t,e);if(this.el=s.createElement(d,i),K.currentNode=this.el.content,e===2||e===3){let c=this.el.content.firstChild;c.replaceWith(...c.childNodes)}for(;(r=K.nextNode())!==null&&l.length<a;){if(r.nodeType===1){if(r.hasAttributes())for(let c of r.getAttributeNames())if(c.endsWith(ts)){let p=h[o++],f=r.getAttribute(c).split(z),E=/([.?@])?(.*)/.exec(p);l.push({type:1,index:n,name:E[2],strings:f,ctor:E[1]==="."?Ht:E[1]==="?"?Rt:E[1]==="@"?zt:ne}),r.removeAttribute(c)}else c.startsWith(z)&&(l.push({type:6,index:n}),r.removeAttribute(c));if(ss.test(r.tagName)){let c=r.textContent.split(z),p=c.length-1;if(p>0){r.textContent=We?We.emptyScript:"";for(let f=0;f<p;f++)r.append(c[f],Se()),K.nextNode(),l.push({type:2,index:++n});r.append(c[p],Se())}}}else if(r.nodeType===8)if(r.data===is)l.push({type:2,index:n});else{let c=-1;for(;(c=r.data.indexOf(z,c+1))!==-1;)l.push({type:7,index:n}),c+=z.length-1}n++}}static createElement(t,e){let i=G.createElement("template");return i.innerHTML=t,i}};function re(s,t,e=s,i){if(t===X)return t;let r=i!==void 0?e._$Co?.[i]:e._$Cl,n=Ee(t)?void 0:t._$litDirective$;return r?.constructor!==n&&(r?._$AO?.(!1),n===void 0?r=void 0:(r=new n(s),r._$AT(s,e,i)),i!==void 0?(e._$Co??=[])[i]=r:e._$Cl=r),r!==void 0&&(t=re(s,r._$AS(s,t.values),r,i)),t}var Bt=class{constructor(t,e){this._$AV=[],this._$AN=void 0,this._$AD=t,this._$AM=e}get parentNode(){return this._$AM.parentNode}get _$AU(){return this._$AM._$AU}u(t){let{el:{content:e},parts:i}=this._$AD,r=(t?.creationScope??G).importNode(e,!0);K.currentNode=r;let n=K.nextNode(),o=0,a=0,l=i[0];for(;l!==void 0;){if(o===l.index){let d;l.type===2?d=new De(n,n.nextSibling,this,t):l.type===1?d=new l.ctor(n,l.name,l.strings,this,t):l.type===6&&(d=new Ut(n,this,t)),this._$AV.push(d),l=i[++a]}o!==l?.index&&(n=K.nextNode(),o++)}return K.currentNode=G,r}p(t){let e=0;for(let i of this._$AV)i!==void 0&&(i.strings!==void 0?(i._$AI(t,i,e),e+=i.strings.length-2):i._$AI(t[e])),e++}},De=class s{get _$AU(){return this._$AM?._$AU??this._$Cv}constructor(t,e,i,r){this.type=2,this._$AH=C,this._$AN=void 0,this._$AA=t,this._$AB=e,this._$AM=i,this.options=r,this._$Cv=r?.isConnected??!0}get parentNode(){let t=this._$AA.parentNode,e=this._$AM;return e!==void 0&&t?.nodeType===11&&(t=e.parentNode),t}get startNode(){return this._$AA}get endNode(){return this._$AB}_$AI(t,e=this){t=re(this,t,e),Ee(t)?t===C||t==null||t===""?(this._$AH!==C&&this._$AR(),this._$AH=C):t!==this._$AH&&t!==X&&this._(t):t._$litType$!==void 0?this.$(t):t.nodeType!==void 0?this.T(t):tn(t)?this.k(t):this._(t)}O(t){return this._$AA.parentNode.insertBefore(t,this._$AB)}T(t){this._$AH!==t&&(this._$AR(),this._$AH=this.O(t))}_(t){this._$AH!==C&&Ee(this._$AH)?this._$AA.nextSibling.data=t:this.T(G.createTextNode(t)),this._$AH=t}$(t){let{values:e,_$litType$:i}=t,r=typeof i=="number"?this._$AC(t):(i.el===void 0&&(i.el=ke.createElement(rs(i.h,i.h[0]),this.options)),i);if(this._$AH?._$AD===r)this._$AH.p(e);else{let n=new Bt(r,this),o=n.u(this.options);n.p(e),this.T(o),this._$AH=n}}_$AC(t){let e=es.get(t.strings);return e===void 0&&es.set(t.strings,e=new ke(t)),e}k(t){qt(this._$AH)||(this._$AH=[],this._$AR());let e=this._$AH,i,r=0;for(let n of t)r===e.length?e.push(i=new s(this.O(Se()),this.O(Se()),this,this.options)):i=e[r],i._$AI(n),r++;r<e.length&&(this._$AR(i&&i._$AB.nextSibling,r),e.length=r)}_$AR(t=this._$AA.nextSibling,e){for(this._$AP?.(!1,!0,e);t!==this._$AB;){let i=Ki(t).nextSibling;Ki(t).remove(),t=i}}setConnected(t){this._$AM===void 0&&(this._$Cv=t,this._$AP?.(t))}},ne=class{get tagName(){return this.element.tagName}get _$AU(){return this._$AM._$AU}constructor(t,e,i,r,n){this.type=1,this._$AH=C,this._$AN=void 0,this.element=t,this.name=e,this._$AM=r,this.options=n,i.length>2||i[0]!==""||i[1]!==""?(this._$AH=Array(i.length-1).fill(new String),this.strings=i):this._$AH=C}_$AI(t,e=this,i,r){let n=this.strings,o=!1;if(n===void 0)t=re(this,t,e,0),o=!Ee(t)||t!==this._$AH&&t!==X,o&&(this._$AH=t);else{let a=t,l,d;for(t=n[0],l=0;l<n.length-1;l++)d=re(this,a[i+l],e,l),d===X&&(d=this._$AH[l]),o||=!Ee(d)||d!==this._$AH[l],d===C?t=C:t!==C&&(t+=(d??"")+n[l+1]),this._$AH[l]=d}o&&!r&&this.j(t)}j(t){t===C?this.element.removeAttribute(this.name):this.element.setAttribute(this.name,t??"")}},Ht=class extends ne{constructor(){super(...arguments),this.type=3}j(t){this.element[this.name]=t===C?void 0:t}},Rt=class extends ne{constructor(){super(...arguments),this.type=4}j(t){this.element.toggleAttribute(this.name,!!t&&t!==C)}},zt=class extends ne{constructor(t,e,i,r,n){super(t,e,i,r,n),this.type=5}_$AI(t,e=this){if((t=re(this,t,e,0)??C)===X)return;let i=this._$AH,r=t===C&&i!==C||t.capture!==i.capture||t.once!==i.once||t.passive!==i.passive,n=t!==C&&(i===C||r);r&&this.element.removeEventListener(this.name,this,i),n&&this.element.addEventListener(this.name,this,t),this._$AH=t}handleEvent(t){typeof this._$AH=="function"?this._$AH.call(this.options?.host??this.element,t):this._$AH.handleEvent(t)}},Ut=class{constructor(t,e,i){this.element=t,this.type=6,this._$AN=void 0,this._$AM=e,this.options=i}get _$AU(){return this._$AM._$AU}_$AI(t){re(this,t)}};var rn=jt.litHtmlPolyfillSupport;rn?.(ke,De),(jt.litHtmlVersions??=[]).push("3.3.3");var ns=(s,t,e)=>{let i=e?.renderBefore??t,r=i._$litPart$;if(r===void 0){let n=e?.renderBefore??null;i._$litPart$=r=new De(t.insertBefore(Se(),n),n,void 0,e??{})}return r._$AI(s),r};var Yt=globalThis,_=class extends N{constructor(){super(...arguments),this.renderOptions={host:this},this._$Do=void 0}createRenderRoot(){let t=super.createRenderRoot();return this.renderOptions.renderBefore??=t.firstChild,t}update(t){let e=this.render();this.hasUpdated||(this.renderOptions.isConnected=this.isConnected),super.update(t),this._$Do=ns(e,this.renderRoot,this.renderOptions)}connectedCallback(){super.connectedCallback(),this._$Do?.setConnected(!0)}disconnectedCallback(){super.disconnectedCallback(),this._$Do?.setConnected(!1)}render(){return X}};_._$litElement$=!0,_.finalized=!0,Yt.litElementHydrateSupport?.({LitElement:_});var nn=Yt.litElementPolyfillSupport;nn?.({LitElement:_});(Yt.litElementVersions??=[]).push("4.2.2");window.Vaadin||={};window.Vaadin.featureFlags||={};function on(s){return s.replace(/-[a-z]/gu,t=>t[1].toUpperCase())}var F={};function m(s,t="25.2.8"){if(Object.defineProperty(s,"version",{get(){return t}}),s.experimental){let i=typeof s.experimental=="string"?s.experimental:`${on(s.is.split("-").slice(1).join("-"))}Component`;if(!window.Vaadin.featureFlags[i]&&!F[i]){F[i]=new Set,F[i].add(s),Object.defineProperty(window.Vaadin.featureFlags,i,{get(){return F[i].size===0},set(r){r&&F[i].size>0&&(F[i].forEach(n=>{customElements.define(n.is,n)}),F[i].clear())}});return}else if(F[i]){F[i].add(s);return}}let e=customElements.get(s.is);if(!e)customElements.define(s.is,s);else{let i=e.version;i&&s.version&&i===s.version?console.warn(`The component ${s.is} has been loaded twice`):console.error(`Tried to define ${s.is} version ${s.version} when version ${e.version} is already in use. Something will probably break.`)}}var B=[];function Kt(s,t,e=s.getAttribute("dir")){t?s.setAttribute("dir",t):e!=null&&s.removeAttribute("dir")}function Gt(){return document.documentElement.getAttribute("dir")}function an(){let s=Gt();B.forEach(t=>{Kt(t,s)})}var ln=new MutationObserver(an);ln.observe(document.documentElement,{attributes:!0,attributeFilter:["dir"]});var I=s=>class extends s{static get properties(){return{dir:{type:String,value:"",reflectToAttribute:!0,converter:{fromAttribute:e=>e||"",toAttribute:e=>e===""?null:e}}}}get __isRTL(){return this.getAttribute("dir")==="rtl"}connectedCallback(){super.connectedCallback(),(!this.hasAttribute("dir")||this.__restoreSubscription)&&(this.__subscribe(),Kt(this,Gt(),null))}attributeChangedCallback(e,i,r){if(super.attributeChangedCallback(e,i,r),e!=="dir")return;let n=Gt(),o=r===n&&B.indexOf(this)===-1,a=!r&&i&&B.indexOf(this)===-1;o||a?(this.__subscribe(),Kt(this,n,r)):r!==n&&i===n&&this.__unsubscribe()}disconnectedCallback(){super.disconnectedCallback(),this.__restoreSubscription=B.includes(this),this.__unsubscribe()}_valueToNodeAttribute(e,i,r){r==="dir"&&i===""&&!e.hasAttribute("dir")||super._valueToNodeAttribute(e,i,r)}_attributeToProperty(e,i,r){e==="dir"&&!i?this.dir="":super._attributeToProperty(e,i,r)}__subscribe(){B.includes(this)||B.push(this)}__unsubscribe(){B.includes(this)&&B.splice(B.indexOf(this),1)}};var os=new WeakMap;function dn(s,t){let e=t;for(;e;){if(os.get(e)===s)return!0;e=Object.getPrototypeOf(e)}return!1}function x(s){return t=>{if(dn(s,t))return t;let e=s(t);return os.set(e,s),e}}function Ie(s,t){return s.split(".").reduce((e,i)=>e?e[i]:void 0,t)}function as(s,t,e){let i=s.split("."),r=i.pop(),n=i.reduce((o,a)=>o[a],e);n[r]=t}var Xt={},hn=/([A-Z])/gu;function ls(s){return Xt[s]||(Xt[s]=s.replace(hn,"-$1").toLowerCase()),Xt[s]}function ds(s){return s[0].toUpperCase()+s.substring(1)}function Zt(s){let[t,e]=s.split("("),i=e.replace(")","").split(",").map(r=>r.trim());return{method:t,observerProps:i}}function Qt(s,t){return Object.prototype.hasOwnProperty.call(s,t)||(s[t]=new Map(s[t])),s[t]}var cn=s=>{class t extends s{static enabledWarnings=[];static createProperty(i,r){[String,Boolean,Number,Array].includes(r)&&(r={type:r}),r?.reflectToAttribute&&(r.reflect=!0),super.createProperty(i,r)}static getOrCreateMap(i){return Qt(this,i)}static finalize(){if(window.litIssuedWarnings&&(window.litIssuedWarnings.add("no-override-create-property"),window.litIssuedWarnings.add("no-override-get-property-descriptor")),super.finalize(),Array.isArray(this.observers)){let i=this.getOrCreateMap("__complexObservers");this.observers.forEach(r=>{let{method:n,observerProps:o}=Zt(r);i.set(n,o)})}}static addCheckedInitializer(i){super.addInitializer(r=>{r instanceof this&&i(r)})}static getPropertyDescriptor(i,r,n){let o=super.getPropertyDescriptor(i,r,n),a=o;if(this.getOrCreateMap("__propKeys").set(i,r),n.sync&&(a={get:o.get,set(l){let d=this[i];qe(l,d)&&(this[r]=l,this.requestUpdate(i,d,n),this.hasUpdated&&this.performUpdate())},configurable:!0,enumerable:!0}),n.readOnly){let l=a.set;this.addCheckedInitializer(d=>{d[`_set${ds(i)}`]=function(h){l.call(d,h)}}),a={get:a.get,set(){},configurable:!0,enumerable:!0}}if("value"in n&&this.addCheckedInitializer(l=>{let d=typeof n.value=="function"?n.value.call(l):n.value;n.readOnly?l[`_set${ds(i)}`](d):l[i]=d}),n.observer){let l=n.observer;this.getOrCreateMap("__observers").set(i,l),this.addCheckedInitializer(d=>{d[l]||console.warn(`observer method ${l} not defined`)})}if(n.notify){if(!this.__notifyProps)this.__notifyProps=new Set;else if(!this.hasOwnProperty("__notifyProps")){let l=this.__notifyProps;this.__notifyProps=new Set(l)}this.__notifyProps.add(i)}if(n.computed){let l=`__assignComputed${i}`,d=Zt(n.computed);this.prototype[l]=function(...h){this[i]=this[d.method](...h)},this.getOrCreateMap("__computedObservers").set(l,d.observerProps)}return n.attribute||(n.attribute=ls(i)),a}static get polylitConfig(){return{asyncFirstRender:!1}}connectedCallback(){super.connectedCallback();let{polylitConfig:i}=this.constructor;!this.hasUpdated&&!i.asyncFirstRender&&this.performUpdate()}firstUpdated(){super.firstUpdated(),this.$||(this.$={}),this.renderRoot.querySelectorAll("[id]").forEach(i=>{this.$[i.id]=i})}ready(){}willUpdate(i){this.constructor.__computedObservers&&this.__runComplexObservers(i,this.constructor.__computedObservers)}updated(i){let r=this.__isReadyInvoked;this.__isReadyInvoked=!0,this.constructor.__observers&&this.__runObservers(i,this.constructor.__observers),this.constructor.__complexObservers&&this.__runComplexObservers(i,this.constructor.__complexObservers),this.__dynamicPropertyObservers&&this.__runDynamicObservers(i,this.__dynamicPropertyObservers),this.__dynamicMethodObservers&&this.__runComplexObservers(i,this.__dynamicMethodObservers),this.constructor.__notifyProps&&this.__runNotifyProps(i,this.constructor.__notifyProps),r||this.ready()}setProperties(i){Object.entries(i).forEach(([r,n])=>{let o=this.constructor.__propKeys.get(r),a=this[o];this[o]=n,this.requestUpdate(r,a)}),this.hasUpdated&&this.performUpdate()}_createMethodObserver(i){let r=Qt(this,"__dynamicMethodObservers"),{method:n,observerProps:o}=Zt(i);r.set(n,o)}_createPropertyObserver(i,r){Qt(this,"__dynamicPropertyObservers").set(r,i)}__runComplexObservers(i,r){r.forEach((n,o)=>{n.some(a=>i.has(a))&&(this[o]?this[o](...n.map(a=>this[a])):console.warn(`observer method ${o} not defined`))})}__runDynamicObservers(i,r){r.forEach((n,o)=>{i.has(n)&&this[o]&&this[o](this[n],i.get(n))})}__runObservers(i,r){i.forEach((n,o)=>{let a=r.get(o);a!==void 0&&this[a]&&this[a](this[o],n)})}__runNotifyProps(i,r){i.forEach((n,o)=>{r.has(o)&&this.dispatchEvent(new CustomEvent(`${ls(o)}-changed`,{detail:{value:this[o]}}))})}_get(i,r){return Ie(i,r)}_set(i,r,n){as(i,r,n)}}return t},g=x(cn);function Ye(s){try{CSS.registerProperty(s)}catch(t){if(t instanceof DOMException&&t.name==="InvalidModificationError")console.warn(`The CSS property ${s.name} has already been registered.`);else throw t}}var hs=(s,...t)=>{let e=document.createElement("style");e.id=s,e.textContent=t.map(i=>i.toString()).join(`
`),document.head.insertAdjacentElement("afterbegin",e)};var Ke=class s extends EventTarget{#s;#e=new Set;#t;#i=!1;constructor(t){super(),this.#s=t,this.#t=new CSSStyleSheet}#n(t){let{propertyName:e}=t;this.#e.has(e)&&this.dispatchEvent(new CustomEvent("property-changed",{detail:{propertyName:e}}))}observe(t){this.connect(),!this.#e.has(t)&&(this.#e.add(t),this.#t.replaceSync(`
      :root::before, :host::before {
        content: '' !important;
        position: absolute !important;
        top: -9999px !important;
        left: -9999px !important;
        visibility: hidden !important;
        transition: 1ms allow-discrete step-end !important;
        transition-property: ${[...this.#e].join(", ")} !important;
      }
    `))}connect(){this.#i||(this.#s.adoptedStyleSheets.unshift(this.#t),this.#r.addEventListener("transitionstart",t=>this.#n(t)),this.#r.addEventListener("transitionend",t=>this.#n(t)),this.#i=!0)}disconnect(){this.#e.clear(),this.#s.adoptedStyleSheets=this.#s.adoptedStyleSheets.filter(t=>t!==this.#t),this.#r.removeEventListener("transitionstart",this.#n),this.#r.removeEventListener("transitionend",this.#n),this.#i=!1}get#r(){return this.#s.documentElement??this.#s.host}static for(t){return t.__cssPropertyObserver||=new s(t),t.__cssPropertyObserver}};function un(s){let{baseStyles:t,themeStyles:e,elementStyles:i,lumoInjector:r}=s.constructor,n=s.__lumoStyleSheet;return n?[...r.includeBaseStyles?t??i:[],n,...e??[]]:i}function Jt(s){Ue(s.shadowRoot,un(s))}function ei(s,t){s.__lumoStyleSheet=t,Jt(s)}function Ge(s){s.__lumoStyleSheet=void 0,Jt(s)}var cs=new Set;function ti(s){cs.has(s)||(cs.add(s),console.warn(s))}var us=new WeakMap;function ps(s){try{return s.media.mediaText}catch{return ti('[LumoInjector] Browser denied to access property "mediaText" for some CSS rules, so they were skipped.'),""}}function pn(s){try{return s.cssRules}catch{return ti('[LumoInjector] Browser denied to access property "cssRules" for some CSS stylesheets, so they were skipped.'),[]}}function _s(s,t={tags:new Map,modules:new Map}){for(let e of pn(s)){if(e instanceof CSSImportRule){let i=ps(e);i.startsWith("lumo_")?t.modules.set(i,[...e.styleSheet.cssRules]):_s(e.styleSheet,t);continue}if(e instanceof CSSMediaRule){let i=ps(e);i.startsWith("lumo_")&&t.modules.set(i,[...e.cssRules]);continue}if(e instanceof CSSStyleRule&&e.cssText.includes("-inject")){for(let i of e.style){let r=i.match(/^--_lumo-(.*)-inject-modules$/u)?.[1];if(!r)continue;let n=e.style.getPropertyValue(i);t.tags.set(r,n.split(",").map(o=>o.trim().replace(/'|"/gu,"")))}continue}}return t}function fs(s){let t=new Map,e=new Map;for(let i of s){let r=us.get(i);r||(r=_s(i),us.set(i,r)),t=new Map([...t,...r.tags]),e=new Map([...e,...r.modules])}return{tags:t,modules:e}}function ii(s){return`--_lumo-${s.is}-inject`}var Xe=class{#s;#e;#t=new Map;#i=new Map;constructor(t=document){this.#s=t,this.handlePropertyChange=this.handlePropertyChange.bind(this),this.#e=Ke.for(t),this.#e.addEventListener("property-changed",this.handlePropertyChange)}disconnect(){this.#e.removeEventListener("property-changed",this.handlePropertyChange),this.#t.clear(),this.#i.values().forEach(t=>t.forEach(Ge))}forceUpdate(){for(let t of this.#t.keys())this.#r(t)}componentConnected(t){let{lumoInjector:e}=t.constructor,{is:i}=e;this.#i.set(i,this.#i.get(i)??new Set),this.#i.get(i).add(t);let r=this.#t.get(i);if(r){r.cssRules.length>0&&ei(t,r);return}this.#n(i);let n=ii(e);this.#e.observe(n)}componentDisconnected(t){let{is:e}=t.constructor.lumoInjector;this.#i.get(e)?.delete(t),Ge(t)}handlePropertyChange(t){let{propertyName:e}=t.detail,i=e.match(/^--_lumo-(.*)-inject$/u)?.[1];i&&this.#r(i)}#n(t){this.#t.set(t,new CSSStyleSheet),this.#r(t)}#r(t){let{tags:e,modules:i}=fs(this.#o),r=(e.get(t)??[]).flatMap(o=>i.get(o)??[]).map(o=>o.cssText).join(`
`),n=this.#t.get(t);n.replaceSync(r),this.#i.get(t)?.forEach(o=>{r?ei(o,n):Ge(o)})}get#o(){let t=new Set;for(let e of[this.#s,document])t=t.union(new Set(e.styleSheets)),t=t.union(new Set(e.adoptedStyleSheets));return[...t]}};var ms=new Set;function vs(s){let t=s.getRootNode();return t.host&&t.host.constructor.version?vs(t.host):t}var b=s=>class extends s{static finalize(){super.finalize();let e=ii(this.lumoInjector);this.is&&!ms.has(e)&&(ms.add(e),Ye({name:e,syntax:"<number>",inherits:!0,initialValue:"0"}))}static get lumoInjector(){return{is:this.is,includeBaseStyles:!1}}connectedCallback(){super.connectedCallback();let e=vs(this);e.__lumoInjectorDisabled||this.isConnected&&(e.__lumoInjector||=new Xe(e),this.__lumoInjector=e.__lumoInjector,this.__lumoInjector.componentConnected(this))}disconnectedCallback(){super.disconnectedCallback(),this.__lumoInjector&&(this.__lumoInjector.componentDisconnected(this),this.__lumoInjector=void 0)}};var gs=s=>class extends s{static get properties(){return{_theme:{type:String,readOnly:!0}}}static get observedAttributes(){return[...super.observedAttributes,"theme"]}attributeChangedCallback(e,i,r){super.attributeChangedCallback(e,i,r),e==="theme"&&this._set_theme(r)}};var si=[],_n=new Set,fn=new Set;function mn(s){return s&&Object.prototype.hasOwnProperty.call(s,"__themes")}function vn(s,t){return(s||"").split(" ").some(e=>new RegExp(`^${e.split("*").join(".*")}$`,"u").test(t))}function gn(s){return s.map(t=>t.cssText).join(`
`)}var bn="vaadin-themable-mixin-style";function yn(s,t){let e=document.createElement("style");e.id=bn,e.textContent=gn(s),t.content.appendChild(e)}function xn(s=""){let t=0;return s.startsWith("lumo-")||s.startsWith("material-")?t=1:s.startsWith("vaadin-")&&(t=2),t}function bs(s){let t=[];return s.include&&[].concat(s.include).forEach(e=>{let i=si.find(r=>r.moduleId===e);i?t.push(...bs(i),...i.styles):console.warn(`Included moduleId ${e} not found in style registry`)},s.styles),t}function Cn(s){let t=`${s}-default-theme`,e=si.filter(i=>i.moduleId!==t&&vn(i.themeFor,s)).map(i=>({...i,styles:[...bs(i),...i.styles],includePriority:xn(i.moduleId)})).sort((i,r)=>r.includePriority-i.includePriority);return e.length>0?e:si.filter(i=>i.moduleId===t)}var y=s=>class extends gs(s){constructor(){super(),_n.add(new WeakRef(this))}static finalize(){if(super.finalize(),this.is&&fn.add(this.is),this.elementStyles)return;let e=this.prototype._template;!e||mn(this)||yn(this.getStylesForThis(),e)}static finalizeStyles(e){return this.baseStyles=e?[e].flat(1/0):[],this.themeStyles=this.getStylesForThis(),[...this.baseStyles,...this.themeStyles]}static getStylesForThis(){let e=s.__themes||[],i=Object.getPrototypeOf(this.prototype),r=(i?i.constructor.__themes:[])||[];this.__themes=[...e,...r,...Cn(this.is)];let n=this.__themes.flatMap(o=>o.styles);return n.filter((o,a)=>a===n.lastIndexOf(o))}};["--vaadin-text-color","--vaadin-text-color-disabled","--vaadin-text-color-secondary","--vaadin-border-color","--vaadin-border-color-secondary","--vaadin-background-color"].forEach(s=>{Ye({name:s,syntax:"<color>",inherits:!0,initialValue:"transparent"})});hs("vaadin-base",u`
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
  `);var ys=u`
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
`;var ri=class extends y(I(g(b(_)))){static get is(){return"vaadin-input-container"}static get styles(){return ys}static get properties(){return{disabled:{type:Boolean,reflectToAttribute:!0},readonly:{type:Boolean,reflectToAttribute:!0},invalid:{type:Boolean,reflectToAttribute:!0}}}render(){return v`
      <slot name="prefix"></slot>
      <slot></slot>
      <slot name="suffix"></slot>
    `}ready(){super.ready(),this.addEventListener("pointerdown",t=>{t.target===this&&t.preventDefault()}),this.addEventListener("click",t=>{t.target===this&&this.shadowRoot.querySelector("slot:not([name])").assignedNodes({flatten:!0}).forEach(e=>e.focus&&e.focus())})}};m(ri);var Ze=u`
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
`;var xs=u`
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
`;var oi=!1;window.addEventListener("keydown",()=>{oi=!0},{capture:!0});window.addEventListener("mousedown",()=>{oi=!1},{capture:!0});function Te(){let s=document.activeElement||document.body;for(;s.shadowRoot&&s.shadowRoot.activeElement;)s=s.shadowRoot.activeElement;return s}function T(){return oi}function Cs(s){let t=s.style;if(t.visibility==="hidden"||t.display==="none")return!0;let e=window.getComputedStyle(s);return e.visibility==="hidden"||e.display==="none"}function wn(s,t){let e=Math.max(s.tabIndex,0),i=Math.max(t.tabIndex,0);return e===0||i===0?i>e:e>i}function Sn(s,t){let e=[];for(;s.length>0&&t.length>0;)wn(s[0],t[0])?e.push(t.shift()):e.push(s.shift());return e.concat(s,t)}function ni(s){let t=s.length;if(t<2)return s;let e=Math.ceil(t/2),i=ni(s.slice(0,e)),r=ni(s.slice(e));return Sn(i,r)}function ws(s){return s.checkVisibility?!s.checkVisibility({visibilityProperty:!0}):s.offsetParent===null&&s.clientWidth===0&&s.clientHeight===0?!0:Cs(s)}function Ae(s){return s.matches('[tabindex="-1"]')?!1:s.matches("input, select, textarea, button, object")?s.matches(":not([disabled])"):s.matches("a[href], area[href], iframe, [tabindex], [contentEditable]")}function oe(s){return s.getRootNode().activeElement===s}function En(s){if(!Ae(s))return-1;let t=s.getAttribute("tabindex")||0;return Number(t)}function Ss(s,t){if(s.nodeType!==Node.ELEMENT_NODE||Cs(s))return!1;let e=s,i=En(e),r=i>0;i>=0&&t.push(e);let n=[];return e.localName==="slot"?n=e.assignedNodes({flatten:!0}):n=(e.shadowRoot||e).children,[...n].forEach(o=>{r=Ss(o,t)||r}),r}function Es(s){let t=[];return Ss(s,t)?ni(t):t}var Qe=s=>s.test(navigator.userAgent),ai=s=>s.test(navigator.platform),kn=s=>s.test(navigator.vendor),Fa=Qe(/Android/u),Ba=Qe(/Chrome/u)&&kn(/Google Inc/u),Ha=Qe(/Firefox/u),Dn=ai(/^iPad/u)||ai(/^Mac/u)&&navigator.maxTouchPoints>1,In=ai(/^iPhone/u),Je=In||Dn,ks=Qe(/^((?!chrome|android).)*safari/iu),Me=(()=>{try{return document.createEvent("TouchEvent"),!0}catch{return!1}})();var et=class{saveFocus(t){this.focusNode=t||Te()}restoreFocus(t){let e=this.focusNode;if(!e)return;let i={preventScroll:t?t.preventScroll:!1,focusVisible:t?t.focusVisible:!1};Te()===document.body?setTimeout(()=>e.focus(i)):e.focus(i),this.focusNode=null}};var li=[];var tt=class{constructor(t){this.host=t,this.__trapNode=null,this.__onKeyDown=this.__onKeyDown.bind(this)}get __focusableElements(){return Es(this.__trapNode)}get __focusedElementIndex(){let t=this.__focusableElements;return t.indexOf(t.filter(oe).pop())}hostConnected(){document.addEventListener("keydown",this.__onKeyDown)}hostDisconnected(){document.removeEventListener("keydown",this.__onKeyDown)}trapFocus(t){if(this.__trapNode=t,this.__focusableElements.length===0)throw this.__trapNode=null,new Error("The trap node should have at least one focusable descendant or be focusable itself.");li.push(this),this.__focusedElementIndex===-1&&this.__focusableElements[0].focus({focusVisible:T()})}releaseFocus(){this.__trapNode=null,li.pop()}__onKeyDown(t){if(this.__trapNode&&this===Array.from(li).pop()&&t.key==="Tab"){if(t.defaultPrevented)return;t.preventDefault();let e=t.shiftKey;this.__focusNextElement(e)}}__focusNextElement(t=!1){let e=this.__focusableElements,i=t?-1:1,r=this.__focusedElementIndex,n=(e.length+r+i)%e.length,o=e[n];o.focus({focusVisible:!0}),o.localName==="input"&&o.select()}};var Ds=s=>class extends s{static get properties(){return{focusTrap:{type:Boolean,value:!1},restoreFocusOnClose:{type:Boolean,value:!1},restoreFocusNode:{type:HTMLElement}}}constructor(){super(),this.__focusTrapController=new tt(this),this.__focusRestorationController=new et}get _contentRoot(){return this}ready(){super.ready(),this.addController(this.__focusTrapController),this.addController(this.__focusRestorationController)}get _focusTrapRoot(){return this.$.overlay}_resetFocus(){if(this.focusTrap&&this.__focusTrapController.releaseFocus(),this.restoreFocusOnClose&&this._shouldRestoreFocus()){let e=T(),i=!e;this.__focusRestorationController.restoreFocus({preventScroll:i,focusVisible:e})}}_saveFocus(){this.restoreFocusOnClose&&this.__focusRestorationController.saveFocus(this.restoreFocusNode)}_trapFocus(){this.focusTrap&&!ws(this._focusTrapRoot)&&this.__focusTrapController.trapFocus(this._focusTrapRoot)}_shouldRestoreFocus(){let e=Te();return e===document.body||this._deepContains(e)}_deepContains(e){if(this._contentRoot.contains(e))return!0;let i=e,r=e.ownerDocument;for(;i&&i!==r&&i!==this._contentRoot;)i=i.parentNode||i.host;return i===this._contentRoot}};var it=new Set,st=()=>[...it].filter(s=>!s.hasAttribute("closing")),Tn=s=>{let t=st(),e=t.indexOf(s);return e===-1?[]:t.slice(e+1)},An=(s,t)=>s._deepContains(t),Is=(s,t=e=>!0)=>{let e=st().filter(t);return s===e.pop()},Ts=s=>class extends s{get _last(){return Is(this)}get _isAttached(){return it.has(this)}bringToFront(){if(Is(this))return;let e=Tn(this),i=e.filter(r=>r._hasOverlayPositionMixin&&An(this,r));i.length!==e.length&&[this,...i].forEach(r=>{r.matches(":popover-open")&&(r.hidePopover(),r.showPopover()),r._removeAttachedInstance(),r._appendAttachedInstance()})}_enterModalState(){document.body.style.pointerEvents!=="none"&&(this._previousDocumentPointerEvents=document.body.style.pointerEvents,document.body.style.pointerEvents="none"),st().forEach(e=>{e!==this&&(e.$.overlay.style.pointerEvents="none")})}_exitModalState(){this._previousDocumentPointerEvents!==void 0&&(document.body.style.pointerEvents=this._previousDocumentPointerEvents,delete this._previousDocumentPointerEvents);let e=st(),i;for(;(i=e.pop())&&!(i!==this&&(i.$.overlay.style.removeProperty("pointer-events"),!i.modeless)););}_appendAttachedInstance(){it.add(this)}_removeAttachedInstance(){this._isAttached&&it.delete(this)}};function As(s,t){let e=null,i,r=document.documentElement;function n(){i&&clearTimeout(i),e?.disconnect(),e=null}function o(a=!1,l=1){n();let{left:d,top:h,width:c,height:p}=s.getBoundingClientRect();if(a||t(),!c||!p)return;let f=Math.floor(h),E=Math.floor(r.clientWidth-(d+c)),Fe=Math.floor(r.clientHeight-(h+p)),Be=Math.floor(d),He={rootMargin:`${-f}px ${-E}px ${-Fe}px ${-Be}px`,threshold:Math.max(0,Math.min(1,l))||1},ji=!0;function jr(qr){let Ot=qr[0].intersectionRatio;if(Ot!==l){if(!ji)return o();Ot?o(!1,Ot):i=setTimeout(()=>{o(!1,1e-7)},1e3)}ji=!1}e=new IntersectionObserver(jr,He),e.observe(s)}return o(!0),n}function k(s,t,e){let i=[s];s.owner&&i.push(s.owner),typeof e=="string"?i.forEach(r=>{r.setAttribute(t,e)}):e?i.forEach(r=>{r.setAttribute(t,"")}):i.forEach(r=>{r.removeAttribute(t)})}var rt=s=>class extends Ds(Ts(s)){static get properties(){return{opened:{type:Boolean,notify:!0,observer:"_openedChanged",reflectToAttribute:!0,sync:!0},owner:{type:Object,sync:!0},model:{type:Object,sync:!0},renderer:{type:Object,sync:!0},modeless:{type:Boolean,value:!1,reflectToAttribute:!0,observer:"_modelessChanged",sync:!0},hidden:{type:Boolean,reflectToAttribute:!0,observer:"_hiddenChanged",sync:!0},withBackdrop:{type:Boolean,value:!1,reflectToAttribute:!0,observer:"_withBackdropChanged",sync:!0}}}static get observers(){return["_rendererOrDataChanged(renderer, owner, model, opened)"]}get _rendererRoot(){return this}constructor(){super(),this._boundMouseDownListener=this._mouseDownListener.bind(this),this._boundMouseUpListener=this._mouseUpListener.bind(this),this._boundOutsideClickListener=this._outsideClickListener.bind(this),this._boundKeydownListener=this._keydownListener.bind(this),Je&&(this._boundIosResizeListener=()=>this._detectIosNavbar())}firstUpdated(){super.firstUpdated(),this.popover="manual",this.addEventListener("click",()=>{}),this.$.backdrop&&this.$.backdrop.addEventListener("click",()=>{}),this.addEventListener("mouseup",()=>{document.activeElement===document.body&&this.$.overlay.getAttribute("tabindex")==="0"&&this.$.overlay.focus()}),this.addEventListener("animationcancel",()=>{this._flushAnimation("opening"),this._flushAnimation("closing")})}connectedCallback(){super.connectedCallback(),this._boundIosResizeListener&&(this._detectIosNavbar(),window.addEventListener("resize",this._boundIosResizeListener)),this.opened&&this._attachOverlay()}disconnectedCallback(){super.disconnectedCallback(),this.__scheduledOpen&&(cancelAnimationFrame(this.__scheduledOpen),this.__scheduledOpen=null),this._boundIosResizeListener&&window.removeEventListener("resize",this._boundIosResizeListener)}requestContentUpdate(){this.renderer&&this.renderer.call(this.owner,this._rendererRoot,this.owner,this.model)}close(e){let i=new CustomEvent("vaadin-overlay-close",{bubbles:!0,cancelable:!0,detail:{overlay:this,sourceEvent:e}});this.dispatchEvent(i),document.body.dispatchEvent(i),i.defaultPrevented||(this.opened=!1)}setBounds(e,i=!0){let r=this.$.overlay,n={...e};i&&r.style.position!=="absolute"&&(r.style.position="absolute"),Object.keys(n).forEach(o=>{n[o]!==null&&!isNaN(n[o])&&(n[o]=`${n[o]}px`)}),Object.assign(r.style,n)}_detectIosNavbar(){if(!this.opened)return;let e=window.innerHeight,r=window.innerWidth>e,n=document.documentElement.clientHeight;r&&n>e?this.style.setProperty("--vaadin-overlay-viewport-bottom",`${n-e}px`):this.style.setProperty("--vaadin-overlay-viewport-bottom","0px")}_shouldAddGlobalListeners(){return!this.modeless}_addGlobalListeners(){this.__hasGlobalListeners||(this.__hasGlobalListeners=!0,document.addEventListener("mousedown",this._boundMouseDownListener),document.addEventListener("mouseup",this._boundMouseUpListener),document.documentElement.addEventListener("click",this._boundOutsideClickListener,!0))}_removeGlobalListeners(){this.__hasGlobalListeners&&(this.__hasGlobalListeners=!1,document.removeEventListener("mousedown",this._boundMouseDownListener),document.removeEventListener("mouseup",this._boundMouseUpListener),document.documentElement.removeEventListener("click",this._boundOutsideClickListener,!0))}_rendererOrDataChanged(e,i,r,n){let o=this._oldOwner!==i||this._oldModel!==r;this._oldModel=r,this._oldOwner=i;let a=this._oldRenderer!==e,l=this._oldRenderer!==void 0;this._oldRenderer=e;let d=this._oldOpened!==n;this._oldOpened=n,a&&l&&(this._rendererRoot.innerHTML="",delete this._rendererRoot._$litPart$),n&&e&&(a||d||o)&&this.requestContentUpdate()}_modelessChanged(e){this.opened&&(this._shouldAddGlobalListeners()?this._addGlobalListeners():this._removeGlobalListeners()),e?this._exitModalState():this.opened&&this._enterModalState(),k(this,"modeless",e)}_withBackdropChanged(e){k(this,"with-backdrop",e)}_openedChanged(e,i){if(e){if(!this.isConnected){this.opened=!1;return}this._saveFocus(),this._animatedOpening(),this.__scheduledOpen=requestAnimationFrame(()=>{setTimeout(()=>{this._trapFocus();let r=new CustomEvent("vaadin-overlay-open",{detail:{overlay:this},bubbles:!0});this.dispatchEvent(r),document.body.dispatchEvent(r)})}),document.addEventListener("keydown",this._boundKeydownListener),this._shouldAddGlobalListeners()&&this._addGlobalListeners()}else i&&(this.__scheduledOpen&&(cancelAnimationFrame(this.__scheduledOpen),this.__scheduledOpen=null),this._resetFocus(),this._animatedClosing(),document.removeEventListener("keydown",this._boundKeydownListener),this._shouldAddGlobalListeners()&&this._removeGlobalListeners())}_hiddenChanged(e){e&&this.hasAttribute("closing")&&this._flushAnimation("closing")}_shouldAnimate(){let e=getComputedStyle(this),i=e.getPropertyValue("animation-name");return!(e.getPropertyValue("display")==="none")&&i&&i!=="none"}_enqueueAnimation(e,i){let r=`__${e}Handler`,n=o=>{o&&o.target!==this||(i(),this.removeEventListener("animationend",n),delete this[r])};this[r]=n,this.addEventListener("animationend",n)}_flushAnimation(e){let i=`__${e}Handler`;typeof this[i]=="function"&&this[i]()}_animatedOpening(){this._isAttached&&this.hasAttribute("closing")&&this._flushAnimation("closing"),this._attachOverlay(),this._appendAttachedInstance(),this.bringToFront(),this.modeless||this._enterModalState(),k(this,"opening",!0),this._shouldAnimate()?this._enqueueAnimation("opening",()=>{this._finishOpening()}):this._finishOpening()}_attachOverlay(){this.matches(":popover-open")||this.showPopover()}_finishOpening(){k(this,"opening",!1)}_finishClosing(){this._detachOverlay(),this._removeAttachedInstance(),this.$.overlay.style.removeProperty("pointer-events"),k(this,"closing",!1),this.dispatchEvent(new CustomEvent("vaadin-overlay-closed"))}_animatedClosing(){this.hasAttribute("opening")&&this._flushAnimation("opening"),this._isAttached&&(this._exitModalState(),k(this,"closing",!0),this.dispatchEvent(new CustomEvent("vaadin-overlay-closing")),this._shouldAnimate()?this._enqueueAnimation("closing",()=>{this._finishClosing()}):this._finishClosing())}_detachOverlay(){this.hidePopover()}_mouseDownListener(e){this._mouseDownInside=e.composedPath().indexOf(this.$.overlay)>=0}_mouseUpListener(e){this._mouseUpInside=e.composedPath().indexOf(this.$.overlay)>=0}_shouldCloseOnOutsideClick(e){return this._last}_outsideClickListener(e){if(e.composedPath().includes(this.$.overlay)||this._mouseDownInside||this._mouseUpInside){this._mouseDownInside=!1,this._mouseUpInside=!1;return}if(!this._shouldCloseOnOutsideClick(e))return;let i=new CustomEvent("vaadin-overlay-outside-click",{cancelable:!0,detail:{sourceEvent:e}});this.dispatchEvent(i),this.opened&&!i.defaultPrevented&&(this.close(e),!this.opened&&!this.modeless&&e.preventDefault())}_keydownListener(e){if(!(!this._last||e.defaultPrevented)&&!(!this._shouldAddGlobalListeners()&&!e.composedPath().includes(this._focusTrapRoot))&&e.key==="Escape"){let i=new CustomEvent("vaadin-overlay-escape-press",{cancelable:!0,detail:{sourceEvent:e}});this.dispatchEvent(i),this.opened&&!i.defaultPrevented&&this.close(e)}}};function Ms(s){let t=[];for(;s;){if(s.nodeType===Node.DOCUMENT_NODE){t.push(s);break}if(s.nodeType===Node.DOCUMENT_FRAGMENT_NODE){t.push(s),s=s.host;continue}if(s.assignedSlot){s=s.assignedSlot;continue}s=s.parentNode}return t}function nt(s){return s?new Set(s.split(" ")):new Set}function Pe(s){return s?[...s].join(" "):""}function di(s,t,e){let i=nt(s.getAttribute(t));i.add(e),s.setAttribute(t,Pe(i))}function Ps(s,t,e){let i=nt(s.getAttribute(t));if(i.delete(e),i.size===0){s.removeAttribute(t);return}s.setAttribute(t,Pe(i))}function Os(s){return s.nodeType===Node.TEXT_NODE&&s.textContent.trim()===""}var hi={start:"top",end:"bottom"},ci={start:"left",end:"right"},Vs=new ResizeObserver(s=>{setTimeout(()=>{s.forEach(t=>{t.target.__overlay&&t.target.__overlay._updatePosition()})})}),ot=s=>class extends s{static get properties(){return{positionTarget:{type:Object,value:null,sync:!0},horizontalAlign:{type:String,value:"start",sync:!0},verticalAlign:{type:String,value:"top",sync:!0},noHorizontalOverlap:{type:Boolean,value:!1,sync:!0},noVerticalOverlap:{type:Boolean,value:!1,sync:!0},requiredVerticalSpace:{type:Number,value:0,sync:!0}}}constructor(){super(),this._hasOverlayPositionMixin=!0,this.__onScroll=this.__onScroll.bind(this),this._updatePosition=this._updatePosition.bind(this)}connectedCallback(){super.connectedCallback(),this.opened&&this.__addUpdatePositionEventListeners()}disconnectedCallback(){super.disconnectedCallback(),this.__removeUpdatePositionEventListeners()}updated(e){if(super.updated(e),e.has("positionTarget")){let r=e.get("positionTarget");this.__oldContentWidth=void 0,this.__oldContentHeight=void 0,(!this.positionTarget&&r||this.positionTarget&&!r&&this.__margins)&&this.__resetPosition()}(e.has("opened")||e.has("positionTarget"))&&this.__updatePositionSettings(this.opened,this.positionTarget),["horizontalAlign","verticalAlign","noHorizontalOverlap","noVerticalOverlap","requiredVerticalSpace"].some(r=>e.has(r))&&this._updatePosition()}__addUpdatePositionEventListeners(){window.visualViewport.addEventListener("resize",this._updatePosition),window.visualViewport.addEventListener("scroll",this.__onScroll,!0),this.__positionTargetAncestorRootNodes=Ms(this.positionTarget),this.__positionTargetAncestorRootNodes.forEach(e=>{e.addEventListener("scroll",this.__onScroll,!0)}),this.positionTarget&&(this.__observePositionTargetMove=As(this.positionTarget,()=>{this._updatePosition()}))}__removeUpdatePositionEventListeners(){window.visualViewport.removeEventListener("resize",this._updatePosition),window.visualViewport.removeEventListener("scroll",this.__onScroll,!0),this.__positionTargetAncestorRootNodes&&(this.__positionTargetAncestorRootNodes.forEach(e=>{e.removeEventListener("scroll",this.__onScroll,!0)}),this.__positionTargetAncestorRootNodes=null),this.__observePositionTargetMove&&(this.__observePositionTargetMove(),this.__observePositionTargetMove=null)}__updatePositionSettings(e,i){if(this.__removeUpdatePositionEventListeners(),i&&(i.__overlay=null,Vs.unobserve(i),e&&(this.__addUpdatePositionEventListeners(),i.__overlay=this,Vs.observe(i))),e){let r=getComputedStyle(this);this.__margins||(this.__margins={},["top","bottom","left","right"].forEach(n=>{this.__margins[n]=parseInt(r[n],10)})),this._updatePosition(),requestAnimationFrame(()=>this._updatePosition())}}__onScroll(e){e.target instanceof Node&&this._deepContains(e.target)||this._updatePosition()}__resetPosition(){this.__margins=null,Object.assign(this.style,{justifyContent:"",alignItems:"",top:"",bottom:"",left:"",right:""}),k(this,"bottom-aligned",!1),k(this,"top-aligned",!1),k(this,"end-aligned",!1),k(this,"start-aligned",!1)}_updatePosition(){if(!this.positionTarget||!this.opened||!this.__margins)return;let e=this.positionTarget.getBoundingClientRect();if(e.width===0&&e.height===0&&this.opened){this.opened=!1;return}let i=this.__shouldAlignStartVertically(e);this.style.justifyContent=i?"flex-start":"flex-end";let r=this.__isRTL,n=this.__shouldAlignStartHorizontally(e,r),o=!r&&n||r&&!n;this.style.alignItems=o?"flex-start":"flex-end";let a=this.getBoundingClientRect(),l=this.__calculatePositionInOneDimension(e,a,this.noVerticalOverlap,hi,this,i),d=this.__calculatePositionInOneDimension(e,a,this.noHorizontalOverlap,ci,this,n);Object.assign(this.style,l,d),k(this,"bottom-aligned",!i),k(this,"top-aligned",i),k(this,"end-aligned",!o),k(this,"start-aligned",o)}__shouldAlignStartHorizontally(e,i){let r=Math.max(this.__oldContentWidth||0,this.$.overlay.offsetWidth);this.__oldContentWidth=this.$.overlay.offsetWidth;let n=Math.min(window.innerWidth,document.documentElement.clientWidth),o=!i&&this.horizontalAlign==="start"||i&&this.horizontalAlign==="end";return this.__shouldAlignStart(e,r,n,this.__margins,o,this.noHorizontalOverlap,ci)}__shouldAlignStartVertically(e){let i=this.requiredVerticalSpace||Math.max(this.__oldContentHeight||0,this.$.overlay.offsetHeight);this.__oldContentHeight=this.$.overlay.offsetHeight;let r=Math.min(window.innerHeight,document.documentElement.clientHeight),n=this.verticalAlign==="top";return this.__shouldAlignStart(e,i,r,this.__margins,n,this.noVerticalOverlap,hi)}__shouldAlignStart(e,i,r,n,o,a,l){let d=r-e[a?l.end:l.start]-n[l.end],h=e[a?l.start:l.end]-n[l.start],c=o?d:h,f=c>(o?h:d)||c>i;return o===f}__adjustBottomProperty(e,i,r){let n;if(e===i.end){if(i.end===hi.end){let o=Math.min(window.innerHeight,document.documentElement.clientHeight);if(r>o&&this.__oldViewportHeight){let a=this.__oldViewportHeight-o;n=r-a}this.__oldViewportHeight=o}if(i.end===ci.end){let o=Math.min(window.innerWidth,document.documentElement.clientWidth);if(r>o&&this.__oldViewportWidth){let a=this.__oldViewportWidth-o;n=r-a}this.__oldViewportWidth=o}}return n}__calculatePositionInOneDimension(e,i,r,n,o,a){let l=a?n.start:n.end,d=a?n.end:n.start,h=parseFloat(o.style[l]||getComputedStyle(o)[l]),c=this.__adjustBottomProperty(l,n,h),p=i[a?n.start:n.end]-e[r===a?n.end:n.start],f=c?`${c}px`:`${h+p*(a?-1:1)}px`;return{[l]:f,[d]:""}}};var $s=s=>class extends ot(rt(s)){_shouldCloseOnOutsideClick(e){return!e.composedPath().includes(this.positionTarget)}_mouseDownListener(e){super._mouseDownListener(e),this._shouldCloseOnOutsideClick(e)&&!Ae(e.composedPath()[0])&&e.preventDefault()}};var ui=class extends $s(I(y(g(b(_))))){static get is(){return"vaadin-date-picker-overlay"}static get styles(){return[Ze,xs]}render(){return v`
      <div id="backdrop" part="backdrop" ?hidden="${!this.withBackdrop}"></div>
      <div part="overlay" id="overlay">
        <div part="content" id="content">
          <slot></slot>
        </div>
      </div>
    `}get _contentRoot(){return this.owner._overlayContent}};m(ui);var Ls=function(){};var Ns=0,Fs=0,ae=[],pi=!1;function Mn(){pi=!1;let s=ae.length;for(let t=0;t<s;t++){let e=ae[t];if(e)try{e()}catch(i){setTimeout(()=>{throw i})}}ae.splice(0,s),Fs+=s}var A={after(s){return{run(t){return window.setTimeout(t,s)},cancel(t){window.clearTimeout(t)}}},run(s,t){return window.setTimeout(s,t)},cancel(s){window.clearTimeout(s)}};var Z={run(s){return window.requestAnimationFrame(s)},cancel(s){window.cancelAnimationFrame(s)}};var at={run(s){return window.requestIdleCallback?window.requestIdleCallback(s):window.setTimeout(s,16)},cancel(s){window.cancelIdleCallback?window.cancelIdleCallback(s):window.clearTimeout(s)}};var Q={run(s){pi||(pi=!0,queueMicrotask(()=>Mn())),ae.push(s);let t=Ns;return Ns+=1,t},cancel(s){let t=s-Fs;if(t>=0){if(!ae[t])throw new Error(`invalid async handle: ${s}`);ae[t]=null}}};var Oe=new Set,w=class s{static debounce(t,e,i){return t instanceof s?t._cancelAsync():t=new s,t.setConfig(e,i),t}constructor(){this._asyncModule=null,this._callback=null,this._timer=null}setConfig(t,e){this._asyncModule=t,this._callback=e,this._timer=this._asyncModule.run(()=>{this._timer=null,Oe.delete(this),this._callback()})}cancel(){this.isActive()&&(this._cancelAsync(),Oe.delete(this))}_cancelAsync(){this.isActive()&&(this._asyncModule.cancel(this._timer),this._timer=null)}flush(){this.isActive()&&(this.cancel(),this._callback())}isActive(){return this._timer!=null}};function lt(s){Oe.add(s)}function Pn(){let s=!!Oe.size;return Oe.forEach(t=>{try{t.flush()}catch(e){setTimeout(()=>{throw e})}}),s}var J=()=>{let s;do s=Pn();while(s)};window.Vaadin||(window.Vaadin={});window.Vaadin.registrations||(window.Vaadin.registrations=[]);window.Vaadin.developmentModeCallback||(window.Vaadin.developmentModeCallback={});window.Vaadin.developmentModeCallback["vaadin-usage-statistics"]=function(){Ls()};var _i,Bs=new Set,U=s=>class extends I(s){static _ensureRegistrations(){let{is:e}=this;if(e&&!Bs.has(e)){window.Vaadin.registrations.push(this),Bs.add(e);let i=window.Vaadin.developmentModeCallback;i&&(_i=w.debounce(_i,at,()=>{i["vaadin-usage-statistics"]()}),lt(_i))}}constructor(){super(),document.doctype===null&&console.warn('Vaadin components require the "standards mode" declaration. Please add <!DOCTYPE html> to the HTML document.'),this.constructor._ensureRegistrations()}};var dt=class{constructor(t,e,i={}){this.target=t,this.callback=e,this.forceInitial=i.forceInitial,this._storedNodes=[],this._isSlot=t instanceof HTMLSlotElement,this._connected=!1,this._scheduled=!1,this._boundSchedule=()=>{this._schedule()},this.connect(),i.syncInitial?this.flush():this._schedule()}connect(){this.target.addEventListener("slotchange",this._boundSchedule),this._connected=!0}disconnect(){this.target.removeEventListener("slotchange",this._boundSchedule),this._connected=!1}_schedule(){this._scheduled||(this._scheduled=!0,queueMicrotask(()=>{this._scheduled&&this.flush()}))}flush(){this._connected&&(this._scheduled=!1,this._processNodes())}_collectNodes(){let t=this._isSlot?[this.target]:[...this.target.querySelectorAll("slot")];return[...new Set(t.flatMap(e=>e.assignedNodes({flatten:!0})))]}_groupNodesBySlot(t){let e=new Map;return t.forEach(i=>{let r=i.assignedSlot;e.set(r,e.get(r)??[]),e.get(r).push(i)}),e}_collectMovedNodes(t){let e=this._groupNodesBySlot(t),i=this._groupNodesBySlot(this._storedNodes),r=[];return e.forEach((n,o)=>{let a=i.get(o)||[];new Set(a).difference(new Set(n)).size>0||a.forEach((l,d)=>{n.indexOf(l)!==d&&r.push(l)})}),r}_processNodes(){let t=this._collectNodes(),e=t.filter(n=>!this._storedNodes.includes(n)),i=this._storedNodes.filter(n=>!t.includes(n)),r=this._collectMovedNodes(t);(e.length||i.length||r.length||this.forceInitial)&&this.callback({addedNodes:e,currentNodes:t,movedNodes:r,removedNodes:i}),this.forceInitial&&(this.forceInitial=!1),this._storedNodes=t}};var On=0;function le(){return On++}var D=class extends EventTarget{static generateId(t,e="default"){return`${e}-${t.localName}-${le()}`}constructor(t,e,i,r={}){super();let{initializer:n,multiple:o,observe:a,useUniqueId:l,uniqueIdPrefix:d}=r;this.host=t,this.slotName=e,this.tagName=i,this.observe=typeof a=="boolean"?a:!0,this.multiple=typeof o=="boolean"?o:!1,this.slotInitializer=n,o&&(this.nodes=[]),l&&(this.defaultId=this.constructor.generateId(t,d||e))}hostConnected(){this.initialized||(this.multiple?this.initMultiple():this.initSingle(),this.observe&&this.observeSlot(),this.initialized=!0)}initSingle(){let t=this.getSlotChild();t?(this.node=t,this.initAddedNode(t)):(t=this.attachDefaultNode(),this.initNode(t))}initMultiple(){let t=this.getSlotChildren();if(t.length===0){let e=this.attachDefaultNode();e&&(this.nodes=[e],this.initNode(e))}else this.nodes=t,t.forEach(e=>{this.initAddedNode(e)})}attachDefaultNode(){let{host:t,slotName:e,tagName:i}=this,r=this.defaultNode;return!r&&i&&(r=document.createElement(i),r instanceof Element&&(e!==""&&r.setAttribute("slot",e),this.defaultNode=r)),r&&(this.node=r,t.appendChild(r)),r}getSlotChildren(){let{slotName:t}=this;return Array.from(this.host.childNodes).filter(e=>e.nodeType===Node.ELEMENT_NODE&&e.hasAttribute("data-slot-ignore")?!1:e.nodeType===Node.ELEMENT_NODE&&e.slot===t||e.nodeType===Node.TEXT_NODE&&e.textContent.trim()&&t==="")}getSlotChild(){return this.getSlotChildren()[0]}initNode(t){let{slotInitializer:e}=this;e&&e(t,this.host)}initCustomNode(t){}teardownNode(t){}initAddedNode(t){t!==this.defaultNode&&(this.initCustomNode(t),this.initNode(t))}observeSlot(){let{slotName:t}=this,e=t===""?"slot:not([name])":`slot[name=${t}]`,i=this.host.shadowRoot.querySelector(e);this.__slotObserver=new dt(i,({addedNodes:r,removedNodes:n})=>{let o=this.multiple?this.nodes:[this.node],a=r.filter(l=>!Os(l)&&!o.includes(l)&&!(l.nodeType===Node.ELEMENT_NODE&&l.hasAttribute("data-slot-ignore")));n.length&&(this.nodes=o.filter(l=>!n.includes(l)),n.forEach(l=>{this.teardownNode(l)})),a?.length>0&&(this.multiple?(this.defaultNode&&this.defaultNode.remove(),this.nodes=[...o,...a].filter(l=>l!==this.defaultNode),a.forEach(l=>{this.initAddedNode(l)})):(this.node&&this.node.remove(),this.node=a[0],this.initAddedNode(this.node)))})}};var O=class extends D{constructor(t){super(t,"tooltip"),this.setTarget(t),this.__onContentChange=this.__onContentChange.bind(this)}initCustomNode(t){t.target=this.target,this.ariaTarget!==void 0&&(t.ariaTarget=this.ariaTarget),this.context!==void 0&&(t.context=this.context),this.manual!==void 0&&(t.manual=this.manual),this.position!==void 0&&(t._position=this.position),this.shouldShow!==void 0&&(t.shouldShow=this.shouldShow),this.manual||this.host.setAttribute("has-tooltip",""),this.__notifyChange(t),t.addEventListener("content-changed",this.__onContentChange)}teardownNode(t){this.manual||this.host.removeAttribute("has-tooltip"),t.removeEventListener("content-changed",this.__onContentChange),this.__notifyChange(null)}setAriaTarget(t){this.ariaTarget=t;let e=this.node;e&&(e.ariaTarget=t)}setContext(t){this.context=t;let e=this.node;e&&(e.context=t)}setManual(t){this.manual=t;let e=this.node;e&&(e.manual=t)}setPosition(t){this.position=t;let e=this.node;e&&(e._position=t)}setShouldShow(t){this.shouldShow=t;let e=this.node;e&&(e.shouldShow=t)}setTarget(t){this.target=t;let e=this.node;e&&(e.target=t)}open(t){let e=this.node;e?.isConnected&&e._stateController.open(t)}close(t){let e=this.node;e&&e._stateController.close(t)}__onContentChange(t){this.__notifyChange(t.target)}__notifyChange(t){this.dispatchEvent(new CustomEvent("tooltip-changed",{detail:{node:t}}))}};var Hs=u`
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
`;var Vn=!1,$n=s=>s,bi=typeof document.head.style.touchAction=="string",vi="__polymerGestures",fi="__polymerGesturesHandled",gi="__polymerGesturesTouchAction",Rs=25,zs=5,Ln=2,Nn=["mousedown","mousemove","mouseup","click"],Fn=[0,1,4,2],Bn=(function(){try{return new MouseEvent("test",{buttons:1}).buttons===1}catch{return!1}})();function yi(s){return Nn.indexOf(s)>-1}var qs=!1;(function(){try{let s=Object.defineProperty({},"passive",{get(){qs=!0}});window.addEventListener("test",null,s),window.removeEventListener("test",null,s)}catch{}})();function Hn(s){if(!(yi(s)||s==="touchend")&&bi&&qs&&Vn)return{passive:!0}}var Rn=navigator.userAgent.match(/iP(?:[oa]d|hone)|Android/u),zn={button:!0,command:!0,fieldset:!0,input:!0,keygen:!0,optgroup:!0,option:!0,select:!0,textarea:!0};function te(s){let t=s.type;if(!yi(t))return!1;if(t==="mousemove"){let i=s.buttons??1;return s instanceof window.MouseEvent&&!Bn&&(i=Fn[s.which]||0),!!(i&1)}return(s.button??0)===0}function Un(s){if(s.type==="click"){if(s.detail===0)return!0;let t=j(s);if(!t.nodeType||t.nodeType!==Node.ELEMENT_NODE)return!0;let e=t.getBoundingClientRect(),i=s.pageX,r=s.pageY;return!(i>=e.left&&i<=e.right&&r>=e.top&&r<=e.bottom)}return!1}var H={mouse:{target:null,mouseIgnoreJob:null},touch:{x:0,y:0,id:-1,scrollDecided:!1}};function jn(s){let t="auto",e=Ys(s);for(let i=0,r;i<e.length;i++)if(r=e[i],r[gi]){t=r[gi];break}return t}function Ws(s,t,e){s.movefn=t,s.upfn=e,document.addEventListener("mousemove",t),document.addEventListener("mouseup",e)}function de(s){document.removeEventListener("mousemove",s.movefn),document.removeEventListener("mouseup",s.upfn),s.movefn=null,s.upfn=null}var Ys=window.ShadyDOM&&window.ShadyDOM.noPatch?window.ShadyDOM.composedPath:s=>s.composedPath&&s.composedPath()||[],xi={},ee=[];function qn(s,t){let e=document.elementFromPoint(s,t),i=e;for(;i?.shadowRoot&&!window.ShadyDOM;){let r=i;if(i=i.shadowRoot.elementFromPoint(s,t),r===i)break;i&&(e=i)}return e}function j(s){let t=Ys(s);return t.length>0?t[0]:s.target}function Wn(s){let t=s.type,i=s.currentTarget[vi];if(!i)return;let r=i[t];if(!r)return;if(!s[fi]&&(s[fi]={},t.startsWith("touch"))){let o=s.changedTouches[0];if(t==="touchstart"&&s.touches.length===1&&(H.touch.id=o.identifier),H.touch.id!==o.identifier)return;bi||(t==="touchstart"||t==="touchmove")&&Yn(s)}let n=s[fi];if(!n.skip){for(let o=0,a;o<ee.length;o++)a=ee[o],r[a.name]&&!n[a.name]&&a.flow&&a.flow.start.indexOf(s.type)>-1&&a.reset&&a.reset();for(let o=0,a;o<ee.length;o++)a=ee[o],r[a.name]&&!n[a.name]&&(n[a.name]=!0,a[t](s))}}function Yn(s){let t=s.changedTouches[0],e=s.type;if(e==="touchstart")H.touch.x=t.clientX,H.touch.y=t.clientY,H.touch.scrollDecided=!1;else if(e==="touchmove"){if(H.touch.scrollDecided)return;H.touch.scrollDecided=!0;let i=jn(s),r=!1,n=Math.abs(H.touch.x-t.clientX),o=Math.abs(H.touch.y-t.clientY);s.cancelable&&(i==="none"?r=!0:i==="pan-x"?r=o>n:i==="pan-y"&&(r=n>o)),r?s.preventDefault():ht("track")}}function ie(s,t,e){return xi[t]?(Kn(s,t,e),!0):!1}function Kn(s,t,e){let i=xi[t],r=i.deps,n=i.name,o=s[vi];o||(s[vi]=o={});for(let a=0,l,d;a<r.length;a++)l=r[a],!(Rn&&yi(l)&&l!=="click")&&(d=o[l],d||(o[l]=d={_count:0}),d._count===0&&s.addEventListener(l,Wn,Hn(l)),d[n]=(d[n]||0)+1,d._count=(d._count||0)+1);s.addEventListener(t,e),i.touchAction&&Xn(s,i.touchAction)}function Ci(s){ee.push(s),s.emits.forEach(t=>{xi[t]=s})}function Gn(s){for(let t=0,e;t<ee.length;t++){e=ee[t];for(let i=0,r;i<e.emits.length;i++)if(r=e.emits[i],r===s)return e}return null}function Xn(s,t){bi&&s instanceof HTMLElement&&Q.run(()=>{s.style.touchAction=t}),s[gi]=t}function wi(s,t,e){let i=new Event(t,{bubbles:!0,cancelable:!0,composed:!0});if(i.detail=e,$n(s).dispatchEvent(i),i.defaultPrevented){let r=e.preventer||e.sourceEvent;r?.preventDefault&&r.preventDefault()}}function ht(s){let t=Gn(s);t.info&&(t.info.prevent=!0)}Ci({name:"downup",deps:["mousedown","touchstart","touchend"],flow:{start:["mousedown","touchstart"],end:["mouseup","touchend"]},emits:["down","up"],info:{movefn:null,upfn:null},reset(){de(this.info)},mousedown(s){if(!te(s))return;let t=j(s),e=this,i=n=>{te(n)||(Ve("up",t,n),de(e.info))},r=n=>{te(n)&&Ve("up",t,n),de(e.info)};Ws(this.info,i,r),Ve("down",t,s)},touchstart(s){Ve("down",j(s),s.changedTouches[0],s)},touchend(s){Ve("up",j(s),s.changedTouches[0],s)}});function Ve(s,t,e,i){t&&wi(t,s,{x:e.clientX,y:e.clientY,sourceEvent:e,preventer:i,prevent(r){return ht(r)}})}Ci({name:"track",touchAction:"none",deps:["mousedown","touchstart","touchmove","touchend"],flow:{start:["mousedown","touchstart"],end:["mouseup","touchend"]},emits:["track"],info:{x:0,y:0,state:"start",started:!1,moves:[],addMove(s){this.moves.length>Ln&&this.moves.shift(),this.moves.push(s)},movefn:null,upfn:null,prevent:!1},reset(){this.info.state="start",this.info.started=!1,this.info.moves=[],this.info.x=0,this.info.y=0,this.info.prevent=!1,de(this.info)},mousedown(s){if(!te(s))return;let t=j(s),e=this,i=n=>{let o=n.clientX,a=n.clientY;Us(e.info,o,a)&&(e.info.state=e.info.started?n.type==="mouseup"?"end":"track":"start",e.info.state==="start"&&ht("tap"),e.info.addMove({x:o,y:a}),te(n)||(e.info.state="end",de(e.info)),t&&mi(e.info,t,n),e.info.started=!0)},r=n=>{e.info.started&&i(n),de(e.info)};Ws(this.info,i,r),this.info.x=s.clientX,this.info.y=s.clientY},touchstart(s){let t=s.changedTouches[0];this.info.x=t.clientX,this.info.y=t.clientY},touchmove(s){let t=j(s),e=s.changedTouches[0],i=e.clientX,r=e.clientY;Us(this.info,i,r)&&(this.info.state==="start"&&ht("tap"),this.info.addMove({x:i,y:r}),mi(this.info,t,e),this.info.state="track",this.info.started=!0)},touchend(s){let t=j(s),e=s.changedTouches[0];this.info.started&&(this.info.state="end",this.info.addMove({x:e.clientX,y:e.clientY}),mi(this.info,t,e))}});function Us(s,t,e){if(s.prevent)return!1;if(s.started)return!0;let i=Math.abs(s.x-t),r=Math.abs(s.y-e);return i>=zs||r>=zs}function mi(s,t,e){if(!t)return;let i=s.moves[s.moves.length-2],r=s.moves[s.moves.length-1],n=r.x-s.x,o=r.y-s.y,a,l=0;i&&(a=r.x-i.x,l=r.y-i.y),wi(t,"track",{state:s.state,x:e.clientX,y:e.clientY,dx:n,dy:o,ddx:a,ddy:l,sourceEvent:e,hover(){return qn(e.clientX,e.clientY)}})}Ci({name:"tap",deps:["mousedown","click","touchstart","touchend"],flow:{start:["mousedown","touchstart"],end:["click","touchend"]},emits:["tap"],info:{x:NaN,y:NaN,prevent:!1},reset(){this.info.x=NaN,this.info.y=NaN,this.info.prevent=!1},mousedown(s){te(s)&&(this.info.x=s.clientX,this.info.y=s.clientY)},click(s){te(s)&&js(this.info,s)},touchstart(s){let t=s.changedTouches[0];this.info.x=t.clientX,this.info.y=t.clientY},touchend(s){js(this.info,s.changedTouches[0],s)}});function js(s,t,e){let i=Math.abs(t.clientX-s.x),r=Math.abs(t.clientY-s.y),n=j(e||t);!n||zn[n.localName]&&n.hasAttribute("disabled")||(isNaN(i)||isNaN(r)||i<=Rs&&r<=Rs||Un(t))&&(s.prevent||wi(n,"tap",{x:t.clientX,y:t.clientY,sourceEvent:t,preventer:e}))}var Zn=s=>class extends s{static get properties(){return{disabled:{type:Boolean,value:!1,observer:"_disabledChanged",reflectToAttribute:!0,sync:!0}}}_disabledChanged(e){this._setAriaDisabled(e)}_setAriaDisabled(e){e?this.setAttribute("aria-disabled","true"):this.removeAttribute("aria-disabled")}click(){this.disabled||super.click()}},q=x(Zn);var Qn=s=>class extends s{ready(){super.ready(),this.addEventListener("keydown",e=>{this._onKeyDown(e)}),this.addEventListener("keyup",e=>{this._onKeyUp(e)})}_onKeyDown(e){switch(e.key){case"Enter":this._onEnter(e);break;case"Escape":this._onEscape(e);break;default:break}}_onKeyUp(e){}_onEnter(e){}_onEscape(e){}},V=x(Qn);var Ks=s=>class extends q(V(s)){get _activeKeys(){return[" "]}ready(){super.ready(),ie(this,"down",e=>{this._shouldSetActive(e)&&this._setActive(!0)}),ie(this,"up",()=>{this._setActive(!1)})}disconnectedCallback(){super.disconnectedCallback(),this._setActive(!1)}_shouldSetActive(e){return!this.disabled}_onKeyDown(e){super._onKeyDown(e),this._shouldSetActive(e)&&this._activeKeys.includes(e.key)&&(this._setActive(!0),document.addEventListener("keyup",i=>{this._activeKeys.includes(i.key)&&this._setActive(!1)},{once:!0}))}_setActive(e){this.toggleAttribute("active",e)}};var Jn=s=>class extends s{get _keyboardActive(){return T()}ready(){this.addEventListener("focusin",e=>{this._shouldSetFocus(e)&&this._setFocused(!0)}),this.addEventListener("focusout",e=>{this._shouldRemoveFocus(e)&&this._setFocused(!1)}),super.ready()}disconnectedCallback(){super.disconnectedCallback(),this.hasAttribute("focused")&&this._setFocused(!1)}focus(e){super.focus(e),e?.focusVisible!==!1&&this.setAttribute("focus-ring","")}_setFocused(e){this.toggleAttribute("focused",e),this.toggleAttribute("focus-ring",e&&this._keyboardActive)}_shouldSetFocus(e){return!0}_shouldRemoveFocus(e){return!0}},$=x(Jn);var ct=s=>class extends q(s){static get properties(){return{tabindex:{type:Number,reflectToAttribute:!0,observer:"_tabindexChanged",sync:!0},_lastTabIndex:{type:Number}}}_disabledChanged(e,i){super._disabledChanged(e,i),!this.__shouldAllowFocusWhenDisabled()&&(e?(this.tabindex!==void 0&&(this._lastTabIndex=this.tabindex),this.setAttribute("tabindex","-1")):i&&(this._lastTabIndex!==void 0?this.setAttribute("tabindex",this._lastTabIndex):this.tabindex=void 0))}_tabindexChanged(e){this.__shouldAllowFocusWhenDisabled()||this.disabled&&e!==-1&&(this._lastTabIndex=e,this.setAttribute("tabindex","-1"))}focus(e){(!this.disabled||this.__shouldAllowFocusWhenDisabled())&&super.focus(e)}__shouldAllowFocusWhenDisabled(){return!1}};var eo=["mousedown","mouseup","click","dblclick","keypress","keydown","keyup"],Gs=s=>class extends Ks(ct($(s))){constructor(){super(),this.__onInteractionEvent=this.__onInteractionEvent.bind(this),eo.forEach(e=>{this.addEventListener(e,this.__onInteractionEvent,!0)}),this.tabindex=0}get _activeKeys(){return["Enter"," "]}ready(){super.ready(),this.hasAttribute("role")||this.setAttribute("role","button"),this.__shouldAllowFocusWhenDisabled()&&this.style.setProperty("--_vaadin-button-disabled-pointer-events","auto")}_onKeyDown(e){super._onKeyDown(e),!(e.altKey||e.shiftKey||e.ctrlKey||e.metaKey)&&this._activeKeys.includes(e.key)&&(e.preventDefault(),this.click())}__onInteractionEvent(e){this.__shouldSuppressInteractionEvent(e)&&e.stopImmediatePropagation()}__shouldSuppressInteractionEvent(e){return this.disabled}};var Si=class extends Gs(U(y(g(b(_))))){static get is(){return"vaadin-button"}static get styles(){return Hs}static get properties(){return{disabled:{type:Boolean,value:!1,observer:"_disabledChanged",reflectToAttribute:!0,sync:!0}}}render(){return v`
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
    `}ready(){super.ready(),this._tooltipController=new O(this),this.addController(this._tooltipController)}__shouldAllowFocusWhenDisabled(){return window.Vaadin.featureFlags.accessibleDisabledButtons}};m(Si);function Xs(s){let t=s.getDay();t===0&&(t=7);let e=4-t,i=new Date(s.getTime()+e*24*3600*1e3),r=new Date(0,0);r.setFullYear(i.getFullYear());let n=i.getTime()-r.getTime(),o=Math.round(n/(24*3600*1e3));return Math.floor(o/7+1)}function ut(s){let t=new Date(s);return t.setHours(0,0,0,0),t}function $e(s){return new Date(Date.UTC(s.getUTCFullYear(),s.getUTCMonth(),s.getUTCDate(),0,0,0,0))}function S(s,t,e=ut){return s instanceof Date&&t instanceof Date&&e(s).getTime()===e(t).getTime()}function Ei(s){return{day:s.getDate(),month:s.getMonth(),year:s.getFullYear()}}function R(s,t,e,i){let r=!1;if(typeof i=="function"&&s){let n=Ei(s);r=i(n)}return(!t||s>=t)&&(!e||s<=e)&&!r}function pt(s,t){return t.filter(e=>e!==void 0).reduce((e,i)=>{if(!i)return e;if(!e)return i;let r=Math.abs(s.getTime()-i.getTime()),n=Math.abs(e.getTime()-s.getTime());return r<n?i:e})}function _t(s){let t=new Date,e=new Date(t);return e.setDate(1),e.setMonth(parseInt(s)+t.getMonth()),e}function Zs(s,t,e=0,i=1){if(t>99)throw new Error("The provided year cannot have more than 2 digits.");if(t<0)throw new Error("The provided year cannot be negative.");let r=t+Math.floor(s.getFullYear()/100)*100;return s<new Date(r-50,e,i)?r-=100:s>new Date(r+50,e,i)&&(r+=100),r}function he(s){let t=/^([-+]\d{1}|\d{2,4}|[-+]\d{6})-(\d{1,2})-(\d{1,2})$/u.exec(s);if(!t)return;let e=new Date(0,0);return e.setFullYear(parseInt(t[1],10)),e.setMonth(parseInt(t[2],10)-1),e.setDate(parseInt(t[3],10)),e}function Qs(s){let t=/^([-+]\d{1}|\d{2,4}|[-+]\d{6})-(\d{1,2})-(\d{1,2})$/u.exec(s);if(!t)return;let e=new Date(Date.UTC(0,0));return e.setUTCFullYear(parseInt(t[1],10)),e.setUTCMonth(parseInt(t[2],10)-1),e.setUTCDate(parseInt(t[3],10)),e}function Js(s){let t=(l,d="00")=>(d+l).substr((d+l).length-d.length),e="",i="0000",r=s.year;r<0?(r=-r,e="-",i="000000"):s.year>=1e4&&(e="+",i="000000");let n=e+t(r,i),o=t(s.month+1),a=t(s.day);return[n,o,a].join("-")}function er(s){return s instanceof Date?Js({year:s.getFullYear(),month:s.getMonth(),day:s.getDate()}):""}function tr(s){return s instanceof Date?Js({year:s.getUTCFullYear(),month:s.getUTCMonth(),day:s.getUTCDate()}):""}var ir=document.createElement("template");ir.innerHTML=`
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
`;var ce=class extends HTMLElement{constructor(){super(),this.attachShadow({mode:"open"}).appendChild(ir.content.cloneNode(!0)),this.bufferSize=20,this._initialScroll=5e5,this._initialIndex=0,this._activated=!1}get active(){return this._activated}set active(t){t&&!this._activated&&(this._createPool(),this._activated=!0)}get bufferOffset(){return this._buffers[0].offsetTop}get itemHeight(){if(!this._itemHeightVal){let t=getComputedStyle(this).getPropertyValue("--vaadin-infinite-scroller-item-height"),e="background-position";this.$.fullHeight.style.setProperty(e,t);let i=getComputedStyle(this.$.fullHeight).getPropertyValue(e);this.$.fullHeight.style.removeProperty(e),this._itemHeightVal=parseFloat(i)}return this._itemHeightVal}get _bufferHeight(){return this.itemHeight*this.bufferSize}get position(){return(this.$.scroller.scrollTop-this._buffers[0].translateY)/this.itemHeight+this._firstIndex}set position(t){this._preventScrollEvent=!0,t>this._firstIndex&&t<this._firstIndex+this.bufferSize*2?this.$.scroller.scrollTop=this.itemHeight*(t-this._firstIndex)+this._buffers[0].translateY:(this._initialIndex=~~t,this._reset(),this._scrollDisabled=!0,this.$.scroller.scrollTop+=t%1*this.itemHeight,this._scrollDisabled=!1)}connectedCallback(){this._ready||(this._ready=!0,this.$={},this.shadowRoot.querySelectorAll("[id]").forEach(t=>{this.$[t.id]=t}),this.$.scroller.addEventListener("scroll",()=>this._scroll()),this._buffers=[...this.shadowRoot.querySelectorAll(".buffer")],this.$.fullHeight.style.height=`${this._initialScroll*2}px`)}disconnectedCallback(){this._debouncerScrollFinish&&this._debouncerScrollFinish.cancel(),this._debouncerUpdateClones&&this._debouncerUpdateClones.cancel(),this.__pendingFinishInit&&cancelAnimationFrame(this.__pendingFinishInit)}forceUpdate(){this._debouncerScrollFinish&&this._debouncerScrollFinish.flush(),this._debouncerUpdateClones&&(this._buffers[0].updated=this._buffers[1].updated=!1,this._updateClones(),this._debouncerUpdateClones.cancel())}_createElement(){}_updateElement(t,e){}_finishInit(){this._initDone||(this._buffers.forEach(t=>{[...t.children].forEach(e=>{this._ensureStampedInstance(e._itemWrapper)})}),this._buffers[0].translateY||this._reset(),this._initDone=!0,this.dispatchEvent(new CustomEvent("init-done")))}_translateBuffer(t){let e=t?1:0;this._buffers[e].translateY=this._buffers[e?0:1].translateY+this._bufferHeight*(e?-1:1),this._buffers[e].style.transform=`translate3d(0, ${this._buffers[e].translateY}px, 0)`,this._buffers[e].updated=!1,this._buffers.reverse()}_scroll(){if(this._scrollDisabled)return;let t=this.$.scroller.scrollTop;(t<this._bufferHeight||t>this._initialScroll*2-this._bufferHeight)&&(this._initialIndex=~~this.position,this._reset());let e=this.itemHeight+this.bufferOffset,i=t>this._buffers[1].translateY+e,r=t<this._buffers[0].translateY+e;(i||r)&&(this._translateBuffer(r),this._updateClones()),this._preventScrollEvent||this.dispatchEvent(new CustomEvent("custom-scroll",{bubbles:!1,composed:!0})),this._preventScrollEvent=!1,this._debouncerScrollFinish=w.debounce(this._debouncerScrollFinish,A.after(200),()=>{let n=this.$.scroller.getBoundingClientRect();!this._isVisible(this._buffers[0],n)&&!this._isVisible(this._buffers[1],n)&&(this.position=this.position)})}_reset(){this._scrollDisabled=!0,this.$.scroller.scrollTop=this._initialScroll,this._buffers[0].translateY=this._initialScroll-this._bufferHeight,this._buffers[1].translateY=this._initialScroll,this._buffers.forEach(t=>{t.style.transform=`translate3d(0, ${t.translateY}px, 0)`}),this._buffers[0].updated=this._buffers[1].updated=!1,this._updateClones(!0),this._debouncerUpdateClones=w.debounce(this._debouncerUpdateClones,A.after(200),()=>{this._buffers[0].updated=this._buffers[1].updated=!1,this._updateClones()}),this._scrollDisabled=!1}_createPool(){let t=this.innerHeight;this._buffers.forEach(e=>{for(let i=0;i<this.bufferSize;i++){let r=document.createElement("div");r.style.height=`${this.itemHeight}px`,r.instance={};let n=`vaadin-infinite-scroller-item-content-${le()}`,o=document.createElement("slot");o.setAttribute("name",n),o._itemWrapper=r,e.appendChild(o),r.setAttribute("slot",n),this.appendChild(r),this.itemHeight*i<=t&&this._ensureStampedInstance(r)}}),this.__pendingFinishInit=requestAnimationFrame(()=>{this._finishInit(),this.__pendingFinishInit=null})}_ensureStampedInstance(t){if(t.firstElementChild)return;let e=t.instance;t.instance=this._createElement(),t.appendChild(t.instance),Object.keys(e).forEach(i=>{t.instance[i]=e[i]})}_updateClones(t){this._firstIndex=Math.round((this._buffers[0].translateY-this._initialScroll)/this.itemHeight)+this._initialIndex;let e=t?this.$.scroller.getBoundingClientRect():void 0;this._buffers.forEach((i,r)=>{if(!i.updated){let n=this._firstIndex+this.bufferSize*r;[...i.children].forEach((o,a)=>{let l=o._itemWrapper;(!t||this._isVisible(l,e))&&this._updateElement(l.instance,n+a)}),i.updated=!0}})}_isVisible(t,e){let i=t.getBoundingClientRect();return i.bottom>e.top&&i.top<e.bottom}};var sr=document.createElement("template");sr.innerHTML=`
  <style>
    :host {
      --vaadin-infinite-scroller-item-height: 270px;
      grid-area: months;
      height: auto;
    }
  </style>
`;var ki=class extends ce{static get is(){return"vaadin-date-picker-month-scroller"}constructor(){super(),this.bufferSize=3,this.shadowRoot.appendChild(sr.content.cloneNode(!0))}_createElement(){return document.createElement("vaadin-month-calendar")}_updateElement(t,e){t.month=_t(e)}};m(ki);var rr=document.createElement("template");rr.innerHTML=`
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
`;var Di=class extends ce{static get is(){return"vaadin-date-picker-year-scroller"}constructor(){super(),this.bufferSize=12,this.shadowRoot.appendChild(rr.content.cloneNode(!0))}_createElement(){return document.createElement("vaadin-date-picker-year")}_updateElement(t,e){t.year=this._yearAfterXYears(e)}_yearAfterXYears(t){let e=new Date,i=new Date(e);return i.setFullYear(parseInt(t)+e.getFullYear()),i.getFullYear()}};m(Di);var nr=u`
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
`;var Ii=class extends y(g(b(_))){static get is(){return"vaadin-date-picker-year"}static get styles(){return nr}static get properties(){return{year:{type:String,sync:!0},selectedDate:{type:Object,sync:!0}}}render(){return v`
      <div part="year-number">${this.year}</div>
      <div part="year-separator" aria-hidden="true"></div>
    `}updated(t){super.updated(t),t.has("year")&&this.toggleAttribute("current",this.year===new Date().getFullYear()),(t.has("year")||t.has("selectedDate"))&&this.toggleAttribute("selected",this.selectedDate&&this.selectedDate.getFullYear()===this.year)}};m(Ii);var or=u`
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
`;var ar=s=>class extends $(s){static get properties(){return{month:{type:Object,value:new Date,sync:!0},selectedDate:{type:Object,notify:!0,sync:!0},focusedDate:{type:Object},showWeekNumbers:{type:Boolean,value:!1},i18n:{type:Object},ignoreTaps:{type:Boolean},minDate:{type:Date,value:null,sync:!0},maxDate:{type:Date,value:null,sync:!0},isDateDisabled:{type:Function,value:()=>!1},enteredDate:{type:Date},disabled:{type:Boolean,reflectToAttribute:!0,computed:"__computeDisabled(month, minDate, maxDate)"},_days:{type:Array,computed:"__computeDays(month, i18n, minDate, maxDate, isDateDisabled)"},_weeks:{type:Array,computed:"__computeWeeks(_days)"},_notTapping:{type:Boolean},__hasFocus:{type:Boolean}}}static get observers(){return["__focusedDateChanged(focusedDate, _days)","_showWeekNumbersChanged(showWeekNumbers, i18n)"]}get focusableDateElement(){return[...this.shadowRoot.querySelectorAll("[part~=date]")].find(e=>S(e.date,this.focusedDate))}ready(){super.ready(),ie(this.$.monthGrid,"tap",this._handleTap.bind(this))}_setFocused(e){super._setFocused(e),this.__hasFocus=e}__computeDisabled(e,i,r){let n=new Date(0,0);n.setFullYear(e.getFullYear()),n.setMonth(e.getMonth()),n.setDate(1);let o=new Date(0,0);return o.setFullYear(e.getFullYear()),o.setMonth(e.getMonth()+1),o.setDate(0),i&&r&&i.getMonth()===r.getMonth()&&i.getMonth()===e.getMonth()&&r.getDate()-i.getDate()>=0?!1:!R(n,i,r)&&!R(o,i,r)}_getTitle(e,i){if(!(e===void 0||i===void 0))return i.formatTitle(i.monthNames[e.getMonth()],e.getFullYear())}_onMonthGridTouchStart(){this._notTapping=!1,setTimeout(()=>{this._notTapping=!0},300)}_dateAdd(e,i){e.setDate(e.getDate()+i)}_applyFirstDayOfWeek(e,i){if(!(e===void 0||i===void 0))return e.slice(i).concat(e.slice(0,i))}__computeWeekDayNames(e,i){if(e===void 0||i===void 0)return[];let{weekdays:r,weekdaysShort:n,firstDayOfWeek:o}=e,a=this._applyFirstDayOfWeek(n,o);return this._applyFirstDayOfWeek(r,o).map((d,h)=>({weekDay:d,weekDayShort:a[h]})).slice(0,7)}__focusedDateChanged(e,i){Array.isArray(i)&&i.some(r=>S(r,e))?this.removeAttribute("aria-hidden"):this.setAttribute("aria-hidden","true")}_getDate(e){return e?e.getDate():""}__computeShowWeekSeparator(e,i){return e&&i?.firstDayOfWeek===1}_isToday(e){return S(new Date,e)}__computeDays(e,i){if(e===void 0||i===void 0)return[];let r=new Date(0,0);for(r.setFullYear(e.getFullYear()),r.setMonth(e.getMonth()),r.setDate(1);r.getDay()!==i.firstDayOfWeek;)this._dateAdd(r,-1);let n=[],o=r.getMonth(),a=e.getMonth();for(;r.getMonth()===a||r.getMonth()===o;)n.push(r.getMonth()===a?new Date(r.getTime()):null),this._dateAdd(r,1);return n}__computeWeeks(e){return e.reduce((i,r,n)=>(n%7===0&&i.push([]),i[i.length-1].push(r),i),[])}_handleTap(e){!this.ignoreTaps&&!this._notTapping&&e.target.date&&!e.target.hasAttribute("disabled")&&(this.selectedDate=e.target.date,this.dispatchEvent(new CustomEvent("date-tap",{detail:{date:e.target.date},bubbles:!0,composed:!0})))}_preventDefault(e){e.preventDefault()}__computeWeekNumber(e){let i=e.reduce((r,n)=>!r&&n?n:r);return Xs(i)}__computeDayAriaLabel(e){if(!e)return"";let i=`${this._getDate(e)} ${this.i18n.monthNames[e.getMonth()]} ${e.getFullYear()}, ${this.i18n.weekdays[e.getDay()]}`;return this._isToday(e)&&(i+=`, ${this.i18n.today}`),i}_showWeekNumbersChanged(e,i){this.__computeShowWeekSeparator(e,i)?this.setAttribute("week-numbers",""):this.removeAttribute("week-numbers")}__computeDatePart(e,i,r,n,o,a,l,d){let h=["date"];return this.__isDayDisabled(e,n,o,a)&&h.push("disabled"),S(e,i)&&(d||S(e,l))&&h.push("focused"),this.__isDaySelected(e,r)&&h.push("selected"),this._isToday(e)&&h.push("today"),e<ut(new Date)&&h.push("past"),e>ut(new Date)&&h.push("future"),h.join(" ")}__isDaySelected(e,i){return S(e,i)}__computeDayAriaSelected(e,i){return String(this.__isDaySelected(e,i))}__isDayDisabled(e,i,r,n){return!R(e,i,r,n)}__computeDayAriaDisabled(e,i,r,n){return e===void 0||i===void 0&&r===void 0&&n===void 0?"false":String(this.__isDayDisabled(e,i,r,n))}__computeDayTabIndex(e,i){return S(e,i)?"0":"-1"}};var Ti=class extends ar(y(g(b(_)))){static get is(){return"vaadin-month-calendar"}static get styles(){return or}render(){let t=this.__computeWeekDayNames(this.i18n,this.showWeekNumbers),e=this._weeks,i=!this.__computeShowWeekSeparator(this.showWeekNumbers,this.i18n);return v`
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
            <th part="weekday" aria-hidden="true" ?hidden="${i}"></th>
            ${t.map(r=>v`
                <th role="columnheader" part="weekday" scope="col" abbr="${r.weekDay}" aria-hidden="true">
                  ${r.weekDayShort}
                </th>
              `)}
          </tr>
        </thead>
        <tbody id="days-container">
          ${e.map(r=>v`
              <tr role="row">
                <td part="week-number" aria-hidden="true" ?hidden="${i}">
                  ${this.__computeWeekNumber(r)}
                </td>
                ${r.map(n=>v`
                    <td
                      role="gridcell"
                      part="${this.__computeDatePart(n,this.focusedDate,this.selectedDate,this.minDate,this.maxDate,this.isDateDisabled,this.enteredDate,this.__hasFocus)}"
                      .date="${n}"
                      ?disabled="${this.__isDayDisabled(n,this.minDate,this.maxDate,this.isDateDisabled)}"
                      tabindex="${this.__computeDayTabIndex(n,this.focusedDate)}"
                      aria-selected="${this.__computeDayAriaSelected(n,this.selectedDate)}"
                      aria-disabled="${this.__computeDayAriaDisabled(n,this.minDate,this.maxDate,this.isDateDisabled)}"
                      aria-label="${this.__computeDayAriaLabel(n)}"
                      >${this._getDate(n)}</td
                    >
                  `)}
              </tr>
            `)}
        </tbody>
      </table>
    `}};m(Ti);var lr=u`
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
`;var ue=class{constructor(t,e){this.query=t,this.callback=e,this._boundQueryHandler=this._queryHandler.bind(this)}hostConnected(){this._removeListener(),this._mediaQuery=window.matchMedia(this.query),this._addListener(),this._queryHandler(this._mediaQuery)}hostDisconnected(){this._removeListener()}_addListener(){this._mediaQuery&&this._mediaQuery.addListener(this._boundQueryHandler)}_removeListener(){this._mediaQuery&&this._mediaQuery.removeListener(this._boundQueryHandler),this._mediaQuery=null}_queryHandler(t){typeof this.callback=="function"&&this.callback(t.matches)}};var dr=s=>class extends s{static get properties(){return{scrollDuration:{type:Number,value:300},selectedDate:{type:Object,value:null,sync:!0},focusedDate:{type:Object,notify:!0,observer:"_focusedDateChanged",sync:!0},_focusedMonthDate:Number,initialPosition:{type:Object,observer:"_initialPositionChanged",sync:!0},_originDate:{type:Object,value:new Date},_visibleMonthIndex:Number,_desktopMode:{type:Boolean,observer:"_desktopModeChanged"},_desktopMediaQuery:{type:String,value:"(min-width: 375px)"},i18n:{type:Object},showWeekNumbers:{type:Boolean,value:!1},_ignoreTaps:Boolean,_notTapping:Boolean,minDate:{type:Object,sync:!0},maxDate:{type:Object,sync:!0},isDateDisabled:{type:Function},enteredDate:{type:Date,sync:!0},label:String,_cancelButton:{type:Object},_todayButton:{type:Object},calendars:{type:Array,value:()=>[]},years:{type:Array,value:()=>[]}}}static get observers(){return["__updateCalendars(calendars, i18n, minDate, maxDate, selectedDate, focusedDate, showWeekNumbers, _ignoreTaps, _theme, isDateDisabled, enteredDate)","__updateCancelButton(_cancelButton, i18n)","__updateTodayButton(_todayButton, i18n, minDate, maxDate, isDateDisabled)","__updateYears(years, selectedDate, _theme)"]}get __useSubMonthScrolling(){return this._monthScroller.clientHeight<this._monthScroller.itemHeight+this._monthScroller.bufferOffset}get focusableDateElement(){return this.calendars.map(e=>e.focusableDateElement).find(Boolean)}_initControllers(){this.addController(new ue(this._desktopMediaQuery,e=>{this._desktopMode=e})),this.addController(new D(this,"today-button","vaadin-button",{observe:!1,initializer:e=>{e.setAttribute("theme","tertiary"),e.addEventListener("keydown",i=>this.__onTodayButtonKeyDown(i)),e.addEventListener("click",this._onTodayTap.bind(this)),this._todayButton=e}})),this.addController(new D(this,"cancel-button","vaadin-button",{observe:!1,initializer:e=>{e.setAttribute("theme","tertiary"),e.addEventListener("keydown",i=>this.__onCancelButtonKeyDown(i)),e.addEventListener("click",this._cancel.bind(this)),this._cancelButton=e}})),this.__initMonthScroller(),this.__initYearScroller()}reset(){this._closeYearScroller()}focusCancel(){this._cancelButton.focus()}scrollToDate(e,i){let r=this.__useSubMonthScrolling?this._calculateWeekScrollOffset(e):0;this._scrollToPosition(this._differenceInMonths(e,this._originDate)+r,i),this._monthScroller.forceUpdate()}__initMonthScroller(){this.addController(new D(this,"months","vaadin-date-picker-month-scroller",{observe:!1,initializer:e=>{e.addEventListener("custom-scroll",()=>{this._onMonthScroll()}),e.addEventListener("touchstart",()=>{this._onMonthScrollTouchStart()}),e.addEventListener("keydown",i=>{this.__onMonthCalendarKeyDown(i)}),e.addEventListener("init-done",()=>{let i=[...this.querySelectorAll("vaadin-month-calendar")];i.forEach(r=>{r.addEventListener("selected-date-changed",n=>{this.selectedDate=n.detail.value})}),this.calendars=i}),this._monthScroller=e}}))}__initYearScroller(){this.addController(new D(this,"years","vaadin-date-picker-year-scroller",{observe:!1,initializer:e=>{e.setAttribute("aria-hidden","true"),ie(e,"tap",i=>{this._onYearTap(i)}),e.addEventListener("custom-scroll",()=>{this._onYearScroll()}),e.addEventListener("touchstart",()=>{this._onYearScrollTouchStart()}),e.addEventListener("init-done",()=>{this.years=[...this.querySelectorAll("vaadin-date-picker-year")]}),this._yearScroller=e}}))}__updateCancelButton(e,i){e&&(e.textContent=i?.cancel)}__updateTodayButton(e,i,r,n,o){e&&(e.textContent=i?.today,e.disabled=!this._isTodayAllowed(r,n,o))}__updateCalendars(e,i,r,n,o,a,l,d,h,c,p){e?.length&&e.forEach(f=>{f.i18n=i,f.minDate=r,f.maxDate=n,f.isDateDisabled=c,f.focusedDate=a,f.selectedDate=o,f.showWeekNumbers=l,f.ignoreTaps=d,f.enteredDate=p,h?f.setAttribute("theme",h):f.removeAttribute("theme")})}__updateYears(e,i,r){e?.length&&e.forEach(n=>{n.selectedDate=i,r?n.setAttribute("theme",r):n.removeAttribute("theme")})}_selectDate(e){return this._dateAllowed(e)?(this.selectedDate=e,this.dispatchEvent(new CustomEvent("date-selected",{detail:{date:e},bubbles:!0,composed:!0})),!0):!1}_desktopModeChanged(e){this.toggleAttribute("desktop",e)}_focusedDateChanged(e){this.revealDate(e)}revealDate(e,i=!0){if(!e)return;let r=this._differenceInMonths(e,this._originDate);if(this.__useSubMonthScrolling){let d=this._calculateWeekScrollOffset(e);this._scrollToPosition(r+d,i);return}let n=this._monthScroller.position>r,a=Math.max(this._monthScroller.itemHeight,this._monthScroller.clientHeight-this._monthScroller.bufferOffset*2)/this._monthScroller.itemHeight,l=this._monthScroller.position+a-1<r;n?this._scrollToPosition(r,i):l&&this._scrollToPosition(r-a+1,i)}_calculateWeekScrollOffset(e){let i=new Date(0,0);i.setFullYear(e.getFullYear()),i.setMonth(e.getMonth()),i.setDate(1);let r=0;for(;i.getDate()<e.getDate();)i.setDate(i.getDate()+1),i.getDay()===this.i18n.firstDayOfWeek&&(r+=1);return r/6}_initialPositionChanged(e){this._monthScroller&&this._yearScroller&&(this._monthScroller.active=!0,this._yearScroller.active=!0),this.scrollToDate(e)}_repositionYearScroller(){let e=this._monthScroller.position;this._visibleMonthIndex=Math.floor(e),this._yearScroller.position=(e+this._originDate.getMonth())/12}_repositionMonthScroller(){this._monthScroller.position=this._yearScroller.position*12-this._originDate.getMonth(),this._visibleMonthIndex=Math.floor(this._monthScroller.position)}_onMonthScroll(){this._repositionYearScroller(),this._doIgnoreTaps()}_onYearScroll(){this._repositionMonthScroller(),this._doIgnoreTaps()}_onYearScrollTouchStart(){this._notTapping=!1,setTimeout(()=>{this._notTapping=!0},300),this._repositionMonthScroller()}_onMonthScrollTouchStart(){this._repositionYearScroller()}_doIgnoreTaps(){this._ignoreTaps=!0,this._debouncer=w.debounce(this._debouncer,A.after(300),()=>{this._ignoreTaps=!1})}_onTodayTap(){let e=this._getTodayMidnight();Math.abs(this._monthScroller.position-this._differenceInMonths(e,this._originDate))<.001?(this._selectDate(e),this._close()):this._scrollToCurrentMonth()}_scrollToCurrentMonth(){this.focusedDate&&(this.focusedDate=new Date),this.scrollToDate(new Date,!0)}_onYearTap(e){if(!this._ignoreTaps&&!this._notTapping){let r=(e.detail.y-(this._yearScroller.getBoundingClientRect().top+this._yearScroller.clientHeight/2))/this._yearScroller.itemHeight;this._scrollToPosition(this._monthScroller.position+r*12,!0)}}_scrollToPosition(e,i){if(this._targetPosition!==void 0){this._targetPosition=e;return}if(!i){this._monthScroller.position=e,this._monthScroller.forceUpdate(),this._targetPosition=void 0,this._repositionYearScroller(),this.__tryFocusDate();return}this._targetPosition=e;let r;this._revealPromise=new Promise(d=>{r=d});let n=(d,h,c,p)=>(d/=p/2,d<1?c/2*d*d+h:(d-=1,-c/2*(d*(d-2)-1)+h)),o=0,a=this._monthScroller.position,l=d=>{o||(o=d);let h=d-o;if(h<this.scrollDuration){let c=n(h,a,this._targetPosition-a,this.scrollDuration);this._monthScroller.position=c,window.requestAnimationFrame(l)}else this.dispatchEvent(new CustomEvent("scroll-animation-finished",{bubbles:!0,composed:!0,detail:{position:this._targetPosition,oldPosition:a}})),this._monthScroller.position=this._targetPosition,this._monthScroller.forceUpdate(),this._targetPosition=void 0,r(),this._revealPromise=void 0;setTimeout(this._repositionYearScroller.bind(this),1)};window.requestAnimationFrame(l)}_toggleYearScroller(){this.toggleAttribute("years-visible")}_closeYearScroller(){this.removeAttribute("years-visible")}_yearAfterXMonths(e){return _t(e).getFullYear()}_differenceInMonths(e,i){return(e.getFullYear()-i.getFullYear())*12-i.getMonth()+e.getMonth()}_clear(){this._selectDate("")}_close(){this.dispatchEvent(new CustomEvent("close",{bubbles:!0,composed:!0}))}_cancel(){this.focusedDate=this.selectedDate,this._close()}__toggleDate(e){S(e,this.selectedDate)?(this._clear(),this.focusedDate=e):this._selectDate(e)}__onMonthCalendarKeyDown(e){let i=!1;switch(e.key){case"ArrowDown":this._moveFocusByDays(7),i=!0;break;case"ArrowUp":this._moveFocusByDays(-7),i=!0;break;case"ArrowRight":this._moveFocusByDays(this.__isRTL?-1:1),i=!0;break;case"ArrowLeft":this._moveFocusByDays(this.__isRTL?1:-1),i=!0;break;case"Enter":this._selectDate(this.focusedDate)&&(this._close(),i=!0);break;case" ":this.__toggleDate(this.focusedDate),i=!0;break;case"Home":this._moveFocusInsideMonth(this.focusedDate,"minDate"),i=!0;break;case"End":this._moveFocusInsideMonth(this.focusedDate,"maxDate"),i=!0;break;case"PageDown":this._moveFocusByMonths(e.shiftKey?12:1),i=!0;break;case"PageUp":this._moveFocusByMonths(e.shiftKey?-12:-1),i=!0;break;case"Tab":this._onTabKeyDown(e,"calendar");break;default:break}i&&(e.preventDefault(),e.stopPropagation())}_onTabKeyDown(e,i){switch(e.stopPropagation(),i){case"calendar":e.shiftKey&&(e.preventDefault(),this.hasAttribute("fullscreen")?this.focusCancel():this.__focusInput());break;case"today":e.shiftKey&&(e.preventDefault(),this.focusDateElement());break;case"cancel":e.shiftKey||(e.preventDefault(),this.hasAttribute("fullscreen")?this.focusDateElement():this.__focusInput());break;default:break}}__onTodayButtonKeyDown(e){e.key==="Tab"&&this._onTabKeyDown(e,"today")}__onCancelButtonKeyDown(e){e.key==="Tab"&&this._onTabKeyDown(e,"cancel")}__focusInput(){this.dispatchEvent(new CustomEvent("focus-input",{bubbles:!0,composed:!0}))}__tryFocusDate(){if(this.__pendingDateFocus){let i=this.focusableDateElement;i&&S(i.date,this.__pendingDateFocus)&&(delete this.__pendingDateFocus,i.focus())}}async focusDate(e,i){let r=e||this.selectedDate||this.initialPosition||new Date;this.focusedDate=r,i||(this._focusedMonthDate=r.getDate()),await this.focusDateElement(!1)}async focusDateElement(e=!0){this.__pendingDateFocus=this.focusedDate,this.calendars.length||await new Promise(i=>{requestAnimationFrame(()=>{setTimeout(()=>{i()})})}),e&&this.revealDate(this.focusedDate),this._revealPromise&&await this._revealPromise,this.__tryFocusDate()}_focusClosestDate(e){this.focusDate(pt(e,[this.minDate,this.maxDate]))}_focusAllowedDate(e,i,r){this._dateAllowed(e,void 0,void 0,()=>!1)?this.focusDate(e,r):this._dateAllowed(this.focusedDate)?i>0?this.focusDate(this.maxDate):this.focusDate(this.minDate):this._focusClosestDate(this.focusedDate)}_getDateDiff(e,i){let r=new Date(0,0);return r.setFullYear(this.focusedDate.getFullYear()),r.setMonth(this.focusedDate.getMonth()+e),i&&r.setDate(this.focusedDate.getDate()+i),r}_moveFocusByDays(e){let i=this._getDateDiff(0,e);this._focusAllowedDate(i,e,!1)}_moveFocusByMonths(e){let i=this._getDateDiff(e),r=i.getMonth();this._focusedMonthDate||(this._focusedMonthDate=this.focusedDate.getDate()),i.setDate(this._focusedMonthDate),i.getMonth()!==r&&i.setDate(0),this._focusAllowedDate(i,e,!0)}_moveFocusInsideMonth(e,i){let r=new Date(0,0);r.setFullYear(e.getFullYear()),i==="minDate"?(r.setMonth(e.getMonth()),r.setDate(1)):(r.setMonth(e.getMonth()+1),r.setDate(0)),this._dateAllowed(r)?this.focusDate(r):this._dateAllowed(e)?this.focusDate(this[i]):this._focusClosestDate(e)}_dateAllowed(e,i=this.minDate,r=this.maxDate,n=this.isDateDisabled){return R(e,i,r,n)}_isTodayAllowed(e,i,r){return this._dateAllowed(this._getTodayMidnight(),e,i,r)}_getTodayMidnight(){let e=new Date,i=new Date(0,0);return i.setFullYear(e.getFullYear()),i.setMonth(e.getMonth()),i.setDate(e.getDate()),i}};var Ai=class extends dr(y(I(g(b(_))))){static get is(){return"vaadin-date-picker-overlay-content"}static get styles(){return lr}static get lumoInjector(){return{...super.lumoInjector,includeBaseStyles:!0}}render(){return v`
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
    `}firstUpdated(){super.firstUpdated(),this.setAttribute("role","dialog"),this._initControllers()}};m(Ai);var pe=s=>s??C;var to=s=>class extends $(ct(s)){static get properties(){return{autofocus:{type:Boolean},focusElement:{type:Object,readOnly:!0,observer:"_focusElementChanged",sync:!0},_lastTabIndex:{value:0}}}constructor(){super(),this._boundOnBlur=this._onBlur.bind(this),this._boundOnFocus=this._onFocus.bind(this)}ready(){super.ready(),this.autofocus&&!this.disabled&&requestAnimationFrame(()=>{this.focus()})}focus(e){this.focusElement&&!this.disabled&&(this.focusElement.focus(),e?.focusVisible!==!1&&this.setAttribute("focus-ring",""))}blur(){this.focusElement&&this.focusElement.blur()}click(){this.focusElement&&!this.disabled&&this.focusElement.click()}_focusElementChanged(e,i){e?(e.disabled=this.disabled,this._addFocusListeners(e),this.__forwardTabIndex(this.tabindex)):i&&this._removeFocusListeners(i)}_addFocusListeners(e){e.addEventListener("blur",this._boundOnBlur),e.addEventListener("focus",this._boundOnFocus)}_removeFocusListeners(e){e.removeEventListener("blur",this._boundOnBlur),e.removeEventListener("focus",this._boundOnFocus)}_onFocus(e){e.stopPropagation(),this.dispatchEvent(new Event("focus"))}_onBlur(e){e.stopPropagation(),this.dispatchEvent(new Event("blur"))}_shouldSetFocus(e){return e.target===this.focusElement}_shouldRemoveFocus(e){return e.target===this.focusElement}_disabledChanged(e,i){super._disabledChanged(e,i),this.focusElement&&(this.focusElement.disabled=e),e&&this.blur()}_tabindexChanged(e){this.__forwardTabIndex(e)}__forwardTabIndex(e){e!==void 0&&this.focusElement&&(this.focusElement.tabIndex=e,e!==-1&&(this.tabindex=void 0)),this.disabled&&e&&(e!==-1&&(this._lastTabIndex=e),this.tabindex=void 0),e===void 0&&this.hasAttribute("tabindex")&&this.removeAttribute("tabindex")}},ft=x(to);var Mi=new WeakMap;function io(s){return Mi.has(s)||Mi.set(s,new Set),Mi.get(s)}function so(s,t){let e=document.createElement("style");e.textContent=s,t===document?document.head.appendChild(e):t.insertBefore(e,t.firstChild)}var ro=s=>class extends s{get slotStyles(){return[]}connectedCallback(){super.connectedCallback(),this.__applySlotStyles()}__applySlotStyles(){let e=this.getRootNode(),i=io(e);this.slotStyles.forEach(r=>{i.has(r)||(so(r,e),i.add(r))})}},hr=x(ro);var no=s=>class extends s{static get properties(){return{inputElement:{type:Object,readOnly:!0,observer:"_inputElementChanged",sync:!0},type:{type:String,readOnly:!0},value:{type:String,value:"",observer:"_valueChanged",notify:!0,sync:!0}}}constructor(){super(),this._boundOnInput=this._onInput.bind(this),this._boundOnChange=this._onChange.bind(this)}get _hasValue(){return this.value!=null&&this.value!==""}get _inputElementValueProperty(){return"value"}get _inputElementValue(){return this.inputElement?this.inputElement[this._inputElementValueProperty]:void 0}set _inputElementValue(e){this.inputElement&&(this.inputElement[this._inputElementValueProperty]=e)}clear(){this.value="",this._inputElementValue=""}_addInputListeners(e){e.addEventListener("input",this._boundOnInput),e.addEventListener("change",this._boundOnChange)}_removeInputListeners(e){e.removeEventListener("input",this._boundOnInput),e.removeEventListener("change",this._boundOnChange)}_forwardInputValue(e){this.inputElement&&(this._inputElementValue=e??"")}_inputElementChanged(e,i){e?this._addInputListeners(e):i&&this._removeInputListeners(i)}_onInput(e){let i=e.composedPath()[0];this.__userInput=e.isTrusted,this.value=i.value,this.__userInput=!1}_onChange(e){}_toggleHasValue(e){this.toggleAttribute("has-value",e)}_valueChanged(e,i){this._toggleHasValue(this._hasValue),!(e===""&&i===void 0)&&(this.__userInput||this._forwardInputValue(e))}},_e=x(no);var cr=s=>class extends _e(V(s)){static get properties(){return{clearButtonVisible:{type:Boolean,reflectToAttribute:!0,value:!1}}}get clearElement(){return console.warn(`Please implement the 'clearElement' property in <${this.localName}>`),null}ready(){super.ready(),this.clearElement&&(this.clearElement.addEventListener("mousedown",e=>this._onClearButtonMouseDown(e)),this.clearElement.addEventListener("click",e=>this._onClearButtonClick(e)))}_onClearButtonClick(e){e.preventDefault(),this._onClearAction()}_onClearButtonMouseDown(e){this._shouldKeepFocusOnClearMousedown()&&e.preventDefault(),Me||this.inputElement.focus()}_onEscape(e){super._onEscape(e),this.clearButtonVisible&&this.value&&!this.readonly&&(e.stopPropagation(),this._onClearAction())}_onClearAction(){this._inputElementValue="",this.inputElement.dispatchEvent(new Event("input",{bubbles:!0,composed:!0})),this.inputElement.dispatchEvent(new Event("change",{bubbles:!0}))}_shouldKeepFocusOnClearMousedown(){return oe(this.inputElement)}};var Pi=new Map;function Oi(s){return Pi.has(s)||Pi.set(s,new WeakMap),Pi.get(s)}function ur(s,t){s&&s.removeAttribute(t)}function pr(s,t){if(!s||!t)return;let e=Oi(t);if(e.has(s))return;let i=nt(s.getAttribute(t));e.set(s,new Set(i))}function _r(s,t){if(!s||!t)return;let e=Oi(t),i=e.get(s);!i||i.size===0?s.removeAttribute(t):di(s,t,Pe(i)),e.delete(s)}function mt(s,t,e={newId:null,oldId:null,fromUser:!1}){if(!s||!t)return;let{newId:i,oldId:r,fromUser:n}=e,o=Oi(t),a=o.get(s);if(!n&&a){r&&a.delete(r),i&&a.add(i);return}n&&(a?i||o.delete(s):pr(s,t),ur(s,t)),Ps(s,t,r);let l=i||Pe(a);l&&di(s,t,l)}function fr(s,t){pr(s,t),ur(s,t)}var vt=class{constructor(t){this.host=t,this.__required=!1}setTarget(t){this.__target=t,this.__setAriaRequiredAttribute(this.__required),this.__setLabelIdToAriaAttribute(this.__labelId,this.__labelId),this.__labelIdFromUser!=null&&this.__setLabelIdToAriaAttribute(this.__labelIdFromUser,this.__labelIdFromUser,!0),this.__setErrorIdToAriaAttribute(this.__errorId),this.__setHelperIdToAriaAttribute(this.__helperId),this.setAriaLabel(this.__label)}setRequired(t){this.__setAriaRequiredAttribute(t),this.__required=t}setAriaLabel(t){this.__setAriaLabelToAttribute(t),this.__label=t}setLabelId(t,e=!1){let i=e?this.__labelIdFromUser:this.__labelId;this.__setLabelIdToAriaAttribute(t,i,e),e?this.__labelIdFromUser=t:this.__labelId=t}setErrorId(t){this.__setErrorIdToAriaAttribute(t,this.__errorId),this.__errorId=t}setHelperId(t){this.__setHelperIdToAriaAttribute(t,this.__helperId),this.__helperId=t}__setAriaLabelToAttribute(t){this.__target&&(t?(fr(this.__target,"aria-labelledby"),this.__target.setAttribute("aria-label",t)):this.__label&&(_r(this.__target,"aria-labelledby"),this.__target.removeAttribute("aria-label")))}__setLabelIdToAriaAttribute(t,e,i){mt(this.__target,"aria-labelledby",{newId:t,oldId:e,fromUser:i})}__setErrorIdToAriaAttribute(t,e){mt(this.__target,"aria-describedby",{newId:t,oldId:e,fromUser:!1})}__setHelperIdToAriaAttribute(t,e){mt(this.__target,"aria-describedby",{newId:t,oldId:e,fromUser:!1})}__setAriaRequiredAttribute(t){this.__target&&(["input","textarea"].includes(this.__target.localName)||(t?this.__target.setAttribute("aria-required","true"):this.__target.removeAttribute("aria-required")))}};var M=document.createElement("div");M.style.position="fixed";M.style.clip="rect(0px, 0px, 0px, 0px)";M.setAttribute("aria-live","polite");document.body.appendChild(M);var gt;function mr(s,t={}){let e=t.mode||"polite",i=t.timeout??150;e==="alert"?(M.removeAttribute("aria-live"),M.removeAttribute("role"),gt=w.debounce(gt,Z,()=>{M.setAttribute("role","alert")})):(gt&&gt.cancel(),M.removeAttribute("role"),M.setAttribute("aria-live",e)),M.textContent="",setTimeout(()=>{M.textContent=s},i)}var W=class extends D{constructor(t,e,i,r={}){super(t,e,i,{...r,useUniqueId:!0})}initCustomNode(t){this.__updateNodeId(t),this.__notifyChange(t)}teardownNode(t){let e=this.getSlotChild();e&&e!==this.defaultNode?this.__notifyChange(e):(this.restoreDefaultNode(),this.updateDefaultNode(this.node))}attachDefaultNode(){let t=super.attachDefaultNode();return t&&this.__updateNodeId(t),t}restoreDefaultNode(){}updateDefaultNode(t){this.__notifyChange(t)}observeNode(t){this.__nodeObserver&&this.__nodeObserver.disconnect(),this.__nodeObserver=new MutationObserver(e=>{e.forEach(i=>{let r=i.target,n=r===this.node;i.type==="attributes"?n&&this.__updateNodeId(r):(n||r.parentElement===this.node)&&this.__notifyChange(this.node)})}),this.__nodeObserver.observe(t,{attributes:!0,attributeFilter:["id"],childList:!0,subtree:!0,characterData:!0})}__hasContent(t){return t?t.nodeType===Node.ELEMENT_NODE&&(customElements.get(t.localName)||t.children.length>0)||t.textContent&&t.textContent.trim()!=="":!1}__notifyChange(t){this.dispatchEvent(new CustomEvent("slot-content-changed",{detail:{hasContent:this.__hasContent(t),node:t}}))}__updateNodeId(t){let e=!this.nodes||t===this.nodes[0];t.nodeType===Node.ELEMENT_NODE&&(!this.multiple||e)&&!t.id&&(t.id=this.defaultId)}};var bt=class extends W{constructor(t){super(t,"error-message","div")}setErrorMessage(t){this.errorMessage=t,this.updateDefaultNode(this.node)}setInvalid(t){this.invalid=t,this.updateDefaultNode(this.node)}initAddedNode(t){t!==this.defaultNode&&this.initCustomNode(t)}initNode(t){this.updateDefaultNode(t)}initCustomNode(t){t.textContent&&!this.errorMessage&&(this.errorMessage=t.textContent.trim()),super.initCustomNode(t)}restoreDefaultNode(){this.attachDefaultNode()}updateDefaultNode(t){let{errorMessage:e,invalid:i}=this,r=!!(i&&e&&e.trim()!=="");t&&(t.textContent=r?e:"",t.hidden=!r,r&&mr(e,{mode:"assertive"})),super.updateDefaultNode(t)}};var yt=class extends W{constructor(t){super(t,"helper",null)}setHelperText(t){this.helperText=t,this.getSlotChild()||this.restoreDefaultNode(),this.node===this.defaultNode&&this.updateDefaultNode(this.node)}restoreDefaultNode(){let{helperText:t}=this;if(t&&t.trim()!==""){this.tagName="div";let e=this.attachDefaultNode();this.observeNode(e)}}updateDefaultNode(t){t&&(t.textContent=this.helperText),super.updateDefaultNode(t)}initCustomNode(t){super.initCustomNode(t),this.observeNode(t)}};var xt=class extends W{constructor(t){super(t,"label","label")}setLabel(t){this.label=t,this.getSlotChild()||this.restoreDefaultNode(),this.node===this.defaultNode&&this.updateDefaultNode(this.node)}restoreDefaultNode(){let{label:t}=this;if(t&&t.trim()!==""){let e=this.attachDefaultNode();this.observeNode(e)}}updateDefaultNode(t){t&&(t.textContent=this.label),super.updateDefaultNode(t)}initCustomNode(t){super.initCustomNode(t),this.observeNode(t)}};var vr=s=>class extends s{static get properties(){return{label:{type:String,observer:"_labelChanged"}}}constructor(){super(),this._labelController=new xt(this),this._labelController.addEventListener("slot-content-changed",e=>{this.toggleAttribute("has-label",e.detail.hasContent)})}get _labelId(){return this._labelNode?.id}get _labelNode(){return this._labelController.node}ready(){super.ready(),this.addController(this._labelController)}_labelChanged(e){this._labelController.setLabel(e)}};var oo=s=>class extends s{static get properties(){return{invalid:{type:Boolean,reflectToAttribute:!0,notify:!0,value:!1,sync:!0},manualValidation:{type:Boolean,value:!1},required:{type:Boolean,reflectToAttribute:!0,sync:!0}}}validate(){let t=this.checkValidity();return this._setInvalid(!t),this.dispatchEvent(new CustomEvent("validated",{detail:{valid:t}})),t}checkValidity(){return!this.required||!!this.value}_setInvalid(t){this._shouldSetInvalid(t)&&(this.invalid=t)}_shouldSetInvalid(t){return!0}_requestValidation(){this.manualValidation||this.validate()}},Ct=x(oo);var wt=s=>class extends Ct(vr(s)){static get properties(){return{ariaTarget:{type:Object,observer:"_ariaTargetChanged"},errorMessage:{type:String,observer:"_errorMessageChanged"},helperText:{type:String,observer:"_helperTextChanged"},accessibleName:{type:String,observer:"_accessibleNameChanged"},accessibleNameRef:{type:String,observer:"_accessibleNameRefChanged"}}}static get observers(){return["_invalidChanged(invalid)","_requiredChanged(required)"]}constructor(){super(),this._fieldAriaController=new vt(this),this._helperController=new yt(this),this._errorController=new bt(this),this._errorController.addEventListener("slot-content-changed",e=>{this.toggleAttribute("has-error-message",e.detail.hasContent)}),this._labelController.addEventListener("slot-content-changed",e=>{let{hasContent:i,node:r}=e.detail;this.__labelChanged(i,r)}),this._helperController.addEventListener("slot-content-changed",e=>{let{hasContent:i,node:r}=e.detail;this.toggleAttribute("has-helper",i),this.__helperChanged(i,r)})}get _errorNode(){return this._errorController.node}get _helperNode(){return this._helperController.node}ready(){super.ready(),this.addController(this._fieldAriaController),this.addController(this._helperController),this.addController(this._errorController)}__helperChanged(e,i){e?this._fieldAriaController.setHelperId(i.id):this._fieldAriaController.setHelperId(null)}_accessibleNameChanged(e){this._fieldAriaController.setAriaLabel(e)}_accessibleNameRefChanged(e){this._fieldAriaController.setLabelId(e,!0)}__labelChanged(e,i){e?this._fieldAriaController.setLabelId(i.id):this._fieldAriaController.setLabelId(null)}_errorMessageChanged(e){this._errorController.setErrorMessage(e)}_helperTextChanged(e){this._helperController.setHelperText(e)}_ariaTargetChanged(e){e&&this._fieldAriaController.setTarget(e)}_requiredChanged(e){this._fieldAriaController.setRequired(e)}_invalidChanged(e){this._errorController.setInvalid(e),setTimeout(()=>{if(e){let i=this._errorNode;this._fieldAriaController.setErrorId(i?.id)}else this._fieldAriaController.setErrorId(null)})}};var ao=s=>class extends s{static get properties(){return{stateTarget:{type:Object,observer:"_stateTargetChanged"}}}static get delegateAttrs(){return[]}static get delegateProps(){return[]}ready(){super.ready(),this._createDelegateAttrsObserver(),this._createDelegatePropsObserver()}_stateTargetChanged(e){e&&(this._ensureAttrsDelegated(),this._ensurePropsDelegated())}_createDelegateAttrsObserver(){this._createMethodObserver(`_delegateAttrsChanged(${this.constructor.delegateAttrs.join(", ")})`)}_createDelegatePropsObserver(){this._createMethodObserver(`_delegatePropsChanged(${this.constructor.delegateProps.join(", ")})`)}_ensureAttrsDelegated(){this.constructor.delegateAttrs.forEach(e=>{this._delegateAttribute(e,this[e])})}_ensurePropsDelegated(){this.constructor.delegateProps.forEach(e=>{this._delegateProperty(e,this[e])})}_delegateAttrsChanged(...e){this.constructor.delegateAttrs.forEach((i,r)=>{this._delegateAttribute(i,e[r])})}_delegatePropsChanged(...e){this.constructor.delegateProps.forEach((i,r)=>{this._delegateProperty(i,e[r])})}_delegateAttribute(e,i){this.stateTarget&&(e==="invalid"&&this._delegateAttribute("aria-invalid",i?"true":!1),typeof i=="boolean"?this.stateTarget.toggleAttribute(e,i):i?this.stateTarget.setAttribute(e,i):this.stateTarget.removeAttribute(e))}_delegateProperty(e,i){this.stateTarget&&(this.stateTarget[e]=i)}},gr=x(ao);var lo=s=>class extends gr(Ct(_e(s))){static get constraints(){return["required"]}static get delegateAttrs(){return[...super.delegateAttrs,"required"]}ready(){super.ready(),this._createConstraintsObserver()}checkValidity(){return this.inputElement&&this._hasValidConstraints(this.constructor.constraints.map(e=>this[e]))?this.inputElement.checkValidity():!this.invalid}_hasValidConstraints(e){return e.some(i=>this.__isValidConstraint(i))}_createConstraintsObserver(){this._createMethodObserver(`_constraintsChanged(stateTarget, ${this.constructor.constraints.join(", ")})`)}_constraintsChanged(e,...i){if(!e)return;let r=this._hasValidConstraints(i),n=this.__previousHasConstraints&&!r;(this._hasValue||this.invalid)&&r?this._requestValidation():n&&!this.manualValidation&&this._setInvalid(!1),this.__previousHasConstraints=r}_onChange(e){e.stopPropagation(),this._requestValidation(),this.dispatchEvent(new CustomEvent("change",{detail:{sourceEvent:e},bubbles:e.bubbles,cancelable:e.cancelable}))}__isValidConstraint(e){return!!e||e===0}},fe=x(lo);var St=s=>class extends hr(ft(fe(wt(cr(V(s)))))){static get properties(){return{allowedCharPattern:{type:String,observer:"_allowedCharPatternChanged"},autoselect:{type:Boolean,value:!1},name:{type:String,reflectToAttribute:!0},placeholder:{type:String,reflectToAttribute:!0},readonly:{type:Boolean,value:!1,reflectToAttribute:!0},title:{type:String,reflectToAttribute:!0}}}static get delegateAttrs(){return[...super.delegateAttrs,"name","type","placeholder","readonly","invalid","title"]}constructor(){super(),this._boundOnPaste=this._onPaste.bind(this),this._boundOnDrop=this._onDrop.bind(this),this._boundOnBeforeInput=this._onBeforeInput.bind(this)}get slotStyles(){let e=this.localName;return[`
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
        `]}_onFocus(e){super._onFocus(e),this.autoselect&&this.inputElement&&this.inputElement.select()}_addInputListeners(e){super._addInputListeners(e),e.addEventListener("paste",this._boundOnPaste),e.addEventListener("drop",this._boundOnDrop),e.addEventListener("beforeinput",this._boundOnBeforeInput)}_removeInputListeners(e){super._removeInputListeners(e),e.removeEventListener("paste",this._boundOnPaste),e.removeEventListener("drop",this._boundOnDrop),e.removeEventListener("beforeinput",this._boundOnBeforeInput)}_onKeyDown(e){super._onKeyDown(e),this.allowedCharPattern&&!this.__shouldAcceptKey(e)&&e.target===this.inputElement&&(e.preventDefault(),this._markInputPrevented())}_markInputPrevented(){this.setAttribute("input-prevented",""),this._preventInputDebouncer=w.debounce(this._preventInputDebouncer,A.after(200),()=>{this.removeAttribute("input-prevented")})}__shouldAcceptKey(e){return e.metaKey||e.ctrlKey||!e.key||e.key.length!==1||this.__allowedCharRegExp.test(e.key)}_onPaste(e){if(this.allowedCharPattern){let i=e.clipboardData.getData("text");this.__allowedTextRegExp.test(i)||(e.preventDefault(),this._markInputPrevented())}}_onDrop(e){if(this.allowedCharPattern){let i=e.dataTransfer.getData("text");this.__allowedTextRegExp.test(i)||(e.preventDefault(),this._markInputPrevented())}}_onBeforeInput(e){this.allowedCharPattern&&e.data&&!this.__allowedTextRegExp.test(e.data)&&(e.preventDefault(),this._markInputPrevented())}_allowedCharPatternChanged(e){if(e)try{this.__allowedCharRegExp=new RegExp(`^${e}$`,"u"),this.__allowedTextRegExp=new RegExp(`^${e}*$`,"u")}catch(i){console.error(i)}}};var me=class extends D{constructor(t,e,i={}){let{uniqueIdPrefix:r}=i;super(t,"input","input",{initializer:(n,o)=>{o.value&&(n.value=o.value),o.type&&n.setAttribute("type",o.type),n.id=this.defaultId,typeof e=="function"&&e(n)},useUniqueId:!0,uniqueIdPrefix:r})}};var ve=class{constructor(t,e){this.input=t,this.__preventDuplicateLabelClick=this.__preventDuplicateLabelClick.bind(this),e.addEventListener("slot-content-changed",i=>{this.__initLabel(i.detail.node)}),this.__initLabel(e.node)}__initLabel(t){t&&(t.addEventListener("click",this.__preventDuplicateLabelClick),this.input&&t.setAttribute("for",this.input.id))}__preventDuplicateLabelClick(){let t=e=>{e.stopImmediatePropagation(),this.input.removeEventListener("click",t)};this.input.addEventListener("click",t)}};var br=u`
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
`;var yr=u`
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
`;var ge=[yr,br];var xr=u`
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
`;var be=new WeakMap,Et=new WeakMap,kt={},Vi=0,Cr=s=>s?.nodeType===Node.ELEMENT_NODE,$i=(...s)=>{console.error(`Error: ${s.join(" ")}. Skip setting aria-hidden.`)},ho=(s,t)=>Cr(s)?t.map(e=>{if(!Cr(e))return $i(e,"is not a valid element"),null;let i=e;for(;i&&i!==s;){if(s.contains(i))return e;i=i.getRootNode().host}return $i(e,"is not contained inside",s),null}).filter(e=>!!e):($i(s,"is not a valid element"),[]),co=(s,t,e,i)=>{let r=ho(t,Array.isArray(s)?s:[s]);kt[e]||(kt[e]=new WeakMap);let n=kt[e],o=[],a=new Set,l=new Set(r),d=c=>{if(!c||a.has(c))return;a.add(c);let p=c.assignedSlot;p&&d(p),d(c.parentNode||c.host)};r.forEach(d);let h=c=>{if(!c||l.has(c))return;let p=c.shadowRoot;(p?[...c.children,...p.children]:[...c.children]).forEach(E=>{if(!["template","script","style"].includes(E.localName))if(a.has(E))h(E);else{let Fe=E.getAttribute(i),Be=Fe!==null&&Fe!=="false",Pt=(be.get(E)||0)+1,He=(n.get(E)||0)+1;be.set(E,Pt),n.set(E,He),o.push(E),Pt===1&&Be&&Et.set(E,!0),He===1&&E.setAttribute(e,"true"),Be||E.setAttribute(i,"true")}})};return h(t),a.clear(),Vi+=1,()=>{o.forEach(c=>{let p=be.get(c)-1,f=n.get(c)-1;be.set(c,p),n.set(c,f),p||(Et.has(c)?Et.delete(c):c.removeAttribute(i)),f||c.removeAttribute(e)}),Vi-=1,Vi||(be=new WeakMap,be=new WeakMap,Et=new WeakMap,kt={})}},wr=(s,t=document.body,e="data-aria-hidden")=>{let i=Array.from(Array.isArray(s)?s:[s]);return t&&i.push(...Array.from(t.querySelectorAll("[aria-live]"))),co(i,t,e,"aria-hidden")};var lu="inert"in HTMLElement.prototype;function Sr(s,...t){let e=n=>Array.isArray(n),i=n=>n&&typeof n=="object"&&!e(n),r=(n,o)=>{i(o)&&i(n)&&Object.keys(o).forEach(a=>{let l=o[a];i(l)?(n[a]||(n[a]={}),r(n[a],l)):e(l)?n[a]=[...l]:l!=null&&(n[a]=l)})};return t.forEach(n=>{r(s,n)}),s}var ye=s=>class extends s{static get properties(){return{i18n:{type:Object},__effectiveI18n:{type:Object,sync:!0}}}static get defaultI18n(){return{}}constructor(){super(),this.i18n=Sr({},this.constructor.defaultI18n)}get i18n(){return this.__customI18n}set i18n(e){e!==this.__customI18n&&(this.__customI18n=e,this.__effectiveI18n=Sr({},this.constructor.defaultI18n,this.__customI18n))}};var Dt=class{constructor(t){this.host=t,t.addEventListener("opened-changed",()=>{t.opened||this.__setVirtualKeyboardEnabled(!1)}),t.addEventListener("blur",()=>this.__setVirtualKeyboardEnabled(!0)),t.addEventListener("touchstart",()=>this.__setVirtualKeyboardEnabled(!0))}__setVirtualKeyboardEnabled(t){this.host.inputElement&&(this.host.inputElement.inputMode=t?"":"none")}};var It=Object.freeze({monthNames:["January","February","March","April","May","June","July","August","September","October","November","December"],weekdays:["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"],weekdaysShort:["Sun","Mon","Tue","Wed","Thu","Fri","Sat"],firstDayOfWeek:0,today:"Today",cancel:"Cancel",referenceDate:"",formatDate(s){let t=String(s.year).replace(/\d+/u,e=>"0000".substr(e.length)+e);return[s.month+1,s.day,t].join("/")},parseDate(s){let t=s.split("/"),e=new Date,i,r=e.getMonth(),n=e.getFullYear();if(t.length===3){if(r=parseInt(t[0])-1,i=parseInt(t[1]),n=parseInt(t[2]),t[2].length<3&&n>=0){let o=this.referenceDate?he(this.referenceDate):new Date;n=Zs(o,n,r,i)}}else t.length===2?(r=parseInt(t[0])-1,i=parseInt(t[1])):t.length===1&&(i=parseInt(t[0]));if(i!==void 0)return{day:i,month:r,year:n}},formatTitle:(s,t)=>`${s} ${t}`}),Er=s=>class extends ye(ft(fe(V(s)))){static get properties(){return{_selectedDate:{type:Object,sync:!0},_focusedDate:{type:Object,sync:!0},value:{type:String,notify:!0,value:"",sync:!0},initialPosition:{type:String},opened:{type:Boolean,reflectToAttribute:!0,notify:!0,observer:"_openedChanged",sync:!0},autoOpenDisabled:{type:Boolean,sync:!0},showWeekNumbers:{type:Boolean,value:!1,sync:!0},_fullscreen:{type:Boolean,value:!1,sync:!0},_fullscreenMediaQuery:{value:"(max-width: 450px), (max-height: 450px)"},min:{type:String,sync:!0},max:{type:String,sync:!0},isDateDisabled:{type:Function},_minDate:{type:Date,computed:"__computeMinOrMaxDate(min)"},_maxDate:{type:Date,computed:"__computeMinOrMaxDate(max)"},_noInput:{type:Boolean,computed:"_isNoInput(inputElement, _fullscreen, _ios, __effectiveI18n, opened, autoOpenDisabled)"},_ios:{type:Boolean,value:Je},_focusOverlayOnOpen:Boolean,_overlayContent:{type:Object,sync:!0},__enteredDate:{type:Date,sync:!0}}}static get observers(){return["_selectedDateChanged(_selectedDate, __effectiveI18n)","_focusedDateChanged(_focusedDate, __effectiveI18n)","__updateOverlayContent(_overlayContent, __effectiveI18n, label, _minDate, _maxDate, _focusedDate, _selectedDate, showWeekNumbers, isDateDisabled, __enteredDate)","__updateOverlayContentTheme(_overlayContent, _theme)","__updateOverlayContentFullScreen(_overlayContent, _fullscreen)"]}static get defaultI18n(){return It}static get constraints(){return[...super.constraints,"min","max"]}constructor(){super(),this._boundOnClick=this._onClick.bind(this),this._boundOnScroll=this._onScroll.bind(this)}get i18n(){return super.i18n}set i18n(e){super.i18n=e}get _inputElementValue(){return super._inputElementValue}set _inputElementValue(e){super._inputElementValue=e;let i=this.__parseDate(e);this.__setEnteredDate(i)}get __unparsableValue(){return!this._inputElementValue||this.__parseDate(this._inputElementValue)?"":this._inputElementValue}_onFocus(e){super._onFocus(e),this._noInput&&!T()&&e.target.blur()}_onBlur(e){super._onBlur(e),this.opened||(this.__commitParsedOrFocusedDate(),document.hasFocus()&&this._requestValidation())}ready(){super.ready(),this.addEventListener("click",this._boundOnClick),this.addController(new ue(this._fullscreenMediaQuery,e=>{this._fullscreen=e})),this.addController(new Dt(this)),this._overlayElement=this.$.overlay}updated(e){super.updated(e),(e.has("showWeekNumbers")||e.has("__effectiveI18n"))&&this.toggleAttribute("week-numbers",this.showWeekNumbers&&this.__effectiveI18n.firstDayOfWeek===1)}disconnectedCallback(){super.disconnectedCallback(),this.opened=!1}focus(e){this._noInput&&!T()?this.open():super.focus(e)}open(){!this.disabled&&!this.readonly&&(this.opened=!0)}close(){this.$.overlay.close()}__ensureContent(){if(this._overlayContent)return;let e=document.createElement("vaadin-date-picker-overlay-content");e.setAttribute("slot","overlay"),this.appendChild(e),this._overlayContent=e,e.addEventListener("close",()=>{this._close()}),e.addEventListener("focus-input",this._focusAndSelect.bind(this)),e.addEventListener("date-tap",i=>{this.__commitDate(i.detail.date),this._close()}),e.addEventListener("date-selected",i=>{this.__commitDate(i.detail.date)}),e.addEventListener("focusin",()=>{this._keyboardActive&&this._setFocused(!0)}),e.addEventListener("focusout",i=>{this._shouldRemoveFocus(i)&&this._setFocused(!1)}),e.addEventListener("focused-date-changed",i=>{this._focusedDate=i.detail.value}),e.addEventListener("click",i=>i.stopPropagation())}__parseDate(e){if(!this.__effectiveI18n.parseDate)return;let i=this.__effectiveI18n.parseDate(e);if(i&&(i=he(`${i.year}-${i.month+1}-${i.day}`)),i&&!isNaN(i.getTime()))return i}__formatDate(e){if(this.__effectiveI18n.formatDate)return this.__effectiveI18n.formatDate(Ei(e))}checkValidity(){let e=this._inputElementValue,i=!e||!!this._selectedDate&&e===this.__formatDate(this._selectedDate),r=!this._selectedDate||R(this._selectedDate,this._minDate,this._maxDate,this.isDateDisabled),n=!0;return this.inputElement&&this.inputElement.checkValidity&&(n=this.inputElement.checkValidity()),i&&r&&n}_shouldSetFocus(e){return!this._shouldKeepFocusRing}_shouldKeepFocusOnClearMousedown(){return this.opened?!0:super._shouldKeepFocusOnClearMousedown()}_shouldRemoveFocus(e){let{relatedTarget:i}=e;return this.opened&&i!==null&&i!==document.body&&!this.contains(i)&&!this._overlayContent.contains(i)?!0:!this.opened}_setFocused(e){super._setFocused(e),this._shouldKeepFocusRing=e&&this._keyboardActive}__commitValueChange(){let e=this.__unparsableValue;this.__committedValue!==this.value?(this._requestValidation(),this.dispatchEvent(new CustomEvent("change",{bubbles:!0}))):this.__committedUnparsableValue!==e&&(this._requestValidation(),this.dispatchEvent(new CustomEvent("unparsable-change"))),this.__committedValue=this.value,this.__committedUnparsableValue=e}__commitDate(e){this.__keepCommittedValue=!0,this._selectedDate=e,this.__keepCommittedValue=!1,this.__commitValueChange()}_close(){this._focus(),this.close()}_isNoInput(e,i,r,n,o,a){return!e||i&&(!a||o)||r&&o||!n.parseDate}_formatISO(e){return er(e)}_inputElementChanged(e){super._inputElementChanged(e),e&&(e.autocomplete="off",e.setAttribute("role","combobox"),e.setAttribute("aria-haspopup","dialog"),e.setAttribute("aria-expanded",!!this.opened),this._applyInputValue(this._selectedDate))}_openedChanged(e){e&&this.__ensureContent(),this.inputElement&&this.inputElement.setAttribute("aria-expanded",e)}_selectedDateChanged(e,i){e===void 0||i===void 0||(this.__keepInputValue||this._applyInputValue(e),this.value=this._formatISO(e),this._ignoreFocusedDateChange=!0,this._focusedDate=e,this._ignoreFocusedDateChange=!1)}_focusedDateChanged(e,i){e===void 0||i===void 0||!this._ignoreFocusedDateChange&&!this._noInput&&this._applyInputValue(e)}_valueChanged(e,i){let r=he(e);if(e&&!r){this.value=i;return}e?S(this._selectedDate,r)||(this._selectedDate=r,i!==void 0&&this._requestValidation()):this._selectedDate=null,this.__keepCommittedValue||(this.__committedValue=this.value,this.__committedUnparsableValue=""),this._toggleHasValue(this._hasValue)}__updateOverlayContent(e,i,r,n,o,a,l,d,h,c){e&&(e.i18n=i,e.label=r,e.minDate=n,e.maxDate=o,e.focusedDate=a,e.selectedDate=l,e.showWeekNumbers=d,e.isDateDisabled=h,e.enteredDate=c)}__updateOverlayContentTheme(e,i){e&&(i?e.setAttribute("theme",i):e.removeAttribute("theme"))}__updateOverlayContentFullScreen(e,i){e&&e.toggleAttribute("fullscreen",i)}_onOverlayEscapePress(e){e.stopPropagation(),this._focusedDate=this._selectedDate,this._applyInputValue(this._selectedDate),this._close()}_onOverlayOpened(){let e=this._overlayContent;e.reset();let i=this._getInitialPosition();e.initialPosition=i;let r=e.focusedDate||i;e.scrollToDate(r),this._ignoreFocusedDateChange=!0,e.focusedDate=r,this._ignoreFocusedDateChange=!1,window.addEventListener("scroll",this._boundOnScroll,!0),this._focusOverlayOnOpen?(e.focusDateElement(),this._focusOverlayOnOpen=!1):this._focus();let n=this.inputElement;this._noInput&&n&&(n.blur(),this._overlayContent.focusDateElement());let o=this._noInput?e:this;this.__showOthers=wr(o)}_getInitialPosition(){let e=he(this.initialPosition),i=this._selectedDate||this._overlayContent.initialPosition||e||new Date;return e||R(i,this._minDate,this._maxDate,this.isDateDisabled)?i:this._minDate||this._maxDate?pt(i,[this._minDate,this._maxDate]):new Date}__commitParsedOrFocusedDate(){if(this._ignoreFocusedDateChange=!0,this.__effectiveI18n.parseDate){let e=this._inputElementValue||"",i=this.__parseDate(e);i?this.__commitDate(i):(this.__keepInputValue=!0,this.__commitDate(null),this.__keepInputValue=!1)}else this._focusedDate&&this.__commitDate(this._focusedDate);this._ignoreFocusedDateChange=!1}_onOverlayClosed(){this.__showOthers&&(this.__showOthers(),this.__showOthers=null),window.removeEventListener("scroll",this._boundOnScroll,!0),this.__commitParsedOrFocusedDate(),this.inputElement&&this.inputElement.selectionStart&&(this.inputElement.selectionStart=this.inputElement.selectionEnd),!this.value&&!this._keyboardActive&&this._requestValidation()}_onScroll(e){(e.target===window||!this._overlayContent.contains(e.target))&&this._overlayContent._repositionYearScroller()}_focus(){this._noInput||this.inputElement.focus()}_focusAndSelect(){this._focus(),this._setSelectionRange(0,this._inputElementValue.length)}_applyInputValue(e){this._inputElementValue=e?this.__formatDate(e):""}_setSelectionRange(e,i){this.inputElement&&this.inputElement.setSelectionRange(e,i)}_onChange(e){e.stopPropagation()}_onClick(e){e.composedPath().includes(this._overlayElement)||this._isClearButton(e)||this._onHostClick(e)}_onHostClick(e){(!this.autoOpenDisabled||this._noInput)&&(e.preventDefault(),this.open())}_onClearButtonClick(e){e.preventDefault(),this.__commitDate(null)}_onKeyDown(e){switch(super._onKeyDown(e),this._noInput&&["Tab","Escape"].indexOf(e.key)===-1&&e.preventDefault(),e.key){case"ArrowDown":case"ArrowUp":e.preventDefault(),this.opened?this._overlayContent.focusDateElement():(this._focusOverlayOnOpen=!0,this.open());break;case"Tab":this.opened&&(e.preventDefault(),e.stopPropagation(),this._setSelectionRange(0,0),e.shiftKey?this._overlayContent.focusCancel():this._overlayContent.focusDateElement());break;default:break}}_onEnter(e){e.composedPath().includes(this._overlayContent)||(this.opened?this.close():this.__commitParsedOrFocusedDate())}_onEscape(e){if(this.opened){this._onOverlayEscapePress(e);return}if(this.clearButtonVisible&&this.value&&!this.readonly){e.stopPropagation(),this._onClearButtonClick(e);return}this.inputElement.value===""?this.__commitDate(null):this._applyInputValue(this._selectedDate)}_isClearButton(e){return e.composedPath()[0]===this.clearElement}_onInput(){!this.opened&&this._inputElementValue&&!this.autoOpenDisabled&&this.open();let e=this.__parseDate(this._inputElementValue||"");e&&(this._ignoreFocusedDateChange=!0,S(e,this._focusedDate)||(this._focusedDate=e),this._ignoreFocusedDateChange=!1),this.__setEnteredDate(e)}__setEnteredDate(e){e?S(this.__enteredDate,e)||(this.__enteredDate=e):this.__enteredDate=null}__computeMinOrMaxDate(e){return he(e)}};var Li=class extends Er(St(y(U(g(b(_)))))){static get is(){return"vaadin-date-picker"}static get styles(){return[ge,xr]}static get properties(){return{_positionTarget:{type:Object,sync:!0}}}get clearElement(){return this.$.clearButton}render(){return v`
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
          theme="${pe(this._theme)}"
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
        theme="${pe(this._theme)}"
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
    `}ready(){super.ready(),this.addController(new me(this,e=>{this._setInputElement(e),this._setFocusElement(e),this.stateTarget=e,this.ariaTarget=e},{uniqueIdPrefix:"search-input"})),this.addController(new ve(this.inputElement,this._labelController)),this._tooltipController=new O(this),this.addController(this._tooltipController),this._tooltipController.setPosition("top"),this._tooltipController.setAriaTarget(this.inputElement),this._tooltipController.setShouldShow(e=>!e.opened),this._positionTarget=this.shadowRoot.querySelector('[part="input-field"]'),this.shadowRoot.querySelector('[part="field-button toggle-button"]').addEventListener("mousedown",e=>e.preventDefault())}_onOpenedChanged(t){this.opened=t.detail.value}_onVaadinOverlayClose(t){let e=t.detail.sourceEvent;e?.composedPath().includes(this)&&!e.composedPath().includes(this._overlayElement)&&t.preventDefault()}_toggle(t){t.stopPropagation(),this.$.overlay.opened?this.close():this.open()}};m(Li);var kr=u`
  :host([focused]) {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-focus-ring-width) / -1);
  }
`;var Dr=s=>class extends s{static get properties(){return{index:{type:Number},item:{type:Object},label:{type:String},selected:{type:Boolean,value:!1,reflectToAttribute:!0},focused:{type:Boolean,value:!1,reflectToAttribute:!0},renderer:{type:Function}}}static get observers(){return["__rendererOrItemChanged(renderer, index, item, selected, focused)","__updateLabel(label, renderer)"]}static get observedAttributes(){return[...super.observedAttributes,"hidden"]}attributeChangedCallback(e,i,r){e==="hidden"&&r!==null?this.index=void 0:super.attributeChangedCallback(e,i,r)}connectedCallback(){super.connectedCallback(),this._owner=this.parentNode.owner;let e=this._getHostDir();e&&this.setAttribute("dir",e)}_getHostDir(){return this._owner&&this._owner.$.overlay.getAttribute("dir")}requestContentUpdate(){if(!this.renderer||this.hidden)return;let e={index:this.index,item:this.item,focused:this.focused,selected:this.selected};this.renderer(this,this._owner,e)}__rendererOrItemChanged(e,i,r){r===void 0||i===void 0||(this._oldRenderer!==e&&(this.innerHTML="",delete this._$litPart$),e&&(this._oldRenderer=e,this.requestContentUpdate()))}__updateLabel(e,i){i||(this.textContent=e)}};var Ir=u`
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
`;var Ni=class extends Dr(y(I(g(b(_))))){static get is(){return"vaadin-time-picker-item"}static get styles(){return[Ir,kr]}render(){return v`
      <span part="checkmark" aria-hidden="true"></span>
      <div part="content">
        <slot></slot>
      </div>
    `}};m(Ni);var Tr=s=>class extends ot(s){static get observers(){return["_setOverlayWidth(positionTarget, opened)"]}constructor(){super(),this.requiredVerticalSpace=200}_shouldCloseOnOutsideClick(e){let i=e.composedPath();return!i.includes(this.positionTarget)&&!i.includes(this)}_mouseDownListener(e){super._mouseDownListener(e),this._shouldCloseOnOutsideClick(e)&&!Ae(e.composedPath()[0])&&e.preventDefault()}_updateOverlayWidth(){this.style.setProperty(`--_${this.localName}-default-width`,`${this.positionTarget.offsetWidth}px`)}_setOverlayWidth(e,i){e&&i&&(this._updateOverlayWidth(),this._updatePosition())}};var Ar=u`
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
`;var Fi=class extends Tr(rt(I(y(g(b(_)))))){static get is(){return"vaadin-time-picker-overlay"}static get styles(){return[Ze,Ar]}render(){return v`
      <div part="overlay" id="overlay">
        <div part="content" id="content">
          <slot></slot>
        </div>
      </div>
    `}};m(Fi);var Mr=navigator.userAgent.match(/iP(?:hone|ad;(?: U;)? CPU) OS (\d+)/u),uo=Mr&&Mr[1]>=8,Pr=3,Or={_ratio:.5,_scrollerPaddingTop:0,_scrollPosition:0,_physicalSize:0,_physicalAverage:0,_physicalAverageCount:0,_physicalTop:0,_virtualCount:0,_estScrollHeight:0,_scrollHeight:0,_viewportHeight:0,_viewportWidth:0,_physicalItems:null,_physicalSizes:null,_firstVisibleIndexVal:null,_lastVisibleIndexVal:null,_maxPages:2,_templateCost:0,get _physicalBottom(){return this._physicalTop+this._physicalSize},get _scrollBottom(){return this._scrollPosition+this._viewportHeight},get _virtualEnd(){return this._virtualStart+this._physicalCount-1},get _hiddenContentSize(){return this._physicalSize-this._viewportHeight},get _maxScrollTop(){return this._estScrollHeight-this._viewportHeight+this._scrollOffset},get _maxVirtualStart(){let s=this._virtualCount;return Math.max(0,s-this._physicalCount)},get _virtualStart(){return this._virtualStartVal||0},set _virtualStart(s){s=this._clamp(s,0,this._maxVirtualStart),this._virtualStartVal=s},get _physicalStart(){return this._physicalStartVal||0},set _physicalStart(s){s%=this._physicalCount,s<0&&(s=this._physicalCount+s),this._physicalStartVal=s},get _physicalEnd(){return(this._physicalStart+this._physicalCount-1)%this._physicalCount},get _physicalCount(){return this._physicalCountVal||0},set _physicalCount(s){this._physicalCountVal=s},get _optPhysicalSize(){return this._viewportHeight===0?1/0:this._viewportHeight*this._maxPages},get _isVisible(){return!!(this.offsetWidth||this.offsetHeight)},get firstVisibleIndex(){let s=this._firstVisibleIndexVal;if(s==null){let t=this._physicalTop+this._scrollOffset;s=this._iterateItems((e,i)=>{if(t+=this._getPhysicalSizeIncrement(e),t>this._scrollPosition)return i})||0,this._firstVisibleIndexVal=s}return s},get lastVisibleIndex(){let s=this._lastVisibleIndexVal;if(s==null){let t=this._physicalTop+this._scrollOffset;this._iterateItems((e,i)=>{t<this._scrollBottom&&(s=i),t+=this._getPhysicalSizeIncrement(e)}),this._lastVisibleIndexVal=s}return s},get _scrollOffset(){return this._scrollerPaddingTop+this.scrollOffset},_scrollHandler(){let s=Math.max(0,Math.min(this._maxScrollTop,this._scrollTop)),t=s-this._scrollPosition,e=t>=0;if(this._scrollPosition=s,this._firstVisibleIndexVal=null,this._lastVisibleIndexVal=null,Math.abs(t)>this._physicalSize&&this._physicalSize>0){t-=this._scrollOffset;let i=Math.round(t/this._physicalAverage);this._virtualStart+=i,this._physicalStart+=i,this._physicalTop=Math.min(Math.floor(this._virtualStart)*this._physicalAverage,this._scrollPosition),this._update()}else if(this._physicalCount>0){let i=this._getReusables(e);e?(this._physicalTop=i.physicalTop,this._virtualStart+=i.indexes.length,this._physicalStart+=i.indexes.length):(this._virtualStart-=i.indexes.length,this._physicalStart-=i.indexes.length),this._update(i.indexes,e?null:i.indexes),this._debounce("_increasePoolIfNeeded",this._increasePoolIfNeeded.bind(this,0),Q)}},_getReusables(s){let t,e,i,r=[],n=this._hiddenContentSize*this._ratio,o=this._virtualStart,a=this._virtualEnd,l=this._physicalCount,d=this._physicalTop+this._scrollOffset,h=this._physicalBottom+this._scrollOffset,c=this._scrollPosition,p=this._scrollBottom;for(s?(t=this._physicalStart,e=c-d):(t=this._physicalEnd,e=h-p);i=this._getPhysicalSizeIncrement(t),e-=i,!(r.length>=l||e<=n);)if(s){if(a+r.length+1>=this._virtualCount||d+i>=c-this._scrollOffset)break;r.push(t),d+=i,t=(t+1)%l}else{if(o-r.length<=0||d+this._physicalSize-i<=p)break;r.push(t),d-=i,t=t===0?l-1:t-1}return{indexes:r,physicalTop:d-this._scrollOffset}},_update(s,t){if(!(s&&s.length===0||this._physicalCount===0)){if(this._assignModels(s),this._updateMetrics(s),t)for(;t.length;){let e=t.pop();this._physicalTop-=this._getPhysicalSizeIncrement(e)}this._positionItems(),this._updateScrollerSize()}},_isClientFull(){return this._scrollBottom!==0&&this._physicalBottom-1>=this._scrollBottom&&this._physicalTop<=this._scrollPosition},_increasePoolIfNeeded(s){let e=this._clamp(this._physicalCount+s,Pr,this._virtualCount-this._virtualStart)-this._physicalCount,i=Math.round(this._physicalCount*.5);if(!(e<0)){if(e>0){let r=window.performance.now();[].push.apply(this._physicalItems,this._createPool(e));for(let n=0;n<e;n++)this._physicalSizes.push(0);this._physicalCount+=e,this._physicalStart>this._physicalEnd&&this._isIndexRendered(this._focusedVirtualIndex)&&this._getPhysicalIndex(this._focusedVirtualIndex)<this._physicalEnd&&(this._physicalStart+=e),this._update(),this._templateCost=(window.performance.now()-r)/e,i=Math.round(this._physicalCount*.5)}this._virtualEnd>=this._virtualCount-1||i===0||(this._isClientFull()?this._physicalSize<this._optPhysicalSize&&this._debounce("_increasePoolIfNeeded",this._increasePoolIfNeeded.bind(this,this._clamp(Math.round(50/this._templateCost),1,i)),at):this._debounce("_increasePoolIfNeeded",this._increasePoolIfNeeded.bind(this,i),Q))}},_render(){if(!(!this.isAttached||!this._isVisible))if(this._physicalCount!==0){let s=this._getReusables(!0);this._physicalTop=s.physicalTop,this._virtualStart+=s.indexes.length,this._physicalStart+=s.indexes.length,this._update(s.indexes),this._update(),this._increasePoolIfNeeded(0)}else this._virtualCount>0&&(this.updateViewportBoundaries(),this._increasePoolIfNeeded(Pr))},_itemsChanged(s){s.path==="items"&&(this._virtualStart=0,this._physicalTop=0,this._virtualCount=this.items?this.items.length:0,this._physicalIndexForKey={},this._firstVisibleIndexVal=null,this._lastVisibleIndexVal=null,this._physicalItems||(this._physicalItems=[]),this._physicalSizes||(this._physicalSizes=[]),this._physicalStart=0,this._scrollTop>this._scrollOffset&&this._resetScrollPosition(0),this._debounce("_render",this._render,Z))},_iterateItems(s,t){let e,i,r,n;if(arguments.length===2&&t){for(n=0;n<t.length;n++)if(e=t[n],i=this._computeVidx(e),(r=s.call(this,e,i))!=null)return r}else{for(e=this._physicalStart,i=this._virtualStart;e<this._physicalCount;e++,i++)if((r=s.call(this,e,i))!=null)return r;for(e=0;e<this._physicalStart;e++,i++)if((r=s.call(this,e,i))!=null)return r}},_computeVidx(s){return s>=this._physicalStart?this._virtualStart+(s-this._physicalStart):this._virtualStart+(this._physicalCount-this._physicalStart)+s},_positionItems(){this._adjustScrollPosition();let s=this._physicalTop;this._iterateItems(t=>{this.translate3d(0,`${s}px`,0,this._physicalItems[t]),s+=this._physicalSizes[t]})},_getPhysicalSizeIncrement(s){return this._physicalSizes[s]},_adjustScrollPosition(){let s=this._virtualStart===0?this._physicalTop:Math.min(this._scrollPosition+this._physicalTop,0);if(s!==0){this._physicalTop-=s;let t=this._scrollPosition;!uo&&t>0&&this._resetScrollPosition(t-s)}},_resetScrollPosition(s){this.scrollTarget&&s>=0&&(this._scrollTop=s,this._scrollPosition=this._scrollTop)},_updateScrollerSize(s){let t=this._physicalBottom+Math.max(this._virtualCount-this._physicalCount-this._virtualStart,0)*this._physicalAverage;this._estScrollHeight=t,(s||this._scrollHeight===0||this._scrollPosition>=t-this._physicalSize||Math.abs(t-this._scrollHeight)>=this._viewportHeight)&&(this.$.items.style.height=`${t}px`,this._scrollHeight=t)},scrollToIndex(s){if(typeof s!="number"||s<0||s>this.items.length-1||(J(),this._physicalCount===0))return;s=this._clamp(s,0,this._virtualCount-1),(!this._isIndexRendered(s)||s>=this._maxVirtualStart)&&(this._virtualStart=s-1),this._assignModels(),this._updateMetrics(),this._physicalTop=this._virtualStart*this._physicalAverage;let t=this._physicalStart,e=this._virtualStart,i=0,r=this._hiddenContentSize;for(;e<s&&i<=r;)i+=this._getPhysicalSizeIncrement(t),t=(t+1)%this._physicalCount,e+=1;this._updateScrollerSize(!0),this._positionItems(),this._resetScrollPosition(this._physicalTop+this._scrollOffset+i),this._increasePoolIfNeeded(0),this._firstVisibleIndexVal=null,this._lastVisibleIndexVal=null},_resetAverage(){this._physicalAverage=0,this._physicalAverageCount=0},_resizeHandler(){this._debounce("_render",()=>{this._firstVisibleIndexVal=null,this._lastVisibleIndexVal=null,this._isVisible?(this.updateViewportBoundaries(),this.toggleScrollListener(!0),this._resetAverage(),this._render()):this.toggleScrollListener(!1)},Z)},_isIndexRendered(s){return s>=this._virtualStart&&s<=this._virtualEnd},_getPhysicalIndex(s){return(this._physicalStart+(s-this._virtualStart))%this._physicalCount},_clamp(s,t,e){return Math.min(e,Math.max(t,s))},_debounce(s,t,e){this._debouncers||(this._debouncers={}),this._debouncers[s]=w.debounce(this._debouncers[s],e,t.bind(this)),lt(this._debouncers[s])}};var po=1e5,Bi=1e3,Le=class{constructor({createElements:t,updateElement:e,scrollTarget:i,scrollContainer:r,reorderElements:n,elementsContainer:o,__disableHeightPlaceholder:a}){this.isAttached=!0,this._vidxOffset=0,this.createElements=t,this.updateElement=e,this.scrollTarget=i,this.scrollContainer=r,this.reorderElements=n,this.elementsContainer=o||r,this.__disableHeightPlaceholder=a??!1,this._maxPages=1.3,this.__placeholderHeight=200,this.__elementHeightQueue=Array(10),this.timeouts={SCROLL_REORDER:500,PREVENT_OVERSCROLL:500,FIX_INVALID_ITEM_POSITIONING:100},this.__resizeObserver=new ResizeObserver(()=>this._resizeHandler()),getComputedStyle(this.scrollTarget).overflow==="visible"&&(this.scrollTarget.style.overflow="auto"),getComputedStyle(this.scrollContainer).position==="static"&&(this.scrollContainer.style.position="relative"),this.__resizeObserver.observe(this.scrollTarget),this.scrollTarget.addEventListener("scroll",()=>this._scrollHandler()),new ResizeObserver(([{contentRect:d}])=>{let h=d.width===0&&d.height===0;!h&&this.__scrollTargetHidden&&this.scrollTarget.scrollTop!==this._scrollPosition&&(this.scrollTarget.scrollTop=this._scrollPosition),this.__scrollTargetHidden=h}).observe(this.scrollTarget),this.scrollTarget.addEventListener("virtualizer-element-focused",d=>this.__onElementFocused(d)),this.elementsContainer.addEventListener("focusin",()=>{this.scrollTarget.dispatchEvent(new CustomEvent("virtualizer-element-focused",{detail:{element:this.__getFocusedElement()}}))}),this.reorderElements&&(this.scrollTarget.addEventListener("mousedown",d=>{d.target===this.scrollTarget&&(this.__mouseDown=!0)}),this.scrollTarget.addEventListener("mouseup",()=>{this.__mouseDown=!1,this.__pendingReorder&&this.__reorderElements()}))}get scrollOffset(){return 0}get adjustedFirstVisibleIndex(){return this.firstVisibleIndex+this._vidxOffset}get adjustedLastVisibleIndex(){return this.lastVisibleIndex+this._vidxOffset}get _maxVirtualIndexOffset(){return this.size-this._virtualCount}__hasPlaceholders(){return this.__getVisibleElements().some(t=>t.__virtualizerPlaceholder)}scrollToIndex(t){if(typeof t!="number"||isNaN(t)||this.size===0||!this.scrollTarget.offsetHeight)return;delete this.__pendingScrollToIndex,this._physicalCount<=3&&this.flush(),t=this._clamp(t,0,this.size-1);let e=this.__getVisibleElements().length,i=Math.floor(t/this.size*this._virtualCount);this._virtualCount-i<e?(i=this._virtualCount-(this.size-t),this._vidxOffset=this._maxVirtualIndexOffset):i<e?t<Bi?(i=t,this._vidxOffset=0):(i=Bi,this._vidxOffset=t-i):this._vidxOffset=t-i,this.__skipNextVirtualIndexAdjust=!0,super.scrollToIndex(i),this.adjustedFirstVisibleIndex!==t&&this._scrollTop<this._maxScrollTop&&!this.grid&&(this._scrollTop-=this.__getIndexScrollOffset(t)||0),this._scrollHandler(),this.__hasPlaceholders()&&(this.__pendingScrollToIndex=t)}flush(){this.scrollTarget.offsetHeight!==0&&(this._resizeHandler(),J(),this._scrollHandler(),this.__fixInvalidItemPositioningDebouncer&&this.__fixInvalidItemPositioningDebouncer.flush(),this.__scrollReorderDebouncer&&this.__scrollReorderDebouncer.flush(),this.__debouncerWheelAnimationFrame&&this.__debouncerWheelAnimationFrame.flush())}hostConnected(){this.scrollTarget.offsetParent&&this.scrollTarget.scrollTop!==this._scrollPosition&&(this.scrollTarget.scrollTop=this._scrollPosition)}update(t=0,e=this.size-1){let i=[];this.__getVisibleElements().forEach(r=>{r.__virtualIndex>=t&&r.__virtualIndex<=e&&(this.__updateElement(r,r.__virtualIndex,!0),i.push(r))}),this.__afterElementsUpdated(i)}_updateMetrics(t){J();let e=0,i=0,r=this._physicalAverageCount,n=this._physicalAverage;this._iterateItems((o,a)=>{i+=this._physicalSizes[o];let l=this._physicalSizes[o];this._physicalSizes[o]=Math.ceil(this.__getBorderBoxHeight(this._physicalItems[o])),this._physicalSizes[o]!==l&&(this.__resizeObserver.unobserve(this._physicalItems[o]),this.__resizeObserver.observe(this._physicalItems[o],{box:"border-box"})),e+=this._physicalSizes[o],this._physicalAverageCount+=this._physicalSizes[o]?1:0},t),this._physicalSize=this._physicalSize+e-i,this._physicalAverageCount!==r&&(this._physicalAverage=Math.round((n*r+e)/this._physicalAverageCount))}__getBorderBoxHeight(t){let e=getComputedStyle(t),i=parseFloat(e.height)||0;if(e.boxSizing==="border-box")return i;let r=parseFloat(e.paddingBottom)||0,n=parseFloat(e.paddingTop)||0,o=parseFloat(e.borderBottomWidth)||0,a=parseFloat(e.borderTopWidth)||0;return i+r+n+o+a}__updateElement(t,e,i){t.__virtualizerPlaceholder&&(t.style.paddingTop="",t.style.opacity="",t.__virtualizerPlaceholder=!1),!this.__preventElementUpdates&&(t.__lastUpdatedIndex!==e||i)&&(this.updateElement(t,e),t.__lastUpdatedIndex=e)}__afterElementsUpdated(t){this.__disableHeightPlaceholder||t.forEach(e=>{let i=e.offsetHeight;if(i===0)e.style.paddingTop=`${this.__placeholderHeight}px`,e.style.opacity="0",e.__virtualizerPlaceholder=!0,this.__placeholderClearDebouncer=w.debounce(this.__placeholderClearDebouncer,Z,()=>this._resizeHandler());else{this.__elementHeightQueue.push(i),this.__elementHeightQueue.shift();let r=this.__elementHeightQueue.filter(n=>n!==void 0);this.__placeholderHeight=Math.round(r.reduce((n,o)=>n+o,0)/r.length)}}),this.__pendingScrollToIndex!==void 0&&!this.__hasPlaceholders()&&this.scrollToIndex(this.__pendingScrollToIndex)}__getIndexScrollOffset(t){let e=this.__getVisibleElements().find(i=>i.__virtualIndex===t);return e?this.scrollTarget.getBoundingClientRect().top-e.getBoundingClientRect().top:void 0}__restoreScrollOffset(t,e){let i=this.__getIndexScrollOffset(t);e!==void 0&&i!==void 0&&(this._scrollTop+=e-i)}get size(){return this.__size}set size(t){if(t===this.size)return;this.__fixInvalidItemPositioningDebouncer&&this.__fixInvalidItemPositioningDebouncer.cancel(),this._debouncers&&this._debouncers._increasePoolIfNeeded&&this._debouncers._increasePoolIfNeeded.cancel();let e=t>0&&this._scrollTop>0,i,r;e&&(i=this.adjustedFirstVisibleIndex,r=this.__getIndexScrollOffset(i)),this.__size=t,this.__preventElementUpdates=e,this._itemsChanged({path:"items"}),J(),e&&(i=Math.min(i,t-1),this.scrollToIndex(i),this.__restoreScrollOffset(i,r)),this.__preventElementUpdates=!1,this._isVisible||this._assignModels(),this.elementsContainer.children.length||requestAnimationFrame(()=>this._resizeHandler()),this._updateScrollerSize(!0),this._resizeHandler(),J(),this._debounce("_update",this._update,Q)}get _scrollTop(){return this.scrollTarget.scrollTop}set _scrollTop(t){this.scrollTarget.scrollTop=t}get items(){return{length:Math.min(this.size,po)}}get offsetHeight(){return this.scrollTarget.offsetHeight}get $(){return{items:this.scrollContainer}}updateViewportBoundaries(){let t=window.getComputedStyle(this.scrollTarget);this._scrollerPaddingTop=this.scrollTarget===this?0:parseInt(t["padding-top"],10),this._isRTL=t.direction==="rtl",this._viewportWidth=this.elementsContainer.offsetWidth,this._viewportHeight=this.scrollTarget.offsetHeight}setAttribute(){}_createPool(t){let e=this.createElements(t),i=document.createDocumentFragment();return e.forEach(r=>{r.style.position="absolute",i.appendChild(r),this.__resizeObserver.observe(r,{box:"border-box"})}),this.elementsContainer.appendChild(i),e}_assignModels(t){let e=[];this._iterateItems((i,r)=>{let n=this._physicalItems[i];n.hidden=r>=this.size,n.hidden?delete n.__lastUpdatedIndex:(n.__virtualIndex=r+(this._vidxOffset||0),this.__updateElement(n,n.__virtualIndex),e.push(n))},t),this.__afterElementsUpdated(e)}_isClientFull(){return setTimeout(()=>{this.__clientFull=!0}),this.__clientFull||super._isClientFull()}translate3d(t,e,i,r){r.style.transform=`translateY(${e})`}toggleScrollListener(){}__getFocusedElement(t=this.__getVisibleElements()){let e=document.activeElement;for(;e?.shadowRoot?.activeElement;)e=e.shadowRoot.activeElement;for(;e&&!t.includes(e);)e=e.assignedSlot||e.parentNode||e.host;return e}__nextFocusableSiblingMissing(t,e){return e.indexOf(t)===e.length-1&&this.size>t.__virtualIndex+1}__previousFocusableSiblingMissing(t,e){return e.indexOf(t)===0&&t.__virtualIndex>0}__onElementFocused(t){if(!this.reorderElements)return;let e=t.detail.element;if(!e)return;let i=this.__getVisibleElements();(this.__previousFocusableSiblingMissing(e,i)||this.__nextFocusableSiblingMissing(e,i))&&this.flush();let r=this.__getVisibleElements();this.__nextFocusableSiblingMissing(e,r)?(this._scrollTop+=Math.ceil(e.getBoundingClientRect().bottom)-Math.floor(this.scrollTarget.getBoundingClientRect().bottom-1),this.flush()):this.__previousFocusableSiblingMissing(e,r)&&(this._scrollTop-=Math.ceil(this.scrollTarget.getBoundingClientRect().top+1)-Math.floor(e.getBoundingClientRect().top),this.flush())}_scrollHandler(){if(this.scrollTarget.offsetHeight===0)return;this._adjustVirtualIndexOffset(this._scrollTop-this._scrollPosition);let t=this._scrollTop-this._scrollPosition;if(super._scrollHandler(),this._physicalCount!==0){let e=t>=0,i=this._getReusables(!e);i.indexes.length&&(this._physicalTop=i.physicalTop,e?(this._virtualStart-=i.indexes.length,this._physicalStart-=i.indexes.length):(this._virtualStart+=i.indexes.length,this._physicalStart+=i.indexes.length),this._resizeHandler())}t&&(this.__fixInvalidItemPositioningDebouncer=w.debounce(this.__fixInvalidItemPositioningDebouncer,A.after(this.timeouts.FIX_INVALID_ITEM_POSITIONING),()=>this.__fixInvalidItemPositioning()),this.__overscrollDebouncer?.isActive()||(this.scrollTarget.style.overscrollBehavior="none"),this.__overscrollDebouncer=w.debounce(this.__overscrollDebouncer,A.after(this.timeouts.PREVENT_OVERSCROLL),()=>{this.scrollTarget.style.overscrollBehavior=null})),this.reorderElements&&(this.__scrollReorderDebouncer=w.debounce(this.__scrollReorderDebouncer,A.after(this.timeouts.SCROLL_REORDER),()=>this.__reorderElements())),this._scrollPosition===0&&this.firstVisibleIndex!==0&&Math.abs(t)>0&&this.scrollToIndex(0)}_resizeHandler(){super._resizeHandler();let t=this.adjustedLastVisibleIndex===this.size-1,e=this._physicalTop-this._scrollPosition;if(t&&e>0){let i=Math.ceil(e/this._physicalAverage);this._virtualStart=Math.max(0,this._virtualStart-i),this._physicalStart=Math.max(0,this._physicalStart-i),super.scrollToIndex(this._virtualCount-1),this.scrollTarget.scrollTop=this.scrollTarget.scrollHeight-this.scrollTarget.clientHeight}}__fixInvalidItemPositioning(){if(!this.scrollTarget.isConnected)return;let t=this._physicalTop>this._scrollTop,e=this._physicalBottom<this._scrollBottom,i=this.adjustedFirstVisibleIndex===0,r=this.adjustedLastVisibleIndex===this.size-1;if(t&&!i||e&&!r){let n=e,o=this._ratio;this._ratio=0,this._scrollPosition=this._scrollTop+(n?-1:1),this._scrollHandler(),this._ratio=o}}_increasePoolIfNeeded(t){if(this._physicalCount>2&&this._physicalAverage>0&&t>0){let i=Math.ceil(this._optPhysicalSize/this._physicalAverage)-this._physicalCount;super._increasePoolIfNeeded(Math.max(t,Math.min(100,i)))}else super._increasePoolIfNeeded(t)}get _optPhysicalSize(){let t=super._optPhysicalSize;return t<=0||this.__hasPlaceholders()?t:t+this.__getItemHeightBuffer()}__getItemHeightBuffer(){if(this._physicalCount===0)return 0;let t=Math.ceil(this._viewportHeight*(this._maxPages-1)/2),e=Math.max(...this._physicalSizes);return e>Math.min(...this._physicalSizes)?Math.max(0,e-t):0}__getVisibleElements(){return Array.from(this.elementsContainer.children).filter(t=>!t.hidden)}__reorderElements(){if(this.__mouseDown){this.__pendingReorder=!0;return}this.__pendingReorder=!1;let t=this._virtualStart+(this._vidxOffset||0),e=this.__getVisibleElements(),i=this.__getFocusedElement(e)||e[0];if(!i)return;let r=i.__virtualIndex-t,n=e.indexOf(i)-r;if(n>0)for(let o=0;o<n;o++)this.elementsContainer.appendChild(e[o]);else if(n<0)for(let o=e.length+n;o<e.length;o++)this.elementsContainer.insertBefore(e[o],e[0]);if(ks){let{transform:o}=this.scrollTarget.style;this.scrollTarget.style.transform="translateZ(0)",setTimeout(()=>{this.scrollTarget.style.transform=o})}}_adjustVirtualIndexOffset(t){let e=this._maxVirtualIndexOffset;if(this._virtualCount>=this.size)this._vidxOffset=0;else if(this.__skipNextVirtualIndexAdjust)this.__skipNextVirtualIndexAdjust=!1;else if(Math.abs(t)>1e4){let i=this._scrollTop/(this.scrollTarget.scrollHeight-this.scrollTarget.clientHeight);this._vidxOffset=Math.round(i*e)}else{let i=this._vidxOffset,r=Bi,n=100,o,a,l=()=>{o=this.adjustedFirstVisibleIndex,a=this.__getIndexScrollOffset(o)};this._scrollTop===0?i!==0&&(l(),this._vidxOffset=0,super.scrollToIndex(0)):this.firstVisibleIndex<r&&this._vidxOffset>0&&(l(),this._vidxOffset-=Math.min(this._vidxOffset,n),super.scrollToIndex(this.firstVisibleIndex+(i-this._vidxOffset))),this._scrollTop>=this._maxScrollTop&&this._maxScrollTop>0?i!==e&&(l(),this._vidxOffset=e,super.scrollToIndex(this._virtualCount-1)):this.firstVisibleIndex>this._virtualCount-r&&this._vidxOffset<e&&(l(),this._vidxOffset+=Math.min(e-this._vidxOffset,n),super.scrollToIndex(this.firstVisibleIndex-(this._vidxOffset-i))),o!==void 0&&this.__restoreScrollOffset(o,a)}}};Object.setPrototypeOf(Le.prototype,Or);var Tt=class{constructor(t){this.__adapter=new Le(t)}get firstVisibleIndex(){return this.__adapter.adjustedFirstVisibleIndex}get lastVisibleIndex(){return this.__adapter.adjustedLastVisibleIndex}get size(){return this.__adapter.size}set size(t){this.__adapter.size=t}scrollToIndex(t){this.__adapter.scrollToIndex(t)}update(t=0,e=this.size-1){this.__adapter.update(t,e)}flush(){this.__adapter.flush()}hostConnected(){this.__adapter.hostConnected()}};var Ne=class{toString(){return""}};var Vr=s=>class extends s{static get properties(){return{items:{type:Array,sync:!0,observer:"__itemsChanged"},focusedIndex:{type:Number,sync:!0,observer:"__focusedIndexChanged"},loading:{type:Boolean,sync:!0,observer:"__loadingChanged"},opened:{type:Boolean,sync:!0,observer:"__openedChanged"},selectedItem:{type:Object,sync:!0,observer:"__selectedItemChanged"},itemClassNameGenerator:{type:Object,observer:"__itemClassNameGeneratorChanged"},itemIdPath:{type:String},owner:{type:Object},getItemLabel:{type:Object},renderer:{type:Object,sync:!0,observer:"__rendererChanged"},theme:{type:String}}}constructor(){super(),this.__boundOnItemClick=this.__onItemClick.bind(this)}get _viewportTotalPaddingBottom(){if(this._cachedViewportTotalPaddingBottom===void 0){let e=window.getComputedStyle(this.$.selector);this._cachedViewportTotalPaddingBottom=[e.paddingBottom,e.borderBottomWidth].map(i=>parseInt(i,10)).reduce((i,r)=>i+r)}return this._cachedViewportTotalPaddingBottom}ready(){super.ready(),this.setAttribute("role","listbox"),this.id=`${this.localName}-${le()}`,this.__hostTagName=this.constructor.is.replace("-scroller",""),this.addEventListener("click",e=>e.stopPropagation()),this.__patchWheelOverScrolling()}requestContentUpdate(){this.__virtualizer&&(this.items&&(this.__virtualizer.size=this.items.length),this.opened&&this.__virtualizer.update())}scrollIntoView(e,i=!1){if(!this.__virtualizer||!(this.opened&&e>=0))return;let r=[...this.children].find(h=>!h.hidden&&h.index===e);if(!i&&r){let h=r.getBoundingClientRect(),c=this.getBoundingClientRect();if(h.top>=c.top&&h.bottom+this._viewportTotalPaddingBottom<=c.bottom)return}let n=e;if(!i){let h=this._visibleItemsCount();e>this.__virtualizer.lastVisibleIndex-1?(this.__virtualizer.scrollToIndex(e),n=e-h+1):e>this.__virtualizer.firstVisibleIndex&&(n=this.__virtualizer.firstVisibleIndex)}this.__virtualizer.scrollToIndex(Math.max(0,n)),this.__virtualizer.flush();let o=[...this.children].find(h=>!h.hidden&&h.index===e);if(!o)return;if(i){o.scrollIntoView({block:"center"});return}let a=o.getBoundingClientRect(),l=this.getBoundingClientRect(),d=a.bottom+this._viewportTotalPaddingBottom;d>l.bottom?this.scrollTop+=d-l.bottom:a.top<l.top&&(this.scrollTop-=l.top-a.top)}_isItemSelected(e,i,r){return e instanceof Ne?!1:r&&e!==void 0&&i!==void 0?Ie(r,e)===Ie(r,i):e===i}__initVirtualizer(){this.__virtualizer=new Tt({createElements:this.__createElements.bind(this),updateElement:this._updateElement.bind(this),elementsContainer:this,scrollTarget:this,scrollContainer:this.$.selector,reorderElements:!0,__disableHeightPlaceholder:!0})}__itemsChanged(e){e&&this.__virtualizer&&this.requestContentUpdate()}__loadingChanged(){this.requestContentUpdate()}__openedChanged(e){if(e){this.__virtualizer||this.__initVirtualizer(),this.requestContentUpdate();return}let i=this.__virtualizer&&this.__virtualizer.__adapter;i&&i._scrollPosition>0&&(this.scrollTop=0,i._scrollPosition=0)}__selectedItemChanged(){this.requestContentUpdate()}__itemClassNameGeneratorChanged(e,i){(e||i)&&this.requestContentUpdate()}__focusedIndexChanged(e,i){e!==i&&this.requestContentUpdate(),e>=0&&!this.loading&&this.scrollIntoView(e)}__rendererChanged(e,i){(e||i)&&this.requestContentUpdate()}__createElements(e){return[...Array(e)].map(()=>{let i=document.createElement(`${this.__hostTagName}-item`);return i.addEventListener("click",this.__boundOnItemClick),i.tabIndex="-1",i.style.width="100%",i})}_updateElement(e,i){let r=this.items[i],n=this.focusedIndex,o=this._isItemSelected(r,this.selectedItem,this.itemIdPath);e.setProperties({item:r,index:i,label:this.getItemLabel(r),selected:o,renderer:this.renderer,focused:!this.loading&&n===i}),typeof this.itemClassNameGenerator=="function"?e.className=this.itemClassNameGenerator(r):e.className!==""&&(e.className=""),e.id=`${this.__hostTagName}-item-${i}`,e.setAttribute("role",i!==void 0?"option":!1),e.setAttribute("aria-selected",o.toString()),e.setAttribute("aria-posinset",i+1),e.setAttribute("aria-setsize",this.items.length),this.theme?e.setAttribute("theme",this.theme):e.removeAttribute("theme"),r instanceof Ne&&this.__requestItemByIndex(i)}__onItemClick(e){this.dispatchEvent(new CustomEvent("selection-changed",{detail:{item:e.currentTarget.item}}))}__patchWheelOverScrolling(){this.$.selector.addEventListener("wheel",e=>{let i=this.scrollTop===0,r=this.scrollHeight-this.scrollTop-this.clientHeight<=1;(i&&e.deltaY<0||r&&e.deltaY>0)&&e.preventDefault()})}__requestItemByIndex(e){requestAnimationFrame(()=>{this.dispatchEvent(new CustomEvent("index-requested",{detail:{index:e}}))})}_visibleItemsCount(){return this.__virtualizer.scrollToIndex(this.__virtualizer.firstVisibleIndex),this.__virtualizer.size>0?this.__virtualizer.lastVisibleIndex-this.__virtualizer.firstVisibleIndex+1:0}};var Hi=u`
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
`;var Ri=class extends Vr(g(_)){static get is(){return"vaadin-time-picker-scroller"}static get styles(){return Hi}render(){return v`
      <div id="selector">
        <slot></slot>
      </div>
    `}};m(Ri);var $r=u`
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
`;var Lr=s=>class extends V(_e(q($(s)))){static get properties(){return{opened:{type:Boolean,notify:!0,value:!1,reflectToAttribute:!0,sync:!0,observer:"_openedChanged"},autoOpenDisabled:{type:Boolean,sync:!0},readonly:{type:Boolean,value:!1,reflectToAttribute:!0},_focusedIndex:{type:Number,observer:"_focusedIndexChanged",value:-1,sync:!0},_toggleElement:{type:Object,observer:"_toggleElementChanged"},_dropdownItems:{type:Array,sync:!0},_overlayOpened:{type:Boolean,sync:!0,observer:"_overlayOpenedChanged"}}}constructor(){super(),this._scroller,this._closeOnBlurIsPrevented,this._boundOverlaySelectedItemChanged=this._overlaySelectedItemChanged.bind(this),this._boundOnClearButtonMouseDown=this.__onClearButtonMouseDown.bind(this),this._boundOnClick=this._onClick.bind(this),this._boundOnOverlayTouchAction=this._onOverlayTouchAction.bind(this),this._boundOnTouchend=this._onTouchend.bind(this)}get _tagNamePrefix(){return"vaadin-combo-box"}_inputElementChanged(e){super._inputElementChanged(e),e&&(e.autocomplete="off",e.autocapitalize="off",e.setAttribute("role","combobox"),e.setAttribute("aria-autocomplete","list"),e.setAttribute("aria-expanded",!!this.opened),e.setAttribute("spellcheck","false"),e.setAttribute("autocorrect","off"))}firstUpdated(){super.firstUpdated(),this._initScroller()}ready(){super.ready(),this._initOverlay(),this.addEventListener("click",this._boundOnClick),this.addEventListener("touchend",this._boundOnTouchend),this.clearElement&&this.clearElement.addEventListener("mousedown",this._boundOnClearButtonMouseDown)}disconnectedCallback(){super.disconnectedCallback(),this.close()}open(){!this.disabled&&!this.readonly&&(this.opened=!0)}close(){this.opened=!1}_initOverlay(){let e=this.$.overlay;e.addEventListener("touchend",this._boundOnOverlayTouchAction),e.addEventListener("touchmove",this._boundOnOverlayTouchAction),e.addEventListener("mousedown",i=>i.preventDefault()),e.addEventListener("opened-changed",i=>{this._overlayOpened=i.detail.value}),e.addEventListener("vaadin-overlay-closed",()=>{this._scroller.items=[],this._onOverlayClosed()}),this._overlayElement=e}_initScroller(){let e=document.createElement(`${this._tagNamePrefix}-scroller`);e.owner=this,e.getItemLabel=this._getItemLabel.bind(this),e.addEventListener("selection-changed",this._boundOverlaySelectedItemChanged),this._renderScroller(e),this._scroller=e}_renderScroller(e){e.setAttribute("slot","overlay"),e.setAttribute("tabindex","-1"),this.appendChild(e)}get _hasDropdownItems(){return!!(this._dropdownItems&&this._dropdownItems.length)}_overlayOpenedChanged(e,i){e?this._onOpened():i&&this._hasDropdownItems&&this.close()}_focusedIndexChanged(e,i){i!==void 0&&this._updateActiveDescendant(e)}_isInputFocused(){return this.inputElement&&oe(this.inputElement)}_updateActiveDescendant(e){let i=this.inputElement;if(!i)return;let r=this._getItemElements().find(n=>n.index===e);r?i.setAttribute("aria-activedescendant",r.id):i.removeAttribute("aria-activedescendant")}_openedChanged(e,i){if(i===void 0)return;e?!this._isInputFocused()&&!Me&&this.inputElement&&this.inputElement.focus():(this.autoselect&&(this.__autoselectPending=!0),this._onClosed());let r=this.inputElement;r&&(r.setAttribute("aria-expanded",!!e),e?r.setAttribute("aria-controls",this._scroller.id):r.removeAttribute("aria-controls"))}_onOverlayTouchAction(){this._closeOnBlurIsPrevented=!0,this.inputElement.blur(),this._closeOnBlurIsPrevented=!1}_isClearButton(e){return e.composedPath()[0]===this.clearElement}__onClearButtonMouseDown(e){e.preventDefault(),this.inputElement.focus()}_onClearButtonClick(e){e.preventDefault(),this._onClearAction()}_onToggleButtonClick(e){e.preventDefault(),this.opened?this.close():this.open()}_onHostClick(e){this.autoOpenDisabled||(e.preventDefault(),this.open())}_onClick(e){this.autoselect&&this.inputElement&&this.__autoselectPending&&(this.inputElement.selectionStart!==this.inputElement.selectionEnd||this.inputElement.select()),this.__autoselectPending=!1,this._isClearButton(e)?this._onClearButtonClick(e):e.composedPath().includes(this._toggleElement)?this._onToggleButtonClick(e):this._onHostClick(e)}_onTouchend(e){!this.clearElement||e.composedPath()[0]!==this.clearElement||(e.preventDefault(),this._onClearAction())}_onKeyDown(e){super._onKeyDown(e),e.key==="ArrowDown"?(this._onArrowDown(),e.preventDefault()):e.key==="ArrowUp"&&(this._onArrowUp(),e.preventDefault())}_getItemLabel(e){return e?e.toString():""}_onArrowDown(){if(this.opened){let e=this._dropdownItems;e&&(this._focusedIndex=Math.min(e.length-1,this._focusedIndex+1),this._prefillFocusedItemLabel())}else this.open()}_onArrowUp(){if(this.opened){if(this._focusedIndex>-1)this._focusedIndex=Math.max(0,this._focusedIndex-1);else{let e=this._dropdownItems;e&&(this._focusedIndex=e.length-1)}this._prefillFocusedItemLabel()}else this.open()}_prefillFocusedItemLabel(){if(this._focusedIndex>-1){let e=this._dropdownItems[this._focusedIndex];this._inputElementValue=this._getItemLabel(e),this._markAllSelectionRange()}}_setSelectionRange(e,i){this._isInputFocused()&&this.inputElement.setSelectionRange&&this.inputElement.setSelectionRange(e,i)}_markAllSelectionRange(){this._inputElementValue!==void 0&&this._setSelectionRange(0,this._inputElementValue.length)}_clearSelectionRange(){if(this._inputElementValue!==void 0){let e=this._inputElementValue?this._inputElementValue.length:0;this._setSelectionRange(e,e)}}_closeOrCommit(){this.opened?this.close():this._commitValue()}_onEnter(e){if(!this._hasValidInputValue()){e.preventDefault(),e.stopPropagation();return}this.opened&&(e.preventDefault(),e.stopPropagation()),this._closeOrCommit()}_hasValidInputValue(){return!0}_onEscape(e){this.autoOpenDisabled&&(this.opened||this.value!==this._inputElementValue&&this._inputElementValue.length>0)?(e.stopPropagation(),this._focusedIndex=-1,this._onEscapeCancel()):this.opened?(e.stopPropagation(),this._focusedIndex>-1?(this._focusedIndex=-1,this._revertInputValue()):this._onEscapeCancel()):this.clearButtonVisible&&this.value&&!this.readonly&&(e.stopPropagation(),this._onClearAction())}_onEscapeCancel(){}_toggleElementChanged(e){e&&(e.addEventListener("mousedown",i=>i.preventDefault()),e.addEventListener("click",()=>{Me&&!this._isInputFocused()&&document.activeElement.blur()}))}_onClearAction(){}_onOpened(){}_onClosed(){}_onOverlayClosed(){}_commitValue(){}_revertInputValue(){this._inputElementValue=this.value,this._clearSelectionRange()}_onInput(e){!this.opened&&!this._isClearButton(e)&&!this.autoOpenDisabled&&(this.opened=!0)}_getItemElements(){return Array.from(this._scroller.querySelectorAll(`${this._tagNamePrefix}-item`))}_scrollIntoView(e,i=!1){this._scroller&&this._scroller.scrollIntoView(e,i)}_overlaySelectedItemChanged(e){e.stopPropagation(),!this.hasAttribute("closing")&&(e.detail.item instanceof Ne||this.opened&&(this._focusedIndex=this._dropdownItems.indexOf(e.detail.item),this.close()))}_setFocused(e){super._setFocused(e),e||(this.__autoselectPending=!1),!e&&!this.readonly&&!this._closeOnBlurIsPrevented&&this._handleFocusOut()}_handleFocusOut(){if(T()){this._closeOrCommit();return}this.opened?this._overlayOpened||this.close():this._commitValue()}_shouldRemoveFocus(e){return e.relatedTarget&&e.relatedTarget.localName===`${this._tagNamePrefix}-item`?!1:e.relatedTarget===this._overlayElement?(e.composedPath()[0].focus(),!1):!0}};var Nr=s=>class extends fe(s){static get properties(){return{pattern:{type:String}}}static get delegateAttrs(){return[...super.delegateAttrs,"pattern"]}static get constraints(){return[...super.constraints,"pattern"]}};function xe(s){if(!s)return"";let t=(i=0,r="00")=>(r+i).substr((r+i).length-r.length),e=`${t(s.hours)}:${t(s.minutes)}`;return s.seconds!==void 0&&(e+=`:${t(s.seconds)}`),s.milliseconds!==void 0&&(e+=`.${t(s.milliseconds,"000")}`),e}var _o="(\\d|[0-1]\\d|2[0-3])",Fr="(\\d|[0-5]\\d)",fo=Fr,mo="(\\d{1,3})",vo=new RegExp(`^${_o}(?::${Fr}(?::${fo}(?:\\.${mo})?)?)?$`,"u");function P(s){let t=vo.exec(s);if(t){if(t[4])for(;t[4].length<3;)t[4]+="0";return{hours:t[1],minutes:t[2],seconds:t[3],milliseconds:t[4]}}}function go(s){let t=s==null?60:parseFloat(s);if(t%3600===0)return 1;if(t%60===0||!t)return 2;if(t%1===0)return 3;if(t<1)return 4}function L(s,t){if(!s)return s;let e=go(t);return{...s,hours:parseInt(s.hours),minutes:parseInt(s.minutes||0),seconds:e<3?void 0:parseInt(s.seconds||0),milliseconds:e<4?void 0:parseInt(s.milliseconds||0)}}var At=Object.freeze({formatTime:xe,parseTime:P}),Br="00:00:00.000",Hr="23:59:59.999",Rr=s=>class extends ye(Nr(Lr(St(s)))){static get properties(){return{value:{type:String,notify:!0,value:"",sync:!0},min:{type:String,value:"",sync:!0},max:{type:String,value:"",sync:!0},step:{type:Number,sync:!0},_comboBoxValue:{type:String,sync:!0,observer:"__comboBoxValueChanged"},_inputContainer:{type:Object}}}static get observers(){return["_openedOrItemsChanged(opened, _dropdownItems)","_updateScroller(opened, _dropdownItems, _focusedIndex, _theme, _comboBoxValue)","__updateAriaAttributes(_dropdownItems, opened, inputElement)","__updateDropdownItems(__effectiveI18n, min, max, step)"]}static get defaultI18n(){return At}static get constraints(){return[...super.constraints,"min","max"]}get _tagNamePrefix(){return"vaadin-time-picker"}get clearElement(){return this.$.clearButton}get i18n(){return super.i18n}set i18n(e){super.i18n=e}get __unparsableValue(){return this._inputElementValue&&!this.__effectiveI18n.parseTime(this._inputElementValue)?this._inputElementValue:""}ready(){super.ready(),this.addController(new me(this,e=>{this._setInputElement(e),this._setFocusElement(e),this.stateTarget=e,this.ariaTarget=e},{uniqueIdPrefix:"search-input"})),this.addController(new ve(this.inputElement,this._labelController)),this._inputContainer=this.shadowRoot.querySelector('[part~="input-field"]'),this._toggleElement=this.$.toggleButton,this._tooltipController=new O(this),this._tooltipController.setShouldShow(e=>!e.opened),this._tooltipController.setPosition("top"),this._tooltipController.setAriaTarget(this.inputElement),this.addController(this._tooltipController)}checkValidity(){return!!(this.inputElement.checkValidity()&&(!this.value||this._timeAllowed(P(this.value)))&&(!this._comboBoxValue||this.__effectiveI18n.parseTime(this._comboBoxValue)))}_getItemLabel(e){return e?e.label:""}_updateScroller(e,i,r,n,o){e&&(this._scroller.style.maxHeight=getComputedStyle(this).getPropertyValue(`--${this._tagNamePrefix}-overlay-max-height`)||"65vh"),this._scroller.setProperties({items:e?i:[],opened:e,focusedIndex:r,theme:n,selectedItem:i?.find(a=>a.value===o)})}_openedOrItemsChanged(e,i){this._overlayOpened=e&&!!i?.length}_onClosed(){this._commitValue()}_onEscapeCancel(){this._inputElementValue=this._comboBoxValue,this._closeOrCommit()}_onClearAction(){this._comboBoxValue="",this._inputElementValue="",this.__commitValueChange()}_commitValue(){if(this._focusedIndex>-1){let e=this._dropdownItems[this._focusedIndex],i=this._getItemLabel(e);this._inputElementValue=i,this._comboBoxValue=i,this._focusedIndex=-1}else this._inputElementValue===""||this._inputElementValue===void 0?this._comboBoxValue="":this._comboBoxValue=this._inputElementValue;this.__commitValueChange(),this._clearSelectionRange()}_closeOrCommit(){this.opened?this.close():this._commitValue()}_revertInputValue(){this._inputElementValue=this._comboBoxValue,this._clearSelectionRange()}_setFocused(e){super._setFocused(e),!e&&!this._closeOnBlurIsPrevented&&document.hasFocus()&&this._requestValidation()}__validDayDivisor(e){return!e||24*3600%e===0||e<1&&e%1*1e3%1===0}_onKeyDown(e){if(super._onKeyDown(e),this.readonly||this.disabled||this._dropdownItems.length)return;let i=this.__validDayDivisor(this.step)&&this.step||60;e.keyCode===40?this.__onArrowPressWithStep(-i):e.keyCode===38&&this.__onArrowPressWithStep(i)}__onArrowPressWithStep(e){let i=this.__addStep(this.__getMsec(this.__memoValue),e,!0);this.__memoValue=i,this.__useMemo=!0,this._comboBoxValue=this.__effectiveI18n.formatTime(i),this.__useMemo=!1,this.__commitValueChange()}__commitValueChange(){let e=this.__unparsableValue;this.__committedValue!==this.value?(this._requestValidation(),this.dispatchEvent(new CustomEvent("change",{bubbles:!0}))):this.__committedUnparsableValue!==e&&(this._requestValidation(),this.dispatchEvent(new CustomEvent("unparsable-change"))),this.__committedValue=this.value,this.__committedUnparsableValue=e}__getMsec(e){let i=(e?.hours||0)*60*60*1e3;return i+=(e?.minutes||0)*60*1e3,i+=(e?.seconds||0)*1e3,i+=parseInt(e?.milliseconds)||0,i}__getSec(e){let i=(e?.hours||0)*60*60;return i+=(e?.minutes||0)*60,i+=e?.seconds||0,i+=(e?.milliseconds||0)/1e3,i}__addStep(e,i,r){e===0&&i<0&&(e=1440*60*1e3);let n=i*1e3,o=e%n;n<0&&o&&r?e-=o:n>0&&o&&r?e-=o-n:e+=n;let a=Math.floor(e/1e3/60/60);e-=a*1e3*60*60;let l=Math.floor(e/1e3/60);e-=l*1e3*60;let d=Math.floor(e/1e3);return e-=d*1e3,{hours:a<24?a:0,minutes:l,seconds:d,milliseconds:e}}__updateDropdownItems(e,i,r,n){let o=L(P(i||Br),n),a=this.__getSec(o),l=L(P(r||Hr),n),d=this.__getSec(l);this._dropdownItems=this.__generateDropdownList(a,d,n);let h=L(P(this.value),n);n!==this.__oldStep&&(this.__oldStep=n,this.__updateValue(h)),this.value&&(this._comboBoxValue=e.formatTime(h))}__updateAriaAttributes(e,i,r){e===void 0||r===void 0||(e.length===0?(r.removeAttribute("role"),r.removeAttribute("aria-expanded")):(r.setAttribute("role","combobox"),r.setAttribute("aria-expanded",!!i)))}__generateDropdownList(e,i,r){if(r<900||!this.__validDayDivisor(r))return[];let n=[];r||(r=3600);let o=-r+e;for(;o+r>=e&&o+r<=i;){let a=L(this.__addStep(o*1e3,r),r);o+=r;let l=this.__effectiveI18n.formatTime(a);n.push({label:l,value:l})}return n}_valueChanged(e,i){let r=this.__memoValue=P(e),n=xe(r)||"";this.__keepCommittedValue||(this.__committedValue=e,this.__committedUnparsableValue=""),e!==""&&e!==null&&!r?this.value=i??"":e!==n?this.value=n:this.__keepInvalidInput?delete this.__keepInvalidInput:this.__updateInputValue(r),this._toggleHasValue(this._hasValue)}__comboBoxValueChanged(e,i){if(e===""&&i===void 0)return;let r=this.__useMemo?this.__memoValue:this.__effectiveI18n.parseTime(e),n=L(r,this.step),o=this.__effectiveI18n.formatTime(n)||"";n?e!==o?this._comboBoxValue=o:(this.__keepCommittedValue=!0,this.__updateValue(n),this.__keepCommittedValue=!1):(this.value!==""&&e!==""&&(this.__keepInvalidInput=!0),this.__keepCommittedValue=!0,this.value="",this.__keepCommittedValue=!1)}__updateValue(e){let i=xe(L(e,this.step))||"";this.value=i,this.__updateInputValue(e)}__updateInputValue(e){let i=this.__effectiveI18n.formatTime(L(e,this.step))||"";this._inputElementValue=i,this._comboBoxValue=i}_timeAllowed(e){let i=P(this.min||Br),r=P(this.max||Hr);return(!i||this.__getMsec(e)>=this.__getMsec(i))&&(!r||this.__getMsec(e)<=this.__getMsec(r))}_onClearButtonClick(e){e.stopPropagation(),super._onClearButtonClick(e),this.opened&&this._scroller.requestContentUpdate()}_onHostClick(e){let i=e.composedPath();(i.includes(this._labelNode)||i.includes(this._inputContainer))&&super._onHostClick(e)}_onChange(e){e.stopPropagation()}};var zi=class extends Rr(y(U(g(b(_))))){static get is(){return"vaadin-time-picker"}static get styles(){return[ge,$r]}render(){return v`
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
          theme="${pe(this._theme)}"
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
        theme="${pe(this._theme)}"
        .positionTarget="${this._inputContainer}"
        no-vertical-overlap
        exportparts="overlay, content"
      >
        <slot name="overlay"></slot>
      </vaadin-time-picker-overlay>
    `}};m(zi);var zr=u`
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
`;var bo=Object.keys(It),yo=Object.keys(At),xo={...It,...At},Mt=class extends D{constructor(t,e){super(t,`${e}-picker`,`vaadin-${e}-picker`,{initializer:(i,r)=>{let n=`__${e}Picker`;r[n]=i}})}},Ur=s=>class extends ye(wt($(q(s)))){static get properties(){return{name:{type:String},value:{type:String,notify:!0,value:"",observer:"__valueChanged",sync:!0},min:{type:String,observer:"__minChanged",sync:!0},max:{type:String,observer:"__maxChanged",sync:!0},__minDateTime:{type:Date,value:"",sync:!0},__maxDateTime:{type:Date,value:"",sync:!0},datePlaceholder:{type:String,sync:!0},timePlaceholder:{type:String,sync:!0},step:{type:Number,sync:!0},initialPosition:{type:String,sync:!0},showWeekNumbers:{type:Boolean,value:!1,sync:!0},autoOpenDisabled:{type:Boolean,sync:!0},readonly:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},autofocus:{type:Boolean},__selectedDateTime:{type:Date,sync:!0},__datePicker:{type:Object,sync:!0,observer:"__datePickerChanged"},__timePicker:{type:Object,sync:!0,observer:"__timePickerChanged"}}}static get observers(){return["__selectedDateTimeChanged(__selectedDateTime)","__datePlaceholderChanged(datePlaceholder, __datePicker)","__timePlaceholderChanged(timePlaceholder, __timePicker)","__stepChanged(step, __timePicker)","__initialPositionChanged(initialPosition, __datePicker)","__showWeekNumbersChanged(showWeekNumbers, __datePicker)","__requiredChanged(required, __datePicker, __timePicker)","__invalidChanged(invalid, __datePicker, __timePicker)","__disabledChanged(disabled, __datePicker, __timePicker)","__readonlyChanged(readonly, __datePicker, __timePicker)","__i18nChanged(__effectiveI18n, __datePicker, __timePicker)","__autoOpenDisabledChanged(autoOpenDisabled, __datePicker, __timePicker)","__themeChanged(_theme, __datePicker, __timePicker)","__pickersChanged(__datePicker, __timePicker)","__labelOrAccessibleNameChanged(label, accessibleName, __effectiveI18n, __datePicker, __timePicker)"]}static get defaultI18n(){return xo}constructor(){super(),this.__defaultDateMinMaxValue=void 0,this.__defaultTimeMinValue="00:00:00.000",this.__defaultTimeMaxValue="23:59:59.999",this.__onGlobalClick=this.__onGlobalClick.bind(this),this.__changeEventHandler=this.__changeEventHandler.bind(this),this.__valueChangedEventHandler=this.__valueChangedEventHandler.bind(this),this.__openedChangedEventHandler=this.__openedChangedEventHandler.bind(this)}get i18n(){return super.i18n}set i18n(e){super.i18n=e}get __pickers(){return[this.__datePicker,this.__timePicker]}get __filledPickers(){return this.__pickers.filter(e=>e.value||e.__unparsableValue)}get __formattedValue(){let e=this.__pickers.map(i=>i.value);return e.every(Boolean)?e.join("T"):""}get __unparsableValue(){return this.__filledPickers.length>0&&!this.__pickers.every(e=>e.value)?this.__pickers.map(e=>e.value||e.__unparsableValue).join("T"):""}ready(){super.ready(),this._datePickerController=new Mt(this,"date"),this.addController(this._datePickerController),this._timePickerController=new Mt(this,"time"),this.addController(this._timePickerController),this.autofocus&&!this.disabled&&window.requestAnimationFrame(()=>this.focus()),this.setAttribute("role","group"),this._tooltipController=new O(this),this.addController(this._tooltipController),this._tooltipController.setPosition("top"),this._tooltipController.setShouldShow(e=>e.__datePicker&&!e.__datePicker.opened&&e.__timePicker&&!e.__timePicker.opened),this.ariaTarget=this}connectedCallback(){super.connectedCallback(),document.addEventListener("click",this.__onGlobalClick,!0)}disconnectedCallback(){super.disconnectedCallback(),document.removeEventListener("click",this.__onGlobalClick,!0)}focus(e){this.__datePicker&&this.__datePicker.focus(e)}__onGlobalClick(e){if(!(this.__datePicker.opened||this.__timePicker.opened))return;e.composedPath().every(n=>![this.__datePicker,this.__datePicker.$.overlay,this.__timePicker,this.__timePicker.$.overlay].includes(n))&&(this.__outsideClickInProgress=!0,setTimeout(()=>{this.__outsideClickInProgress=!1}))}_setFocused(e){super._setFocused(e),!e&&document.hasFocus()&&this.__commitPendingValueChange()}_shouldRemoveFocus(e){let i=e.relatedTarget;return!(this.__datePicker.opened||this.__timePicker.opened||this.__datePicker.contains(i)||this.__timePicker.contains(i))}__syncI18n(e,i,r){let n={};r.forEach(o=>{i?.hasOwnProperty(o)&&(n[o]=i[o])}),e.i18n=n}__changeEventHandler(e){e.stopPropagation();let i=this.invalid,r=this.__filledPickers;r.length===1&&r[0].checkValidity()&&!i||this.__hasPendingValueChange&&this.__commitPendingValueChange()}__openedChangedEventHandler(){let e=this.__datePicker.opened||this.__timePicker.opened;this.style.pointerEvents=e?"auto":"",!e&&this.__outsideClickInProgress&&this.__commitPendingValueChange()}__addInputListeners(e){e.addEventListener("change",this.__changeEventHandler),e.addEventListener("unparsable-change",this.__changeEventHandler),e.addEventListener("value-changed",this.__valueChangedEventHandler),e.addEventListener("opened-changed",this.__openedChangedEventHandler)}__removeInputListeners(e){e.removeEventListener("change",this.__changeEventHandler),e.removeEventListener("unparsable-change",this.__changeEventHandler),e.removeEventListener("value-changed",this.__valueChangedEventHandler),e.removeEventListener("opened-changed",this.__openedChangedEventHandler)}__isDefaultPicker(e,i){let r=this[`_${i}PickerController`];return r&&e===r.defaultNode}__datePickerChanged(e,i){e&&(i&&(this.__removeInputListeners(i),i.remove()),this.__addInputListeners(e),this.__isDefaultPicker(e,"date")||(this.datePlaceholder=e.placeholder,this.initialPosition=e.initialPosition,this.showWeekNumbers=e.showWeekNumbers),e.min=this.__formatDateISO(this.__minDateTime,this.__defaultDateMinMaxValue),e.max=this.__formatDateISO(this.__maxDateTime,this.__defaultDateMinMaxValue),e.manualValidation=!0)}__timePickerChanged(e,i){e&&(i&&(this.__removeInputListeners(i),i.remove()),this.__addInputListeners(e),this.__isDefaultPicker(e,"time")||(this.timePlaceholder=e.placeholder,this.step=e.step),this.__updateTimePickerMinMax(),e.manualValidation=!0)}__updateTimePickerMinMax(){if(this.__timePicker&&this.__datePicker){let e=this.__parseDate(this.__datePicker.value),i=S(this.__minDateTime,this.__maxDateTime,$e);this.__minDateTime&&S(e,this.__minDateTime,$e)||i?this.__timePicker.min=this.__dateToIsoTimeString(this.__minDateTime):this.__timePicker.min=this.__defaultTimeMinValue,this.__maxDateTime&&S(e,this.__maxDateTime,$e)||i?this.__timePicker.max=this.__dateToIsoTimeString(this.__maxDateTime):this.__timePicker.max=this.__defaultTimeMaxValue}}__i18nChanged(e,i,r){i&&this.__isDefaultPicker(i,"date")&&this.__syncI18n(i,e,bo),r&&this.__isDefaultPicker(r,"time")&&this.__syncI18n(r,e,yo)}__labelOrAccessibleNameChanged(e,i,r,n,o){let a=i||e||"";n&&(n.accessibleName=`${a} ${r.dateLabel||""}`.trim()),o&&(o.accessibleName=`${a} ${r.timeLabel||""}`.trim())}__datePlaceholderChanged(e,i){i&&(i.placeholder=e)}__timePlaceholderChanged(e,i){i&&(i.placeholder=e)}__stepChanged(e,i){i&&i.step!==e&&(i.step=e)}__initialPositionChanged(e,i){i&&(i.initialPosition=e)}__showWeekNumbersChanged(e,i){i&&(i.showWeekNumbers=e)}__invalidChanged(e,i,r){i&&(i.invalid=e),r&&(r.invalid=e)}__requiredChanged(e,i,r){i&&(i.required=e),r&&(r.required=e),this.__oldRequired&&!e&&this._requestValidation(),this.__oldRequired=e}__disabledChanged(e,i,r){i&&(i.disabled=e),r&&(r.disabled=e)}__readonlyChanged(e,i,r){i&&(i.readonly=e),r&&(r.readonly=e)}__parseDate(e){return Qs(e)}__formatDateISO(e,i){return e?tr(e):i}__parseDateTime(e){let[i,r]=e.split("T");if(!(i&&r))return;let n=this.__parseDate(i);if(!n)return;let o=P(r);if(o)return n.setUTCHours(parseInt(o.hours)),n.setUTCMinutes(parseInt(o.minutes||0)),n.setUTCSeconds(parseInt(o.seconds||0)),n.setUTCMilliseconds(parseInt(o.milliseconds||0)),n}__formatDateTime(e){if(!e)return"";let i=this.__formatDateISO(e,""),r=this.__dateToIsoTimeString(e);return`${i}T${r}`}__dateToIsoTimeString(e){return xe(L({hours:e.getUTCHours(),minutes:e.getUTCMinutes(),seconds:e.getUTCSeconds(),milliseconds:e.getUTCMilliseconds()},this.step))}checkValidity(){let e=this.__pickers.some(n=>!n.checkValidity()),i=this.__filledPickers.length===1,r=this.required&&this.__pickers.some(n=>!n.value);return!e&&!r&&!i}__commitPendingValueChange(){this._requestValidation(),this.__committedValue!==this.value?this.dispatchEvent(new CustomEvent("change",{bubbles:!0})):this.__committedUnparsableValue!==this.__unparsableValue&&this.dispatchEvent(new CustomEvent("unparsable-change")),this.__committedValue=this.value,this.__committedUnparsableValue=this.__unparsableValue}get __hasPendingValueChange(){return this.__committedValue!==this.value||this.__committedUnparsableValue!==this.__unparsableValue}__dateTimeEquals(e,i){return S(e,i,$e)?e.getUTCHours()===i.getUTCHours()&&e.getUTCMinutes()===i.getUTCMinutes()&&e.getUTCSeconds()===i.getUTCSeconds()&&e.getUTCMilliseconds()===i.getUTCMilliseconds():!1}__handleDateTimeChange(e,i,r,n){if(!r){this[e]="",this[i]="";return}let o=this.__parseDateTime(r);if(!o){this[e]=n;return}this.__dateTimeEquals(this[i],o)||(this[i]=o)}__valueChanged(e,i){this.__handleDateTimeChange("value","__selectedDateTime",e,i),this.__keepCommittedValue||(this.__committedValue=e,this.__committedUnparsableValue=""),this.toggleAttribute("has-value",!!e),this.__updateTimePickerMinMax()}__dispatchChange(){this.dispatchEvent(new CustomEvent("change",{bubbles:!0}))}__minChanged(e,i){this.__handleDateTimeChange("min","__minDateTime",e,i),this.__datePicker&&(this.__datePicker.min=this.__formatDateISO(this.__minDateTime,this.__defaultDateMinMaxValue)),this.__updateTimePickerMinMax(),this.__datePicker&&this.__timePicker&&this.value&&this._requestValidation()}__maxChanged(e,i){this.__handleDateTimeChange("max","__maxDateTime",e,i),this.__datePicker&&(this.__datePicker.max=this.__formatDateISO(this.__maxDateTime,this.__defaultDateMinMaxValue)),this.__updateTimePickerMinMax(),this.__datePicker&&this.__timePicker&&this.value&&this._requestValidation()}__selectedDateTimeChanged(e){let i=this.__formatDateTime(e);if(this.value!==i&&(this.value=i),!!(this.__datePicker&&this.__datePicker.$)&&!this.__ignoreInputValueChange){this.__ignoreInputValueChange=!0;let[n,o]=this.value.split("T");this.__datePicker.value=n||"",this.__timePicker.value=o||"",this.__ignoreInputValueChange=!1}}__valueChangedEventHandler(){this.__ignoreInputValueChange||(this.__ignoreInputValueChange=!0,this.__keepCommittedValue=!0,this.__updateTimePickerMinMax(),this.value=this.__formattedValue,this.__keepCommittedValue=!1,this.__ignoreInputValueChange=!1)}__autoOpenDisabledChanged(e,i,r){i&&(i.autoOpenDisabled=e),r&&(r.autoOpenDisabled=e)}__themeChanged(e,i,r){!i||!r||[i,r].forEach(n=>{e?n.setAttribute("theme",e):n.removeAttribute("theme")})}__pickersChanged(e,i){!e||!i||this.__isDefaultPicker(e,"date")===this.__isDefaultPicker(i,"time")&&(e.value?this.__valueChangedEventHandler():this.value&&(this.__selectedDateTimeChanged(this.__selectedDateTime),(this.min&&this.__minDateTime||this.max&&this.__maxDateTime)&&this._requestValidation()))}};var Ui=class extends Ur(y(U(g(b(_))))){static get is(){return"vaadin-date-time-picker"}static get styles(){return[ge,zr]}render(){return v`
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
    `}};m(Ui);
