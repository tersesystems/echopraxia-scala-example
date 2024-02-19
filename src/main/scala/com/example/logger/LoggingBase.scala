package com.example.logger

import com.tersesystems.echopraxia.api.{Attribute, Attributes, Field, PresentationField, SimpleFieldVisitor, Value}
import com.tersesystems.echopraxia.plusscala.api.{EitherValueTypes, OptionValueTypes, ValueTypeClasses}
import com.tersesystems.echopraxia.spi.{EchopraxiaService, FieldCreator, PresentationHintAttributes}

import scala.language.implicitConversions

// This trait should be extended for domain model classes
trait LoggingBase extends ValueTypeClasses with OptionValueTypes with EitherValueTypes {
  trait ValueAttributes[-A] {
    def attributes(tv: A): Attributes
  }

  object ValueAttributes {
    implicit def empty[TV]: ValueAttributes[TV] = _ => Attributes.empty()
  }

  trait ToName[-T] {
    def toName: String
  }

  trait ToLog[-TF] {
    def toName: ToName[TF]
    def toValue: ToValue[TF]
  }

  object ToName {
    def apply[T](name: String): ToName[T] = new ToName[T] {
      override def toName: String = name
    }
  }

  object ToLog {
    def apply[TF](name: String, tv: ToValue[TF]): ToLog[TF] = new ToLog[TF] {
      override val toName: ToName[TF] = ToName(name)
      override val toValue: ToValue[TF] = tv
    }
  }

  implicit def convertToLogToValue[TL: ToLog]: ToValue[TL] = implicitly[ToLog[TL]].toValue
  implicit def convertToLogToName[TL: ToLog]: ToName[TL] = implicitly[ToLog[TL]].toName

  implicit def throwableToName[T <: Throwable]: ToName[T] = ToName("exception")

  // turn a Map into a value, just to be fancy
  implicit def mapToValue[V: ToValue]: ToValue[Map[String, V]] = { v =>
    val value: Seq[Value.ObjectValue] = v.map { case (k, v) =>
      ToObjectValue(keyValue("key", k), keyValue("value", v))
    }.toSeq
    ToArrayValue(value)
  }

  implicit def tupleToField[TV: ToValue](tuple: (String, TV))(implicit va: ValueAttributes[TV]): Field = keyValue(tuple._1, tuple._2)

  implicit def nameAndValueToField[TV: ToValue: ToName](value: TV)(implicit va: ValueAttributes[TV]): Field =
    keyValue(implicitly[ToName[TV]].toName, value)

  def keyValue[TV: ToValue](name: String, tv: TV)(implicit va: ValueAttributes[TV]): Field = {
    val value = implicitly[ToValue[TV]].toValue(tv)
    LoggingBase.fieldCreator.create(name, value, va.attributes(tv));
  }
}

object LoggingBase {
  private val fieldCreator: FieldCreator[PresentationField] = EchopraxiaService.getInstance.getFieldCreator(classOf[PresentationField])

  def withStringFormat(string: String): Attribute[_] = {
    PresentationHintAttributes.withToStringFormat(new SimpleFieldVisitor() {
      override def visit(f: Field): Field = LoggingBase.fieldCreator.create(f.name(), Value.string(string), Attributes.empty())
    })
  }
}
