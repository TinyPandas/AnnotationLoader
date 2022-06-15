package org.tinypandas;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinypandas.annotations.LoaderInfo;
import org.tinypandas.annotations.RegisterOnStart;
import org.tinypandas.annotations.RegisterTag;
import org.tinypandas.utility.Loader;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AnnotationLoader processes all Annotations at Runtime.
 * Current Implementations: @RegisterOnStart
 */
public class AnnotationLoader {

    private static final Logger logger = LoggerFactory.getLogger("AnnotationLoader");
    private static final Map<String, Loader> loaders = new HashMap<>();

    private final Reflections reflections;

    public AnnotationLoader(String packageName) {
        reflections = new Reflections(packageName);

        identifyLoaders();
        verifyDefaultLoaders();
        checkTaggedClasses();
    }

    private void identifyLoaders() {
        Set<Class<?>> loaderClasses = reflections.getTypesAnnotatedWith(LoaderInfo.class)
                .stream()
                .filter(loader -> {
                    LoaderInfo annotation = loader.getAnnotation(LoaderInfo.class);
                    return annotation.isLoader();
                })
                .collect(Collectors.toSet());
        logger.info("Registering " + loaderClasses.size() + " loaders.");
        loaderClasses.forEach(loaderClass -> {
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
        });
    }

    private void verifyDefaultLoaders() {
        Set<Class<?>> defaultLoaderClasses = reflections.getTypesAnnotatedWith(LoaderInfo.class).stream()
                .filter(clazz -> {
                    LoaderInfo loaderInfo = clazz.getAnnotation(LoaderInfo.class);
                    return loaderInfo.loader() == Loader.DefaultLoader.class;
                })
                .filter(clazz -> {
                    try {
                        clazz.getDeclaredConstructor();
                        return false;
                    } catch (NoSuchMethodException e) {
                        return true;
                    }
                })
                .collect(Collectors.toSet());

        int size = defaultLoaderClasses.size();
        if (size == 0) return;
        String append = size > 1 ? "es" : "";

        logger.error("Found {} class{} missing a zero-arg constructor while using DefaultLoader.", size, append);
        logger.error(defaultLoaderClasses.stream().map(Class::getName).collect(Collectors.joining(", ")));
        throw new IllegalStateException(String.format("Invalid state of class%s using DefaultLoader.", append));
    }

    private void checkTaggedClasses() {
        Set<Class<?>> untaggedClasses = reflections.getTypesAnnotatedWith(RegisterTag.class).stream()
                .filter(clazz -> {
                    RegisterTag annoTag = clazz.getAnnotation(RegisterTag.class);
                    return annoTag.tag().isBlank();
                })
                .collect(Collectors.toSet());

        int size = untaggedClasses.size();
        if (size == 0) return;

        logger.warn("Found {} class{} without tag.", size, size > 1 ? "es" : "");
        logger.warn(untaggedClasses.stream().map(Class::getName).collect(Collectors.joining(", ")));
    }

    /**
     * Gathers all classes annotated with `@RegisterOnStart` and attempts to
     * register them using their defined loader.
     */
    public void onStartRegister() {
        Set<Class<?>> onStartClasses = reflections.getTypesAnnotatedWith(RegisterOnStart.class);
        logger.info("Annotations found for onStart registration: " + onStartClasses.size());
        handleClassLoader(onStartClasses);
    }

    /**
     * Gathers all classes annotated with `@RegisterTag(tag = tag)` and attempts
     * to register them using their defined loader.
     * @param tag - The tag to register by.
     */
    public void registerByTag(String tag) {
        logger.info("Gathering classes with tag: " + tag);
        Set<Class<?>> taggedClasses = reflections.getTypesAnnotatedWith(RegisterTag.class).stream()
                .filter(clazz -> {
                    RegisterTag annoTag = clazz.getAnnotation(RegisterTag.class);
                    return annoTag.tag().equals(tag);
                })
                .collect(Collectors.toSet());
        int size = taggedClasses.size();
        logger.info("Found {} class{} to register.", size, size > 1 ? "es" : "");

        handleClassLoader(taggedClasses);
    }

    private void handleClassLoader(Set<Class<?>> classesToLoad) {
        logger.info("Loading {} class{}.", classesToLoad.size(), classesToLoad.size() > 1 ? "es" : "");
        classesToLoad.forEach(classToLoad -> {
            LoaderInfo loaderInfo = classToLoad.getAnnotation(LoaderInfo.class);
            if (loaderInfo == null) return;

            Class<? extends Loader> loaderDefinition = loaderInfo.loader();
            Loader loader = loaders.get(loaderDefinition.getName());
            if (loader == null) {
                logger.warn("Loader for '" + loaderDefinition + "' was not defined.");
                return;
            }
            loader.registerClass(classToLoad);
        });
    }
}
