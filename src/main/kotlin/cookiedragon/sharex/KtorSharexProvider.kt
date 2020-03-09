package cookiedragon.sharex

import cookiedragon.sharex.ShareX.directory
import cookiedragon.sharex.ShareX.fileNameProvider
import cookiedragon.sharex.ShareX.token
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.http.content.*
import io.ktor.request.receiveMultipart
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * @author cookiedragon234 09/Mar/2020
 */
object ShareX: ApplicationFeature<ApplicationCallPipeline, ShareX, ShareX> {
	var token: String? = null
	var directory: File = File("i")
	var fileNameProvider: () -> String = {
		UUID.randomUUID().toString()
	}
	
	override val key: AttributeKey<ShareX> = AttributeKey("ShareX")
	
	override fun install(pipeline: ApplicationCallPipeline, configure: ShareX.() -> Unit): ShareX {
		return ShareX.also(configure).also {
			if (!directory.exists()) {
				directory.mkdir()
			}
		}
	}
}

fun Routing.shareX(folder: String = "") {
	route(folder) {
		post("upload") {
			val multipart = this.call.receiveMultipart()
			var secret: String? = null
			var ext: String? = null
			var stream: (() -> InputStream)? = null
			multipart.forEachPart { part ->
				when (part) {
					is PartData.FormItem -> {
						if (part.name == "token") {
							secret = part.value
						}
					}
					is PartData.FileItem -> {
						ext = File(part.originalFileName!!).extension
						stream = part.streamProvider
					}
					is PartData.BinaryItem -> {
						println(part.headers)
						println(part.contentDisposition)
						println(part.contentType)
						println(part.name)
					}
				}
			}
			
			when {
				secret == null -> error("Invalid parameters: Supply a secret")
				ext == null    -> error("Invalid parameters: Supply an extension")
				stream == null -> error("Invalid parameters: Supply a stream")
			}
			
			if (secret != null && ext != null && stream != null) {
				if (token == null || token == secret) {
					val childFile = "${fileNameProvider()}.$ext"
					val file = File(directory, childFile)
					stream!!().use { input ->
						file.outputStream().buffered().use { output ->
							input.copyToSuspend(output)
						}
						// This assumes that the shareX call is not nested within routes. Not the best option but I cant
						// think of another way, so this will do for now.
						call.respondText("${folder.removeSuffix("/")}/get/$childFile")
						return@post
					}
				}
			}
			error("Invalid parameters")
		}
		static ("get") {
			files(directory)
		}
	}
}

suspend fun InputStream.copyToSuspend(
	out: OutputStream,
	bufferSize: Int = DEFAULT_BUFFER_SIZE,
	yieldSize: Int = 4 * 1024 * 1024,
	dispatcher: CoroutineDispatcher = Dispatchers.IO
): Long {
	return withContext(dispatcher) {
		val buffer = ByteArray(bufferSize)
		var bytesCopied = 0L
		var bytesAfterYield = 0L
		while (true) {
			val bytes = read(buffer).takeIf { it >= 0 } ?: break
			out.write(buffer, 0, bytes)
			if (bytesAfterYield >= yieldSize) {
				yield()
				bytesAfterYield %= yieldSize
			}
			bytesCopied += bytes
			bytesAfterYield += bytes
		}
		return@withContext bytesCopied
	}
}
