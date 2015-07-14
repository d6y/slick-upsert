import doobie.imports._
import scalaz._, Scalaz._, scalaz.concurrent.Task

import doobie.contrib.specs2.analysisspec.AnalysisSpec
import org.specs2.mutable.Specification

object Queries {
  val allMessages =
    sql""" select "content" from "message" """.query[String]
}

object AnalysisTestSpec extends Specification with AnalysisSpec {
  val transactor = DriverManagerTransactor[Task](
    "org.postgresql.Driver", "jdbc:postgresql:chat", "richard", ""
  )
  check(Queries.allMessages)
}