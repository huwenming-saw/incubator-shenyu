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

package org.apache.shenyu.client.grpc;

import org.apache.shenyu.client.core.disruptor.ShenyuClientRegisterEventPublisher;
import org.apache.shenyu.common.enums.RpcTypeEnum;
import org.apache.shenyu.common.utils.IpUtils;
import org.apache.shenyu.register.common.config.PropertiesConfig;
import org.apache.shenyu.register.common.dto.URIRegisterDTO;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.StringUtils;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Grpc context refreshed event listener.
 */
public class GrpcContextRefreshedEventListener implements ApplicationListener<ContextRefreshedEvent> {

    private ShenyuClientRegisterEventPublisher publisher = ShenyuClientRegisterEventPublisher.getInstance();

    private final AtomicBoolean registered = new AtomicBoolean(false);
    
    private final String ipAndPort;

    private final String host;

    private final int port;
    
    private final String contextPath;
    
    /**
     * Instantiates a new Grpc context refreshed event listener.
     *
     * @param config the config
     */
    public GrpcContextRefreshedEventListener(final PropertiesConfig config) {
        Properties props = config.getProps();
        String contextPath = props.getProperty("contextPath");
        String ipAndPort = props.getProperty("ipAndPort");
        String port = props.getProperty("port");
        if (StringUtils.isEmpty(contextPath) || StringUtils.isEmpty(ipAndPort) || StringUtils.isEmpty(port)) {
            throw new RuntimeException("grpc client must config the contextPath, ipAndPort, port");
        }
        this.ipAndPort = ipAndPort;
        this.contextPath = contextPath;
        this.host = props.getProperty("host");
        this.port = Integer.parseInt(port);
    }
    
    @Override
    public void onApplicationEvent(final ContextRefreshedEvent contextRefreshedEvent) {
        if (!registered.compareAndSet(false, true)) {
            return;
        }
        publisher.publishEvent(buildURIRegisterDTO());
    }
    
    private URIRegisterDTO buildURIRegisterDTO() {
        String host = IpUtils.isCompleteHost(this.host) ? this.host : IpUtils.getHost(this.host);
        return URIRegisterDTO.builder()
                .contextPath(this.contextPath)
                .appName(this.ipAndPort)
                .rpcType(RpcTypeEnum.GRPC.getName())
                .host(host)
                .port(port)
                .build();
    }
}
