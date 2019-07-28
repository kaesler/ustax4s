package org.kae.ustax4s.app

import cats.effect.{ExitCode, IO, IOApp}

object MyApp extends IOApp {
override def run(args: List[String]): IO[ExitCode] =
  for {
    _ <- IO (println("hello"))
  } yield ExitCode.Success
}
