# Suite de Fibonacci — Comparaison d'implémentations Python

Projet d'apprentissage Python centré sur les stratégies de cache et l'optimisation algorithmique, en comparant quatre approches pour calculer le n-ième terme de la suite de Fibonacci.

> Installation, prérequis et exécution de l'ensemble du dépôt (Python **et** stack .NET 9 / Vue 3) : voir [TECH_README.md](TECH_README.md).

**Définition :** $F(n) = F(n-1) + F(n-2)$, avec $F(0) = 0$ et $F(1) = 1$

---

## Architecture

```
fibonacci_algorithms.py   — fonctions pures (4 implémentations + measure_execution)
fibonacci_service.py      — couche service (modèles de données, orchestration)
fibonacci_flet.py         — interface graphique (Flet)
MyFibonacciTest.py        — interface ligne de commande (CLI)
```

---

## Implémentations

| Stratégie | Complexité temps | Complexité espace | Limite |
|-----------|-----------------|-------------------|--------|
| Récursion naïve | O(2ⁿ) | O(n) | n ≤ 35 |
| Récursion mémoïsée (`lru_cache`) | O(n) | O(n) | n ≤ 5 000 |
| Itération optimisée | O(n) | O(1) | n ≤ 5 000 |
| SymPy (`sympy.fibonacci`) | O(log n) | O(1) | n ≤ 5 000 |

### Récursion naïve
Implémentation directe de la définition mathématique. Chaque appel recrée les mêmes sous-arbres de calcul, ce qui entraîne une explosion exponentielle du nombre d'opérations. **Limitée à n ≤ 35 pour éviter un temps de calcul prohibitif.**

### Récursion mémoïsée
Utilise le décorateur `@lru_cache` de la bibliothèque standard pour mettre en cache chaque résultat intermédiaire. Chaque valeur n'est calculée qu'une seule fois.

### Itération optimisée
Ne conserve que les deux dernières valeurs en mémoire — complexité spatiale constante O(1), sans pile de récursion ni cache.

### SymPy
Délègue le calcul à `sympy.fibonacci(n)`, qui utilise l'algorithme **fast doubling** (doublement rapide) basé sur les identités de Lucas. Sa complexité est O(log n), ce qui le rend particulièrement efficace pour de très grandes valeurs de n. Retourne un entier Python natif via `int()`.

---

## Utilisation

### Interface ligne de commande

```bash
python MyFibonacciTest.py
```

Le programme demande une valeur de `n` (défaut : 30), puis affiche pour chacune des implémentations :
- le résultat calculé
- le temps d'exécution mesuré avec `time.perf_counter()`
- les statistiques de cache (hits / misses) pour la version mémoïsée
- un comparatif de performance (facteur d'accélération vs récursion naïve)

### Interface graphique

```bash
python fibonacci_flet.py
```

### Exemple de sortie CLI

```
Entrez la valeur de n (défaut : 30) : 30

# Évaluation pour n = 30

--- Version Récursive Naïve ---
Résultat : 832040
Temps    : 0.2315 secondes

--- Version avec Cache (Mémoïsation) ---
Résultat : 832040
Temps    : 0.000012 secondes
Cache    : Hits=28, Misses=31

--- Version Itérative Ultra Optimisée ---
Résultat : 832040
Temps    : 0.000003 secondes

--- Version SymPy ---
Résultat : 832040
Temps    : 0.000021 secondes

--- Vérification de l'intégrité ---
✅ Succès : les 4 implémentations retournent le même résultat.

--- Comparaison des performances ---
Cache vs Naïve                : 0.005183% du temps (≈ 19 294× plus rapide)
Itérative vs Naïve            : 0.001295% du temps (≈ 77 222× plus rapide)
SymPy vs Naïve                : 0.009071% du temps (≈ 11 025× plus rapide)
Itérative vs Cache            : 24.983740% du temps (≈ 4× plus rapide)
SymPy vs Cache                : 175.000000% du temps (≈ 2× plus lent)
SymPy vs Itérative            : 700.000000% du temps (≈ 7× plus lent)

--- Conclusion ---
🏆 La plus efficace pour n=30 : Itérative (0.000003 s)
```

---

## Prérequis

- Python 3.10+
- Dépendances : `pip install -r requirements.txt`
  - [sympy](https://www.sympy.org/) `>=1.12`
  - [flet](https://flet.dev/) `>=0.24.0` (interface graphique uniquement)
