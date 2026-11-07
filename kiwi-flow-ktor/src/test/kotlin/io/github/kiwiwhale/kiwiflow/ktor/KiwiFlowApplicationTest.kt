package io.github.kiwiwhale.kiwiflow.ktor

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.ktor.server.application.install
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KiwiFlowApplicationTest {

    private val workflow =
        KiwiFlow.workflow<String, String>("Test") {
            input.uppercase()
        }

    @Test
    fun `등록된 Workflow를 조회한다`() = testApplication {
        application {
            install(KiwiFlowPlugin) {
                workflow(workflow)
            }

            assertTrue(kiwiFlow.contains("Test"))
            assertFalse(kiwiFlow.contains("Unknown"))

            assertEquals(1, kiwiFlow.size)
            assertFalse(kiwiFlow.isEmpty)
            assertTrue(kiwiFlow.isNotEmpty)

            assertEquals(setOf("Test"), kiwiFlow.keys)
            assertEquals(listOf(workflow), kiwiFlow.values.toList())
        }
    }
}
