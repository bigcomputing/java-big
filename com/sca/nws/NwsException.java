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
 * NwsException is the top level class thrown by classes in
 * the com.sca.nws package.
 */
public class NwsException extends Exception {
    public NwsException(String message) {
        super(message);
    }

    public NwsException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
