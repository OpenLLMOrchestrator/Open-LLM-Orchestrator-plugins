/*
 * Copyright 2026 Open LLM Orchestrator contributors.
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
package com.openllmorchestrator.olo;

/**
 * Structural role when a plugin is used in a GROUP.
 * CAPABILITY_STAGE = normal stage under a capability; CONDITION = group IF;
 * ITERATOR = group loop; FORK/JOIN = ASYNC group fork/join.
 *
 * @see <a href="https://github.com/Open-LLM-Orchestrator/Open-LLM-Orchestrator-Configuration/blob/main/docs/plugin-scope-schema.md">Plugin scope schema</a>
 */
public enum PluginRole {
    CAPABILITY_STAGE, CONDITION, ITERATOR, FORK, JOIN
}
