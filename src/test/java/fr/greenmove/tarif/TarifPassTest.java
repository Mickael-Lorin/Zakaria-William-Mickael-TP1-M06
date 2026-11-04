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
        when(compteur.trajetsInclus("alice",JOUR)).thenReturn(1);
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
  

}