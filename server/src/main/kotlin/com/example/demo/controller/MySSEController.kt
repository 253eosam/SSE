package com.example.demo.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.annotation.PostConstruct
import org.springframework.http.MediaType
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.random.Random

@CrossOrigin(origins = ["*"])
@RestController
class SseController {

    private val emitters: ConcurrentHashMap<String, SseEmitter> = ConcurrentHashMap()
    private val scheduler: TaskScheduler = ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor())

    @GetMapping("/stream-sse", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamEvents(@RequestParam(value = "user", required = false, defaultValue = "empty") user: String): SseEmitter {
        val emitter = SseEmitter(60_000L)
        emitters[user] = emitter
        emitter.send(
                SseEmitter.event().name("message").data(user)
        )
        emitter.onCompletion { emitters.remove(user) }
        emitter.onTimeout { emitters.remove(user) }

        return emitter
    }

    @PostConstruct
    fun startPeriodicTimeEvents() {
        scheduler.scheduleWithFixedDelay({
            // 모든 사용자 중 랜덤하게 한 명을 선택
            if (emitters.isNotEmpty()) {
                val users = emitters.keys.toList()
                val selectedUser = users[Random.nextInt(users.size)]

                // 선택된 사용자에게만 메시지 전송
                emitters[selectedUser]?.let { emitter ->
                    try {
                        val eventId = System.currentTimeMillis().toString()
                        val data = jacksonObjectMapper().writeValueAsString(mapOf("time" to eventId))
                        emitter.send(SseEmitter.event().id(eventId).name("message").data(data, MediaType.APPLICATION_JSON))
                    } catch (e: IOException) {
                        println("Error sending message: $e") // 로그 출력
                        emitters.remove(selectedUser)
                    }
                }
            }
        }, 2000)
    }
}
