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

import org.ysb33r.gradle.gradlerunner.Step
import org.ysb33r.gradle.gradlerunner.StepInfo

/** Abstract base class for implementing steps
 *
 * @since 1.0
 */
abstract class AbstractStep implements Step, StepInfo {

    /** Information that was used during execution of the step
     *
     * @return Step information.
     */
    @Override
    StepInfo getStepInfo() {
        this
    }

    /** Directory where reports should be written to
     *
     * @return Directory unique to this step
     */
    @Override
    File getReportDir() {
        this.reportDir
    }

    /** Base directory where work occurs in
     *
     * @return Directory/ Null if action
     */
    @Override
    File getWorkingDir() {
        this.workingDir
    }

    /** Name of this step.
     *
     * @return Name.
     */
    @Override
    String getName() {
        this.name
    }


    /** Set working directory for this step.
     *
     * @param wd Working directory
     */
    protected void setWorkingDir(File wd) {
        this.workingDir = wd
    }

    /** Set unique reporting directory for this step.
     *
     * @param rd Reporting directory
     */
    protected void setReportDir(File rd) {
        this.reportDir = rd
    }

    protected void createDirs(File workingDir, File reportsDir) {
        this.workingDir = workingDir
        this.reportDir = reportsDir
        this.workingDir.mkdirs()
        this.reportDir.mkdirs()
    }

    protected AbstractStep(final String name) {
        this.name = name
    }

    final String name
    File reportDir
    File workingDir
}
