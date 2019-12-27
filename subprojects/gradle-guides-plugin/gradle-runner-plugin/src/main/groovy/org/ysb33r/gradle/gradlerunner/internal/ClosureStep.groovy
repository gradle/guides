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

import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.ysb33r.gradle.gradlerunner.Step
import org.ysb33r.gradle.gradlerunner.StepInfo

/** Using a closure as a step
 *
 * @since 1.0
 */
@CompileStatic
class ClosureStep extends AbstractStep {

    ClosureStep( final String name, Closure action) {
        super(name)
        if( action.parameterTypes.size() != 1 && !(action.parameterTypes[0] instanceof StepInfo) ) {
            throw new GradleException( " Closure needs to take a StepInfo as parameter")
        }

        this.action = action
    }

    /** Executes the closure, passing a StepInfo
     *
     * @param workingDir Working directory for step
     * @param reportsDir Directory whether reports should be written to.
     */
    @Override
    void execute(File workingDir, File reportsDir) {
        createDirs(workingDir,reportsDir)
        action.call(this)
    }

    Closure action
}
