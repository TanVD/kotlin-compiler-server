package com.compiler.server.compiler.file

import com.compiler.server.model.ProjectType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.PsiFileFactoryImpl
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile

abstract class KotlinFile(val file: KtFile) {
    abstract fun elementAt(line: Int, character: Int): PsiElement?
    abstract fun insert(content: String, atLine: Int, atCharacter: Int): KotlinFile

    companion object {
        fun from(
            type: ProjectType,
            project: Project,
            name: String,
            content: String
        ): KotlinFile {
            val factory = PsiFileFactory.getInstance(project) as PsiFileFactoryImpl
            val fileName = if (name.endsWith(".kt")) name else "$name.kt"

            return when (type) {
                ProjectType.JAVA -> {
                    KotlinAutomationScriptFile(factory, name, content)
                }
                else -> {
                    KotlinFullFile(factory, KotlinFileProvider.create(factory, fileName, content))
                }
            }

        }
    }

    protected fun offsetFor(line: Int, character: Int): Int {
        return (file.viewProvider.document?.getLineStartOffset(line) ?: 0) + character
    }

    protected fun expressionFor(element: PsiElement): PsiElement {
        return if (element is KtExpression) element else expressionFor(element.parent)
    }

}