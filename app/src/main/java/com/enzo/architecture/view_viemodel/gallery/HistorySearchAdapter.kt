package com.enzo.architecture.view_viemodel.gallery

import android.app.SearchManager
import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cursoradapter.widget.ResourceCursorAdapter
import com.enzo.architecture.databinding.ItemSearchHistoryListBinding

class HistorySearchAdapter(context: Context, resource: Int, cursor: Cursor?) : ResourceCursorAdapter(context, resource, cursor) {


    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup): View {

        val binding = ItemSearchHistoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ViewHolder(binding)
        binding.root.tag = viewHolder

        return binding.root
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val viewHolder = view.tag as ViewHolder
        val str = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))
        viewHolder.searchStr.text = str
    }

    fun getSuggestionText(position: Int): String? {
        if (position >= 0 && position < cursor.count) {
            val cursor = cursor
            cursor.moveToPosition(position)
            return cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))
        }
        return null
    }

    class ViewHolder(binding: ItemSearchHistoryListBinding) {
        var searchStr: TextView = binding.searchStr
    }

}


