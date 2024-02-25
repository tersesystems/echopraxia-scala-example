package com.example.logger

import com.example.Price
import com.example.logger.LoggingBase.{abbreviateAfter, withAttributes, withStringFormat}
import com.tersesystems.echopraxia.api._
import com.tersesystems.echopraxia.plusscala.api.{EitherValueTypes, OptionValueTypes, ValueTypeClasses}
import com.tersesystems.echopraxia.spi.{EchopraxiaService, FieldConstants, FieldCreator, PresentationHintAttributes}

import java.util
import java.util.List
import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.jdk.OptionConverters.RichOption
import scala.language.implicitConversions
import scala.reflect.{ClassTag, classTag}

// This trait should be extended for domain model classes
trait LoggingBase extends ValueTypeClasses with OptionValueTypes with EitherValueTypes {

  // Provides a default name for a field if not provided
  trait ToName[-T] {
    def toName: String
  }

  object ToName {
    def create[T](name: String): ToName[T] = new ToName[T] {
      override def toName: String = name
    }
  }

  // Provides easier packaging for ToName and ToValue
  trait ToLog[-TF] {
    def toName: ToName[TF]
    def toValue: ToValue[TF]
  }

  object ToLog {
    def create[TF](name: String, tv: ToValue[TF]): ToLog[TF] = new ToLog[TF] {
      override val toName: ToName[TF] = ToName.create(name)
      override val toValue: ToValue[TF] = tv
    }

    def createFromClass[TF: ClassTag](tv: ToValue[TF]): ToLog[TF] = new ToLog[TF] {
      override val toName: ToName[TF] = ToName.create(classTag[TF].runtimeClass.getName)
      override val toValue: ToValue[TF] = tv
    }
  }

  // Used for rendering value in message template in conjunction with ValueAttributes
  trait ToStringValue[T] {
    def toStringValue(v: T): Value[_]
  }

  object ToStringValue {
    def apply[T: ToStringValue](v: T): Value[_] = implicitly[ToStringValue[T]].toStringValue(v)
  }

  // Allows custom attributes on fields through implicits
  trait ToValueAttribute[-T] {
    def toValue(v: T): Value[_]

    def toAttributes(value: Value[_]): Attributes
  }

  trait LowPriorityToValueAttributeImplicits {
    implicit def optionValueFormat[TV: ToValueAttribute]: ToValueAttribute[Option[TV]] = new ToValueAttribute[Option[TV]] {
      override def toValue(v: Option[TV]): Value[_] = v match {
        case Some(tv) =>
          val ev = implicitly[ToValueAttribute[TV]]
          ev.toValue(tv)
        case None => Value.nullValue()
      }

      override def toAttributes(value: Value[_]): Attributes = implicitly[ToValueAttribute[TV]].toAttributes(value)
    }

    implicit def iterableValueFormat[TV: ToValueAttribute]: ToValueAttribute[Iterable[TV]] = new ToValueAttribute[Iterable[TV]]() {
      override def toValue(seq: collection.Iterable[TV]): Value[_] = {
        val list: Seq[Value[_]] = seq.map(el => implicitly[ToValueAttribute[TV]].toValue(el)).toSeq
        Value.array(list.asJava)
      }

      override def toAttributes(value: Value[_]): Attributes = implicitly[ToValueAttribute[TV]].toAttributes(value)
    }


    //    implicit def eitherToValueAttribute[TVL, TVR, T <: Either[TVL, TVR]](t: T)(implicit left: ToValueAttribute[TVL], right: ToValueAttribute[TVR]): ToValueAttribute[T] = new ToValueAttribute[T] {
    //      override def toValue(v: T): Value[_] = t match {
    //        case Left(l) => left.toValue(l.asInstanceOf[TVL])
    //        case Right(r) => right.toValue(r.asInstanceOf[TVR])
    //      }
    //
    //      override def toAttributes(value: Value[_]): Attributes = t match {
    //        case Left(l) => left.toAttributes(left.toValue(l.asInstanceOf[TVL]))
    //        case Right(r) => right.toAttributes(right.toValue(r.asInstanceOf[TVR]))
    //      }
    //    }

    // default low priority implicit that gets applied if nothing is found
    implicit def empty[TV]: ToValueAttribute[TV] = new ToValueAttribute[TV] {
      override def toValue(v: TV): Value[_] = Value.nullValue()
      override def toAttributes(value: Value[_]): Attributes = Attributes.empty()
    }
  }

  object ToValueAttribute extends LowPriorityToValueAttributeImplicits

  trait ToStringFormat[T] extends ToValueAttribute[T] {
    override def toAttributes(value: Value[_]): Attributes = withAttributes(withStringFormat(value))
  }
  object ToStringFormat extends LowPriorityToValueAttributeImplicits

  trait AbbreviateAfter[T] extends ToValueAttribute[T] {
    override def toAttributes(value: Value[_]): Attributes = withAttributes(abbreviateAfter(5))
  }

  // implicit conversion from a ToLog to a ToValue
  implicit def convertToLogToValue[TL: ToLog]: ToValue[TL] = implicitly[ToLog[TL]].toValue

  // implicit conversion from a ToLog to a ToName
  implicit def convertToLogToName[TL: ToLog]: ToName[TL] = implicitly[ToLog[TL]].toName

  // Convert a tuple into a field.  This does most of the heavy lifting.
  // i.e logger.info("foo" -> foo) becomes logger.info(Field.keyValue("foo", ToValue(foo)))
  implicit def tupleToField[TV: ToValue](tuple: (String, TV))(implicit va: ToValueAttribute[TV]): Field = keyValue(tuple._1, tuple._2)

  // Convert an object with implicit ToValue and ToName to a field.
  // i.e. logger.info(foo) becomes logger.info(Field.keyValue(ToName[Foo].toName, ToValue(foo)))
  implicit def nameAndValueToField[TV: ToValue: ToName](value: TV)(implicit va: ToValueAttribute[TV]): Field =
    keyValue(implicitly[ToName[TV]].toName, value)

  // All exceptions should use "exception" field constant by default
  implicit def throwableToName[T <: Throwable]: ToName[T] = ToName.create(FieldConstants.EXCEPTION)

  // turn a Map into a value (this demos how you can manage custom abstract data types)
  implicit def mapToValue[TV: ToValue](implicit va: ToValueAttribute[TV]): ToValue[Map[String, TV]] = { v =>
    val value: Seq[Value.ObjectValue] = v.map { case (k, v) =>
      ToObjectValue(keyValue("key", k), keyValue("value", v))
    }.toSeq
    ToArrayValue(value)
  }

  // Creates a field, this is private so it's not exposed to traits that extend this
  private def keyValue[TV: ToValue](name: String, tv: TV)(implicit va: ToValueAttribute[TV]): Field = {
    LoggingBase.fieldCreator.create(name, ToValue(tv), va.toAttributes(va.toValue(tv)))
  }
}

object LoggingBase {
  private val fieldCreator: FieldCreator[PresentationField] = EchopraxiaService.getInstance.getFieldCreator(classOf[PresentationField])

  def withAttributes(seq: Attribute[_]*): Attributes = {
    Attributes.create(seq.asJava)
  }

  // Add a custom string format attribute using the passed in value
  def withStringFormat(value: Value[_]): Attribute[_] = {
    PresentationHintAttributes.withToStringFormat(new SimpleFieldVisitor() {
      override def visit(f: Field): Field = Field.keyValue(f.name(), value)
    })
  }

  def abbreviateAfter(after: Int): Attribute[_] = {
    PresentationHintAttributes.abbreviateAfter(after)
  }
}
