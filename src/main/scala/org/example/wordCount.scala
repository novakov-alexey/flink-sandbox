package org.example

import org.apache.flink.api._
import org.apache.flink.api.serializers._

@main def wordCountExample =
  val env = StreamExecutionEnvironment.getExecutionEnvironment  
  val text = env.fromElements(
    "To be, or not to be,--that is the question:--",
    "Whether 'tis nobler in the mind to suffer",
    "The slings and arrows of outrageous fortune",
    "Or to take arms against a sea of troubles,"
  )

  text
    .flatMap(_.toLowerCase.split("\\W+"))
    .map((_, 1))
    .keyBy(_._1)
    .sum(1)
    .print()

  env.execute("wordCount")
