package scalaz
package example

import scalaz.syntax.monad._

object MTLUsage extends App {
  def app[F[_]](implicit M: Monad[F], R: MonadReader[F, Int], S: MonadError[F, String]): F[Int] = {
    for {
      a <- R.ask
      b <- S.raiseError[Int]("error")
    } yield a + b
  }

  type App[A] = Kleisli[String \/ ?, Int, A]
  // Hello SI-2712
  implicit val readerMonadReader = Kleisli.kleisliMonadReader[String \/ ?, Int]
  implicit val readerMonad = Kleisli.kleisliMonad[String \/ ?, Int]
  implicit val errorInstance = Kleisli.kleisliMonadError[String \/ ?, String, Int]

  val application: App[Int] = app[App]
}
