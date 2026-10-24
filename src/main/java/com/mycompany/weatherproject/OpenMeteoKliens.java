package com.mycompany.weatherproject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

public class OpenMeteoKliens implements IdojarasSzolgaltatas {

    private static final String API_ALAP_URL = "https://api.open-meteo.com/v1/forecast";
    private static final Duration IDOZITES = Duration.ofSeconds(15);

    private final HttpClient httpKliens;

    public OpenMeteoKliens() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build());
    }

    public OpenMeteoKliens(HttpClient httpKliens) {
        this.httpKliens = Objects.requireNonNull(httpKliens, "A HTTP-kliens nem lehet null.");
    }

    @Override
    public Idojaras aktualis(double lat, double lon) throws IdojarasKivetel {
        if (!Double.isFinite(lat) || lat < -90 || lat > 90) {
            throw new IdojarasKivetel("A földrajzi szélességnek -90 és 90 közé kell esnie.");
        }
        if (!Double.isFinite(lon) || lon < -180 || lon > 180) {
            throw new IdojarasKivetel("A földrajzi hosszúságnak -180 és 180 közé kell esnie.");
        }

        URI uri = URI.create(API_ALAP_URL
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&current=temperature_2m,wind_speed_10m");
        HttpRequest keres = HttpRequest.newBuilder(uri)
                .timeout(IDOZITES)
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> valasz = httpKliens.send(
                    keres,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (valasz.statusCode() < 200 || valasz.statusCode() >= 300) {
                throw new IdojarasKivetel(
                        "Az Open-Meteo API HTTP-hibát adott: " + valasz.statusCode() + ".");
            }
            return parseValasz(valasz.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IdojarasKivetel("Az időjárás-lekérés megszakadt.", e);
        } catch (IOException e) {
            throw new IdojarasKivetel("Nem sikerült elérni az Open-Meteo API-t.", e);
        }
    }

    static Idojaras parseValasz(String json) throws IdojarasKivetel {
        try {
            JsonElement gyoker = JsonParser.parseString(json);
            if (!gyoker.isJsonObject()) {
                throw new IllegalArgumentException("A JSON gyökérelemének objektumnak kell lennie.");
            }

            JsonObject aktualis = kotelezoObjektum(gyoker.getAsJsonObject(), "current");
            double homerseklet = kotelezoSzam(aktualis, "temperature_2m");
            double szel = kotelezoSzam(aktualis, "wind_speed_10m");
            String idopont = kotelezoSzoveg(aktualis, "time");

            if (!Double.isFinite(homerseklet) || !Double.isFinite(szel)) {
                throw new IllegalArgumentException("A hőmérsékletnek és a szélsebességnek véges számnak kell lennie.");
            }
            if (idopont.isBlank()) {
                throw new IllegalArgumentException("Az időpont nem lehet üres.");
            }

            return new Idojaras(homerseklet, szel, idopont);
        } catch (RuntimeException e) {
            throw new IdojarasKivetel("Az Open-Meteo válasza hibás vagy hiányos JSON.", e);
        }
    }

    private static JsonObject kotelezoObjektum(JsonObject objektum, String mezo) {
        JsonElement ertek = objektum.get(mezo);
        if (ertek == null || !ertek.isJsonObject()) {
            throw new IllegalArgumentException("Hiányzó vagy érvénytelen objektum: " + mezo);
        }
        return ertek.getAsJsonObject();
    }

    private static double kotelezoSzam(JsonObject objektum, String mezo) {
        JsonPrimitive ertek = kotelezoPrimitiv(objektum, mezo);
        if (!ertek.isNumber()) {
            throw new IllegalArgumentException("A(z) " + mezo + " mezőnek számnak kell lennie.");
        }
        return ertek.getAsDouble();
    }

    private static String kotelezoSzoveg(JsonObject objektum, String mezo) {
        JsonPrimitive ertek = kotelezoPrimitiv(objektum, mezo);
        if (!ertek.isString()) {
            throw new IllegalArgumentException("A(z) " + mezo + " mezőnek szövegnek kell lennie.");
        }
        return ertek.getAsString();
    }

    private static JsonPrimitive kotelezoPrimitiv(JsonObject objektum, String mezo) {
        JsonElement ertek = objektum.get(mezo);
        if (ertek == null || !ertek.isJsonPrimitive()) {
            throw new IllegalArgumentException("Hiányzó vagy érvénytelen mező: " + mezo);
        }
        return ertek.getAsJsonPrimitive();
    }
}
