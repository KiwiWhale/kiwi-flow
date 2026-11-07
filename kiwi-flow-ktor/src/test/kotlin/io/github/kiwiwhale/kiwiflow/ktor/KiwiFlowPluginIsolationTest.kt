package io.github.kiwiwhale.kiwiflow.ktor

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.ktor.server.application.install
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KiwiFlowPluginIsolationTest {

    private val firstWorkflow =
        KiwiFlow.workflow<String, String>("First") {
            "First $input"
        }

    private val secondWorkflow =
        KiwiFlow.workflow<String, String>("Second") {
            "Second $input"
        }

    @Test
    fun `각 Application은 독립적인 Workflow Registry를 가진다`() {
        testApplication {
            application {
                install(KiwiFlowPlugin) {
                    workflow(firstWorkflow)
                }

                assertEquals(setOf("First"), kiwiFlow.keys)
                assertTrue(kiwiFlow.contains("First"))
                assertFalse(kiwiFlow.contains("Second"))
            }
        }

        testApplication {
            application {
                install(KiwiFlowPlugin) {
                    workflow(secondWorkflow)
                }

                assertEquals(setOf("Second"), kiwiFlow.keys)
                assertTrue(kiwiFlow.contains("Second"))
                assertFalse(kiwiFlow.contains("First"))
            }
        }
    }

    @Test
    fun `서로 다른 Application은 동일한 이름의 Workflow도 독립적으로 관리한다`() {
        val firstWorkflow =
            KiwiFlow.workflow<String, String>("Shared") {
                "First $input"
            }

        val secondWorkflow =
            KiwiFlow.workflow<String, String>("Shared") {
                "Second $input"
            }

        testApplication {
            application {
                install(KiwiFlowPlugin) {
                    workflow(firstWorkflow)
                }

                assertEquals(
                    "First Ktor",
                    kiwiFlow.workflow(
                        io.github.kiwiwhale.kiwiflow.core.workflow
                            .KiwiWorkflowKey<String, String>("Shared")
                    ).execute("Ktor")
                )
            }
        }

        testApplication {
            application {
                install(KiwiFlowPlugin) {
                    workflow(secondWorkflow)
                }

                assertEquals(
                    "Second Ktor",
                    kiwiFlow.workflow(
                        io.github.kiwiwhale.kiwiflow.core.workflow
                            .KiwiWorkflowKey<String, String>("Shared")
                    ).execute("Ktor")
                )
            }
        }
    }
}
