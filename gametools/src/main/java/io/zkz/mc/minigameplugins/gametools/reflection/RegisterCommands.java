package io.zkz.mc.minigameplugins.gametools.reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks this method as a command registry. The method may or may not be public, but must have the following signature: <pre>static void methodName(CommandRegistry registry)</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RegisterCommands {
}
