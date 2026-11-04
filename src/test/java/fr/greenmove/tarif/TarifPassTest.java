package fr.greenmove.tarif;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TarifPassTest {

    private final Abonnements abonnements = mock(Abonnements.class);
    private final CompteurTrajets compteur = mock(CompteurTrajets.class);
    private final Horloge horloge = () -> LocalDate.of(2026, 11, 3);

    private final TarifPass tarifPass = new TarifPass(abonnements, compteur, horloge);

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
}