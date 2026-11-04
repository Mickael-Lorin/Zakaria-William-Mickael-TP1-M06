package fr.greenmove.tarif;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

class TarifPassTest {

    private static final LocalDate JOUR = LocalDate.of(2027, 9, 1);

    private final CompteurTrajets compteur = mock(CompteurTrajets.class);
    private final Horloge horloge = () -> JOUR;

    private record FauxAbonnements(boolean abonne, boolean pass) implements Abonnements {
        @Override public boolean estAbonne(String usager) { return abonne; }
        @Override public boolean aUnPassActif(String usager, LocalDate jour) { return pass; }
    }

    @Test
    void un_velo_de_12_minutes_sans_abonnement_coute_3_euros_40() {
        TarifPass tarif = new TarifPass(new FauxAbonnements(false, false), compteur, horloge);

        Montant prix = tarif.prix("alice", TypeVehicule.VELO, 12);

        assertThat(prix).isEqualTo(Montant.euros(3.40));
    }

    @Test
    void une_trottinette_de_12_minutes_sans_abonnement_coute_4_euros() {
        TarifPass tarif = new TarifPass(new FauxAbonnements(false, false), compteur, horloge);

        Montant prix = tarif.prix("alice", TypeVehicule.VELO, 12);

        assertThat(prix).isEqualTo(Montant.euros(4));
    }
}
