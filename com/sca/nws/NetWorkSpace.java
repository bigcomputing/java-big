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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Perform operations against workspaces on NWS servers.
 * The <code>NetWorkSpace</code> object is the basic object used to perform
 * operatations on workspaces.  Variables can be declared, created, deleted,
 * and the values of those variables can be manipulated.  The <code>store</code>
 * method puts a value into the list associated with the specified variable.
 * The <code>find</code> method returns a single value from a list.
 * Which value it returns depends on the <code>mode</code> of the variable
 * (see the <code>declare</code> method for more information on the variable
 * <code>mode</code>).  If the list is empty, the <code>find</code> method
 * will not return until a value is stored in that list.  The
 * <code>findTry</code> method works like the <code>find</code> method,
 * but doesn't wait, returning a default value instead.  The
 * <code>fetch</code> method works like the <code>find</code> method, but
 * it will also remove the value from the list.  If multiple clients are all
 * blocked on a <code>fetch</code> operation, and a value is stored into
 * that variable, the server guarantees that only one client will be able to
 * get that value.  The <code>fetchTry</code> method, not surprisingly,
 * works like the <code>fetch</code> method, but doesn't wait, returning
 * a default value instead.
 */

public class NetWorkSpace {
    /**
     * Used with the <code>declare</code> method to specify that a
     * workspace variable should be "fifo" mode.  That means that
     * <code>find</code> and <code>fetch</code> retrieve the oldest
     * value stored in the variable.
     */
    public final static String FIFO = "fifo";
    /**
     * Used with the <code>declare</code> method to specify that a
     * workspace variable should be "lifo" mode.  That means that
     * <code>find</code> and <code>fetch</code> retrieve the last value
     * stored in the variable.
     */
    public final static String LIFO = "lifo";
    /**
     * Used with the <code>declare</code> method to specify that a
     * workspace variable should be "multi" mode.  That means that
     * <code>find</code> and <code>fetch</code> retrieve values in
     * undefined, pseudo-random order.
     */
    public final static String MULTI = "multi";
    /**
     * Used with the <code>declare</code> method to specify that a
     * workspace variable should be "single" mode.  This means that any
     * value stored in the variable will overwrite any previous value.
     */
    public final static String SINGLE = "single";

    private final static int JAVA_FP = 0x07000000;
    private final static int DIRECT_STRING = 0x00000001;
    private final static String DEFAULT_WSNAME = "__default";

    protected NwsServer server;
    protected String curWs;

    /**
     * Create a NetWorkSpace object with the default name, "__default".
     * The workspace is connected to the NetWorkSpaces server at default
     * host address (localhost) and port (8765).
     *
     * @exception NwsException
     */
    public NetWorkSpace() throws NwsException {
        serverConnection(DEFAULT_WSNAME, new NwsOptions());
    }

    /**
     * Create a NetWorkSpace object with the specified name.
     * The workspace is connected to the NetWorkSpaces server at the default
     * host address (localhost) and port (8765).
     *
     * @param wsName workspace name
     * @exception NwsException
     */
    public NetWorkSpace(String wsName) throws NwsException {
        serverConnection(wsName, new NwsOptions());
    }

    /**
     * Create a NetWorkSpace object with the specified name.
     * The workspace is connected to the NetWorkSpaces server at
     * the specified host address and default port (8765).
     *
     * @param wsName workspace name
     * @param host server host name
     * @exception NwsException
     */
    public NetWorkSpace(String wsName, String host) throws NwsException {
        NwsOptions opts = new NwsOptions();
        opts.host = host;
        serverConnection(wsName, opts);
    }

    /**
     * Create a NetWorkSpace object with the specified name.
     * The workspace is connected to the NetWorkSpaces server at
     * the specified host address and port.
     *
     * @param wsName workspace name
     * @param host server host name
     * @param port server port number
     * @exception NwsException
     */
    public NetWorkSpace(String wsName, String host, int port) throws NwsException {
        NwsOptions opts = new NwsOptions();
        opts.host = host;
        opts.port = port;
        serverConnection(wsName, opts);
    }

