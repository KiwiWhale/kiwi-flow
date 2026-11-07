package io.github.kiwiwhale.kiwiflow.spring

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowKey
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import org.aopalliance.intercept.MethodInterceptor
import org.springframework.aop.framework.ProxyFactory

class KiwiFlowAopIntegrationTest {

    @Workflow(name = "AopWorkflow")
    open class AopWorkflow {

        @Step
        open fun execute(input: String): String = "Hello $input"
    }

    @Test
    fun `Spring AOP Proxy를 통해 Workflow를 실행한다`() = runBlocking {
        val invocationCount = AtomicInteger(0)

        val proxyFactory = ProxyFactory(AopWorkflow()).apply {
            isProxyTargetClass = true

            addAdvice(
                MethodInterceptor { invocation ->
                    invocationCount.incrementAndGet()
                    invocation.proceed()
                }
            )
        }

        val proxy = proxyFactory.proxy as AopWorkflow

        KiwiFlowTestContext.create(
            "aopWorkflow" to proxy
        ).use { test ->
            val workflow = test.registry.workflow(
                KiwiWorkflowKey<String, String>("AopWorkflow")
            )

            assertEquals(
                "Hello KFlow",
                workflow.execute("KFlow")
            )

            assertEquals(
                1,
                invocationCount.get()
            )
        }
    }
}
