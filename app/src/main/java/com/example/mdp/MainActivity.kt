package com.example.mdp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.text.InputType
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.lifecycle.enableSavedStateHandles
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random
import kotlin.random.nextInt

data class NRP(val nextState: String, val reward: Double, val prob: Double): java.io.Serializable
class MainActivity : AppCompatActivity() {
    private lateinit var canvas:LinearLayout
    private lateinit var hiddenControls:LinearLayout
    private lateinit var controls:LinearLayout
    private lateinit var arrowHelper:FrameLayout
    private lateinit var deleteStateButton: Button
    private lateinit var editStateButton: Button
    private lateinit var addActionStateButton: Button
    private lateinit var addModelStateButton: Button
    private lateinit var addStateButton: Button
    private var hiddenControlsVisible = false
    private val inUse = mutableSetOf<String>()      //contains all states that are in some connection
    private val states = mutableSetOf<String>()   //contains all the states

    private var up_down = false
    private var heightOffsetUp = 80     //create for each state
    private var heightOffsetDown = 24  //create for each state

    private lateinit var offsetStatesPosUp:MutableMap<String,Int>
    private lateinit var offsetStatesPosDown:MutableMap<String,Int>

    //    for each state, we have some actions
//    for each (state,action), we have (nextState,reward,probability)
    private val stateActionMap = mutableMapOf<String,MutableSet<String>>()
    private val model = mutableMapOf<String,MutableMap<String,MutableSet<NRP>>>()

