package com.example.project.statistikPage
import android.app.AlertDialog
import android.graphics.Color
import android.util.Log
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
    private val notificationBadge: TextView
) {
    private data class NotificationData(
        val message: String,
        val timestamp: Long,
        val roomStatuses: List<RoomStatus>
    )
    private lateinit var database: DatabaseReference
    private val notificationHistory = mutableMapOf<String, NotificationData>()
    private lateinit var notificationsRef: DatabaseReference
    private var lastNotificationTime = 0L
    private val notifiedRooms = mutableSetOf<String>()
    private var notificationListener: ValueEventListener? = null
    private var capacityListener: ValueEventListener? = null
    private var isInitialized = false

    fun initialize() {
        if (isInitialized) return

        try {
            database = FirebaseDatabase.getInstance().reference
            notificationsRef = database.child("notifications")
            setupFloatingActionButton()
            monitorRoomCapacity()
            isInitialized = true
        } catch (e: Exception) {
            Log.e("NotificationSystem", "Failed to initialize: ${e.message}")
        }
    }

    fun cleanup() {
        notificationListener?.let { listener ->
            notificationsRef.removeEventListener(listener)
        }
        capacityListener?.let { listener ->
            database.child("Ruangan").removeEventListener(listener)
        }
        notifiedRooms.clear()
    }

    private fun setupFloatingActionButton() {
        try {
            fab.visibility = View.VISIBLE
            fab.setOnClickListener {
                showNotifications()
            }

            if (notificationListener == null) {
                notificationListener = notificationsRef.orderByChild("isRead")
                    .equalTo(false)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!fragment.isAdded) return

                            val count = snapshot.childrenCount.toInt()
                            fragment.activity?.runOnUiThread {
                                updateNotificationBadge(count)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            if (!fragment.isAdded) return
                            showError("Failed to update notification badge: ${error.message}")
                        }
                    })
            }
        } catch (e: Exception) {
            Log.e("NotificationSystem", "Error setting up FAB: ${e.message}")
        }
    }

    private fun updateNotificationBadge(count: Int) {
        try {
            notificationBadge.apply {
                visibility = if (count > 0) View.VISIBLE else View.GONE
                text = count.toString()
            }
            fab.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e("NotificationSystem", "Error updating badge: ${e.message}")
        }
    }

    private fun monitorRoomCapacity() {
        if (capacityListener == null) {
            capacityListener = database.child("Ruangan").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastNotificationTime < MIN_NOTIFICATION_INTERVAL) {
                        return
                    }

                    val highOccupancyRooms = mutableListOf<RoomStatus>()

                    snapshot.children.forEach { roomSnapshot ->
                        val isi = roomSnapshot.child("isi").getValue(Int::class.java) ?: 0
                        val kapasitas = roomSnapshot.child("kapasitas").getValue(Int::class.java) ?: 0
                        val namaRuangan = roomSnapshot.child("nama_Ruangan").getValue(String::class.java) ?: ""

                        if (kapasitas > 0) {
                            val occupancyPercentage = (isi.toFloat() / kapasitas.toFloat()) * 100
                            if (occupancyPercentage >= 85) {
                                highOccupancyRooms.add(RoomStatus(namaRuangan, occupancyPercentage.toInt()))
                            }
                        }
                    }

                    if (highOccupancyRooms.isNotEmpty() && !isDuplicateNotification(highOccupancyRooms)) {
                        createAggregatedNotification(highOccupancyRooms)
                        lastNotificationTime = currentTime
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Failed to monitor room capacity: ${error.message}")
                }
            })
        }
    }

    private fun isDuplicateNotification(currentRooms: List<RoomStatus>): Boolean {
        val currentTime = System.currentTimeMillis()
        val twoHoursInMillis = 2 * 60 * 60 * 1000 // 2 hours in milliseconds

        // Create a key for the current room statuses
        val currentKey = createNotificationKey(currentRooms)

        // Check if we have a similar notification within the last 2 hours
        val existingNotification = notificationHistory[currentKey]
        if (existingNotification != null) {
            val timeDifference = currentTime - existingNotification.timestamp
            if (timeDifference < twoHoursInMillis) {
                return true // Duplicate notification within 2 hours
            }
        }

        // Clean up old entries
        notificationHistory.entries.removeIf { entry ->
            currentTime - entry.value.timestamp > twoHoursInMillis
        }

        // Add current notification to history
        val notificationMessage = createNotificationMessage(currentRooms)
        notificationHistory[currentKey] = NotificationData(
            message = notificationMessage,
            timestamp = currentTime,
            roomStatuses = currentRooms
        )

        return false
    }
    private fun createNotificationKey(rooms: List<RoomStatus>): String {
        // Create a unique key based on room names and occupancy levels
        return rooms.sortedBy { it.name }
            .joinToString("|") { "${it.name}:${it.occupancy}" }
    }

    private fun createNotificationMessage(rooms: List<RoomStatus>): String {
        val message = StringBuilder("Beberapa ruangan memiliki tingkat hunian tinggi:\n\n")
        rooms.forEach { room ->
            message.append("• ${room.name}: ${room.occupancy}% kapasitas\n")
        }
        return message.toString().trim()
    }




    private fun createAggregatedNotification(rooms: List<RoomStatus>) {
        val notificationMessage = StringBuilder("Beberapa ruangan memiliki tingkat hunian tinggi:\n\n")
        rooms.forEach { room ->
            notificationMessage.append("• ${room.name}: ${room.occupancy}% kapasitas\n")
        }

        val notification = hashMapOf(
            "rooms" to rooms.map { room ->
                mapOf(
                    "name" to room.name,
                    "occupancy" to room.occupancy
                )
            },
            "timestamp" to ServerValue.TIMESTAMP,
            "message" to notificationMessage.toString().trim(),
            "isRead" to false
        )

        notificationsRef.push().setValue(notification)
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
                        val notification = parseNotification(notificationSnapshot)
                        notification?.let { notifications.add(it) }
                        notificationSnapshot.ref.child("isRead").setValue(true)
                    }

                    showCustomNotificationDialog(notifications.sortedByDescending { it.timestamp })
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Failed to fetch notifications: ${error.message}")
                }
            })
    }

    private fun parseNotification(snapshot: DataSnapshot): NotificationItem? {
        val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: return null
        val notificationId = snapshot.key ?: return null
        val message = snapshot.child("message").getValue(String::class.java) ?: "Tidak ada pesan."

        val roomsList = mutableListOf<RoomStatus>()
        snapshot.child("rooms").children.forEach { roomSnapshot ->
            val name = roomSnapshot.child("name").getValue(String::class.java) ?: ""
            val occupancy = roomSnapshot.child("occupancy").getValue(Int::class.java) ?: 0
            roomsList.add(RoomStatus(name, occupancy))
        }

        return NotificationItem(notificationId, roomsList, timestamp, message)
    }

    private fun showCustomNotificationDialog(notifications: List<NotificationItem>) {
        val builder = AlertDialog.Builder(fragment.requireContext(), R.style.CustomAlertDialogTheme)
        val view = LayoutInflater.from(fragment.requireContext())
            .inflate(R.layout.dialog_notifications, null)

        val containerLayout = view.findViewById<LinearLayout>(R.id.notificationsContainer)
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id"))

        notifications.forEach { notification ->
            val itemView = createNotificationItemView(notification, dateFormat, containerLayout)
            containerLayout.addView(itemView)
        }

        builder.setView(view)
            .setTitle("Peringatan Kapasitas Ruangan")
            .setPositiveButton("Tutup", null)
            .setNeutralButton("Hapus Semua") { _, _ ->
                deleteAllNotifications()
            }

        builder.create().show()
    }

    private fun createNotificationItemView(
        notification: NotificationItem,
        dateFormat: SimpleDateFormat,
        containerLayout: LinearLayout
    ): View {
        val itemView = LayoutInflater.from(fragment.requireContext())
            .inflate(R.layout.item_notification, null)

        val timeText = itemView.findViewById<TextView>(R.id.timeText)
        val contentText = itemView.findViewById<TextView>(R.id.contentText)
        val deleteButton = itemView.findViewById<TextView>(R.id.deleteButton)

        timeText.text = dateFormat.format(Date(notification.timestamp))

        // Directly use the message as it's already formatted properly
        contentText.text = notification.message

        deleteButton.setOnClickListener {
            deleteNotification(notification.id) {
                containerLayout.removeView(itemView)
                if (containerLayout.childCount == 0) {
                    showMessage("Semua notifikasi telah dihapus")
                }
            }
        }

        return itemView
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
        val timestamp: Long,
        val message: String // Tambahkan properti ini
    )

    companion object {
        private const val MIN_NOTIFICATION_INTERVAL = 5 * 60 * 1000 // 5 minutes in milliseconds
    }

}