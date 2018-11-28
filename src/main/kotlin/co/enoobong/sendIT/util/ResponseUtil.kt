package co.enoobong.sendIT.util

import org.springframework.http.HttpStatus

fun Int.toHttpStatus(): HttpStatus {
    return HttpStatus.resolve(this) ?: HttpStatus.INTERNAL_SERVER_ERROR
}