package org.example

import io.findify.flink.api._
import io.findify.flinkadt.api._

@main def wordCount =
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  
  val text = env.fromElements(
    "To be, or not to be,--that is the question:--",
    "Whether 'tis nobler in the mind to suffer",
    "The slings and arrows of outrageous fortune",
    "Or to take arms against a sea of troubles,"
  )

  text.flatMap(_.toLowerCase.split("\\W+")).map((_, 1)).keyBy(_._1).sum(1).print()

  env.execute("wordCount")
