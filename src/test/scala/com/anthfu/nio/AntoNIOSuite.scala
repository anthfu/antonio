package com.anthfu.nio

import com.anthfu.nio.AntoNIO._
import munit.FunSuite

import scala.concurrent.ExecutionContext

class AntoNIOSuite extends FunSuite {
  implicit val ec: ExecutionContext = ExecutionContext.global

  test("Start server") {
    val server = serverChannel("localhost", 8080)
    assert(server.isOpen)
  }

  test("Start client") {
    clientChannel("Localhost", 8080).map { channel =>
      assert(channel.isOpen)
    }
  }
}
