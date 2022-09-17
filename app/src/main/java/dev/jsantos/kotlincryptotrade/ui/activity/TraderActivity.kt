package dev.jsantos.kotlincryptotrade.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import dev.jsantos.kotlincryptotrade.R
import dev.jsantos.kotlincryptotrade.adapter.CryptosAdapter
import dev.jsantos.kotlincryptotrade.adapter.CryptosAdapterListener
import dev.jsantos.kotlincryptotrade.models.Crypto
import dev.jsantos.kotlincryptotrade.models.User
import dev.jsantos.kotlincryptotrade.network.Callback
import dev.jsantos.kotlincryptotrade.network.FirestoreService
import dev.jsantos.kotlincryptotrade.network.RealtimeDataListener
import kotlinx.android.synthetic.main.activity_trader.*
import java.lang.Exception


class TraderActivity : AppCompatActivity(), CryptosAdapterListener {

    // definimos una instancia de nuestro servicio
    lateinit var firestoreService: FirestoreService

    // instancia de cryptosAdapter
    private val cryptosAdapter: CryptosAdapter = CryptosAdapter(this)
    private var username: String? = null
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trader)

        // inicializamos servicio
        firestoreService = FirestoreService(FirebaseFirestore.getInstance())
        username = intent.extras!![USERNAME_KEY]!!.toString()
        // actualizamos el nombre del usuario en la vista
        usernameTextView.text = username
        configureRecyclerView()
        loadCryptos()

        fab.setOnClickListener { view ->
            Snackbar.make(view, getString(R.string.generating_new_cryptos), Snackbar.LENGTH_SHORT)
                .setAction("Info", null).show()
            generateCryptoCurrenciesRandom()
        }

    }

    // generar monedas para no quedarte sin balance en el simulador
    private fun generateCryptoCurrenciesRandom() {
        for (crypto in cryptosAdapter.cryptoList) {
            val amount = (1..10).random()
            crypto.available += amount
            firestoreService.updateCrypto(crypto)
        }
    }

    private fun loadCryptos() {
        // Cargar cryptos desde firestore
        // callback que retorna una lista de cryptomodenas
        firestoreService.getCryptos(object : Callback<List<Crypto>> {
            override fun onSuccess(cryptoList: List<Crypto>?) {

                // nos devuelve el usuario que encuentra en DB
                firestoreService.findUserById(username!!, object : Callback<User> {
                    override fun onSuccess(result: User?) {
                        // exitoso - guardamos el usuario recuperado
                        user = result
                        if (user!!.cryptosList == null) {
                            val userCryptoList = mutableListOf<Crypto>()
                            for (crypto in cryptoList!!) {
                                // instancia de la clase crypto
                                val cryptoUser = Crypto()
                                // set data
                                cryptoUser.name = crypto.name
                                cryptoUser.available = crypto.available
                                cryptoUser.imageUrl = crypto.imageUrl
                                userCryptoList.add(cryptoUser)

                            }
                            // le asiganmos la lista de crypto a la lista del usuario
                            user!!.cryptosList = userCryptoList
                            firestoreService.updateUser(user!!, null)
                        }

                        loadUserCryptosOnView()
                        // Realtime listener para actualizar datos despues de compra de criptomoneda
                        addRealtimeDatabaseListeners(user!!,cryptoList!!)


                    }

                    override fun onFailed(exception: Exception) {
                        showGeneralServerErrorMessage()
                    }
                })


                // exitoso - Actualizamos la vista
                this@TraderActivity.runOnUiThread {
                    // Actualizamos la lista de criptomonedas
                    cryptosAdapter.cryptoList = cryptoList!!
                    cryptosAdapter.notifyDataSetChanged()
                }

            }

            override fun onFailed(exception: Exception) {
                Log.e("Trader Activity", "error al cargar las criptomonedas", exception)
                showGeneralServerErrorMessage()
            }

        })

    }

    private fun addRealtimeDatabaseListeners(user: User, cryptosList: List<Crypto>) {

        firestoreService.listenForUpdates(user, object : RealtimeDataListener<User> {
            override fun onDataChange(updatedData: User) {
                this@TraderActivity.user = updatedData
                // loadUserCryptos()
            }

            override fun onError(exception: Exception) {
                showGeneralServerErrorMessage()
            }


        })

        firestoreService.listenForUpdates(cryptosList, object : RealtimeDataListener<Crypto> {
            override fun onDataChange(updatedData: Crypto) {
                var pos = 0
                for (crypto in cryptosAdapter.cryptoList) {
                    // Buscar moneda que vamos actualizar
                    if (crypto.name.equals(updatedData.name)) {
                        crypto.available = updatedData.available
                        // Actuazamos el item en la posicion especifica
                        cryptosAdapter.notifyItemChanged(pos)
                    }
                    pos++
                }
            }

            override fun onError(exception: Exception) {
                showGeneralServerErrorMessage()
            }

        })

    }



    private fun loadUserCryptosOnView() {
        runOnUiThread {
            if (user != null && user!!.cryptosList != null) {
                // limpiar vista
                infoPanel.removeAllViews()
                // mostrar monedas del usuario en el info panel
                for (cryptomoneda in user!!.cryptosList!!) {
                    addUserCryptoInfoRow(cryptomoneda)
                }

            }
        }
    }

    private fun addUserCryptoInfoRow(cryptomoneda: Crypto) {
        // this -> porque es esta actividad
        val view = LayoutInflater.from(this).inflate(R.layout.coin_info, infoPanel, false)
        // Actualizamos los valores de la vista(available y nombre de la moneda) y img de la cryptomoneda
        view.findViewById<TextView>(R.id.coinLabel).text =
            getString(R.string.coin_info, cryptomoneda.name, cryptomoneda.available.toString())
        Picasso.get().load(cryptomoneda.imageUrl).into(view.findViewById<ImageView>(R.id.coinIcon))
        // agregamos vista al layout
        infoPanel.addView(view)
    }

    private fun configureRecyclerView() {
        // TODO
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = cryptosAdapter
    }

    fun showGeneralServerErrorMessage() {
        Snackbar.make(
            fab,
            getString(R.string.error_while_connecting_to_the_server),
            Snackbar.LENGTH_LONG
        )
            .setAction("Info", null).show()
    }


    // funciona para comprar criptomonedas
    override fun onBuyCryptoClicked(crypto: Crypto) {
        // Validamos cantaidad antes de comprar...
        if (crypto.available > 0) {
            for (userCrypto in user!!.cryptosList!!) {
                if (userCrypto.name == crypto.name) {
                    userCrypto.available += 1
                    break
                }
            }
            crypto.available--

            firestoreService.updateUser(user!!, null)
            firestoreService.updateCrypto(crypto)
        }
    }

}