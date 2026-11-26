/*
  Datum Academy - eMBDS - Hadoop, Spark and map/reduce
	             2020
  --
  WCount.java : classe principale (driver). Elle configure le job MapReduce et le soumet à YARN.
  Lancement : yarn jar wordcount.jar org.mbds.hadoop.wordcount.WCount <entrée HDFS> <sortie HDFS>
*/
package org.mbds.hadoop.wordcount;                          // paquetage : donne le chemin org/mbds/hadoop/wordcount/ dans le jar

import org.apache.hadoop.fs.Path;                           // chemin d'un fichier ou dossier HDFS
import org.apache.hadoop.mapreduce.Job;                     // description d'un job MapReduce (classes, types, entrées/sorties)
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;   // indique où lire les données d'entrée
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat; // indique où écrire le résultat
import org.apache.hadoop.conf.Configuration;                // configuration Hadoop (lit core-site, mapred-site, yarn-site…)
import org.apache.hadoop.util.GenericOptionsParser;         // traite les options génériques Hadoop (-D propriété=valeur…)
import org.apache.hadoop.io.Text;                           // type Hadoop sérialisable pour une chaîne de caractères
import org.apache.hadoop.io.IntWritable;                    // type Hadoop sérialisable pour un entier


public class WCount                                         // classe principale du job
{
	public static void main(String[] args) throws Exception // point d'entrée appelé par « yarn jar »
	{
		Configuration conf=new Configuration();             // charge la configuration du cluster
		String[] ourArgs=new GenericOptionsParser(conf, args).getRemainingArgs(); // retire les options -D… et garde nos 2 arguments (entrée, sortie)
		Job job=Job.getInstance(conf, "Compteur de mots v1.0"); // crée le job ; ce nom apparaît dans l'interface YARN (localhost:8088)

		job.setJarByClass(WCount.class);                    // indique à YARN quel jar envoyer aux NodeManagers
		job.setMapperClass(WCountMap.class);                // classe exécutée pendant la phase map
		job.setReducerClass(WCountReduce.class);            // classe exécutée pendant la phase reduce (pas de combiner déclaré → Combine records = 0)

		job.setOutputKeyClass(Text.class);                  // type des clés émises : le mot
		job.setOutputValueClass(IntWritable.class);         // type des valeurs émises par le map : le 1 (le reduce écrit du Text, accepté par TextOutputFormat)

		FileInputFormat.addInputPath(job, new Path(ourArgs[0]));   // 1er argument : fichier d'entrée dans HDFS (poem.txt)
		FileOutputFormat.setOutputPath(job, new Path(ourArgs[1])); // 2e argument : dossier de sortie dans HDFS ; il ne doit pas déjà exister

		if(job.waitForCompletion(true))                     // soumet le job à YARN, affiche la progression et attend la fin
			System.exit(0);                                 // succès : code retour 0
		System.exit(-1);                                    // échec : code retour -1
	}
}
