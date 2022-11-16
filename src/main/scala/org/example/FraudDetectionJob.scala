/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.example

//import org.apache.flink.streaming.api.scala._
import io.findify.flink.api._
import io.findify.flinkadt.api._
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.walkthrough.common.sink.AlertSink
import org.apache.flink.walkthrough.common.source.TransactionSource
import org.apache.flink.walkthrough.common.entity.Transaction

/** Skeleton code for the DataStream code walkthrough
  */
object FraudDetectionJob extends App {
  implicit val tranTypeInfo: TypeInformation[Transaction] = TypeInformation.of(classOf[Transaction])

  val env = StreamExecutionEnvironment.getExecutionEnvironment

  val transactions = env
    .addSource(new TransactionSource)
    .name("transactions")

  transactions
    .flatMap(t => if (t.getAmount() < 1.0d) List(t, t) else List(t))
    .keyBy(_.getAccountId)
    .map(new RunningAverage)
    .keyBy(_ => "all")
    .reduce { (a, b) =>
      val runningAvg = (a._2 + b._2) / 2
      println(s"average ${Thread.currentThread.getName}: $runningAvg")
      b._1 -> runningAvg
    }
    .name("fraud-detector")

  // val alerts = transactions
  //   .keyBy(_.getAccountId)
  //   .process(new FraudDetector)
  //   .name("fraud-detector")
  // alerts
  //   .addSink(new AlertSink)
  //   .name("send-alerts")

  env.execute("Fraud Detection")
}
