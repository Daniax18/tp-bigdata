/*
  Datum Academy - eMBDS - Hadoop, Spark and map/reduce
                     2020
  --
  WCountMap.java : le Mapper. Pour chaque ligne du texte, il émet un couple (mot, 1) par mot.
*/
package org.mbds.hadoop.wordcount;                          // même paquetage que le driver

import org.apache.hadoop.mapreduce.Job;                     // import inutilisé ici (hérité de l'archive)
import org.apache.hadoop.io.Text;                           // type Hadoop pour une chaîne
import org.apache.hadoop.io.IntWritable;                    // type Hadoop pour un entier
import java.util.StringTokenizer;                           // découpe une chaîne en morceaux (tokens)
import org.apache.hadoop.mapreduce.Mapper;                  // classe de base de tout Mapper
import java.io.IOException;                                 // exception possible lors de l'écriture


public class WCountMap extends Mapper<Object, Text, Text, IntWritable> // types : <clé entrée, valeur entrée, clé sortie, valeur sortie>
{                                                           // entrée = (position dans le fichier, ligne) ; sortie = (mot, 1)
	private static final IntWritable ONE=new IntWritable(1); // constante 1, créée une seule fois pour éviter de réallouer à chaque mot

	protected void map(Object key, Text value, Context context) throws IOException, InterruptedException // appelée une fois par ligne (64 fois pour poem.txt)
	{
		StringTokenizer tok=new StringTokenizer(value.toString(), " "); // découpe la ligne sur les espaces uniquement (pas la ponctuation, pas la casse)
		while(tok.hasMoreTokens())                          // tant qu'il reste des mots dans la ligne
		{
			Text word=new Text(tok.nextToken());            // mot suivant, converti en Text
			context.write(word, ONE);                       // émet le couple (mot, 1) → 314 couples au total pour poem.txt
		}
	}
}
