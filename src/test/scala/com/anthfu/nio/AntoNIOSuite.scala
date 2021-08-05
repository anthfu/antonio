package com.anthfu.nio

import com.anthfu.nio.AntoNIO._
import munit.FunSuite

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.concurrent.ExecutionContext

class AntoNIOSuite extends FunSuite {
  implicit val ec: ExecutionContext = ExecutionContext.global

  test("Open a client channel to the server") {
    val server = serverChannel("localhost", 8080)
    assert(server.isOpen)

    clientChannel("localhost", 8080).map { channel =>
      assert(channel.isOpen)
    }

    server.close()
  }

  test("Send a message to the server") {
    val message = Files.readString(
      Path.of(getClass.getClassLoader.getResource("message.txt").toURI))

    val server = serverChannel("localhost", 8080)
    assert(server.isOpen)

    clientChannel("localhost", 8080).map { channel =>
      assert(channel.isOpen)
      write(channel, message.getBytes(StandardCharsets.UTF_8))
    }

    for {
      channel <- accept(server)
      bytes   <- read(channel)
    } yield {
      val result = new String(bytes, StandardCharsets.UTF_8)
      assertEquals(result, message)
    }

    server.close()
  }
}
