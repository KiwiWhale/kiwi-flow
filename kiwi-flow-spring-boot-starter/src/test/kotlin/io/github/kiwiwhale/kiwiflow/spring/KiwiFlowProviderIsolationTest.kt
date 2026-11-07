package io.github.kiwiwhale.kiwiflow.spring

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KiwiFlowProviderIsolationTest {

    @Workflow(name = "ApplicationAWorkflow")
    class ApplicationAWorkflow {

        @Step
        fun execute(input: String) = "A: $input"
    }

    @Workflow(name = "ApplicationBWorkflow")
    class ApplicationBWorkflow {

        @Step
        fun execute(input: String) = "B: $input"
    }

    @Test
    fun `서로 다른 Application은 Workflow Registry를 공유하지 않는다`() {
        KiwiFlowTestContext.create(
            "workflowA" to ApplicationAWorkflow()
        ).use { applicationA ->

            KiwiFlowTestContext.create(
                "workflowB" to ApplicationBWorkflow()
            ).use { applicationB ->

                assertTrue(
                    applicationA.registry.contains("ApplicationAWorkflow")
                )

                assertFalse(
                    applicationA.registry.contains("ApplicationBWorkflow")
                )

                assertTrue(
                    applicationB.registry.contains("ApplicationBWorkflow")
                )

                assertFalse(
                    applicationB.registry.contains("ApplicationAWorkflow")
                )
            }
        }
    }

    @Test
    fun `한 Application의 Registry 변경은 다른 Application에 영향을 주지 않는다`() {
        KiwiFlowTestContext.create(
            "workflowA" to ApplicationAWorkflow()
        ).use { applicationA ->

            KiwiFlowTestContext.create(
                "workflowB" to ApplicationBWorkflow()
            ).use { applicationB ->

                val originalKeys = applicationB.registry.keys

                assertEquals(
                    setOf("ApplicationBWorkflow"),
                    originalKeys
                )

                assertEquals(
                    setOf("ApplicationAWorkflow"),
                    applicationA.registry.keys
                )

                assertEquals(
                    originalKeys,
                    applicationB.registry.keys
                )
            }
        }
    }
}
