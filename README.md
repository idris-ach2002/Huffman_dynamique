# Huffman dynamique

Ce dépôt contient une implémentation **Java** de la **compression** et de la **décompression** basées sur l’algorithme de **Huffman adaptatif**.  
---

## Arborescence

```
.
├── src/
│   ├── compression/
│   │   ├── Compression.java
│   │   └── Main.java
│   ├── decompression/
│   │   ├── Decompression.java
│   │   └── Main.java
│   ├── structure/
│   │   └── HuffmanTree.java
│   ├── utils/
│   │   ├── BitBufferedInput.java
│   │   ├── DotGenerator.java
│   │   ├── Fichier.java
│   │   ├── UTF8DecodeException.java
│   │   ├── UTF8Decoder.java
│   │   └── UTF8Reader.java
│   └── Experimentation/
│       └── (lanceur + génération de courbes)
├── lib/
│   ├── jfreechart-1.5.4.jar
│   └── jcommon-1.0.24.jar
├── bin/        (classes compilées)
├── data/       (données d’expérimentation)
├── out/        (sorties/exports)
├── compresser
├── decompresser
└── Makefile
```

---

## Compilation

### Compilation principale
```bash
make
```

### Nettoyage
```bash
make clean
```

### Expérimentations & courbes
Le `Makefile` fournit également :
```bash
make experiment
make plot
```

> Les plots utilisent `jfreechart` / `jcommon` (présents dans `lib/`).

---

## Utilisation

### Compression
Via le script :
```bash
./compresser fichier.txt fichier_compresse.huff
```

Qui écrit et ajoute pour chaque compression, dans le fichier ./compression.txt, les informations suivantes :
- le nom du fichier d’entrée (texte original).
- le nom du fichier de sortie (fichier compressé).
- le nombre d’octets du fichier d’entrée.
- le nombre d’octets du fichier de sortie.
- le taux de compression.
- le temps de compression en millisecondes.


### Décompression
Via le script :
```bash
./decompresser fichier_compresse.huff fichier.txt
```

Qui écrit les ajoute pour chaque décompression, dans le fichier ./decompression.txt les informations analogues à celles citées.

---
## Utils (I/O, debug, visualisation)

- `UTF8Reader`, `UTF8Decoder` : lecture/decodage de symboles UTF‑8
- `BitBufferedInput` : lecture bit-à-bit (utile en décompression)
- `DotGenerator` : génération de `.dot` (Graphviz) pour visualiser l’arbre
- `Fichier` : utilitaires de fichiers (lecture/écriture). Réponses

## Technologies utilisées
```bash
Java 

idris-achabou@idris-achabou-LOQ-15IAX9E:~/git/Huffman_dynamique$ java --version
java 24.0.1 2025-04-15
Java(TM) SE Runtime Environment (build 24.0.1+9-30)
Java HotSpot(TM) 64-Bit Server VM (build 24.0.1+9-30, mixed mode, sharing)

idris-achabou@idris-achabou-LOQ-15IAX9E:~/git/Huffman_dynamique$ javac --version
javac 24.0.1

Système d'exploitation Ubuntu 24.04.3 LTS
```
---