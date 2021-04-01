package com.compiler.server.compiler.file

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.PsiFileFactoryImpl
import org.intellij.lang.annotations.Language

class KotlinAutomationScriptFile(val factory: PsiFileFactoryImpl, name: String, val content: String)
    : KotlinFile(KotlinFileProvider.create(factory, name, wrap(content))) {

    companion object {

        private const val offsetLine = 4
        private const val offsetChar = 4

        private fun wrap(text: String): String {
            @Language("kotlin")
            val new = """
                import circlet.pipelines.config.dsl.scriptdefinition.ProjectScriptDefinition
                import circlet.pipelines.config.dsl.api.*
                
                fun ProjectScriptDefinition.script() {
                ${text.trimIndent().lines().mapIndexed { i, line -> if (i != 0) " ".repeat(5 * 4) + line else line }.joinToString(separator = "\n")}
                }
            """.trimIndent()
            return new
        }
    }

    override fun elementAt(line: Int, character: Int): PsiElement? {
        return file.findElementAt(offsetFor(line + offsetLine, character + offsetChar))?.let { expressionFor(it) }
    }

    override fun insert(content: String, atLine: Int, atCharacter: Int): KotlinFile {
        val caretPositionOffset = offsetFor(atLine + offsetLine, atCharacter + offsetChar) - offsetFor(offsetLine, offsetChar)
        return if (caretPositionOffset != 0) {
            val lines = this.content.lines()
            val previous = lines.take(atLine) + lines[atLine].take(atCharacter)
            val after = listOf(lines[atLine].drop(atCharacter)) + lines.drop(atLine + 1)
            val newContent = buildString {
                append(previous.joinToString(separator = "\n"))
                append(content)
                append(after.joinToString(separator = "\n"))
            }
            KotlinAutomationScriptFile(factory, file.name, newContent)
        } else this
    }


}