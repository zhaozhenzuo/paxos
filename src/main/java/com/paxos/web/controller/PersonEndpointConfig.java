package com.paxos.web.controller;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by zhenzuo.zzz on 2017/11/5.
 *
 * @author zhenzuo.zzz
 * @date 2017/11/05
 */
@Configuration
public class PersonEndpointConfig {

    @Bean
    @ConditionalOnClass(PersonEndpoint.class)
    @ConditionalOnEnabledEndpoint
    public PersonEndpoint personEndpoint(){
        return new PersonEndpoint();
    }

}
