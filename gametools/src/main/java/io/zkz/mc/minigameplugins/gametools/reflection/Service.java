package io.zkz.mc.minigameplugins.gametools.reflection;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {
    /**
     * The plugin ID to register this service to.
     *
     * @return the plugin ID
     */
    @NotNull String value();

    /**
     * Help to define the registration order. Higher priority services are registered first.
     *
     * @return the priority of the service
     */
    int priority() default 0;
}