    /**
     * Create a NetWorkSpace object with the specified name.
     * The workspace is connected to the NetWorkSpaces server at
     * the host address and port specified in the NwsOptions object.
     *
     * @param wsName workspace name
     * @param opts NwsOptions instance
     * @exception NwsException
     */
    public NetWorkSpace(String wsName, NwsOptions opts) throws NwsException {
        serverConnection(wsName, opts);
    }

    /**
     * Connect to server.
     *
     * @param wsName workspace name
     * @param opts NwsOptions object
     * @return NwsServer object
     * @exception NwsException
     */
    private void serverConnection(String wsName, NwsOptions opts)
                throws NwsException {
        curWs = wsName;

        if (opts.server == null) {
            server = new NwsServer(opts.host, opts.port);
            try {
                opts.space = this;
                if (opts.useUse)
                    server.useWs(wsName, opts);
                else
                    server.openWs(wsName, opts);
            } catch (NwsException e) {
                // close the server and re-throw the exception
                try {
                    server.close();
                } catch (NwsException ignore) {
                }
                throw e;
            }
        } else {
            server = opts.server;
        }
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        return "NetWorkSpace '" + curWs + "' [" + server.toString() + "]";
    }

    /**
     * Calls the <code>close</code> method on the associated
     * <code>NwsServer</code> object.
     *
     * @exception NwsException
     */
    public void close() throws NwsException {
        server.close();
    }

    /**
     * Declares a workspace variable to have the specified mode.
     * The mode controls the order in which values are accessed, and
     * how many values it can hold.  The legal modes are "fifo", "lifo",
     * "multi", and "single".  In the first three cases, multiple values
     * can be associated with variable.  When a value is retrieved from
     * the variable, the oldest value stored will be used
     * in 'fifo' mode, the youngest in 'lifo' mode, and a nondeterministic
     * choice will be made in 'multi' mode.  In 'single' mode, only the
     * most recent value is retained.
     *
     * @param varname workspace variable name
     * @param mode variable mode
     * @exception NwsException
     */
    public void declare(String varname, String mode) throws NwsException {
        if (!mode.equals(FIFO) && !mode.equals(LIFO) &&
            !mode.equals(MULTI) && !mode.equals(SINGLE))
            throw new IllegalArgumentException("unsupported mode: " + mode);

        String op = "declare var";
        server.writeBytes("0004");
        server.writeBytes(NwsUtils.paddedZeros(op.length()));
        server.writeBytes(op);
        server.writeBytes(NwsUtils.paddedZeros(curWs.length()));
        server.writeBytes(curWs);
        server.writeBytes(NwsUtils.paddedZeros(varname.length()));
        server.writeBytes(varname);
        server.writeBytes(NwsUtils.paddedZeros(mode.length()));
        server.writeBytes(mode);
        server.sendAll();

        int status = Integer.parseInt(new String(server.recvN(4)));
        if (status != 0)
            throw new NwsDeclarationFailedException("variable declaration failed");
    }

    /**
     * Deletes a variable from the workspace.
     *
     * @param varname name of the variable to be deleted
     * @exception NwsException
     */
    public void deleteVar(String varname) throws NwsException {
        String op = "delete var";
        server.writeBytes("0003");
        server.writeBytes(NwsUtils.paddedZeros(op.length()));
        server.writeBytes(op);
        server.writeBytes(NwsUtils.paddedZeros(curWs.length()));
        server.writeBytes(curWs);
        server.writeBytes(NwsUtils.paddedZeros(varname.length()));
        server.writeBytes(varname);
        server.sendAll();

        int status = Integer.parseInt(new String(server.recvN(4)));
        if (status != 0)
            throw new NwsOperationException("deleteVar failed");
    }

