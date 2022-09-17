package dev.jsantos.kotlincryptotrade.network

import java.lang.Exception

// notifica cada vez que haya un cambio...

interface RealtimeDataListener<T> {

    // devuelve data que ha sido cambiada
    fun onDataChange(updatedData: T)

    fun onError(exception: Exception)
}