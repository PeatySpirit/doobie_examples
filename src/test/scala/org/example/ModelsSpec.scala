package org.example

import org.scalatest.funsuite.AnyFunSuite
import org.example.Models._
import org.scalatest.matchers.should.Matchers

class ModelsTest extends AnyFunSuite with Matchers {
  test("Frequency should throw for invalid string") {
    assertThrows[IllegalArgumentException] {
      Frequency.stringToFrequency("Invalid")
    }
  }

  test("TaskDefinition case class should work") {
    val instance = Instance("id1", "display1")
    val task = TaskDefinition(1, "name", Some("details"), "owner", "2021-01-01", Frequency.Monthly, instance)
    task.name shouldEqual "name"
    task.frequency shouldEqual Frequency.Monthly
    task.id shouldEqual 1
  }
}
