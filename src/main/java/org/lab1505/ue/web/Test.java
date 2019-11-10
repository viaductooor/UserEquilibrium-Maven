package org.lab1505.ue.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Test {
    public static void main(String[] args) {
        Log logger = LogFactory.getLog(Test.class);
        System.out.println(logger.getClass());
        logger.info("hello");
    }
}