    /**
     * Returns and removes a value of a variable from a workspace.
     * If the variable has no values, the operation will not return
     * until it does.  In other words, this is a "blocking" operation.
     * <code>fetchTry</code> is the "non-blocking" version of this method.
     *
     * @param varname variable name
     * @return value associates with fetched variable
     * @exception NwsException
     * @see NetWorkSpace#fetchTry
     */
    public Object fetch(String varname) throws NwsException {
        return retrieve(varname, "fetch", null);
    }

    /**
     * Returns and removes a value of a variable from a workspace.
     * If the variable has no values, the operation will return
     * <code>null</code>.  <code>fetch</code> is the "blocking" version
     * of this method.
     *
     * @param varname name of the variable
     * @return value from workspace variable, or <code>null</code>
     * @exception NwsException
     * @see NetWorkSpace#fetch
     */
    public Object fetchTry(String varname) throws NwsException {
        return fetchTry(varname, null);
    }

    /**
     * Returns and removes a value of a variable from a workspace.
     * If the variable has no values, the operation will return
     * <code>missing</code>.  <code>fetch</code> is the "blocking"
     * version of this method.
     *
     * @param varname name of the variable
     * @param missing value to return if the varible has no values
     * @return value from workspace variable, or <code>missing</code>
     * @exception NwsException
     * @see NetWorkSpace#fetch
     */
    public Object fetchTry(String varname, Object missing) throws NwsException {
        try  {
            return retrieve(varname, "fetchTry", missing);
        } catch (NwsOperationException e) {
            return missing;
        }
    }

    /**
     * Returns a value of a variable from a workspace.
     * If the variable has no values, the operation will not return
     * until it does.  In other words, this is a "blocking" operation.
     * <code>findTry</code> is the "non-blocking" version of this method.
     * <p>
     * Note that this operation cannot be used all of the values
     * of a <code>FIFO</code> mode variable.  That must be done with
     * an <code>NwsVariable</code> returned by the
     * <code>ifind</code> method.
     *
     * @param varname name of the variable
     * @return value from the workspace variable
     * @exception NwsException
     * @see NetWorkSpace#findTry
     * @see NetWorkSpace#ifind
     */
    public Object find(String varname) throws NwsException {
        return retrieve(varname, "find", null);
    }

    /**
     * Returns a value of a variable from a workspace.
     * If the variable has no values, the operation will return
     * <code>null</code>.  <code>find</code> is the "blocking" version
     * of this method.
     * <p>
     * Note that this operation cannot be used all of the values
     * of a <code>FIFO</code> mode variable.  That must be done with
     * an <code>NwsVariable</code> returned by the
     * <code>ifindTry</code> method.
     *
     * @param varname name of the variable
     * @return value from the workspace variable, or <code>null</code>
     * @exception NwsException
     * @see NetWorkSpace#find
     * @see NetWorkSpace#ifindTry
     */
    public Object findTry(String varname) throws NwsException {
        return findTry(varname, null);
    }

    /**
     * Returns a value of a variable from a workspace.
     * If the variable has no values, the operation will return
     * <code>missing</code>.  <code>find</code> is the "blocking" version
     * of this method.
     * <p>
     * Note that this operation cannot be used all of the values
     * of a <code>FIFO</code> mode variable.  That must be done with
     * an <code>NwsVariable</code> returned by the
     * <code>ifindTry</code> method.
     *
     * @param varname name of the variable
     * @param missing value to return if the varible has no values
     * @return value from workspace variable, or <code>missing</code>
     * @exception NwsException
     * @see NetWorkSpace#find
     * @see NetWorkSpace#ifindTry
     */
    public Object findTry(String varname, Object missing) throws NwsException {
        try {
            return retrieve(varname, "findTry", missing);
        } catch (NwsOperationException e) {
            return missing;
        }
    }

