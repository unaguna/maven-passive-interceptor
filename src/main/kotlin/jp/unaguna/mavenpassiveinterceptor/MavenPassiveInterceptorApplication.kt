package jp.unaguna.mavenpassiveinterceptor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MavenPassiveInterceptorApplication

fun main(args: Array<String>) {
	runApplication<MavenPassiveInterceptorApplication>(*args)
}
