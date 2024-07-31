package com.example.mdp

import android.app.AlertDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.lang.Math.abs
import kotlin.math.round

class MainActivity3 : AppCompatActivity() {
    val maxIterations = 100
    var gamma = 0.5
    private lateinit var displayPolicyLinearLayout: LinearLayout
    private fun runValueIteration(states:MutableSet<String>,stateActionMap:MutableMap<String,MutableSet<String>>,model:MutableMap<String,MutableMap<String,MutableSet<NRP>>>):MutableMap<String,Double>{
        val values = states.associateWith { 0.0 }.toMutableMap()
        val prevValues = states.associateWith { 0.0 }.toMutableMap()

        var i = 0
        var converged = false
        while(i<maxIterations && !converged){
            converged = true
            //compute new values
            for(state in states){
                var maxUtility = -1e5
                for(action in stateActionMap[state]!!){
                    var actUtility=0.0
                    for(nrp in model[state]!![action]!!){
                        actUtility = actUtility + nrp.prob*(nrp.reward+gamma*(prevValues[nrp.nextState] as Double))
                    }
                    if(actUtility>maxUtility){
                        maxUtility=actUtility
                    }
                }
                if(maxUtility!=-1e5) values[state] = maxUtility
            }
            //check covered
            for(state in states){
                if(prevValues[state]!=null && abs(values[state]!!-prevValues[state]!!)>1e-6){
                    converged = false
                }
                prevValues[state]=values[state]!!
            }
            if(converged)break
        }

        return values
    }
    fun computePolicy(values:MutableMap<String,Double>,states:MutableSet<String>,stateActionMap:MutableMap<String,MutableSet<String>>,model:MutableMap<String,MutableMap<String,MutableSet<NRP>>>):MutableMap<String,String?>{
    val policy= mutableMapOf<String,String?>()
    for(state in states){
        var maxUtility= -1e-5
        var maxAct:String? = null

        for(action in stateActionMap[state]!!){
            var actUtility = 0.0
            for(nrp in model[state]!![action]!!){
                actUtility = actUtility + nrp.prob*(nrp.reward+gamma*(values[nrp.nextState] as Double))
            }
            if(actUtility>maxUtility){
                maxUtility=actUtility
                maxAct=action
            }
        }
        policy[state] = maxAct
    }
    return policy
    }
    private fun getInputFromPrompt(promptTitle:String,states:MutableSet<String>,stateActionMap:MutableMap<String,MutableSet<String>>,model:MutableMap<String,MutableMap<String,MutableSet<NRP>>>){
        val inputEditText = EditText(this)
        inputEditText.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        // Create and configure the AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle(promptTitle)
        builder.setView(inputEditText)

        builder.setPositiveButton("OK") { _, _ ->
            var enteredText = inputEditText.text.toString().trim().lowercase()
            try{
                gamma = enteredText.toDouble()
                Toast.makeText(this,"Setting discount to $gamma",Toast.LENGTH_SHORT).show()
            }catch (e:Exception){
                Toast.makeText(this,"Can't convert to double, setting discount to 0.5",Toast.LENGTH_SHORT).show()
            }
            val values = runValueIteration(states,stateActionMap,model)
            val policy = computePolicy(values,states,stateActionMap,model)
            display(states,values,policy)
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        // Show the AlertDialog
        builder.show()
        // Request focus for the EditText and show the keyboard
        inputEditText.requestFocus()
        inputEditText.postDelayed({
            val imm =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(inputEditText, InputMethodManager.SHOW_IMPLICIT)
        }, 500) // Delay may be necessary to ensure the keyboard opens reliably
    }
    fun createTextView(text:String):TextView{
        val textView = TextView(this)
        textView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        textView.text = text
        textView.textSize = 16f
        textView.setPadding(16, 16, 16, 16)
        return textView
    }
    fun display(states:MutableSet<String>,values:MutableMap<String,Double>,policy:MutableMap<String,String?>){
        for(state in states){
            val parentLayout = LinearLayout(this)
            parentLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            parentLayout.orientation = LinearLayout.HORIZONTAL
            val stateTextView = createTextView("$state:")
            val valueTextView = createTextView("${round(values[state]!!)}")
            val actionTextView = createTextView("${policy[state]?:"null"}")
            parentLayout.addView(stateTextView)
            parentLayout.addView(valueTextView)
            parentLayout.addView(actionTextView)
            displayPolicyLinearLayout.addView(parentLayout)
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        val states=intent.getSerializableExtra("states") as? MutableSet<String>
        val stateActionMap = intent.getSerializableExtra("stateActionMap") as? MutableMap<String,MutableSet<String>>
        val model = intent.getSerializableExtra("model") as? MutableMap<String,MutableMap<String,MutableSet<NRP>>>
        displayPolicyLinearLayout = findViewById(R.id.display)
        getInputFromPrompt("Enter Discount Factor",states!!,stateActionMap!!,model!!)
    }
}