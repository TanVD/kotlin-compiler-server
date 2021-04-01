package com.compiler.server.compiler.file

import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtFile
import java.nio.charset.Charset

object KotlinFileProvider {
    fun create(factory: PsiFileFactoryImpl, name: String, content: String): KtFile {

        val lightFile = LightVirtualFile(name, KotlinLanguage.INSTANCE, content).apply {
            charset = Charset.defaultCharset()
        }
        return factory.trySetupPsiForFile(lightFile, KotlinLanguage.INSTANCE, true, false) as KtFile
    }
}