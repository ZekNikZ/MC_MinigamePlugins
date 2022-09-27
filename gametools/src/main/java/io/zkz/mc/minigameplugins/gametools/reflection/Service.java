package io.zkz.mc.minigameplugins.gametools.reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {
    /**
     * Help to define the registration order. Higher priority services are registered first.
     *
     * @return the priority of the service
     */
    int priority() default 0;
}