    private fun validateDistribution():Boolean{
        for(state in states){
            for(action in stateActionMap[state]!!){
                var sum= 0.toDouble()
                for(nrp in model[state]!![action]!!){
                    sum += nrp.prob
                }
                if(sum>0 && abs(1-sum)>1e-6){
                    return false
                }
            }
        }
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun disableScreen(selectedState: TextView){
        //disable the controls layout completely
        controls.isClickable = false
        controls.alpha = 0.2f
        //disable the other text views except the on that was clicked
        var childView:View?
        for (i in 0 until canvas.childCount){
            childView = canvas.getChildAt(i)
            childView.isClickable = false
            childView.alpha = 0.2f
        }
        //make the selected state text view as clickable
        selectedState.isClickable = true
        selectedState.alpha = 1.0f
//        disable the on touch listener
        addStateButton.setOnTouchListener(null)
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun enableScreen(){
        //disable the controls layout completely
        controls.isClickable = true
        controls.alpha = 1.0f
        //disable the other text views except the on that was clicked
        var childView:View?
        for (i in 0 until canvas.childCount){
            childView = canvas.getChildAt(i)
            childView.isClickable = true
            childView.alpha = 1.0f
        }
        addStateButton.setOnTouchListener { _, event ->
            animate(event)
            false // Return false to allow the event to continue propagating.
        }
    }
    private fun inputModelEntry(promptTitle:String,selectedState:TextView?){
        // Inflate the custom layout
        val inflater = LayoutInflater.from(this)
        val customLayout = inflater.inflate(R.layout.input_model_entry_layout, null)
        val spinner1 = customLayout.findViewById<Spinner>(R.id.firstSpinner)
        val adapter1 = ArrayAdapter(this, android.R.layout.simple_spinner_item, stateActionMap[selectedState!!.text]!!.toList())
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner1.adapter = adapter1
        val spinner2 = customLayout.findViewById<Spinner>(R.id.secondSpinner)
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, states.toList())
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner2.adapter = adapter2

        AlertDialog.Builder(this)
            .setTitle(promptTitle)
            .setView(customLayout)
            .setPositiveButton("OK"){_,_->
                //display the selected option
                val action:String? = spinner1.selectedItem as? String
                val nextState:String? = spinner2.selectedItem as? String
                val prob = customLayout.findViewById<EditText>(R.id.firstEditText).text.toString()
                val reward = customLayout.findViewById<EditText>(R.id.secondEditText).text.toString()
                //checks
                if(action==null){
                    Toast.makeText(this,"Can't set transition for a null action!",Toast.LENGTH_SHORT).show()
                }else if(nextState==null){
                    Toast.makeText(this,"Action $action must lead to a next State!",Toast.LENGTH_SHORT).show()
                }else if(prob.isEmpty()){
                    Toast.makeText(this,"Probability value must be entered",Toast.LENGTH_SHORT).show()
                }else if(reward.isEmpty()){
                    Toast.makeText(this,"Reward value must be entered",Toast.LENGTH_SHORT).show()
                }else{
                    addModelState(selectedState.text.toString(),action,nextState,prob.toDouble(),reward.toDouble())
                }
            }
            .setNegativeButton("Cancel"){dialog,_->
                dialog.cancel()
            }
            .show()

    }
    private fun getTextViewFromText(text:String):TextView?{
        for (i in 0 until canvas.childCount) {
            val childView = canvas.getChildAt(i)
            if (childView is TextView && childView.text == text)
                //found
                return childView
        }
        return null
    }
    private fun getInputFromPrompt(promptTitle:String,choice:String,selectedState:TextView?){
        if(inUse.size>0){
            Toast.makeText(this,"Can't alter the states/actions when model is defined",Toast.LENGTH_SHORT).show()
            return
        }
        // Create an EditText for text input
        val inputEditText = EditText(this)
        inputEditText.inputType = InputType.TYPE_CLASS_TEXT

        // Create and configure the AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle(promptTitle)
        builder.setView(inputEditText)

        var enteredText:String
        builder.setPositiveButton("OK") { _, _ ->
            enteredText = inputEditText.text.toString().trim().lowercase()
            when (choice) {
                "add" -> {
                    addState(enteredText)
                }
                "edit" -> {
                    editState(enteredText,selectedState)
                }
                "action" -> {
                    addActionState(selectedState as TextView,enteredText)
                }
            }
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
    private fun addState(enteredText:String?){
//        val enteredText = getInputFromPrompt()
        if(enteredText == null || enteredText.isEmpty()){
                Toast.makeText(this,"State Label cannot be empty!",Toast.LENGTH_SHORT).show()
        }else if(states.contains(enteredText)){
                Toast.makeText(this,"State Label already in use!",Toast.LENGTH_SHORT).show()
        }else{
            // Create a new TextView with the entered text
            states.add(enteredText)
            stateActionMap[enteredText] = mutableSetOf()
            model[enteredText] = mutableMapOf()
            val newTextView = TextView(this)
            newTextView.text = enteredText
            // Create LayoutParams with margins
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            // Set margins (left, top, right, bottom) in pixels
            layoutParams.setMargins(32, 0, 16, 0)

            newTextView.layoutParams = layoutParams
            newTextView.setPadding(16, 16, 16, 16)
            newTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 64.0f)
            newTextView.setBackgroundResource(R.drawable.round_text_view)
            // Add the TextView to the container
            canvas.addView(newTextView,0)

//                add onclick listeners to all these text views
            newTextView.setOnClickListener {
                val selectedState = it as TextView
//                    first of all make other layouts and views un-clickable, so that no other text view can be tapped

//                    Toast.makeText(this,newTextView.text,Toast.LENGTH_SHORT).show()
//                    display following controls:-
//                    1. delete state
//                    2. add action
//                    3. define model
//                    these three buttons are needed

//                    the eale the scree
                if(!hiddenControlsVisible){
                    disableScreen(selectedState)
                    hiddenControls.visibility = View.VISIBLE
                    hiddenControlsVisible = true
                }else{
                    enableScreen()
                    hiddenControlsVisible = false
                    hiddenControls.visibility = View.INVISIBLE
                }
                deleteStateButton.setOnClickListener{
                    if(hiddenControlsVisible)deleteState(selectedState)
                }
                editStateButton.setOnClickListener{
                    if(hiddenControlsVisible)getInputFromPrompt("Enter new label","edit",selectedState)
                }
                addActionStateButton.setOnClickListener{
                    if(hiddenControlsVisible)getInputFromPrompt("Enter Action","action",selectedState)
                }
                addModelStateButton.setOnClickListener{
                    if(hiddenControlsVisible)inputModelEntry("Enter your transition",selectedState)
                }
            }

        }
    }
    private fun deleteState(selectedState:TextView){

//        implement to delete only if no connections
        if(inUse.contains(selectedState.text.toString())){
            Toast.makeText(this,"Can't delete a state with associated transitions!",Toast.LENGTH_SHORT).show()
            return
        }


        states.remove(selectedState.text)
        stateActionMap.remove(selectedState.text)
//        first re-eale the scree
        enableScreen()
        hiddenControlsVisible = false
        hiddenControls.visibility = View.INVISIBLE

        //the remove the view finally
        canvas.removeView(selectedState)
        Toast.makeText(this,"Deleted state ${selectedState.text}",Toast.LENGTH_SHORT).show()
    }
    private fun editState(enteredText: String?,selectedState:TextView?){
//        implement to edit only if no connections
        if(inUse.contains(selectedState!!.text.toString())) {
            Toast.makeText(
                this,
                "Can't edit a state with associated transitions!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if(enteredText == null || enteredText.isEmpty()){
            Toast.makeText(this,"State Label cannot be empty!",Toast.LENGTH_SHORT).show()
        }else if(states.contains(enteredText)){
            Toast.makeText(this,"State Label already in use!",Toast.LENGTH_SHORT).show()
        }else {
            states.remove(selectedState.text)
            model.remove(selectedState.text)
            val actions = stateActionMap[selectedState.text]
            stateActionMap.remove(selectedState.text.toString())
            states.add(enteredText)
            if(actions!=null ){
                //will always e this case only
                stateActionMap[enteredText] = actions
            }
            selectedState.text = enteredText
            model[enteredText] = mutableMapOf()
        }
    }
    private fun addActionState(selectedState:TextView,action: String){

//        println(selectedState.text.toString())
        if(action.isEmpty()){
            Toast.makeText(this,"Action cannot be empty!",Toast.LENGTH_SHORT).show()
            return
        }
        stateActionMap[selectedState.text]?.add(action)
        model[selectedState.text.toString()]!![action] = mutableSetOf()
        Toast.makeText(this,"Added action $action to state ${selectedState.text}!",Toast.LENGTH_SHORT).show()
    }
    private fun addModelState(state:String,action: String,nextState:String,prob: Double,reward: Double){

        Toast.makeText(this,"Added transition from $state to $nextState",Toast.LENGTH_SHORT).show()
//        input prompt needs to give these things
//        1. start state is already selected
//        2. a select list drop down showing all actions for start state
//        3. a select list drop down showing all the states as candidates for nextState
//        4. reward for this transition
//        5. probability for this transition
        if(inUse.size==0){
            //initialize the heights offset array
            for(stateCur in states){
                offsetStatesPosUp[stateCur] = heightOffsetUp
            }
            for(stateCur in states){
                offsetStatesPosDown[stateCur] = heightOffsetDown
            }
//            println("testing offset")
        }
        model[state]!![action]!!.add(NRP(nextState,reward, prob))
        inUse.add(state)
        inUse.add(nextState)
        //code for drawing the arrow
        val arrow = ArcDrawingView(this,null)

        val curTextView = getTextViewFromText(state)
        val nextTextView = getTextViewFromText(nextState)
        val indexCur = canvas.indexOfChild(curTextView)
        val indexnext = canvas.indexOfChild(nextTextView)
        if(indexnext == indexCur){
            val x1 = curTextView!!.x
            val x2 = curTextView.x + curTextView.width
            val y1 = curTextView.y + curTextView.height / 2
            val y2 = y1
            val x_cot = (x1+x2)/2f
            var y_cot = y1
            if(up_down){
                y_cot = y_cot - curTextView.height / 2 - offsetStatesPosUp[state]!!
                offsetStatesPosUp[state] = offsetStatesPosUp[state]!! + 80
            }else{
                y_cot += curTextView.height / 2 + offsetStatesPosDown[state]!!
                offsetStatesPosDown[state] = offsetStatesPosDown[state]!! + 80
            }
            arrow.setCoords(x1, y1)
            arrow.setCoords(x_cot, y_cot)
            arrow.setCoords(x2, y2)
        }else {
//        get the position of state
            val x_coordCur = curTextView!!.x + curTextView.width / 2
            val y_coordCur = curTextView.y + curTextView.height / 2
            //get the position of ext state
            val x_coordnext = nextTextView!!.x + nextTextView.width / 2
            val y_coordnext = nextTextView.y + nextTextView.height / 2
//        get the control point
//            val canvasHeight = canvas.height
            val width = abs(x_coordCur - x_coordnext)
            val x_coordCot = min(x_coordCur, x_coordnext) + width / 2
            var y_coordCot = Random.nextInt(40, y_coordnext.toInt()-curTextView.height/2-40).toFloat()
            if(!up_down){
                y_coordCot += y_coordCot + curTextView.height/2
            }
            arrow.setCoords(x_coordCur, y_coordCur)
            arrow.setCoords(x_coordCot, y_coordCot)
            arrow.setCoords(x_coordnext, y_coordnext)
        }
        //add text to arrow
        arrow.setTextViewText("S=$state,A=$action\nS'=$nextState,P=$prob,R=$reward")
        //add to layout
        arrowHelper.addView(arrow)
        up_down = !up_down
    }



    private fun animate(event:MotionEvent){
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Button is touched down, change the background color to highlight color
                addStateButton.setBackgroundResource(R.drawable.rounded_button_no_border)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Button touch is lifted or canceled, restore the original background color
                addStateButton.setBackgroundResource(R.drawable.rounded_button)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//test starts
//        setContentView(R.layout.test)
//        // Get a reference to the custom view
//        val semicircleArcView = findViewById<CurvedArrowView>(R.id.semicircleArcView)
//        val button1 = findViewById<Button>(R.id.myButton1)
////        val button5 = findViewById<Button>(R.id.myButton5)
//        val locationOnScreen1 = IntArray(2)
////        val locationOnScreen5 = IntArray(2)
//        // Add a global layout listener to wait for the button's layout to be computed
//        button1.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                // Remove the listener to avoid multiple calls
//                button1.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                button1.getLocationOnScreen(locationOnScreen1)
//                semicircleArcView.setButtonCoordinates(0.0f,1800f,0.0f)
//            }
//        })
        // Set the coordinates of the two buttons and draw the arc
//        semicircleArcView.setButtonCoordinates(locationOnScreen1[0].toFloat(), locationOnScreen5[0].toFloat(), locationOnScreen1[1].toFloat())
//        Log.i("ma","${locationOnScreen1[0].toFloat()}, ${locationOnScreen5[0].toFloat()}, ${locationOnScreen1[1].toFloat()}")
//        setContentView(R.layout.test2)
//        val arcDrawingView = findViewById<ArcDrawingView>(R.id.arcDraw)
//        arcDrawingView.setCoords(0.0f,809f)
//        arcDrawingView.setCoords(289f,0f)
//        arcDrawingView.setCoords(550f,809f)
//        return
//        test eds
        canvas = findViewById(R.id.canvas)
        hiddenControls = findViewById(R.id.hidden_controls)
        controls = findViewById(R.id.controls)
        arrowHelper = findViewById(R.id.arrow_helper)
        deleteStateButton = findViewById(R.id.delete_state)
        editStateButton = findViewById(R.id.edit_state)
        addActionStateButton = findViewById(R.id.add_action_state)
        addModelStateButton = findViewById(R.id.add_model_state)
        addStateButton = findViewById(R.id.add_state)
        offsetStatesPosUp = mutableMapOf()
        offsetStatesPosDown = mutableMapOf()
        val getPT = findViewById<Button>(R.id.get_pt)
        getPT.setOnClickListener{
            if(validateDistribution()){
                val intent = Intent(this, MainActivity2::class.java)
                intent.putExtra("states",states as java.io.Serializable)
                intent.putExtra("stateActionMap",stateActionMap as java.io.Serializable)
                intent.putExtra("model",model as java.io.Serializable)
                startActivity(intent)
            }else{
                Toast.makeText(this,"Illegal Distribution! Recheck!",Toast.LENGTH_SHORT).show()
            }
        }
        addStateButton.setOnClickListener{
            getInputFromPrompt("Enter label","add",null)
        }
        addStateButton.setOnTouchListener { _, event ->
            animate(event)
            false // Return false to allow the event to continue propagating.
        }

    }
}