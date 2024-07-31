package com.example.mdp.adapters

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mdp.NRP
import com.example.mdp.R

class TableAdapter(private val states: MutableSet<String>,private val stateActionMap:MutableMap<String,MutableSet<String>>,private val model:MutableMap<String,MutableMap<String,MutableSet<NRP>>>) : RecyclerView.Adapter<TableAdapter.ViewHolder>() {
    private var statesList:List<String>
    init {
        statesList = states.toList()
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stateView:TextView = itemView.findViewById(R.id.state_text_view)
        val actionsListView:LinearLayout = itemView.findViewById(R.id.actions_list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_table, parent, false)
        return ViewHolder(itemView)
    }
    fun createTextView(context:Context,text:String):TextView{
        // Create a TextView
        val textView = TextView(context)
        // Set the width and height
        val layoutParams = LinearLayout.LayoutParams(
            0, // Width
            LinearLayout.LayoutParams.MATCH_PARENT// Height
        )
        layoutParams.weight = 1.0f // Set layout weight
        layoutParams.setMargins(4, 4, 4, 4)
        textView.layoutParams = layoutParams

        // Set the text and text size
        textView.text = text
        textView.textSize = 16f // Text size in SP (scaled pixels)

        // Set padding
        val paddingInDp = context.resources.getDimensionPixelSize(R.dimen.your_padding_dimension) // Define your padding dimension in resources
        textView.setPadding(paddingInDp, paddingInDp, paddingInDp, paddingInDp)

        // Set gravity
        textView.gravity = Gravity.CENTER_VERTICAL
        textView.setBackgroundColor(Color.parseColor("#F1F1F1"))
        textView.setTextColor(Color.BLACK)
        return textView
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        //must set up etire view here for oe state, at states[positio]
        val state:String = statesList[position]
        holder.stateView.text = state
        holder.stateView.setBackgroundColor(Color.parseColor("#F1F1F1"))
        holder.stateView.setTextColor(Color.BLACK)
        for(action in stateActionMap[state]!!){
            val actionsListHorizontal = LinearLayout(context)
            val layoutParams1 = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // Width
                LinearLayout.LayoutParams.WRAP_CONTENT // Height
            )
            actionsListHorizontal.layoutParams = layoutParams1
            actionsListHorizontal.orientation = LinearLayout.HORIZONTAL


            val nrpListVertical = LinearLayout(context)
            val layoutParams = LinearLayout.LayoutParams(
                0, // Width
                LinearLayout.LayoutParams.WRAP_CONTENT // Height
            )
            layoutParams.weight = 4f
            nrpListVertical.layoutParams = layoutParams
            nrpListVertical.orientation = LinearLayout.VERTICAL
            for(nrp in model[state]!![action]!!){
                //create a liear layout for rp
                val nrpLinearLayout = LinearLayout(context)
                // Set the width and height
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, // Width
                    LinearLayout.LayoutParams.WRAP_CONTENT // Height
                )
                nrpLinearLayout.layoutParams = layoutParams
                // Set the orientation
                nrpLinearLayout.orientation = LinearLayout.HORIZONTAL
                val nextStateTextView = createTextView(context,nrp.nextState)
                val probTextView = createTextView(context,nrp.prob.toString())
                val rewardTextView = createTextView(context,nrp.reward.toString())
                nrpLinearLayout.addView(nextStateTextView)
                nrpLinearLayout.addView(probTextView)
                nrpLinearLayout.addView(rewardTextView)

                nrpListVertical.addView(nrpLinearLayout)
            }
            //create a text view ad liear layout
            val actionTextView = createTextView(context,action)
            actionsListHorizontal.addView(actionTextView)
            actionsListHorizontal.addView(nrpListVertical)

            holder.actionsListView.addView(actionsListHorizontal)
        }
    }

    override fun getItemCount(): Int {
        return statesList.size
    }
}