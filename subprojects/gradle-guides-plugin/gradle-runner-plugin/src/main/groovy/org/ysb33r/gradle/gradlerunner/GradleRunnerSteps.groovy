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

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.FileUtils
import org.ysb33r.gradle.gradlerunner.internal.ActionStep
import org.ysb33r.gradle.gradlerunner.internal.ClosureStep
import org.ysb33r.gradle.gradlerunner.internal.GradleRunnerFactory
import org.ysb33r.gradle.gradlerunner.internal.GradleStep

/** Executes a set of staps including running Gradle with tasks and capturing the output
 *
 * @since 1.0
 */
@CompileStatic
class GradleRunnerSteps extends DefaultTask {

    GradleRunnerSteps() {
        super()
        Closure up2Date = { GradleRunnerSteps runner ->
            List<Step> allSteps = runner.getSteps()
            "${allSteps.size()}:" + allSteps.collect { Step step ->
                step.class.getName()
            }.join(':')
        }.curry(this)
        inputs.property 'steps-up2date', up2Date
    }

    void workingDir(final Object path) {
        setWorkingDir(path)
    }

    void setWorkingDir(final Object path) {
        this.workingDir = path
    }

    @OutputDirectory
    File getWorkingDir() {
        project.file(this.workingDir)
    }

    void reportsDir(final Object path) {
        setWorkingDir(path)
    }

    void setReportsDir(final Object path) {
        this.reportsDir = path
    }

    @OutputDirectory
    File getReportsDir() {
        project.file(this.reportsDir)
    }

    /** Executes a closure
     *
     * @param name Name of the step
     * @param action The action to execute. Will be passed a {@link StepInfo}.
     */
    void step(final String name, Closure action) {
        steps.add new ClosureStep(name,action)
    }

    /** Executes an {@code Action}.
     *
     * @param name Name of the step
     * @param action The action to execute. Will be passed a {@link StepInfo}.
     */
    void step(final String name, Action<StepInfo> action) {
        steps.add new ActionStep(name,action)
    }

    /** Execute a Gradle step in the directory specified by {@link #getWorkingDir()}.
     *
     * Outputs from the step will be stored in a fodler before {@link #getReportsDir()}
     *
     * @param name Name of step
     * @param gradleArgs Arguments to pass to Gradle
     */
    void step(final String name, Iterable<String> gradleArgs) {
        this.steps.add new GradleStep(
                (GradleRunnerFactory)(project.extensions.getByName(GradleRunnerPlugin.CLASSPATH_EXTENSION)),
                name,gradleArgs,false)
    }

    /** Execute a Gradle step in the directory specified by {@link #getWorkingDir()}.
     *
     * Outputs from the step will be stored in a folder before {@link #getReportsDir()}
     *
     * @param name Name of step
     * @param gradleArgs Arguments to pass to Gradle
     */
    void step(final String name, String... gradleArgs) {
        step(name,gradleArgs as List)
    }

    /** Execute a failing Gradle step in the directory specified by {@link #getWorkingDir()}.
     *
     * Outputs from the step will be stored in a fodler before {@link #getReportsDir()}
     *
     * @param name Name of step
     * @param gradleArgs Arguments to pass to Gradle
     */
    void failingStep(final String name, Iterable<String> gradleArgs) {
        this.steps.add new GradleStep(
                (GradleRunnerFactory)(project.extensions.getByName(GradleRunnerPlugin.CLASSPATH_EXTENSION)),
                name,gradleArgs,true)
    }

    /** Execute a failing Gradle step in the directory specified by {@link #getWorkingDir()}.
     *
     * Outputs from the step will be stored in a fodler before {@link #getReportsDir()}
     *
     * @param name Name of step
     * @param gradleArgs Arguments to pass to Gradle
     */
    void failingStep(final String name, String... gradleArgs) {
        failingStep(name,gradleArgs as List)
    }

    /** Returns the list of steps.
     *
     * @return Steps in order as that they were added
     */
    List<Step> getSteps() {
        this.steps
    }

    /** Returns the working directory for a specific step.
     *
     * @param stepName Name of step.
     * @return The reposrting directory for the step. Can be {@code null} if the step did not execute.
     */
    File getStepReportDir(final String stepName) {
        Step needle = steps.find { Step step ->
            step.name == stepName
        }

        if(needle == null) {
            throw new GradleException("Step '${stepName}' does not exist")
        }

        needle.stepInfo.reportDir
    }

    /** Returns the working directory for a specific step.
     *
     * @param stepIndex Index number of step.
     * @return The reposrting directory for the step. Can be {@code null} if the step did not execute.
     */
    File getStepReportDir(int stepIndex) {
        steps.get(stepIndex).stepInfo.reportDir
    }

    @TaskAction
    void exec() {
        final File work = getWorkingDir()
        final File report = getReportsDir()

        [ work, report ].each { File it ->
            it.deleteDir()
            it.mkdirs()
        }

        for (Step step : steps ) {
            logger.info " -- ${step.getName()}"
            step.execute( work, new File(report,FileUtils.toSafeFileName(step.getName()) ) )
        }
    }

    private final List<Step> steps = []
    private final String pathIdentifier = FileUtils.toSafeFileName(getName())
    private Object workingDir = { String id -> "${project.buildDir}/runners/${id}"}.curry(pathIdentifier)
    private Object reportsDir = { String id -> "${project.buildDir}/reports/runners/${id}"}.curry(pathIdentifier)
}
