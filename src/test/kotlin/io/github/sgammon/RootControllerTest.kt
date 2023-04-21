/*
 * MIT License
 *
 * Copyright (c) 2023, Sam Gammon.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.sgammon

import elide.annotations.Inject
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpStatus
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test

@MicronautTest class RootControllerTest {
    @Inject lateinit var controller: RootController
    @Inject lateinit var server: EmbeddedServer

    @Test fun testRunning(): Unit = assert(server.isRunning) {
        "Server should be running"
    }

    @Test fun testRedirect(): Unit = controller.redirect().let {
        assert(it.status.code == HttpStatus.TEMPORARY_REDIRECT.code) {
            "Should serve a temporary redirect"
        }
        assert(it.headers.contains(HttpHeaders.LOCATION)) {
            "Redirect must have location header"
        }
        assert(it.headers[HttpHeaders.LOCATION]!!.contains("planetscale.com")) {
            "Redirect must point to planetscale.com"
        }
    }
}
