package com.example

import com.tersesystems.echopraxia.plusscala.generic._
import com.tersesystems.echopraxia.plusscala.trace._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object TraceMain {
  import ExecutionContext.Implicits._

  trait AutoTraceFieldBuilder extends DefaultTraceFieldBuilder with AutoDerivation
  object AutoTraceFieldBuilder extends AutoTraceFieldBuilder

  private val logger = TraceLoggerFactory.getLogger.withFieldBuilder(AutoTraceFieldBuilder)

  private def createFoo(barValue: String): Foo = logger.trace {
    Foo(Bar(barValue))
  }

  private def getBar(foo: Foo): Bar = logger.trace {
    foo.bar
  }

  private def noArgsBar: Bar = logger.trace {
    Bar("noArgsBar")
  }

  private def someFuture: Future[Bar] = Future {
    logger.trace {
      Bar("futureBar")
    }
  }

  def main(args: Array[String]): Unit = {
    val foo = createFoo("bar")
    getBar(foo)
    noArgsBar

    Await.result(someFuture, Duration.Inf)
  }
}
