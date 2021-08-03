package com.anthfu.nio

import java.net.InetSocketAddress
import java.nio.ByteBuffer
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
          p.success(client)

        override def failed(e: Throwable, attachment: Void): Unit =
          p.failure(e)
      }
    )

    p.future
  }

  def accept(server: AsynchronousServerSocketChannel): Future[AsynchronousSocketChannel] = {
    val p = Promise[AsynchronousSocketChannel]()

    server.accept(
      null,
      new CompletionHandler[AsynchronousSocketChannel, Void]() {
        override def completed(result: AsynchronousSocketChannel, attachment: Void): Unit =
          p.success(result)

        override def failed(e: Throwable, attachment: Void): Unit =
          p.failure(e)
      }
    )

    p.future
  }

  def read(channel: AsynchronousSocketChannel): Future[Array[Byte]] = {
    val buffer = ByteBuffer.allocate(1024)
    val p = Promise[Array[Byte]]()

    channel.read(buffer, null, new CompletionHandler[Integer, Void]() {
      override def completed(result: Integer, attachment: Void): Unit = {
        buffer.flip()
        p.success(buffer.array())
      }

      override def failed(e: Throwable, attachment: Void): Unit =
        p.failure(e)
    })

    p.future
  }
}
