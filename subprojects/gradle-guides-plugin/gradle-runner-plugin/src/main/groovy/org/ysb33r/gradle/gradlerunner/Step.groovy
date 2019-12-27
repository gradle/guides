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

/** A step to be executed.
 *
 * @since 1.0
 */
@CompileStatic
interface Step {

    /** Name of this step.
     *
     * @return Name.
     */
    String getName()

    /** Executes a step
     *
     * @param workingDir Working directory for step
     * @param reportsDir Directory whether reports should be written to.
     */
    void execute( final File workingDir, final File reportsDir )

    /** Information that was used during execution of the step
     *
     * @return Step information.
     */
    StepInfo getStepInfo()
}