package com.example.dyf

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class LoginService {
    fun login(email: String, password: String): Boolean {
        return email == "test@example.com" && password == "password"
    }
}

class LoginServiceTest {

    private lateinit var mockLoginService: LoginService

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockLoginService = mock(LoginService::class.java)
    }

    @Test
    fun testLoginSuccess() {
        `when`(mockLoginService.login("test@example.com", "password")).thenReturn(true)

        val result = mockLoginService.login("test@example.com", "password")

        assert(result)

        verify(mockLoginService).login("test@example.com", "password")
    }

    @Test
    fun testLoginFailure() {
        `when`(mockLoginService.login("wrong@example.com", "wrongPassword")).thenReturn(false)

        val result = mockLoginService.login("wrong@example.com", "wrongPassword")

        assert(!result)

        verify(mockLoginService).login("wrong@example.com", "wrongPassword")
    }
}