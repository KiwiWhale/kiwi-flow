package io.github.kiwiwhale.kiwiflow.spring.autoconfigure

import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowRegistry
import io.github.kiwiwhale.kiwiflow.spring.factory.SpringWorkflowFactory
import io.github.kiwiwhale.kiwiflow.spring.registrar.KiwiFlowBeanRegistrar
import io.github.kiwiwhale.kiwiregistry.provider.RegistryProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.getBeansOfType
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class KiwiFlowAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(KiwiFlowAutoConfiguration::class.java)
        )

    @Test
    fun `기본 인프라 Bean들이 자동 구성된다`() {
        contextRunner.run { context ->
            assertEquals(1, context.getBeansOfType<KiwiWorkflowRegistry>().size)
            assertEquals(1, context.getBeansOfType<SpringWorkflowFactory>().size)
            assertEquals(1, context.getBeansOfType<KiwiFlowBeanRegistrar>().size)
        }
    }

    @Test
    fun `사용자 정의 WorkflowRegistry를 유지한다`() {
        val customRegistry = KiwiWorkflowRegistry(
            RegistryProvider()
        )

        contextRunner
            .withBean(
                "customWorkflowRegistry",
                KiwiWorkflowRegistry::class.java,
                { customRegistry }
            )
            .run { context ->
                assertEquals(
                    1,
                    context.getBeansOfType<KiwiWorkflowRegistry>().size
                )

                assertSame(
                    customRegistry,
                    context.getBean<KiwiWorkflowRegistry>()
                )
            }
    }

    @Test
    fun `사용자 정의 SpringWorkflowFactory를 유지한다`() {
        val customFactory = SpringWorkflowFactory()

        contextRunner
            .withBean(
                "customSpringWorkflowFactory",
                SpringWorkflowFactory::class.java,
                { customFactory }
            )
            .run { context ->
                assertEquals(
                    1,
                    context.getBeansOfType<SpringWorkflowFactory>().size
                )

                assertSame(
                    customFactory,
                    context.getBean<SpringWorkflowFactory>()
                )
            }
    }

    @Test
    fun `사용자 정의 Registrar를 유지한다`() {
        contextRunner
            .withInitializer { context ->
                context.beanFactory.registerSingleton(
                    "customRegistrar",
                    KiwiFlowBeanRegistrar(
                        applicationContext = context,
                        factory = SpringWorkflowFactory()
                    )
                )
            }
            .run { context ->
                assertEquals(
                    1,
                    context.getBeansOfType<KiwiFlowBeanRegistrar>().size
                )
            }
    }
}
