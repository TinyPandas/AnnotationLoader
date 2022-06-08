package org.tinypandas.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * For use on Annotations.
 *
 * Marks an Annotation to be Registered when
 * AnnotationLoader#onStartRegister is invoked.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterOnStart {}