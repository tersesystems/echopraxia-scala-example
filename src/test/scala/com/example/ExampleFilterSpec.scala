package com.example

import com.tersesystems.echopraxia.plusscala.LoggerFactory
import org.scalatest.wordspec.AnyWordSpec

class ExampleFilterSpec extends AnyWordSpec {

  "test" should {

    "work" in {
      LoggerFactory.getLogger
      succeed
    }
  }
}
