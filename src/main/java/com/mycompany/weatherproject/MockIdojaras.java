package com.mycompany.weatherproject;

import java.util.Objects;

public class MockIdojaras implements IdojarasSzolgaltatas {

    private final Idojaras valasz;
    private final IdojarasKivetel hiba;

    public MockIdojaras() {
        this(new Idojaras(18.5, 11.2, "2026-10-24T12:00"));
    }

    public MockIdojaras(Idojaras valasz) {
        this(Objects.requireNonNull(valasz, "A mockválasz nem lehet null."), null);
    }

    public MockIdojaras(Idojaras valasz, IdojarasKivetel hiba) {
        if (hiba == null) {
            this.valasz = Objects.requireNonNull(valasz, "Sikeres működéshez válasz szükséges.");
        } else {
            this.valasz = valasz;
        }
        this.hiba = hiba;
    }

    public static MockIdojaras hibaval(String uzenet) {
        return new MockIdojaras(null, new IdojarasKivetel(uzenet));
    }

    @Override
    public Idojaras aktualis(double lat, double lon) throws IdojarasKivetel {
        if (hiba != null) {
            throw hiba;
        }
        return valasz;
    }
}
