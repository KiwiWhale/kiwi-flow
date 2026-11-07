package io.github.kiwiwhale.kiwiflow.ktor.config

import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KiwiFlowProviderTest {

    private val firstWorkflow =
        KiwiFlow.workflow<String, String>("First") {
            "First $input"
        }

    private val secondWorkflow =
        KiwiFlow.workflow<String, String>("Second") {
            "Second $input"
        }

    @Test
    fun `등록된 Workflow로 RegistryProvider를 생성한다`() {
        val config = KiwiFlowConfig().apply {
            workflow(firstWorkflow)
            workflow(secondWorkflow)
        }

        val provider = config.buildProvider()
        val registry = provider.graph[
            KiwiWorkflowKey.WORKFLOW_REGISTRY
        ]

        assertEquals(2, registry.size)
        assertEquals(
            setOf("First", "Second"),
            registry.keys
        )

        assertEquals(firstWorkflow, registry["First"])
        assertEquals(secondWorkflow, registry["Second"])
    }

    @Test
    fun `Workflow가 없어도 빈 RegistryProvider를 생성한다`() {
        val provider = KiwiFlowConfig().buildProvider()
        val registry = provider.graph[
            KiwiWorkflowKey.WORKFLOW_REGISTRY
        ]

        assertTrue(registry.isEmpty)
        assertEquals(0, registry.size)
    }

    @Test
    fun `Provider 생성 이후 Config 변경은 기존 Provider에 영향을 주지 않는다`() {
        val config = KiwiFlowConfig().apply {
            workflow(firstWorkflow)
        }

        val firstProvider = config.buildProvider()

        config.workflow(secondWorkflow)

        val firstRegistry = firstProvider.graph[
            KiwiWorkflowKey.WORKFLOW_REGISTRY
        ]

        assertEquals(setOf("First"), firstRegistry.keys)
        assertEquals(1, firstRegistry.size)

        val secondRegistry = config.buildProvider().graph[
            KiwiWorkflowKey.WORKFLOW_REGISTRY
        ]

        assertEquals(
            setOf("First", "Second"),
            secondRegistry.keys
        )
    }
}
