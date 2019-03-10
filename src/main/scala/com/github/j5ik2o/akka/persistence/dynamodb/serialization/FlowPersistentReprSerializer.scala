/*
 * Copyright 2017 Dennis Vriend
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
package com.github.j5ik2o.akka.persistence.dynamodb.serialization

import akka.NotUsed
import akka.persistence.PersistentRepr
import akka.stream.scaladsl.Flow

trait FlowPersistentReprSerializer[T] extends PersistentReprSerializer[T] {

  def deserializeFlow: Flow[T, (PersistentRepr, Set[String], Long), NotUsed] = {
    Flow[T].map(deserialize).map {
      case Right(r) => r
      case Left(ex) => throw ex
    }
  }

  def deserializeFlowWithoutTags: Flow[T, PersistentRepr, NotUsed] = {
    deserializeFlow.map {
      case (repr, _, _) =>
        repr
    }
  }

}
