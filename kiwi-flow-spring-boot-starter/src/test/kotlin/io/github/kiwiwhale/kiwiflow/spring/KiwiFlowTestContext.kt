package io.github.kiwiwhale.kiwiflow.spring

import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowRegistry
import io.github.kiwiwhale.kiwiflow.spring.factory.SpringWorkflowFactory
import io.github.kiwiwhale.kiwiflow.spring.registrar.KiwiFlowBeanRegistrar
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.registerBean

internal class KiwiFlowTestContext private constructor(
    val context: AnnotationConfigApplicationContext,
    val registry: KiwiWorkflowRegistry
) : AutoCloseable {

    override fun close() {
        context.close()
    }

    companion object {
        fun create(vararg beans: Pair<String, Any>): KiwiFlowTestContext {
            val context = AnnotationConfigApplicationContext()

            try {
                beans.forEach { (name, instance) ->
                    context.registerBean(name) { instance }
                }

                context.refresh()

                val registrar = KiwiFlowBeanRegistrar(
                    applicationContext = context,
                    factory = SpringWorkflowFactory()
                )

                val provider = registrar.buildProvider()

                return KiwiFlowTestContext(
                    context = context,
                    registry = KiwiWorkflowRegistry(provider)
                )
            } catch (exception: Throwable) {
                context.close()
                throw exception
            }
        }
    }
}
