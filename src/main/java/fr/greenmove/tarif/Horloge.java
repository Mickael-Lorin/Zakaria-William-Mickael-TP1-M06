package fr.greenmove.tarif;

import java.time.LocalDate;

/** Port : donne le jour courant, pour que le domaine n'appelle jamais l'heure système. */
public interface Horloge {
    LocalDate aujourdhui();
}