    /**
     * Returns a fetch iterable for the variable varName.
     * This is only supported for <code>FIFO</code> and
     * <code>SINGLE</code> mode variables.
     *
     * @param varname name of the variable
     * @param c class elements to get
     * @return NwsVariable object representing the specified variable
     * @exception NwsException
     */
    public <E> NwsVariable<E> ifetch(String varname, Class<E> c)
                throws NwsException {
        return new NwsVariable<E>(this, varname, "ifetch");
    }

    /**
     * Returns a fetch iterable for the variable varName.
     * This is only supported for <code>FIFO</code> and
     * <code>SINGLE</code> mode variables.
     *
     * @param varname name of the variable
     * @return NwsVariable object representing the specified variable
     * @exception NwsException
     */
    public <E> NwsVariable<E> ifetch(String varname)
                throws NwsException {
        return new NwsVariable<E>(this, varname, "ifetch");
    }

    /**
     * Returns a fetchTry iterable for the variable varName.
     * This is only supported for <code>FIFO</code> and
     * <code>SINGLE</code> mode variables.
     *
     * @param varname name of the variable
     * @param c class elements to get
     * @return NwsVariable object representing the specified variable
     * @exception NwsException
     */
    public <E> NwsVariable<E> ifetchTry(String varname, Class<E> c)
                throws NwsException {
        return new NwsVariable<E>(this, varname, "ifetchTry");
    }

    /**
     * Returns a fetchTry iterable for the variable varName.
     * This is only supported for <code>FIFO</code> and
     * <code>SINGLE</code> mode variables.
     *
     * @param varname name of the variable
     * @return NwsVariable object representing the specified variable
     * @exception NwsException
     */
    public <E> NwsVariable<E> ifetchTry(String varname)
                throws NwsException {
        return new NwsVariable<E>(this, varname, "ifetchTry");
    }

    /**
     * Returns a find iterable for the variable varName.
     * This is only supported for <code>FIFO</code> and
     * <code>SINGLE</code> mode variables.
     *
     * @param varname name of the variable
     * @param c class elements to get
     * @return NwsVariable object representing the specified variable
     * @exception NwsException
     */
    public <E> NwsVariable<E> ifind(String varname, Class<E> c)
                throws NwsException {
        return new NwsVariable<E>(this, varname, "ifind");
    }

    /**
     * Returns a find iterable for the variable varName.
     * This is only supported for <code>FIFO</code> and
     * <code>SINGLE</code> mode variables.
     *
     * @param varname name of the variable
     * @return NwsVariable object representing the specified variable
     * @exception NwsException
     */
    public <E> NwsVariable<E> ifind(String varname)
                throws NwsException {
        return new NwsVariable<E>(this, varname, "ifind");
    }

    /**
     * Returns a findTry iterable for the variable varName.
     * This is only supported for <code>FIFO</code> and
     * <code>SINGLE</code> mode variables.
     *
     * @param varname name of the variable
     * @param c class elements to get
     * @return NwsVariable object representing the specified variable
     * @exception NwsException
     */
    public <E> NwsVariable<E> ifindTry(String varname, Class<E> c)
                throws NwsException {
        return new NwsVariable<E>(this, varname, "ifindTry");
    }

    /**
     * Returns a findTry iterable for the variable varName.
     * This is only supported for <code>FIFO</code> and
     * <code>SINGLE</code> mode variables.
     *
     * @param varname name of the variable
     * @return NwsVariable object representing the specified variable
     * @exception NwsException
     */
    public <E> NwsVariable<E> ifindTry(String varname)
                throws NwsException {
        return new NwsVariable<E>(this, varname, "ifindTry");
    }

    /**
     * Returns the name of the workspace.
     *
     * @return name of the workspace
     */
    public String getWsName() {
        return curWs;
    }

    /**
     * Returns the NwsServer associated with this NetWorkSpace object.
     *
     * @return NwsServer associated with this NetWorkSpace object
     */
    public NwsServer getNwsServer() {
        return server;
    }

