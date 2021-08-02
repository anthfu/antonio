package com.anthfu.nio

import java.nio.channels.{AsynchronousServerSocketChannel, AsynchronousSocketChannel, CompletionHandler}
import scala.concurrent.{Future, Promise}

object AntoNIO {
  def accept[A](server: AsynchronousServerSocketChannel): Future[AsynchronousSocketChannel] = {
    val p = Promise[AsynchronousSocketChannel]()

    server.accept(null, new CompletionHandler[AsynchronousSocketChannel, Void]() {
      override def completed(result: AsynchronousSocketChannel, attachment: Void): Unit =
        Promise.successful(result)

      override def failed(exc: Throwable, attachment: Void): Unit =
        Promise.failed(exc)
    })

    p.future
  }
}
