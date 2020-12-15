package me.plony.processor.dsl

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import kotlin.reflect.KClass

inline fun fileSpec(packageName: String, filename: String, block: FileSpec.Builder.() -> Unit) =
    FileSpec.builder(packageName, filename)
        .apply(block)
        .build()

fun FileSpec.Builder.import(vararg classes: KClass<*>) = classes.forEach { addImport(it.qualifiedName!!.removeSuffix("." +it.simpleName!!), it.simpleName!!.toString())}
inline fun FileSpec.Builder.function(name: String, block: FunSpec.Builder.() -> Unit) =
    addFunction(FunSpec.builder(name)
        .apply(block)
        .build())
fun FunSpec.Builder.code(content: String, vararg args: Any) = addCode(content, args)