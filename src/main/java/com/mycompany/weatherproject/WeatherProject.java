/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.weatherproject;

import javax.swing.SwingUtilities;

public class WeatherProject {

    public static void main(String[] args) {
        IdojarasSzolgaltatas szolgaltatas;
        if (args.length == 0 || (args.length == 1 && "--real".equals(args[0]))) {
            szolgaltatas = new OpenMeteoKliens();
        } else if (args.length == 1 && "--mock".equals(args[0])) {
            szolgaltatas = new MockIdojaras();
        } else if (args.length == 1 && "--mock-error".equals(args[0])) {
            szolgaltatas = MockIdojaras.hibaval("Példa hiba: a mock szolgáltatás szándékosan hibát dobott.");
        } else {
            System.err.println("Használat: java -jar WeatherProject.jar [--real | --mock | --mock-error]");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            IdojarasAblak ablak = new IdojarasAblak(szolgaltatas);
            ablak.setVisible(true);
        });
    }
}
