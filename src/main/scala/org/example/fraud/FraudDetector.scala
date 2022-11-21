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

package org.example.fraud

import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector
import org.apache.flink.walkthrough.common.entity.Alert
import org.apache.flink.walkthrough.common.entity.Transaction
import org.apache.flink.api.common.state.ValueState
import org.apache.flink.api.common.state.ValueStateDescriptor
import org.apache.flink.configuration.Configuration
// import org.apache.flink.api.scala.typeutils.Types
import io.findify.flink.api._
import io.findify.flinkadt.api._
import org.apache.flink.api.common.functions.RichMapFunction

object FraudDetector {
  val SmallAmount = 1.00
  val LargeAmount = 500.00
  val OneMinute = 60 * 1000L
}

@SerialVersionUID(1L)
class FraudDetector extends KeyedProcessFunction[Long, Transaction, Alert] {
  @transient lazy val flagState =
    getRuntimeContext.getState(new ValueStateDescriptor("flag", classOf[Boolean]))

  @transient lazy val timerState = getRuntimeContext.getState(
    new ValueStateDescriptor("timer-state", classOf[Long])
  )

  @throws[Exception]
  def processElement(
      transaction: Transaction,
      context: KeyedProcessFunction[Long, Transaction, Alert]#Context,
      collector: Collector[Alert]
  ): Unit = {
    // Get the current state for the current key
    Option(flagState.value).foreach { _ =>
      if (transaction.getAmount > FraudDetector.LargeAmount) {
        // Output an alert downstream
        val alert = new Alert
        alert.setId(transaction.getAccountId)

        collector.collect(alert)
        println(s"large amount: ${transaction.getAmount}")
      }
      // Clean up our state
      cleanUp(context)
    }

    if (transaction.getAmount < FraudDetector.SmallAmount) {
      // set the flag to true
      flagState.update(true)

      // set the timer and timer state
      val timer =
        context.timerService.currentProcessingTime + FraudDetector.OneMinute
      context.timerService.registerProcessingTimeTimer(timer)
      timerState.update(timer)
      println(s"small amount: ${transaction.getAmount}")
    }
  }

  override def onTimer(
      timestamp: Long,
      ctx: KeyedProcessFunction[Long, Transaction, Alert]#OnTimerContext,
      out: Collector[Alert]
  ): Unit = {
    // remove flag after 1 minute, assuming that attacker makes fraudulent transactions within a minute
    timerState.clear
    flagState.clear
  }

  @throws[Exception]
  private def cleanUp(
      ctx: KeyedProcessFunction[Long, Transaction, Alert]#Context
  ): Unit = {
    // delete timer
    val timer = timerState.value
    ctx.timerService.deleteProcessingTimeTimer(timer)

    // clean up all states
    timerState.clear
    flagState.clear
  }
}
