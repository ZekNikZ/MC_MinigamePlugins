package io.zkz.mc.minigameplugins.gametools.reflection;

import io.zkz.mc.minigameplugins.gametools.GTPlugin;
import io.zkz.mc.minigameplugins.gametools.command.CommandRegistry;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.util.Pair;
import org.bukkit.permissions.Permission;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.ReflectionUtilsPredicates;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;

import static org.reflections.ReflectionUtils.Fields;
import static org.reflections.ReflectionUtils.Methods;
import static org.reflections.scanners.Scanners.MethodsAnnotated;
import static org.reflections.scanners.Scanners.TypesAnnotated;

@SuppressWarnings({"java:S3011"})
public class ReflectionHelper {
    private ReflectionHelper() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends GTPlugin<T>> List<PluginService<T>> findAllServices(ClassLoader loader, GTPlugin<T> plugin) {
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackage(plugin.getClass().getPackageName(), loader)
        );

        Set<Pair<PluginService<T>, Integer>> res = new HashSet<>();
        Set<Class<?>> potentialServiceClasses = reflections.getTypesAnnotatedWith(Service.class);
        for (Class<?> potentialServiceClass : potentialServiceClasses) {
            Service annotation = potentialServiceClass.getAnnotation(Service.class);

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
                service = potentialMethods.stream().findAny().orElseThrow(IllegalAccessException::new).invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                plugin.getLogger().log(Level.WARNING, "Potential service class " + potentialServiceClass.getCanonicalName() + " has multiple getInstance methods.", e);
            }

            int priority = annotation.priority();
            res.add(new Pair<>((PluginService<T>) service, priority));
        }

        return res.stream()
            .sorted(Comparator.comparing((Pair<PluginService<T>, Integer> p) -> p.second()).reversed()) //NOSONAR
            .map(Pair::first)
            .toList();
    }

    public static void findAndRegisterCommands(ClassLoader loader, GTPlugin<?> plugin, CommandRegistry registry) {
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackage(plugin.getClass().getPackageName(), loader)
                .addScanners(MethodsAnnotated)
        );

        reflections.get(MethodsAnnotated.with(RegisterCommands.class)
            .as(Method.class)
            .filter(
                ReflectionUtilsPredicates.withReturnType(Void.TYPE)
                    .and(ReflectionUtilsPredicates.withParameters(CommandRegistry.class))
                    .and(ReflectionUtilsPredicates.withStatic())
            )
        ).forEach(method -> {
            try {
                method.setAccessible(true);
                method.invoke(null, registry);
            } catch (IllegalAccessException | InvocationTargetException e) {
                plugin.getLogger().log(Level.WARNING, "Could not register commands using method " + method.getName() + " in " + method.getDeclaringClass().getCanonicalName(), e);
            }
        });
    }

    public static List<Permission> findPermissions(ClassLoader loader, GTPlugin<?> plugin) {
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackage(plugin.getClass().getPackageName(), loader)
                .addScanners(TypesAnnotated)
        );

        List<Permission> res = new ArrayList<>();
        reflections.get(TypesAnnotated.with(RegisterPermissions.class)
            .asClass()
        ).forEach(clazz ->
            reflections.get(Fields.of(clazz)
                .as(Field.class)
                .filter(
                    ReflectionUtilsPredicates.withType(Permission.class)
                        .and(ReflectionUtilsPredicates.withStatic())
                )
            ).forEach(field -> {
                if (!Modifier.isFinal(field.getModifiers())) {
                    plugin.getLogger().warning("Field" + field.getName() + " in " + field.getDeclaringClass().getCanonicalName() + " will be registered as a permission node but is not final.");
                }

                try {
                    field.setAccessible(true);
                    res.add((Permission) field.get(null));
                } catch (IllegalAccessException e) {
                    plugin.getLogger().log(Level.WARNING, "Could not register permission in field " + field.getName() + " in " + field.getDeclaringClass().getCanonicalName(), e);
                }
            }));

        return res;
    }
}
