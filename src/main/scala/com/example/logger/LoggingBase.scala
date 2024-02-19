package com.example.logger

import com.tersesystems.echopraxia.api._
import com.tersesystems.echopraxia.plusscala.api.{EitherValueTypes, OptionValueTypes, ValueTypeClasses}
import com.tersesystems.echopraxia.spi.{EchopraxiaService, FieldConstants, FieldCreator, PresentationHintAttributes}

import java.util.concurrent.atomic.AtomicInteger
import scala.language.implicitConversions

// This trait should be extended for domain model classes
trait LoggingBase extends ValueTypeClasses with OptionValueTypes with EitherValueTypes {

  // Provides a default name for a field if not provided
  trait ToName[-T] {
    def toName: String
  }

  object ToName {
    def apply[T](name: String): ToName[T] = new ToName[T] {
      override def toName: String = name
    }
  }

  // Provides easier packaging for ToName and ToValue
  trait ToLog[-TF] {
    def toName: ToName[TF]
    def toValue: ToValue[TF]
  }

  object ToLog {
    def apply[TF](name: String, tv: ToValue[TF]): ToLog[TF] = new ToLog[TF] {
      override val toName: ToName[TF] = ToName(name)
      override val toValue: ToValue[TF] = tv
    }
  }

  // Allows custom attributes on fields through implicits
  trait ValueAttributes[-A] {
    def attributes(tv: A): Attributes
  }

  object ValueAttributes {
    // default low priority implicit that gets applied if nothing is found
    implicit def empty[TV]: ValueAttributes[TV] = _ => Attributes.empty()
  }

  // implicit conversion from a ToLog to a ToValue
  implicit def convertToLogToValue[TL: ToLog]: ToValue[TL] = implicitly[ToLog[TL]].toValue

  // implicit conversion from a ToLog to a ToName
  implicit def convertToLogToName[TL: ToLog]: ToName[TL] = implicitly[ToLog[TL]].toName

  // Convert a tuple into a field.  This does most of the heavy lifting.
  // i.e logger.info("foo" -> foo) becomes logger.info(Field.keyValue("foo", ToValue(foo)))
  implicit def tupleToField[TV: ToValue](tuple: (String, TV))(implicit va: ValueAttributes[TV]): Field = keyValue(tuple._1, tuple._2)

  // Convert an object with implicit ToValue and ToName to a field.
  // i.e. logger.info(foo) becomes logger.info(Field.keyValue(ToName[Foo].toName, ToValue(foo)))
  implicit def nameAndValueToField[TV: ToValue: ToName](value: TV)(implicit va: ValueAttributes[TV]): Field =
    keyValue(implicitly[ToName[TV]].toName, value)

  // All exceptions should use "exception" field constant by default
  implicit def throwableToName[T <: Throwable]: ToName[T] = ToName(FieldConstants.EXCEPTION)

  // turn a Map into a value (this demos how you can manage custom abstract data types)
  implicit def mapToValue[V: ToValue]: ToValue[Map[String, V]] = { v =>
    val value: Seq[Value.ObjectValue] = v.map { case (k, v) =>
      ToObjectValue(keyValue("key", k), keyValue("value", v))
    }.toSeq
    ToArrayValue(value)
  }

  // Creates a field, this is private so it's not exposed to traits that extend this
  private def keyValue[TV: ToValue](name: String, tv: TV)(implicit va: ValueAttributes[TV]): Field = {
    val value = implicitly[ToValue[TV]].toValue(tv)
    LoggingBase.fieldCreator.create(name, value, va.attributes(tv));
  }
}

object LoggingBase {
  private val fieldCreator: FieldCreator[PresentationField] = EchopraxiaService.getInstance.getFieldCreator(classOf[PresentationField])

  // Add a custom string format attribute using the passed in string value
  def withStringFormat(string: String): Attribute[_] = {
    PresentationHintAttributes.withToStringFormat(new SimpleFieldVisitor() {
      override def visit(f: Field): Field = LoggingBase.fieldCreator.create(f.name(), Value.string(string), Attributes.empty())
    })
  }

  // Adds a custom string format using the passed in strings
  def withSeqStringFormat(strings: Seq[String]): Attribute[_] = {
    PresentationHintAttributes.withToStringFormat(new SimpleFieldVisitor() {
      override def visitArray(): FieldVisitor.ArrayVisitor = new SimpleArrayVisitor() {
        val counter = new AtomicInteger(0)
        override def visitElement(value: Value[_]): Unit = {
          super.visitStringElement(Value.string(strings(counter.getAndAdd(1))))
        }
      }
    })
  }
}
