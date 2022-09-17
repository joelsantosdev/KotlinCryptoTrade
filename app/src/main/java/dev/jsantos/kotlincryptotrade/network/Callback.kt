package dev.jsantos.kotlincryptotrade.network
import java.lang.Exception

// Callback que notifica si operacion fue exitosa o falló
interface Callback<T>{

    // Generico para mapear cualquier tipo de resultado <T>
    fun onSuccess(result: T?)

    fun onFailed(exception: Exception)
}