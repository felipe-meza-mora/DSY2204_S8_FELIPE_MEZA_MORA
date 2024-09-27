import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CambioPasswordScreenTest {

    @Test
    fun validatePasswords_correctPasswords_returnTrue() {
        val currentPassword = "password123"
        val newPassword = "newPassword123"
        val confirmPassword = "newPassword123"

        val isValid = validatePasswords(currentPassword, newPassword, confirmPassword)

        assertTrue(isValid)
    }

    @Test
    fun validatePasswords_mismatchPasswords_returnFalse() {
        val currentPassword = "password123"
        val newPassword = "newPassword123"
        val confirmPassword = "differentPassword"

        val isValid = validatePasswords(currentPassword, newPassword, confirmPassword)

        assertFalse(isValid)
    }

    private fun validatePasswords(currentPassword: String, newPassword: String, confirmPassword: String): Boolean {
        return if (newPassword == confirmPassword) {
            true
        } else {
            false
        }
    }
}
