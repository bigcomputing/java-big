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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Perform operations against an NWS server.
 * Operations against workspaces are performed
 * using a <code>NetWorkSpace</code> object.
 */
public class NwsServer {
    /** Default host name of NWS server. */
    public final static String DEFAULT_HOSTNAME = "localhost";
    /** Default port number of NWS server. */
    public final static int DEFAULT_PORT = 8765;

    private final static String HANDSHAKE = "1112";
    private final static String OLDPROTOCOL = "2222";

    protected String serverHost;
    protected int serverPort;
    protected Socket nwsSocket;
    protected DataOutputStream dos;
    protected DataInputStream dis;
    protected String handshake;

    /**
     * Creates a connection to the NWS server on the default hostname
     * and port.
     *
     * @exception NwsServerException
     */
    public NwsServer() throws NwsServerException {
        serverHost = DEFAULT_HOSTNAME;
        serverPort = DEFAULT_PORT;
        socketConnection();
    }

    /**
     * Creates a connection to the NWS server on the specified host and
     * default port.
     *
     * @param host host address
     * @exception NwsServerException
     */
    public NwsServer(String host) throws NwsServerException {
        serverHost = host;
        serverPort = DEFAULT_PORT;
        socketConnection();
    }

    /**
     * Creates a connection to the NWS server on the specified host and
     * port parameters.
     *
     * @param host host address
     * @param port port number
     * @exception NwsServerException
     */
    public NwsServer(String host, int port) throws NwsServerException {
        serverHost = host;
        serverPort = port;
        socketConnection();
    }

    /**
     * Makes a socket connection at the specified host address and port number.
     *
     * @param host host address
     * @param port port number
     * @exception NwsServerException
     */
    private void socketConnection() throws NwsServerException {
        try {
            try {
                nwsSocket = new Socket(serverHost, serverPort);
            } catch (UnknownHostException e) {
                throw new NwsConnectException(
                        "unable to connect to unknown host: " + serverHost, e);
            } catch (IOException e) {
                throw new NwsConnectException(
                        "unable to connect to the NWS server at " +
                        serverHost + ":" + serverPort, e);
            }

            nwsSocket.setTcpNoDelay(true);
            nwsSocket.setKeepAlive(true);

            dos = new DataOutputStream(
                    new BufferedOutputStream(nwsSocket.getOutputStream()));
            dis = new DataInputStream(
                    new BufferedInputStream(nwsSocket.getInputStream()));

            // tell the server that you support the cookie protocol
            writeBytes(HANDSHAKE);
            sendAll();

            handshake = new String(recvN(4));
            if (handshake.equals(OLDPROTOCOL))
                throw new NwsUnsupportedProtocolException("old/unsupported protocol");
        } catch (IOException ioe) {
            throw new NwsServerException("NwsServer IOException", ioe);
        }
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        return "NwsServer " + serverHost + ":" + serverPort;
    }

    /**
     * Closes the connection to the associated NWS server.
     *
     * @exception NwsServerException
     */
    public void close() throws NwsServerException {
        try {
            nwsSocket.close();
            dos.close();
            dis.close();
        } catch (IOException ioe) {
            throw new NwsServerException("IOException", ioe);
        }
    }

    /**
     * Deletes a workspace on the associated NWS server.
     *
     * @param wsname workspace name
     * @exception NwsException
     */
    public void deleteWs(String wsname) throws NwsException {
        String op = "delete ws";
        writeBytes("0002");
        writeBytes(NwsUtils.paddedZeros(op.length()));
        writeBytes(op);
        writeBytes(NwsUtils.paddedZeros(wsname.length()));
        writeBytes(wsname);
        sendAll();

        int status = Integer.parseInt(new String(recvN(4)));
        if (status != 0)
            throw new NwsOperationException("deleteWs failed");
    }

    /**
     * Returns the host name of the associated NWS server.
     *
     * @return host name of the associated NWS server.
     */
    public String getHost() {
        return serverHost;
    }

