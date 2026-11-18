package com.example.estadoscidades

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CidadeAdapter(
    private val cidades: MutableList<Cidade>
) : RecyclerView.Adapter<CidadeAdapter.CidadeViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CidadeViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cidade, parent, false)

        return CidadeViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CidadeViewHolder,
        position: Int
    ) {

        val cidade = cidades[position]

        holder.txtNome.text = cidade.nome
        holder.txtCodigo.text = "Código IBGE: ${cidade.id}"
    }

    override fun getItemCount(): Int = cidades.size

    class CidadeViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val txtNome: TextView =
            itemView.findViewById(R.id.txtNome)

        val txtCodigo: TextView =
            itemView.findViewById(R.id.txtCodigo)
    }
}
