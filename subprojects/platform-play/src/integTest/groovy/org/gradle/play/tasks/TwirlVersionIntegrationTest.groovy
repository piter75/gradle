/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.play.tasks

import org.gradle.integtests.fixtures.AbstractIntegrationSpec

class TwirlVersionIntegrationTest extends AbstractIntegrationSpec {
    def baseBuildFile = """
        plugins {
            id 'play-application'
        }

        repositories{
            jcenter()
            maven{
                name = "typesafe-maven-release"
                url = "https://repo.typesafe.com/typesafe/maven-releases"
            }
        }
    """

    def setup() {
        settingsFile << """ rootProject.name = 'twirl-play-app' """
    }

    def "changing between twirl-incompatible versions of play causes Twirl to recompile" () {
        withPlayVersion("2.2.1")
        withTemplateSource(file("app", "views", "index.scala.html"))

        when:
        succeeds "playBinary"

        then:
        executedAndNotSkipped(":twirlCompileTwirlTemplatesPlayBinary", ":scalaCompilePlayBinary")

        and:
        file("build/playBinary/src/twirlCompileTwirlTemplatesPlayBinary/views/html/index.template.scala").exists()

        when:
        withPlayVersion("2.3.7")
        succeeds "playBinary"

        then:
        executedAndNotSkipped(":twirlCompileTwirlTemplatesPlayBinary", ":scalaCompilePlayBinary")

        and:
        file("build/playBinary/src/twirlCompileTwirlTemplatesPlayBinary/views/html/index.template.scala").exists()
    }

    def "changing between twirl-compatible versions of play does NOT cause Twirl to recompile" () {
        withPlayVersion("2.3.1")
        withTemplateSource(file("app", "views", "index.scala.html"))

        when:
        succeeds "playBinary"

        then:
        executedAndNotSkipped(":twirlCompileTwirlTemplatesPlayBinary", ":scalaCompilePlayBinary")

        and:
        file("build/playBinary/src/twirlCompileTwirlTemplatesPlayBinary/views/html/index.template.scala").exists()

        when:
        withPlayVersion("2.3.7")
        succeeds "playBinary"

        then:
        skipped(":twirlCompileTwirlTemplatesPlayBinary")
        executedAndNotSkipped(":scalaCompilePlayBinary")
    }

    def withPlayVersion(String playVersion) {
        buildFile.delete()
        buildFile << """
            $baseBuildFile

            model {
                components {
                    play {
                        targetPlatform "play-${playVersion}"
                    }
                }
            }
        """
    }

    def withTemplateSource(File templateFile) {
        templateFile << """@(message: String)

            @play20.welcome(message)

        """
    }
}
