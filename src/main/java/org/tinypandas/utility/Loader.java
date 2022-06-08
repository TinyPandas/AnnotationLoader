package org.tinypandas.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Loader {

    private static final Logger logger = LoggerFactory.getLogger(Loader.class.getName());

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