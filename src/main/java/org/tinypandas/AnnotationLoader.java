package org.tinypandas;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinypandas.annotations.LoaderInfo;
import org.tinypandas.annotations.RegisterOnStart;
import org.tinypandas.utility.Loader;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AnnotationLoader processes all Annotations at Runtime.
 * Current Implementations: @RegisterOnStart
 */
public class AnnotationLoader {

    private final Logger logger = LoggerFactory.getLogger(AnnotationLoader.class.getName());
    private final Reflections reflections;
    private final Map<String, Loader> loaders = new HashMap<>();

    /**
     * Constructs a new Instance of AnnotationLoader.
     *
     * This constructor also gathers all classes marked with `@LoaderInfo(isLoader = true)`.
     * @param packageName - The top level package to reflect on.
     */
    public AnnotationLoader(String packageName) {
        this.reflections = new Reflections(packageName);

        Set<Class<?>> loaderClasses = reflections.getTypesAnnotatedWith(LoaderInfo.class);
        logger.info("Registering " + loaderClasses.size() + " loaders.");
        loaderClasses.forEach(loaderClass -> {
            LoaderInfo annotation = loaderClass.getAnnotation(LoaderInfo.class);
            if (annotation.isLoader()) {
                String clazz = loaderClass.getName();
                Loader loader = null;
                try {
                    loader = (Loader) loaderClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    logger.warn("Failed to register loader for " + clazz);
                    logger.warn(e.getMessage());
                }
                loaders.put(clazz, loader);
                logger.info("Registered loader: " + loader + ".");
            }
        });
    }

    /**
     * Gathers all classes annotated with `@RegisterOnStart` and attempts to
     * register them using their defined loader.
     */
    public void onStartRegister() {
        Set<Class<?>> onStartClasses = reflections.getTypesAnnotatedWith(RegisterOnStart.class).stream()
                .filter(clazz -> clazz.getPackageName().contains("annotations"))
                .collect(Collectors.toSet());
        logger.info("Annotations found for onStart registration: " + onStartClasses.size());
        onStartClasses.forEach(onStartClass -> {
            Class<? extends Annotation> annotation = onStartClass.asSubclass(Annotation.class);
            logger.info("Loading classes annotated with '" + annotation + "'");
            Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(annotation);
            logger.info("Classes with " + annotation + ": " + annotatedClasses.size());

            annotatedClasses.forEach(annotatedClass -> {
                logger.info("Loading: " + annotatedClass);
                LoaderInfo loaderAnnotation = annotatedClass.getAnnotation(LoaderInfo.class);
                if (loaderAnnotation == null) {
                    logger.warn("Class annotated with " + annotation + " does not have a defined loader.");
                    return;
                }

                Class<? extends Loader> loaderDefinition = loaderAnnotation.loader();
                Loader loader = loaders.get(loaderDefinition.getName());
                if (loader == null) {
                    logger.warn("Loader for '" + loaderDefinition + "' was not defined.");
                    return;
                }
                loader.registerClass(annotatedClass);
            });
        });
    }
}
