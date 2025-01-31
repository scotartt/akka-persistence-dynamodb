/*
 * Copyright 2019 Junichi Kato
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.j5ik2o.akka.persistence.dynamodb.config

import com.github.j5ik2o.akka.persistence.dynamodb.utils.ConfigOps._
import com.typesafe.config.Config

import scala.concurrent.duration._

object QueryPluginConfig {

  def fromConfig(config: Config): QueryPluginConfig = {
    QueryPluginConfig(
      tableName = config.asString("table-name", "Journal"),
      columnsDefConfig = JournalColumnsDefConfig.fromConfig(config.asConfig("columns-def")),
      tagsIndexName = config.asString("tags-index-name", "TagsIndex"),
      getJournalRowsIndexName = config.asString("get-journal-rows-index-name", "GetJournalRowsIndex"),
      tagSeparator = config.asString("tag-separator", ","),
      bufferSize = config.asInt("buffer-size", Int.MaxValue),
      batchSize = config.asInt("batch-size", 16),
      parallelism = config.asInt("parallelism", 32),
      refreshInterval = config.asFiniteDuration("refresh-interval", 1 seconds),
      shardCount = config.asInt("shard-count", 64),
      journalSequenceRetrievalConfig =
        JournalSequenceRetrievalConfig.fromConfig(config.asConfig("journal-sequence-retrieval")),
      metricsReporterClassName = config.asString(
        "metrics-reporter-class-name",
        "com.github.j5ik2o.akka.persistence.dynamodb.metrics.NullMetricsReporter"
      ),
      clientConfig = DynamoDBClientConfig.fromConfig(config.asConfig("dynamodb-client"))
    )
  }

}
case class QueryPluginConfig(
    tableName: String,
    columnsDefConfig: JournalColumnsDefConfig,
    tagsIndexName: String,
    getJournalRowsIndexName: String,
    tagSeparator: String,
    bufferSize: Int,
    batchSize: Int,
    parallelism: Int,
    refreshInterval: FiniteDuration,
    shardCount: Int,
    journalSequenceRetrievalConfig: JournalSequenceRetrievalConfig,
    metricsReporterClassName: String,
    clientConfig: DynamoDBClientConfig
)
