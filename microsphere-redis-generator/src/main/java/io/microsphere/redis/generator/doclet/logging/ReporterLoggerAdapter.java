/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.microsphere.redis.generator.doclet.logging;

import io.microsphere.io.StringBuilderWriter;
import io.microsphere.logging.AbstractLogger;
import io.microsphere.logging.Logger;
import jdk.javadoc.doclet.Reporter;

import javax.tools.Diagnostic;
import java.io.PrintWriter;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.OTHER;
import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * The {@link Logger} adapter based on {@link Reporter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractLogger
 * @see Reporter
 * @since 1.0.0
 */
public class ReporterLoggerAdapter extends AbstractLogger {

    private final Reporter reporter;

    public ReporterLoggerAdapter(String name, Reporter reporter) {
        super(name);
        this.reporter = reporter;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public void trace(String message) {
        trace(message, (Throwable) null);
    }

    @Override
    public void trace(String message, Throwable t) {
        log(OTHER, message, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return isTraceEnabled();
    }

    @Override
    public void debug(String message) {
        trace(message);
    }

    @Override
    public void debug(String message, Throwable t) {
        trace(message, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String message) {
        info(message, (Throwable) null);
    }

    @Override
    public void info(String message, Throwable t) {
        log(NOTE, message, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(String message) {
        warn(message, (Throwable) null);
    }

    @Override
    public void warn(String message, Throwable t) {
        log(WARNING, message, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(String message) {
        error(message, (Throwable) null);
    }

    @Override
    public void error(String message, Throwable t) {
        log(ERROR, message, t);
    }

    protected void log(Diagnostic.Kind kind, String message, Throwable t) {
        this.reporter.print(kind, message);
        if (t != null) {
            StringBuilderWriter writer = new StringBuilderWriter();
            t.printStackTrace(new PrintWriter(writer));
            this.reporter.print(kind, writer.toString());
        }
    }
}