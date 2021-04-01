package com.compiler.server.compiler.file

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.PsiFileFactoryImpl
import org.jetbrains.kotlin.psi.KtFile

class KotlinFullFile(val factory: PsiFileFactoryImpl, file: KtFile) : KotlinFile(file) {
    override fun elementAt(line: Int, character: Int): PsiElement? {
        return file.findElementAt(offsetFor(line, character))?.let { expressionFor(it) }
    }

    override fun insert(content: String, atLine: Int, atCharacter: Int): KotlinFile {
        val caretPositionOffset = offsetFor(atLine, atCharacter)
        return if (caretPositionOffset != 0) {
            val newContent = buildString {
                append(file.text.substring(0, caretPositionOffset))
                append(content)
                append(file.text.substring(caretPositionOffset)).toString()
            }
            KotlinFullFile(factory, KotlinFileProvider.create(factory, newContent, file.name))
        } else this
    }
}