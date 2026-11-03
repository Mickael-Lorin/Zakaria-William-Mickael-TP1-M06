package fr.greenmove.tarif;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class TarifPassTest {
    private static final LocalDate JOUR = LocalDate.of(2027, 9, 1);

    @Test
    void un_velo_de_12_minutes_sans_pass_ni_abonnement_coute_le_tarif_normal() {
        Abonnements abonnements = mock(Abonnements.class);
        when(abonnements.estAbonne("alice")).thenReturn(false);
        when(abonnements.aUnPassActif("alice", JOUR)).thenReturn(false);
        CompteurTrajets compteur = mock(CompteurTrajets.class);
        Horloge horloge = () -> JOUR;
        TarifPass tarif = new TarifPass(abonnements, compteur, horloge);

        Montant prix = tarif.prix("alice", TypeVehicule.VELO, 12);

        assertThat(prix).isEqualTo(Montant.euros(3.40));
    }
}
