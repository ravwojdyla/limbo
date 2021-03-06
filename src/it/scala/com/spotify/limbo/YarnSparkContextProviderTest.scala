/*
 * Copyright 2016 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.limbo

import org.scalatest.{FlatSpec, Matchers}

class YarnSparkContextProviderTest extends FlatSpec with Matchers with TestUtils {

  "createYarnSparkContext" should "be able to start and stop spark context" in {
    withMiniClusterWithURL { (confUrl) =>
      val spark = SparkContextProvider.createYarnSparkContext(confUrl)
      try {
        spark.isLocal shouldBe false
        spark.isStopped shouldBe false
      } finally {
        spark.stop()
      }
      spark.isStopped shouldBe true
    }
  }

}
