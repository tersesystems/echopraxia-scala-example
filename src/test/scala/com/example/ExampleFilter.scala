package com.example

import com.tersesystems.echopraxia.api.{CoreLogger, CoreLoggerFilter}

class ExampleFilter extends CoreLoggerFilter {
  override def apply(t: CoreLogger): CoreLogger = {
    println("I LIKE GOATS")
    t
  }
}
