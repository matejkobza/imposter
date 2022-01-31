package io.gatehill.imposter.store.service

import com.jayway.jsonpath.DocumentContext
import io.gatehill.imposter.http.HttpExchange
import io.gatehill.imposter.http.HttpRequest
import io.gatehill.imposter.lifecycle.EngineLifecycleHooks
import io.gatehill.imposter.plugin.config.capture.CaptureConfig
import io.gatehill.imposter.plugin.config.capture.ItemCaptureConfig
import io.gatehill.imposter.store.model.Store
import io.gatehill.imposter.store.model.StoreFactory
import io.gatehill.imposter.util.DateTimeUtil
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicReference

class CaptureServiceImplTest {
    @Test
    fun `capture value using expression`() {
        val store = mock<Store>()
        val storeFactory = mock<StoreFactory> {
            on { getStoreByName(any(), any()) } doReturn store
        }
        val service = CaptureServiceImpl(
            storeFactory = storeFactory,
            lifecycleHooks = EngineLifecycleHooks(),
            expressionService = ExpressionServiceImpl(),
        )

        val request = mock<HttpRequest> {
            on { getHeader(eq("Correlation-ID")) } doReturn "test-id"
        }
        val httpExchange = mock<HttpExchange> {
            on { request() } doReturn request
        }

        service.captureItem(
            captureConfigKey = "correlationId",
            itemConfig = ItemCaptureConfig(
                _key = "foo",
                expression = "\${context.request.headers.Correlation-ID}",
                _store = "test",
            ),
            httpExchange = httpExchange,
            jsonPathContextHolder = AtomicReference<DocumentContext>(),
        )

        verify(store).save(eq("foo"), eq("test-id"))
    }

    @Test
    fun `generate key using expression`() {
        val store = mock<Store>()
        val storeFactory = mock<StoreFactory> {
            on { getStoreByName(any(), any()) } doReturn store
        }
        val service = CaptureServiceImpl(
            storeFactory = storeFactory,
            lifecycleHooks = EngineLifecycleHooks(),
            expressionService = ExpressionServiceImpl(),
        )

        val request = mock<HttpRequest>()
        val httpExchange = mock<HttpExchange> {
            on { request() } doReturn request
        }

        service.captureItem(
            captureConfigKey = "correlationId",
            itemConfig = ItemCaptureConfig(
                _key = "key_\${datetime.now.iso8601_date}",
                constValue = "bar",
                _store = "test",
            ),
            httpExchange = httpExchange,
            jsonPathContextHolder = AtomicReference<DocumentContext>(),
        )

        // check key name calculated correctly
        val keyName = "key_${DateTimeUtil.DATE_FORMATTER.format(LocalDate.now())}"
        verify(store).save(eq(keyName), eq("bar"))
    }

    @Test
    fun `generate store name using expression`() {
        val store = mock<Store>()
        val storeFactory = mock<StoreFactory> {
            on { getStoreByName(any(), any()) } doReturn store
        }
        val service = CaptureServiceImpl(
            storeFactory = storeFactory,
            lifecycleHooks = EngineLifecycleHooks(),
            expressionService = ExpressionServiceImpl(),
        )

        val request = mock<HttpRequest>()
        val httpExchange = mock<HttpExchange> {
            on { request() } doReturn request
        }

        service.captureItem(
            captureConfigKey = "correlationId",
            itemConfig = ItemCaptureConfig(
                _key = CaptureConfig(
                    constValue = "foo"
                ),
                constValue = "bar",
                _store = "store_\${datetime.now.iso8601_date}",
            ),
            httpExchange = httpExchange,
            jsonPathContextHolder = AtomicReference<DocumentContext>(),
        )

        verify(store).save(eq("foo"), eq("bar"))

        // check store name calculated correctly
        val storeName = "store_${DateTimeUtil.DATE_FORMATTER.format(LocalDate.now())}"
        verify(storeFactory).getStoreByName(eq(storeName), eq(false))
    }
}