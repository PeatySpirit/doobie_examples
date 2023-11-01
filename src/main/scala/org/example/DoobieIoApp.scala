package org.example

import doobie._
import doobie.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import org.example.Models.{Instance, TaskDefinition}

object DoobieIoApp extends IOApp {

  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:main",
    "grand",
    "poobah"
  )

  def findAllTaskDefinitionsProgram(instance: Instance): IO[List[TaskDefinition]] = {
    sql"select t.id, name, details, owner, starting_date, frequency, i.id, i.display FROM baconjam.manual_task_definitions t join baconjam.manual_task_instances i on t.instance=i.id  where instance=${instance.id} and deleted_ts is null order by frequency, name"
      .query[TaskDefinition]
      .to[List]
      .transact(xa)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    findAllTaskDefinitionsProgram(Instance("dqm", "DQM"))
      .map(println)
      .as(ExitCode.Success)
  }
}
