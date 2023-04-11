/*
 * Copyright 2023 Jeff Hain
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.jadecy.parsing.tests;

public sealed class SeImpl2 implements SealedInterface
permits SeImpl21, SeImpl22, SeImpl23,
SeImpl2.SeImpl2NestedPub, SeImpl2.SeImpl2NestedPp {
    public static final class SeImpl2NestedPub extends SeImpl2 {
    }
    static final class SeImpl2NestedPp extends SeImpl2 {
    }
    public void depToPermittedInApi(SeImpl21 permitted) {
        if (permitted.hashCode() == 0) {
            throw new AssertionError();
        }
        depToPermittedInNonApi(null);
    }
    private void depToPermittedInNonApi(SeImpl22 permitted) {
        if (permitted == null) {
            throw new AssertionError();
        }
    }
    public void depToPermittedInMethodCode(int a) {
        a += SeImpl23.class.hashCode();
        if (a == 0) {
            throw new AssertionError();
        }
    }
}
