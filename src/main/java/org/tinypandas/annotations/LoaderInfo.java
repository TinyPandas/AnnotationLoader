package org.tinypandas.annotations;

import org.tinypandas.utility.Loader;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface LoaderInfo {
    /**
     * Defines a loader to use when registering the annotated class.
     *
     * Loader.DefaultLoader is used when no specific data needs to be provided
     * from outside the classes obtainable scope. (ie Methods or Fields). If
     * data needs to be provided from Annotations or another source, define a
     * custom Loader.
     *
     * @return - Default: Loader.DefaultLoader.class
     */
    Class<? extends Loader> loader() default Loader.DefaultLoader.class;

    /**
     * Specifies whether the class annotated by this is a Loader or not.
     * @return - Default: false - Not a Loader.
     */
    boolean isLoader() default false;
}