package ABR;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import utils.BitInputStreamOptimized;
import utils.UTF8Reader;

public class Archivage{
	private String inputFile;
	private String outputFile;
	
	
	public Archivage(String inputFile, String outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}
	
	
	
	public String getInputFile() {return inputFile;}
	public void setInputFile(String inputFile) {this.inputFile = inputFile;}
	public String getOutputFile() {return outputFile;}
	public void setOutputFile(String outputFile) {this.outputFile = outputFile;}
	
	private void creerFichierSortie(String fichier) {
		if (!Files.exists(Paths.get(fichier).toAbsolutePath())) {
			try {
				Files.createFile(Paths.get(fichier).toAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	


	
	public void compresser(){
		HuffmanTreeOptimized AHA = new HuffmanTreeOptimized();
		creerFichierSortie(outputFile);
		
        try (
        		// Ouvrir un flux binaire vers le ficher à lire et puis décoder 
        		//les suites binaires en UTF8 (Char <=> Unicode qui est un UTF-16 en java)
        		 BufferedInputStream in = new BufferedInputStream(new FileInputStream(inputFile), 65_536);
                 InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        		
        		 // Écriture optimisée : sortie bufferisée (32K)
                BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(outputFile), 65_536)  ;      		
        	) 
        {

		            int codePoint;
		            int bitBuffer = 0;  // tampon de bits
		            int bitCount = 0;   // nombre de bits dans le tampon
		            
		            while((codePoint = UTF8Reader.readCodePoint(reader)) != -1) {
		            	// Transformer le code point (Unicode) à un caractère à partir de son char UTF-16 java
		            	 String symbol = new String(Character.toChars(codePoint));
		            	 
		                // On a une table de hashage dans HuffmanTreeOptimized pour savoir si un car est présent
		                // Accès en O(1) en moyenne (Pas coûteux)
		                HuffmanTreeOptimized.Leaf feuille = AHA.getCars().get(symbol);
		                String codeBinaire;
		                
		                if(feuille == null) {	
		                	// Transformer le code point (Unicode) d'un caractère à une suite binaire (lisible Base 2)
		                	// caractère n'existe pas (Code de la fuille SP | Code UTF8 du car (en binaire)
			                codeBinaire = AHA.getCarSpecial().getCode() + UTF8Reader.toUTF8Bits(codePoint);
		                }else {
		                	// On commence par écrire son code dans l'arbre avant de l'ajouter (pour le décompresseur)
		                	codeBinaire = feuille.getCode();
		                }
		                
		                for (int i = 0; i < codeBinaire.length(); i++) {
		                    bitBuffer = (bitBuffer << 1) | (codeBinaire.charAt(i) - '0');
		                    bitCount++;

		                    if (bitCount == 8) {
		                        writer.write(bitBuffer);
		                        bitBuffer = 0;
		                        bitCount = 0;
		                    }
		                }
		                
		                AHA.modification(symbol);
		            }
		            // traitement du bourage (Le dernier octet ne contient pas 8 bits (sois moins ou (pas de bourage) dans ce cas on 
		            //ajoute une information pour le décompresseur (le nombre de bits qu'on a rajouté pour atteindre 
		            // 8 bits (perturbation) même dans le cas de 0 bits (on écrit le flag comme quoi on a pas perturbé le dernier octet
		            // Gestion du bourrage (padding)
		            int padding = 0;
		            if (bitCount > 0) {
		                padding = 8 - bitCount;
		                bitBuffer <<= padding;  // décale les bits restants pour remplir un octet
		                writer.write(bitBuffer);
		            }
		            
		            //perturbation (un entier entre 0 et 7)
		            writer.write(padding);
		            
		            writer.flush();
		            
	                //AHA.afficherArbre();
		            
        } catch(IOException ie) {
        	ie.printStackTrace();
        }
        
        /*
        //On transforme le fichier qui représente la suite binaire en String
        //en un fichier binaire et on vire le fichier textuel
        String outputFileBin = outputFile.replace("txt", "bin");
		creerFichierSortie(outputFileBin);
		// Transformation txt -> bin (0010100011111001) -> (28F9)
        Fichier.ecriture(outputFile, outputFileBin);
        //On supprime le fichier textuel (0010100011111001)
        try {
			Files.delete(Paths.get(outputFile).toAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
	/**
	 * Décompresse un fichier binaire (.bin) produit par la méthode compresser
	 * en restaurant le texte original à l’aide d’un arbre de Huffman adaptatif (AHA).
	 * 
	 * Cette méthode lit le flux binaire bit par bit via BitInputStream,
	 * en reconstruisant dynamiquement l’arbre de Huffman à chaque symbole décodé.
	 * Elle gère également le cas du premier symbole (arbre vide au départ)
	 * et le symbole spécial NYT ("Not Yet Transmitted") pour les nouveaux caractères.
	 * 
	 *
	 * Principe de fonctionnement :
	 *
	 *  Lit le fichier binaire compressé correspondant à outputFile.replace("txt", "bin")
	 *  Récupère le dernier octet du fichier (le padding), qui indique combien de bits
	 *  de bourrage ont été ajoutés à la fin du fichier pour compléter le dernier octet.
	 *  
	 *  Calcule le nombre total de bits utiles :
	 *  totalBitsUtiles = ((taille - 1) * 8) - padding
	 *  Repositionne le flux au début et lit les bits un à un jusqu’à atteindre la limite utile.
	 *  Le premier symbole UTF-8 est lu intégralement (sans arbre), puis ajouté à l’AHA.
	 *  Les bits suivants sont décodés soit :
	 *      
	 *     en traversant l’arbre Huffman si le code existe déjà,
	 *     soit après lecture du code NYT, en ajoutant un nouveau caractère (UTF-8 complet).
	 *       
	 *   
	 *  Chaque symbole décodé est écrit dans outputFile (texte UTF-8).
	 * 
	 *
	 * Gestion du bourrage (padding)
	 * Lors de la compression, le dernier octet du fichier contient un entier (0–7)
	 * représentant le nombre de bits inutiles ajoutés à la fin du flux.
	 * Cette méthode lit ce dernier octet et ignore les bits correspondants
	 * pour éviter de décoder des symboles erronés en fin de fichier.
	 
	 */	
	public void decompresser() {
	    HuffmanTreeOptimized AHA = new HuffmanTreeOptimized();
	    
	    String outputDecompressed = inputFile.replace(".txt", "Decompresse.txt");
	    creerFichierSortie(outputDecompressed);

	    File fichier = new File(outputFile);
	    if (!fichier.exists()) {
	        System.err.println("Fichier binaire introuvable : " + outputFile);
	        return;
	    }

	    try (
	        FileInputStream fis = new FileInputStream(outputFile);
	        BufferedWriter writer = new BufferedWriter(new FileWriter(outputDecompressed, StandardCharsets.UTF_8), 65_536);
	    ) {
	        long taille = fichier.length();

	        //exp [01100011][01000011][00000100] taille = 3, padding = 4
	        //totalBitsUtiles = (int)((3 - 1) * 8 - 4)
            // = (int)(16 - 4) = 12 Donc on sait qu’on doit lire 12 bits utiles.
	        
	        
	        // Lire le dernier octet : nombre de bits de bourrage ajoutés
	        fis.getChannel().position(taille - 1);
	        int padding = fis.read();
	        int totalBitsUtiles = (int) ((taille - 1) * 8 - padding);

	        // Revenir au début pour lire les bits utiles uniquement
	        fis.getChannel().position(0);

	        try (BitInputStreamOptimized bitReader = new BitInputStreamOptimized(fis)) {
	            // Premier symbole (UTF-8 complet)
	            String utf8Bits = "";
	            for (int i = 0; i < 8; i++) {
	                int b = bitReader.readBit();
	                if (b == -1) return;
	                utf8Bits += (b == 0 ? "0" : "1");
	            }

	            byte firstByte = (byte) Integer.parseInt(utf8Bits, 2);
	            int nbOctets = UTF8Reader.utf8LengthFromFirstByte(firstByte);
	            byte[] bytes = new byte[nbOctets];
	            bytes[0] = firstByte;

	            for (int i = 1; i < nbOctets; i++) {
	                String nextBits = "";
	                for (int j = 0; j < 8; j++) {
	                    int b = bitReader.readBit();
	                    if (b == -1) return;
	                    nextBits += (b == 0 ? "0" : "1");
	                }
	                bytes[i] = (byte) Integer.parseInt(nextBits, 2);
	            }

	            String symbole = new String(bytes, StandardCharsets.UTF_8);
	            writer.append(symbole);
	            AHA.modification(symbole);

	            int bit;
	            int bitsLus = nbOctets * 8; // Compteur global des bits utiles
	            HuffmanTreeOptimized.Leaf nyt = AHA.getCarSpecial();
	            StringBuilder codeCourant = new StringBuilder();

	            // Lecture bit à bit jusqu’au totalBitsUtiles
	            while (bitsLus < totalBitsUtiles && (bit = bitReader.readBit()) != -1) {
	                bitsLus++;
	                codeCourant.append(bit == 0 ? '0' : '1');

	                if (codeCourant.toString().equals(nyt.getCode())) {
	                    // Cas symbole nouveau
	                    String nouveauBits = "";
	                    for (int i = 0; i < 8; i++) {
	                        int b2 = bitReader.readBit();
	                        if (b2 == -1) return;
	                        nouveauBits += b2 == 0 ? "0" : "1";
	                        bitsLus++;
	                    }

	                    byte first = (byte) Integer.parseInt(nouveauBits, 2);
	                    int n = UTF8Reader.utf8LengthFromFirstByte(first);
	                    byte[] utf8 = new byte[n];
	                    utf8[0] = first;

	                    for (int j = 1; j < n; j++) {
	                        String octetBits = "";
	                        for (int k = 0; k < 8; k++) {
	                            int b3 = bitReader.readBit();
	                            if (b3 == -1) return;
	                            octetBits += b3 == 0 ? "0" : "1";
	                            bitsLus++;
	                        }
	                        utf8[j] = (byte) Integer.parseInt(octetBits, 2);
	                    }

	                    String nouveau = new String(utf8, StandardCharsets.UTF_8);
	                    writer.append(nouveau);
	                    AHA.modification(nouveau);
	                    codeCourant.setLength(0);

	                } else {
	                    HuffmanTreeOptimized.Node feuille = AHA.getNodebyCode(codeCourant.toString());
	                    if (feuille != null && (feuille instanceof HuffmanTreeOptimized.Leaf)) {
	                        // Cas symbole connu
	                        String car = ((HuffmanTreeOptimized.Leaf) feuille).getCaractere();
	                        writer.append(car);
	                        AHA.modification(car);
	                        codeCourant.setLength(0);
	                    }
	                }
	            }

	            System.out.println("Décompression terminée !");
	            System.out.println("Bits lus : " + bitsLus + "/" + totalBitsUtiles + " (padding ignoré = " + padding + ")");
	            //AHA.afficherArbre();
	        }

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}



    public static void main(String[] args) throws IOException {
    	Archivage comp = new Archivage("src/resources/livre.txt", "src/resources/livreCompresse.bin");
    	//Archivage comp = new Archivage("src/resources/Carambar.txt", "src/resources/CarambarCompresse.bin");
    	long deb = System.currentTimeMillis();
    	comp.compresser();
    	long fin = System.currentTimeMillis();
    	
    	System.out.println("Compression à pris => " + ((double)(fin - deb) / 1000.0) + "S");
    	
    	deb = System.currentTimeMillis();
    	comp.decompresser();
    	fin = System.currentTimeMillis();
    	
    	System.out.println("Décompression à pris => " + ((double)(fin - deb) / 1000.0) + "S");
    	//114 285 963  bytes
    }
    
}
