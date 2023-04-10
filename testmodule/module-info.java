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

/**
 * This module is quarantined in its own directory
 * else it wreaks havoc in the IDE
 * which assumes it's the actual module of the project
 * (dependencies to javax.tools and junit causing compilation error).
 * NB: That still happens whenever this file gets saved,
 *     which needs to be followed by a project clean.
 */
module testmodule {
}
