package jp.unaguna.mavenpassiveinterceptor.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.fileSize
import kotlin.jvm.optionals.getOrNull

@Service
class HttpClientService(
    @Value("\${mavenpassiveinterceptor.source.url}") private val sourceUrl: URI,
    @Value("\${mavenpassiveinterceptor.cache.root}") private val cacheRoot: Path,
) {
    fun fetch(path: String, httpMethod: String): FetchResult {
        val relativePath = path.removePrefix("/")
        val url = sourceUrl.resolve(relativePath)
        // TODO: path の末尾が / 、クエリパラメータがある等の対策
        val cachePath = cacheRoot.resolve(relativePath)

        // TODO: キャッシュの活用

        cachePath.deleteIfExists()
        cachePath.parent.createDirectories()

        val httpClient = HttpClient.newHttpClient()
        val httpRequest = HttpRequest.newBuilder(url)
            .method(httpMethod, BodyPublishers.noBody())
            .build()

        logger.debug("send to {}", httpRequest.uri())
        val httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofFile(cachePath))
        logger.debug(
            "response received; status={}, size={}, header={}",
            httpResponse.statusCode(),
            cachePath.fileSize(),
            httpResponse.headers(),
        )

        return FetchResult(
            FileSystemResource(cachePath),
            statusCode = httpResponse.statusCode(),
            contentType = httpResponse.headers().firstValue(HttpHeaders.CONTENT_TYPE)
                .map { MediaType.parseMediaType(it) }
                .getOrNull(),
            contentDisposition = httpResponse.headers().firstValue(HttpHeaders.CONTENT_DISPOSITION).getOrNull(),
        )
    }

    data class FetchResult(
        val bodyFile: FileSystemResource,
        val statusCode: Int,
        val contentType: MediaType?,
        val contentDisposition: String?,
    )
}

private val logger = LoggerFactory.getLogger(HttpClientService::class.java)
