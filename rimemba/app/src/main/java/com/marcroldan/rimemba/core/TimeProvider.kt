package com.marcroldan.rimemba.core

import java.time.LocalDateTime

/**
 * Inyectado en todo el dominio en vez de llamar a LocalDateTime.now() directamente,
 * para que el parser y el scheduler sean deterministas y testeables.
 */
interface TimeProvider {
    fun now(): LocalDateTime
}

class SystemTimeProvider : TimeProvider {
    override fun now(): LocalDateTime = LocalDateTime.now()
}
