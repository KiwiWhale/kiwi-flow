package io.github.kiwiwhale.kiwiflow.core.workflow

import io.github.kiwiwhale.kiwiregistry.core.Registry
import io.github.kiwiwhale.kiwiregistry.provider.RegistryProvider

/**
 * Runtime access point for registered workflows.
 *
 * Workflow data is provided by an immutable RegistryGraph
 * managed by RegistryProvider.
 */
class KiwiWorkflowRegistry(
    private val provider: RegistryProvider
) {

    private val registry: Registry<String, KiwiWorkflow<*, *>>
        get() = provider.graph[KiwiWorkflowKey.WORKFLOW_REGISTRY]

    val size: Int
        get() = registry.size

    val isEmpty: Boolean
        get() = registry.isEmpty

    val isNotEmpty: Boolean
        get() = registry.isNotEmpty

    val keys: Set<String>
        get() = registry.keys

    val values: Collection<KiwiWorkflow<*, *>>
        get() = registry.values

    operator fun contains(key: KiwiWorkflowKey<*, *>): Boolean =
        key.name in registry

    operator fun contains(name: String): Boolean =
        name in registry

    operator fun get(
        key: KiwiWorkflowKey<*, *>
    ): KiwiWorkflow<*, *> =
        registry.getOrThrow(key.name)

    operator fun get(name: String): KiwiWorkflow<*, *>? =
        registry.getOrNull(name)

    fun getOrNull(
        key: KiwiWorkflowKey<*, *>
    ): KiwiWorkflow<*, *>? =
        registry.getOrNull(key.name)

    fun getOrNull(name: String): KiwiWorkflow<*, *>? =
        registry.getOrNull(name)

    /**
     * Returns a workflow using its typed key.
     */
    @Suppress("UNCHECKED_CAST")
    fun <I, O> workflow(
        key: KiwiWorkflowKey<I, O>
    ): KiwiWorkflow<I, O> =
        registry.getOrThrow(key.name) as KiwiWorkflow<I, O>

    /**
     * Returns a workflow using its name.
     */
    fun workflow(name: String): KiwiWorkflow<*, *> =
        registry.getOrThrow(name)
}
