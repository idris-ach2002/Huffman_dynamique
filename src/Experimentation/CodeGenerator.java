package Experimentation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * CodeGenerator
 *
 * Cette classe permet de générer des fichiers structurés réalistes
 * destinés aux expérimentations de Huffman dynamique :
 *
 *   - JSON (objets, tableaux, chaînes)
 *   - Code Python (fonctions, variables, imports)
 *   - Code C (fonctions, boucles, commentaires)
 *
 * Contrairement à une version naïve, cette implémentation n'utilise pas
 * de StringBuilder géant. Elle écrit directement dans le fichier grâce
 * à un BufferedWriter, ce qui permet de générer des fichiers de très
 * grande taille (100 Mo, 500 Mo, 1 Go…) sans surconsommation mémoire.
 */
public class CodeGenerator {

    private static final Random rand = new Random();

    // -------------------------------------------------------------
    // 1) JSON — version bufferisée
    // -------------------------------------------------------------

    /**
     * Génère un JSON contenant un tableau d'objets.
     *
     * Exemple produit :
     *   [
     *     { "id": 0, "name": "obj_0", "value": 123, "tags": ["a","b"] },
     *     { "id": 1, "name": "obj_1", "value": 542, "tags": ["x"] },
     *     ...
     *   ]
     *
     * @param objects nombre d'objets dans le tableau JSON
     * @param path fichier de sortie
     */
    public static void generateJsonToFile(int objects, String path) throws IOException {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path), 65_536)) {

            bw.write("[\n");

            for (int i = 0; i < objects; i++) {

                bw.write("  {\n");
                bw.write("    \"id\": " + i + ",\n");
                bw.write("    \"name\": \"obj_" + i + "\",\n");
                bw.write("    \"value\": " + rand.nextInt(10_000) + ",\n");

                // tableau de tags
                bw.write("    \"tags\": [");

                int tagCount = 1 + rand.nextInt(4);
                for (int t = 0; t < tagCount; t++) {
                    bw.write("\"tag" + rand.nextInt(100) + "\"");
                    if (t < tagCount - 1) bw.write(", ");
                }

                bw.write("]\n");
                bw.write("  }");

                if (i < objects - 1) bw.write(",");
                bw.write("\n");
            }

            bw.write("]\n");
        }
    }

    // -------------------------------------------------------------
    // 2) Code C réaliste — version bufferisée
    // -------------------------------------------------------------

    /**
     * Génère un code C réaliste avec fonctions, variables, boucles.
     *
     * @param lines nombre de lignes approximatif
     * @param path fichier de sortie
     */
    public static void generateCToFile(int lines, String path) throws IOException {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path), 65_536)) {

            bw.write("#include <stdio.h>\n");
            bw.write("#include <stdlib.h>\n\n");

            bw.write("int compute(int x) {\n");
            bw.write("    return x * x + 3;\n");
            bw.write("}\n\n");

            bw.write("int main() {\n");

            for (int i = 0; i < lines; i++) {
                switch (rand.nextInt(4)) {
                case 0:
                    bw.write("    int x" + i + " = " + rand.nextInt(1000) + ";\n");
                    break;
                case 1:
                    bw.write("    printf(\"Value: %d\\n\", compute(" + rand.nextInt(50) + "));\n");
                    break;
                case 2:
                    bw.write("    // commentaire " + i + "\n");
                    break;
                case 3:
                    bw.write("    for (int j = 0; j < 5; j++) { printf(\"j=%d\\n\", j); }\n");
                    break;
                }
            }

            bw.write("    return 0;\n");
            bw.write("}\n");
        }
    }

    // -------------------------------------------------------------
    // 3) Python réaliste — version bufferisée
    // -------------------------------------------------------------

    /**
     * Génère un fichier Python réaliste : imports, variables, fonctions.
     *
     * @param lines nombre de lignes approximatif
     * @param path fichier de sortie
     */
    public static void generatePythonToFile(int lines, String path) throws IOException {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path), 65_536)) {

            bw.write("import math\nimport random\n\n");

            bw.write("def compute(x):\n");
            bw.write("    return x * x + 5\n\n");

            for (int i = 0; i < lines; i++) {
                switch (rand.nextInt(4)) {
                case 0:
                    bw.write("x" + i + " = random.randint(0, 1000)\n");
                    break;
                case 1:
                    bw.write("print(compute(" + rand.nextInt(50) + "))\n");
                    break;
                case 2:
                    bw.write("# commentaire python " + i + "\n");
                    break;
                case 3:
                    bw.write("for j in range(3): print(j)\n");
                    break;
                }
            }
        }
    }

    // -------------------------------------------------------------
    // Exemple d'utilisation
    // -------------------------------------------------------------

    public static void main(String[] args) throws Exception {

        new java.io.File("data").mkdirs();

        generateJsonToFile(50000, "data/generated.json");
        generatePythonToFile(200000, "data/generated.py");
        generateCToFile(200000, "data/generated.c");

        System.out.println("Fichiers réalistes générés !");
    }
}
