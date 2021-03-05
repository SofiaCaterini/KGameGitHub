package it.polito.kgame.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.kgame.R


class ItemAdapterUsers() : RecyclerView.Adapter<ItemAdapterUsers.ViewHolder>() {
    private var data: List<ItemUsers> = emptyList()
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val num: TextView = v.findViewById(R.id.numero)
        private val nick: TextView = v.findViewById(R.id.nickname2)
        private val ped: ImageView = v.findViewById(R.id.pedina)

        fun bind(item: ItemUsers) {
            num.text = item.posizione.toString()
            nick.text = item.nome
            ped.setImageResource(item.icona)
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
    fun sortPosizione() {
        val newData = data.sortedBy{ it.posizione}
        data = newData
        notifyDataSetChanged()
    }
    fun setData(_data:List<ItemUsers>){
        data = _data;
        notifyDataSetChanged()}

}