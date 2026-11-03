package fr.greenmove.tarif;

/** Un montant en centimes, immuable, jamais négatif. */
public record Montant(long centimes) {
    private static final int PAR_EURO = 100;
    public static final Montant ZERO = new Montant(0);

    public Montant {
        if (centimes < 0) throw new IllegalArgumentException("montant négatif");
    }
    public static Montant euros(double e) { return new Montant(Math.round(e * PAR_EURO)); }
    public Montant plus(Montant a) { return new Montant(centimes + a.centimes); }
    public Montant fois(long n) { return new Montant(centimes * n); }
    public Montant plafonneA(Montant m) { return centimes > m.centimes ? m : this; }
    public Montant moinsPourcent(int p) { return new Montant(centimes - centimes * p / 100); }
    @Override public String toString() { return String.format("%d,%02d €", centimes / PAR_EURO, centimes % PAR_EURO); }
}
