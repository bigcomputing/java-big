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

class NwsUtils {
    static private String twentyZeros = "00000000000000000000";

    private NwsUtils() {
    }

    /**
     * Get the process id.  This is currently phony to avoid requiring
     * a native method.
     *
     * @return 0
     */
    static int getPid() {
        return 0;
    }

    /**
     * Return number in string format. The string is padded with zeros
     * to make up length of 20.
     *
     * @param num
     * @return number in string format
     */
    static String paddedZeros(int num) {
        String numStr = Integer.toString(num);
        String str = twentyZeros.substring(0, 20 - numStr.length()) + numStr;
        return str;
    }
}
