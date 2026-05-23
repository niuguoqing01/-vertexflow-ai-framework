package com.vertexflow.ai.spring.boot.starter;

import com.vertexflow.ai.core.tool.AiTool;
import com.vertexflow.ai.core.tool.ToolRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class AiToolBeanPostProcessor implements BeanPostProcessor {

    private final ToolRegistry toolRegistry;
    private final Set<String> registeredBeanNames = new HashSet<>();

    public AiToolBeanPostProcessor(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean == null) {
            return null;
        }

        if (bean instanceof ToolRegistry) {
            return bean;
        }

        if (registeredBeanNames.contains(beanName)) {
            return bean;
        }

        if (hasAiToolMethod(bean.getClass())) {
            toolRegistry.register(bean);
            registeredBeanNames.add(beanName);
        }

        return bean;
    }

    private boolean hasAiToolMethod(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(AiTool.class)) {
                return true;
            }
        }

        return false;
    }
}