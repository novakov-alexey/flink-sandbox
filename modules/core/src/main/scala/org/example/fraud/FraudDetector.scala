package org.example.fraud

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

import org.apache.flinkx.api.*
import org.apache.flinkx.api.serializers.*

import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.state.api.functions.KeyedStateReaderFunction
import org.apache.flink.state.api.functions.KeyedStateReaderFunction.Context
import org.apache.flink.util.Collector

import org.example.{Alert, Transaction}

import org.slf4j.LoggerFactory
import scala.concurrent.duration.*

import FraudDetector.*

object Givens:
  given tranTypeInfo: TypeInformation[Transaction] =
    deriveTypeInformation[Transaction]
  given keyedStateInfo: TypeInformation[KeyedFraudState] =
    deriveTypeInformation[KeyedFraudState]

object FraudDetector:
  val SmallAmount = 1.00
  val LargeAmount = 500.00
  val OneMinute: Long = 1.minute.toMillis

  def readState(context: RuntimeContext) =
    (
      context.getState(
        ValueStateDescriptor("flag", boolInfo)
      ),
      context.getState(
        ValueStateDescriptor("timer-state", longInfo)
      ),
      context.getState(
        ValueStateDescriptor("last-transaction", Givens.tranTypeInfo)
      )
    )

case class KeyedFraudState(
    key: Long,
    flagState: Boolean,
    timerState: Long,
    lastTransaction: Transaction
)

class ReaderFunction extends KeyedStateReaderFunction[Long, KeyedFraudState]:
  var flagState: ValueState[Boolean] = _
  var timerState: ValueState[Long] = _
  var lastTransaction: ValueState[Transaction] = _

  override def open(parameters: Configuration): Unit =
    val state = readState(getRuntimeContext)
    flagState = state._1
    timerState = state._2
    lastTransaction = state._3

  override def readKey(
      key: Long,
      ctx: Context,
      out: Collector[KeyedFraudState]
  ): Unit =
    out.collect(
      KeyedFraudState(
        key,
        flagState.value(),
        timerState.value(),
        lastTransaction.value()
      )
    )

@SerialVersionUID(1L)
class FraudDetector extends KeyedProcessFunction[Long, Transaction, Alert]:
  @transient lazy val logger = LoggerFactory.getLogger(classOf[FraudDetector])

  var flagState: ValueState[Boolean] = _
  var timerState: ValueState[Long] = _
  var lastTransaction: ValueState[Transaction] = _

  override def open(parameters: Configuration): Unit =
    val state = readState(getRuntimeContext)
    flagState = state._1
    timerState = state._2
    lastTransaction = state._3
    logger.info(s"Loaded last transaction: ${lastTransaction}")

  @throws[Exception]
  def processElement(
      transaction: Transaction,
      context: KeyedProcessFunction[Long, Transaction, Alert]#Context,
      collector: Collector[Alert]
  ): Unit =
    // Get the current state for the current key
    Option(flagState.value).foreach { _ =>
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
      flagState.update(true)

      // set the timer and timer state
      val timer =
        context.timerService.currentProcessingTime + FraudDetector.OneMinute
      context.timerService.registerProcessingTimeTimer(timer)
      timerState.update(timer)
      logger.info(s"small amount: ${transaction.amount}")

      lastTransaction.update(transaction)

  override def onTimer(
      timestamp: Long,
      ctx: KeyedProcessFunction[Long, Transaction, Alert]#OnTimerContext,
      out: Collector[Alert]
  ): Unit =
    // remove flag after 1 minute, assuming that attacker makes fraudulent transactions within a minute
    // fraudState.clear()
    flagState.clear()
    timerState.clear()

  @throws[Exception]
  private def cleanUp(
      ctx: KeyedProcessFunction[Long, Transaction, Alert]#Context
  ): Unit =
    // delete timer
    val timer = timerState.value
    ctx.timerService.deleteProcessingTimeTimer(timer)

    // clean up all states
    flagState.clear()
    timerState.clear()
