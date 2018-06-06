/*
 * Copyright 2018 Jaroslav Chmurny
 *
 * This file is part of SSL Sandbox.
 *
 * SSL Sandbox is free software developed for educational purposes. It
 * is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jch.education.ssl.perftest.client;

public class IterationSummary {

    private final TestParameters testParameters;

    private final long durationMillis;

    public IterationSummary(TestParameters testParameters, long durationMillis) {
        this.testParameters = testParameters;
        this.durationMillis = durationMillis;
    }

    public int overallMessageCount() {
        return this.testParameters.messagesPerConnection();
    }

    public long overallByteCount() {
        return this.testParameters.messagesPerConnection() * this.testParameters.messageSizeInBytes();
    }

    public long durationMillis() {
        return this.durationMillis;
    }
}
