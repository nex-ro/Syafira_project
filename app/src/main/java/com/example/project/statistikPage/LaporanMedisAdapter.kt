import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project.Data.Laporan_Penanganan
import com.example.project.R
import com.example.project.databinding.ItemLaporanMedisBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class LaporanMedisAdapter(
    private var data: List<Laporan_Penanganan>
) : RecyclerView.Adapter<LaporanMedisAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemLaporanMedisBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(laporan: Laporan_Penanganan) {
            binding.namaPasien.text = laporan.nama_Pasien
            binding.namaRuangan.text = laporan.nama_Ruangan
            binding.penyakit.text = laporan.penyakit
            binding.status.text = laporan.status
            binding.jenisRuangan.text = laporan.jenis ?: "Jenis tidak diketahui"

            val timestamp = laporan.tanggal_Masuk
            val formattedDate = if (timestamp != null && timestamp > 0) {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                sdf.format(Date(timestamp))
            } else {
                Log.w("TanggalError", "Tanggal tidak valid untuk laporan: ${laporan.nama_Pasien}")
                "Tanggal tidak tersedia"
            }
            binding.tanggalMasuk.text = formattedDate

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLaporanMedisBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun updateData(newData: List<Laporan_Penanganan>) {
        val oldSize = data.size
        data = newData
        notifyItemRangeChanged(0, oldSize) // Atau notifyItemRangeInserted sesuai kebutuhan
    }

}
