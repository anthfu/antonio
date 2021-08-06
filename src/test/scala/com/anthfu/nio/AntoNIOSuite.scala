package com.anthfu.nio

import com.anthfu.nio.AntoNIO._
import munit.FunSuite

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class AntoNIOSuite extends FunSuite {
  implicit val ec: ExecutionContext = ExecutionContext.global

  test("Send a message to the server") {
    val message = Files.readString(
      Path.of(getClass.getClassLoader.getResource("message.txt").toURI))

    val host = "localhost"
    val port = 8080

    val server = serverChannel(host, port)
    assert(server.isOpen)

    val writeFut = clientChannel(host, port).flatMap { channel =>
      assert(channel.isOpen)
      write(channel, message.getBytes(StandardCharsets.UTF_8))
    }

    Await.result(writeFut, Duration(5, TimeUnit.SECONDS)) // linearize writes and reads

    val readFut = for {
      channel <- accept(server)
      bytes   <- read(channel)
    } yield {
      new String(bytes, StandardCharsets.UTF_8)
        .replaceAll("\u0000", "")
    }

    val res = Await.result(readFut, Duration(5, TimeUnit.SECONDS))
    assertEquals(res, message)
  }
}
