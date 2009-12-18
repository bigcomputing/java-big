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

import java.io.UnsupportedEncodingException;

/**
 * Utility that translates Java objects to strings for the web interface.
 */
public final class Babelfish {
    private static final int MAXLEN = 1000;

    private Babelfish() {
    }

    public static void main(String[] args)
            throws UnsupportedEncodingException, NwsException {
        NetWorkSpace nws = new NetWorkSpace("Java babelfish");

        while (true) {
            String t;
            try {
                Object v = nws.fetch("food");
                t = v.toString();
                if (t.length() > MAXLEN)
                    t = t.substring(0, MAXLEN) +  "[WARNING: output truncated]";
                else if (t.length() == 0)
                    t = "\"\"";
            } catch (NwsDeserializeException e) {
                t = e.getMessage();
                if (t == null)
                    t = "[Error: unable to deserialize object]";
            }
            nws.store("doof", t.getBytes("UTF-8"));
        }
    }
}