    /**
     * Lists the variables in this workspace.
     *
     * @return listing of variables
     * @exception NwsException
     */
    public String listVars() throws NwsException {
        return listVars(curWs);
    }

    /**
     * Lists variables in the specified workspace.
     *
     * @param wsname workspace name
     * @return listing of variables
     * @exception NwsException
     */
    public String listVars(String wsname) throws NwsException {
        String op = "list vars";
        server.writeBytes("0002");
        server.writeBytes(NwsUtils.paddedZeros(op.length()));
        server.writeBytes(op);
        server.writeBytes(NwsUtils.paddedZeros(wsname.length()));
        server.writeBytes(wsname);
        server.sendAll();

        int status = Integer.parseInt(new String(server.recvN(4)));  // unused at the moment
        byte[] desc = server.recvN(20);  // unused at the moment
        byte[] cookie = server.recvN(40);  // unused at the moment
        int n = Integer.parseInt(new String(server.recvN(20)));
        byte[] listing = server.recvN(n);
        if (status != 0)
            throw new NwsOperationException("listVars failed");

        return new String(listing);
    }

    /**
     * Stores a value in the specified workspace variable.
     * If a mode has not already declared for the variable, "fifo" will
     * be used.  Note that, by default ("fifo" mode), <code>store</code>
     * is not idempotent: repeating <code>store(varname, value)</code>
     * will add additional values to the specified variable.
     *
     * @param varname name of the variable
     * @param value value to be stored
     * @exception NwsException
     * @see NetWorkSpace#declare
     */
    public void store(String varname, Object value) throws NwsException {
        if (value == null)
            throw new IllegalArgumentException("null value is not supported");

        String op = "store";

        // byte arrays are not serialized
        int desc = JAVA_FP;
        byte[] xVal;
        if (value instanceof byte[]) {
            desc |= DIRECT_STRING;
            xVal = (byte []) value;
        } else {
            xVal = serialize(value);
        }

        server.writeBytes("0005");
        server.writeBytes(NwsUtils.paddedZeros(op.length()));
        server.writeBytes(op);
        server.writeBytes(NwsUtils.paddedZeros(curWs.length()));
        server.writeBytes(curWs);
        server.writeBytes(NwsUtils.paddedZeros(varname.length()));
        server.writeBytes(varname);
        server.writeBytes(NwsUtils.paddedZeros(20));
        server.writeBytes(NwsUtils.paddedZeros(desc));
        server.writeBytes(NwsUtils.paddedZeros(xVal.length));
        server.writeBytes(xVal);
        server.sendAll();

        int status = Integer.parseInt(new String(server.recvN(4)));
        if (status != 0)
            throw new NwsOperationException("store failed");
    }

    /**
     * Returns an NwsVariable object that can be used to safely
     * and conveniently access and create values of a workspace variable.
     *
     * @param varname name of the variable
     * @param c class of the elements in the variable
     * @return NwsVariable object representing the specified variable
     * @exception NwsException
     */
    public <E> NwsVariable<E> variable(String varname, Class<E> c)
                throws NwsException {
        // This is implemented exactly the same as the ifindTry operation
        // Strange, but I think it makes sense
        return new NwsVariable<E>(this, varname, "ifindTry");
    }

    /**
     * Returns an NwsVariable object that can be used to safely
     * and conveniently access and create values of a workspace variable.
     *
     * @param varname name of the variable
     * @return NwsVariable object representing the specified variable
     * @exception NwsException
     */
    public <E> NwsVariable<E> variable(String varname)
                throws NwsException {
        // This is implemented exactly the same as the ifindTry operation
        // Strange, but I think it makes sense
        return new NwsVariable<E>(this, varname, "ifindTry");
    }

