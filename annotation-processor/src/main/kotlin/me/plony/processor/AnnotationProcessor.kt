package me.plony.processor

import com.gitlab.kordlib.core.Kord
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import me.plony.processor.dsl.code
import me.plony.processor.dsl.fileSpec
import me.plony.processor.dsl.function
import me.plony.processor.dsl.import
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@Target(AnnotationTarget.FUNCTION)
annotation class Module

@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedAnnotationTypes("me.plony.processor.Module")
@SupportedOptions(AnnotationProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class AnnotationProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        val utils = processingEnv.elementUtils
        val elements = roundEnv.getElementsAnnotatedWith(Module::class.java)
        if (elements.isEmpty()) return false
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: run {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can't find the target directory for generated Kotlin files.")
            return false
        }

        fileSpec("", "configuration") {
            import(ServiceManager::class, Service::class)
            addImport("me.plony.processor", "newDiscordReceiver")
            elements.forEach {
                addImport(utils.getPackageOf(it).qualifiedName.toString(), it.simpleName.toString())
            }
            function("createServices") {
                receiver(Kord::class)
                returns(ServiceManager::class)
                code("""
                    val serviceManager = ServiceManager()
                    val services = listOf(
                        ${
                            elements.joinToString(",\n") { 
                                "Service(newDiscordReceiver(), \"${it.simpleName}\") { ${it.simpleName}() }"
                            }
                        }
                    )
                    serviceManager.addAll(services)
                    serviceManager.executeAll()
                    return serviceManager
                """.trimIndent())
            }
        }.writeTo(File(kaptKotlinGeneratedDir, "generated.kt"))

        return true
    }
}

