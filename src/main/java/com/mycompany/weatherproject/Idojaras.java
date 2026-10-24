package com.mycompany.weatherproject;

import java.util.Objects;

/** Egy adott helyen és időpontban mért időjárási adatok. */
public record Idojaras(double homerseklet, double szel, String idopont) {

    public Idojaras {
        Objects.requireNonNull(idopont, "Az időpont nem lehet null.");
    }
}
