package com.mycompany.weatherproject;

/** Időjárási adatok forrása; a felület ezen az interfészen keresztül kér adatot. */
public interface IdojarasSzolgaltatas {

    Idojaras aktualis(double lat, double lon) throws IdojarasKivetel;
}
