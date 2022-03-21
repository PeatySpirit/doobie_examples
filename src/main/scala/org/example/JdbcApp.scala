package org.example

import org.example.Models.{Frequency, Instance, TaskDefinition}
import java.sql.{Connection, PreparedStatement, ResultSet}
import scala.collection.mutable.ListBuffer
import scala.util.Using

object JdbcApp extends App {

  def getTasks(instance: Instance)(implicit c: Connection): List[TaskDefinition] = {
    var ps: PreparedStatement = null
    var rs: ResultSet = null
    val res = ListBuffer.empty[TaskDefinition]

    try {
      ps = c.prepareStatement(s"select t.id, name, details, owner, starting_date, frequency, i.id, i.display FROM baconjam.manual_task_definitions t join baconjam.manual_task_instances i on t.instance=i.id  where instance=? and deleted_ts is null order by frequency, name")
      ps.setString(1, instance.id)
      rs = ps.executeQuery
      while (rs.next) {
        res += TaskDefinition(
          rs.getInt(1),
          rs.getString(2),
          Option(rs.getString(3)),
          rs.getString(4),
          rs.getString(5),
          Frequency.stringToFrequency(rs.getString(6)),
          Models.Instance(rs.getString(7), rs.getString(8))
          )
      }
    } finally {
      if (rs != null) rs.close
      if (ps != null) ps.close
    }

    res.toList
  }

  Using.resource(DatabaseConnection.get()){ implicit c =>
    println(getTasks(Instance("dqm", "DQM")).mkString("\n"))
  }
}
