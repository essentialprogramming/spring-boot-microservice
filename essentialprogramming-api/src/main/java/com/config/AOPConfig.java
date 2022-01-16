package com.config;

import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AOPConfig {

    @Pointcut("execution(public * *(..))")
    private void anyPublicOperation() {}

    @Pointcut("within(com.api.service..*)")
    private void inService() {}

    @Pointcut("anyPublicOperation() && inService()")
    public void serviceOperation() {}
}