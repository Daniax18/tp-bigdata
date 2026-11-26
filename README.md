# TP Big Data — Hadoop MapReduce (WordCount)

Exercice guidé du chapitre 2 (MCCI, Master EADL 2) : programmer MapReduce en Java sur un cluster Hadoop Docker.
Avancement : phases **A** et **B1 → B3** terminées.

## Structure

```
tp-bigdata/
├── docker-compose-hadoop.yml      # cluster : namenode, datanode, resourcemanager, nodemanager
├── config                         # configuration Hadoop (correction A0 incluse)
├── data/                          # monté dans le conteneur namenode sous /data
│   ├── poem.txt                   # entrée du WordCount
│   ├── common_words_en.txt        # entrée phase C (anagrammes)
│   ├── graph_input.txt            # entrée phase D (graphe)
│   ├── sales.csv                  # entrée phase E (ventes)
│   └── mapreduce/wordcount/
│       ├── WCount.java            # driver : configure et lance le job
│       ├── WCountMap.java         # map : ligne → (mot, 1)
│       └── WCountReduce.java      # reduce : (mot, [1,1,…]) → « mot  N occurences. »
└── rendu/
    ├── TP_Hadoop_A-B3.pdf         # rendu avec captures
    ├── rendu.html                 # source du PDF
    └── captures/                  # captures d'écran
```

## Architecture

```
            HDFS (stockage)                    YARN (calcul)
   ┌─────────────────────────┐      ┌─────────────────────────────┐
   │ namenode  :9870 (maître)│      │ resourcemanager :8088 (maître)│
   │ datanode     (travailleur)│    │ nodemanager      (travailleur)│
   └─────────────────────────┘      └─────────────────────────────┘
        ./data (poste) ──monté──▶ /data (conteneur namenode)
```

Prérequis : Docker Desktop lancé, image `apache/hadoop:3.4.1`.

## Workflow

### Phase A — Cluster et données

**A0.** Dans `config`, `HADOOP_MAPRED_HOME` vaut `/opt/hadoop` (et pas `$HADOOP_HOME`, que Compose remplace par une chaîne vide). Déjà fait.

**A1. Démarrer le cluster** (PowerShell, dossier `tp-bigdata`)
```powershell
docker compose -f docker-compose-hadoop.yml up -d
docker compose -f docker-compose-hadoop.yml ps --format "{{.Service}}: {{.Status}}"
```
✅ 4 services `Up`. Attendre ~2 min que HDFS et YARN soient prêts.

**A2. Vérifier HDFS et YARN** (dans le conteneur namenode)
```powershell
docker compose -f docker-compose-hadoop.yml exec namenode /bin/bash
```
```bash
hdfs dfsadmin -report | grep -i "live datanodes"
yarn node -list
```
✅ `Live datanodes (1)` et `Total Nodes:1` en `RUNNING`.

**A3. Charger les données dans HDFS**
```bash
hdfs dfs -mkdir -p /user/hadoop/tp/entrees
hdfs dfs -put /data/poem.txt /data/common_words_en.txt /data/graph_input.txt /data/sales.csv /user/hadoop/tp/entrees/
hdfs dfs -ls /user/hadoop/tp/entrees
```
✅ `Found 4 items`.

### Phase B — WordCount

**B1. Compiler et empaqueter**
```bash
mkdir -p /tmp/tp && cd /tmp/tp
rm -rf wordcount && mkdir wordcount
javac -cp "$(hadoop classpath)" -d wordcount /data/mapreduce/wordcount/*.java
jar cf wordcount.jar -C wordcount .
jar tf wordcount.jar
```
✅ Les 3 classes dans `org/mbds/hadoop/wordcount/`.

**B2. Lancer le job sur YARN** (suivi sur http://localhost:8088)
```bash
cd /tmp/tp
yarn jar wordcount.jar org.mbds.hadoop.wordcount.WCount /user/hadoop/tp/entrees/poem.txt /user/hadoop/tp/wordcount
```
✅ `completed successfully` et ces compteurs :

| Compteur | Valeur |
|---|---|
| Map input records | 64 |
| Map output records | 314 |
| Combine input / output records | 0 / 0 |
| Reduce output records | 139 |

**B3. Lire le résultat**
```bash
hdfs dfs -ls /user/hadoop/tp/wordcount
hdfs dfs -cat /user/hadoop/tp/wordcount/part-r-00000 | sed -n '1,6p'
hdfs dfs -cat /user/hadoop/tp/wordcount/part-r-00000 | wc -l
```
✅ `_SUCCESS` + `part-r-00000` (2823 octets), `a  6 occurences.` en tête, 139 lignes.

## Flux MapReduce du WordCount

```
poem.txt (64 lignes)
   │  map : découpe sur les espaces, émet (mot, 1)
   ▼
314 couples (mot, 1)
   │  shuffle : tri et regroupement par mot
   ▼
139 groupes (mot, [1,1,…])
   │  reduce : somme
   ▼
part-r-00000 : 139 lignes « mot <tab> N occurences. »
```

## À savoir

- Le dossier de sortie **ne doit pas exister** : sinon `FileAlreadyExistsException` (B4). Le supprimer avec `hdfs dfs -rm -r /user/hadoop/tp/wordcount`.
- Après un `docker compose … down`, HDFS est vide : refaire la phase A.
- Arrêter le cluster :
  ```powershell
  docker compose -f docker-compose-hadoop.yml down
  ```
