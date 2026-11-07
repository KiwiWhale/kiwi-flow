package io.github.kiwiwhale.kiwiflow.spring.autoconfigure

import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowRegistry
import io.github.kiwiwhale.kiwiflow.spring.factory.SpringWorkflowFactory
import io.github.kiwiwhale.kiwiflow.spring.registrar.KiwiFlowBeanRegistrar
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

/**
 * Provides Spring Boot autoconfiguration for KiwiFlow.
 *
 * Configures the workflow registry, Spring workflow factory,
 * and workflow bean registrar.
 *
 * Each infrastructure component can be replaced by providing
 * a custom Spring bean of the corresponding type.
 *
 * Core owns workflow registration and access,
 * while Spring provides bean discovery and dependency injection.
 *
 * @see KiwiWorkflowRegistry
 * @see SpringWorkflowFactory
 * @see KiwiFlowBeanRegistrar
 */
@AutoConfiguration
class KiwiFlowAutoConfiguration {

    /**
     * Provides the default Spring workflow factory.
     */
    @Bean
    @ConditionalOnMissingBean(SpringWorkflowFactory::class)
    fun springWorkflowFactory(): SpringWorkflowFactory =
        SpringWorkflowFactory()

    /**
     * Provides the default workflow bean registrar.
     */
    @Bean
    @ConditionalOnMissingBean(KiwiFlowBeanRegistrar::class)
    fun kiwiFlowBeanRegistrar(
        applicationContext: ApplicationContext,
        factory: SpringWorkflowFactory
    ): KiwiFlowBeanRegistrar =
        KiwiFlowBeanRegistrar(
            applicationContext,
            factory
        )

    /**
     * Provides the default Core workflow registry.
     *
     * @param registrar Workflow registrar responsible for contributing workflows.
     */
    @Bean
    @ConditionalOnMissingBean(KiwiWorkflowRegistry::class)
    fun kiwiWorkflowRegistry(
        registrar: KiwiFlowBeanRegistrar
    ): KiwiWorkflowRegistry =
        KiwiWorkflowRegistry(registrar.buildProvider())
}
