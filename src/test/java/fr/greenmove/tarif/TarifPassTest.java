package fr.greenmove.tarif;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TarifPassTest {

    private static final LocalDate JOUR = LocalDate.of(2027, 9, 1);
    private static final String ALICE = "alice";

    private final CompteurTrajets compteur = mock(CompteurTrajets.class);
    private final Horloge horloge = () -> JOUR;

    private record FauxAbonnements(boolean abonne, boolean pass) implements Abonnements {
        @Override public boolean estAbonne(String usager) { return abonne; }
        @Override public boolean aUnPassActif(String usager, LocalDate jour) { return pass; }
    }

    private static final Abonnements SANS_PASS_NON_ABONNE = new FauxAbonnements(false, false);
    private static final Abonnements SANS_PASS_ABONNE = new FauxAbonnements(true, false);
    private static final Abonnements PASS_NON_ABONNE = new FauxAbonnements(false, true);
    private static final Abonnements PASS_ABONNE = new FauxAbonnements(true, true);

    private TarifPass tarifPour(Abonnements abonnements) {
        return new TarifPass(abonnements, compteur, horloge);
    }

    private void trajetsInclusDejaFaits(int nombre) {
        when(compteur.trajetsInclus(ALICE, JOUR)).thenReturn(nombre);
    }

    @Test
    void un_velo_de_12_minutes_sans_abonnement_coute_3_euros_40() {
        TarifPass tarif = tarifPour(SANS_PASS_NON_ABONNE);

        Montant prix = tarif.prix(ALICE, TypeVehicule.VELO, 12);

        assertThat(prix).isEqualTo(Montant.euros(3.40));
    }

    @Test
    void une_trottinette_de_12_minutes_sans_abonnement_coute_4_euros() {
        TarifPass tarif = tarifPour(SANS_PASS_NON_ABONNE);

        Montant prix = tarif.prix(ALICE, TypeVehicule.TROTTINETTE, 12);

        assertThat(prix).isEqualTo(Montant.euros(4.00));
    }

    @Test
    void un_velo_de_90_minutes_est_plafonne_a_15_euros() {
        TarifPass tarif = tarifPour(SANS_PASS_NON_ABONNE);

        Montant prix = tarif.prix(ALICE, TypeVehicule.VELO, 90);

        assertThat(prix).isEqualTo(Montant.euros(15.00));
    }

    @Test
    void un_velo_de_70_minutes_atteint_exactement_le_plafond_de_15_euros() {
        TarifPass tarif = tarifPour(SANS_PASS_NON_ABONNE);

        Montant prix = tarif.prix(ALICE, TypeVehicule.VELO, 70);

        assertThat(prix).isEqualTo(Montant.euros(15.00));
    }

    @Test
    void un_abonne_paie_un_velo_de_12_minutes_2_euros_72() {
        TarifPass tarif = tarifPour(SANS_PASS_ABONNE);

        Montant prix = tarif.prix(ALICE, TypeVehicule.VELO, 12);

        assertThat(prix).isEqualTo(Montant.euros(2.72));
    }

    @Test
    void avec_le_pass_le_premier_trajet_velo_de_12_minutes_est_gratuit() {
        trajetsInclusDejaFaits(0);
        TarifPass tarif = tarifPour(PASS_NON_ABONNE);

        Montant prix = tarif.prix(ALICE, TypeVehicule.VELO, 12);

        assertThat(prix).isEqualTo(Montant.euros(0.00));
    }

    @Test
    void avec_le_pass_une_trottinette_de_25_minutes_coute_1_euro_25() {
        trajetsInclusDejaFaits(0);
        TarifPass tarif = tarifPour(PASS_NON_ABONNE);

        Montant prix = tarif.prix(ALICE, TypeVehicule.TROTTINETTE, 25);

        assertThat(prix).isEqualTo(Montant.euros(1.25));
    }

    @Test
    void avec_le_pass_un_velo_de_20_minutes_est_gratuit() {
        trajetsInclusDejaFaits(0);
        TarifPass tarif = tarifPour(PASS_NON_ABONNE);

        Montant prix = tarif.prix(ALICE, TypeVehicule.VELO, 20);

        assertThat(prix).isEqualTo(Montant.euros(0.00));
    }

    @Test
    void avec_le_pass_un_velo_de_21_minutes_coute_20_centimes() {
        trajetsInclusDejaFaits(0);
        TarifPass tarif = tarifPour(PASS_NON_ABONNE);

        Montant prix = tarif.prix(ALICE, TypeVehicule.VELO, 21);

        assertThat(prix).isEqualTo(Montant.euros(0.20));
    }

    @Test
    void avec_le_pass_le_deuxieme_trajet_velo_de_30_minutes_coute_2_euros() {
        trajetsInclusDejaFaits(1);
        TarifPass tarif = tarifPour(PASS_NON_ABONNE);

        Montant prix = tarif.prix(ALICE, TypeVehicule.VELO, 30);

        assertThat(prix).isEqualTo(Montant.euros(2.00));
    }

    @Test
    void avec_le_pass_le_troisieme_trajet_velo_de_12_minutes_est_au_tarif_normal() {
        trajetsInclusDejaFaits(2);
        TarifPass tarif = tarifPour(PASS_NON_ABONNE);

        Montant prix = tarif.prix(ALICE, TypeVehicule.VELO, 12);

        assertThat(prix).isEqualTo(Montant.euros(3.40));
    }

    @Test
    void un_trajet_inclus_gratuit_est_enregistre_une_fois_au_compteur_pour_le_jour_de_l_horloge() {
        trajetsInclusDejaFaits(0);
        TarifPass tarif = tarifPour(PASS_NON_ABONNE);

        tarif.prix(ALICE, TypeVehicule.VELO, 12);

        verify(compteur, times(1)).enregistrerTrajetInclus(ALICE, JOUR);
    }

    @Test
    void le_troisieme_trajet_du_jour_n_est_pas_enregistre_au_compteur() {
        trajetsInclusDejaFaits(2);
        TarifPass tarif = tarifPour(PASS_NON_ABONNE);

        tarif.prix(ALICE, TypeVehicule.VELO, 12);

        verify(compteur, never()).enregistrerTrajetInclus(anyString(), any());
    }

    @Test
    void un_trajet_sans_pass_n_est_pas_enregistre_au_compteur() {
        TarifPass tarif = tarifPour(SANS_PASS_NON_ABONNE);

        tarif.prix(ALICE, TypeVehicule.VELO, 12);

        verify(compteur, never()).enregistrerTrajetInclus(anyString(), any());
    }

    @Test
    void avec_le_pass_et_abonne_le_premier_trajet_velo_de_30_minutes_coute_1_euro_60() {
        trajetsInclusDejaFaits(0);
        TarifPass tarif = tarifPour(PASS_ABONNE);

        Montant prix = tarif.prix(ALICE, TypeVehicule.VELO, 30);

        assertThat(prix).isEqualTo(Montant.euros(1.60));
    }

    @Test
    void avec_le_pass_un_velo_de_120_minutes_est_plafonne_a_15_euros() {
        trajetsInclusDejaFaits(0);
        TarifPass tarif = tarifPour(PASS_NON_ABONNE);

        Montant prix = tarif.prix(ALICE, TypeVehicule.VELO, 120);

        assertThat(prix).isEqualTo(Montant.euros(15.00));
    }

    @Test
    void une_duree_negative_est_refusee() {
        TarifPass tarif = tarifPour(SANS_PASS_NON_ABONNE);

        Throwable erreur = catchThrowable(() -> tarif.prix(ALICE, TypeVehicule.VELO, -1));

        assertThat(erreur)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("durée négative");
    }

    @Test
    void une_duree_negative_avec_le_pass_n_est_pas_enregistree_au_compteur() {
        trajetsInclusDejaFaits(0);
        TarifPass tarif = tarifPour(PASS_NON_ABONNE);

        catchThrowable(() -> tarif.prix(ALICE, TypeVehicule.VELO, -1));

        verify(compteur, never()).enregistrerTrajetInclus(anyString(), any());
    }
}
