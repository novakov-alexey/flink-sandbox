package org.example

import org.apache.flinkx.api.*
import org.apache.flinkx.api.serializers.*

@main def job =
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  env.fromElements(1, 2, 3, 4, 5, 6).filter(_ % 2 == 1).map(i => i * i).print()
  env.execute()
    
