package co.enoobong.sendIT.utill

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

val objectMapper: ObjectMapper
    get() {
        return jacksonObjectMapper().apply {
            registerModule(JavaTimeModule())
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        }

    }

fun <T> T.toJsonString(): String {
    return objectMapper.writeValueAsString(this)
}

const val USER_TOKEN =
    "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNTQzMzQwMDQzLCJleHAiOjE1NDM5NDQ4NDN9.fnAtynsY5MHHhoRE6DiUlUzuG0uisSvnKkcdQcwAhDN1IE9JPKj8UWnkyoSuoQzimqt48ElDlUoZOJIFWvyN2A"

const val ENCRYPTED_PASSWORD = "\$2a\$10\$gptP07mpA8RX2s6EI4l1KO99kQuwxU19A1ALrhpBuyvKQFTnQOGKG"

const val USER_ID = 1L
const val ADMIN_ID = 2L