package fr.greenmove.tarif;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class TarifPassTest {

    private static final LocalDate JOUR = LocalDate.of(2027, 9, 1);
    private static final String USAGER = "alice";

    private final Abonnements abonnements = mock(Abonnements.class);
    private final CompteurTrajets compteur = mock(CompteurTrajets.class);
    private final Horloge horloge = () -> JOUR;
    private final TarifPass tarif = new TarifPass(abonnements, compteur, horloge);

    private void usagerAbonne() {
        when(abonnements.estAbonne(USAGER)).thenReturn(true);
    }

    private void usagerAvecPass(int trajetsDejaFaits) {
        when(abonnements.aUnPassActif(USAGER, JOUR)).thenReturn(true);
        when(compteur.trajetsInclus(USAGER, JOUR)).thenReturn(trajetsDejaFaits);
    }

    // R1 : tarif normal et plafond

    @Test
    void un_velo_de_12_minutes_sans_pass_ni_abonnement_coute_le_tarif_normal() {
        Montant prix = tarif.prix(USAGER, TypeVehicule.VELO, 12);

        assertThat(prix).isEqualTo(Montant.euros(3.40));
    }

    @Test
    void une_trottinette_de_10_minutes_coute_un_euro_plus_25_centimes_la_minute() {
        Montant prix = tarif.prix(USAGER, TypeVehicule.TROTTINETTE, 10);

        assertThat(prix).isEqualTo(Montant.euros(3.50));
    }

    @Test
    void le_prix_d_une_location_normale_est_plafonne_a_15_euros() {
        Montant prix = tarif.prix(USAGER, TypeVehicule.VELO, 90);

        assertThat(prix).isEqualTo(Montant.euros(15.00));
    }

    // R2 : réduction abonné GreenMove

    @Test
    void un_abonne_greenmove_beneficie_de_20_pour_cent_de_reduction() {
        usagerAbonne();

        Montant prix = tarif.prix(USAGER, TypeVehicule.VELO, 12);

        assertThat(prix).isEqualTo(Montant.euros(2.72));
    }

    // R8 : durée négative

    @Test
    void une_duree_negative_est_refusee() {
        assertThatThrownBy(() -> tarif.prix(USAGER, TypeVehicule.VELO, -1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // R3 : trajets inclus du Pass (1er et 2e trajet du jour)

    @Test
    void le_premier_trajet_du_pass_de_12_minutes_est_gratuit() {
        usagerAvecPass(0);

        Montant prix = tarif.prix(USAGER, TypeVehicule.VELO, 12);

        assertThat(prix).isEqualTo(Montant.euros(0.00));
    }

    @Test
    void le_deuxieme_trajet_du_pass_de_30_minutes_ne_facture_que_les_10_dernieres_minutes() {
        usagerAvecPass(1);

        Montant prix = tarif.prix(USAGER, TypeVehicule.VELO, 30);

        assertThat(prix).isEqualTo(Montant.euros(2.00));
    }

    @Test
    void une_trottinette_pass_de_25_minutes_ne_facture_que_5_minutes() {
        usagerAvecPass(0);

        Montant prix = tarif.prix(USAGER, TypeVehicule.TROTTINETTE, 25);

        assertThat(prix).isEqualTo(Montant.euros(1.25));
    }

    // R5 : enregistrement au compteur

    @Test
    void un_trajet_inclus_gratuit_est_enregistre_au_compteur_du_jour() {
        usagerAvecPass(0);

        tarif.prix(USAGER, TypeVehicule.VELO, 12);

        verify(compteur).enregistrerTrajetInclus(USAGER, JOUR);
    }

    // R4 : à partir du 3e trajet, tarif normal

    @Test
    void le_troisieme_trajet_du_jour_avec_le_pass_est_au_tarif_normal() {
        usagerAvecPass(2);

        Montant prix = tarif.prix(USAGER, TypeVehicule.VELO, 12);

        assertThat(prix).isEqualTo(Montant.euros(3.40));
    }

    @Test
    void un_trajet_non_inclus_n_est_jamais_enregistre() {
        usagerAvecPass(2);

        tarif.prix(USAGER, TypeVehicule.VELO, 12);

        verify(compteur, never()).enregistrerTrajetInclus(any(), any());
    }

    // R6 : Pass + abonné

    @Test
    void la_reduction_abonne_s_applique_au_reste_a_payer_du_pass() {
        usagerAvecPass(0);
        usagerAbonne();

        Montant prix = tarif.prix(USAGER, TypeVehicule.VELO, 30);

        assertThat(prix).isEqualTo(Montant.euros(1.60));
    }

    // R7 : plafond avec le Pass

    @Test
    void le_plafond_de_15_euros_s_applique_aussi_aux_trajets_du_pass() {
        usagerAvecPass(0);

        Montant prix = tarif.prix(USAGER, TypeVehicule.VELO, 120);

        assertThat(prix).isEqualTo(Montant.euros(15.00));
    }

    @Test
    void le_plafond_est_applique_avant_la_reduction_abonne() {
        usagerAvecPass(0);
        usagerAbonne();

        Montant prix = tarif.prix(USAGER, TypeVehicule.VELO, 120);

        assertThat(prix).isEqualTo(Montant.euros(12.00));
    }

    // Limites des 20 minutes offertes

    @Test
    void la_vingtieme_minute_du_pass_est_encore_offerte() {
        usagerAvecPass(0);

        Montant prix = tarif.prix(USAGER, TypeVehicule.VELO, 20);

        assertThat(prix).isEqualTo(Montant.euros(0.00));
    }

    @Test
    void la_vingt_et_unieme_minute_du_pass_est_facturee() {
        usagerAvecPass(0);

        Montant prix = tarif.prix(USAGER, TypeVehicule.VELO, 21);

        assertThat(prix).isEqualTo(Montant.euros(0.20));
    }
}
