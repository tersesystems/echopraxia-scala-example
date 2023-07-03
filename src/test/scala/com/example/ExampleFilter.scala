package com.example

import com.tersesystems.echopraxia.spi.{CoreLogger, CoreLoggerFilter}

class ExampleFilter extends CoreLoggerFilter {
  override def apply(t: CoreLogger): CoreLogger = {
    println("I am a filter")
    t
  }
}
