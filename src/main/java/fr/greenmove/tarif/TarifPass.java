package fr.greenmove.tarif;

public class TarifPass {
    private static final Montant DEVERROUILLAGE = Montant.euros(1.00);
    private static final Montant TARIF_VELO = Montant.euros(0.20);
    private static final Montant TARIF_TROTTINETTE = Montant.euros(0.25);
    private static final Montant PLAFOND = Montant.euros(15.00);
    private static final int REDUCTION_ABONNE = 20;
    private static final int MINUTES_OFFERTES = 20;
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
        if (minutes < 0) {
            throw new IllegalArgumentException("durée négative");
        }
        Montant montant;
        if (abonnements.aUnPassActif(usager, horloge.aujourdhui())
                && compteur.trajetsInclus(usager, horloge.aujourdhui()) < TRAJETS_INCLUS_PAR_JOUR) {
            compteur.enregistrerTrajetInclus(usager, horloge.aujourdhui());
            montant = tarifMinute(type).fois(Math.max(0, minutes - MINUTES_OFFERTES)).plafonneA(PLAFOND);
        } else {
            montant = DEVERROUILLAGE.plus(tarifMinute(type).fois(minutes)).plafonneA(PLAFOND);
        }
        if (abonnements.estAbonne(usager)) {
            return montant.moinsPourcent(REDUCTION_ABONNE);
        }
        return montant;
    }

    private Montant tarifMinute(TypeVehicule type) {
        return type == TypeVehicule.VELO ? TARIF_VELO : TARIF_TROTTINETTE;
    }
}
