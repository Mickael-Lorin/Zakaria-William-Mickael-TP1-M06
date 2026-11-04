package fr.greenmove.tarif;

public class TarifPass {

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
        Montant tarifMinute = type == TypeVehicule.VELO ? Montant.euros(0.20) : Montant.euros(0.25);
        Montant total = Montant.euros(1.00).plus(tarifMinute.fois(minutes)).plafonneA(Montant.euros(15.00));
        if (abonnements.estAbonne(usager)) {
            return total.moinsPourcent(20);
        }
        return total;
    }

}