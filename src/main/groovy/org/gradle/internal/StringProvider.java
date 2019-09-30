/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.internal;

import org.gradle.api.internal.provider.Providers;
import org.gradle.api.provider.Provider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

// Deferred the String evaluation for Asciidoc task until it support Provider API.
// Using a simple interpolating closure expression in a GString doesn't work as it's not serializable.
public class StringProvider implements Serializable {
    private Provider<String> value;

    private StringProvider(Provider<String> value) {
        this.value = value;
    }

    public static StringProvider of(Provider<String> value) {
        return new StringProvider(value);
    }

    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return value.getOrElse("");
    }

    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        String value = (String) aInputStream.readObject();
        this.value = Providers.of(value);
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException  {
        aOutputStream.writeObject(toString());
    }
}
