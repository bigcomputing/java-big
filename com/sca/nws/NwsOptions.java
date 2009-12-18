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

/**
 * Specifies options when constructing a NetWorkSpace object.
 * Current options are:
 * <ul>
 * <li> useUse
 * <li> persistent
 * <li> host
 * <li> port
 * <li> create
 * <li> server
 * </ul>
 */
public final class NwsOptions {
    /** Specifies that you only want to use the workspace, not own it. */
    public boolean useUse = false;
    /** Specifies that the workspace should be persistent. */
    public boolean persistent = false;
    /** Specifies the host name of the NWS server. */
    public String host = NwsServer.DEFAULT_HOSTNAME;
    /** Specifies the port number of the NWS server. */
    public int port = NwsServer.DEFAULT_PORT;
    /** Specifies that the workspace should be created if it doesn't exist. */
    public boolean create = true;
    /** Specifies the NwsServer object to associate with the NetWorkSpace object. */
    public NwsServer server = null;
    /** Used internally. */
    protected NetWorkSpace space = null;
}
