package org.example

import io.findify.flink.api._
import io.findify.flinkadt.api._

import org.apache.flink.api.java.ExecutionEnvironment

@main def job =
  val env = ExecutionEnvironment.getExecutionEnvironment
  env.fromElements(1, 2, 3, 4, 5, 6).filter(_ % 2 == 1).map(i => i * i).print
  env.execute()
