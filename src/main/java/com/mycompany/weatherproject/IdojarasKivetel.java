package com.mycompany.weatherproject;

public class IdojarasKivetel extends Exception {

    public IdojarasKivetel(String uzenet) {
        super(uzenet);
    }

    public IdojarasKivetel(String uzenet, Throwable ok) {
        super(uzenet, ok);
    }
}
