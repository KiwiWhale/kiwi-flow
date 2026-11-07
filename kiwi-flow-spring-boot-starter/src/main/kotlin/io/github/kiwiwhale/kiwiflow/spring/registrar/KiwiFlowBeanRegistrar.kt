package io.github.kiwiwhale.kiwiflow.spring.registrar

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowKey
import io.github.kiwiwhale.kiwiflow.spring.factory.SpringWorkflowFactory
import io.github.kiwiwhale.kiwiregistry.builder.RegistryContributor
import io.github.kiwiwhale.kiwiregistry.dsl.registryGraph
import io.github.kiwiwhale.kiwiregistry.provider.RegistryProvider
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils

/**
 * Discovers Spring-managed beans annotated with [Workflow]
 * and contributes them to the workflow registry.
 *
 * Workflow creation is delegated to [SpringWorkflowFactory].
 *
 * @property applicationContext Spring application context.
 * @property factory Factory responsible for creating workflows.
 */
class KiwiFlowBeanRegistrar(
    private val applicationContext: ApplicationContext,
    private val factory: SpringWorkflowFactory
) {

    /**
     * Discovers all workflows declared by Spring beans.
     *
     * @return Workflows in deterministic discovery order.
     */
    fun discoverWorkflows(): List<KiwiWorkflow<*, *>> {
        val workflows = LinkedHashMap<String, KiwiWorkflow<*, *>>()

        for (beanName in applicationContext.beanDefinitionNames) {
            discoverBean(beanName).forEach { workflow ->
                check(workflows.putIfAbsent(workflow.name, workflow) == null) {
                    "Duplicate workflow name '${workflow.name}' found across Spring beans."
                }
            }
        }

        return workflows.values.toList()
    }

    /**
     * Builds a RegistryProvider containing discovered workflows.
     */
    fun buildProvider(): RegistryProvider {
        val workflows = discoverWorkflows()

        val contributor = RegistryContributor("spring-workflows") {
            registry(KiwiWorkflowKey.WORKFLOW_REGISTRY) {
                workflows.forEach { workflow ->
                    add(workflow.name, workflow)
                }
            }
        }

        val graph = registryGraph {
            contribute(contributor)
        }

        return RegistryProvider(graph)
    }

    private fun discoverBean(
        beanName: String
    ): List<KiwiWorkflow<*, *>> {
        val beanType = applicationContext.getType(beanName)
            ?: return emptyList()

        val candidateAnnotations = findWorkflowAnnotations(beanType)

        if (candidateAnnotations.isEmpty()) {
            return emptyList()
        }

        val bean = applicationContext.getBean(beanName)
        val targetClass = AopUtils.getTargetClass(bean)

        val annotations = findWorkflowAnnotations(targetClass)
            .ifEmpty { candidateAnnotations }
            .sortedBy { it.name }

        return annotations.map { annotation ->
            factory.create(
                instance = bean,
                annotation = annotation,
                targetClass = targetClass
            )
        }
    }

    private fun findWorkflowAnnotations(
        targetClass: Class<*>
    ): Set<Workflow> =
        AnnotatedElementUtils.findAllMergedAnnotations(
            targetClass,
            Workflow::class.java
        )
}
