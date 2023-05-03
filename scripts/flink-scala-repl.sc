// supports Scala 2.13 and 3
//> using dep "io.findify::flink-scala-api:1.15-2"

//> using dep "org.apache.flink:flink-clients:1.15.2"
//> using dep "org.apache.flink:flink-streaming-scala_2.12:1.15.2" // It contains one factory class to support Scala Products. Rest Scala code is not really required
//> using dep "org.apache.flink:flink-connector-kafka:1.15.2"
//> using dep "org.apache.flink:flink-csv:1.15.2"

//> using dep "org.apache.flink:flink-table-api-java:1.15.2"
//> using dep "org.apache.flink:flink-table-api-java-bridge:1.15.2"
//> using dep "org.apache.flink:flink-table-runtime:1.15.2"
//> using dep "org.apache.flink:flink-table-planner-loader:1.15.2"

import io.findify.flink.api._
import io.findify.flinkadt.api._

val env = StreamExecutionEnvironment.getExecutionEnvironment