    /**
     * Helper function for fetch/find methods.
     * Retrieve value associates variable varname from the workspace.
     *
     * @param varname variable name
     * @param op operation
     * @param missing default value in case values are not found
     * @return value associates with variable var
     * @exception NwsException
     */
    private Object retrieve(String varname, String op, Object missing) throws NwsException {
        server.writeBytes("0003");
        server.writeBytes(NwsUtils.paddedZeros(op.length()));
        server.writeBytes(op);
        server.writeBytes(NwsUtils.paddedZeros(curWs.length()));
        server.writeBytes(curWs);
        server.writeBytes(NwsUtils.paddedZeros(varname.length()));
        server.writeBytes(varname);
        server.sendAll();

        int status = Integer.parseInt(new String(server.recvN(4)));
        int desc = Integer.parseInt(new String(server.recvN(20)));
        byte[] cookie = server.recvN(40);  // unused at the moment
        int n = Integer.parseInt(new String(server.recvN(20)));
        byte[] xVal = server.recvN(n);

        if (status != 0)
            throw new NwsOperationException("retrieval failed");

        // byte arrays are not been serialized
        if ((desc & DIRECT_STRING) != 0)
            return xVal;
        else if (xVal.length > 0)
            return deserialize(xVal);
        else
            return missing;
    }

    /**
     * Helper function for ifetch/ifind methods.
     * Retrieve value associates variable varname from the workspace.
     *
     * @param varname variable name
     * @param op operation (whether it's a fetch, fetchTry, find, or findTry operation)
     * @param varId part of the cookie that needs to be sent to the NWS server
     * @param valIndex part for the cookie that needs to be sent to the NWS server
     * @return IValue object
     * @exception NwsException
     */
    protected IValue iretrieve(String varname, String op, byte[] varId, int valIndex)
            throws NwsException {
        server.writeBytes("0005");
        server.writeBytes(NwsUtils.paddedZeros(op.length()));
        server.writeBytes(op);
        server.writeBytes(NwsUtils.paddedZeros(curWs.length()));
        server.writeBytes(curWs);
        server.writeBytes(NwsUtils.paddedZeros(varname.length()));
        server.writeBytes(varname);
        server.writeBytes(NwsUtils.paddedZeros(varId.length));
        server.writeBytes(varId);
        server.writeBytes(NwsUtils.paddedZeros(20));
        server.writeBytes(NwsUtils.paddedZeros(valIndex));
        server.sendAll();

        int status = Integer.parseInt(new String(server.recvN(4)));
        int desc = Integer.parseInt(new String(server.recvN(20)));
        varId = server.recvN(20);
        valIndex = Integer.parseInt(new String(server.recvN(20)));
        int n = Integer.parseInt(new String(server.recvN(20)));
        byte[] xVal = server.recvN(n);

        // byte arrays are not been serialized
        if ((desc & DIRECT_STRING) != 0)
            return new IValue(status, xVal, varId, valIndex);
        else if (xVal.length > 0)
            return new IValue(status, deserialize(xVal), varId, valIndex);
        else
            throw new NoSuchElementException("variable " + varname + " has no values");
    }

    /**
     * Serialize object.
     *
     * @param obj object to be serialized
     * @return serialized objects in a byte array.
     * @exception NwsException
     */
    private byte[] serialize(Object obj) throws NwsException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
            oos.close();
        } catch (IOException ioe) {
            throw new NwsServerException("IOException", ioe);
        }

        return baos.toByteArray();
    }

    /**
     * Deserialize byte array.
     *
     * @param ba byte array to be deserialized
     * @return deserialized object.
     * @exception NwsException
     */
    private Object deserialize(byte[] ba) throws NwsException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        Object obj = null;

        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            obj = ois.readObject();
            ois.close();
        } catch (IOException ioe) {
            throw new NwsServerException("IOException", ioe);
        } catch (ClassNotFoundException cnfe) {
            throw new NwsDeserializeException("ClassNotFoundException", cnfe);
        }

        return obj;
    }
}
