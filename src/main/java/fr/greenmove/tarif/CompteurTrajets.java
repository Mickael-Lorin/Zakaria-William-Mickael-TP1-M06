package fr.greenmove.tarif;

import java.time.LocalDate;

/** Port : tient le compte des trajets inclus dans le Pass Mobilité, par usager et par jour. */
public interface CompteurTrajets {
    int trajetsInclus(String usager, LocalDate jour);

    void enregistrerTrajetInclus(String usager, LocalDate jour);
}
