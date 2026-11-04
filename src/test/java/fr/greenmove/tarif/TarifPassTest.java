package fr.greenmove.tarif;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TarifPassTest {

    private final Abonnements abonnements = mock(Abonnements.class);
    private final CompteurTrajets compteur = mock(CompteurTrajets.class);
    private final Horloge horloge = () -> LocalDate.of(2026, 11, 3);

    private final TarifPass tarifPass = new TarifPass(abonnements, compteur, horloge);

    private static final LocalDate JOUR = LocalDate.of(2026, 11, 3);

    private void alice_est_abonnee() {
        when(abonnements.estAbonne("alice")).thenReturn(true);
    }
    private void alice_a_un_pass_et_deja_fait(int trajets) {
        when(abonnements.aUnPassActif("alice", JOUR)).thenReturn(true);
        when(compteur.trajetsInclus("alice",JOUR)).thenReturn(trajets);
    }

    @Test
    void un_velo_de_12_minutes_sans_pass_ni_abonnement_coute_3_40_euros() {
        Montant prix = tarifPass.prix("alice", TypeVehicule.VELO, 12);

        assertEquals(Montant.euros(3.40), prix);
    }
    @Test
    void une_trottinette_de_10_minutes_coute_3_50_euros() {
        Montant prix = tarifPass.prix("alice", TypeVehicule.TROTTINETTE, 10);

        assertEquals(Montant.euros(3.50), prix);
    }

    @Test
    void un_velo_de_90_minutes_coute_15_euros_plafond_atteint(){
        Montant prix = tarifPass.prix("alice", TypeVehicule.VELO, 90);
        assertEquals(Montant.euros(15.00), prix);
    }

    @Test
    void un_velo_atteint_le_plafond_exactement(){
        Montant prix = tarifPass.prix("alice", TypeVehicule.VELO, 70);
        assertEquals(Montant.euros(15.00), prix);
    }

    @Test
    void un_abonne_beneficie_de_20_pour_cent_de_reduction() {
        alice_est_abonnee();

        Montant prix = tarifPass.prix("alice", TypeVehicule.VELO, 12);

        assertEquals(Montant.euros(2.72), prix);
    }

    @Test
    void une_duree_negative_est_refusee_et_rien_n_est_enregistre() {
        alice_a_un_pass_et_deja_fait(0);

        IllegalArgumentException erreur = assertThrows(IllegalArgumentException.class,
                () -> tarifPass.prix("alice", TypeVehicule.VELO, -1));

        assertEquals("durée négative", erreur.getMessage());
        verify(compteur, never()).enregistrerTrajetInclus(any(), any());
    }

    @Test
    void le_premier_trajet_pass_de_12_minutes_est_gratuit_et_enregistrer(){
        alice_a_un_pass_et_deja_fait(0);

        Montant prix = tarifPass.prix("alice", TypeVehicule.VELO, 12);
        assertEquals(Montant.euros(0.00), prix);
        verify(compteur).enregistrerTrajetInclus("alice", JOUR);
    }

    @Test
    void un_trajt_pass_de_30_minutes_facture_les_10_minutes_au_dela_de_20(){
        alice_a_un_pass_et_deja_fait(1);
        Montant prix = tarifPass.prix("alice", TypeVehicule.VELO, 30);
        assertEquals(Montant.euros(2.00), prix);
    }

    @Test
    void la_vingtieme_minute_est_encore_offerte() {
        alice_a_un_pass_et_deja_fait(0);
        Montant prix = tarifPass.prix("alice", TypeVehicule.VELO, 20);
        assertEquals(Montant.euros(0.00), prix);
    }

    @Test
    void la_vingt_et_unieme_minute_est_facturee() {
        alice_a_un_pass_et_deja_fait(0);
        Montant prix = tarifPass.prix("alice", TypeVehicule.VELO, 21);
        assertEquals(Montant.euros(0.20), prix);
    }
    @Test
    void une_trotinette_pass_de_25_minutes_facture_5_minutes_a_0_25_euros(){
        alice_a_un_pass_et_deja_fait(0);
        Montant prix = tarifPass.prix("alice",TypeVehicule.TROTTINETTE, 25);
        assertEquals(Montant.euros(1.25), prix);
    }

    @Test
    void le_troisieme_trajet_du_jour_est_au_tarif_normal_et_non_enregistre(){
        alice_a_un_pass_et_deja_fait(2);
        Montant prix = tarifPass.prix("alice", TypeVehicule.VELO, 12);
        assertEquals(Montant.euros(3.40), prix);
        verify(compteur, never()).enregistrerTrajetInclus(any(),any());
    }

    @Test
    void la_reduction_abonne_s_applique_au_reste_a_payer_du_pass(){
        alice_est_abonnee();
        alice_a_un_pass_et_deja_fait(1);

        Montant prix = tarifPass.prix("alice", TypeVehicule.VELO, 30);

        assertEquals(Montant.euros(1.60), prix);

    }

}