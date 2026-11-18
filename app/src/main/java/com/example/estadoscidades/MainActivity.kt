package com.example.estadoscidades

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    private val urlBase =
        "https://servicodados.ibge.gov.br/api/v1/localidades"

    private lateinit var spnEstados: Spinner
    private lateinit var prbCarregando: ProgressBar
    private lateinit var txtQuantidade: TextView
    private lateinit var rcvCidades: RecyclerView

    private val listaEstados = mutableListOf<Estado>()
    private val listaCidades = mutableListOf<Cidade>()

    private lateinit var cidadeAdapter: CidadeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v, insets ->

            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        spnEstados = findViewById(R.id.spnEstados)
        prbCarregando = findViewById(R.id.prbCarregando)
        txtQuantidade = findViewById(R.id.txtQuantidade)
        rcvCidades = findViewById(R.id.rcvCidades)

        // Configura a RecyclerView
        cidadeAdapter = CidadeAdapter(listaCidades)

        rcvCidades.layoutManager = LinearLayoutManager(this)
        rcvCidades.adapter = cidadeAdapter

        rcvCidades.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        // O que acontece quando o usuário escolhe um estado
        spnEstados.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (position == 0) {

                        listaCidades.clear()
                        cidadeAdapter.notifyDataSetChanged()

                        txtQuantidade.text =
                            "Nenhum estado selecionado"

                        return
                    }

                    // -1 porque a posição 0 é o texto inicial
                    val estado = listaEstados[position - 1]

                    buscarCidades(estado)
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                }
            }

        buscarEstados()
    }

    private fun buscarEstados() {

        prbCarregando.visibility = View.VISIBLE

        val request = Request.Builder()
            .url("$urlBase/estados?orderBy=nome")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(
                call: Call,
                e: IOException
            ) {

                runOnUiThread {

                    prbCarregando.visibility = View.GONE

                    Toast.makeText(
                        this@MainActivity,
                        "Falha na conexão: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(
                call: Call,
                response: Response
            ) {

                val corpo = response.body?.string()

                if (!response.isSuccessful || corpo == null) {

                    runOnUiThread {

                        prbCarregando.visibility = View.GONE

                        Toast.makeText(
                            this@MainActivity,
                            "Erro ${response.code} ao buscar estados",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    return
                }

                val jsonArray = JSONArray(corpo)

                val estados = mutableListOf<Estado>()

                for (i in 0 until jsonArray.length()) {

                    val obj = jsonArray.getJSONObject(i)

                    estados.add(
                        Estado(
                            obj.getInt("id"),
                            obj.getString("sigla"),
                            obj.getString("nome")
                        )
                    )
                }

                runOnUiThread {

                    prbCarregando.visibility = View.GONE

                    listaEstados.clear()
                    listaEstados.addAll(estados)

                    // Monta os textos que aparecem no Spinner
                    val nomes = mutableListOf("Selecione um estado")

                    for (estado in listaEstados) {

                        nomes.add(
                            "${estado.sigla} - ${estado.nome}"
                        )
                    }

                    val adapterSpinner = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_spinner_item,
                        nomes
                    )

                    adapterSpinner.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item
                    )

                    spnEstados.adapter = adapterSpinner
                }
            }
        })
    }

    private fun buscarCidades(estado: Estado) {

        prbCarregando.visibility = View.VISIBLE

        txtQuantidade.text =
            "Buscando cidades de ${estado.nome}..."

        val request = Request.Builder()
            .url(
                "$urlBase/estados/${estado.sigla}/municipios?orderBy=nome"
            )
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(
                call: Call,
                e: IOException
            ) {

                runOnUiThread {

                    prbCarregando.visibility = View.GONE

                    txtQuantidade.text =
                        "Não foi possível carregar as cidades"

                    Toast.makeText(
                        this@MainActivity,
                        "Falha na conexão: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(
                call: Call,
                response: Response
            ) {

                val corpo = response.body?.string()

                if (!response.isSuccessful || corpo == null) {

                    runOnUiThread {

                        prbCarregando.visibility = View.GONE

                        txtQuantidade.text =
                            "Erro ${response.code} ao buscar cidades"
                    }

                    return
                }

                val jsonArray = JSONArray(corpo)

                val cidades = mutableListOf<Cidade>()

                for (i in 0 until jsonArray.length()) {

                    val obj = jsonArray.getJSONObject(i)

                    cidades.add(
                        Cidade(
                            obj.getInt("id"),
                            obj.getString("nome")
                        )
                    )
                }

                runOnUiThread {

                    prbCarregando.visibility = View.GONE

                    listaCidades.clear()
                    listaCidades.addAll(cidades)

                    cidadeAdapter.notifyDataSetChanged()

                    // Volta a lista para o topo
                    rcvCidades.scrollToPosition(0)

                    txtQuantidade.text =
                        "${cidades.size} cidades encontradas em ${estado.nome}"
                }
            }
        })
    }
}
