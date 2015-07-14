import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

object CompositePkExample extends App {

  import slick.driver.H2Driver.api._

  final case class Review(critic: String, title: String, rating: Int)

  final class ReviewTable(tag: Tag) extends Table[Review](tag, "review") {
    def critic = column[String]("critic_name")
    def title  = column[String]("title")
    def rating = column[Int]("rating")
    def * = (critic, title, rating) <> (Review.tupled, Review.unapply)
    def pk = primaryKey("review_pk", (critic, title))
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

  def postReview(critic: String, title: String, rating: Int): DBIO[Int] = for {
    existing <- reviews.filter(r => r.title === title && r.critic === critic).result.headOption
    row       = existing.map(_.copy(rating=rating)) getOrElse Review(critic, title, rating)
    result  <- reviews.insertOrUpdate(row)
  } yield result

  // NB: https://github.com/slick/slick/issues/966

  println("Results of insertOrUpdate of Godzilla (2014)")
  println(
    Await.result(
      db.run(
        postReview("Richard", "Godzilla (2014)", 10)
      ),
      2 seconds)
  )

  println("Final database state")
  Await.result( db.run(reviews.result).map { _ foreach println }, 2 seconds)
  db.close
}