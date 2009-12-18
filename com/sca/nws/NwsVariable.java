//
// Copyright (c) 2007-2008, REvolution Computing, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.sca.nws;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Allows iteration through values of workspace variables.
 * This is particularly useful for non-destructive iteration.
 * For this purpose, use one the <code>ifetch</code>, <code>ifetchTry</code>,
 * <code>ifind</code>, or <code>ifindTry</code> methods of the NetWorkSpace
 * class to create an instance of this class.
 * <p>
 * This also provides generic versions of the <code>store</code>,
 * <code>find</code>, <code>findTry</code>, <code>fetch</code>,
 * and <code>fetchTry</code> methods of the NetWorkSpace class.
 * For this purpose, you can use the <code>variable</code> method of the
 * NetWorkSpace class to create an instance of this class, although
 * you can use any method that returns a NwsVariable.
 */
public class NwsVariable<E> implements Iterator<E>, Iterable<E> {
    private final NetWorkSpace ws;
    private final String varName;
    private final String op;
    private byte[] varId;
    private int valIndex;
    private IValue ival;
 
    NwsVariable(NetWorkSpace ws, String varName, String op) {
        this.ws = ws;
        this.varName = varName;
        this.op = op;
        varId = new byte[0];
        valIndex = 0;
        ival = null;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        return "NwsVariable '" + varName + "' [" + ws.toString() + "]";
    }

    /**
     * Returns true if the iteration has more elements.
     *
     * @return true if the iteration has more elements
     */
    public boolean hasNext() {
        assert op.equals("ifind") || op.equals("ifindTry") ||
                    op.equals("ifetch") || op.equals("ifetchTry");

        if (ival == null) {
            try {
                ival = ws.iretrieve(varName, op, varId, valIndex);
                return true;
            } catch (NoSuchElementException e) {
                return false;
            } catch (NwsException nwse) {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     */
    public E next() {
        if (!hasNext())
            throw new NoSuchElementException("variable " + varName + " has no values");

        if (ival.status != 0)
            throw new NoSuchElementException("retrieval failed");

        varId = ival.varId;
        valIndex = ival.valIndex;

        // I think we're stuck with this unchecked assignment
        // Ultimately, we can't check what goes into it
        E val = (E) ival.val;
        ival = null;
        return val;
    }

    /**
     * Throws an UnsupportedOperationException.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an iterator over the elements of the variable.
     *
     * @return an iterator over the elements of the variable
     */
    public Iterator<E> iterator() {
        // I think this is safer/better than returning 'this'
        return new NwsVariable(ws, varName, op);
    }

    /**
     * Resets the state of the iterator.
     * This allows previous values to be read again.
     */
    public void reset() {
        varId = new byte[0];
        valIndex = 0;
        ival = null;
    }

    /**
     * Stores a value in this workspace variable.
     * This provides a safe and convenient method of storing
     * values.
     *
     * @param value value to be stored
     * @exception NwsException
     * @see NetWorkSpace#store
     */
    public void store(E value) throws NwsException {
        ws.store(varName, value);
    }

    /**
     * Returns and removes a value of this variable.
     * If the variable has no values, the operation will not return
     * until it does.  In other words, this is a "blocking" operation.
     * <code>fetchTry</code> is the "non-blocking" version of this method.
     *
     * @return the appropriate value from this variable
     * @exception NwsException
     * @see NwsVariable#fetchTry
     * @see NetWorkSpace#fetch
     */
    public E fetch() throws NwsException {
        return (E) ws.fetch(varName);
    }

    /**
     * Returns and removes a value of this variable.
     * If the variable has no values, the operation will return
     * <code>null</code>.  <code>fetch</code> is the "blocking" version
     * of this method.
     *
     * @return the appropriate value from this variable
     * @exception NwsException
     * @see NwsVariable#fetch
     * @see NetWorkSpace#fetchTry
     */
    public E fetchTry() throws NwsException {
        return (E) ws.fetchTry(varName);
    }

    /**
     * Returns and removes a value of this variable.
     * If the variable has no values, the operation will return
     * <code>missing</code>.  <code>fetch</code> is the "blocking" version
     * of this method.
     *
     * @return the appropriate value from this variable
     * @exception NwsException
     * @see NwsVariable#fetch
     * @see NetWorkSpace#fetchTry
     */
    public E fetchTry(E missing) throws NwsException {
        return (E) ws.fetchTry(varName, missing);
    }

    /**
     * Returns a value of this variable.
     * If the variable has no values, the operation will not return
     * until it does.  In other words, this is a "blocking" operation.
     * <code>findTry</code> is the "non-blocking" version of this method.
     *
     * @return the appropriate value from this variable
     * @exception NwsException
     * @see NwsVariable#findTry
     * @see NetWorkSpace#find
     */
    public E find() throws NwsException {
        return (E) ws.find(varName);
    }

    /**
     * Returns a value of this variable.
     * If the variable has no values, the operation will return
     * <code>null</code>.  <code>find</code> is the "blocking" version
     * of this method.
     *
     * @return the appropriate value from this variable
     * @exception NwsException
     * @see NwsVariable#find
     * @see NetWorkSpace#findTry
     */
    public E findTry() throws NwsException {
        return (E) ws.findTry(varName);
    }

    /**
     * Returns a value of this variable.
     * If the variable has no values, the operation will return
     * <code>missing</code>.  <code>find</code> is the "blocking" version
     * of this method.
     *
     * @return the appropriate value from this variable
     * @exception NwsException
     * @see NwsVariable#find
     * @see NetWorkSpace#findTry
     */
    public E findTry(E missing) throws NwsException {
        return (E) ws.findTry(varName, missing);
    }

    /**
     * Declares this variable to have the specified mode.
     * The mode controls the order in which values are accessed, and
     * how many values it can hold.  The legal modes are "fifo", "lifo",
     * "multi", and "single".  In the first three cases, multiple values
     * can be associated with variable.  When a value is retrieved from
     * the variable, the oldest value stored will be used
     * in 'fifo' mode, the youngest in 'lifo' mode, and a nondeterministic
     * choice will be made in 'multi' mode.  In 'single' mode, only the
     * most recent value is retained.
     *
     * @param mode variable mode
     * @exception NwsException
     * @see NetWorkSpace#declare
     */
    public void declare(String mode) throws NwsException {
        ws.declare(varName, mode);
    }

    /**
     * Deletes this variable from the workspace.
     *
     * @exception NwsException
     * @see NetWorkSpace#deleteVar
     */
    public void delete() throws NwsException {
        ws.deleteVar(varName);
    }

    /**
     * Returns this variable's name.
     *
     * @return this variable's name
     */
    public String getName() {
        return varName;
    }

    /**
     * Returns the associated NetWorkSpace object.
     *
     * @return the associated NetWorkSpace object
     */
    public NetWorkSpace getWs() {
        return ws;
    }
}
