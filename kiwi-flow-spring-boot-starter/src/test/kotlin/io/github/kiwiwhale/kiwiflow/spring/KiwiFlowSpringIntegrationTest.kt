package io.github.kiwiwhale.kiwiflow.spring

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowKey
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowRegistry
import io.github.kiwiwhale.kiwiflow.spring.annotation.EnableKiwiFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.getBean
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

class KiwiFlowSpringIntegrationTest {

    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(TestApplication::class.java)

    @Test
    fun `Workflow Bean들이 자동 등록된다`() {
        contextRunner.run { context ->
            val registry = context.getBean<KiwiWorkflowRegistry>()

            assertTrue(registry.contains(TEST_WORKFLOW))
            assertTrue(registry.contains(META_WORKFLOW))
            assertTrue(registry.contains(DEFAULT_NAME_WORKFLOW))
        }
    }

    @Test
    fun `등록된 Workflow를 실행할 수 있다`() {
        contextRunner.run { context ->
            val result = runBlocking {
                context.workflow(TEST_WORKFLOW).execute("KFlow")
            }

            assertEquals("Hello KFlow", result)
        }
    }

    @Test
    fun `Meta annotation으로 선언된 Workflow를 실행할 수 있다`() {
        contextRunner.run { context ->
            val result = runBlocking {
                context.workflow(META_WORKFLOW).execute("KFlow")
            }

            assertEquals("Meta KFlow", result)
        }
    }

    @Test
    fun `Workflow 이름이 없으면 클래스 이름으로 등록된다`() {
        contextRunner.run { context ->
            val result = runBlocking {
                context.workflow(DEFAULT_NAME_WORKFLOW).execute("KFlow")
            }

            assertEquals("KFlow", result)
        }
    }

    @Test
    fun `Workflow annotation이 없는 Bean은 등록되지 않는다`() {
        contextRunner.run { context ->
            val registry = context.getBean<KiwiWorkflowRegistry>()

            assertFalse(registry.contains(PLAIN_BEAN))
        }
    }

    private fun ApplicationContext.workflow(name: String) =
        getBean<KiwiWorkflowRegistry>().workflow(
            KiwiWorkflowKey<String, String>(name)
        )

    @SpringBootConfiguration
    @EnableKiwiFlow
    @Import(WorkflowConfiguration::class)
    class TestApplication

    class WorkflowConfiguration {

        @Bean
        fun testWorkflow() = TestWorkflow()

        @Bean
        fun metaTestWorkflow() = MetaTestWorkflow()

        @Bean
        fun defaultNameWorkflow() = DefaultNameWorkflow()

        @Bean
        fun plainBean() = PlainBean()
    }

    @Workflow(name = TEST_WORKFLOW)
    class TestWorkflow {

        @Step(order = 1)
        fun execute(input: String): String = "Hello $input"
    }

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @Workflow(name = META_WORKFLOW)
    annotation class MetaWorkflow

    @MetaWorkflow
    class MetaTestWorkflow {

        @Step(order = 1)
        fun execute(input: String): String = "Meta $input"
    }

    @Workflow
    class DefaultNameWorkflow {

        @Step(order = 1)
        fun execute(input: String): String = input
    }

    class PlainBean

    private companion object {
        const val TEST_WORKFLOW = "TestWorkflow"
        const val META_WORKFLOW = "MetaWorkflow"
        const val DEFAULT_NAME_WORKFLOW = "DefaultNameWorkflow"
        const val PLAIN_BEAN = "PlainBean"
    }
}
