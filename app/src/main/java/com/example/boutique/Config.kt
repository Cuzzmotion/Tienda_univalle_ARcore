import java.io.FileInputStream
import java.util.Properties

object Config {
    private val properties = Properties()

    init {
        // Load the properties file
        val inputStream = this::class.java.classLoader.getResourceAsStream("config.properties")
        inputStream?.use { properties.load(it) }
    }

    // Get properties with a default value if not found
    val databaseUrl: String = properties.getProperty("DATABASE_URL") ?: ""
    val apiKey: String = properties.getProperty("API_KEY") ?: ""
    val debugMode: Boolean = properties.getProperty("DEBUG_MODE")?.toBoolean() ?: false
}
