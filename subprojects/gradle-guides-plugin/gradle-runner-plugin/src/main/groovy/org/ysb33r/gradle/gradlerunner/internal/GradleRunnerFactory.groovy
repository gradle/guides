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
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.ysb33r.gradle.gradlerunner.GradleRunnerPlugin

import java.lang.reflect.Method

/** Provides a private project extension for loading GradleRunner instances.
 *
 * @since 1.0
 */
@CompileStatic
class GradleRunnerFactory {

    GradleRunnerFactory(Project project) {
        configuration = project.configurations.getByName(GradleRunnerPlugin.CLASSPATH_CONFIGURATION)
    }

    def createRunner() {
        URL[] urls = configuration.files.collect { File f ->
            f.absoluteFile.toURI().toURL()
        } as URL[]
        URLClassLoader loader = URLClassLoader.newInstance(urls,this.getClass().classLoader)
        Class gradleRunner = loader.loadClass('org.gradle.testkit.runner.GradleRunner')
        Method createMethod = gradleRunner.getDeclaredMethod('create')
        createMethod.invoke(gradleRunner)
    }

    private Configuration configuration
}
