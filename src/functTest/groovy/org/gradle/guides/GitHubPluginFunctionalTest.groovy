/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.guides

import org.gradle.testkit.runner.TaskOutcome
import org.yaml.snakeyaml.Yaml

class GitHubPluginFunctionalTest extends AbstractFunctionalTest {
    def setup() {
        buildFile << """
            plugins {
                id 'org.gradle.guides.github'
            }
        """
    }

    def "can generate dco.yml configuration file"() {
        def dcoDotYml = new File(projectDir, ".github/dco.yml")

        expect:
        !dcoDotYml.exists()

        def result = build("configureGitHub")
        result.task(":generateDeveloperCertificateOfOriginConfiguration").outcome == TaskOutcome.SUCCESS

        dcoDotYml.exists()
        new Yaml().load(dcoDotYml.newReader()).require.members == false
    }

    def "can generate .gitignore configuration file"() {
        def dotGitIgnore = new File(projectDir, ".gitignore")

        expect:
        !dotGitIgnore.exists()
        build("configureGitHub").task(":generateGitIgnoreConfiguration").outcome == TaskOutcome.SUCCESS
        dotGitIgnore.exists()
    }
}
