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

import groovy.transform.CompileStatic
import org.gradle.api.model.ReplacedBy
import org.gradle.api.provider.Property

/**
 *
 * @since 0.1
 */
@CompileStatic
abstract class GuidesExtension {
    /**
     * Path of repository relative to {@code https://github.com}.
     * @deprecated
     */
    @ReplacedBy('repositoryPath')
    String getRepoPath() {
        return getRepositoryPath().get()
    }
    void setRepoPath(String value) {
        getRepositoryPath().set(value)
    }

    /**
     * Path of repository relative to {@code https://github.com}.
     * @since 0.15.8
     */
    abstract Property<String> getRepositoryPath()

    /** Name of main author
     *
     * @deprecated
     * @since 0.5
     */
    String mainAuthor

    /**
     * Minimum Gradle version this guide works on.
     * @since 0.15.7
     */
    abstract Property<String> getMinimumGradleVersion()

    /**
     * Short description of the guide.
     * @since 0.15.8
     */
    abstract Property<String> getDescription()

    /**
     * Title of the guide.
     *
     * @since 0.15.8
     */
    abstract Property<String> getTitle()
}
