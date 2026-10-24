package com.mycompany.weatherproject;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

public class IdojarasAblak extends JFrame {

    private final IdojarasSzolgaltatas szolgaltatas;
    private final JComboBox<Varos> varosValaszto;
    private final JButton frissitesGomb;
    private final JLabel homersekletErtek = new JLabel("—");
    private final JLabel szelErtek = new JLabel("—");
    private final JLabel idopontErtek = new JLabel("—");

    public IdojarasAblak(IdojarasSzolgaltatas szolgaltatas) {
        super("Időjárás");
        this.szolgaltatas = szolgaltatas;

        varosValaszto = new JComboBox<>(Varos.values());
        frissitesGomb = new JButton("Frissítés");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(380, 250));
        setLocationByPlatform(true);
        setContentPane(epitFelhasznaloiFelulet());
        frissitesGomb.addActionListener(event -> frissit());
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel epitFelhasznaloiFelulet() {
        JPanel gyoker = new JPanel(new BorderLayout(12, 12));
        gyoker.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel valasztoPanel = new JPanel(new BorderLayout(8, 0));
        valasztoPanel.add(new JLabel("Város:"), BorderLayout.WEST);
        valasztoPanel.add(varosValaszto, BorderLayout.CENTER);
        valasztoPanel.add(frissitesGomb, BorderLayout.EAST);

        JPanel eredmenyek = new JPanel(new GridLayout(3, 2, 10, 12));
        eredmenyek.setBorder(BorderFactory.createTitledBorder("Aktuális időjárás"));
        eredmenyek.add(new JLabel("Hőmérséklet:"));
        eredmenyek.add(formazottErtek(homersekletErtek));
        eredmenyek.add(new JLabel("Szélsebesség:"));
        eredmenyek.add(formazottErtek(szelErtek));
        eredmenyek.add(new JLabel("Mérés időpontja:"));
        eredmenyek.add(formazottErtek(idopontErtek));

        gyoker.add(valasztoPanel, BorderLayout.NORTH);
        gyoker.add(eredmenyek, BorderLayout.CENTER);
        return gyoker;
    }

    private JLabel formazottErtek(JLabel cimke) {
        cimke.setFont(cimke.getFont().deriveFont(Font.BOLD));
        return cimke;
    }

    private void frissit() {
        Varos varos = (Varos) varosValaszto.getSelectedItem();
        if (varos == null) {
            return;
        }

        frissitesGomb.setEnabled(false);
        frissitesGomb.setText("Lekérés...");

        new SwingWorker<Idojaras, Void>() {
            @Override
            protected Idojaras doInBackground() throws IdojarasKivetel {
                return szolgaltatas.aktualis(varos.latitude(), varos.longitude());
            }

            @Override
            protected void done() {
                frissitesGomb.setEnabled(true);
                frissitesGomb.setText("Frissítés");
                try {
                    megjelenit(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    megjelenitHibat("Az időjárás-lekérés megszakadt.");
                } catch (ExecutionException e) {
                    Throwable ok = e.getCause();
                    String uzenet = ok == null ? e.getMessage() : ok.getMessage();
                    megjelenitHibat(uzenet == null ? "Ismeretlen hiba történt." : uzenet);
                }
            }
        }.execute();
    }

    private void megjelenit(Idojaras idojaras) {
        homersekletErtek.setText(String.format(Locale.forLanguageTag("hu-HU"), "%.1f °C", idojaras.homerseklet()));
        szelErtek.setText(String.format(Locale.forLanguageTag("hu-HU"), "%.1f km/h", idojaras.szel()));
        idopontErtek.setText(idojaras.idopont());
    }

    private void megjelenitHibat(String uzenet) {
        JOptionPane.showMessageDialog(
                this,
                uzenet,
                "Időjárási hiba",
                JOptionPane.ERROR_MESSAGE);
    }

    private enum Varos {
        MAROSVASARHELY("Marosvásárhely", 46.54, 24.56),
        KOLOZSVAR("Kolozsvár", 46.77, 23.59),
        BUDAPEST("Budapest", 47.50, 19.04),
        BUKAREST("Bukarest", 44.43, 26.10),
        SZEGED("Szeged", 46.25, 20.14);

        private final String nev;
        private final double latitude;
        private final double longitude;

        Varos(String nev, double latitude, double longitude) {
            this.nev = nev;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        double latitude() {
            return latitude;
        }

        double longitude() {
            return longitude;
        }

        @Override
        public String toString() {
            return nev;
        }
    }
}
