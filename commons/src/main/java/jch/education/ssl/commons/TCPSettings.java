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
package jch.education.ssl.commons;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TCPSettings {

    @JsonProperty(value = "address", required = true)
    private String address;

    @JsonProperty(value = "port", required = true)
    private int port;

    public String address() {
        return this.address;
    }

    public int port() {
        return this.port;
    }

    public InetSocketAddress socketAddress() throws Exception {
        InetAddress address = InetAddress.getByName(this.address);
        return new InetSocketAddress(address, this.port);
    }
}
