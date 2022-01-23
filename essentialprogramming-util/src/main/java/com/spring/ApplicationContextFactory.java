package com.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextFactory implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    private ApplicationContextFactory() {
    }


    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }


    public static <T> T getBean(Class<T> clazz) {
        assertContextInjected();
        return applicationContext.getBean(clazz);
    }

    public static <T> T getBean(String s, Class<T> type) {
        assertContextInjected();
        return applicationContext.getBean(s, type);
    }

    public static void assertContextInjected() {
        if (applicationContext == null) {
            throw new RuntimeException("ApplicationContext is not injected");

        }
    }
}