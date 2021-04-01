package com.compiler.server.service

import com.compiler.server.compiler.components.CompletionProvider
import com.compiler.server.compiler.components.ErrorAnalyzer
import com.compiler.server.compiler.components.KotlinCompiler
import com.compiler.server.compiler.components.KotlinToJSTranslator
import com.compiler.server.compiler.file.KotlinFile
import com.compiler.server.model.*
import com.compiler.server.model.bean.VersionInfo
import common.model.Completion
import component.KotlinEnvironment
import org.apache.commons.logging.LogFactory
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.springframework.stereotype.Component

@Component
class KotlinProjectExecutor(
  private val kotlinCompiler: KotlinCompiler,
  private val completionProvider: CompletionProvider,
  private val errorAnalyzer: ErrorAnalyzer,
  private val version: VersionInfo,
  private val kotlinToJSTranslator: KotlinToJSTranslator,
  private val kotlinEnvironment: KotlinEnvironment
) {

  private val log = LogFactory.getLog(KotlinProjectExecutor::class.java)

  fun run(project: Project): ExecutionResult {
    return kotlinEnvironment.environment { environment ->
      val files = getFilesFrom(project, environment).map { it.file }
      kotlinCompiler.run(files, environment, project.args)
    }
  }

  fun test(project: Project): ExecutionResult {
    return kotlinEnvironment.environment { environment ->
      val files = getFilesFrom(project, environment).map { it.file }
      kotlinCompiler.test(files, environment)
    }
  }

  fun convertToJs(project: Project): TranslationJSResult {
    return kotlinEnvironment.environment { environment ->
      val files = getFilesFrom(project, environment).map { it.file }
      kotlinToJSTranslator.translate(
        files,
        project.args.split(" "),
        environment,
        kotlinToJSTranslator::doTranslate
      )
    }
  }

  fun convertToJsIr(project: Project): TranslationJSResult {
    return kotlinEnvironment.environment { environment ->
      val files = getFilesFrom(project, environment).map { it.file }
      kotlinToJSTranslator.translate(
        files,
        project.args.split(" "),
        environment,
        kotlinToJSTranslator::doTranslateWithIr
      )
    }
  }

  fun complete(project: Project, line: Int, character: Int): List<Completion> {
    return kotlinEnvironment.environment {
      val file = getFilesFrom(project, it).first()
      try {
        val isJs = project.confType.isJsRelated()
        completionProvider.complete(file, line, character, isJs, it)
      } catch (e: Exception) {
        log.warn("Exception in getting completions. Project: $project", e)
        emptyList()
      }
    }
  }

  fun highlight(project: Project): Map<String, List<ErrorDescriptor>> {
    return kotlinEnvironment.environment { environment ->
      val files = getFilesFrom(project, environment).map { it.file }
      try {
        val isJs = project.confType.isJsRelated()
        errorAnalyzer.errorsFrom(
          files = files,
          coreEnvironment = environment,
          isJs = isJs
        ).errors
      } catch (e: Exception) {
        log.warn("Exception in getting highlight. Project: $project", e)
        emptyMap()
      }
    }
  }

  fun getVersion() = version

  private fun getFilesFrom(project: Project, coreEnvironment: KotlinCoreEnvironment) = project.files.map {
    KotlinFile.from(project.confType, coreEnvironment.project, it.name, it.text)
  }
}
