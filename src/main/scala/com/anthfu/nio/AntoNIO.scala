package com.anthfu.nio

import com.typesafe.scalalogging.LazyLogging

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousChannelGroup, AsynchronousServerSocketChannel, AsynchronousSocketChannel, CompletionHandler}
import scala.concurrent.{ExecutionContext, Future, Promise}

object AntoNIO extends LazyLogging {
  def serverChannel(
    host: String,
    port: Int,
    channelGroup: Option[AsynchronousChannelGroup] = None
  ): AsynchronousServerSocketChannel = {
    logger.debug("Opening server channel")
    val server = AsynchronousServerSocketChannel.open(channelGroup.orNull)
    server.bind(new InetSocketAddress(host, port))
    server
  }

  def clientChannel(
    host: String,
    port: Int,
    channelGroup: Option[AsynchronousChannelGroup] = None
  ): Future[AsynchronousSocketChannel] = {
    val p = Promise[AsynchronousSocketChannel]()

    logger.debug("Opening client channel")
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

    logger.debug("Accepting client channel")
    server.accept(
      null,
      new CompletionHandler[AsynchronousSocketChannel, Void]() {
        override def completed(channel: AsynchronousSocketChannel, attachment: Void): Unit =
          p.success(channel)

        override def failed(e: Throwable, attachment: Void): Unit =
          p.failure(e)
      }
    )

    p.future
  }

  def read(channel: AsynchronousSocketChannel): Future[Array[Byte]] = {
    val p = Promise[Array[Byte]]()

    logger.debug("Reading channel")
    val buffer = ByteBuffer.allocate(1024)
    channel.read(
      buffer,
      null,
      new CompletionHandler[Integer, Void]() {
        override def completed(bytesRead: Integer, attachment: Void): Unit = {
          logger.debug(s"Bytes read: $bytesRead")
          buffer.flip()
          p.success(buffer.array())
        }

        override def failed(e: Throwable, attachment: Void): Unit =
          p.failure(e)
      }
    )

    p.future
  }

  def write(channel: AsynchronousSocketChannel, bytes: Array[Byte])(implicit ec: ExecutionContext): Future[Unit] = {
    logger.debug("Writing channel")
    writeChunk(channel, bytes).flatMap { bytesWritten =>
      if (bytesWritten == bytes.length) Future.successful(())
      else write(channel, bytes.drop(bytesWritten))
    }
  }

  private def writeChunk(channel: AsynchronousSocketChannel, bytes: Array[Byte]): Future[Integer] = {
    val p = Promise[Integer]()

    channel.write(
      ByteBuffer.wrap(bytes),
      null,
      new CompletionHandler[Integer, Void]() {
        override def completed(bytesWritten: Integer, attachment: Void): Unit = {
          logger.debug(s"Bytes written: $bytesWritten")
          p.success(bytesWritten)
        }

        override def failed(e: Throwable, attachment: Void): Unit =
          p.failure(e)
      }
    )

    p.future
  }
}
