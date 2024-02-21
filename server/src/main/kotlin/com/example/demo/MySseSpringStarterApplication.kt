package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MySseSpringStarterApplication

fun main(args: Array<String>) {
	runApplication<MySseSpringStarterApplication>(*args)
	println("Hello world~!")
}
