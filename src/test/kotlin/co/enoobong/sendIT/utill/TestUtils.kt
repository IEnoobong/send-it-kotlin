package co.enoobong.sendIT.utill

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.convertValue
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

inline fun <reified R> Any.toType(): R {
    return objectMapper.convertValue(this)
}

const val TOKEN =
    "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNTQzMDU1MTMyLCKleHAiOjE1NDM2NTk5MzJ9.EVP_Pv63WNfFyZemPmFKxGtaqyETSssgzJjr1V7QS3DTUY5J1zjzk4B46rAyrnmgHTZwQuGFNLD8x_-AN-BcUg"

const val ENCRYPTED_PASSWORD = "\$2a\$10\$gptP07mpA8RX2s6EI4l1KO99kQuwxU19A1ALrhpBuyvKQFTnQOGKG"