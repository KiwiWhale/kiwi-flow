package io.github.kiwiwhale.kiwiflow.spring.registrar

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.spring.KiwiFlowTestContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KiwiFlowBeanRegistrarTest {

    @Workflow(name = "TestWorkflow")
    class TestWorkflow {

        @Step
        fun execute(input: String) = input
    }

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @Workflow(name = "MetaWorkflow")
    annotation class MetaWorkflow

    @MetaWorkflow
    class MetaTestWorkflow {

        @Step
        fun execute(input: String) = "Meta $input"
    }

    class PlainBean

    @Test
    fun `Workflow 어노테이션이 있는 Bean을 등록한다`() {
        KiwiFlowTestContext.create(
            "testWorkflow" to TestWorkflow()
        ).use { test ->
            assertEquals(
                setOf("TestWorkflow"),
                test.registry.keys
            )
        }
    }

    @Test
    fun `Meta annotation으로 선언된 Workflow를 등록한다`() {
        KiwiFlowTestContext.create(
            "metaWorkflow" to MetaTestWorkflow()
        ).use { test ->
            assertEquals(
                setOf("MetaWorkflow"),
                test.registry.keys
            )
        }
    }

    @Test
    fun `Workflow annotation이 없는 Bean은 무시한다`() {
        KiwiFlowTestContext.create(
            "plainBean" to PlainBean()
        ).use { test ->
            assertTrue(test.registry.isEmpty)
        }
    }

    @Test
    fun `하나의 Bean에 선언된 여러 Workflow를 등록한다`() {
        KiwiFlowTestContext.create(
            "multiWorkflow" to MultiWorkflow()
        ).use { test ->
            assertEquals(
                setOf("FirstWorkflow", "SecondWorkflow"),
                test.registry.keys
            )
        }
    }

    @FirstWorkflow
    @SecondWorkflow
    class MultiWorkflow {

        @Step
        fun execute(input: String) = input
    }

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @Workflow(name = "FirstWorkflow")
    annotation class FirstWorkflow

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @Workflow(name = "SecondWorkflow")
    annotation class SecondWorkflow
}
