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

    val server = serverChannel("localhost", 8080)
    assert(server.isOpen)

    val writeOp = clientChannel("localhost", 8080).flatMap { channel =>
      assert(channel.isOpen)
      write(channel, message.getBytes(StandardCharsets.UTF_8))
    }

    Await.result(writeOp, Duration(5, TimeUnit.SECONDS)) // linearize writes and reads

    val readOp = for {
      channel <- accept(server)
      bytes   <- read(channel)
    } yield {
      new String(bytes, StandardCharsets.UTF_8)
    }

    val res = Await.result(readOp, Duration(5, TimeUnit.SECONDS))
    assertEquals(res, message)
  }
}
