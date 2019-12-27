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
package org.ysb33r.gradle.gradlerunner.internal

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.ysb33r.gradle.gradlerunner.GradleRunnerPlugin
import org.ysb33r.gradle.gradlerunner.Step
import org.ysb33r.gradle.gradlerunner.StepInfo

import java.lang.reflect.Method

/** Runs a Gradle execution.
 *
 * @since 1.0
 */
@CompileStatic
class GradleStep extends AbstractStep {

//    GradleStep(final Project project,final String name, final List<String> args,boolean expectFailure) {
//        super(name)
//        this.project = project
//        this.args = args
//        this.expectFailure = expectFailure
//    }

    GradleStep(final GradleRunnerFactory factory,final String name,final Iterable<String> args,boolean expectFailure) {
        super(name)
        this.args = []
        this.args.addAll(args)
        this.expectFailure = expectFailure
        this.factory = factory
    }

    @Override
    void execute(File projectDir, File reportDir) {
        createDirs(projectDir,reportDir)
        this.outputFile = new File(reportDir,'out.txt')
        this.errorFile = new File(reportDir,'err.txt')
        run( projectDir, this.outputFile, this.errorFile)
    }

    /** Location of output from execution.
     *
     * @return Output file. Null if step has not executed yet.
     */
    File getOutput() {
        this.outputFile
    }

    /** Location of error output from execution.
     *
     * @return Error file. Null if step has not executed yet.
     */
    File getError() {
        this.errorFile
    }

    @CompileDynamic
    private def run(final File projDir, final File outFile,final  File errFile) {

        def result
        def runner = factory.createRunner()
        runner.withArguments(this.args).withProjectDir(projDir)
        if (!args.contains('init')) {
            def settingsFile = new File(projDir, 'settings.gradle')
            def settingsKtsFile = new File(projDir, 'settings.gradle.kts')
            if (!settingsFile.exists() && !settingsKtsFile.exists()) {
                settingsFile.text = ''
            }
        }
        outFile.withPrintWriter { PrintWriter out ->
            errFile.withPrintWriter { PrintWriter err ->
                runner.forwardStdError(err).forwardStdOutput(out)
                result = this.expectFailure ? runner.buildAndFail() : runner.build()
            }
        }
        return result
    }

    private File outputFile
    private File errorFile

    private final List<String> args
    private final boolean expectFailure
    private final GradleRunnerFactory factory
}
