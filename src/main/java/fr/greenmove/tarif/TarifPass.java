package fr.greenmove.tarif;

import java.time.LocalDate;

/** Calcule le prix d'une location GreenMove, avec ou sans Pass Mobilité. */
public class TarifPass {
    private static final Montant DEVERROUILLAGE = Montant.euros(1.00);
    private static final Montant TARIF_VELO = Montant.euros(0.20);
    private static final Montant TARIF_TROTTINETTE = Montant.euros(0.25);
    private static final Montant PLAFOND = Montant.euros(15.00);
    private static final int REDUCTION_ABONNE_POURCENT = 20;
    private static final int MINUTES_OFFERTES_PASS = 20;
    private static final int TRAJETS_INCLUS_PAR_JOUR = 2;

    private final Abonnements abonnements;
    private final CompteurTrajets compteur;
    private final Horloge horloge;

    public TarifPass(Abonnements abonnements, CompteurTrajets compteur, Horloge horloge) {
        this.abonnements = abonnements;
        this.compteur = compteur;
        this.horloge = horloge;
    }

    public Montant prix(String usager, TypeVehicule type, int minutes) {
        refuserDureeNegative(minutes);                                  // R8
        LocalDate jour = horloge.aujourdhui();

        Montant montant;
        if (trajetInclusDansLePass(usager, jour)) {                     // R3, R4
            compteur.enregistrerTrajetInclus(usager, jour);             // R5
            montant = tarifPass(type, minutes);
        } else {
            montant = tarifNormal(type, minutes);                       // R1
        }
        return reductionAbonne(usager, montant.plafonneA(PLAFOND));     // R1, R7 puis R2, R6
    }

    private static void refuserDureeNegative(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("durée négative");
        }
    }

    private boolean trajetInclusDansLePass(String usager, LocalDate jour) {
        return abonnements.aUnPassActif(usager, jour)
                && compteur.trajetsInclus(usager, jour) < TRAJETS_INCLUS_PAR_JOUR;
    }

    private static Montant tarifNormal(TypeVehicule type, int minutes) {
        return DEVERROUILLAGE.plus(tarifMinute(type).fois(minutes));
    }

    private static Montant tarifPass(TypeVehicule type, int minutes) {
        int minutesFacturees = Math.max(0, minutes - MINUTES_OFFERTES_PASS);
        return tarifMinute(type).fois(minutesFacturees);
    }

    private Montant reductionAbonne(String usager, Montant montant) {
        return abonnements.estAbonne(usager) ? montant.moinsPourcent(REDUCTION_ABONNE_POURCENT) : montant;
    }

    private static Montant tarifMinute(TypeVehicule type) {
        return type == TypeVehicule.VELO ? TARIF_VELO : TARIF_TROTTINETTE;
    }
}
