package io.github.gdimitriu.droidcontrolcenter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView

class CustomAdapter(private val dataSet: ArrayList<*>, mContext: Context) :
    ArrayAdapter<Any?>(mContext, R.layout.raw_item, dataSet) {

    private class ViewHolder {
        lateinit var txtName: TextView
        lateinit var checkBox: CheckBox
    }
    override fun getCount(): Int {
        return dataSet.size
    }

    override fun getItem(position: Int): DataModel {
        return dataSet[position] as DataModel
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView
        val viewHolder: ViewHolder
        val result: View
        if (convertView == null) {
            viewHolder = ViewHolder()
            convertView =
                LayoutInflater.from(parent.context).inflate(R.layout.raw_item, parent, false)
            viewHolder.txtName =
                convertView.findViewById(R.id.list_txtName)
            viewHolder.checkBox =
                convertView.findViewById(R.id.list_checkBox)
            result = convertView
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
            result = convertView
        }

        val item: DataModel = getItem(position)
        viewHolder.txtName.text = item.command
        viewHolder.checkBox.isChecked = item.checked
        return result
    }
}