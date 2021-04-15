package it.polito.kgame.ui.calendar
/*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.applandeo.materialcalendarview.EventDay
import it.polito.kgame.R
import java.util.*

class EventAdapter() : RecyclerView.Adapter<EventAdapter.ViewHolder>() {
    private var ev: List<EventDay> = emptyList()
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tit: TextView = v.findViewById(R.id.lista_titolo)
        private val dt: TextView = v.findViewById(R.id.lista_data)
        private val h: TextView = v.findViewById(R.id.lista_ora)

        fun bind(item: EventDay) {
            dt.text = item.calendar.get(Calendar.DAY_OF_MONTH).toString()

        }
        fun unbind(){
            tit.setOnClickListener(null)
            dt.setOnClickListener(null)
            h.setOnClickListener(null)

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.`anteprima_evento.xml`,
                        parent,
                        false)
        );
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(ev[position])
    }
    override fun getItemCount() = ev.size;

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    fun setData(_data:List<EventDay>){
        ev = _data;
        notifyDataSetChanged()}

}*/