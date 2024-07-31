package com.example.mdp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mdp.adapters.TableAdapter
import com.google.android.material.tabs.TabLayout.Tab

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

//        retrieve the data structures of use
        val states=intent.getSerializableExtra("states") as? MutableSet<String>
        val stateActionMap = intent.getSerializableExtra("stateActionMap") as? MutableMap<String,MutableSet<String>>
        val model = intent.getSerializableExtra("model") as? MutableMap<String,MutableMap<String,MutableSet<NRP>>>

//        println(states)
//        states!!.add("hello")
//        states.add("hi")
//        states.add("ye")
//        stateActionMap!!["hello"]= mutableSetOf("1","2","3")
//        stateActionMap!!["hi"]= mutableSetOf("1","2","3")
//        stateActionMap!!["ye"]= mutableSetOf("1","2","3")
//        model!!.put("hello", mutableMapOf())
//        model["hello"]!!.put("1", mutableSetOf())
//        model["hello"]!!.put("2", mutableSetOf())
//        model["hello"]!!.put("3", mutableSetOf())
//        model!!.put("hi", mutableMapOf())
//        model["hi"]!!.put("1", mutableSetOf())
//        model["hi"]!!.put("2", mutableSetOf())
//        model["hi"]!!.put("3", mutableSetOf())
//        model!!.put("ye", mutableMapOf())
//        model["ye"]!!.put("1", mutableSetOf())
//        model["ye"]!!.put("2", mutableSetOf())
//        model["ye"]!!.put("3", mutableSetOf())
//        model!!["hello"]!!["1"]!!.add(NRP("o",1.0,1.0))
//        model!!["hello"]!!["1"]!!.add(NRP("p",1.0,1.0))
//        model!!["hello"]!!["1"]!!.add(NRP("q",1.0,1.0))
//        model!!["hello"]!!["2"]!!.add(NRP("o",1.0,1.0))
//        model!!["hello"]!!["2"]!!.add(NRP("p",1.0,1.0))
//        model!!["hello"]!!["2"]!!.add(NRP("q",1.0,1.0))
//        model!!["hello"]!!["3"]!!.add(NRP("o",1.0,1.0))
//        model!!["hello"]!!["3"]!!.add(NRP("p",1.0,1.0))
//        model!!["hello"]!!["3"]!!.add(NRP("q",1.0,1.0))
//        model!!["hello"]!!["3"]!!.add(NRP("r",1.0,1.0))
//        model!!["hello"]!!["3"]!!.add(NRP("s",1.0,1.0))
//        model!!["hi"]!!["1"]!!.add(NRP("o",1.0,1.0))
//        model!!["hi"]!!["1"]!!.add(NRP("o",1.0,1.0))
//        model!!["hi"]!!["1"]!!.add(NRP("o",1.0,1.0))
//        model!!["hi"]!!["2"]!!.add(NRP("o",1.0,1.0))
//        model!!["hi"]!!["2"]!!.add(NRP("o",1.0,1.0))
//        model!!["hi"]!!["2"]!!.add(NRP("o",1.0,1.0))
//        model!!["hi"]!!["3"]!!.add(NRP("o",1.0,1.0))
//        model!!["hi"]!!["3"]!!.add(NRP("o",1.0,1.0))
//        model!!["hi"]!!["3"]!!.add(NRP("o",1.0,1.0))
//        model!!["hi"]!!["3"]!!.add(NRP("o",1.0,1.0))
//        model!!["hi"]!!["3"]!!.add(NRP("o",1.0,1.0))
//        model!!["ye"]!!["1"]!!.add(NRP("o",1.0,1.0))
//        model!!["ye"]!!["1"]!!.add(NRP("o",1.0,1.0))
//        model!!["ye"]!!["1"]!!.add(NRP("o",1.0,1.0))
//        model!!["ye"]!!["2"]!!.add(NRP("o",1.0,1.0))
//        model!!["ye"]!!["2"]!!.add(NRP("o",1.0,1.0))
//        model!!["ye"]!!["2"]!!.add(NRP("o",1.0,1.0))
//        model!!["ye"]!!["3"]!!.add(NRP("o",1.0,1.0))
//        model!!["ye"]!!["3"]!!.add(NRP("o",1.0,1.0))
//        model!!["ye"]!!["3"]!!.add(NRP("o",1.0,1.0))
//        model!!["ye"]!!["3"]!!.add(NRP("o",1.0,1.0))
//        model!!["ye"]!!["3"]!!.add(NRP("o",1.0,1.0))
        val button = findViewById<Button>(R.id.policy)
        val recyclerView = findViewById<RecyclerView>(R.id.table)
        val adapter = TableAdapter(states!!,stateActionMap!!,model!!)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        button.setOnClickListener{
            val intent = Intent(this, MainActivity3::class.java)
            intent.putExtra("states",states as java.io.Serializable)
            intent.putExtra("stateActionMap",stateActionMap as java.io.Serializable)
            intent.putExtra("model",model as java.io.Serializable)
            startActivity(intent)
        }
    }

}