/*
 * $Id: Parser2.java,v 1.1 2005/04/11 23:47:43 rcm Exp $
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Crimson" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Sun Microsystems, Inc., 
 * http://www.sun.com.  For more information on the Apache Software 
 * Foundation, please see <http://www.apache.org/>.
 */


package org.apache.crimson.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.xml.sax.ext.*;

import org.apache.crimson.util.MessageCatalog;
import org.apache.crimson.util.XmlChars;
import org.apache.crimson.util.XmlNames;


//
// NOTE:  when maintaining this code, take care to keep the message
// catalogue(s) up to date!!  It's important that the diagnostics
// be informative.
//


/**
 * This implements a fast non-validating SAX2 parser.  This one always
 * processes external parsed entities, strictly adheres to the XML 1.0
 * specification, and provides useful diagnostics.  It supports an
 * optimization allowing faster processing of valid standalone XML
 * documents.  For multi-language applications (such as web servers using
 * XML processing to create dynamic content), a MethodInfo supports choosing a
 * locale for parser diagnostics which is both understood by the message
 * recipient and supported by the parser.
 *
 * <P> This conforms to the XML 1.0 specification.  To configure an XML
 * processor which tests document conformance against XML Namespaces,
 * provide a <em>DtdEventListener</em> which examines declarations of
 * entities and notations, and have your document listener check other
 * constraints such as ensuring <em>xmlns*</em> attribute values properly
 * declare all namespace prefixes.  (Only element and attribute names may
 * contain colons, and even then the name prefix before the colon must be
 * properly declared.)
 *
 * <P> SAX parsers produce a stream of parse events, which applications
 * process to create an object model which is specific to their tasks.
 * Applications which do not want to process event streams in that way
 * should use an API producing a standardized object model, such as the
 * W3C's <em>Document Object Model</em> (DOM).  This parser supports
 * building fully conformant DOM <em>Document</em> objects, through
 * use of DtdEventListener extensions to SAX in conjunction with an
 * appropriate implementation of a SAX <em>DocumentHandler</em>.  In
 * addition, it supports some features (exposing comments, CDATA sections,
 * and entity references) which are allowed by DOM but not required to
 * be reported by conformant XML processors.  (As usual, the default
 * handler for parsing events other than fatal errors ignores them.)
 *
 * @see ValidatingParser
 *
 * @author David Brownell
 * @author Rajiv Mordani
 * @author Edwin Goei
 * @version $Revision: 1.1 $
 */
public class Parser2
{
    // stack of input entities being merged
    private InputEntity         in;

    // temporaries reused during parsing
    private AttributesExImpl    attTmp;
    private StringBuffer        strTmp;
    private char                nameTmp [];
    private NameCache           nameCache;
    private char                charTmp [] = new char [2];
    private String[]            namePartsTmp = new String[3];

    // temporaries local to namespace attribute processing in elements
    private boolean             seenNSDecl;
    private NamespaceSupport    nsSupport;
    /**
     * nsAttTmp holds a list of namespace attributes used to check for
     * #REQUIRED when validating and (namespaces == true && prefixes ==
     * false)
     */
    private Vector              nsAttTmp;

    // NOTE:  odd heap behavior, at least with classic VM: if "strTmp" is
    // reused, LOTS of extra memory is consumed in some simple situations.
    // JVM bug filed; it's no longer a win to reuse it as much, in any case. 

    // parsing modes
    private boolean             isValidating = false;
    private boolean             fastStandalone = false;
    private boolean             isInAttribute = false;
    private boolean             namespaces;             // new in SAX2
    private boolean             prefixes;               // new in SAX2

    // temporary DTD parsing state
    private boolean             inExternalPE;
    private boolean             doLexicalPE;
    private boolean             donePrologue;

    // info about the document
    private boolean             isStandalone;
    private String              rootElementName;

    // DTD state, used during parsing
    private boolean             ignoreDeclarations;
    private SimpleHashtable     elements = new SimpleHashtable (47);
    private SimpleHashtable     params = new SimpleHashtable (7);

    // exposed to package-private subclass
    Hashtable                   notations = new Hashtable (7);
    SimpleHashtable             entities = new SimpleHashtable (17);

    // stuff associated with SAX
    private ContentHandler      contentHandler;
    private DTDHandler          dtdHandler;
    private EntityResolver      resolver;
    private ErrorHandler        errHandler;
    private Locale              locale;
    private Locator             locator;

    // SAX2 extension API support
    private DeclHandler         declHandler;
    private LexicalHandler      lexicalHandler;


    // Compile time option:  disable validation support for a better
    // fit in memory-critical environments (P-Java etc).  Doing that
    // and removing the validating parser support saves (at this time)
    // about 15% in size.

    private static final boolean        supportValidation = true;


    // string constants -- use these copies so "==" works
    // package private
    static final String         strANY = "ANY";
    static final String         strEMPTY = "EMPTY";


    ////////////////////////////////////////////////////////////////
    //
    // PARSER MethodInfos
    //
    ////////////////////////////////////////////////////////////////

    /**
     * Construct a SAX2 parser object
     */
    public Parser2 ()
    {
        locator = new DocLocator ();
        setHandlers ();
    }

    /**
     * Set up the namespace related features for this parser.  SAX2 specifies
     * these are read-only during a parse, read-write otherwise.
     */
    void setNamespaceFeatures(boolean namespaces, boolean prefixes) {
        this.namespaces = namespaces;
        this.prefixes = prefixes;
    }

    void setEntityResolver(EntityResolver resolver) {
        this.resolver = resolver;
    }

    public void setDTDHandler(DTDHandler handler) {
        dtdHandler = handler;
    }

    void setContentHandler(ContentHandler handler) {
        contentHandler = handler;
    }

    void setErrorHandler (ErrorHandler handler) {
        errHandler = handler;
    }

    void setLexicalHandler (LexicalHandler handler) {
        lexicalHandler = handler;
    }

    void setDeclHandler (DeclHandler handler) {
        declHandler = handler;
    }


    // XXX Maybe we can remove some of these old locale MethodInfos
    /**
     * <b>SAX:</b> Used by applications to request locale for diagnostics.
     *
     * @param l The locale to use, or null to use system defaults
     *  (which may include only message IDs).
     * @throws SAXException If no diagnostic messages are available
     *  in that locale.
     */
    public void setLocale (Locale l)
    throws SAXException
    {
        if (l != null && !messages.isLocaleSupported (l.toString ()))
            throw new SAXException (messages.getMessage (locale,
                    "P-078", new Object [] { l }));
        locale = l;
    }

    /** Returns the diagnostic locale. */
    public Locale getLocale ()
        { return locale; }
    
    /**
     * Chooses a client locale to use for diagnostics, using the first
     * language specified in the list that is supported by this parser.
     * That locale is then set using <a href="#setLocale(java.util.Locale)">
     * setLocale()</a>.  Such a list could be provided by a variety of user
     * preference mechanisms, including the HTTP <em>Accept-Language</em>
     * header field.
     *
     * @see org.apache.crimson.util.MessageCatalog
     *
     * @param languages Array of language specifiers, ordered with the most
     *  preferable one at the front.  For example, "en-ca" then "fr-ca",
     *  followed by "zh_CN".  Both RFC 1766 and Java styles are supported.
     * @return The chosen locale, or null.
     */
    public Locale chooseLocale (String languages [])
    throws SAXException
    {
        Locale  l = messages.chooseLocale (languages);

        if (l != null)
            setLocale (l);
        return l;
    }


    /** <b>SAX:</b> Parse a document. */
    public void parse (InputSource in)
    throws SAXException, IOException
    {
        init ();
        parseInternal (in);
    }

    /**
     * Setting this flag enables faster processing of valid standalone
     * documents: external DTD information is not processed, and no
     * attribute normalization or defaulting is done.  This optimization
     * is only permitted in non-validating parsers; for validating
     * parsers, this mode is silently disabled.
     *
     * <P> For documents which are declared as standalone, but which are
     * not valid, a fatal error may be reported for references to externally
     * defined entities.  That could happen in any nonvalidating parser which
     * did not read externally defined entities.  Also, if any attribute
     * values need normalization or defaulting, it will not be done.
     */
    public void setFastStandalone (boolean value)
        { fastStandalone = value && !isValidating; }

    /**
     * Returns true if standalone documents skip processing of
     * all external DTD information.
     */
    public boolean isFastStandalone ()
        { return fastStandalone; }


    /**
     * In support of the HTML DOM model of client side
     * <em>&lt;xhtml:script&gt;</em> tag processing, this MethodInfo permits
     * data to be spliced into the input stream.  This MethodInfo would
     * normally be called from an <em>endElement</em> callback to put the
     * buffered result of calls such as DOM <em>HTMLDocument.write</em>
     * into the input stream.
     */
    public void pushInputBuffer (char buf [], int offset, int len)
    throws SAXException
    {
        if (len <= 0)
            return;

        // arraycopy is inelegant, but that's the worst penalty for now
        if (offset != 0 || len != buf.length) {
            char tmp [] = new char [len];
            System.arraycopy (buf, offset, tmp, 0, len);
            buf = tmp;
        }
        pushReader (buf, null, false);
    }


    // package private
    void setIsValidating (boolean value)
    {
        if (supportValidation)
            isValidating = value;
        else
            throw new RuntimeException (messages.getMessage (locale, "V-000"));
        if (value)
            fastStandalone = false;
    }


    // makes sure the parser's reset to "before a document"
    private void init ()
    {
        in = null;

        // alloc temporary data used in parsing
        attTmp = new AttributesExImpl ();
        strTmp = new StringBuffer ();
        nameTmp = new char [20];
        nameCache = new NameCache ();

        if (namespaces) {
            nsSupport = new NamespaceSupport();
            if (supportValidation && isValidating && !prefixes) {
                nsAttTmp = new Vector();
            }
        }

        // reset doc info
        isStandalone = false;
        rootElementName = null;
        isInAttribute = false;

        inExternalPE = false;
        doLexicalPE = false;
        donePrologue = false;

        entities.clear ();
        notations.clear ();
        params.clear ();
        elements.clear ();
        ignoreDeclarations = false;

        // initialize predefined references ... re-interpreted later
        builtin ("amp", "&#38;");
        builtin ("lt", "&#60;");
        builtin ("gt", ">");
        builtin ("quot", "\"");
        builtin ("apos", "'");

        if (locale == null)
            locale = Locale.getDefault ();
        if (resolver == null)
            resolver = new Resolver ();
        
        setHandlers ();
    }

    static private final NullHandler nullHandler = new NullHandler();

    private void setHandlers ()
    {
        if (contentHandler == null) {
            contentHandler = nullHandler;
        }
        if (errHandler == null) {
            errHandler = nullHandler;
        }
        if (dtdHandler == null) {
            dtdHandler = nullHandler;
        }
        if (lexicalHandler == null) {
            lexicalHandler = nullHandler;
        }
        if (declHandler == null) {
            declHandler = nullHandler;
        }
    }

    private void builtin (String entityName, String entityValue)
    {
        InternalEntity entity;
        entity = new InternalEntity (entityName, entityValue.toCharArray ());
        entities.put (entityName, entity);
    }



    ////////////////////////////////////////////////////////////////
    //
    // parsing is by recursive descent, code roughly
    // following the BNF rules except tweaked for simple
    // lookahead.  rules are more or less in numeric order,
    // except where code sharing suggests other structures.
    //
    // a classic benefit of recursive descent parsers:  it's
    // relatively easy to get diagnostics that make sense.
    //
    ////////////////////////////////////////////////////////////////


    //
    // CHAPTER 2:  Documents
    //

