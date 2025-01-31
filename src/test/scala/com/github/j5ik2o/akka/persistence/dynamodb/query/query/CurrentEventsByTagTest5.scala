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

package com.github.j5ik2o.akka.persistence.dynamodb.query.query

import java.net.URI

import akka.pattern.ask
import akka.persistence.query.{ EventEnvelope, NoOffset, Sequence }
import com.github.j5ik2o.akka.persistence.dynamodb.query.QueryJournalSpec
import com.github.j5ik2o.akka.persistence.dynamodb.utils.DynamoDBSpecSupport
import com.github.j5ik2o.reactive.aws.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import software.amazon.awssdk.services.dynamodb.{ DynamoDbAsyncClient => JavaDynamoDbAsyncClient }

import scala.concurrent.duration._

abstract class CurrentEventsByTagTest5(config: String) extends QueryJournalSpec(config) {

  it should "persist and find a tagged event with multiple tags" in
  withTestActors() { (actor1, actor2, actor3) =>
    withClue("Persisting multiple tagged events") {
      (for {
        _ <- actor1 ? withTags("a", "one", "1", "prime")
        _ <- actor1 ? withTags("a", "two", "2", "prime")
        _ <- actor1 ? withTags("a", "three", "3", "prime")
        _ <- actor1 ? withTags("a", "four", "4")
        _ <- actor1 ? withTags("a", "five", "5", "prime")

        _ <- actor2 ? withTags("a", "three", "3", "prime")
        _ <- actor3 ? withTags("a", "three", "3", "prime")

        _ <- actor1 ? "a"
        _ <- actor1 ? "a"
      } yield ()).toTry should be a 'success
    }

    withCurrentEventsByTag()("one", NoOffset) { tp =>
      tp.request(Int.MaxValue)
      tp.expectNext(EventEnvelope(Sequence(1), "my-1", 1, "a-1"))
      tp.expectComplete()
    }
    withCurrentEventsByTag()("one", Sequence(1)) { tp =>
      tp.request(Int.MaxValue)
      tp.expectComplete()
    }

    withCurrentEventsByTag()("prime", NoOffset) { tp =>
      tp.request(Int.MaxValue)
      tp.expectNext(EventEnvelope(Sequence(1), "my-1", 1, "a-1"))
      tp.expectNext(EventEnvelope(Sequence(2), "my-1", 2, "a-2"))
      tp.expectNext(EventEnvelope(Sequence(3), "my-1", 3, "a-3"))
      tp.expectNext(EventEnvelope(Sequence(4), "my-1", 5, "a-5"))
      tp.expectNext(EventEnvelope(Sequence(5), "my-2", 1, "a-1"))
      tp.expectNext(EventEnvelope(Sequence(6), "my-3", 1, "a-1"))
      tp.expectComplete()
    }

    withCurrentEventsByTag()("prime", Sequence(0)) { tp =>
      tp.request(Int.MaxValue)
      tp.expectNext(EventEnvelope(Sequence(1), "my-1", 1, "a-1"))
      tp.expectNext(EventEnvelope(Sequence(2), "my-1", 2, "a-2"))
      tp.expectNext(EventEnvelope(Sequence(3), "my-1", 3, "a-3"))
      tp.expectNext(EventEnvelope(Sequence(4), "my-1", 5, "a-5"))
      tp.expectNext(EventEnvelope(Sequence(5), "my-2", 1, "a-1"))
      tp.expectNext(EventEnvelope(Sequence(6), "my-3", 1, "a-1"))
      tp.expectComplete()
    }

    withCurrentEventsByTag()("prime", Sequence(1)) { tp =>
      tp.request(Int.MaxValue)
      tp.expectNext(EventEnvelope(Sequence(2), "my-1", 2, "a-2"))
      tp.expectNext(EventEnvelope(Sequence(3), "my-1", 3, "a-3"))
      tp.expectNext(EventEnvelope(Sequence(4), "my-1", 5, "a-5"))
      tp.expectNext(EventEnvelope(Sequence(5), "my-2", 1, "a-1"))
      tp.expectNext(EventEnvelope(Sequence(6), "my-3", 1, "a-1"))
      tp.expectComplete()
    }

    withCurrentEventsByTag()("prime", Sequence(2)) { tp =>
      tp.request(Int.MaxValue)
      tp.expectNext(EventEnvelope(Sequence(3), "my-1", 3, "a-3"))
      tp.expectNext(EventEnvelope(Sequence(4), "my-1", 5, "a-5"))
      tp.expectNext(EventEnvelope(Sequence(5), "my-2", 1, "a-1"))
      tp.expectNext(EventEnvelope(Sequence(6), "my-3", 1, "a-1"))
      tp.expectComplete()
    }

    withCurrentEventsByTag()("prime", Sequence(3)) { tp =>
      tp.request(Int.MaxValue)
      tp.expectNext(EventEnvelope(Sequence(4), "my-1", 5, "a-5"))
      tp.expectNext(EventEnvelope(Sequence(5), "my-2", 1, "a-1"))
      tp.expectNext(EventEnvelope(Sequence(6), "my-3", 1, "a-1"))
      tp.expectComplete()
    }

    withCurrentEventsByTag()("prime", Sequence(4)) { tp =>
      tp.request(Int.MaxValue)
      tp.expectNext(EventEnvelope(Sequence(5), "my-2", 1, "a-1"))
      tp.expectNext(EventEnvelope(Sequence(6), "my-3", 1, "a-1"))
      tp.expectComplete()
    }

    withCurrentEventsByTag()("prime", Sequence(5)) { tp =>
      tp.request(Int.MaxValue)
      tp.expectNext(EventEnvelope(Sequence(6), "my-3", 1, "a-1"))
      tp.expectComplete()
    }

    withCurrentEventsByTag()("prime", Sequence(6)) { tp =>
      tp.request(Int.MaxValue)
      tp.expectComplete()
    }

    withCurrentEventsByTag()("prime", Sequence(7)) { tp =>
      tp.request(Int.MaxValue)
      tp.expectComplete()
    }

    withCurrentEventsByTag()("3", NoOffset) { tp =>
      tp.request(Int.MaxValue)
      tp.expectNext(EventEnvelope(Sequence(1), "my-1", 3, "a-3"))
      tp.expectNext(EventEnvelope(Sequence(2), "my-2", 1, "a-1"))
      tp.expectNext(EventEnvelope(Sequence(3), "my-3", 1, "a-1"))
      tp.expectComplete()
    }

    withCurrentEventsByTag()("4", NoOffset) { tp =>
      tp.request(Int.MaxValue)
      tp.expectNext(EventEnvelope(Sequence(1), "my-1", 4, "a-4"))
      tp.expectComplete()
    }

    withCurrentEventsByTag()("four", NoOffset) { tp =>
      tp.request(Int.MaxValue)
      tp.expectNext(EventEnvelope(Sequence(1), "my-1", 4, "a-4"))
      tp.expectComplete()
    }

    withCurrentEventsByTag()("5", NoOffset) { tp =>
      tp.request(Int.MaxValue)
      tp.expectNext(EventEnvelope(Sequence(1), "my-1", 5, "a-5"))
      tp.expectComplete()
    }

    withCurrentEventsByTag()("five", NoOffset) { tp =>
      tp.request(Int.MaxValue)
      tp.expectNext(EventEnvelope(Sequence(1), "my-1", 5, "a-5"))
      tp.expectComplete()
    }
  }
}

class DynamoDBCurrentEventsByTagTest5 extends CurrentEventsByTagTest5("default.conf") with DynamoDBSpecSupport {

  override implicit val pc: PatienceConfig = PatienceConfig(20 seconds, 1 seconds)

  override protected lazy val dynamoDBPort: Int = 8000

  val underlying: JavaDynamoDbAsyncClient = JavaDynamoDbAsyncClient
    .builder()
    .credentialsProvider(
      StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey))
    )
    .endpointOverride(URI.create(dynamoDBEndpoint))
    .build()

  override def asyncClient: DynamoDbAsyncClient = DynamoDbAsyncClient(underlying)

  override def afterAll(): Unit = {
    underlying.close()
    super.afterAll()
  }

  before { createTable }

  after { deleteTable }

}
