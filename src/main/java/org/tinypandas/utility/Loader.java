package org.tinypandas.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loader implementation for custom Loader objects.
 */
public abstract class Loader {

    private static final Logger logger = LoggerFactory.getLogger(Loader.class.getName());

    /**
     * Method used to instantiated provided class as instance of T.
     *
     * Example: return clazz.getDeclaredConstructor().newInstance();
     * @param clazz - Class of type T to instantiate.
     * @return A new instance of T.
     * @param <T> The inner class to be instantiated.
     */
    public abstract <T> T registerClass(Class<T> clazz);

    /**
     * The DefaultLoader implementation. This method will construct a class
     * per normal means. (ex: `Object object = new Object();`)
     */
    public static class DefaultLoader extends Loader {
        public <T> T registerClass(Class<T> clazz) {
            logger.info("Registering: " + clazz);
            try {
                logger.info("Successfully registered: " + clazz);
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                logger.warn("Failed to register: " + clazz);
                e.printStackTrace();
                return null;
            }
        }
    }
}