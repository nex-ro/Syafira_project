package com.example.project.statistikPage
import android.app.AlertDialog
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.project.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class RoomNotificationSystem(
    private val fragment: Fragment,
    private val fab: FloatingActionButton,
    private val badge: TextView
) {
    private lateinit var database: DatabaseReference
    private lateinit var notificationsRef: DatabaseReference
    private var lastNotificationTime = 0L
    private val notifiedRooms = mutableSetOf<String>()
    private var notificationListener: ValueEventListener? = null

    fun initialize() {
        database = FirebaseDatabase.getInstance().reference
        notificationsRef = database.child("notifications")
        setupFloatingActionButton()
        monitorRoomCapacity()
    }
    fun cleanup() {
        // Remove listener when fragment is destroyed
        notificationListener?.let { listener ->
            notificationsRef.removeEventListener(listener)
        }
    }
    private fun setupFloatingActionButton() {
        fab.setOnClickListener {
            showNotifications()
        }

        // Keep listening for unread notifications
        notificationListener = notificationsRef.orderByChild("isRead")
            .equalTo(false)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!fragment.isAdded) return // Check if fragment is still attached

                    val count = snapshot.childrenCount.toInt()
                    badge.apply {
                        visibility = if (count > 0) View.VISIBLE else View.GONE
                        text = count.toString()
                    }
                    fab.visibility = View.VISIBLE
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!fragment.isAdded) return
                    showError("Failed to update notification badge: ${error.message}")
                }
            })
    }

    private fun monitorRoomCapacity() {
        database.child("Ruangan").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Only check capacity if enough time has passed since last notification
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastNotificationTime < MIN_NOTIFICATION_INTERVAL) {
                    return
                }

                val highOccupancyRooms = mutableListOf<RoomStatus>()
                val currentNotifiedRooms = mutableSetOf<String>()

                snapshot.children.forEach { roomSnapshot ->
                    val isi = roomSnapshot.child("isi").getValue(Int::class.java) ?: 0
                    val kapasitas = roomSnapshot.child("kapasitas").getValue(Int::class.java) ?: 0
                    val namaRuangan = roomSnapshot.child("nama_Ruangan").getValue(String::class.java) ?: ""

                    if (kapasitas > 0) {
                        val occupancyPercentage = (isi.toFloat() / kapasitas.toFloat()) * 100
                        if (occupancyPercentage >= 85) {
                            // Only add rooms that haven't been notified yet
                            if (!notifiedRooms.contains(namaRuangan)) {
                                highOccupancyRooms.add(RoomStatus(namaRuangan, occupancyPercentage.toInt()))
                                currentNotifiedRooms.add(namaRuangan)
                            }
                        } else {
                            // Remove from notified rooms if occupancy drops below threshold
                            notifiedRooms.remove(namaRuangan)
                        }
                    }
                }

                if (highOccupancyRooms.isNotEmpty()) {
                    createAggregatedNotification(highOccupancyRooms)
                    notifiedRooms.addAll(currentNotifiedRooms)
                    lastNotificationTime = currentTime
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showError("Failed to monitor room capacity: ${error.message}")
            }
        })
    }


    private fun showNotifications() {
        notificationsRef.orderByChild("timestamp").limitToLast(10)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        showMessage("Tidak ada notifikasi baru")
                        return
                    }

                    val notifications = mutableListOf<NotificationItem>()
                    snapshot.children.forEach { notificationSnapshot ->
                        val timestamp = notificationSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L

                        // Get notification ID
                        val notificationId = notificationSnapshot.key ?: ""

                        // Parse rooms data
                        val roomsList = mutableListOf<RoomStatus>()
                        notificationSnapshot.child("rooms").children.forEach { roomSnapshot ->
                            val name = roomSnapshot.child("name").getValue(String::class.java) ?: ""
                            val occupancy = roomSnapshot.child("occupancy").getValue(Int::class.java) ?: 0
                            roomsList.add(RoomStatus(name, occupancy))
                        }

                        notifications.add(NotificationItem(notificationId, roomsList, timestamp))

                        // Mark as read
                        notificationSnapshot.ref.child("isRead").setValue(true)
                    }

                    showCustomNotificationDialog(notifications.sortedByDescending { it.timestamp })
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Failed to fetch notifications: ${error.message}")
                }
            })
    }

    // Update createAggregatedNotification to match the data structure
    private fun createAggregatedNotification(rooms: List<RoomStatus>) {
        val notification = hashMapOf(
            "rooms" to rooms.map { room ->
                mapOf(
                    "name" to room.name,
                    "occupancy" to room.occupancy
                )
            },
            "timestamp" to ServerValue.TIMESTAMP,
            "isRead" to false
        )

        notificationsRef.push().setValue(notification)
    }

    private fun showCustomNotificationDialog(notifications: List<NotificationItem>) {
        val builder = AlertDialog.Builder(fragment.requireContext())
        val view = LayoutInflater.from(fragment.requireContext())
            .inflate(R.layout.dialog_notifications, null)

        val containerLayout = view.findViewById<LinearLayout>(R.id.notificationsContainer)
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id"))

        notifications.forEach { notification ->
            val itemView = LayoutInflater.from(fragment.requireContext())
                .inflate(R.layout.item_notification, null)

            val timeText = itemView.findViewById<TextView>(R.id.timeText)
            val contentText = itemView.findViewById<TextView>(R.id.contentText)
            val deleteButton = itemView.findViewById<TextView>(R.id.deleteButton)

            timeText.text = dateFormat.format(Date(notification.timestamp))

            val contentBuilder = StringBuilder()
            notification.rooms.forEach { room ->
                contentBuilder.append("${room.name} telah mencapai ${room.occupancy}% kapasitas\n")
            }
            contentText.text = contentBuilder.toString().trim()

            // Setup delete button
            deleteButton.setOnClickListener {
                deleteNotification(notification.id) {
                    containerLayout.removeView(itemView)
                    if (containerLayout.childCount == 0) {
                        showMessage("Semua notifikasi telah dihapus")
                    }
                }
            }

            containerLayout.addView(itemView)
        }

        // Add "Delete All" button
        builder.setView(view)
            .setTitle("Peringatan Kapasitas Ruangan")
            .setPositiveButton("Tutup", null)
            .setNeutralButton("Hapus Semua") { _, _ ->
                deleteAllNotifications()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteNotification(notificationId: String, onSuccess: () -> Unit) {
        notificationsRef.child(notificationId).removeValue()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                showError("Gagal menghapus notifikasi")
            }
    }

    private fun deleteAllNotifications() {
        notificationsRef.removeValue()
            .addOnSuccessListener {
                showMessage("Semua notifikasi telah dihapus")
            }
            .addOnFailureListener {
                showError("Gagal menghapus semua notifikasi")
            }
    }

    private fun showMessage(message: String) {
        Snackbar.make(fragment.requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showError(error: String) {
        Snackbar.make(fragment.requireView(), error, Snackbar.LENGTH_LONG)
            .setBackgroundTint(fragment.resources.getColor(android.R.color.holo_red_light, null))
            .show()
    }


    // Data class for room status
    private data class RoomStatus(val name: String, val occupancy: Int)

    // Data class for notification items
    private data class NotificationItem(
        val id: String,
        val rooms: List<RoomStatus>,
        val timestamp: Long
    )

    companion object {
        private const val MIN_NOTIFICATION_INTERVAL = 5 * 60 * 1000 // 5 minutes in milliseconds
    }
}