import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

object PkExample extends App {

  import slick.driver.H2Driver.api._

  final case class Review(title: String, rating: Int, id: Long = 0L)

  final class ReviewTable(tag: Tag) extends Table[Review](tag, "review") {
    def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title   = column[String]("content")
    def rating  = column[Int]("rating")
    def * = (title, rating, id) <> (Review.tupled, Review.unapply)
  }

  val reviews = TableQuery[ReviewTable]

  val testData = Seq(
    Review("Godzilla", 8),
    Review("Godzilla Raids Again", 6),
    Review("King Kong vs. Godzilla", 5)
  )

  val actions = for {
    _   <- reviews.schema.create
    _   <- reviews ++= testData
    all <- reviews.result
  } yield all

  val db = Database.forConfig("upsert")

  println("Results of inserting and selected data:")
  val future = db.run(actions).map { _ foreach println }
  Await.result(future, 2 seconds)

  def postReview(title: String, rating: Int): DBIO[Int] = for {
    existing <- reviews.filter(_.title === title).result.headOption
    row       = existing getOrElse Review(title, rating)
    result  <- reviews.insertOrUpdate(row)
  } yield result

/*
select 1 from "review" where "id"=?
DEBUG slick.jdbc.JdbcBackend.statement - Preparing statement: update "review" set "content"=?,"rating"=? where "id"=?

DEBUG slick.jdbc.JdbcBackend.statement - Preparing statement: select 1 from "review" where "id"=?
DEBUG slick.jdbc.JdbcBackend.statement - Preparing statement: insert into "review" ("content","rating")  values (?,?)
1
*/

  println("Results of insertOrUpdate")
  println(
    Await.result(
      db.run(
        postReview("Godzilla (2014)", 10)
      ),
      2 seconds)
  )

  db.close
}