    /**
     * Returns the port number of the associated NWS server.
     *
     * @return port number of the associated NWS server.
     */
    public int getPort() {
        return serverPort;
    }

    /**
     * Lists all workspaces on the associated NWS server.
     *
     * @return listings of all workspaces
     * @exception NwsException
     */
    public String listWss() throws NwsException {
        String op = "list wss";
        writeBytes("0001");
        writeBytes(NwsUtils.paddedZeros(op.length()));
        writeBytes(op);
        sendAll();

        int status = Integer.parseInt(new String(recvN(4)));
        byte[] desc = recvN(20);  // unused at the moment
        byte[] cookie = recvN(40);  // unused at the moment
        int n = Integer.parseInt(new String(recvN(20)));
        byte[] ba = recvN(n);

        if (status != 0)
            throw new NwsOperationException("listWss failed");
        return new String(ba);
    }

    /**
     * Creates a unique temporary workspace name using the default template string,
     * '__jws__%d'. The user must then invoke openWs() or useWs() with this name
     * to create an object to access to this workspace.
     *
     * @return unique workspace name.
     * @exception NwsException
     */
    public String mktempWs() throws NwsException {
        return mktempWs("__jws__%d");
    }

    /**
     * Creates a unique temporary workspace name using a template string.
     * The user must then invoke openWs() or useWs() with this name
     * to create an object to access to this workspace.
     *
     * @param wsName template string used to generate unique workspace name
     * @return unique workspace name.
     * @exception NwsException
     */
    public String mktempWs(String wsName) throws NwsException {
        String op = "mktemp ws";
        writeBytes("0002");
        writeBytes(NwsUtils.paddedZeros(op.length()));
        writeBytes(op);
        writeBytes(NwsUtils.paddedZeros(wsName.length()));
        writeBytes(wsName);
        sendAll();

        int status = Integer.parseInt(new String(recvN(4)));
        byte[] desc = recvN(20);  // unused at the moment
        byte[] cookie = recvN(40);  // unused at the moment
        int n = Integer.parseInt(new String(recvN(20)));
        byte[] ba = recvN(n);

        if (status != 0)
            throw new NwsOperationException("mktempWs failed");
        return new String(ba);
    }

    /**
     * Creates a workspace on the NWS server, if it doesn't already exist.
     * If the workspace doesn't exist, or no one owns it, then caller will
     * claim the ownership of the workspace.
     *
     * @param wsname workspace name
     * @return options for constructing the NetWorkSpace object
     * @exception NwsException
     */
    public NetWorkSpace openWs(String wsname) throws NwsException {
        NwsOptions opts = new NwsOptions();
        return openWs(wsname, opts);
    }

    /**
     * Creates a workspace on the NWS server, if it doesn't already exist.
     * If the workspace doesn't exist, or no one owns it, then caller will
     * claim the ownership of the workspace.
     *
     * @param wsname workspace name
     * @param opts options for constructing the NetWorkSpace object
     * @return NetWorkSpace object
     * @exception NwsException
     */
    public NetWorkSpace openWs(String wsname, NwsOptions opts) throws NwsException {
        NetWorkSpace space;
        if (opts.space == null) {
            opts.server = this;
            space = new NetWorkSpace(wsname, opts);
        } else {
            space = opts.space;
        }

        String op = "open ws";
        int pid = 0;
        try {
            pid = NwsUtils.getPid();
        } catch (UnsatisfiedLinkError e) {
        }
        String owner = Integer.toString(pid);
        String p = "no";
        if (opts.persistent)
            p = "yes";
        String c = "yes";
        if (!opts.create)
            c = "no";

        writeBytes("0005");
        writeBytes(NwsUtils.paddedZeros(op.length()));
        writeBytes(op);
        writeBytes(NwsUtils.paddedZeros(wsname.length()));
        writeBytes(wsname);
        writeBytes(NwsUtils.paddedZeros(owner.length()));
        writeBytes(owner);
        writeBytes(NwsUtils.paddedZeros(p.length()));
        writeBytes(p);
        writeBytes(NwsUtils.paddedZeros(c.length()));
        writeBytes(c);
        sendAll();

        int status = Integer.parseInt(new String(recvN(4)));
        if (status != 0)
            throw new NwsNoWorkSpaceException("workspace " + wsname + " doesn't exist");
        return space;
    }

