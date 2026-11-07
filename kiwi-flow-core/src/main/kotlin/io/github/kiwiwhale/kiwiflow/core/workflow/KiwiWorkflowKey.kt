package io.github.kiwiwhale.kiwiflow.core.workflow

import io.github.kiwiwhale.kiwiregistry.core.registryKey

/**
 * Typed identifier for a [KiwiWorkflow].
 *
 * A workflow key identifies a workflow within the workflow registry.
 *
 * @param I Workflow input type.
 * @param O Workflow output type.
 */
class KiwiWorkflowKey<I, O>(
    val name: String
) {

    init {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank."
        }
    }

    companion object {
        fun <I, O> of(name: String): KiwiWorkflowKey<I, O> = KiwiWorkflowKey(name)

        val WORKFLOW_REGISTRY =
            registryKey<String, KiwiWorkflow<*, *>>(
                "kiwi-flow",
                "workflows"
            )
    }

    override fun equals(other: Any?): Boolean =
        this === other ||
                (other is KiwiWorkflowKey<*, *> && name == other.name)

    override fun hashCode(): Int =
        name.hashCode()

    override fun toString(): String =
        "KiwiWorkflowKey(name='$name')"
}
