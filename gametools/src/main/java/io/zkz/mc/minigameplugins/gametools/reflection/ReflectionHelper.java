package io.zkz.mc.minigameplugins.gametools.reflection;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.zkz.mc.minigameplugins.gametools.GTPlugin;
import io.zkz.mc.minigameplugins.gametools.command.CommandRegistry;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.util.Pair;
import net.minecraft.commands.CommandSourceStack;
import org.checkerframework.checker.units.qual.C;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.ReflectionUtilsPredicates;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import static org.reflections.ReflectionUtils.Methods;
import static org.reflections.scanners.Scanners.MethodsAnnotated;

public class ReflectionHelper {
    public static <T extends GTPlugin<T>> List<PluginService<T>> findAllServices(ClassLoader loader, GTPlugin<T> plugin) {
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackage(plugin.getClass().getPackageName(), loader)
        );

        Set<Pair<PluginService<T>, Integer>> res = new HashSet<>();
        Set<Class<?>> potentialServiceClasses = reflections.getTypesAnnotatedWith(Service.class);
        for (Class<?> potentialServiceClass : potentialServiceClasses) {
            Service annotation = potentialServiceClass.getAnnotation(Service.class);
            if (!annotation.value().equals(plugin.getName())) {
                plugin.getLogger().warning("Found potential service class " + potentialServiceClass.getCanonicalName() + " but is it not for the right plugin.");
                continue;
            }

            if (!PluginService.class.isAssignableFrom(potentialServiceClass)) {
                plugin.getLogger().warning("Found potential service class " + potentialServiceClass.getCanonicalName() + " but it does not extend PluginService<?>.");
                continue;
            }

            Set<Method> potentialMethods = reflections.get(
                Methods.of(potentialServiceClass)
                    .filter(
                        ReflectionUtilsPredicates.withReturnType(potentialServiceClass)
                            .and(ReflectionUtilsPredicates.withParameters())
                            .and(ReflectionUtilsPredicates.withStatic())
                    )
                    .as(Method.class)
            );
            if (potentialMethods.isEmpty()) {
                plugin.getLogger().warning("Potential service class " + potentialServiceClass.getCanonicalName() + " has no getInstance methods.");
                continue;
            }
            if (potentialMethods.size() > 1) {
                plugin.getLogger().warning("Potential service class " + potentialServiceClass.getCanonicalName() + " has multiple getInstance methods.");
                continue;
            }
            Object service = null;
            try {
                service = potentialMethods.stream().findAny().get().invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                plugin.getLogger().log(Level.WARNING, "Potential service class " + potentialServiceClass.getCanonicalName() + " has multiple getInstance methods.", e);
            }

            int priority = annotation.priority();
            res.add(new Pair<>((PluginService<T>) service, priority));
        }

        return res.stream()
            .sorted(Comparator.comparing((Pair<PluginService<T>, Integer> p) -> p.second()).reversed())
            .map(Pair::first)
            .toList();
    }

    public static void findAllCommands(ClassLoader loader, GTPlugin<?> plugin) {
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackage(plugin.getClass().getPackageName(), loader)
                .addScanners(MethodsAnnotated)
        );

        CommandRegistry registry = new CommandRegistry(plugin);
        reflections.get(MethodsAnnotated.with(RegisterCommands.class)
            .as(Method.class)
            .filter(
                ReflectionUtilsPredicates.withReturnType(Void.TYPE)
                    .and(ReflectionUtilsPredicates.withParameters(CommandRegistry.class))
                    .and(ReflectionUtilsPredicates.withStatic())
            )
        ).forEach(method -> {
            try {
                method.invoke(null, registry);
            } catch (IllegalAccessException | InvocationTargetException e) {
                plugin.getLogger().warning("Could not register commands using method " + method.getName() + " in " + method.getDeclaringClass().getCanonicalName());
            }
        });
    }
}
