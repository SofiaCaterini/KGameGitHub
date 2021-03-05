package it.polito.kgame.ui.account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.kgame.R


class ItemAdapterFamily() : RecyclerView.Adapter<ItemAdapterFamily.ViewHolder>() {
    private var data: List<ItemFamily> = emptyList()
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val nome: TextView = v.findViewById(R.id.nickname2);

        fun bind(item: ItemFamily) {
            nome.text = item.nomefamiglia
        }
        fun unbind(){
            nome.setOnClickListener(null)

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.lista_famiglie_layout,
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
    fun setData(_data:List<ItemFamily>){
        data = _data;
        notifyDataSetChanged()}

}