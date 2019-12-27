/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2015 - 2017
 *
 * This software is licensed under the Apache License 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0 for license details
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * ============================================================================
 */
package org.ysb33r.gradle.gradlerunner

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.ysb33r.gradle.gradlerunner.internal.GradleStep
import spock.lang.Specification



class GradleRunnerSpec extends Specification {

    Project project = ProjectBuilder.builder().build()

    def 'Add basic steps and execute'() {
        when:
        project.apply plugin : 'org.ysb33r.gradlerunner'
        GradleRunnerSteps gradleRunner = project.tasks.getByName('gradleRunner')

        gradleRunner.step'closure', {
            StepInfo info -> println project.name
        }

        gradleRunner.step'action', new Action<StepInfo>() {
            void execute(StepInfo i) {
                println i.getName()
            }
        }

        gradleRunner.step 'gradle', 'tasks'

        gradleRunner.failingStep 'failing', 'non-existent-task'

        then:
        gradleRunner.steps.size() == 4

        when:
        gradleRunner.execute()
        File reportDir = gradleRunner.getStepReportDir( 'gradle')

        then:
        gradleRunner.getStepReportDir(0) == new File("${project.buildDir}/reports/runners/gradleRunner/closure")
        gradleRunner.workingDir == new File("${project.buildDir}/runners/gradleRunner")
        reportDir.parentFile.parentFile.exists()
        reportDir.parentFile.exists()
        reportDir.exists()
        new File(reportDir,'out.txt').exists()

    }

    def 'Running the task from the plugin'() {
        setup:
        project.allprojects {
            apply plugin : 'org.ysb33r.gradlerunner'

            gradleRunner {
                step 'gradle', 'tasks', '--all'
            }
        }

        when:
        project.evaluate()
        project.gradleRunner.execute()

        then:
        ((GradleStep)(project.gradleRunner.getSteps()[0])).output.exists()

    }
}