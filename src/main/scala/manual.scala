import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration._

object ManualExample extends App {

  import slick.driver.H2Driver.api._

  final case class Review(critic: String, title: String, rating: Int)

  final class ReviewTable(tag: Tag) extends Table[Review](tag, "review") {
    def critic = column[String]("critic_name")
    def title  = column[String]("title")
    def rating = column[Int]("rating")
    def * = (critic, title, rating) <> (Review.tupled, Review.unapply)
  }

  val reviews = TableQuery[ReviewTable]

  val testData = Seq(
    Review("Richard", "Godzilla", 8),
    Review("Jono", "Godzilla", 7)
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

  def postReview(critic: String, title: String, rating: Int)(implicit ec: ExecutionContext): DBIO[Int] = {
    for {
      rowsAffected <- reviews.filter(r => r.critic === critic && r.title === title).map(_.rating).update(rating)
      result <- rowsAffected match {
        case 0 => reviews += Review(critic, title, rating)
        case 1 => DBIO.successful(1)
        case n => DBIO.failed(new RuntimeException(s"Expected 0 or 1 change, not $n for $critic @ $title"))
      }
    } yield result
  }

  println("Results of updating Richard's review of Godzilla to 10")
  println(
    Await.result(
      db.run(
        postReview("Richard", "Godzilla", 10)
      ),
      2 seconds)
  )

  println("Final database state")
  Await.result( db.run(reviews.result).map { _ foreach println }, 2 seconds)

  db.close
}