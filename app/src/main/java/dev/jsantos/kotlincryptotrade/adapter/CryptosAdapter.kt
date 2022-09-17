package dev.jsantos.kotlincryptotrade.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.jsantos.kotlincryptotrade.R
import dev.jsantos.kotlincryptotrade.models.Crypto
import com.squareup.picasso.Picasso

// Recibe como parametro el listener CryptoAdapter
class CryptosAdapter(val cryptoAdapterListener: CryptosAdapterListener) : RecyclerView.Adapter<CryptosAdapter.ViewHolder> (){

    // Lista que guarda las criptomonedas
    var cryptoList: List<Crypto> = ArrayList()

    // ViewHolder Extiende de RecycleView.ViewHolder
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        // Componentes del layout crpto_row
        var image = view.findViewById<ImageView>(R.id.image)
        var name = view.findViewById<TextView>(R.id.nameTextView)
        var available = view.findViewById<TextView>(R.id.availableTextView)
        var buyButton = view.findViewById<TextView>(R.id.buyButton)
    }

    // Metodo que Crea la vista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.crypto_row,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Actualiza los valores de la lista de crypto monedas

        //obtenemos la posicion de la crypto que recibmos como parametro
        val crypto = cryptoList[position]
        // Actualizamos el valor del viewholder
        Picasso.get().load(crypto.imageUrl).into(holder.image)
        // Act el nombre de la crypto
        holder.name.text = crypto.name
        // Actualizamos la cantidad disponible
        Log.e("VALOR DE AVAILABLE ",crypto.available.toString())
        holder.available.text = holder.itemView.context.getString(R.string.available_message,crypto.available.toString())
        holder.buyButton.setOnClickListener{
            // Enviar evento
            cryptoAdapterListener.onBuyCryptoClicked(crypto)
        }
    }

    override fun getItemCount(): Int {
        return cryptoList.size
    }

}