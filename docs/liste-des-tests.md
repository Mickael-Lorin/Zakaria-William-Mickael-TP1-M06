# Liste des tests de `TarifPass` (branche `test-zak`)


| # | Règle | Test | Attendu | Statut |
|---|---|---|---|---|
| 1 | R1 | un vélo de 12 minutes sans abonnement coûte 3,40 € | 3,40 € | fait |
| 2 | R1 | une trottinette de 12 minutes sans abonnement coûte 4,00 € | 4,00 € | fait |
| 3 | R1 | un vélo de 90 minutes est plafonné à 15,00 € | 15,00 € | fait |
| 4 | R1 (limite) | un vélo de 70 minutes atteint exactement le plafond de 15,00 € | 15,00 € | fait, vert d'emblée |
| 5 | R2 | un abonné paie un vélo de 12 minutes 2,72 € | 2,72 € | fait |
| 6 | R3 | avec le Pass, le 1er trajet vélo de 12 minutes est gratuit | 0,00 € | fait |
| 7 | R3 | avec le Pass, une trottinette de 25 minutes coûte 1,25 € | 1,25 € | fait |
| 8 | R3 (limite) | avec le Pass, un vélo de 20 minutes est gratuit (20e minute offerte) | 0,00 € | fait, vert d'emblée |
| 9 | R3 (limite) | avec le Pass, un vélo de 21 minutes coûte 0,20 € | 0,20 € | fait, vert d'emblée |
| 10 | R3 | avec le Pass, le 2e trajet vélo de 30 minutes coûte 2,00 € | 2,00 € | fait, vert d'emblée |
| 11 | R4 | avec le Pass, le 3e trajet vélo de 12 minutes est au tarif normal | 3,40 € | fait |
| 12 | R5 | un trajet inclus gratuit est enregistré une fois au compteur, pour le jour de l'horloge | verify(times(1)) | fait |
| 13 | R5 | le 3e trajet du jour n'est pas enregistré au compteur | verify(never()) | fait, vert d'emblée |
| 14 | R5 | un trajet sans Pass n'est pas enregistré au compteur | verify(never()) | fait, vert d'emblée |
| 15 | R6 | avec le Pass et abonné, le 1er trajet vélo de 30 minutes coûte 1,60 € | 1,60 € | fait |
| 16 | R7 | avec le Pass, un vélo de 120 minutes est plafonné à 15,00 € | 15,00 € | fait |
| 17 | R8 | une durée négative est refusée | IllegalArgumentException("durée négative") | fait |
| 18 | R8 | une durée négative n'est jamais enregistrée au compteur | verify(never()) | fait |

## Doubles choisis

- `Abonnements` : un **fake** (record `FauxAbonnements`) — on ne fait que lui poser des questions.
- `Horloge` : une **lambda** qui renvoie toujours le 1er septembre 2027 — le jour est figé, le test est déterministe.
- `CompteurTrajets` : un **mock Mockito** — stub de `trajetsInclus` (lecture) et `verify` / `never` sur
  `enregistrerTrajetInclus` (ce que `TarifPass` fait, règle R5).


