package fr.greenmove.tarif;

import java.time.LocalDate;

public class TarifPass {

    private static final Montant DEVERROUILLAGE = Montant.euros(1.00);
    private static final Montant TARIF_MINUTE_VELO = Montant.euros(0.20);
    private static final Montant TARIF_MINUTE_TROTTINETTE = Montant.euros(0.25);
    private static final Montant PLAFOND = Montant.euros(15.00);
    private static final int MINUTES_OFFERTES_PASS = 20;
    private static final int TRAJETS_INCLUS_PAR_JOUR = 2;
    private static final int REDUCTION_ABONNE_POURCENT = 20;

    private final Abonnements abonnements;
    private final CompteurTrajets compteur;
    private final Horloge horloge;

    public TarifPass(Abonnements abonnements, CompteurTrajets compteur, Horloge horloge) {
        this.abonnements = abonnements;
        this.compteur = compteur;
        this.horloge = horloge;
    }

    public Montant prix(String usager, TypeVehicule type, int minutes) {
        refuserDureeNegative(minutes);
        LocalDate jour = horloge.aujourdhui();

        Montant total;
        if (trajetInclusDansLePass(usager, jour)) {
            total = tarifPass(type, minutes);
            compteur.enregistrerTrajetInclus(usager, jour);
        } else {
            total = tarifNormal(type, minutes);
        }

        return reductionAbonne(usager, total.plafonneA(PLAFOND));
    }

    // R8
    private void refuserDureeNegative(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("durée négative");
        }
    }

    // R3 et R4
    private boolean trajetInclusDansLePass(String usager, LocalDate jour) {
        return abonnements.aUnPassActif(usager, jour)
                && compteur.trajetsInclus(usager, jour) < TRAJETS_INCLUS_PAR_JOUR;
    }

    // R1
    private Montant tarifNormal(TypeVehicule type, int minutes) {
        return DEVERROUILLAGE.plus(tarifMinute(type).fois(minutes));
    }

    // R3
    private Montant tarifPass(TypeVehicule type, int minutes) {
        int minutesFacturees = Math.max(0, minutes - MINUTES_OFFERTES_PASS);
        return tarifMinute(type).fois(minutesFacturees);
    }

    private Montant tarifMinute(TypeVehicule type) {
        return type == TypeVehicule.VELO ? TARIF_MINUTE_VELO : TARIF_MINUTE_TROTTINETTE;
    }

    // R2 et R6
    private Montant reductionAbonne(String usager, Montant total) {
        if (abonnements.estAbonne(usager)) {
            return total.moinsPourcent(REDUCTION_ABONNE_POURCENT);
        }
        return total;
    }
}
