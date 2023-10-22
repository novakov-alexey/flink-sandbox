//> using dep "org.flinkextended::flink-scala-api:1.17.1_1.1.0"
//> using dep "org.apache.flink:flink-clients:1.17.1"
//> using dep "org.apache.flink:flink-csv:1.17.1"
//> using dep "org.apache.flink:flink-connector-files:1.17.1"
//> using dep "org.apache.flink:flink-table-runtime:1.17.1"
//> using dep "org.apache.flink:flink-table-planner-loader:1.17.1"

import org.apache.flinkx.api.*
import org.apache.flinkx.api.serializers.*
import org.apache.flink.configuration.Configuration

val env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(Configuration())
