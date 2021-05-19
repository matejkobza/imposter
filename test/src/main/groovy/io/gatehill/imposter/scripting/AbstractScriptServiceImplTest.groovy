package io.gatehill.imposter.scripting

import com.google.inject.Guice
import io.gatehill.imposter.plugin.config.PluginConfig
import io.gatehill.imposter.plugin.config.PluginConfigImpl
import io.gatehill.imposter.plugin.config.resource.ResourceConfig
import io.gatehill.imposter.script.ResponseBehaviourType
import io.gatehill.imposter.script.RuntimeContext
import io.gatehill.imposter.script.ScriptUtil
import io.gatehill.imposter.service.ScriptService
import io.vertx.core.http.CaseInsensitiveHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerRequest
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.junit.Before
import org.junit.Test

import java.nio.file.Paths

import static org.junit.Assert.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
abstract class AbstractScriptServiceImplTest {
    @Before
    void setUp() throws Exception {
        Guice.createInjector().injectMembers(this)
    }

    protected abstract ScriptService getService()

    protected abstract String getScriptName()

    protected PluginConfig configureScript() {
        def script = Paths.get(AbstractScriptServiceImplTest.class.getResource("/script/${scriptName}").toURI())

        def config = new PluginConfigImpl()
        config.with {
            parentDir = script.parent.toFile()
            responseConfig.with {
                scriptFile = scriptName
            }
        }
        config
    }

    protected RuntimeContext buildRuntimeContext(
            Map<String, String> additionalBindings,
            Map<String, String> headers = [:],
            Map<String, String> params = [:]
    ) {
        def logger = LogManager.getLogger('script-engine-test')

        def mockRequest = mock(HttpServerRequest.class)
        when(mockRequest.method()).thenReturn(HttpMethod.GET)
        when(mockRequest.absoluteURI()).thenReturn('/example')
        when(mockRequest.headers()).thenReturn(new CaseInsensitiveHeaders().addAll(headers))
        when(mockRequest.params()).thenReturn(new CaseInsensitiveHeaders().addAll(params))

        def mockRoutingContext = mock(RoutingContext.class)
        when(mockRoutingContext.request()).thenReturn(mockRequest)
        when(mockRoutingContext.getBodyAsString()).thenReturn('')

        def executionContext = ScriptUtil.buildContext(mockRoutingContext, null)
        def runtimeContext = new RuntimeContext(logger, null, additionalBindings, executionContext)
        runtimeContext
    }

    @Test
    void testExecuteScript_Immediate() throws Exception {
        def config = configureScript()
        def pluginConfig = config as PluginConfig
        def resourceConfig = config as ResourceConfig

        def additionalBindings = [
                'hello': 'world'
        ]
        def runtimeContext = buildRuntimeContext(additionalBindings)
        def actual = service.executeScript(pluginConfig, resourceConfig, runtimeContext)

        assertNotNull actual
        assertEquals 201, actual.statusCode
        assertEquals 'foo.bar', actual.responseFile
        assertEquals ResponseBehaviourType.IMMEDIATE_RESPONSE, actual.behaviourType
        assertEquals 1, actual.getResponseHeaders().size()
        assertTrue actual.getResponseHeaders().containsKey("MyHeader")
        assertEquals "AwesomeHeader", actual.getResponseHeaders().get("MyHeader")
    }

    @Test
    void testExecuteScript_Default() throws Exception {
        def config = configureScript()
        def pluginConfig = config as PluginConfig
        def resourceConfig = config as ResourceConfig

        def additionalBindings = [
                'hello': 'should not match'
        ]
        def runtimeContext = buildRuntimeContext(additionalBindings)
        def actual = service.executeScript(pluginConfig, resourceConfig, runtimeContext)

        assertNotNull actual
        assertEquals 200, actual.statusCode
        assertNull actual.responseFile
        assertEquals ResponseBehaviourType.DEFAULT_BEHAVIOUR, actual.behaviourType
    }

    @Test
    void testExecuteScript_ParseRequestParams() throws Exception {
        def config = configureScript()
        def pluginConfig = config as PluginConfig
        def resourceConfig = config as ResourceConfig

        def additionalBindings = [
                'hello': 'world'
        ]
        def params = ['foo': 'bar']

        RuntimeContext runtimeContext = buildRuntimeContext(additionalBindings, [:], params)
        def actual = service.executeScript(pluginConfig, resourceConfig, runtimeContext)

        assertNotNull actual
        assertEquals 200, actual.statusCode
        assertEquals ResponseBehaviourType.DEFAULT_BEHAVIOUR, actual.behaviourType
        assertTrue actual.getResponseHeaders().containsKey('X-Echo-Foo')
        assertEquals 'bar', actual.getResponseHeaders().get('X-Echo-Foo')
    }

    @Test
    void testExecuteScript_ParseRequestHeaders() throws Exception {
        def config = configureScript()
        def pluginConfig = config as PluginConfig
        def resourceConfig = config as ResourceConfig

        def additionalBindings = [
                'hello': 'world'
        ]
        def headers = ['baz': 'qux']

        RuntimeContext runtimeContext = buildRuntimeContext(additionalBindings, headers, [:])
        def actual = service.executeScript(pluginConfig, resourceConfig, runtimeContext)

        assertNotNull actual
        assertEquals 202, actual.statusCode
        assertEquals ResponseBehaviourType.DEFAULT_BEHAVIOUR, actual.behaviourType
        assertTrue actual.getResponseHeaders().containsKey('X-Echo-Baz')
        assertEquals 'qux', actual.getResponseHeaders().get('X-Echo-Baz')
    }
}