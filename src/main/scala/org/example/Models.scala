package org.example

import doobie.util.{Get, Put}
import org.example.Models.Frequency.Frequency

object Models {
  object Frequency extends Enumeration {
    type Frequency = Value
    val Weekly, Monthly, Quarterly = Value

    implicit val getFrequency: Get[Frequency] = Get[String].map(stringToFrequency)
    implicit val putFrequency: Put[Frequency] = Put[String].contramap(_.toString)

    def stringToFrequency(s: String) = s match {
      case "Weekly"    => Weekly
      case "Monthly"   => Monthly
      case "Quarterly" => Quarterly
      case s           => throw new IllegalArgumentException(s"Invalid Frequency value: $s")
    }
  }

  case class Instance(id: String, display: String)

  case class TaskDefinition(id: Int, name: String, details: Option[String], owner: String, startingDate: String,
                            frequency: Frequency, instance: Instance)

}
