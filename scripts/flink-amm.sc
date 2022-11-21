import $ivy.`io.findify::flink-scala-api:1.15-2`
import $ivy.`org.apache.flink:flink-clients:1.15.2`
import $ivy.`org.apache.flink:flink-streaming-scala_2.12:1.15.2` // It contains one factory class to support Scala Products. Rest Scala code is not really required

import io.findify.flink.api._
import io.findify.flinkadt.api._

val env = StreamExecutionEnvironment.getExecutionEnvironment
