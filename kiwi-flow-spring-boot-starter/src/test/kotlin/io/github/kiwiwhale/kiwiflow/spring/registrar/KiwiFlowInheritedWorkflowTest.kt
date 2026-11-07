package io.github.kiwiwhale.kiwiflow.spring.registrar

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowKey
import io.github.kiwiwhale.kiwiflow.spring.KiwiFlowTestContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class KiwiFlowInheritedWorkflowTest {

    @Workflow(name = "ParentWorkflow")
    open class ParentWorkflow {

        @Step
        open fun execute(input: String): String = "Parent $input"
    }

    class ChildWorkflow : ParentWorkflow()

    @Test
    fun `상위 클래스에 선언된 Workflow도 등록한다`() {
        KiwiFlowTestContext.create(
            "childWorkflow" to ChildWorkflow()
        ).use { test ->
            assertTrue(
                test.registry.contains("ParentWorkflow")
            )

            assertEquals(
                setOf("ParentWorkflow"),
                test.registry.keys
            )
        }
    }

    @Test
    fun `상속된 Workflow를 실행할 수 있다`() = runBlocking {
        KiwiFlowTestContext.create(
            "childWorkflow" to ChildWorkflow()
        ).use { test ->
            val workflow = test.registry.workflow(
                KiwiWorkflowKey<String, String>("ParentWorkflow")
            )

            assertEquals(
                "Parent KFlow",
                workflow.execute("KFlow")
            )
        }
    }
}
