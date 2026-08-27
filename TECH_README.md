# README technique — Fibonacci

Documentation d'installation, de configuration et d'exécution du dépôt.
Pour la description fonctionnelle et algorithmique de la partie Python, voir [README.md](README.md).

Le dépôt contient **trois stacks indépendantes** qui traitent le même sujet (comparaison de stratégies de cache sur le calcul de F(n)) :

| Stack | Emplacement | Rôle |
|-------|-------------|------|
| Python | racine du dépôt | CLI + interface Flet, 4 implémentations, suite pytest |
| .NET 9 + Vue 3 | [fibonacci-dotnet/](fibonacci-dotnet/) | API REST + SPA de visualisation |
| Java 21 + Spring Boot 3 | [fibonacci-java/](fibonacci-java/) | API REST + interface rendue serveur (Thymeleaf) |

Le dossier [openspec/](openspec/) contient les specs et propositions de changement (pipeline *spec-driven development*) ; il ne fait pas partie du code exécutable.

---

## 1. Prérequis

### Communs
- **Git**
- Windows / macOS / Linux (les commandes ci-dessous sont données pour **PowerShell** ; les équivalents bash sont indiqués quand la syntaxe diffère)

### Stack Python
| Outil | Version | Vérification |
|-------|---------|--------------|
| Python | 3.10+ (testé en 3.14.3) | `python --version` |
| pip | fourni avec Python | `pip --version` |

### Stack Java
| Outil | Version | Vérification |
|-------|---------|--------------|
| JDK | 21 (LTS, testé en 21.0.12.1 Microsoft) | `java -version` |
| Maven | non requis — le wrapper `mvnw` est versionné | `mvn -version` (facultatif) |

### Stack .NET / Vue
| Outil | Version | Vérification |
|-------|---------|--------------|
| SDK .NET | 9.0 (testé en 9.0.314) | `dotnet --version` |
| Node.js | ^20.19 ou >=22.12 — requis par Vite 8 (testé en 24.14.0) | `node --version` |
| npm | 10+ (testé en 11.9.0) | `npm --version` |

