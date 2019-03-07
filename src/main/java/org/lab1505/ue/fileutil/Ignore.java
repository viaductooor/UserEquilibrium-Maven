package org.lab1505.ue.fileutil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Any field with this annotation will not be written in the output file.
 * 
 * see {@link CsvGraphWriter#writeTo(org.jgrapht.Graph, Class, String)}
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Ignore {

}