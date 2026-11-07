package io.github.kiwiwhale.kiwiflow.ktor.config

import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowKey
import io.github.kiwiwhale.kiwiregistry.builder.RegistryContributor
import io.github.kiwiwhale.kiwiregistry.dsl.registryGraph
import io.github.kiwiwhale.kiwiregistry.provider.RegistryProvider

/**
 * Configuration for registering workflows in a Ktor application.
 *
 * Workflow definitions are collected during configuration
 * and submitted to kiwi-registry during application initialization.
 */
class KiwiFlowConfig {

    private val workflows = LinkedHashMap<String, KiwiWorkflow<*, *>>()

    /**
     * Registers a workflow.
     */
    fun workflow(workflow: KiwiWorkflow<*, *>) {
        registerWorkflow(workflow)
    }

    /**
     * Registers a workflow using an explicit typed key.
     */
    fun workflow(
        key: KiwiWorkflowKey<*, *>,
        workflow: KiwiWorkflow<*, *>
    ) {
        require(key == workflow.key) {
            "Workflow key '${key.name}' does not match workflow key '${workflow.name}'."
        }

        registerWorkflow(workflow)
    }

    /**
     * Registers a workflow using its name.
     */
    fun workflow(
        name: String,
        workflow: KiwiWorkflow<*, *>
    ) {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank."
        }

        require(name == workflow.name) {
            "Workflow name '$name' does not match workflow name '${workflow.name}'."
        }

        registerWorkflow(workflow)
    }

    /**
     * Registers multiple workflows atomically.
     */
    fun workflows(workflows: Collection<KiwiWorkflow<*, *>>) {
        val names = HashSet<String>(workflows.size)

        for (workflow in workflows) {
            check(names.add(workflow.name)) {
                "Duplicate workflow name '${workflow.name}' in input."
            }

            check(workflow.name !in this.workflows) {
                "Workflow '${workflow.name}' is already registered."
            }
        }

        workflows.forEach { workflow ->
            this.workflows[workflow.name] = workflow
        }
    }

    /**
     * Registers multiple workflows using typed keys.
     */
    fun workflowsByKey(
        workflows: Map<KiwiWorkflowKey<*, *>, KiwiWorkflow<*, *>>
    ) {
        workflows.forEach { (key, workflow) ->
            require(key == workflow.key) {
                "Workflow key '${key.name}' does not match workflow key '${workflow.name}'."
            }
        }

        this.workflows(workflows.values)
    }

    /**
     * Registers multiple workflows using names.
     */
    fun workflowsByName(
        workflows: Map<String, KiwiWorkflow<*, *>>
    ) {
        workflows.forEach { (name, workflow) ->
            require(name.isNotBlank()) {
                "Workflow name cannot be blank."
            }

            require(name == workflow.name) {
                "Workflow name '$name' does not match workflow name '${workflow.name}'."
            }
        }

        this.workflows(workflows.values)
    }

    /**
     * Returns a snapshot of registered workflows mapped by typed key.
     */
    fun registeredWorkflows():
            Map<KiwiWorkflowKey<*, *>, KiwiWorkflow<*, *>> =
        workflows.values.associateBy { it.key }

    /**
     * Builds a RegistryProvider containing all registered workflows.
     */
    internal fun buildProvider(): RegistryProvider {
        val entries = LinkedHashMap(workflows)

        val contributor = RegistryContributor("ktor-workflows") {
            registry(KiwiWorkflowKey.WORKFLOW_REGISTRY) {
                entries.forEach { (name, workflow) ->
                    add(name, workflow)
                }
            }
        }

        val graph = registryGraph {
            contribute(contributor)
        }

        return RegistryProvider(graph)
    }

    private fun registerWorkflow(workflow: KiwiWorkflow<*, *>) {
        check(workflow.name !in workflows) {
            "Workflow '${workflow.name}' is already registered."
        }

        workflows[workflow.name] = workflow
    }
}
