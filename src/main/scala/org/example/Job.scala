package org.example

import org.apache.flink.api._
import org.apache.flink.api.serializers._

@main def job =
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  env.fromElements(1, 2, 3, 4, 5, 6).filter(_ % 2 == 1).map(i => i * i).print()
  env.execute()
