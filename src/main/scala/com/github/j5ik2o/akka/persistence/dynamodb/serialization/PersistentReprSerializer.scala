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

import akka.persistence.journal.Tagged
import akka.persistence.{ AtomicWrite, PersistentRepr }

object EitherSeq {
  def sequence[A](seq: Seq[Either[Throwable, A]]): Either[Throwable, Seq[A]] = {
    def recurse(remaining: Seq[Either[Throwable, A]], processed: Seq[A]): Either[Throwable, Seq[A]] = remaining match {
      case Seq()               => Right(processed)
      case Right(head) +: tail => recurse(remaining = tail, processed :+ head)
      case Left(t) +: _        => Left(t)
    }
    recurse(seq, Vector.empty)
  }
}

trait PersistentReprSerializer[A] {

  def serialize(messages: Seq[AtomicWrite]): Seq[Either[Throwable, Seq[A]]] = {
    messages.map { atomicWrite =>
      val serialized = atomicWrite.payload.map(serialize)
      EitherSeq.sequence(serialized)
    }
  }

  def serialize(persistentRepr: PersistentRepr): Either[Throwable, A] = persistentRepr.payload match {
    case Tagged(payload, tags) =>
      serialize(persistentRepr.withPayload(payload), tags)
    case _ => serialize(persistentRepr, Set.empty[String])
  }

  def serialize(persistentRepr: PersistentRepr, tags: Set[String]): Either[Throwable, A]

  def deserialize(t: A): Either[Throwable, (PersistentRepr, Set[String], Long)]

}
