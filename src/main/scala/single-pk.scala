import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

object PkExample extends App {

  import slick.driver.H2Driver.api._

  final case class Review(title: String, rating: Int, id: Long = 0L)

  final class ReviewTable(tag: Tag) extends Table[Review](tag, "review") {
    def id      = column[Long]("id", O.PrimaryKey)//, O.AutoInc)
    def title   = column[String]("title")
    def rating  = column[Int]("rating")
    def * = (title, rating, id) <> (Review.tupled, Review.unapply)
  }

  val reviews = TableQuery[ReviewTable]

  val testData = Seq(
    Review("Godzilla", 8, 1),
    Review("Godzilla Raids Again", 6, 2),
    Review("King Kong vs. Godzilla", 5, 3)
  )

  val actions = for {
    _   <- reviews.schema.create
    _   <- reviews ++= testData
    all <- reviews.result
  } yield all

  val db = Database.forConfig("upsert")

  println("Results of inserting and selected data:")
  val future = db.run(actions)//.map { _ foreach println }
  Await.result(future, 2 seconds)

  {
  println("Upserting a new review")
  val review: Review = Review("Godzilla (2014)", 10, 4)
  val upsertNew: DBIO[Int] = reviews.insertOrUpdate(review)
  println(
    Await.result(db.run(upsertNew),2 seconds)
  )
  }

  println("Upserting an existing review")

  val upsertExisting: DBIO[Int] = for {
    existing <- reviews.filter(_.title === "Godzilla (2014)").result.head
    rows     <- reviews.insertOrUpdate(existing.copy(rating=9))
  } yield rows

  println(
    Await.result(db.run(upsertExisting),2 seconds)
  )

  println("Final database state")
  Await.result( db.run(reviews.result).map { _ foreach println }, 2 seconds)

  db.close
}