package fr.greenmove.tarif;

import java.time.LocalDate;

public class TarifPass {

    private static final Montant DEVEROUILLAGE = Montant.euros(1.00);
    private static final Montant PRIX_MINUTE_VELO = Montant.euros(0.20);
    private static final Montant PRIX_MINUTE_TROTTINETTE = Montant.euros(0.25);
    private static final Montant PLAFOND = Montant.euros(15.00);
    private static final int MINUTES_GRATUITES = 20;
    private static final int TRAJETS_INCLUS_PAR_JOUR = 2;
    private static final int POURCENT_REDUCTION_ABONNE = 20;
    private final Abonnements abonnements;
    private final CompteurTrajets compteur;
    private final Horloge horloge;


    public TarifPass(Abonnements abonnements, CompteurTrajets compteur, Horloge horloge) {
        this.abonnements = abonnements;
        this.compteur = compteur;
        this.horloge = horloge;

    }

    public Montant prix(String usager, TypeVehicule type, int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("durée négative");
        }

        Montant tarifMinute = type == TypeVehicule.VELO ? PRIX_MINUTE_VELO : PRIX_MINUTE_TROTTINETTE;

        LocalDate jour = horloge.aujourdhui();
    if (abonnements.aUnPassActif(usager, jour) && compteur.trajetsInclus(usager, jour) < TRAJETS_INCLUS_PAR_JOUR) {
            compteur.enregistrerTrajetInclus(usager, jour);
            int minutesFacturees = Math.max(0, minutes - MINUTES_GRATUITES);
            Montant resteAPayer = tarifMinute.fois(minutesFacturees).plafonneA(PLAFOND);
            if (abonnements.estAbonne(usager)) {
                return resteAPayer.moinsPourcent(POURCENT_REDUCTION_ABONNE);
            }
            return resteAPayer;
        }

        Montant total = DEVEROUILLAGE.plus(tarifMinute.fois(minutes)).plafonneA(PLAFOND);
        if (abonnements.estAbonne(usager)) {
            return total.moinsPourcent(POURCENT_REDUCTION_ABONNE);
        }
        return total;
    }

}