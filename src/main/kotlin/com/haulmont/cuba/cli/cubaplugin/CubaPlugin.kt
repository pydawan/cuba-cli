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

package com.haulmont.cuba.cli.cubaplugin

import com.google.common.eventbus.Subscribe
import com.haulmont.cuba.cli.*
import com.haulmont.cuba.cli.cubaplugin.appcomponentxml.AppComponentCommand
import com.haulmont.cuba.cli.cubaplugin.browsescreen.CreateBrowseScreenCommand
import com.haulmont.cuba.cli.cubaplugin.componentbean.CreateComponentBeanCommand
import com.haulmont.cuba.cli.cubaplugin.config.ConfigCommand
import com.haulmont.cuba.cli.cubaplugin.editscreen.CreateEditScreenCommand
import com.haulmont.cuba.cli.cubaplugin.entity.CreateEntityCommand
import com.haulmont.cuba.cli.cubaplugin.entitylistener.CreateEntityListenerCommand
import com.haulmont.cuba.cli.cubaplugin.enumeration.CreateEnumerationCommand
import com.haulmont.cuba.cli.cubaplugin.installcomponent.ComponentInstallCommand
import com.haulmont.cuba.cli.cubaplugin.polymer.CreatePolymerModuleCommand
import com.haulmont.cuba.cli.cubaplugin.project.ProjectInitCommand
import com.haulmont.cuba.cli.cubaplugin.screen.CreateScreenCommand
import com.haulmont.cuba.cli.cubaplugin.screenextension.ExtendDefaultScreenCommand
import com.haulmont.cuba.cli.cubaplugin.service.CreateServiceCommand
import com.haulmont.cuba.cli.cubaplugin.statictemplate.StaticTemplateCommand
import com.haulmont.cuba.cli.cubaplugin.theme.ThemeExtensionCommand
import com.haulmont.cuba.cli.cubaplugin.updatescript.UpdateScript
import com.haulmont.cuba.cli.event.BeforeCommandExecutionEvent
import com.haulmont.cuba.cli.event.InitPluginEvent
import org.kodein.di.generic.instance
import java.io.PrintWriter

@Suppress("UNUSED_PARAMETER")
class CubaPlugin : CliPlugin {
    private val context: CliContext by kodein.instance()

    private val writer: PrintWriter by kodein.instance()

    private val namesUtils: NamesUtils by kodein.instance()

    private val printHelper: PrintHelper by kodein.instance()

    private val messages by localMessages()

    @Subscribe
    fun onInit(event: InitPluginEvent) {
        event.commandsRegistry {
            command("create-app", ProjectInitCommand())
            command("entity", CreateEntityCommand())
            command("screen", CreateScreenCommand()) {
                command("extend", ExtendDefaultScreenCommand())
                command("browse", CreateBrowseScreenCommand())
                command("edit", CreateEditScreenCommand())
            }
            command("service", CreateServiceCommand())
            command("template", StaticTemplateCommand())
            command("bean", CreateComponentBeanCommand())
            command("entity-listener", CreateEntityListenerCommand())
            command("app-component", AppComponentCommand())
            command("enumeration", CreateEnumerationCommand())
            command("theme", ThemeExtensionCommand())
            command("install-component", ComponentInstallCommand())
            command("polymer", CreatePolymerModuleCommand())
            command("config", ConfigCommand())
            command("updateScript", UpdateScript())
        }
    }

    @Subscribe
    fun beforeCommand(event: BeforeCommandExecutionEvent) {
        context.addModel("names", namesUtils)

        val projectStructure = try {
            ProjectStructure()
        } catch (e: Exception) {
            return
        }

        try {
            context.addModel(ProjectModel.MODEL_NAME, ProjectModel(projectStructure))
        } catch (e: ProjectScanException) {
            writer.println(messages["projectParsingError"].bgRed())

            printHelper.saveStacktrace(e)
        }
    }

    companion object {
        const val TEMPLATES_BASE_PATH = "/com/haulmont/cuba/cli/cubaplugin/templates/"
        const val SNIPPETS_BASE_PATH = "/com/haulmont/cuba/cli/cubaplugin/snippets/"
    }
}