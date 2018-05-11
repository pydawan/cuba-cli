/*
 * Copyright (c) 2008-2018 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.cuba.cli.cubaplugin.polymer

import com.beust.jcommander.Parameters
import com.haulmont.cuba.cli.Messages
import com.haulmont.cuba.cli.PrintHelper
import com.haulmont.cuba.cli.commands.GeneratorCommand
import com.haulmont.cuba.cli.cubaplugin.CubaPlugin
import com.haulmont.cuba.cli.generation.TemplateProcessor
import com.haulmont.cuba.cli.kodein
import com.haulmont.cuba.cli.prompting.Answers
import com.haulmont.cuba.cli.prompting.QuestionsList
import org.kodein.di.generic.instance
import java.nio.file.Paths

@Parameters(commandDescription = "Creates empty polymer module")
class CreatePolymerModuleCommand : GeneratorCommand<PolymerModel>() {

    private val messages = Messages(javaClass)

    private val printHelper: PrintHelper by kodein.instance()

    override fun getModelName(): String = "polymer"

    override fun QuestionsList.prompting() {}

    override fun createModel(answers: Answers): PolymerModel = PolymerModel(projectModel)

    override fun generate(bindings: Map<String, Any>) {
        val destinationDir = Paths.get("modules/polymer-client")

        TemplateProcessor(CubaPlugin.TEMPLATES_BASE_PATH + "polymer", bindings) {
            templatePath.walk(1).filter {
                it.fileName.toString() != "images"
            }.forEach {
                transform(it, to = destinationDir)
            }
            copy("images", to = destinationDir)
        }

        projectStructure.buildGradle.toFile().apply {
            readText()
                    .replace(messages.getMessage("moduleVarSearch"), messages.getMessage("moduleVarReplace"))
                    .replace(messages.getMessage("moduleConfigureSearch"), messages.getMessage("moduleConfigureReplace"))
                    .replace(messages.getMessage("undeploySearch"), messages.getMessage("undeployReplace"))
                    .let {
                        writeText(it)
                    }
        }
        printHelper.fileAltered(projectStructure.buildGradle)


        projectStructure.settingsGradle.toFile().apply {
            val lines = readLines().map {
                if (it.startsWith("include(")) {
                    it.replace(Regex("include\\((.*)\\)")) {
                        val modules = it.groupValues[1].split(",")
                        (modules + "\":\${modulePrefix}-polymer-client\"").joinToString(" ,", "include(", ")")
                    }
                } else it
            } + messages.getMessage("moduleRegistration")
            writeText(lines.joinToString("\n"))
        }
        printHelper.fileAltered(projectStructure.settingsGradle)
    }
}