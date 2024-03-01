package com.example.logger

import com.tersesystems.echopraxia.api.FieldBuilderResult.list
import com.tersesystems.echopraxia.api.{Field, FieldBuilderResult}
import com.tersesystems.echopraxia.plusscala.api.Level._
import com.tersesystems.echopraxia.plusscala.api._
import com.tersesystems.echopraxia.spi.{CoreLogger, CoreLoggerFactory}

import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.language.implicitConversions

// A very simple logger that can be customized for your app
class Logger(core: CoreLogger) {

  def withFields(fields: => Seq[Field]): Logger = {
    new Logger(core.withFields(_ => FieldBuilderResult.list(fields.asJava), PresentationFieldBuilder))
  }

  def withCondition(condition: Condition): Logger = new Logger(core.withCondition(condition.asJava))

  abstract class LoggerMethod(level: Level) {
    def enabled: Boolean = core.isEnabled(level.asJava)

    def apply(message: => String): Unit = core.log(level.asJava, message)
    def apply(message: => String, f1: => Field): Unit = handle(level, message, f1)
    def apply(message: => String, f1: => Field, f2: => Field): Unit = handle(level, message, f1 ++ f2)
    def apply(message: => String, f1: => Field, f2: => Field, f3: => Field): Unit = handle(level, message, f1 ++ f2 ++ f3)
    def apply(message: => String, f1: => Field, f2: => Field, f3: => Field, f4: => Field): Unit = handle(level, message, f1 ++ f2 ++ f3 ++ f4)
    def apply(message: => String, fs: => Seq[Field]): Unit = handle(level, message, FieldBuilderResult.list(fs.asJava))
    def apply(message: => String, fbr: => FieldBuilderResult): Unit = handle(level, message, fbr)


    def apply(): Unit = core.log(level.asJava, "")
    def apply(f1: => Field): Unit = apply("{}", f1)
    def apply(f1: => Field, f2: => Field): Unit = apply("{} {}", f1, f2)
    def apply(f1: => Field, f2: => Field, f3: => Field): Unit = apply("{} {} {}", f1, f2, f3)
    def apply(f1: => Field, f2: => Field, f3: => Field, f4: => Field): Unit = apply("{} {} {} {}", f1, f2, f3, f4)
    def apply(fs: => FieldBuilderResult): Unit = apply("{} ".repeat(fs.fields().size()).trim, fs)


    // variadic params don't take call by name  :-(
    def v(fields: Field*): Unit = handle(level, "{} ".repeat(fields.size).trim, list(fields.toArray))

    private def handle(level: Level, message: String, f: => FieldBuilderResult): Unit = {
      import scala.compat.java8.FunctionConverters._

      val f1: PresentationFieldBuilder => FieldBuilderResult = _ => f
      core.log(level.asJava, message, f1.asJava, PresentationFieldBuilder)
    }
  }

  object info extends LoggerMethod(INFO)

  object debug extends LoggerMethod(DEBUG)

  object trace extends LoggerMethod(TRACE)

  object warn extends LoggerMethod(WARN)

  object error extends LoggerMethod(ERROR)
}

object LoggerFactory {
  private val FQCN = classOf[Logger].getName

  def getLogger(clazz: Class[_]): Logger = {
    val core: CoreLogger = CoreLoggerFactory.getLogger(FQCN, clazz)
    new Logger(core)
  }

  def getLogger(name: String): Logger = {
    val core: CoreLogger = CoreLoggerFactory.getLogger(FQCN, name)
    new Logger(core)
  }
}
