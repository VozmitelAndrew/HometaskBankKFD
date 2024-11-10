import java.io.File
import java.time.LocalDateTime
import java.util.HashMap

abstract class Logger(fileName: String) : Observer {
    companion object {
        init {
            val logDir = File("Log")
            if (logDir.exists()) {
                logDir.listFiles()?.forEach { file ->
                    if (file.isFile) file.delete()
                }
            } else {
                logDir.mkdir()
            }
        }
    }

    private val logFile: File = initLogs(fileName)

    private fun initLogs(name: String): File {

        val logFile = File("Log", name)

        if (logFile.exists()) {
            logFile.writeText("")
        } else {
            logFile.createNewFile()
        }

        return logFile
    }

    protected fun logMessage(message: String) {
        synchronized(this) {
            logFile.appendText("Happened at ${LocalDateTime.now()}: $message\n")
        }
    }
}

class LoggerExchange : Logger("LoggerExchange.txt") {
    override fun update(message: String) {
        logMessage(message)
        println("Exchange logged: $message")
    }
}

class LoggerTransactions : Logger("LoggerTransactions.txt") {
    override fun update(message: String) {
        logMessage(message)
        println("Transaction logged: $message")
    }
}

open class LoggerUser private constructor(id: Int) : Logger("LoggerUser$id.txt") {

    companion object {
        private val userLoggers = HashMap<Int, LoggerUser>()

        fun getInstance(id: Int): LoggerUser {
            return userLoggers.getOrPut(id) { LoggerUser(id) }
        }
    }

    override fun update(message: String) {
        logMessage(message)
    }
}


fun main() {
    val logger = LoggerExchange()
    logger.update("Test message")
}