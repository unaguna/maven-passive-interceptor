package jp.unaguna.mavenpassiveinterceptor.controller

import jakarta.servlet.http.HttpServletRequest
import jp.unaguna.mavenpassiveinterceptor.service.HttpClientService
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class MainController(
    private val httpClientService: HttpClientService,
) {
    @GetMapping("/**")
    @ResponseBody
    fun whole(request: HttpServletRequest): ResponseEntity<Resource> {
        val result = httpClientService.fetch(request.servletPath, request.method)

        return ResponseEntity.status(result.statusCode).also { res ->
            if (result.contentType != null) { res.contentType(result.contentType) }

            res.header(HttpHeaders.CONTENT_DISPOSITION, result.contentDisposition)
        }
            .body(result.bodyFile)

    }
}
