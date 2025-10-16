package com.example.myapplication.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.AppNotification
import com.example.myapplication.data.NotificationStore

class NotificationsFragment: Fragment() {

    private lateinit var store: NotificationStore
    private lateinit var rv: RecyclerView
    private lateinit var btnAll: Button
    private lateinit var btnUnread: Button
    private var showUnread = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_notifications, container, false)
        store = NotificationStore(requireContext())
        store.seedIfEmpty()

        rv = v.findViewById(R.id.rvNotif)
        btnAll = v.findViewById(R.id.btnAll)
        btnUnread = v.findViewById(R.id.btnUnread)

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = Adapter(emptyList())

        btnAll.setOnClickListener { showUnread = false; load() }
        btnUnread.setOnClickListener { showUnread = true; load() }

        load()
        return v
    }

    private fun load() {
        val list = if (showUnread) store.unread() else store.all()
        (rv.adapter as Adapter).submit(list)
    }

    inner class Adapter(private var data: List<AppNotification>): RecyclerView.Adapter<VH>() {
        fun submit(list: List<AppNotification>) { data = list; notifyDataSetChanged() }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false)
            return VH(v)
        }
        override fun getItemCount() = data.size
        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(data[position])
    }

    inner class VH(v: View): RecyclerView.ViewHolder(v) {
        private val t1 = v.findViewById<TextView>(android.R.id.text1)
        private val t2 = v.findViewById<TextView>(android.R.id.text2)
        fun bind(n: AppNotification) {
            t1.text = n.title + if (!n.read) " •" else ""
            t2.text = n.message
            itemView.setOnClickListener { store.markRead(n.id); load() }
        }
    }
}