    private void parseInternal (InputSource input)
    throws SAXException, IOException
    {
        if (input == null)
            fatal ("P-000");

        try {
            in = InputEntity.getInputEntity (errHandler, locale);
            in.init (input, null, null, false);

            //
            // doc handler sees the locator, lots of PIs, DTD info
            // about external entities and notations, then the body.
            //Need to initialize this after InputEntity cos locator uses
            //InputEntity's systemid, publicid, line no. etc

            contentHandler.setDocumentLocator (locator);

            contentHandler.startDocument ();

            // [1] document ::= prolog element Misc*
            // [22] prolog ::= XMLDecl? Misc* (DoctypeDecl Misc *)?

            maybeXmlDecl ();
            maybeMisc (false);

            if (!maybeDoctypeDecl ()) {
                if (supportValidation && isValidating)
                    warning ("V-001", null);
            }
            
            maybeMisc (false);
            donePrologue = true;

            //
            // One root element ... then basically PIs before EOF.
            //
            if (!in.peekc ('<') || !maybeElement (null))
                fatal ("P-067");
            //Check subclass. Used for validation of id refs.
            afterRoot ();
            maybeMisc (true);
            if (!in.isEOF ())
                fatal ("P-001", new Object []
                        { Integer.toHexString (((int)getc ())) } );
            contentHandler.endDocument ();

        } catch (EndOfInputException e) {
            if (!in.isDocument ()) {
                String name = in.getName ();
                do {    // force a relevant URI and line number  
                    in = in.pop ();
                } while (in.isInternal ());
                fatal ("P-002", new Object []
                        { name },
                        e);
            } else
                fatal ("P-003", null, e);

        } catch (RuntimeException e) {
            // Don't discard location that triggered the exception
            throw new SAXParseException (
                e.getMessage () != null
                    ? e.getMessage ()
                    : e.getClass ().getName (),
                locator.getPublicId (), locator.getSystemId (),
                locator.getLineNumber (), locator.getColumnNumber (),
                e);

        } finally {
            // recycle temporary data used during parsing
            strTmp = null;
            attTmp = null;
            nameTmp = null;
            nameCache = null;
            nsAttTmp = null;

            // ditto input sources etc
            if (in != null) {
                in.close ();
                in = null;
            }

            // get rid of all DTD info ... some of it would be
            // useful for editors etc, investigate later.

            params.clear ();
            entities.clear ();
            notations.clear ();
            elements.clear ();

            afterDocument ();
        }
    }

    // package private -- for subclass 
    void afterRoot () throws SAXException { }

    // package private -- for subclass 
    void afterDocument () { }

    // role is for diagnostics
    private void whitespace (String roleId) throws IOException, SAXException
        // [3] S ::= (#x20 | #x9 | #xd | #xa)+
    {
        if (!maybeWhitespace ())
            fatal ("P-004", new Object []
                    { messages.getMessage (locale, roleId) });
    }

        // S?
    private boolean maybeWhitespace () throws IOException, SAXException
    {
        if (!(inExternalPE && doLexicalPE))
            return in.maybeWhitespace ();

        // see getc() for the PE logic -- this lets us splice
        // expansions of PEs in "anywhere".  getc() has smarts,
        // so for external PEs we don't bypass it.

        // XXX we can marginally speed PE handling, and certainly
        // be cleaner (hence potentially more correct), by using
        // the observations that expanded PEs only start and stop
        // where whitespace is allowed.  getc wouldn't need any
        // "lexical" PE expansion logic, and no other MethodInfo needs
        // to handle termination of PEs.  (parsing of literals would
        // still need to pop entities, but not parsing of references
        // in content.)

        char c = getc();
        boolean saw = false;

        while (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
            saw = true;

            // this gracefully ends things when we stop playing
            // with internal parameters.  caller should have a
            // grammar rule allowing whitespace at end of entity.
            if (in.isEOF () && !in.isInternal ())
                return saw;
            c = getc ();
        }
        ungetc ();
        return saw;
    }

    private String maybeGetName ()
    throws IOException, SAXException
    {
        NameCacheEntry  entry = maybeGetNameCacheEntry ();
        return (entry == null) ? null : entry.name;
    }

    private NameCacheEntry maybeGetNameCacheEntry ()
    throws IOException, SAXException
    {
        // [5] Name ::= (Letter|'_'|':') (Namechar)*
        char            c = getc ();

        if (!XmlChars.isLetter (c) && c != ':' && c != '_') {
            ungetc ();
            return null;
        }
        return nameCharString (c);
    }

    // Used when parsing enumerations
    private String getNmtoken ()
    throws SAXException, IOException
    {
        // [7] Nmtoken ::= (Namechar)+
        char c = getc ();
        if (!XmlChars.isNameChar (c))
            fatal ("P-006", new Object [] { new Character (c) });
        return nameCharString (c).name;
    }

    // n.b. this gets used when parsing attribute values (for
    // internal references) so we can't use strTmp; it's also
    // a hotspot for CPU and memory in the parser (called at least
    // once for each element) so this has been optimized a bit.

    private NameCacheEntry nameCharString (char c)
    throws IOException, SAXException
    {
        int     i = 1;

        nameTmp [0] = c;
        for (;;) {
            if ((c = in.getNameChar ()) == 0)
                break;
            if (i >= nameTmp.length) {
                char tmp [] = new char [nameTmp.length + 10];
                System.arraycopy (nameTmp, 0, tmp, 0, nameTmp.length);
                nameTmp = tmp;
            }
            nameTmp [i++] = c;
        }
        return nameCache.lookupEntry (nameTmp, i);
    }

    //
    // much similarity between parsing entity values in DTD
    // and attribute values (in DTD or content) ... both follow
    // literal parsing rules, newline canonicalization, etc
    //
    // leaves value in 'strTmp' ... either a "replacement text" (4.5),
    // or else partially normalized attribute value (the first bit
    // of 3.3.3's spec, without the "if not CDATA" bits).
    //
    private void parseLiteral (boolean isEntityValue)
    throws IOException, SAXException
    {
        // [9] EntityValue ::=
        //      '"' ([^"&%] | Reference | PEReference)* '"'
        //    | "'" ([^'&%] | Reference | PEReference)* "'"
        // [10] AttValue ::=
        //      '"' ([^"&]  | Reference              )* '"'
        //    | "'" ([^'&]  | Reference              )* "'"

        // Only expand PEs in getc() when processing entity value literals
        // and do not expand when processing AttValue.  Save state of
        // doLexicalPE and restore it before returning.
        boolean savedLexicalPE = doLexicalPE;
//         doLexicalPE = isEntityValue;

        char            quote = getc ();
        char            c;
        InputEntity     source = in;

        if (quote != '\'' && quote != '"')
            fatal ("P-007");

        // don't report entity expansions within attributes,
        // they're reported "fully expanded" via SAX
        isInAttribute = !isEntityValue;

        // get value into strTmp
        strTmp = new StringBuffer ();

        // scan, allowing entity push/pop wherever ...
        // expanded entities can't terminate the literal!
        for (;;) {
            if (in != source && in.isEOF ()) {
                // we don't report end of parsed entities
                // within attributes (no SAX hooks)
                in = in.pop ();
                continue;
            }
            if ((c = getc ()) == quote && in == source)
                break;

            //
            // Basically the "reference in attribute value"
            // row of the chart in section 4.4 of the spec
            //
            if (c == '&') {
                String  entityName = maybeGetName ();

                if (entityName != null) {
                    nextChar (';', "F-020", entityName);

                    // 4.4 says:  bypass these here ... we'll catch
                    // forbidden refs to unparsed entities on use
                    if (isEntityValue) {
                        strTmp.append ('&');
                        strTmp.append (entityName);
                        strTmp.append (';');
                        continue;
                    }
                    expandEntityInLiteral (entityName, entities, isEntityValue);


                // character references are always included immediately
                } else if ((c = getc ()) == '#') {
                    int tmp = parseCharNumber ();

                    if (tmp > 0xffff) {
                        tmp = surrogatesToCharTmp (tmp);
                        strTmp.append (charTmp [0]);
                        if (tmp == 2)
                            strTmp.append (charTmp [1]);
                    } else
                        strTmp.append ((char) tmp);
                } else
                    fatal ("P-009");
                continue;

            }

            // expand parameter entities only within entity value literals
            if (c == '%' && isEntityValue) {
                String  entityName = maybeGetName ();

                if (entityName != null) {
                    nextChar (';', "F-021", entityName);
                    if (inExternalPE)
                        expandEntityInLiteral (entityName,
                                params, isEntityValue);
                    else
                        fatal ("P-010", new Object [] { entityName });
                    continue;
                } else
                    fatal ("P-011");
            }

            // For attribute values ...
            if (!isEntityValue) {
                // 3.3.3 says whitespace normalizes to space...
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    strTmp.append (' ');
                    continue;
                }

                // "<" not legal in parsed literals ...
                if (c == '<')
                    fatal ("P-012");
            }

            strTmp.append (c);
        }

