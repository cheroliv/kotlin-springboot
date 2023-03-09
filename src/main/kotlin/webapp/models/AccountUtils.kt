package webapp.models

import org.apache.commons.lang3.RandomStringUtils
import java.security.SecureRandom

object AccountUtils {
    private const val DEF_COUNT = 20
    private val SECURE_RANDOM: SecureRandom by lazy {
        SecureRandom().apply { nextBytes(ByteArray(size = 64)) }
    }

    private val generateRandomAlphanumericString: String
        get() = RandomStringUtils.random(
            DEF_COUNT,
            0,
            0,
            true,
            true,
            null,
            SECURE_RANDOM
        )

    val generatePassword: String
        get() = generateRandomAlphanumericString

    val generateActivationKey: String
        get() = generateRandomAlphanumericString

    val generateResetKey: String
        get() = generateRandomAlphanumericString
}