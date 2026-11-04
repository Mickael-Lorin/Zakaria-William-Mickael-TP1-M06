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

    private void refusDureeNegative(int minutes){
        if (minutes < 0){
            throw new IllegalArgumentException("durée négative");
        }
    }
    private boolean estTrajetInclus(String usager, LocalDate jour){
        return abonnements.aUnPassActif(usager, jour)
                && compteur.trajetsInclus(usager, jour) < TRAJETS_INCLUS_PAR_JOUR;
    }

    private Montant tarifNormal(TypeVehicule type, int minutes){
        return DEVEROUILLAGE.plus(tarifMinute(type).fois(minutes)).plafonneA(PLAFOND);
    }

    private Montant tarifMinute(TypeVehicule type){
        return type == TypeVehicule.VELO ? PRIX_MINUTE_VELO : PRIX_MINUTE_TROTTINETTE;
    }
    private Montant estAbonne(String usager, Montant montant){
        return abonnements.estAbonne(usager) ? montant.moinsPourcent(POURCENT_REDUCTION_ABONNE) : montant;
    }
    private Montant tarifTrajetInclus(TypeVehicule type, int minutes){
        int minutesFacturees = Math.max(0, minutes - MINUTES_GRATUITES);
        return tarifMinute(type).fois(minutesFacturees).plafonneA(PLAFOND);
    }

    public Montant prix(String usager, TypeVehicule type, int minutes) {
        refusDureeNegative(minutes);

        LocalDate jour = horloge.aujourdhui();
        Montant avantReduction;
        if (estTrajetInclus(usager, jour)) {
            compteur.enregistrerTrajetInclus(usager, jour);
            avantReduction = tarifTrajetInclus(type, minutes);
        } else {
            avantReduction = tarifNormal(type, minutes);
        }
        return estAbonne(usager, avantReduction);
    }

}