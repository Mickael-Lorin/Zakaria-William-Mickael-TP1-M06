package fr.greenmove.tarif;

public class TarifPass {
    private static final Montant DEVERROUILLAGE = Montant.euros(1.00);
    private static final Montant TARIF_VELO = Montant.euros(0.20);
    private static final Montant TARIF_TROTTINETTE = Montant.euros(0.25);
    private static final Montant PLAFOND = Montant.euros(15.00);

    public TarifPass(Abonnements abonnements, CompteurTrajets compteur, Horloge horloge) {
    }

    public Montant prix(String usager, TypeVehicule type, int minutes) {
        return DEVERROUILLAGE.plus(tarifMinute(type).fois(minutes)).plafonneA(PLAFOND);
    }

    private Montant tarifMinute(TypeVehicule type) {
        return type == TypeVehicule.VELO ? TARIF_VELO : TARIF_TROTTINETTE;
    }
}