    /**
     * Creates a workspace on the NWS server, if it doesn't already exist.
     * The caller will never claim ownership of the workspace.
     *
     * @param wsname workspace name
     * @return NetWorkSpace object
     * @exception NwsException
     */
    public NetWorkSpace useWs(String wsname) throws NwsException {
        NwsOptions opts = new NwsOptions();
        return useWs(wsname, opts);
    }

    /**
     * Creates a workspace on the NWS server, if it doesn't already exist.
     * The caller will never claim ownership of the workspace.
     *
     * @param wsname workspace name
     * @param opts options for constructing the NetWorkSpace object
     * @return NetWorkSpace object
     * @exception NwsException
     */
    public NetWorkSpace useWs(String wsname, NwsOptions opts) throws NwsException {
        String op = "use ws";
        String owner = "";
        String p = "no";
        String c = "yes";
        if (!opts.create)
            c = "no";

        NetWorkSpace space = null;
        if (opts.space == null) {
            opts.server = this;
            opts.useUse = true;
            space = new NetWorkSpace(wsname, opts);
        } else {
            space = opts.space;
        }

        writeBytes("0005");
        writeBytes(NwsUtils.paddedZeros(op.length()));
        writeBytes(op);
        writeBytes(NwsUtils.paddedZeros(wsname.length()));
        writeBytes(wsname);
        writeBytes(NwsUtils.paddedZeros(owner.length()));
        writeBytes(owner);
        writeBytes(NwsUtils.paddedZeros(p.length()));
        writeBytes(p);
        writeBytes(NwsUtils.paddedZeros(c.length()));
        writeBytes(c);
        sendAll();

        int status = Integer.parseInt(new String(recvN(4)));
        if (status != 0)
            throw new NwsNoWorkSpaceException("workspace " + wsname + " doesn't exist");
        return space;
    }

    /**
     * Converts string into bytes and writes it to the NWS server.
     *
     * @param s string to be written
     * @exception NwsServerException
     */
    protected void writeBytes(String s) throws NwsServerException {
        writeBytes(s.getBytes());
    }

    /**
     * Writes byte array to the NWS server.
     *
     * @param b byte array to be written
     * @exception NwsServerException
     */
    protected void writeBytes(byte[] b) throws NwsServerException {
        try {
            dos.write(b, 0, b.length);
        } catch (IOException ioe) {
            throw new NwsServerException("IOException", ioe);
        }
    }

    /**
     * Flushes all data in the socket's output stream and sends them
     * to the NWS server.
     */
    protected void sendAll() throws NwsServerException {
        try {
            dos.flush();
        } catch (IOException ioe) {
            throw new NwsServerException("IOException", ioe);
        }
    }

    /**
     * Reads exactly n bytes from the NWS server.
     *
     * @param n number of bytes to be received
     * @return byte array
     * @exception NwsServerException
     */
    protected byte[] recvN(int n) throws NwsServerException  {
        byte[] buf = new byte[n];
        int m = 0;
        int total = 0;
        int b = 0;

        while (total < n) {
            m = n - total;
            try {
                b = dis.read(buf, total, m);
                if (b == -1)
                    throw new NwsConnectionDroppedException(
                            "NWS server connection dropped");
            } catch (IOException ioe) {
                throw new NwsServerException("IOException", ioe);
            }
            total = total + b;
        }

        return buf;
    }
}
