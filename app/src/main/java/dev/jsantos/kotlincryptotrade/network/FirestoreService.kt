package dev.jsantos.kotlincryptotrade.network

import com.google.firebase.firestore.FirebaseFirestore
import dev.jsantos.kotlincryptotrade.models.Crypto
import dev.jsantos.kotlincryptotrade.models.User


const val CRYPTO_COLLECTION_NAME = "cryptos"
const val USERS_COLLECTION_NAME = "users"
const val CRYPTOS_LIST_USER_DOC = "cryptosList"

class FirestoreService(val firebaseFirestore: FirebaseFirestore) {


    // Crear documento en Firestore
    fun setDocument(data: Any, collectionName: String, id: String, callback: Callback<Void>) {
        firebaseFirestore.collection(collectionName).document(id).set(data)
            .addOnSuccessListener { callback.onSuccess(null) }
            .addOnFailureListener { exception -> callback.onFailed(exception) }
    }

    fun updateUser(user: User, callback: Callback<User>?) {
        firebaseFirestore.collection(USERS_COLLECTION_NAME).document(user.username)
            .update(CRYPTOS_LIST_USER_DOC, user.cryptosList)
            .addOnSuccessListener { result ->
                if (callback != null)
                    callback.onSuccess(user)

            }
            .addOnFailureListener { exception -> callback!!.onFailed(exception) }
    }

    // Actualizar cantidad de monedas disponibles
    fun updateCrypto(crypto: Crypto) {
        firebaseFirestore.collection(CRYPTO_COLLECTION_NAME).document(crypto.getDocumentId())
            .update("available", crypto.available)
    }

    // Obtener lista de criptomonedas desde firestore
    fun getCryptos(callback: Callback<List<Crypto>>?) {
        firebaseFirestore.collection(CRYPTO_COLLECTION_NAME).get()
            .addOnSuccessListener { result ->
                val cryptoList = result.toObjects(Crypto::class.java)
                // propagamos el resultado(cryptoList) a traves de callback
                callback?.onSuccess(cryptoList)
            }
            .addOnFailureListener { exception ->
                callback?.onFailed(exception) // propagamos la excepcion a traves del callback
            }
    }


    fun findUserById(id: String, callback: Callback<User>) {
        firebaseFirestore.collection(USERS_COLLECTION_NAME).document(id)
            .get()
            .addOnSuccessListener { result ->
                if (result.data != null) {
                    callback.onSuccess(result.toObject(User::class.java))
                } else {
                    callback.onSuccess(null)
                }
            }
            .addOnFailureListener { exception -> callback.onFailed(exception) }
    }

        // Recibe lista de crypto monedas y listener
        fun listenForUpdates(cryptos: List<Crypto>, listener: RealtimeDataListener<Crypto>) {
            val cryptoReferenceCollection = firebaseFirestore.collection(CRYPTO_COLLECTION_NAME)
            for (crypto in cryptos) {
                cryptoReferenceCollection.document(crypto.getDocumentId())
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            listener.onError(e)
                        }
                        if (snapshot != null && snapshot.exists()) {
                            // propagamos data a traves del listener
                            listener.onDataChange(snapshot.toObject(Crypto::class.java)!!)
                        }
                    }
            }
        }




        fun listenForUpdates(user: User, listener: RealtimeDataListener<User>) {
            val usersReferenceCollection = firebaseFirestore.collection(USERS_COLLECTION_NAME)

            usersReferenceCollection.document(user.username).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    listener.onError(e)
                }
                if (snapshot != null && snapshot.exists()) {
                    // propagamos snapshot o data a traves del listener
                    listener.onDataChange(snapshot.toObject(User::class.java)!!)
                }
            }
        }




}
