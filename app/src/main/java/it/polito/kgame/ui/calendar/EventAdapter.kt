package it.polito.kgame.ui.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.kgame.EventoInfo
import it.polito.kgame.R
import java.util.*

class EventAdapter(var datee: List<EventoInfo>) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {
    //private var ev: List<EventoInfo> = emptyList()
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val dt: TextView = v.findViewById(R.id.dettagli_data)
        private val tit: TextView = v.findViewById(R.id.dettagli_titolo)
        private val h: TextView = v.findViewById(R.id.dettagli_ora)
        private val desc: TextView = v.findViewById(R.id.dettagli_descrizione)
        private val luo: TextView = v.findViewById(R.id.dettagli_luogo)

        fun bind(item: EventoInfo) {
            dt.text = refactorDate(item.cal!!)
            h.text = refactorTime(item.cal!!)
            tit.text = item.titolo
            desc.text = item.descrizione
            luo.text = item.luogo


        }
        fun unbind(){
            tit.setOnClickListener(null)
            dt.setOnClickListener(null)
            desc.setOnClickListener(null)
            h.setOnClickListener(null)
            luo.setOnClickListener(null)

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.dettagli_impegno,
                        parent,
                        false)
        );
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datee[position])
    }
    override fun getItemCount() = datee.size;

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    /*fun setData(_data:List<EventoInfo>){
        ev = _data;
        notifyDataSetChanged()}*/

}
private fun refactorDate(cal: java.util.Calendar): String {

    val day : String = if(cal.get(java.util.Calendar.DAY_OF_MONTH)>=10) cal.get(java.util.Calendar.DAY_OF_MONTH).toString()
    else  "0" + cal.get(java.util.Calendar.DAY_OF_MONTH)
    val month : String = if (cal.get(java.util.Calendar.MONTH)>=9) (cal.get(java.util.Calendar.MONTH)+1).toString()
    else "0"+ (cal.get(java.util.Calendar.MONTH)+1)

    return day + "/" + month + "/" + cal.get(java.util.Calendar.YEAR)
}
private fun refactorTime(cal: java.util.Calendar?): String {

    val hour : String = if(cal?.get(java.util.Calendar.HOUR_OF_DAY)!! >= 10) cal?.get(java.util.Calendar.HOUR_OF_DAY).toString()
    else  "0" + cal?.get(java.util.Calendar.HOUR_OF_DAY)
    val minute : String = if (cal.get(java.util.Calendar.MINUTE)>=10) (cal.get(java.util.Calendar.MINUTE)).toString()
    else "0"+ (cal.get(java.util.Calendar.MINUTE))

    return hour + ":" + minute
}