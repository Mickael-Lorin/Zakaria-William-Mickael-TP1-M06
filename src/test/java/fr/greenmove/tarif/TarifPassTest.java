package fr.greenmove.tarif;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TarifPassTest {

    private static final LocalDate JOUR = LocalDate.of(2027, 9, 1);

    private final CompteurTrajets compteur = mock(CompteurTrajets.class);
    private final Horloge horloge = () -> JOUR;

    private record FauxAbonnements(boolean abonne, boolean pass) implements Abonnements {
        @Override public boolean estAbonne(String usager) { return abonne; }
        @Override public boolean aUnPassActif(String usager, LocalDate jour) { return pass; }
    }

    private static final Abonnements SANS_PASS_NON_ABONNE = new FauxAbonnements(false, false);
    private static final Abonnements SANS_PASS_ABONNE = new FauxAbonnements(true, false);
    private static final Abonnements PASS_NON_ABONNE = new FauxAbonnements(false, true);

    private TarifPass tarifPour(Abonnements abonnements) {
        return new TarifPass(abonnements, compteur, horloge);
    }

    @Test
    void un_velo_de_12_minutes_sans_abonnement_coute_3_euros_40() {
        TarifPass tarif = tarifPour(SANS_PASS_NON_ABONNE);

        Montant prix = tarif.prix("alice", TypeVehicule.VELO, 12);

        assertThat(prix).isEqualTo(Montant.euros(3.40));
    }

    @Test
    void une_trottinette_de_12_minutes_sans_abonnement_coute_4_euros() {
        TarifPass tarif = tarifPour(SANS_PASS_NON_ABONNE);

        Montant prix = tarif.prix("alice", TypeVehicule.TROTTINETTE, 12);

        assertThat(prix).isEqualTo(Montant.euros(4.00));
    }

    @Test
    void un_velo_de_90_minutes_est_plafonne_a_15_euros() {
        TarifPass tarif = tarifPour(SANS_PASS_NON_ABONNE);

        Montant prix = tarif.prix("alice", TypeVehicule.VELO, 90);

        assertThat(prix).isEqualTo(Montant.euros(15.00));
    }

    @Test
    void un_velo_de_70_minutes_atteint_exactement_le_plafond_de_15_euros() {
        TarifPass tarif = tarifPour(SANS_PASS_NON_ABONNE);

        Montant prix = tarif.prix("alice", TypeVehicule.VELO, 70);

        assertThat(prix).isEqualTo(Montant.euros(15.00));
    }

    @Test
    void un_abonne_paie_un_velo_de_12_minutes_2_euros_72() {
        TarifPass tarif = tarifPour(SANS_PASS_ABONNE);

        Montant prix = tarif.prix("alice", TypeVehicule.VELO, 12);

        assertThat(prix).isEqualTo(Montant.euros(2.72));
    }

    @Test
    void avec_le_pass_le_premier_trajet_velo_de_12_minutes_est_gratuit() {
        when(compteur.trajetsInclus("alice", JOUR)).thenReturn(0);
        TarifPass tarif = tarifPour(PASS_NON_ABONNE);

        Montant prix = tarif.prix("alice", TypeVehicule.VELO, 12);

        assertThat(prix).isEqualTo(Montant.euros(0.00));
    }

    @Test
    void avec_le_pass_une_trottinette_de_25_minutes_coute_1_euro_25() {
        when(compteur.trajetsInclus("alice", JOUR)).thenReturn(0);
        TarifPass tarif = tarifPour(PASS_NON_ABONNE);

        Montant prix = tarif.prix("alice", TypeVehicule.TROTTINETTE, 25);

        assertThat(prix).isEqualTo(Montant.euros(1.25));
    }

    @Test
    void avec_le_pass_un_velo_de_20_minutes_est_gratuit() {
        when(compteur.trajetsInclus("alice", JOUR)).thenReturn(0);
        TarifPass tarif = tarifPour(PASS_NON_ABONNE);

        Montant prix = tarif.prix("alice", TypeVehicule.VELO, 20);

        assertThat(prix).isEqualTo(Montant.euros(0.00));
    }

    @Test
    void avec_le_pass_un_velo_de_21_minutes_coute_20_centimes() {
        when(compteur.trajetsInclus("alice", JOUR)).thenReturn(0);
        TarifPass tarif = tarifPour(PASS_NON_ABONNE);

        Montant prix = tarif.prix("alice", TypeVehicule.VELO, 21);

        assertThat(prix).isEqualTo(Montant.euros(0.20));
    }
}
