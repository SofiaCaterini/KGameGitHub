package it.polito.kgame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ItemAdapter() : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
    private var data: List<Item> = emptyList()
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val nome: TextView = v.findViewById(R.id.nickname);

        fun bind(item:Item) {
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
    fun setData(_data:List<Item>){
        data = _data;
        notifyDataSetChanged()}

}