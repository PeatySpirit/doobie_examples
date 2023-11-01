package org.example

import java.sql.{Connection, DriverManager}

object DatabaseConnection {
  def get(): Connection = {
    val driver   = "org.postgresql.Driver"
    val url      = "jdbc:postgresql:main"
    val username = "grand"
    val password = "poobah"

    // make the connection
    Class.forName(driver)
    DriverManager.getConnection(url, username, password)
  }
}
