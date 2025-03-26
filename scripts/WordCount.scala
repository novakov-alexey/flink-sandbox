//> using scala 3.3.5

//> using packaging.packageType assembly
//> using packaging.provided org.apache.flink:flink-clients
//> using packaging.provided org.scala-lang:scala-library
//> using packaging.provided org.scala-lang:scala-reflect
//> using packaging.provided org.scala-lang:scala3-library_3

//> using dep "org.flinkextended::flink-scala-api:1.18.1_1.2.4"
//> using dep "org.apache.flink:flink-clients:1.18.1"

import org.apache.flinkx.api.*
import org.apache.flinkx.api.serializers.*
import org.slf4j.LoggerFactory

@main def wordCount =
  val logger = LoggerFactory.getLogger(this.getClass())
  val env = StreamExecutionEnvironment.getExecutionEnvironment

  val text = env.fromElements(
    "To be, or not to be,--that is the question:--",
    "Whether 'tis nobler in the mind to suffer",
    "The slings and arrows of outrageous fortune",
    "Or to take arms against a sea of troubles,"
  )

  text
    .flatMap(_.toLowerCase.split("\\W+"))
    .map { w =>
      logger.info(w); w
    }
    // .keyBy(_._1)
    // .sum(1)
    .print()

  env.execute("wordCount")
