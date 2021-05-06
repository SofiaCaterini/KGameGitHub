package it.polito.kgame.ui.account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import it.polito.kgame.R
import it.polito.kgame.User
import kotlinx.android.synthetic.main.fragment_account.*


class ItemAdapterComponentsFamily() : RecyclerView.Adapter<ItemAdapterComponentsFamily.ViewHolder>() {
    private var data: List<User> = emptyList()

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val nome: TextView = v.findViewById(R.id.nickname2)
        private val imgprofilo: ImageView = v.findViewById(R.id.img)

        fun bind(item: User) {
            nome.text = item.username
            Picasso.get().load(item.profileImg).fit().into(imgprofilo)
            //imgprofilo.setImageResource(R.drawable.lion)
        }
        fun unbind(){
            nome.setOnClickListener(null)

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.lista_componenti_famiglie_layout,
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
    fun setData(_data:List<User>){
        data = _data
        notifyDataSetChanged()}

}