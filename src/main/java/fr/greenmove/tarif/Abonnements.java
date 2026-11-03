package fr.greenmove.tarif;

import java.time.LocalDate;

/** Port : renseigne sur l'abonnement GreenMove et sur le Pass Mobilité d'un usager. */
public interface Abonnements {
    boolean estAbonne(String usager);

    boolean aUnPassActif(String usager, LocalDate jour);
}