Téléchargements : [SDK .NET 9](https://dotnet.microsoft.com/download/dotnet/9.0) · [Node.js LTS](https://nodejs.org/)

---

## 2. Récupération du dépôt

```powershell
git clone <url-du-depot> Fibonacci
cd Fibonacci
```

---

## 3. Stack Python

### 3.1 Installation

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1      # bash/macOS/Linux : source .venv/bin/activate
pip install -r requirements.txt
```

Dépendances (`requirements.txt`) :
- `sympy>=1.12` — implémentation *fast doubling* de référence
- `flet>=0.24.0` — interface graphique (inutile si vous n'utilisez que la CLI)
- `pytest>=8.0` et `pytest-cov>=5.0` — suite de tests

> Si la PowerShell refuse d'exécuter `Activate.ps1`, autoriser les scripts locaux pour la session :
> `Set-ExecutionPolicy -Scope Process -ExecutionPolicy RemoteSigned`

### 3.2 Utilisation

```powershell
python MyFibonacciTest.py     # CLI : saisie de n, comparatif des 4 implémentations
python fibonacci_flet.py      # interface graphique Flet
```

Bornes appliquées : `n ≤ 35` pour la récursion naïve, `n ≤ 5000` pour les autres.

L'interface Flet persiste l'historique des calculs dans `fibonacci_history.json` à la racine du dépôt (fichier gitignoré, créé au premier lancement).

### 3.3 Tests

```powershell
pytest                    # configuration dans pytest.ini : testpaths = tests
pytest --cov              # avec couverture (pytest-cov)
```

La suite couvre `fibonacci_algorithms.py` (exactitude, concordance entre implémentations, entrées invalides, comportement du cache) et `fibonacci_service.py` (orchestration, cas ignorés, sélection du meilleur résultat).

### 3.4 Organisation des modules

| Fichier | Responsabilité |
|---------|----------------|
| [fibonacci_algorithms.py](fibonacci_algorithms.py) | fonctions pures + `measure_execution` |
| [fibonacci_service.py](fibonacci_service.py) | couche service (modèles, orchestration) |
| [fibonacci_history.py](fibonacci_history.py) | persistance JSON de l'historique des calculs de la GUI |
| [fibonacci_flet.py](fibonacci_flet.py) | vue Flet |
| [MyFibonacciTest.py](MyFibonacciTest.py) | point d'entrée CLI |
| [tests/](tests/) | suite pytest |

---

## 4. Stack .NET 9 + Vue 3

### 4.1 Structure

```
fibonacci-dotnet/
├── Fibonacci.sln
├── src/Fibonacci.Api/            # API ASP.NET Core 9 (controllers)
│   ├── Algorithms/               # 4 implémentations derrière IFibonacciAlgorithm
│   ├── Constants/                # bornes MaxN (5000) et MaxNNaive (35)
│   ├── Controllers/              # FibonacciController
│   ├── Infrastructure/           # BigIntegerJsonConverter (sérialisation BigInteger)
│   ├── Models/                   # records de requête/réponse
│   ├── Services/                 # FibonacciService (orchestration + mesure)
│   └── Program.cs                # DI, CORS, OpenAPI/Scalar, gestion d'erreurs
├── tests/Fibonacci.Tests/        # xUnit + FluentAssertions (53 tests)
└── frontend/                     # Vue 3 + TypeScript + Vite + Tailwind
    └── src/{components,composables,services,types}
```

### 4.2 Installation

```powershell
cd fibonacci-dotnet
dotnet restore Fibonacci.sln
cd frontend
npm install
cd ..
```

### 4.3 Exécution

L'application nécessite **deux processus** : l'API et le serveur de développement Vite.

**Terminal 1 — API** (depuis `fibonacci-dotnet/`) :

```powershell
$env:ASPNETCORE_ENVIRONMENT = "Development"
dotnet run --project src/Fibonacci.Api --urls "http://localhost:5000"
```

```bash
# bash
ASPNETCORE_ENVIRONMENT=Development dotnet run --project src/Fibonacci.Api --urls "http://localhost:5000"
```

**Terminal 2 — frontend** (depuis `fibonacci-dotnet/frontend/`) :

```powershell
npm run dev
```

Puis ouvrir **http://localhost:5173**.

> **Le port 5000 n'est pas optionnel.** `launchSettings.json` est volontairement exclu du dépôt (voir [fibonacci-dotnet/.gitignore](fibonacci-dotnet/.gitignore)), donc aucun profil de lancement n'impose d'URL. Or deux éléments sont câblés sur `http://localhost:5000` :
> - le proxy `/api` du serveur Vite ([frontend/vite.config.ts](fibonacci-dotnet/frontend/vite.config.ts)) ;
> - la politique CORS `AllowVueDev`, qui autorise l'origine `http://localhost:5173` ([Program.cs](fibonacci-dotnet/src/Fibonacci.Api/Program.cs)).
>
> `ASPNETCORE_ENVIRONMENT=Development` est également requis pour exposer la documentation d'API (voir 4.4) : sans lui l'environnement est `Production` et les routes OpenAPI/Scalar renvoient 404.

### 4.4 Documentation d'API (environnement Development uniquement)

| Ressource | URL |
|-----------|-----|
| Scalar (UI interactive) | http://localhost:5000/scalar/v1 |
| Document OpenAPI (JSON) | http://localhost:5000/openapi/v1.json |

### 4.5 Endpoint

`POST /api/fibonacci/compute`

Requête :

```json
{ "n": 30, "includeNaive": true }
```

- `n` : entier dans `[0, 5000]` — hors bornes ⇒ `400` avec un `ValidationProblemDetails`
- `includeNaive` : `false` exclut la récursion naïve du comparatif (défaut `true`). Elle est de toute façon ignorée au-delà de `n = 35`, avec un `skipReason` explicite.

Réponse (extrait réel pour `n = 30`) :

```json
{
  "n": 30,
  "results": [
    {
      "name": "recursive", "label": "Naive Recursive",
      "timeComplexity": "O(2^n)", "spaceComplexity": "O(n)",
      "n": 30, "result": "832040", "timeSeconds": 0.0416894,
      "cacheStats": null, "skipped": false, "skipReason": null
    },
    {
      "name": "memoized", "label": "Memoized (IMemoryCache)",
      "timeComplexity": "O(n)", "spaceComplexity": "O(n)",
      "n": 30, "result": "832040", "timeSeconds": 0.001054,
      "cacheStats": { "hits": 28, "misses": 31, "total": 59, "hitRatio": 0.4745762711864407 },
      "skipped": false, "skipReason": null
    }
  ],
  "allMatch": true,
  "bestName": "Iterative",
  "bestTimeSeconds": 0.0001148
}
```

`result` est une **chaîne** : les valeurs sont des `BigInteger` sérialisés par [BigIntegerJsonConverter](fibonacci-dotnet/src/Fibonacci.Api/Infrastructure/BigIntegerJsonConverter.cs) pour éviter toute perte de précision côté JavaScript.

Test rapide sans frontend :

```powershell
curl -X POST http://localhost:5000/api/fibonacci/compute -H "Content-Type: application/json" -d '{\"n\":30,\"includeNaive\":true}'
```

### 4.6 Algorithmes exposés

| `name` | Label | Temps | Espace | Borne | Cache |
|--------|-------|-------|--------|-------|-------|
| `recursive` | Naive Recursive | O(2ⁿ) | O(n) | 35 | — |
| `memoized` | Memoized (IMemoryCache) | O(n) | O(n) | 5000 | `IMemoryCache`, hits/misses exposés |
| `iterative` | Iterative | O(n) | O(1) | 5000 | — |
| `fast-doubling` | Fast Doubling | O(log n) | O(log n) | 5000 | — |

Le cache mémoïsé est borné à 10 000 entrées (`SizeLimit`), avec expiration glissante de 10 minutes et invalidation O(1) par `CancellationChangeToken` — il est réinitialisé avant chaque mesure pour que les statistiques restent comparables d'un appel à l'autre.

L'ordre d'enregistrement dans le conteneur DI ([Program.cs](fibonacci-dotnet/src/Fibonacci.Api/Program.cs)) détermine l'ordre d'affichage des résultats.

### 4.7 Tests

```powershell
cd fibonacci-dotnet
dotnet test Fibonacci.sln
```

Résultat attendu : **53 tests, 0 échec** (xUnit + FluentAssertions).

### 4.8 Build de production

```powershell
# API
cd fibonacci-dotnet
dotnet publish src/Fibonacci.Api -c Release -o publish

# Frontend (type-check vue-tsc puis bundle Vite dans frontend/dist)
cd frontend
npm run build
```

Scripts npm disponibles : `dev`, `build`, `preview`, `type-check`.

> **TypeScript est volontairement maintenu en 5.x.** `vue-tsc` 3.x se greffe sur `typescript/lib/tsc`, que TypeScript 7 (compilateur natif) n'expose plus : `npm install -D typescript@latest` casse `npm run build` et `npm run type-check` avec `ERR_PACKAGE_PATH_NOT_EXPORTED`. Attendre une version de `vue-tsc` compatible avant de passer en 7.x.

> Le build de l'API utilise `TreatWarningsAsErrors` : tout avertissement du compilateur casse la build. C'est volontaire.

---

## 5. Stack Java 21 + Spring Boot 3

### 5.1 Structure

```
fibonacci-java/
├── pom.xml                       # Spring Boot 3.5, Java 21, -Werror
├── mvnw / mvnw.cmd / .mvn/       # Maven Wrapper (aucune installation de Maven requise)
├── src/main/java/com/bretzelcoder/fibonacci/
│   ├── algorithm/                # 4 implémentations derrière FibonacciAlgorithm
│   ├── config/                   # bornes, cache Caffeine, CORS
│   ├── model/                    # records de requête/réponse
│   ├── service/                  # orchestration + mesure
│   ├── web/                      # contrôleur API, contrôleur Thymeleaf, gestion d'erreurs
│   └── FibonacciApplication.java
├── src/main/resources/
│   ├── templates/index.html      # page Thymeleaf (formulaire + résultats)
│   ├── static/css/app.css
│   └── application.yml
└── src/test/java/                # JUnit 5 + AssertJ + MockMvc (54 tests)
```

### 5.2 Installation

Aucune. Le wrapper télécharge Maven au premier lancement ; seul un **JDK 21** est requis.

```powershell
cd fibonacci-java
.\mvnw verify        # bash/macOS/Linux : ./mvnw verify
```

### 5.3 Exécution

Un **seul processus**, contrairement à la stack .NET : l'interface est rendue par le serveur.

```powershell
cd fibonacci-java
.\mvnw spring-boot:run
```

Puis ouvrir **http://localhost:8080**.

> Le port **8080** est la valeur par défaut de Spring Boot et n'est pas configuré dans `application.yml`. C'est délibéré : l'API .NET est câblée sur 5000 et le serveur Vite sur 5173, donc les trois stacks peuvent tourner simultanément sans collision. Pour en changer ponctuellement : `.\mvnw spring-boot:run "-Dspring-boot.run.arguments=--server.port=9090"`.

L'interface fonctionne **sans JavaScript** : le formulaire est un POST HTML classique et la page ne charge aucun script.

### 5.4 Endpoint

`POST /api/fibonacci/compute` — **contrat identique à celui de l'API .NET** (mêmes noms de champs, même casing, `result` sérialisé en chaîne).

Requête :

```json
{ "n": 30, "includeNaive": true }
```

Réponse (extrait réel pour `n = 30`) :

```json
{
  "n": 30,
  "results": [
    {
      "name": "recursive", "label": "Naive Recursive",
      "timeComplexity": "O(2^n)", "spaceComplexity": "O(n)",
      "n": 30, "result": "832040", "timeSeconds": 0.0499091,
      "cacheStats": null, "skipped": false, "skipReason": null
    },
    {
      "name": "memoized", "label": "Memoized (Caffeine)",
      "timeComplexity": "O(n)", "spaceComplexity": "O(n)",
      "n": 30, "result": "832040", "timeSeconds": 0.0064921,
      "cacheStats": { "hits": 28, "misses": 31, "total": 59, "hitRatio": 0.4745762711864407 },
      "skipped": false, "skipReason": null
    }
  ],
  "allMatch": true,
  "bestName": "Iterative",
  "bestTimeSeconds": 2.59e-05
}
```

Les statistiques de cache (`hits: 28, misses: 31`) sont **exactement celles des stacks Python et .NET** pour `n = 30` — cette concordance est vérifiée par les tests des trois côtés.

Hors bornes ⇒ `400` avec un `ProblemDetail` (RFC 9457) qui porte en plus un champ `message`, afin que le gestionnaire d'erreurs du frontend Vue fonctionne sans modification.

Test rapide :

```powershell
curl -X POST http://localhost:8080/api/fibonacci/compute -H "Content-Type: application/json" -d '{\"n\":30,\"includeNaive\":true}'
```

### 5.5 Algorithmes exposés

| `name` | Label | Temps | Espace | Borne | Cache |
|--------|-------|-------|--------|-------|-------|
| `recursive` | Naive Recursive | O(2ⁿ) | O(n) | 35 | — |
| `memoized` | Memoized (Caffeine) | O(n) | O(n) | 5000 | Caffeine, hits/misses exposés |
| `iterative` | Iterative | O(n) | O(1) | 5000 | — |
| `fast-doubling` | Fast Doubling | O(log n) | O(log n) | 5000 | — |

L'ordre d'affichage vient des annotations `@Order` portées par chaque implémentation : Spring peuple le `List<FibonacciAlgorithm>` injecté dans le service en les triant, comme la DI .NET peuple `IEnumerable<IFibonacciAlgorithm>` par ordre d'enregistrement. Un test `@SpringBootTest` verrouille cette séquence.

Le cache est borné à 10 000 entrées avec expiration de 10 minutes après le dernier accès, et il est vidé avant chaque mesure pour que les statistiques restent comparables d'un appel à l'autre.

> **Pourquoi pas `@Cacheable` ?** L'abstraction cache de Spring repose sur des proxies AOP, qui n'interceptent que les appels venant de l'**extérieur** du bean. L'appel récursif de `memoized(n)` vers `memoized(n-1)` est un appel `this` : il ne traverse jamais le proxy et ne consulte donc jamais le cache. Annoter la méthode `@Cacheable` ne mettrait en cache que l'appel le plus externe et laisserait l'algorithme en O(2ⁿ) — indiscernable de la récursion naïve, mais bien plus déroutant. Le code passe donc directement par l'API Caffeine, ce que fait aussi la version .NET avec `IMemoryCache`.

> **Pourquoi un thread dédié ?** À `n = 5000`, la première passe du mémoïsé descend 5000 niveaux de récursion. Les threads de travail Tomcat n'ont pas la pile pour ça. La récursion tourne donc sur un thread créé avec une pile de 32 Mo, construit une seule fois au démarrage pour que sa création ne soit pas comptée dans la mesure. C'est l'équivalent JVM du `sys.setrecursionlimit` de la stack Python.

### 5.6 Tests

```powershell
cd fibonacci-java
.\mvnw test
```

Résultat attendu : **54 tests, 0 échec** (JUnit 5 + AssertJ + MockMvc).

La suite couvre les algorithmes (exactitude, concordance, entrées invalides, métadonnées déclarées), le comportement du cache (compteurs hits/misses, sémantique du reset, absence de `StackOverflowError` à `n = 5000`), l'orchestration du service, le **contrat JSON champ par champ**, la page Thymeleaf rendue pour de vrai, et le câblage Spring.

> Le test de contrat compare la liste exacte des champs sérialisés à celle déclarée dans [fibonacci-dotnet/frontend/src/types/fibonacci.ts](fibonacci-dotnet/frontend/src/types/fibonacci.ts). Il a déjà servi : Jackson ne sérialise que les *composants* d'un record, pas ses accesseurs dérivés, donc `total` et `hitRatio` manquaient au payload jusqu'à ce qu'ils soient annotés `@JsonProperty`.

### 5.7 Réutiliser le frontend Vue avec l'API Java

Le contrat étant identique, le SPA de la stack .NET fonctionne tel quel contre l'API Java. Il suffit de changer la cible du proxy dans [fibonacci-dotnet/frontend/vite.config.ts](fibonacci-dotnet/frontend/vite.config.ts) de `http://localhost:5000` vers `http://localhost:8080`. Aucune modification du code TypeScript n'est nécessaire : la politique CORS de la stack Java autorise déjà l'origine `http://localhost:5173`.

### 5.8 Build de production

```powershell
cd fibonacci-java
.\mvnw clean package          # produit target/fibonacci-1.0.0.jar (exécutable)
java -jar target/fibonacci-1.0.0.jar
```

> Le build utilise `-Xlint:all -Werror` : tout avertissement du compilateur casse la build, comme le `TreatWarningsAsErrors` de la stack .NET. C'est volontaire.

### 5.9 Note sur les mesures de temps

Les temps affichés sont des mesures **wall-clock uniques prises dans une JVM en cours d'exécution**. Le premier passage d'un algorithme paie la compilation JIT et peut être un ordre de grandeur plus lent que son régime établi — c'est pourquoi `bestName` peut différer de la réponse donnée par les stacks Python ou .NET pour le même `n`, et pourquoi le mémoïsé paraît anormalement lent au premier appel après le démarrage.

Le classement est donc **indicatif, pas un benchmark**. Une mesure rigoureuse sur JVM demande [JMH](https://github.com/openjdk/jmh) (warm-up, itérations multiples, protection contre l'élimination de code mort), ce qui sortirait du modèle requête/réponse partagé par les trois stacks et les rendrait incomparables. La page le rappelle sous le tableau de résultats.

---

## 6. Dépannage

| Symptôme | Cause probable | Correctif |
|----------|----------------|-----------|
| Frontend : erreurs réseau / 404 sur `/api/fibonacci/compute` | API non démarrée, ou démarrée sur un autre port que 5000 | relancer avec `--urls "http://localhost:5000"` |
| `/scalar/v1` renvoie 404 | environnement `Production` | définir `ASPNETCORE_ENVIRONMENT=Development` |
| Erreur CORS dans la console du navigateur | frontend servi ailleurs que sur `http://localhost:5173` | libérer le port 5173, ou ajouter l'origine dans la politique `AllowVueDev` |
| `MSB1009 : Le fichier projet n'existe pas` | commande `dotnet` lancée depuis la racine du dépôt | se placer dans `fibonacci-dotnet/` avant `dotnet run/build/test` |
| Port 5000 déjà occupé | autre service (parfois un service système) | libérer le port, ou changer **conjointement** `--urls`, le proxy Vite et la politique CORS |
| Java : `release version 21 not supported` | JDK antérieur à 21 sur le PATH | installer un JDK 21 et pointer `JAVA_HOME` dessus |
| Java : port 8080 déjà occupé | autre application | `.\mvnw spring-boot:run "-Dspring-boot.run.arguments=--server.port=9090"` |
| Java : `mvnw` refusé sous PowerShell | script non exécutable / chemin | lancer `.\mvnw` depuis `fibonacci-java/`, pas `mvnw` seul |
| `Activate.ps1` bloqué | politique d'exécution PowerShell | `Set-ExecutionPolicy -Scope Process -ExecutionPolicy RemoteSigned` |

---

## 7. Éléments non versionnés

`bin/`, `obj/`, `target/`, `node_modules/`, `dist/`, `__pycache__/`, `.venv/`, `fibonacci_history.json` et `launchSettings.json` sont ignorés (voir [.gitignore](.gitignore), [fibonacci-dotnet/.gitignore](fibonacci-dotnet/.gitignore) et [fibonacci-java/.gitignore](fibonacci-java/.gitignore)). Une installation propre passe donc toujours par `pip install`, `dotnet restore`, `npm install` et `mvnw verify`.
