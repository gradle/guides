/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.docs.internal.exemplar;

import net.rubygrapefruit.ansi.AnsiParser;
import net.rubygrapefruit.ansi.console.AnsiConsole;
import net.rubygrapefruit.ansi.token.NewLine;
import net.rubygrapefruit.ansi.token.Text;

import java.io.IOException;
import java.io.OutputStream;

public class AnsiCharactersToPlainTextOutputStream extends OutputStream {
    private final OutputStream delegate;
    private final AnsiConsole console;

    public AnsiCharactersToPlainTextOutputStream() {
        AnsiParser parser = new AnsiParser();

        this.console = new AnsiConsole();
        this.delegate = parser.newParser("utf-8", console);
    }

    @Override
    public void write(int b) throws IOException {
        delegate.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        delegate.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        delegate.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.flush();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        console.contents(token -> {
            if (token instanceof Text) {
                result.append(((Text) token).getText());
            } else if (token instanceof NewLine) {
                result.append("\n");
            }
        });
        return result.toString();
    }
}
