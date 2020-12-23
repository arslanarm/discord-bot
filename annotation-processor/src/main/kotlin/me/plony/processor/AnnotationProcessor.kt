package me.plony.processor

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
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
annotation class Module(val help: String = "")

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
            addImport("dev.kord.core", "on")
            addImport("dev.kord.core.behavior.channel", "createEmbed")
            import(MessageCreateEvent::class)
            function("createServices") {
                receiver(Kord::class)
                returns(ServiceManager::class)
                code("""
                    on<MessageCreateEvent> {
                        if (message.author?.isBot == true || message.content != "+help") return@on
                        
                        message.channel.createEmbed {
                            title = "Help"
                            description = "Описание модулей: \n${
                                elements.filter { 
                                    it.getAnnotation(Module::class.java).help.isNotBlank()
                                }.joinToString("\\n") { element ->
                                    val annotation = element.getAnnotation(Module::class.java)
                                    "\\t${element.simpleName.toString().capitalize()}: ${annotation.help}"
                                }
                            }"
                        }
                    }
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

