package fr.greenmove.tarif;

public class TarifPass {
    private static final Montant DEVERROUILLAGE = Montant.euros(1.00);
    private static final Montant TARIF_VELO = Montant.euros(0.20);
    private static final Montant TARIF_TROTTINETTE = Montant.euros(0.25);
    private static final Montant PLAFOND = Montant.euros(15.00);
    private static final int REDUCTION_ABONNE = 20;

    private final Abonnements abonnements;
    private final Horloge horloge;

    public TarifPass(Abonnements abonnements, CompteurTrajets compteur, Horloge horloge) {
        this.abonnements = abonnements;
        this.horloge = horloge;
    }

    public Montant prix(String usager, TypeVehicule type, int minutes) {
        if (abonnements.aUnPassActif(usager, horloge.aujourdhui())) {
            return Montant.ZERO;
        }
        Montant montant = DEVERROUILLAGE.plus(tarifMinute(type).fois(minutes)).plafonneA(PLAFOND);
        if (abonnements.estAbonne(usager)) {
            return montant.moinsPourcent(REDUCTION_ABONNE);
        }
        return montant;
    }

    private Montant tarifMinute(TypeVehicule type) {
        return type == TypeVehicule.VELO ? TARIF_VELO : TARIF_TROTTINETTE;
    }
}
