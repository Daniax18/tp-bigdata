/*
  Datum Academy - eMBDS - Hadoop, Spark and map/reduce
                     2020
  --
  WCountReduce.java : le Reducer. Il reçoit un mot et tous ses 1, et écrit le total.
*/
package org.mbds.hadoop.wordcount;                          // même paquetage que le driver

import org.apache.hadoop.io.Text;                           // type Hadoop pour une chaîne
import org.apache.hadoop.io.IntWritable;                    // type Hadoop pour un entier
import org.apache.hadoop.mapreduce.Reducer;                 // classe de base de tout Reducer
import java.util.Iterator;                                  // pour parcourir la liste des valeurs
import java.io.IOException;                                 // exception possible lors de l'écriture


public class WCountReduce extends Reducer<Text, IntWritable, Text, Text> // types : <clé entrée, valeur entrée, clé sortie, valeur sortie>
{                                                           // sortie en Text (et pas IntWritable) → ne peut pas servir de combiner
  public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException // appelée une fois par mot distinct (139 fois), clés déjà triées par le shuffle
	{
		Iterator<IntWritable> i=values.iterator();          // parcourt tous les 1 reçus pour ce mot
		int count=0;                                        // compteur d'occurrences
		while(i.hasNext())                                  // pour chaque valeur reçue
			count+=i.next().get();                          // ajoute la valeur (1) au total
		context.write(key, new Text(count+" occurences.")); // écrit « mot <tab> N occurences. » dans part-r-00000
  }
}
