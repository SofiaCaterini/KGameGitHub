package it.polito.kgame.ui.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.polito.kgame.DbManager
import it.polito.kgame.EventoInfo
import it.polito.kgame.R
import kotlinx.android.synthetic.main.fragment_calendar.view.*
import kotlinx.coroutines.awaitAll
import java.util.*

class EventAdapter(var datee: List<EventoInfo>) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {
    //private var ev: List<EventoInfo> = emptyList()
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val dt: TextView = v.findViewById(R.id.dettagli_data)
        private val tit: TextView = v.findViewById(R.id.dettagli_titolo)
        private val h: TextView = v.findViewById(R.id.dettagli_ora)
        private val desc: TextView = v.findViewById(R.id.dettagli_descrizione)
        private val luo: TextView = v.findViewById(R.id.dettagli_luogo)
        val butt: View = v.findViewById(R.id.deletedettagli)
        val vista = v
        val contesto = v.context

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
        //var removedPosition : Int? = null
        holder.bind(datee[position])
        holder.butt.setOnClickListener {

            MaterialAlertDialogBuilder(holder.contesto)
                    .setTitle("Elimina impegno")
                    .setMessage("Sei sicuro di voler eliminare questo impegno?")
                    .setPositiveButton("Si") { _, _ ->
                        //holder.vista.rvdettagli?.isVisible = false

                        val list = datee.toMutableList()
                        list.removeAt(position)
                        notifyDataSetChanged()
                        holder.itemView.visibility = (View.GONE)
                        DbManager.deleteAppointment(datee[position])
                        Toast.makeText(
                                holder.contesto, "Impegno eliminato correttamente ",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton("No"){  _, _ ->

                    }
                    .show()

        }
    }
    override fun getItemCount() = datee.size;

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }
    /*fun getRemovedItemPosition() : Int {
        var position = removedPosition
        return position
    }*/

    fun deleteEvento(eve: EventoInfo) {
        val list = datee.toMutableList()
        list.remove(eve)
        notifyDataSetChanged()
    }
    //Trovare posizione dell'elemento dentro la lista e rimuovere quella posizione

/*fun deleteItem(index:Int){
    val list = datee.toMutableList()
    list.removeAt(index)
    notifyDataSetChanged()
}

fun setData(_data:st<EventoInfo>){
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