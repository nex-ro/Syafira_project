// TambahKamarDialog.kt
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.project.Data.Ruangan
import com.example.project.databinding.FragmentTambahKamarBinding
import com.google.firebase.database.*

class TambahKamar : DialogFragment() {
    private var _binding: FragmentTambahKamarBinding? = null
    private val binding get() = _binding!!
    private lateinit var ref: DatabaseReference

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahKamarBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        ref = FirebaseDatabase.getInstance().reference.child("Ruangan")
        setupViews()
        return binding.root
    }
    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog?.window?.setLayout(width, height)
    }


    private fun setupViews() {
        // Setup spinner
        val jenisList = listOf("VVIP", "VIP", "Kelas I", "Kelas II", "Kelas III", "Laboratorium", "ICU", "HCU")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, jenisList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerJenis.adapter = adapter

        // Setup buttons
        binding.buttonSimpan.setOnClickListener {
            saveRuangan()
        }

        binding.buttonBatal.setOnClickListener {
            dismiss()
        }
    }

    private fun saveRuangan() {
        val nama = binding.inputNamaRuangan.text.toString().trim()
        val nomor = binding.inputNomorRuangan.text.toString().toIntOrNull()
        val kapasitas = binding.inputKapasitas.text.toString().toIntOrNull()
        val jenis = binding.spinnerJenis.selectedItem.toString()


        if (nama.isEmpty() || nomor == null || kapasitas == null) {
            Toast.makeText(requireContext(), "Mohon input dengan benar", Toast.LENGTH_SHORT).show()
            return
        }

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isDuplicate = false
                for (ruanganSnapshot in snapshot.children) {
                    val existingNama = ruanganSnapshot.child("nama_Ruangan").getValue(String::class.java)
                    if (existingNama != null && existingNama.equals(nama, ignoreCase = true)) {
                        isDuplicate = true
                        break
                    }
                }

                if (isDuplicate) {
                    Toast.makeText(
                        requireContext(),
                        "Nama ruangan sudah ada, silakan gunakan nama lain",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val status = "kosong"
                    val idRuangan = ref.push().key ?: return
                    val ruangan = Ruangan(idRuangan, nomor, jenis, nama, status, kapasitas)

                    ref.child(idRuangan).setValue(ruangan).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Data berhasil ditambahkan",
                                Toast.LENGTH_SHORT
                            ).show()
                            dismiss()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Gagal menambahkan data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Gagal memeriksa data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}