name := "storageformats_meetup"
version := "1.0"

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
   {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
   }
}

scalaVersion := "2.10.4"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.6.0" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.6.0" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-hive" % "1.6.0" % "provided"
libraryDependencies += "com.databricks" %% "spark-avro" % "2.0.1" 
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "1.6.0" 
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka" % "1.6.0"
libraryDependencies += "com.databricks" %% "spark-csv" % "1.3.0"
libraryDependencies += "org.apache.spark" %% "spark-streaming-twitter" % "1.6.0"
libraryDependencies += "io.confluent" % "kafka-avro-serializer" % "1.0"

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"
resolvers += Resolver.mavenLocal
resolvers += Resolver.sonatypeRepo("public")
resolvers += "confluent" at "http://packages.confluent.io/maven/"


mergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf")          => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$")      => MergeStrategy.discard
  case "log4j.properties"                                  => MergeStrategy.discard
  case "log4j-defaults.properties"                                  => MergeStrategy.discard
  case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
  case "reference.conf"                                    => MergeStrategy.concat
  case _                                                   => MergeStrategy.first
}