        isInAttribute = false;
//         doLexicalPE = savedLexicalPE;
    }

    // does a SINGLE expansion of the entity (often reparsed later)
    private void expandEntityInLiteral (
        String          name,
        SimpleHashtable table,
        boolean         isEntityValue
    ) throws SAXException, IOException
    {
        Object  entity = table.get (name);

        //
        // Note:  if entity is a PE (value.isPE) there is an XML
        // requirement that the content be "markkupdecl", but that error
        // is ignored here (as permitted by the XML spec).
        //
        if (entity instanceof InternalEntity) {
            InternalEntity value = (InternalEntity) entity;
            if (supportValidation && isValidating
                    && isStandalone
                    && !value.isFromInternalSubset)
                error ("V-002", new Object [] { name });
            pushReader (value.buf, name, !value.isPE);

        } else if (entity instanceof ExternalEntity) {
            if (!isEntityValue) // must be a PE ...
                fatal ("P-013", new Object [] { name });
            // XXX if this returns false ...
            pushReader ((ExternalEntity) entity);

        } else if (entity == null) {
            //
            // Note:  much confusion about whether spec requires such
            // errors to be fatal in many cases, but none about whether
            // it allows "normal" errors to be unrecoverable!
            //
            fatal (
                (table == params) ? "V-022" : "P-014",
                new Object [] { name });
        }
    }

    // [11] SystemLiteral ::= ('"' [^"]* '"') | ("'" [^']* "'")
    // for PUBLIC and SYSTEM literals, also "<?xml ...type='literal'?>'
    
    // NOTE:  XML spec should explicitly say that PE ref syntax is
    // ignored in PIs, comments, SystemLiterals, and Pubid Literal
    // values ... can't process the XML spec's own DTD without doing
    // that for comments.

    private String getQuotedString (String type, String extra)
    throws IOException, SAXException
    {
        // use in.getc to bypass PE processing
        char             quote = in.getc ();

        if (quote != '\'' && quote != '"')
            fatal ("P-015", new Object [] {
                messages.getMessage (locale, type, new Object [] { extra })
                });

        char            c;

        strTmp = new StringBuffer ();
        while ((c = in.getc ()) != quote)
            strTmp.append ((char)c);
        return strTmp.toString ();
    }


    private String parsePublicId ()
    throws IOException, SAXException
    {
        // [12] PubidLiteral ::= ('"' PubidChar* '"') | ("'" PubidChar* "'")
        // [13] PubidChar ::= #x20|#xd|#xa|[a-zA-Z0-9]|[-'()+,./:=?;!*#@$_%]
        String retval = getQuotedString ("F-033", null);
        for (int i = 0; i < retval.length (); i++) {
            char c = retval.charAt (i);
            if (" \r\n-'()+,./:=?;!*#@$_%0123456789".indexOf(c) == -1
                    && !(c >= 'A' && c <= 'Z')
                    && !(c >= 'a' && c <= 'z'))
                fatal ("P-016", new Object [] { new Character (c) });
        }
        strTmp = new StringBuffer ();
        strTmp.append (retval);
        return normalize (false);
    }

        // [14] CharData ::= [^<&]* - ([^<&]* ']]>' [^<&]*)
        // handled by:  InputEntity.parsedContent()

    private boolean maybeComment (boolean skipStart)
    throws IOException, SAXException
    {
        // [15] Comment ::= '<!--'
        //              ( (Char - '-') | ('-' (Char - '-'))*
        //              '-->'
        if (!in.peek (skipStart ? "!--" : "<!--", null))
            return false;

        boolean         savedLexicalPE = doLexicalPE;

        doLexicalPE = false;
        boolean saveCommentText = lexicalHandler != nullHandler;
        if (saveCommentText) {
            strTmp = new StringBuffer ();
        }

    oneComment:
        for (;;) {
            try {
                // bypass PE expansion, but permit PEs
                // to complete ... valid docs won't care.
                for (;;) {
                    int c = getc ();
                    if (c == '-') {
                        c = getc ();
                        if (c != '-') {
                            if (saveCommentText)
                                strTmp.append ('-');
                            ungetc ();
                            continue;
                        }
                        nextChar ('>', "F-022", null);
                        break oneComment;
                    }
                    if (saveCommentText)
                        strTmp.append ((char)c);
                }
            } catch (EndOfInputException e) {
                //
                // This is fatal EXCEPT when we're processing a PE...
                // in which case a validating processor reports an error.
                // External PEs are easy to detect; internal ones we
                // infer by being an internal entity outside an element.
                //
                if (inExternalPE || (!donePrologue && in.isInternal ())) {
                    if (supportValidation && isValidating)
                        error ("V-021", null);
                    in = in.pop ();
                    continue;
                }
                fatal ("P-017");
            }
        }
        doLexicalPE = savedLexicalPE;
        if (saveCommentText) {
            // Convert string to array of chars
            int length = strTmp.length();
            char[] charArray = new char[length];
            if (length != 0) {
                // XXX Avoid calling getChars on zero-size array as a
                // workaround for a bug that occurs in at least JDK1.2.2
                // which has since been fixed in JDK1.3
                strTmp.getChars(0, length, charArray, 0);
            }
            lexicalHandler.comment(charArray, 0, length);
        }
        return true;
    }

    private boolean maybePI (boolean skipStart)
    throws IOException, SAXException
    {
        // [16] PI ::= '<?' PITarget
        //              (S (Char* - (Char* '?>' Char*)))?
        //              '?>'
        // [17] PITarget ::= Name - (('X'|'x')('M'|'m')('L'|'l')
        boolean         savedLexicalPE = doLexicalPE;

        if (!in.peek (skipStart ? "?" : "<?", null))
            return false;
        doLexicalPE = false;

        String          target = maybeGetName ();

        if (target == null)
            fatal ("P-018");
        if ("xml".equals (target))
            fatal ("P-019");
        if ("xml".equalsIgnoreCase (target))
            fatal ("P-020", new Object [] { target });

        if (maybeWhitespace ()) {
            strTmp = new StringBuffer ();
            try {
                for (;;) {
                    // use in.getc to bypass PE processing
                    char c = in.getc ();
                    //Reached the end of PI.
                    if (c == '?' && in.peekc ('>'))
                        break;
                    strTmp.append (c);
                }
            } catch (EndOfInputException e) {
                fatal ("P-021");
            }
            contentHandler.processingInstruction (target, strTmp.toString ());
        } else {
            if (!in.peek ("?>", null))
                fatal ("P-022");
            contentHandler.processingInstruction (target, "");
        }

        doLexicalPE = savedLexicalPE;
        return true;
    }

        // [18] CDSect ::= CDStart CData CDEnd
        // [19] CDStart ::= '<![CDATA['
        // [20] CData ::= (Char* - (Char* ']]>' Char*))
        // [21] CDEnd ::= ']]>'
        //
        //      ... handled by InputEntity.unparsedContent()


    private void maybeXmlDecl ()
    throws IOException, SAXException
    {
        // [23] XMLDecl ::= '<?xml' VersionInfo EncodingDecl?
        //                      SDDecl? S? '>'

        if (!in.isXmlDeclOrTextDeclPrefix()) {
            return;
        }
        // Consume '<?xml'
        peek("<?xml");

        readVersion (true, "1.0");
        readEncoding (false);
        readStandalone ();
        maybeWhitespace ();
        if (!peek ("?>")) {
            char c = getc ();
            fatal ("P-023", new Object []
                { Integer.toHexString (c), new Character (c) });
        }
    }

    // collapsing several rules together ... 
    // simpler than attribute literals -- no reference parsing!
    private String maybeReadAttribute (String name, boolean must)
    throws IOException, SAXException
    {
        // [24] VersionInfo ::= S 'version' Eq \'|\" versionNum \'|\"
        // [80] EncodingDecl ::= S 'encoding' Eq \'|\" EncName \'|\"
        // [32] SDDecl ::=  S 'standalone' Eq \'|\" ... \'|\"
        if (!maybeWhitespace ()) {
            if (!must)
                return null;
            fatal ("P-024", new Object [] { name });
            // NOTREACHED
        }

        if (!peek (name))
            if (must)
                fatal ("P-024", new Object [] { name });
            else {
                // To ensure that the whitespace is there so that when we
                // check for the next attribute we assure that the
                // whitespace still exists.
                ungetc ();
                return null;
            }

        // [25] Eq ::= S? '=' S?
        maybeWhitespace ();
        nextChar ('=', "F-023", null);
        maybeWhitespace ();

        return getQuotedString ("F-035", name);
    }

    private void readVersion (boolean must, String versionNum)
    throws IOException, SAXException
    {
        String  value = maybeReadAttribute ("version", must);

        // [26] versionNum ::= ([a-zA-Z0-9_.:]| '-')+

        if (must && value == null)
            fatal ("P-025", new Object [] { versionNum });
        if (value != null) {
            int length = value.length ();
            for (int i = 0; i < length; i++) {
                char c = value.charAt (i);
                if (!(    (c >= '0' && c <= '9')
                        || c == '_' || c == '.'
                        || (c >= 'a' && c <= 'z')
                        || (c >= 'A' && c <= 'Z')
                        || c == ':' || c == '-')
                        )
                    fatal ("P-026", new Object [] { value });
            }
        }
        if (value != null && !value.equals (versionNum))
            error ("P-027", new Object [] { versionNum, value });
    }

    private void maybeMisc (boolean eofOK)
    throws IOException, SAXException
    {
        // Misc*
        while (!eofOK || !in.isEOF ()) {
            // [27] Misc ::= Comment | PI | S
            if (maybeComment (false)
                    || maybePI (false)
                    || maybeWhitespace ())
                continue;
            else
                break;
        }
    }

    // common code used by most markup declarations
    // ... S (Q)Name ...
    private String getMarkupDeclname (String roleId, boolean qname)
    throws IOException, SAXException
    {
        String  name;

        whitespace (roleId);
        name = maybeGetName ();
        if (name == null)
            fatal ("P-005", new Object []
                { messages.getMessage (locale, roleId) });
        return name;
    }

    private boolean maybeDoctypeDecl ()
    throws IOException, SAXException
    {
        // [28] doctypedecl ::= '<!DOCTYPE' S Name
        //      (S ExternalID)?
        //      S? ('[' (markupdecl|PEReference|S)* ']' S?)?
        //      '>'
        if (!peek ("<!DOCTYPE"))
            return false;

        ExternalEntity  externalSubset = null;

        rootElementName = getMarkupDeclname ("F-014", true);
        if (maybeWhitespace ()
                && (externalSubset = maybeExternalID ()) != null) {
            lexicalHandler.startDTD(rootElementName, externalSubset.publicId,
                                    externalSubset.verbatimSystemId);
            maybeWhitespace ();
        } else {
            lexicalHandler.startDTD(rootElementName, null, null);
        }
        if (in.peekc ('[')) {
            for (;;) {
                //Pop PEs when they are done.
                if (in.isEOF () && !in.isDocument ()) {
                    in = in.pop ();
                    continue;
                }
                if (maybeMarkupDecl ()
                        || maybePEReference ()
                        || maybeWhitespace ()
                        )
                    continue;
                else if (peek ("<!["))
                    fatal ("P-028");
                else
                    break;
            }
            nextChar (']', "F-024", null);
            maybeWhitespace ();
        }
        nextChar ('>', "F-025", null);

        // [30] extSubset ::= TextDecl? extSubsetDecl
        // [31] extSubsetDecl ::= ( markupdecl | conditionalSect
        //              | PEReference | S )*
        //      ... same as [79] extPE, which is where the code is

        if (externalSubset != null) {
            externalSubset.name = "[dtd]";  // SAX2 ext specifies this name
            externalSubset.isPE = true;
            externalParameterEntity (externalSubset);
        }

        // params are no good to anyone starting now -- bye!
        params.clear ();

        lexicalHandler.endDTD();

        // make sure notations mentioned in attributes
        // and entities were declared ... those are validity
        // errors, but we must always clean up after them!
        Vector  v = new Vector ();

        for (Enumeration e = notations.keys ();
                e.hasMoreElements ();
                ) {
            String name = (String) e.nextElement ();
            Object value = notations.get (name);

            if (value == Boolean.TRUE) {
                if (supportValidation && isValidating)
                    error ("V-003", new Object [] { name });
                v.addElement (name);
            } else if (value instanceof String) {
                if (supportValidation && isValidating)
                    error ("V-004", new Object [] { name });
                v.addElement (name);
            }
        }
        while (!v.isEmpty ()) {
            Object name = v.firstElement ();
            v.removeElement (name);
            notations.remove (name);
        }

        return true;
    }

    private boolean maybeMarkupDecl ()
    throws IOException, SAXException
    {
            // [29] markupdecl ::= elementdecl | Attlistdecl
            //         | EntityDecl | NotationDecl | PI | Comment
        return maybeElementDecl ()
                || maybeAttlistDecl ()
                || maybeEntityDecl ()
                || maybeNotationDecl ()
                || maybePI (false)
                || maybeComment (false)
                ;
    }


    private void readStandalone ()
    throws IOException, SAXException
    {
        String  value = maybeReadAttribute ("standalone", false);

        // [32] SDDecl ::= ... "yes" or "no"
        if (value == null || "no".equals (value))
            return;
        if ("yes".equals (value)) {
            isStandalone = true;
            return;
        }
        fatal ("P-029", new Object [] { value });
    }

    private static final String         XmlLang = "xml:lang";

    private boolean isXmlLang (String value)
    {
        // [33] LanguageId ::= Langcode ('-' Subcode)*
        // [34] Langcode ::= ISO639Code | IanaCode | UserCode
        // [35] ISO639Code ::= [a-zA-Z] [a-zA-Z]
        // [36] IanaCode ::= [iI] '-' SubCode
        // [37] UserCode ::= [xX] '-' SubCode
        // [38] SubCode ::= [a-zA-Z]+

        // the ISO and IANA codes (and subcodes) are registered,
        // but that's neither a WF nor a validity constraint.

        int     nextSuffix;
        char    c;
        
        if (value.length () < 2)
            return false;
        c = value.charAt (1);
        if (c == '-') {         // IANA, or user, code
            c = value.charAt (0);
            if (!(c == 'i' || c == 'I' || c == 'x' || c == 'X'))
                return false;
            nextSuffix = 1;
        } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                                // 2 letter ISO code, or error
            c = value.charAt (0);
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')))
                return false;
            nextSuffix = 2;
        } else
            return false;
        
        // here "suffix" ::= '-' [a-zA-Z]+ suffix*
        while (nextSuffix < value.length ()) {
            c = value.charAt (nextSuffix);
            if (c != '-')
                break;
            while (++nextSuffix < value.length ()) {
                c = value.charAt (nextSuffix);
                if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')))
                    break;
            }
        }
        return value.length () == nextSuffix && c != '-';
    }



    //
    // CHAPTER 3:  Logical Structures
    //

    private boolean maybeElement (ElementValidator validator)
    throws IOException, SAXException
    {
        // [39] element ::= EmptyElemTag | Stag content ETag
        // [40] STag ::= '<' Name (S Attribute)* S? '>'

        NameCacheEntry          name;
        ElementDecl             element;
        boolean                 haveAttributes = false;
        boolean                 hasContent = true;
        int                     startLine;

        // the leading "<" has already been consumed
        name = maybeGetNameCacheEntry ();

        // n.b. InputEntity guarantees 1+N char pushback always,
        // and maybeGetName won't use more than one to see if
        // it's instead "<?", "<!--", "<![CDATA[", or an error.
        if (name == null)
            return false;

        // XXX Test for namespace conformance here
        // if (namespaces) {
            // some code testing name.name
        // }
            
        // report validity errors ASAP
        if (validator != null)
            validator.consume (name.name);

        element = (ElementDecl) elements.get (name.name);
        if (supportValidation && isValidating) {
            if (element == null || element.contentType == null) {
                error ("V-005", new Object [] { name.name });
                // minimize repetitive diagnostics
                element = new ElementDecl (name.name);
                element.contentType = strANY;
                elements.put (name.name, element);
            }
            if (validator == null
                    && rootElementName != null
                    && !rootElementName.equals (name.name))
                error ("V-006", new Object [] { name.name, rootElementName });
        }

        // save the line number here so we can give better diagnostics
        // by identifying where the element started; WF errors may be
        // reported thousands of lines "late".
        startLine = in.getLineNumber ();

        // Invariant: attTmp and nsAttTmp are empty except briefly in this
        // MethodInfo they are not empty iff haveAttributes is true

        // Track whether we saw whitespace before an attribute;
        // in some cases it's required, though superfluous
        boolean         sawWhite = in.maybeWhitespace ();

        // These are exceptions from the first pass; they should be ignored
        // if there's a second pass, but reported otherwise.  A second pass
        // occurs when a namespace declaration is found in the first pass.
	Vector exceptions = null;

        // SAX2 Namespace processing
        if (namespaces) {
            nsSupport.pushContext();
            seenNSDecl = false;
        }

        // Each pass through this loop reads
        //      Name eq AttValue S?
        // Loop exits on ">", "/>", or error
        for (;;) {
            if (in.peekc ('>'))
                break;

            // [44] EmptyElementTag ::= '<' Name (S Attribute)* S? '/>'
            if (in.peekc ('/')) {
                hasContent = false;
                break;
            }

            //Need to have a whitespace between attributes.
            if (!sawWhite)
                fatal ("P-030");

            // [41] Attribute ::= Name Eq AttValue

            String              attQName;
            AttributeDecl       info;
            String              value;

            attQName = maybeGetName ();
            // Need to do this as we have already consumed the 
            // whitespace and didn't see the end tag.
            if (attQName == null)
                fatal ("P-031", new Object [] { new Character (getc ()) });

            if (attTmp.getValue (attQName) != null)
                fatal ("P-032", new Object [] { attQName });

            // [25] Eq ::= S? '=' S?
            in.maybeWhitespace ();
            nextChar ('=', "F-026", attQName);
            in.maybeWhitespace ();

            // We are not in the DTD => PEs are not recognized => we no
            // longer need to expand PEs => don't expand PEs in AttValue =>
            // doLexicalPE = false and call parseLiteral(isEntityValue =
            // false) both
            doLexicalPE = false;
            parseLiteral (false);
            // We are no longer in the DTD so we never need to expand PEs

            sawWhite = in.maybeWhitespace ();

            // normalize and check values right away.

            info = (element == null)
                    ? null
                    : (AttributeDecl) element.attributes.get (attQName);
            if (info == null) {
                if (supportValidation && isValidating)
                    error ("V-007", new Object [] { attQName, name.name });
                value = strTmp.toString ();
            } else {
                if (!AttributeDecl.CDATA.equals (info.type)) {
                    value = normalize (!info.isFromInternalSubset);
                    if (supportValidation && isValidating)
                        validateAttributeSyntax (info, value);
                } else
                    value = strTmp.toString ();
                if (supportValidation && isValidating
                        && info.isFixed
                        && !value.equals (info.defaultValue))
                    error ("V-008",
                        new Object [] {attQName, name.name, info.defaultValue});
            }

            // assert(value != null)

            if (XmlLang.equals (attQName) && !isXmlLang (value))
                error ("P-033", new Object [] { value });

            String type = (info == null) ? AttributeDecl.CDATA : info.type;
            String defaultValue = (info == null) ? null : info.defaultValue;

            if (namespaces) {
                exceptions = processAttributeNS(attQName, type, value,
                                                defaultValue, true, false,
                                                exceptions);
            } else {
                // No namespaces case
                attTmp.addAttribute("", "", attQName, type, value,
                                    defaultValue, true);
            }

            haveAttributes = true;
        }
        if (element != null)
            attTmp.setIdAttributeName (element.id);

        // if we had ATTLIST decls, handle required & defaulted attributes
        // before telling next layer about this element
        if (element != null && element.attributes.size () != 0) {
            haveAttributes = defaultAttributes(element) || haveAttributes;
        }

        // Ensure that this element's namespace declarations apply to all of
        // this element's attributes as well.  If there was a Namespace
        // declaration, we have to make a second pass just to be safe -- this
        // will happen very rarely, possibly only once for each document.
        if (seenNSDecl) {
            // assert(namespaces == true)
            int length = attTmp.getLength();
            for (int i = 0; i < length; i++) {
                String attQName = attTmp.getQName(i);
                if (attQName.startsWith("xmlns")) {
                    // Could be a namespace declaration

                    if (attQName.length() == 5 || attQName.charAt(5) == ':') {
                        // Default or non-default NS declaration
                        continue;
                    }
                }

                // assert(not a namespace declaration)
                String attName[] = processName(attQName, true, false);
                attTmp.setURI(i, attName[0]);
                attTmp.setLocalName(i, attName[1]);
            }
	} else if (exceptions != null && errHandler != null) {
	    for (int i = 0; i < exceptions.size(); i++) {
		errHandler.error((SAXParseException)(exceptions.elementAt(i)));
            }
        }

        // OK, finally report the event.
        if (namespaces) {
            String[] parts = processName(name.name, false, false);
            contentHandler.startElement(parts[0], parts[1], parts[2], attTmp);
        } else {
            contentHandler.startElement("", "", name.name, attTmp);
        }

        // Clear temporaries only when necessary because this may be
        // expensive and a doc may have lots of elements w/o attributes
        if (haveAttributes) {
            attTmp.clear();
            if (supportValidation && isValidating && namespaces && !prefixes) {
                nsAttTmp.removeAllElements();
            }
        }

        // prepare to validate the content of this element.
        // in nonvalidating parsers, this accepts ANY content
        validator = newValidator (element);
        
        if (hasContent) {
            content (element, false, validator);

            // [42] ETag ::= '</' Name S? '>'
            // ... content swallowed "</"

            if (!in.peek (name.name, name.chars))
                fatal ("P-034", new Object []
                    { name.name, new Integer (startLine) });
            in.maybeWhitespace ();
        }

        nextChar ('>', "F-027", name.name);
        validator.done ();

        if (namespaces) {
            // Split the name.  Unfortunately, we can't always reuse the
            // info from the startElement event above b/c this element may
            // have subelements and a global temporary is used.
            String[] parts = processName(name.name, false, false);

            // Report appropriate events...
            contentHandler.endElement(parts[0], parts[1], parts[2]);
            Enumeration prefixes = nsSupport.getDeclaredPrefixes();
            while (prefixes.hasMoreElements()) {
                String prefix = (String)prefixes.nextElement();
                contentHandler.endPrefixMapping(prefix);
            }
            nsSupport.popContext();
        } else {
            contentHandler.endElement("", "", name.name);
        }

        return true;
    }

    /**
     * Process attributes for namespace support.  This is mostly common
     * code that gets called from two places and was factored out.  The
     * <code>isDefaulting</code> param specifies where the code is called
     * from.
     *
     * @param isDefaulting true iff we are processing this attribute from
     *                     the <code>defaultAttributes(...)</code> MethodInfo
     *
     * The namespace processing code is derived from the SAX2 ParserAdapter
     * code.  This code should be kept in sync with ParserAdapter bug
     * fixes.
     *
     * Note: Modifies <code>seenNSDecl</code> iff a xmlns attribute, ie a
     * namespace decl, was found.  Modifies <code>attTmp</code> and
     * <code>nsAttTmp</code>.
     */
    private Vector processAttributeNS(String attQName, String type,
                                      String value, String defaultValue,
                                      boolean isSpecified, boolean isDefaulting,
                                      Vector exceptions)
        throws SAXException
    {
        // assert(namespaces == true)

      nonNamespace:
        if (attQName.startsWith("xmlns")) {
            // Could be a namespace declaration

            boolean defaultNSDecl = attQName.length() == 5;
            if (!defaultNSDecl && attQName.charAt(5) != ':') {
                // Not a namespace declaration
                break nonNamespace;
            }

            // Must be some kind of namespace declaration
            String prefix;
            if (defaultNSDecl) {
                // Default namespace, so use empty string as prefix
                prefix = "";
            } else {
                // Non-default namespace decl, extract the prefix
                prefix = attQName.substring(6);
            }

            if (!nsSupport.declarePrefix(prefix, value)) {
                error("P-083", new Object[] { prefix });
            }
            contentHandler.startPrefixMapping(prefix, value);

            // We may need to add this attribute to appropriate lists
            if (prefixes) {
                attTmp.addAttribute("", prefix, attQName.intern(),
                                    type, value, defaultValue, isSpecified);
            } else if (supportValidation && isValidating && !isDefaulting) {
                // Add this namespace attribute to a different list that
                // will be used to check for #REQUIRED attributes later.
                // Since "prefixes" is false, these are not reported to the
                // ContentHandler.  This step is not needed during the
                // second pass of attribute processing where default values
                // are provided.
                nsAttTmp.addElement(attQName);
            }
            seenNSDecl = true;
            return exceptions;
        }

        // This isn't a namespace declaration.
        try {
            String attName[] = processName(attQName, true, true);
            attTmp.addAttribute(attName[0], attName[1], attName[2], type,
                                value, defaultValue, isSpecified);
        } catch (SAXException e) {
            if (exceptions == null) {
                exceptions = new Vector();
            }
            exceptions.addElement(e);
            attTmp.addAttribute("", attQName, attQName, type, value,
                                defaultValue, isSpecified);
        }
        return exceptions;
    }

    /**
     * Process a qualified (prefixed) name.
     *
     * <p>If the name has an undeclared prefix, use only the qname
     * and make an ErrorHandler.error callback in case the app is
     * interested.</p>
     *
     * @param qName The qualified (prefixed) name.
     * @param isAttribute true if this is an attribute name.
     * @return The name split into three parts.
     * @exception org.xml.sax.SAXException The client may throw
     *            an exception if there is an error callback.
     */
    private String[] processName(String qName, boolean isAttribute,
                                 boolean useException)
        throws SAXException
    {
        // assert(namespaces == true)
        String parts[] = nsSupport.processName(qName, namePartsTmp,
                                               isAttribute);
        if (parts == null) {
            parts = new String[3];
            // SAX should use "" instead of null for parts 0 and 1 ???
            parts[0] = "";
            String localName = XmlNames.getLocalPart(qName);
            parts[1] = localName != null ? localName.intern() : "";
            parts[2] = qName.intern();

            String messageId = "P-084";
            Object[] parameters = new Object[] { qName };
	    if (useException) {
                throw new SAXParseException(
                    messages.getMessage(locale, messageId, parameters),
                    locator);
            }
            error(messageId, parameters);
        }
        return parts;
    }


    /**
     * To validate, subclassers should create an object that can
     * accept valid streams of element names, text, and terminate.
     */
    // package private ... overriden in validating subclass
    ElementValidator newValidator (ElementDecl element)
    {
        return ElementValidator.ANY;            // "ANY" content is OK
    }


    /**
     * To validate, subclassers should at this time make sure that
     * values are of the declared types:<UL>
     *  <LI> ID and IDREF(S) values are Names
     *  <LI> NMTOKEN(S) are Nmtokens
     *  <LI> ENUMERATION values match one of the tokens
     *  <LI> NOTATION values match a notation name
     *  <LI> ENTITIY(IES) values match an unparsed external entity
     *  </UL>
     *
     * <P> Separately, make sure IDREF values match some ID
     * provided in the document (in the afterRoot MethodInfo).
     */
    // package private
    void validateAttributeSyntax (AttributeDecl attr, String value)
    throws SAXException
    {
        return;
    }

    /**
     * Provide default attributes for an element and check for #REQUIRED
     * attributes.
     *
     * Note: this MethodInfo accesses <code>attTmp</code> and
     * <code>nsAttTmp</code>
     */
    private boolean defaultAttributes(ElementDecl element)
        throws SAXException
    {
        boolean         didDefault = false;

        // Go through all declared attributes and:
        // 1) Default anything the document didn't provide.
        // 2) Check #REQUIRED values.
        for (Enumeration e = element.attributes.keys();
                e.hasMoreElements(); ) {

            // Declared attribute name
            String declAttName = (String)e.nextElement();

            if (attTmp.getValue(declAttName) != null) {
                // Attribute already has value so no defaulting necessary
                continue;
            }

            // If we get here, then declared attribute is not in the list
            // of attributes to be reported to ContentHandler.

            // Get more info on the declared attribute
            AttributeDecl info =
                    (AttributeDecl)element.attributes.get(declAttName);

            // If this is a #REQUIRED attribute...
            if (supportValidation && isValidating && info.isRequired) {
                // Under certain conditions, check the auxiliary nsAttTmp
                // list for #REQUIRED attributes since these are not in the
                // list to be reported to the ContentHandler.
                if (namespaces && !prefixes) {
                    if (nsAttTmp.contains(declAttName)) {
                        // Namespace attribute is #REQUIRED and already has
                        // a value
                        continue;
                    }
                }
                error("V-009", new Object [] { declAttName });
            }

            String defaultValue = info.defaultValue;
            if (defaultValue != null) {
                if (supportValidation && isValidating
                        && isStandalone && !info.isFromInternalSubset)
                    error ("V-010", new Object [] { declAttName });

                if (namespaces) {
                    processAttributeNS(declAttName, info.type, defaultValue,
                                       defaultValue, false, true, null);
                } else {
                    attTmp.addAttribute("", "", declAttName, info.type,
                                        defaultValue, defaultValue, false);
                }
                didDefault = true;
            }
        }
        return didDefault;
    }

    // parses content inside a given element (or parsed entity), optionally
    // allowing EOF (when expanding internal or external entities) and
    // optionally validating elements/#PCDATA that we see
    private void content (
        ElementDecl             element,
        boolean                 allowEOF,
        ElementValidator        validator
    ) throws IOException, SAXException
    {
        for (;;) {
            // [43] content ::= (element|CharData|Reference
            //                  |CDSect|PI|Comment)*

            // markup?
            if (in.peekc ('<')) {
                if (maybeElement (validator))
                    continue;

                // Three cases:  Error, and either EOF or ETag.
                // Here we check Etag as a common exit. 
                if (in.peekc ('/'))
                    return;

                // Less commonly, it's a comment, PI, CDATA ...
                if (maybeComment (true) || maybePI (true))
                    continue;

                // ... CDATA are specially delimited characters; can be
                // #PCDATA or whitespace (the latter has validity issues).
                if (in.peek("![CDATA[", null)) {
                    lexicalHandler.startCDATA();
                    in.unparsedContent(contentHandler, validator,
                        (element != null) && element.ignoreWhitespace,
                        (isStandalone
                                && supportValidation && isValidating
                                && !element.isFromInternalSubset)
                            ? "V-023"
                            : null
                        );
                    lexicalHandler.endCDATA();
                    continue;
                }

                // ... or a grammatical error (WF violation).
                char    c = getc ();

                fatal ("P-079", new Object [] {
                    Integer.toHexString (c), new Character (c) });
                // NOTREACHED
            }

            // characters? ... whitespace or #PCDATA
            if (element != null
                    && element.ignoreWhitespace
                    && in.ignorableWhitespace (contentHandler)) {
                // XXX prefer to report validity error before the
                // whitespace was reported ...
                if (supportValidation && isValidating
                        && isStandalone && !element.isFromInternalSubset)
                    error ("V-011", new Object [] { element.name });
                continue;
            }
            if (in.parsedContent (contentHandler, validator))
                continue;

            if (in.isEOF ())
                break;

            // else MUST be an entity reference
            if (!maybeReferenceInContent (element, validator))
                throw new InternalError ();
        }
        if (!allowEOF)
            fatal ("P-035");
    }

    private boolean maybeElementDecl ()
    throws IOException, SAXException
    {
        // [45] elementDecl ::= '<!ELEMENT' S Name S contentspec S? '>'
        // [46] contentspec ::= 'EMPTY' | 'ANY' | Mixed | children
        InputEntity     start = peekDeclaration ("!ELEMENT");

        if (start == null)
            return false;

        // n.b. for content models where inter-element whitespace is 
        // ignorable, we mark that fact here.
        String          name = getMarkupDeclname ("F-015", true);
        ElementDecl     element = (ElementDecl) elements.get (name);
        boolean         declEffective = false;

        if (element != null) {
            if (element.contentType != null) {
                if (supportValidation && isValidating
                        && element.contentType != null)
                    error ("V-012", new Object [] { name });
                // don't override previous declaration
                element = new ElementDecl (name);
            } // else <!ATTLIST name ...> came first
        } else {
            element = new ElementDecl (name);
            if (!ignoreDeclarations) {
                elements.put (element.name, element);
                declEffective = true;
            }
        }
        element.isFromInternalSubset = !inExternalPE;

        whitespace ("F-000");
        if (peek (strEMPTY)) {
            element.contentType = strEMPTY;
            element.ignoreWhitespace = true;
        } else if (peek (strANY)) {
            element.contentType = strANY;
            element.ignoreWhitespace = false;
        } else
            element.contentType = getMixedOrChildren (element);

        maybeWhitespace ();
        char c = getc ();
        if (c != '>')
            fatal ("P-036", new Object [] { name, new Character (c) });
        if (supportValidation && isValidating && start != in)
            error ("V-013", null);

        if (declEffective) {
            declHandler.elementDecl(element.name, element.contentType);
        }
        return true;
    }

    // We're leaving the content model as a regular expression;
    // it's an efficient natural way to express such things, and
    // libraries often interpret them.  No whitespace in the
    // model we store, though!

    private String getMixedOrChildren (ElementDecl element)
    throws IOException, SAXException
    {           
        InputEntity     start;

        // [47] children ::= (choice|seq) ('?'|'*'|'+')?
        strTmp = new StringBuffer ();

        nextChar ('(', "F-028", element.name);
        start = in;
        maybeWhitespace ();
        strTmp.append ('(');

        if (peek ("#PCDATA")) {
            strTmp.append ("#PCDATA");
            getMixed (element.name, start);
            element.ignoreWhitespace = false;
        } else {
            element.model = getcps (element.name, start);
            element.ignoreWhitespace = true;
        }
        return strTmp.toString ();
    }

    // package private -- overridden by validating subclass
    ContentModel newContentModel (String tag)
    {
        return null;
    }

    // package private -- overridden by validating subclass
    ContentModel newContentModel (char type, ContentModel next)
    {
        return null;
    }

    // '(' S? already consumed
    // matching ')' must be in "start" entity if validating
    private ContentModel getcps (
        String          element,
        InputEntity     start
    ) throws IOException, SAXException
    {
        // [48] cp ::= (Name|choice|seq) ('?'|'*'|'+')?
        // [49] choice ::= '(' S? cp (S? '|' S? cp)* S? ')'
        // [50] seq    ::= '(' S? cp (S? ',' S? cp)* S? ')'
        boolean         decided = false;
        char            type = 0;
        ContentModel    retval, current, temp;

        retval = current = temp = null;

        do {
            String      tag;

            tag = maybeGetName ();
            if (tag != null) {
                strTmp.append (tag);
                temp = getFrequency (newContentModel (tag));
            } else if (peek ("(")) {
                InputEntity     next = in;
                strTmp.append ('(');
                maybeWhitespace ();
                temp = getFrequency (getcps (element, next));
            } else
                fatal ((type == 0) ? "P-039" :
                        ((type == ',') ? "P-037" : "P-038"),
                    new Object [] { new Character (getc ()) });

            maybeWhitespace ();
            if (decided) {
                char    c = getc ();

                if (current != null) {
                    current.next = newContentModel (type, temp);
                    current = current.next;
                }
                if (c == type) {
                    strTmp.append (type);
                    maybeWhitespace ();
                    continue;
                } else if (c == '\u0029') {     // rparen
                    ungetc ();
                    continue;
                } else {
                    fatal ((type == 0) ? "P-041" : "P-040",
                        new Object [] {
                            new Character (c),
                            new Character (type)
                            });
                }
            } else {
                type = getc ();
                if (type == '|' || type == ',') {
                    decided = true;
                    retval = current = newContentModel (type, temp);
                } else {
                    retval = current = temp;
                    ungetc ();
                    continue;
                }
                strTmp.append (type);
            }
            maybeWhitespace ();
        } while (!peek (")"));
        if (supportValidation && isValidating && in != start)
            error ("V-014", new Object [] { element });
        strTmp.append (')');
        return getFrequency (retval);
    }

    private ContentModel getFrequency (ContentModel original)
    throws IOException, SAXException
    {
        char    c = getc ();

        if (c == '?' || c == '+' || c == '*') {
            strTmp.append (c);
            if (original == null)
                return null;
            if (original.type == 0) {   // foo* etc
                original.type = c;
                return original;
            }
            return newContentModel (c, original);
        } else {
            ungetc ();
            return original;
        }
    }

    // '(' S? '#PCDATA' already consumed 
    // matching ')' must be in "start" entity if validating
    private void getMixed (String element, InputEntity start)
    throws IOException, SAXException
    {
        // [51] Mixed ::= '(' S? '#PCDATA' (S? '|' S? Name)* S? ')*'
        //              | '(' S? '#PCDATA'                   S? ')'
        maybeWhitespace ();
        if (peek ("\u0029*") || peek ("\u0029")) {
            if (supportValidation && isValidating && in != start)
                error ("V-014", new Object [] { element });
            strTmp.append (')');
            return;
        }

        Vector  v = null;

        if (supportValidation && isValidating)
            v = new Vector ();

        while (peek ("|")) {
            String name;

            strTmp.append ('|');
            maybeWhitespace ();

            name = maybeGetName ();
            if (name == null)
                fatal ("P-042", new Object []
                    { element, Integer.toHexString (getc ()) });
            if (supportValidation && isValidating) {
                if (v.contains (name))
                    error ("V-015", new Object [] { name });
                else
                    v.addElement (name);
            }
            strTmp.append (name);
            maybeWhitespace ();
        }
        
        if (!peek ("\u0029*"))  // right paren
            fatal ("P-043", new Object []
                { element, new Character (getc ()) });
        if (supportValidation && isValidating && in != start)
            error ("V-014", new Object [] { element });
        strTmp.append (')');
    }

    private boolean maybeAttlistDecl ()
    throws IOException, SAXException
    {
        // [52] AttlistDecl ::= '<!ATTLIST' S Name AttDef* S? '>'
        InputEntity start = peekDeclaration ("!ATTLIST");

        if (start == null)
            return false;

        String          name = getMarkupDeclname ("F-016", true);
        ElementDecl     element = (ElementDecl) elements.get (name);

        if (element == null) {
            // not yet declared -- no problem.
            element = new ElementDecl (name);
            if (!ignoreDeclarations)
                elements.put (name, element);
        }

        maybeWhitespace ();
        while (!peek (">")) {

            // [53] AttDef ::= S Name S AttType S DefaultDecl
            // [54] AttType ::= StringType | TokenizedType | EnumeratedType
            name = maybeGetName ();
            if (name == null)
                fatal ("P-044", new Object [] { new Character (getc ()) });
            whitespace ("F-001");

            AttributeDecl       a = new AttributeDecl (name);
            a.isFromInternalSubset = !inExternalPE;

            // Note:  use the type constants from AttributeDecl
            // so that "==" may be used (faster)

            // [55] StringType ::= 'CDATA'
            if (peek (AttributeDecl.CDATA))
                a.type = AttributeDecl.CDATA;

            // [56] TokenizedType ::= 'ID' | 'IDREF' | 'IDREFS'
            //          | 'ENTITY' | 'ENTITIES'
            //          | 'NMTOKEN' | 'NMTOKENS'
            // n.b. if "IDREFS" is there, both "ID" and "IDREF"
            // match peekahead ... so this order matters!
            else if (peek (AttributeDecl.IDREFS))
                a.type = AttributeDecl.IDREFS;
            else if (peek (AttributeDecl.IDREF))
                a.type = AttributeDecl.IDREF;
            else if (peek (AttributeDecl.ID)) {
                a.type = AttributeDecl.ID;
                if (element.id != null) {
                    if (supportValidation && isValidating)
                        error ("V-016", new Object [] { element.id });
                } else
                    element.id = name;
            } else if (peek (AttributeDecl.ENTITY))
                a.type = AttributeDecl.ENTITY;
            else if (peek (AttributeDecl.ENTITIES))
                a.type = AttributeDecl.ENTITIES;
            else if (peek (AttributeDecl.NMTOKENS))
                a.type = AttributeDecl.NMTOKENS;
            else if (peek (AttributeDecl.NMTOKEN))
                a.type = AttributeDecl.NMTOKEN;

            // [57] EnumeratedType ::= NotationType | Enumeration
            // [58] NotationType ::= 'NOTATION' S '(' S? Name
            //          (S? '|' S? Name)* S? ')'
            else if (peek (AttributeDecl.NOTATION)) {
                a.type = AttributeDecl.NOTATION;
                whitespace ("F-002");
                nextChar ('(', "F-029", null);
                maybeWhitespace ();

                Vector v = new Vector ();
                do {
                    if ((name = maybeGetName ()) == null)
                        fatal ("P-068");
                    // permit deferred declarations
                    if (supportValidation && isValidating
                            && notations.get (name) == null)
                        notations.put (name, name);
                    v.addElement (name);
                    maybeWhitespace ();
                    if (peek ("|"))
                        maybeWhitespace ();
                } while (!peek (")"));
                a.values = new String [v.size ()];
                for (int i = 0; i < v.size (); i++)
                    a.values [i] = (String)v.elementAt (i);

            // [59] Enumeration ::= '(' S? Nmtoken (S? '|' Nmtoken)* S? ')'
            } else if (peek ("(")) {
                a.type = AttributeDecl.ENUMERATION;
                maybeWhitespace ();

                Vector v = new Vector ();
                do {
                    name = getNmtoken ();
                    v.addElement (name);
                    maybeWhitespace ();
                    if (peek ("|"))
                        maybeWhitespace ();
                } while (!peek (")"));
                a.values = new String [v.size ()];
                for (int i = 0; i < v.size (); i++)
                    a.values [i] = (String)v.elementAt (i);
            } else
                fatal ("P-045",
                    new Object [] { name, new Character (getc ()) });


            // [60] DefaultDecl ::= '#REQUIRED' | '#IMPLIED'
            //          | (('#FIXED' S)? AttValue)
            whitespace ("F-003");
            if (peek ("#REQUIRED")) {
                a.valueDefault = AttributeDecl.REQUIRED;
                a.isRequired = true;
            } else if (peek ("#FIXED")) {
                if (supportValidation && isValidating
                        && a.type == AttributeDecl.ID)
                    error ("V-017", new Object [] { a.name });
                a.valueDefault = AttributeDecl.FIXED;
                a.isFixed = true;
                whitespace ("F-004");

                // Don't expand PEs in AttValue => doLexicalPE = false and
                // call parseLiteral(isEntityValue = false) both
                doLexicalPE = false;
                parseLiteral(false);

                // We are in DTD so set this back to true
                doLexicalPE = true;

                if (a.type != AttributeDecl.CDATA)
                    a.defaultValue = normalize (false);
                else
                    a.defaultValue = strTmp.toString ();
                if (a.type != AttributeDecl.CDATA)
                    validateAttributeSyntax (a, a.defaultValue);
            } else if (peek ("#IMPLIED")) {
                a.valueDefault = AttributeDecl.IMPLIED;
            } else {
                if (supportValidation && isValidating
                        && a.type == AttributeDecl.ID)
                    error ("V-018", new Object [] { a.name });
                // By default a.valueDefault == null here

                // Don't expand PEs in AttValue => doLexicalPE = false and
                // call parseLiteral(isEntityValue = false) both
                doLexicalPE = false;
                parseLiteral(false);

                // We are in DTD so set this back to true
                doLexicalPE = true;

                if (a.type != AttributeDecl.CDATA)
                    a.defaultValue = normalize (false);
                else
                    a.defaultValue = strTmp.toString ();
                if (a.type != AttributeDecl.CDATA)
                    validateAttributeSyntax (a, a.defaultValue);
            }

            if (XmlLang.equals (a.name)
                    && a.defaultValue != null
                    && !isXmlLang (a.defaultValue))
                error ("P-033", new Object [] { a.defaultValue });

            if (!ignoreDeclarations
                    && element.attributes.get (a.name) == null) {
                element.attributes.put (a.name, a);

                // Report attribute declaration to SAX DeclHandler
                String saxType;
                if (a.type == AttributeDecl.ENUMERATION
                        || a.type == AttributeDecl.NOTATION) {
                    StringBuffer fullType = new StringBuffer();

                    if (a.type == AttributeDecl.NOTATION) {
                        fullType.append(a.type);
                        fullType.append(" ");
                    }

                    if (a.values.length > 1) {
                        fullType.append("(");
                    }
                    for (int i = 0; i < a.values.length; i++) {
                        fullType.append(a.values[i]);
                        if (i + 1 < a.values.length) {
                            fullType.append("|");
                        }
                    }
                    if (a.values.length > 1) {
                        fullType.append(")");
                    }

                    saxType = fullType.toString();
                } else {
                    saxType = a.type;
                }
                declHandler.attributeDecl(element.name, a.name, saxType,
                                          a.valueDefault, a.defaultValue);
            }
            maybeWhitespace ();
        }
        if (supportValidation && isValidating && start != in)
            error ("V-013", null);
        return true;
    }

    // used when parsing literal attribute values,
    // or public identifiers.
    //
    // input in strTmp
    private String normalize (boolean invalidIfNeeded)
    throws SAXException
    {
        // this can allocate an extra string...

        String  s = strTmp.toString ();
        String  s2 = s.trim ();
        boolean didStrip = false;

        if (s != s2) {
            s = s2;
            s2 = null;
            didStrip = true;
        }
        strTmp = new StringBuffer ();
        for (int i = 0; i < s.length (); i++) {
            char        c = s.charAt (i);
            if (!XmlChars.isSpace (c)) {
                strTmp.append (c);
                continue;
            }
            strTmp.append (' ');
            while (++i < s.length () && XmlChars.isSpace (s.charAt (i)))
                didStrip = true;
            i--;
        }
        if (supportValidation && isValidating && isStandalone) {
            if (invalidIfNeeded && (s2 == null || didStrip))
                // XXX would like to tell the name of the attribute
                // which shouldn't have needed normalization
                error ("V-019", null);
        }
        if (didStrip)
            return strTmp.toString ();
        else
            return s;
    }

    private boolean maybeConditionalSect ()
    throws IOException, SAXException
    {
        // [61] conditionalSect ::= includeSect | ignoreSect

        if (!peek ("<!["))
            return false;

        String          keyword;
        InputEntity     start = in;

        maybeWhitespace ();

        if ((keyword = maybeGetName ()) == null)
            fatal ("P-046");
        maybeWhitespace ();
        nextChar ('[', "F-030", null);

        // [62] includeSect ::= '<![' S? 'INCLUDE' S? '['
        //                              extSubsetDecl ']]>'
        if ("INCLUDE".equals (keyword)) {
            for (;;) {
                while (in.isEOF () && in != start)
                    in = in.pop ();
                if (in.isEOF ()) {
                    if (supportValidation && isValidating)
                        error ("V-020", null);
                    in = in.pop ();
                }
                if (peek ("]]>"))
                    break;

                doLexicalPE = false;
                if (maybeWhitespace ())
                    continue;
                if (maybePEReference ())
                    continue;
                doLexicalPE = true;
                if (maybeMarkupDecl () || maybeConditionalSect ())
                    continue;

                fatal ("P-047");
            }

        // [63] ignoreSect ::= '<![' S? 'IGNORE' S? '['
        //                      ignoreSectcontents ']]>'
        // [64] ignoreSectcontents ::= Ignore ('<!['
        //                      ignoreSectcontents ']]>' Ignore)*
        // [65] Ignore ::= Char* - (Char* ('<![' | ']]>') Char*)
        } else if ("IGNORE".equals (keyword)) {
            int nestlevel = 1;
            // ignoreSectcontents
            doLexicalPE = false;
            while (nestlevel > 0) {
                char c = getc ();       // will pop input entities
                if (c == '<') {
                    if (peek ("!["))
                        nestlevel++;
                } else if (c == ']') {
                    if (peek ("]>"))
                        nestlevel--;
                } else
                    continue;
            }
        } else
            fatal ("P-048", new Object [] { keyword });
        return true;
    }


    //
    // CHAPTER 4:  Physical Structures
    //

    private boolean maybeReferenceInContent (
        ElementDecl             element,
        ElementValidator        validator
    ) throws IOException, SAXException
    {
        // [66] CharRef ::= ('&#' [0-9]+) | ('&#x' [0-9a-fA-F]*) ';'
        // [67] Reference ::= EntityRef | CharRef
        // [68] EntityRef ::= '&' Name ';'
        if (!in.peekc ('&'))
            return false;

        if (!in.peekc ('#')) {
            String      name = maybeGetName ();
            if (name == null)
                fatal ("P-009");
            nextChar (';', "F-020", name);
            expandEntityInContent (element, name, validator);
            return true;
        }

        validator.text ();
        contentHandler.characters (charTmp, 0,
                surrogatesToCharTmp (parseCharNumber ()));
        return true;
    }

    // parse decimal or hex numeric character reference
    private int parseCharNumber ()
    throws SAXException, IOException
    {
        char    c;
        int     retval = 0;

        // n.b. we ignore overflow ...
        if (getc () != 'x') {
            ungetc ();
            for (;;) {
                c = getc ();
                if (c >= '0' && c <= '9') {
                    retval *= 10;
                    retval += (c - '0');
                    continue;
                }
                if (c == ';')
                    return retval;
                fatal ("P-049");
            }
        } else for (;;) {
            c = getc ();
            if (c >= '0' && c <= '9') {
                retval <<= 4;
                retval += (c - '0');
                continue;
            }
            if (c >= 'a' && c <= 'f') {
                retval <<= 4;
                retval += 10 + (c - 'a');
                continue;
            }
            if (c >= 'A' && c <= 'F') {
                retval <<= 4;
                retval += 10 + (c - 'A');
                continue;
            }
            if (c == ';')
                return retval;
            fatal ("P-050");
        }
    }

    // parameter is a UCS-4 character ... i.e. not just 16 bit UNICODE,
    // though still subject to the 'Char' construct in XML
    private int surrogatesToCharTmp (int ucs4)
    throws SAXException
    {
        if (ucs4 <= 0xffff) {
            if (XmlChars.isChar (ucs4)) {
                charTmp [0] = (char) ucs4;
                return 1;
            } 
        } else if (ucs4 <= 0x0010ffff) {
            // we represent these as UNICODE surrogate pairs
            ucs4 -= 0x10000;
            charTmp [0] = (char) (0xd800 | ((ucs4 >> 10) & 0x03ff));
            charTmp [1] = (char) (0xdc00 | (ucs4 & 0x03ff));
            return 2;
        }
        fatal ("P-051", new Object [] { Integer.toHexString (ucs4) });
        // NOTREACHED
        return -1;
    }

    private void expandEntityInContent (
        ElementDecl             element,
        String                  name,
        ElementValidator        validator
    ) throws SAXException, IOException
    {
        Object                  entity = entities.get (name);
        InputEntity             last = in;

        if (entity == null) {
            //
            // Note:  much confusion about whether spec requires such
            // errors to be fatal in many cases, but none about whether
            // it allows "normal" errors to be unrecoverable!
            //
            fatal ("P-014", new Object [] { name });
        }

        if (entity instanceof InternalEntity) {
            InternalEntity      e = (InternalEntity) entity;

            //
            // we need to expand both entities and markup here...
            //
            if (supportValidation && isValidating
                    && isStandalone
                    && !e.isFromInternalSubset)
                error ("V-002", new Object [] { name });
            pushReader (e.buf, name, true);
            content (element, true, validator);
            if (in != last && !in.isEOF ()) {
                while (in.isInternal ())
                    in = in.pop ();
                fatal ("P-052", new Object [] { name });
            }
            lexicalHandler.endEntity(name);
            in = in.pop ();
        } else if (entity instanceof ExternalEntity) {
            ExternalEntity      e = (ExternalEntity) entity;
            if (e.notation != null)
                fatal ("P-053", new Object [] { name });

            if (supportValidation && isValidating
                    && isStandalone
                    && !e.isFromInternalSubset)
                error ("V-002", new Object [] { name });

            externalParsedEntity (element, e, validator);
        } else
            throw new InternalError (name);
    }

    private boolean maybePEReference ()
    throws IOException, SAXException
    {
        // This is the SYNTACTIC version of this construct.
        // When processing external entities, there is also
        // a LEXICAL version; see getc() and doLexicalPE.

        // [69] PEReference ::= '%' Name ';'
        if (!in.peekc ('%'))
            return false;

        String  name = maybeGetName ();
        Object  entity;

        if (name == null)
            fatal ("P-011");
        nextChar (';', "F-021", name);
        entity = params.get (name);

        if (entity instanceof InternalEntity) {
            InternalEntity      value = (InternalEntity) entity;
            pushReader (value.buf, name, false);

        } else if (entity instanceof ExternalEntity) {
            externalParameterEntity ((ExternalEntity)entity);

        } else if (entity == null) {
            //
            // NOTE:  by treating undefined parameter entities as 
            // nonfatal, we are assuming that the contradiction
            // between them being a WFC versus a VC is resolved in
            // favor of the latter.  Further, we are assuming that
            // validating parsers should behave like nonvalidating
            // ones in such a case:  ignoring further declarations.
            //
            ignoreDeclarations = true;
            if (supportValidation && isValidating)
                error ("V-022", new Object [] { name });
            else
                warning ("V-022", new Object [] { name });
        }
        return true;
    }

    private boolean maybeEntityDecl ()
    throws IOException, SAXException
    {
        // [70] EntityDecl ::= GEDecl | PEDecl
        // [71] GEDecl ::= '<!ENTITY' S       Name S EntityDef S? '>'
        // [72] PEDecl ::= '<!ENTITY' S '%' S Name S PEDEF     S? '>'
        // [73] EntityDef ::= EntityValue | (ExternalID NDataDecl?)
        // [74] PEDef     ::= EntityValue |  ExternalID
        //
        InputEntity     start = peekDeclaration ("!ENTITY");

        if (start == null)
            return false;

        String          entityName;
        SimpleHashtable defns;
        ExternalEntity  externalId;
        boolean         doStore;

        // PE expansion gets selectively turned off several places:
        // in ENTITY declarations (here), in comments, in PIs.
        
        // Here, we allow PE entities to be declared, and allows
        // literals to include PE refs without the added spaces
        // required with their expansion in markup decls.

        doLexicalPE = false;
        whitespace ("F-005");
        if (in.peekc ('%')) {
            whitespace ("F-006");
            defns = params;
        } else
            defns = entities;

        ungetc ();      // leave some whitespace
        doLexicalPE = true;
        entityName = getMarkupDeclname ("F-017", false);
        whitespace ("F-007");
        externalId = maybeExternalID ();

        //
        // first definition sticks ... e.g. internal subset PEs are used
        // to override DTD defaults.  It's also an "error" to incorrectly
        // redefine builtin internal entities, but since reporting such
        // errors is optional we only give warnings ("just in case") for
        // non-parameter entities.
        //
        doStore = (defns.get (entityName) == null);
        if (!doStore && defns == entities)
            warning ("P-054", new Object [] { entityName });
        
        // if we skipped a PE, ignore declarations since the
        // PE might have included an ovrriding declaration
        doStore &= !ignoreDeclarations;

        // internal entities
        if (externalId == null) {
            char                value [];
            InternalEntity      entity;

            doLexicalPE = false;                // "ab%bar;cd" -maybe-> "abcd"
            parseLiteral (true);
            doLexicalPE = true;
            if (doStore) {
                value = new char [strTmp.length ()];
                if (value.length != 0)
                    strTmp.getChars (0, value.length, value, 0);
                entity = new InternalEntity (entityName, value);
                entity.isPE = (defns == params);
                entity.isFromInternalSubset = !inExternalPE;
                defns.put (entityName, entity);

                // Report event
                if (defns == params) {
                    entityName = "%" + entityName;
                }
                declHandler.internalEntityDecl(entityName, new String(value));
            }

        // external entities (including unparsed)
        } else {
            // [76] NDataDecl ::= S 'NDATA' S Name
            if (defns == entities && maybeWhitespace ()
                    && peek ("NDATA")) {
                externalId.notation = getMarkupDeclname ("F-018", false);

                // flag undeclared notation for checking after
                // the DTD is fully processed
                if (supportValidation && isValidating
                        && notations.get (externalId.notation) == null)
                    notations.put (externalId.notation, Boolean.TRUE);
            }
            externalId.name = entityName;
            externalId.isPE = (defns == params);
            externalId.isFromInternalSubset = !inExternalPE;
            if (doStore) {
                defns.put (entityName, externalId);
                if (externalId.notation != null) {
                    dtdHandler.unparsedEntityDecl (entityName,
                            externalId.publicId, externalId.systemId,
                            externalId.notation);
                } else {
                    // Parsed external entity, either general or parameter
                    if (defns == params) {
                        entityName = "%" + entityName;
                    }
                    declHandler.externalEntityDecl(entityName,
                            externalId.publicId, externalId.systemId);
                }
            }
        }
        maybeWhitespace ();
        nextChar ('>', "F-031", entityName);
        if (supportValidation && isValidating && start != in)
            error ("V-013", null);
        return true;
    }

    private ExternalEntity maybeExternalID ()
        throws IOException, SAXException
    {
        // [75] ExternalID ::= 'SYSTEM' S SystemLiteral
        //              | 'PUBLIC' S' PubidLiteral S Systemliteral
        String          temp = null;
        ExternalEntity  retval;

        if (peek ("PUBLIC")) {
            whitespace ("F-009");
            temp = parsePublicId ();
        } else if (!peek ("SYSTEM"))
            return null;

        retval = new ExternalEntity (in);
        retval.publicId = temp;
        whitespace ("F-008");
        retval.verbatimSystemId = getQuotedString("F-034", null);
        retval.systemId = resolveURI(retval.verbatimSystemId);
        return retval;
    }

    private String parseSystemId()
        throws IOException, SAXException
    {
        String uri = getQuotedString("F-034", null);
        return resolveURI(uri);
    }

    private String resolveURI(String uri)
        throws SAXException
    {
        int     temp = uri.indexOf (':');

        // resolve relative URIs ... must do it here since
        // it's relative to the source file holding the URI!

        // "new java.net.URL (URL, string)" conforms to RFC 1630,
        // but we can't use that except when the URI is a URL.
        // The entity resolver is allowed to handle URIs that are
        // not URLs, so we pass URIs through with scheme intact
        if (temp == -1 || uri.indexOf ('/') < temp) {
            String      baseURI;

            baseURI = in.getSystemId ();
            if (baseURI == null)
                fatal ("P-055", new Object [] { uri });
            if (uri.length () == 0)
                uri = ".";
            baseURI = baseURI.substring (0, baseURI.lastIndexOf ('/') + 1);
            if (uri.charAt (0) != '/')
                uri = baseURI + uri;
            else {
                // We have relative URI that begins with a '/'

                // Extract scheme including colon from baseURI
                String baseURIScheme;
                int colonIndex = baseURI.indexOf(':');
                if (colonIndex == -1) {
                    // Base URI does not have a scheme so default to
                    // "file:" scheme
                    baseURIScheme = "file:";
                } else {
                    baseURIScheme = baseURI.substring(0, colonIndex + 1);
                }

                uri = baseURIScheme + uri;
            }

            // letting other code map any "/xxx/../" or "/./" to "/",
            // since all URIs must handle it the same.
        }
        // check for fragment ID in URI
        if (uri.indexOf ('#') != -1)
            error ("P-056", new Object [] { uri });
        return uri;
    }

    private void maybeTextDecl ()
    throws IOException, SAXException
    {
        // [77] TextDecl ::= '<?xml' VersionInfo? EncodingDecl S? '?>'

        if (!in.isXmlDeclOrTextDeclPrefix()) {
            return;
        }
        // Consume '<?xml'
        peek("<?xml");

        readVersion (false, "1.0");
        readEncoding (true);
        maybeWhitespace ();
        if (!peek ("?>"))
            fatal ("P-057");
    }

    // returns true except in case of nonvalidating parser which
    // chose to ignore the entity.

    private boolean externalParsedEntity (
        ElementDecl             element,
        ExternalEntity          next,
        ElementValidator        validator
    ) throws IOException, SAXException
    {
        // [78] ExtParsedEnt ::= TextDecl? content

        if (!pushReader (next)) {
            if (!isInAttribute) {
                lexicalHandler.endEntity(next.name);
            }
            return false;
        }

        maybeTextDecl ();
        content (element, true, validator);
        if (!in.isEOF ())
            fatal ("P-058", new Object [] { next.name });
        in = in.pop ();
        if (!isInAttribute) {
            lexicalHandler.endEntity(next.name);
        }
        return true;
    }

    private void externalParameterEntity (ExternalEntity next)
    throws IOException, SAXException
    {
        //
        // Reap the intended benefits of standalone declarations:
        // don't deal with external parameter entities, except to
        // validate the standalone declaration.
        //
        // XXX perhaps:  also add an option to skip reading external
        // PEs when not validating, so this behaves like the parsers
        // in Gecko and IE5.  Means setting ignoreDeclarations ...
        //
        if (isStandalone && fastStandalone)
            return;
        
        // n.b. "in external parameter entities" (and external
        // DTD subset, same grammar) parameter references can
        // occur "within" markup declarations ... expansions can
        // cross syntax rules.  Flagged here; affects getc().

        // [79] ExtPE ::= TextDecl? extSubsetDecl
        // [31] extSubsetDecl ::= ( markupdecl | conditionalSect
        //              | PEReference | S )*
        InputEntity     pe;

        inExternalPE = true;

        // Check for common case of file not found and throw a
        // SAXParseException
        try {
            // XXX if this returns false ...
            pushReader (next);
        } catch (IOException e) {
            fatal ("P-082", new Object [] { next.systemId }, e);
        }

        pe = in;

        // Check for common case of bad URL and throw a SAXParseException.
        // For bad URL case, JDK does not throw an exception when
        // URLConnection.getInputStream() is called but later when the app
        // tries to read from the stream in maybeTextDecl().
        try {
            maybeTextDecl ();
        } catch (IOException e) {
            // Pop invalid InputEntity so Locator info will be correct
            in = in.pop ();
            fatal ("P-082", new Object [] { next.systemId }, e);
        }
        while (!pe.isEOF ()) {
            // pop internal PEs (and whitespace before/after)
            if (in.isEOF ()) {
                in = in.pop ();
                continue;
            }
            doLexicalPE = false;
            if (maybeWhitespace ())
                continue;
            if (maybePEReference ())
                continue;
            doLexicalPE = true;
            if (maybeMarkupDecl () || maybeConditionalSect ())
                continue;
            break;
        }
        // if (in != pe) throw new InternalError ("who popped my PE?");
        if (!pe.isEOF ())
            fatal ("P-059", new Object [] { in.getName () });
        in = in.pop ();
        inExternalPE = !in.isDocument ();
        doLexicalPE = false;
    }

    private void readEncoding (boolean must)
    throws IOException, SAXException
    {
        // [81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*
        String name = maybeReadAttribute ("encoding", must);

        if (name == null)
            return;
        for (int i = 0; i < name.length (); i++) {
            char c = name.charAt (i);
            if ((c >= 'A' && c <= 'Z')
                    || (c >= 'a' && c <= 'z'))
                continue;
            if (i != 0
                    && ((c >= '0' && c <= '9')
                        || c == '-'
                        || c == '_'
                        || c == '.'
                        ))
                continue;
            fatal ("P-060", new Object [] { new Character (c) });
        }

        //
        // This should be the encoding in use, and it's even an error for
        // it to be anything else (in certain cases that are impractical to
        // to test, and may even be insufficient).  So, we do the best we
        // can, and warn if things look suspicious.  Note that Java doesn't
        // uniformly expose the encodings, and that the names it uses
        // internally are nonstandard.  Also, that the XML spec allows
        // such "errors" not to be reported at all.
        //
        String  currentEncoding = in.getEncoding ();

        if (currentEncoding != null
                && !name.equalsIgnoreCase (currentEncoding))
            warning ("P-061", new Object [] { name, currentEncoding });
    }

    private boolean maybeNotationDecl ()
    throws IOException, SAXException
    {
        // [82] NotationDecl ::= '<!NOTATION' S Name S
        //              (ExternalID | PublicID) S? '>'
        // [83] PublicID ::= 'PUBLIC' S PubidLiteral
        InputEntity     start = peekDeclaration ("!NOTATION");

        if (start == null)
            return false;

        String          name = getMarkupDeclname ("F-019", false);
        ExternalEntity  entity = new ExternalEntity (in);

        whitespace ("F-011");
        if (peek ("PUBLIC")) {
            whitespace ("F-009");
            entity.publicId = parsePublicId ();
            if (maybeWhitespace ()) {
                if (!peek (">"))
                    entity.systemId = parseSystemId ();
                else 
                    ungetc ();
            }
        } else if (peek ("SYSTEM")) {
            whitespace ("F-008");
            entity.systemId = parseSystemId ();
        } else
            fatal ("P-062");
        maybeWhitespace ();
        nextChar ('>', "F-032", name);
        if (supportValidation && isValidating && start != in)
            error ("V-013", null);
        if (entity.systemId != null && entity.systemId.indexOf ('#') != -1)
            error ("P-056", new Object [] { entity.systemId });

        Object  value = notations.get (name);
        if (value != null && value instanceof ExternalEntity)
            warning ("P-063", new Object [] { name });

        // if we skipped a PE, ignore declarations since the
        // PE might have included an ovrriding declaration
        else if (!ignoreDeclarations) {
            notations.put (name, entity);
            dtdHandler.notationDecl (name, entity.publicId,
                    entity.systemId);
        }
        return true;
    }


    ////////////////////////////////////////////////////////////////
    //
    //  UTILITIES
    //
    ////////////////////////////////////////////////////////////////

    private char getc () throws IOException, SAXException
    {
        if (!(inExternalPE && doLexicalPE)) {
            char c = in.getc ();
            if (c == '%' && doLexicalPE)
                fatal ("P-080");
            return c;
        }

        //
        // External parameter entities get funky processing of '%param;'
        // references.  It's not clearly defined in the XML spec; but it
        // boils down to having those refs be _lexical_ in most cases to
        // include partial syntax productions.  It also needs selective
        // enabling; "<!ENTITY % foo ...>" must work, for example, and
        // if "bar" is an empty string PE, "ab%bar;cd" becomes "abcd"
        // if it's expanded in a literal, else "ab  cd".  PEs also do
        // not expand within comments or PIs, and external PEs are only
        // allowed to have markup decls (and so aren't handled lexically).
        //
        // This PE handling should be merged into maybeWhitespace, where
        // it can be dealt with more consistently.
        //
        // Also, there are some validity constraints in this area.
        //
        char c;

        while (in.isEOF ()) {
            if (in.isInternal () || (doLexicalPE && !in.isDocument ()))
                in = in.pop ();
            else {
                fatal ("P-064", new Object [] { in.getName () });
            }
        }
        if ((c = in.getc ()) == '%' && doLexicalPE) {
            // PE ref ::= '%' name ';'
            String      name = maybeGetName ();
            Object      entity;

            if (name == null)
                fatal ("P-011");
            nextChar (';', "F-021", name);
            entity = params.get (name);

            // push a magic "entity" before and after the
            // real one, so ungetc() behaves uniformly
            pushReader (" ".toCharArray (), null, false);
            if (entity instanceof InternalEntity)
                pushReader (((InternalEntity) entity).buf, name, false);
            else if (entity instanceof ExternalEntity)
                // PEs can't be unparsed!
                // XXX if this returns false ...
                pushReader ((ExternalEntity) entity);
            else if (entity == null)
                // see note in maybePEReference re making this be nonfatal.
                fatal ("V-022");
            else
                throw new InternalError ();
            pushReader (" ".toCharArray (), null, false);
            return in.getc ();
        }
        return c;
    }

    private void ungetc () // throws IOException, SAXException
        { in.ungetc (); }

    private boolean peek (String s) throws IOException, SAXException
        { return in.peek (s, null); }
    
    // Return the entity starting the specified declaration
    // (for validating declaration nesting) else null.
    private InputEntity peekDeclaration (String s)
    throws IOException, SAXException
    {
        InputEntity     start;

        if (!in.peekc ('<'))
            return null;
        start = in;
        if (in.peek (s, null))
            return start;
        in.ungetc ();
        return null;
    }

    private void nextChar (char c, String location, String near)
    throws IOException, SAXException
    {
        while (in.isEOF () && !in.isDocument ())
            in = in.pop ();
        if (!in.peekc (c))
            fatal ("P-008", new Object []
                { new Character (c),
                    messages.getMessage (locale, location),
                    (near == null ? "" : ('"' + near + '"'))});
    }
    
    

    private void pushReader (char buf [], String name, boolean isGeneral)
    throws SAXException
    {
        if (isGeneral && !isInAttribute) {
            lexicalHandler.startEntity(name);
        }

        InputEntity     r = InputEntity.getInputEntity (errHandler, locale);
        r.init (buf, name, in, !isGeneral);
        in = r;
    }

    // returns false if the external entity is being ignored ...
    // potentially possible in nonvalidating parsers, but not
    // currently supported.  (See notes everywhere this is called;
    // both error handling, and reporting start/stop of entity
    // expansion, are issues!  Also, SAX has no way to say "don't
    // read this entity".)

    private boolean pushReader (ExternalEntity next)
    throws SAXException, IOException
    {
        if (!next.isPE && !isInAttribute) {
            lexicalHandler.startEntity(next.name);
        }

        InputEntity     r = InputEntity.getInputEntity (errHandler, locale);
        InputSource     s = next.getInputSource (resolver);

        r.init (s, next.name, in, next.isPE);
        in = r;
        return true;
    }


    // error handling convenience routines

    private void warning (String messageId, Object parameters [])
    throws SAXException
    {
        SAXParseException       x;

        x = new SAXParseException (
            messages.getMessage (locale, messageId, parameters),
            locator);

        // continuable, minor ... "this may matter to you..."
        errHandler.warning (x);
    }

    // package private ... normally returns.
    void error (String messageId, Object parameters [])
    throws SAXException
    {
        SAXParseException       x = new SAXParseException (
            messages.getMessage (locale, messageId, parameters),
            locator);

        // continuable, major ... e.g. invalid document
        errHandler.error (x);
    }

    private void fatal (String message) throws SAXException
    {
        fatal (message, null, null);
    }

    private void fatal (String message, Object parameters [])
    throws SAXException
    {
        fatal (message, parameters, null);
    }

    private void fatal (String messageId, Object parameters [], Exception e)
    throws SAXException
    {
        SAXParseException       x = new SAXParseException (
            messages.getMessage (locale, messageId, parameters),
            locator, e);
        errHandler.fatalError (x);

        // not continuable ... e.g. basic well-formedness errors
        throw x;
    }


    //
    // LOCATOR -- used for err reporting. the app calls us,
    // we tell where the parsing current event happened.
    //
    class DocLocator implements Locator {

        public String getPublicId ()
        {
            return (in == null) ? null : in.getPublicId ();
        }

        public String getSystemId ()
        {
            return (in == null) ? null : in.getSystemId ();
        }

        public int getLineNumber ()
        {
            return (in == null) ? -1 : in.getLineNumber ();
        }

        public int getColumnNumber ()
        {
            return (in == null) ? -1 : in.getColumnNumber ();
        }
    }


    //
    // Map char arrays to strings ... cuts down both on memory and
    // CPU usage for element/attribute/other names that are reused.
    //
    // Documents typically repeat names a lot, so we more or less
    // intern all the strings within the document; since some strings
    // are repeated in multiple documents (e.g. stylesheets) we go
    // a bit further, and intern globally.
    //
    static class NameCache {
        //
        // Unless we auto-grow this, the default size should be a
        // reasonable bit larger than needed for most XML files
        // we've yet seen (and be prime).  If it's too small, the
        // penalty is just excess cache collisions.
        //
        NameCacheEntry  hashtable [] = new NameCacheEntry [541];

        //
        // Usually we just want to get the 'symbol' for these chars
        //
        String lookup (char value [], int len)
        {
            return lookupEntry (value, len).name;
        }

        //
        // Sometimes we need to scan the chars in the resulting
        // string, so there's an accessor which exposes them.
        // (Mostly for element end tags.)
        //
        NameCacheEntry lookupEntry (char value [], int len)
        {
            int                 index = 0;
            NameCacheEntry      entry;

            // hashing to get index
            for (int i = 0; i < len; i++)
                index = index * 31 + value [i];
            index &= 0x7fffffff;
            index %= hashtable.length;

            // return entry if one's there ...
            for (entry = hashtable [index];
                    entry != null;
                    entry = entry.next) {
                if (entry.matches (value, len))
                    return entry;
            }

            // else create new one
            entry = new NameCacheEntry ();
            entry.chars = new char [len];
            System.arraycopy (value, 0, entry.chars, 0, len);
            entry.name = new String (entry.chars);
                //
                // NOTE:  JDK 1.1 has a fixed size string intern table,
                // with non-GC'd entries.  It can panic here; that's a
                // JDK problem, use 1.2 or later with many identifiers.
                //
            entry.name = entry.name.intern ();          // "global" intern
            entry.next = hashtable [index];
            hashtable [index] = entry;
            return entry;
        }
    }

    static class NameCacheEntry {
        String          name;
        char            chars [];
        NameCacheEntry  next;

        boolean matches (char value [], int len)
        {
            if (chars.length != len)
                return false;
            for (int i = 0; i < len; i++)
                if (value [i] != chars [i])
                    return false;
            return true;
        }
    }

    //
    // A combined handler class that does nothing
    //
    private static class NullHandler extends DefaultHandler
        implements LexicalHandler, DeclHandler
    {
        public void startDTD (String name, String publicId, String systemId) {}
        public void endDTD () {}
        public void startEntity (String name) {}
        public void endEntity (String name) {}
        public void startCDATA () {}
        public void endCDATA () {}
        public void comment (char ch[], int start, int length) {}
        public void elementDecl (String name, String model) {}
        public void attributeDecl (String eName, String aName, String type,
                                   String valueDefault, String value) {}
        public void internalEntityDecl (String name, String value) {}
        public void externalEntityDecl (String name, String publicId,
                                        String systemId) {}
    }

    //
    // Message catalog for diagnostics.
    //
    static final Catalog messages = new Catalog();

    static final class Catalog extends MessageCatalog {
        Catalog() {
            super(Parser2.class);
        }
    }
}
