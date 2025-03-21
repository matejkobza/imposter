/*
 * Copyright (c) 2016-2021.
 *
 * This file is part of Imposter.
 *
 * "Commons Clause" License Condition v1.0
 *
 * The Software is provided to you by the Licensor under the License, as
 * defined below, subject to the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights
 * under the License will not include, and the License does not grant to
 * you, the right to Sell the Software.
 *
 * For purposes of the foregoing, "Sell" means practicing any or all of
 * the rights granted to you under the License to provide to third parties,
 * for a fee or other consideration (including without limitation fees for
 * hosting or consulting/support services related to the Software), a
 * product or service whose value derives, entirely or substantially, from
 * the functionality of the Software. Any license notice or attribution
 * required by the License must also include this Commons Clause License
 * Condition notice.
 *
 * Software: Imposter
 *
 * License: GNU Lesser General Public License version 3
 *
 * Licensor: Peter Cornish
 *
 * Imposter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Imposter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Imposter.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.gatehill.imposter.scripting

import io.gatehill.imposter.plugin.config.resource.BasicResourceConfig
import io.gatehill.imposter.script.ResponseBehaviourType
import io.gatehill.imposter.script.ScriptUtil
import org.junit.Assert.*
import org.junit.Test

/**
 * @author Pete Cornish
 */
abstract class AbstractScriptServiceImplTest : AbstractBaseScriptTest() {
    @Test
    fun testExecuteScript_Immediate() {
        val pluginConfig = configureScript()
        val resourceConfig = pluginConfig as BasicResourceConfig

        val additionalBindings = mapOf(
            "hello" to "world"
        )
        val runtimeContext = buildRuntimeContext(additionalBindings)
        val scriptPath = ScriptUtil.resolveScriptPath(pluginConfig, resourceConfig.responseConfig.scriptFile)
        val actual = getService().executeScript(scriptPath, runtimeContext)

        assertNotNull(actual)
        assertEquals(201, actual.statusCode)
        assertEquals("foo.bar", actual.responseFile)
        assertEquals(ResponseBehaviourType.SHORT_CIRCUIT, actual.behaviourType)
        assertEquals(1, actual.responseHeaders.size)
        assertEquals("AwesomeHeader", actual.responseHeaders.get("MyHeader"))
    }

    @Test
    fun testExecuteScript_Default() {
        val pluginConfig = configureScript()
        val resourceConfig = pluginConfig as BasicResourceConfig

        val additionalBindings = mapOf(
            "hello" to "should not match"
        )
        val runtimeContext = buildRuntimeContext(additionalBindings)
        val scriptPath = ScriptUtil.resolveScriptPath(pluginConfig, resourceConfig.responseConfig.scriptFile)
        val actual = getService().executeScript(scriptPath, runtimeContext)

        assertNotNull(actual)
        // zero as un-set by script
        assertEquals(0, actual.statusCode)
        assertNull(actual.responseFile)
        assertEquals(ResponseBehaviourType.DEFAULT_BEHAVIOUR, actual.behaviourType)
    }

    @Test
    fun testExecuteScript_ParsePathParams() {
        val pluginConfig = configureScript()
        val resourceConfig = pluginConfig as BasicResourceConfig

        val additionalBindings = mapOf(
            "hello" to "world"
        )
        val pathParams = mapOf("qux" to "quux")

        val runtimeContext = buildRuntimeContext(additionalBindings, emptyMap(), pathParams, emptyMap(), emptyMap())
        val scriptPath = ScriptUtil.resolveScriptPath(pluginConfig, resourceConfig.responseConfig.scriptFile)
        val actual = getService().executeScript(scriptPath, runtimeContext)

        assertNotNull(actual)
        assertEquals(203, actual.statusCode)
        assertEquals(ResponseBehaviourType.DEFAULT_BEHAVIOUR, actual.behaviourType)
        assertEquals("quux", actual.responseHeaders.get("X-Echo-Qux"))
    }

    @Test
    fun testExecuteScript_ParseQueryParams() {
        val pluginConfig = configureScript()
        val resourceConfig = pluginConfig as BasicResourceConfig

        val additionalBindings = mapOf(
            "hello" to "world"
        )
        val queryParams = mapOf("foo" to "bar")

        val runtimeContext = buildRuntimeContext(additionalBindings, emptyMap(), emptyMap(), queryParams, emptyMap())
        val scriptPath = ScriptUtil.resolveScriptPath(pluginConfig, resourceConfig.responseConfig.scriptFile)
        val actual = getService().executeScript(scriptPath, runtimeContext)

        assertNotNull(actual)
        assertEquals(200, actual.statusCode)
        assertEquals(ResponseBehaviourType.DEFAULT_BEHAVIOUR, actual.behaviourType)
        assertEquals("bar", actual.responseHeaders.get("X-Echo-Foo"))
    }

    @Test
    fun testExecuteScript_ParseRequestHeaders() {
        val pluginConfig = configureScript()
        val resourceConfig = pluginConfig as BasicResourceConfig

        val additionalBindings = mapOf(
            "hello" to "world"
        )
        val headers = mapOf("baz" to "qux")

        val runtimeContext = buildRuntimeContext(additionalBindings, headers, emptyMap(), emptyMap(), emptyMap())
        val scriptPath = ScriptUtil.resolveScriptPath(pluginConfig, resourceConfig.responseConfig.scriptFile)
        val actual = getService().executeScript(scriptPath, runtimeContext)

        assertNotNull(actual)
        assertEquals(202, actual.statusCode)
        assertEquals(ResponseBehaviourType.DEFAULT_BEHAVIOUR, actual.behaviourType)
        assertEquals("qux", actual.responseHeaders.get("X-Echo-Baz"))
    }

    @Test
    fun testExecuteScript_ParseNormalisedRequestHeaders() {
        val pluginConfig = configureScript()
        val resourceConfig = pluginConfig as BasicResourceConfig

        val additionalBindings = mapOf(
            "hello" to "world"
        )

        // request header casing should be normalised by the script engine
        val headers = mapOf("CORGE" to "grault")

        val runtimeContext = buildRuntimeContext(additionalBindings, headers, emptyMap(), emptyMap(), emptyMap())
        val scriptPath = ScriptUtil.resolveScriptPath(pluginConfig, resourceConfig.responseConfig.scriptFile)
        val actual = getService().executeScript(scriptPath, runtimeContext)

        assertNotNull(actual)
        assertEquals(202, actual.statusCode)
        assertEquals(ResponseBehaviourType.DEFAULT_BEHAVIOUR, actual.behaviourType)
        assertEquals("grault", actual.responseHeaders.get("X-Echo-Corge"))
    }

    @Test
    fun testExecuteScript_ReadEnvironmentVariable() {
        val pluginConfig = configureScript()
        val resourceConfig = pluginConfig as BasicResourceConfig

        val additionalBindings = mapOf(
            "hello" to "world"
        )
        // override environment
        val env = mapOf(
            "example" to "foo"
        )
        val runtimeContext = buildRuntimeContext(additionalBindings, emptyMap(), emptyMap(), emptyMap(), env)
        val scriptPath = ScriptUtil.resolveScriptPath(pluginConfig, resourceConfig.responseConfig.scriptFile)
        val actual = getService().executeScript(scriptPath, runtimeContext)

        assertNotNull(actual)
        assertEquals(204, actual.statusCode)
        assertEquals(ResponseBehaviourType.DEFAULT_BEHAVIOUR, actual.behaviourType)
        assertEquals("foo", actual.responseHeaders.get("X-Echo-Env-Var"))
    }
}
