package io.github.kiwiwhale.kiwiflow.spring.registrar

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.spring.KiwiFlowTestContext
import kotlin.test.Test
import kotlin.test.assertFailsWith

class KiwiFlowDuplicateWorkflowTest {

    @Workflow(name = "DuplicateWorkflow")
    class FirstWorkflow {

        @Step
        fun execute(input: String) = "First $input"
    }

    @Workflow(name = "DuplicateWorkflow")
    class SecondWorkflow {

        @Step
        fun execute(input: String) = "Second $input"
    }

    @Test
    fun `서로 다른 Bean이 동일한 Workflow 이름을 사용하면 예외가 발생한다`() {
        assertFailsWith<IllegalStateException> {
            KiwiFlowTestContext.create(
                "firstWorkflow" to FirstWorkflow(),
                "secondWorkflow" to SecondWorkflow()
            )
        }
    }
}
