package it.polito.kgame.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.kgame.Pedina
import it.polito.kgame.R
import it.polito.kgame.User


class ItemAdapterUsers() : RecyclerView.Adapter<ItemAdapterUsers.ViewHolder>() {
    private var data: List<User> = emptyList()
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val num: TextView = v.findViewById(R.id.numero)
        private val nick: TextView = v.findViewById(R.id.nickname2)
        private val ped: ImageView = v.findViewById(R.id.pedina)

        fun bind(item: User) {
            num.text = item.position.toString()
            nick.text = item.username
            ped.setImageResource(Pedina.pedina(item.pawnCode))
        }
        fun unbind(){
            num.setOnClickListener(null)
            nick.setOnClickListener(null)
            ped.setOnClickListener(null)

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.lista_account_layout,
                        parent,
                        false)
        );
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }
    override fun getItemCount() = data.size;

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }
    fun sortItems() {
        val newData = data.sortedBy{ it.position}
        data = newData
        notifyDataSetChanged()
    }
    fun setData(_data:List<User>){
        data = _data;
        notifyDataSetChanged()}

}