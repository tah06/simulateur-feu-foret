# Simulation de propagation d'un feu de foret

Implémentation Java d'un simulateur de feu de forêt stochastique avec interface graphique Swing.

## Lancer le projet

Aucune dependance externe, aucun outil de build necessaire : uniquement un JDK 17+.

```bash
# Compiler le code source principal
javac -d out $(find src/main -name "*.java")

# Compiler et lancer les tests
javac -cp out -d out-test $(find src/test -name "*.java")
java -cp out:out-test com.forestfire.ForestFireTests

# Lancer la simulation (config par defaut : config/forest.properties)
java -cp out com.forestfire.app.Main

# Ou avec une configuration specifique
java -cp out com.forestfire.app.Main config/forest-multi-fire.properties
```

Un script `run.sh` fait ces trois etapes en une commande : `./run.sh [chemin-config]`.

## Format du fichier de configuration

Le format .properties (natif au JDK) a été choisi pour garder le projet léger, sans nécessiter de bibliothèques tierces comme Jackson ou Gson.

```properties
grid.height=10
grid.width=15
propagation.probability=0.35
fire.initial=5,7
simulation.seed=42
```

- `grid.height` / `grid.width` : dimensions de la grille.
- `propagation.probability` : probabilite p qu'une case en feu enflamme une case adjacente saine.
- `fire.initial` : positions (ligne,colonne) initialement en feu, separees par `;`. Ex : `2,3;5,5`.
- `simulation.seed` (optionnel) : fixe la graine du generateur aleatoire pour rejouer une simulation a l'identique. A retirer pour de l'aleatoire pur.

Deux exemples fournis : `config/forest.properties` (un seul foyer) et
`config/forest-multi-fire.properties` (plusieurs foyers, probabilite plus forte).

## Architecture

```
com.forestfire
├── model      -> structures de données pures (Grid, CellState, Position)
├── engine     -> logique de propagation stochastique (Simulation)
├── config     -> lecture et validation du fichier de configuration
├── render     -> interface graphique Java Swing (SwingRenderer), isolée du moteur
└── app        -> point d'entrée (Main), assemble les composants
```

## Choix techniques et justifications

Pour éviter les effets de bord liés à l'ordre de parcours de la grille (un feu qui se propagerait en 
cascade lors de la même étape), le moteur calcule systématiquement l'état t+1 sur une 
nouvelle instance de grille en lisant l'état t.

## Tests
Pour maintenir la philosophie "zéro dépendance", les tests s'appuient sur un utilitaire d'assertions maison léger (Assert.java).
La suite unifiée ForestFireTests valide :

* Le modèle : vérification des limites, calcul du voisinage (centre, bords, coins).

* Le moteur : comportements aux bornes (probabilité de 0 ou 1), règle métier (une case en cendre ne brûle plus).

* La configuration : parsing des coordonnées, rejets des paramètres invalides.
