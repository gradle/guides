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

/** Information requires to execute a step
 *
 * @since 1.0
 */
@CompileStatic
interface StepInfo {

    /** Name of the step
     *
     * @return Name as a string
     */
    String getName()

    /** Directory where reports should be written to
     *
     * @return Directory unique to this step. Can be null.
     */
    File getReportDir()

    /** Base directory where work occurs in
     *
     * @return Directory. Can be null.
     */
    File getWorkingDir()
}