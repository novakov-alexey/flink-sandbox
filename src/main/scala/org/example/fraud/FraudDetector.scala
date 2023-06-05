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
import org.apache.flink.api.common.state.ValueState
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.common.state.ValueStateDescriptor
import org.apache.flink.configuration.Configuration

import org.apache.flink.api._
import org.apache.flink.api.serializers._
import org.slf4j.LoggerFactory

import org.example.Transaction
import org.example.TransactionsSource
import org.example.Alert

import scala.concurrent.duration._

case class FraudStateVars(
    flagState: ValueState[Boolean],
    timerState: ValueState[Long]
):
  def clear(): Unit =
    flagState.clear
    timerState.clear

object FraudDetector:
  val SmallAmount = 1.00
  val LargeAmount = 500.00
  val OneMinute = 1.minute.toMillis

@SerialVersionUID(1L)
class FraudDetector extends KeyedProcessFunction[Long, Transaction, Alert]:
  @transient lazy val logger = LoggerFactory.getLogger(classOf[FraudDetector])

  @transient var fraudState: FraudStateVars = _

  override def open(parameters: Configuration): Unit =
    fraudState = FraudStateVars(
      getRuntimeContext.getState(
        ValueStateDescriptor("flag", classOf[Boolean])
      ),
      getRuntimeContext.getState(
        ValueStateDescriptor("timer-state", classOf[Long])
      )
    )

  @throws[Exception]
  def processElement(
      transaction: Transaction,
      context: KeyedProcessFunction[Long, Transaction, Alert]#Context,
      collector: Collector[Alert]
  ): Unit =
    // Get the current state for the current key
    Option(fraudState.flagState.value).foreach { _ =>
      if transaction.amount > FraudDetector.LargeAmount then
        // Output an alert downstream
        val alert = Alert(transaction.accountId)
        collector.collect(alert)
        logger.info(s"Fraudulent transaction: $transaction")

      // Clean up our state
      cleanUp(context)
    }

    if transaction.amount < FraudDetector.SmallAmount then
      // set the flag to true
      fraudState.flagState.update(true)

      // set the timer and timer state
      val timer =
        context.timerService.currentProcessingTime + FraudDetector.OneMinute
      context.timerService.registerProcessingTimeTimer(timer)
      fraudState.timerState.update(timer)
      logger.info(s"small amount: ${transaction.amount}")

  override def onTimer(
      timestamp: Long,
      ctx: KeyedProcessFunction[Long, Transaction, Alert]#OnTimerContext,
      out: Collector[Alert]
  ): Unit =
    // remove flag after 1 minute, assuming that attacker makes fraudulent transactions within a minute
    fraudState.clear()

  @throws[Exception]
  private def cleanUp(
      ctx: KeyedProcessFunction[Long, Transaction, Alert]#Context
  ): Unit =
    // delete timer
    val timer = fraudState.timerState.value
    ctx.timerService.deleteProcessingTimeTimer(timer)

    // clean up all states
    fraudState.clear()
