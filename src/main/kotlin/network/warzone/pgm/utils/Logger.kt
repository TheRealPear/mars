package network.warzone.pgm.utils

import org.bukkit.Bukkit
import tc.oc.pgm.util.ClassLogger
import java.util.logging.Logger
import kotlin.reflect.KClass

val BASE_LOGGER: Logger by lazy {
    Bukkit.getLogger()
}

fun <T : Any> createLogger(clazz: KClass<T>): ClassLogger {
    return ClassLogger.get(BASE_LOGGER, clazz.java)
}