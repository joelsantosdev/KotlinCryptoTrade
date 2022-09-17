package dev.jsantos.kotlincryptotrade.adapter

import dev.jsantos.kotlincryptotrade.models.Crypto

interface CryptosAdapterListener{


    fun onBuyCryptoClicked(crypto: Crypto){

    }
}