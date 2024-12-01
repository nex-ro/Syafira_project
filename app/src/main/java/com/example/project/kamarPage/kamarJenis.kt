package com.example.project.kamarPage
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.project.databinding.FragmentKamarJenisBinding
import com.google.firebase.database.*
import com.example.project.Data.Ruangan


class kamarJenis : Fragment() {

    private lateinit var binding: FragmentKamarJenisBinding
    private lateinit var ref: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentKamarJenisBinding.inflate(inflater, container, false)

        ref = FirebaseDatabase.getInstance().getReference("Ruangan")

        val jenis = arguments?.getString(ARG_JENIS)
        if (jenis != null) {
            fetchRuanganByJenis(jenis)
        }

        return binding.root
    }

    private fun fetchRuanganByJenis(jenis: String) {
        ref.orderByChild("jenis").equalTo(jenis)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val hasil = StringBuilder()
                    for (ruanganSnapshot in snapshot.children) {
                        val ruangan = ruanganSnapshot.getValue(Ruangan::class.java)
                        if (ruangan != null) {
                            hasil.append(
                                "Nama: ${ruangan.nama_Ruangan}\n" +
                                        "Nomor: ${ruangan.nomor_Ruangan}\n" +
                                        "Status: ${ruangan.status}\n\n"
                            )
                        }
                    }
                    // Tampilkan hasil pada textViewJenis
                    if (hasil.isNotEmpty()) {
                        binding.textdeskripsiJenis.text = hasil.toString()
                    } else {
                        binding.textdeskripsiJenis.text = "Tidak ada data ruangan untuk jenis ini."
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Tangani error
                    binding.textdeskripsiJenis.text = "Gagal memuat data: ${error.message}"
                }
            })
    }

    companion object {
        private const val ARG_JENIS = "jenis"

        fun newInstance(jenis: String): kamarJenis {
            val fragment = kamarJenis()
            val args = Bundle().apply {
                putString(ARG_JENIS, jenis)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
