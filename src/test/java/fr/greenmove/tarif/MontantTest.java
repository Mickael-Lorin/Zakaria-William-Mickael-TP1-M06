package fr.greenmove.tarif;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MontantTest {

    @Test
    void un_euro_vaut_cent_centimes() {
        Montant unEuro = Montant.euros(1.00);

        assertEquals(100L, unEuro.centimes());
    }

    @Test
    void deux_euros_cinquante_valent_250_centimes() {
        Montant deuxCinquante = Montant.euros(2.50);

        assertEquals(250L, deuxCinquante.centimes());
    }

    @Test
    void addition_de_deux_montants() {
        Montant somme = Montant.euros(1.00).plus(Montant.euros(2.50));

        assertEquals(new Montant(350), somme);
    }

    @Test
    void multiplication_par_une_duree() {
        Montant produit = Montant.euros(0.20).fois(12);

        assertEquals(new Montant(240), produit);
    }

    @Test
    void un_montant_negatif_est_refuse() {
        IllegalArgumentException erreur =
                assertThrows(IllegalArgumentException.class, () -> new Montant(-1));

        assertEquals("montant négatif", erreur.getMessage());
    }

    @Test
    void dix_huit_euros_plafonnes_a_quinze_donnent_quinze() {
        Montant plafonne = Montant.euros(18.00).plafonneA(Montant.euros(15.00));

        assertEquals(Montant.euros(15.00), plafonne);
    }

    @Test
    void douze_euros_plafonnes_a_quinze_restent_douze() {
        Montant plafonne = Montant.euros(12.00).plafonneA(Montant.euros(15.00));

        assertEquals(Montant.euros(12.00), plafonne);
    }

    @Test
    void dix_euros_moins_vingt_pour_cent_font_huit() {
        Montant remise = Montant.euros(10.00).moinsPourcent(20);

        assertEquals(Montant.euros(8.00), remise);
    }
}
