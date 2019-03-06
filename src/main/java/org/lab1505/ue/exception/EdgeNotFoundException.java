package org.lab1505.ue.exception;

public class EdgeNotFoundException extends Exception{
    private static final long serialVersionUID = 10000000033355L;

    public EdgeNotFoundException(String message){
        super(message);
    }
}