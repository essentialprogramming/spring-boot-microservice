package com.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class ApplicationContextWrapper {

    @Getter
    private static ApplicationContext applicationContext;

    @Autowired
    public ApplicationContextWrapper(ApplicationContext ac) {
        applicationContext = ac;
    }
}
