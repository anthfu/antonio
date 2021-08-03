package com.anthfu.nio

import java.net.InetSocketAddress
import java.nio.channels.{AsynchronousChannelGroup, AsynchronousServerSocketChannel, AsynchronousSocketChannel, CompletionHandler}
import scala.concurrent.{Future, Promise}

object AntoNIO {
  def server(
    host: String,
    port: Int,
    channelGroup: Option[AsynchronousChannelGroup] = None
  ): AsynchronousServerSocketChannel = {
    val server = AsynchronousServerSocketChannel.open(channelGroup.orNull)
    server.bind(new InetSocketAddress(host, port))
    server
  }

  def client(
    host: String,
    port: Int,
    channelGroup: Option[AsynchronousChannelGroup] = None
  ): Future[AsynchronousSocketChannel] = {
    val p = Promise[AsynchronousSocketChannel]()

    val client = AsynchronousSocketChannel.open(channelGroup.orNull)
    client.connect(
      new InetSocketAddress(host, port),
      null,
      new CompletionHandler[Void, Void]() {
        override def completed(result: Void, attachment: Void): Unit =
          Promise.successful(client)

        override def failed(e: Throwable, attachment: Void): Unit =
          Promise.failed(e)
      }
    )

    p.future
  }

  def accept(server: AsynchronousServerSocketChannel): Future[AsynchronousSocketChannel] = {
    val p = Promise[AsynchronousSocketChannel]()

    server.accept(
      null,
      new CompletionHandler[AsynchronousSocketChannel, Void]() {
        override def completed(channel: AsynchronousSocketChannel, attachment: Void): Unit =
          Promise.successful(channel)

        override def failed(e: Throwable, attachment: Void): Unit =
          Promise.failed(e)
      }
    )

    p.future
  }
}
