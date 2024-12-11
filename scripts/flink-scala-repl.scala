//> using dep "org.flinkextended::flink-scala-api:1.19.1_1.2.0"
//> using dep "org.apache.flink:flink-clients:1.19.1"

import org.apache.flinkx.api.*
import org.apache.flinkx.api.serializers.*
import org.slf4j.LoggerFactory
import java.io.File

@main def wordCountExample =
  println("Running wordCountExample")
  val logger = LoggerFactory.getLogger(this.getClass())
  val files = File(".").listFiles ++ Option(File("/flink/lib/").listFiles)
    .getOrElse(Array.empty[File])
  val elems = files.filter(_.isFile).map(_.getAbsolutePath())

  val env = StreamExecutionEnvironment.getExecutionEnvironment
  
  val text = env.fromElements(elems*)  
  //text.addSink(logger.info(_))
  text.print()

  env.execute("wordCount")
