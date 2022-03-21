package org.example

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie._
import doobie.implicits._
import org.example.Models.Frequency.Frequency
import org.example.Models.{Frequency, Instance, TaskDefinition}
import java.sql.Connection
import scala.util.{Try, Using}

object DoobieApp extends App {
  implicit class LoggingTry[T](val t: Try[T]) {
    def logCustom(s: String): T = t.recover { e =>
      println(s, e)
      throw e
    }.get
  }

  def getTasks(instance: Instance)(implicit c: Connection) = {
    Try(sql"select t.id, name, details, owner, starting_date, frequency, i.id, i.display FROM baconjam.manual_task_definitions t join baconjam.manual_task_instances i on t.instance=i.id  where instance=${instance.id} and deleted_ts is null order by frequency, name"
      .query[TaskDefinition]
      .to[List]
      .transact(Transactor.fromConnection[IO](c))
      .unsafeRunSync()).logCustom("IntelligenceDatabase.getTasks")
  }

  def getTasksHCProgram(instance: Instance)(implicit c: Connection): Option[TaskDefinition] = {
    val query = "select t.id, name, details, owner, starting_date, frequency, i.id, i.display FROM baconjam.manual_task_definitions t join baconjam.manual_task_instances i on t.instance=i.id  where instance=? and deleted_ts is null order by frequency, name"
    HC.stream[TaskDefinition](
      query,
      HPS.set(instance.id),   // Parameters start from index 1 by default
      512
    ).compile
      .toList
      .map(_.headOption)
      .transact(Transactor.fromConnection[IO](c))
      .unsafeRunSync()
  }

  def getTasksByInitialLetterUsingFragments(initialLetter: String)(implicit c: Connection): List[TaskDefinition] = {
    val select: Fragment = fr"select id, name"
    val from: Fragment = fr"from baconjam.manual_task_definitions"
    val where: Fragment = fr"where LEFT(name, 1) = $initialLetter"

    val statement = select ++ from ++ where

    statement.query[TaskDefinition].stream.compile.toList
      .transact(Transactor.fromConnection[IO](c))
      .unsafeRunSync()
  }

  def getTask(id: Int)(implicit c: Connection) =
    Try(sql"select t.id, name, details, owner, starting_date, frequency, i.id, i.display FROM baconjam.manual_task_definitions t join baconjam.manual_task_instances i on t.instance=i.id where t.id = $id and deleted_ts is null"
      .query[TaskDefinition]
      .unique
      .transact(Transactor.fromConnection[IO](c))
      .unsafeRunSync()).logCustom("IntelligenceDatabase.getTask")

  def updateTask(task: TaskDefinition)(implicit c: Connection) =
    Try(sql"update baconjam.manual_task_definitions set name = ${task.name}, details = ${task.details}, owner = ${task.owner}, starting_date = ${task.startingDate}, frequency = ${task.frequency} where id = ${task.id} and deleted_ts is null"
      .update //Update0
      .run //ConnectionIO[Int]
      .transact(Transactor.fromConnection[IO](c)) //IO[Int]
      .unsafeRunSync()).logCustom("IntelligenceDatabase.updateTask")

  def createNewTask(name: String, details: Option[String], owner: String, startingDate: String, frequency: Frequency, instance: Instance)(implicit c: Connection) =
    Try(sql"insert into baconjam.manual_task_definitions (name, details, owner, starting_date, frequency, instance) values ($name, $details, $owner, $startingDate, $frequency, ${instance.id})"
      .update //Update0
      .withUniqueGeneratedKeys[Int]("id")
      .transact(Transactor.fromConnection[IO](c)) //IO[Int]
      .unsafeRunSync()).logCustom("IntelligenceDatabase.createNewTask")

  def deleteTask(id: Int, who: String)(implicit c: Connection) =
    Try(sql"update baconjam.manual_task_definitions set deleted_ts = ${System.currentTimeMillis()}, deleted_by = $who where id = $id"
      .update //Update0
      .run //ConnectionIO[Int]
      .transact(Transactor.fromConnection[IO](c)) //IO[Int]
      .unsafeRunSync()).logCustom("IntelligenceDatabase.deleteTask")

  Using.resource(DatabaseConnection.get()){ implicit c =>
    val id = createNewTask("newFancyTask", None, "m.rehora@medallia.com", "20220801", Frequency.Weekly, Instance("dqm", "somePrintableName"))
    val task = getTask(id)
    println(task)
    val updatedTask = task.copy(owner = "owner@medallia.com")
    updateTask(updatedTask)
    deleteTask(id, "m.rehora@gmail.com")
  }
}
