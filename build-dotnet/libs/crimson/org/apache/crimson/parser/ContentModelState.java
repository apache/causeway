/*
 * $Id: ContentModelState.java,v 1.1 2005/04/11 23:47:43 rcm Exp $
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


/**
 * A content model state. This is basically an index into a content
 * model node, emulating an automaton with primitives to consume tokens.
 * It may create new content model states as a consequence of that
 * consumption, or modify the current state.  "Next" is used to track
 * states that are pending completion after the "current automaton"
 * completes its task.
 *
 * @see ContentModel
 * @see ValidatingParser
 *
 * @author David Brownell
 * @author Arthur van Hoff
 * @version 	$Revision: 1.1 $ 
 */
class ContentModelState
{
    private ContentModel	model;
    private boolean		sawOne;
    private ContentModelState	next;

    /**
     * Create a content model state for a content model.  When
     * the state advances to null, this automaton has finished.
     */
    ContentModelState (ContentModel model)
    {
	this (model, null);
    }

    /**
     * Create a content model state for a content model, stacking
     * a state for subsequent processing.
     */
    private ContentModelState (Object content, ContentModelState next)
    {
	this.model = (ContentModel)content;
	this.next = next;
	this.sawOne = false;
    }

    /**
     * Check if the state can be terminated.  That is, there are no more
     * tokens required in the input stream.
     * @return true if the model can terminate without further input
     */
    boolean terminate ()
    {
	switch (model.type) {
	  case '+':
	    if (!sawOne && !((ContentModel)model).empty ())
		return false;
	    // FALLTHROUGH
	  case '*':
	  case '?':
	    return (next == null) || next.terminate ();

	  case '|':
	    return model.empty () && (next == null || next.terminate ());

	  case ',':
	    ContentModel m;
	    for (m = model; (m != null) && m.empty () ; m = m.next)
		continue;
	    if (m != null)
		return false;
	    return (next == null) || next.terminate ();

	  case 0:
	    return false;
	
	  default:
	    throw new InternalError ();
	}
    }

    /**
     * Advance this state to a new state, or throw an
     * exception (use a more appropriate one?) if the
     * token is illegal at this point in the content model.
     * The current state is modified if possible, conserving
     * memory that's already been allocated.
     * @return next state after reducing a token
     */
    ContentModelState advance (String token)
    throws EndOfInputException
    {
	switch (model.type) {
	  case '+':
	  case '*':
	    if (model.first (token)) {
		sawOne = true;
		if (model.content instanceof String)
		    return this;
		return new ContentModelState (model.content, this)
			.advance (token);
	    }
	    if ((model.type == '*' || sawOne) && next != null)
		return next.advance (token);
	    break;

	  case '?':
	    if (model.first (token)) {
		if (model.content instanceof String)
		    return next;
		return new ContentModelState (model.content, next)
			.advance (token);
	    }
	    if (next != null)
		return next.advance (token);
	    break;

	  case '|':
	    for (ContentModel m = model; m != null; m = m.next) {
		if (m.content instanceof String) {
		    if (token == m.content)
			return next;
		    continue;
		}
		if (((ContentModel)m.content).first (token))
		    return new ContentModelState (m.content, next)
			.advance (token);
	    }
	    if (model.empty () && next != null)
		return next.advance (token);
	    break;

	  case ',':
	    if (model.first (token)) {
		ContentModelState	nextState;

		if (model.type == 0)
		    return next;
		if (model.next == null)
		    nextState = new ContentModelState (model.content, next);
		else {
		    nextState = new ContentModelState (model.content, this);
		    model = model.next;
		}
		return nextState.advance (token);
	    } else if (model.empty () && next != null) {
		return next.advance (token);
	    }
	    break;

	  case 0:
	    if (model.content == token)
		return next;
	    // FALLTHROUGH

	  default:
	    // FALLTHROUGH
	}
	throw new EndOfInputException ();
    }
}
